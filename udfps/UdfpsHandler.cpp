/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "UdfpsHander.nubia"

#include "UdfpsHandler.h"

#include <android-base/logging.h>
#include <fcntl.h>
#include <poll.h>
#include <thread>
#include <unistd.h>
#include <vendor/goodix/hardware/biometrics/fingerprint/2.1/IGoodixFingerprintDaemon.h>

#define NOTIFY_FINGER_DOWN 1536
#define NOTIFY_FINGER_UP 1537

#define SINGLE_TAP_GESTURE "single_tap=true"
#define DOUBLE_TAP_GESTURE "double_tap=true"
#define AOD_AREAMEET_DOWN "aod_areameet_down=true"
#define AREAMEET_DOWN "areameet_down=true"
#define AREAMEET_UP "areameet_up=true"

#include <cutils/uevent.h>

#define NEW_TP_WAKEUP_EVENT_PATH "MODALIAS=platform:zte_touch"
#define UEVENT_BUFFER_SIZE 4096

using ::android::sp;
using ::android::hardware::hidl_vec;
using ::vendor::goodix::hardware::biometrics::fingerprint::V2_1::IGoodixFingerprintDaemon;

class NubiaUdfpsHander : public UdfpsHandler {
  public:
    void init(fingerprint_device_t *device) {
        mDevice = device;

        mGoodixFingerprintDaemon = IGoodixFingerprintDaemon::getService();

        std::thread([this]() {
            int fd = uevent_open_socket(256 * 1024, false);

            fcntl(fd, F_SETFL, O_NONBLOCK);

            if (fd < 0) {
                LOG(ERROR) << "failed to open fd, err: " << fd;
                return;
            }

            struct pollfd ufd = {
                    .fd = fd,
                    .events = POLLERR | POLLPRI,
                    .revents = 0,
            };

            while (true) {
				int rc = poll(&ufd, 1, -1);
                if (rc < 0) {
                    LOG(ERROR) << "failed to poll fd, err: " << rc;
                    continue;
                }

                if (ufd.revents & POLLIN) {
					int command = -1;
					while(ReadUevent(fd, &command)) {
						if (command > 0) {
						    mGoodixFingerprintDaemon->sendCommand(command, {}, [](int, const hidl_vec<signed char>&) {});
						}
					}
				}
            }
        }).detach();
    }

    void onFingerDown(uint32_t /*x*/, uint32_t /*y*/, float /*minor*/, float /*major*/) {
        // nothing
    }

    void onFingerUp() {
        // nothing
    }

    void onAcquired(int32_t /*result*/, int32_t /*vendorCode*/) {
        // nothing
    }

    void cancel() {
        // nothing
    }

  private:
    fingerprint_device_t *mDevice;
    sp<IGoodixFingerprintDaemon> mGoodixFingerprintDaemon;
    char buf[UEVENT_BUFFER_SIZE + 2];
    std::map<std::string, std::string> msg;

    // Inspired by tri-state-key/uevent_listener.cpp from 1+6
    int ParseUevent() {
		msg.clear();
		char *ptr = buf;

		bool found = false;

		while (*ptr) {
			std::string kv(ptr);
			ptr += kv.length() + 1;

			if (kv.find(NEW_TP_WAKEUP_EVENT_PATH) != std::string::npos) {
				found = true;
			}

			auto pos = kv.find("=");
			if (pos == std::string::npos) {
				LOG(DEBUG) << "WTF is this uevent:" << kv;
			} else {
				std::string key = kv.substr(0, pos);
				std::string value = kv.substr(pos + 1, kv.length());
				msg.insert({key, value});
				LOG(DEBUG) << "Parsed uevent (from '" << kv << "'): " << key << " = " << value;
			}
		}
		if (found) {
			LOG(DEBUG) << "Processed zte_touch uevent!";
			auto fod_down_pos = msg.find(AREAMEET_DOWN);
			if (fod_down_pos != msg.end()) {
				if (fod_down_pos->second == "true") {
					return NOTIFY_FINGER_DOWN;
				}
			}
			auto fod_up_pos = msg.find(AREAMEET_UP);
			if (fod_up_pos != msg.end()) {
				if (fod_up_pos->second == "true") {
					return NOTIFY_FINGER_UP;
				}
			}
		}

		return -1;
	}

    bool ReadUevent(int fd, int *command) {
		//readBool(fd) ? NOTIFY_FINGER_DOWN: NOTIFY_FINGER_UP
        int n = uevent_kernel_multicast_recv(fd, buf, UEVENT_BUFFER_SIZE);
        if (n <= 0) {
            if (errno != EAGAIN && errno != EWOULDBLOCK) {
                LOG(ERROR) << "Error reading from uevent fd";
            }
            return false;
        }
        if (n >= UEVENT_BUFFER_SIZE) {
            LOG(ERROR) << "Uevent overflowed buffer, discarding";
            // Return true here even if we discard as we may have more uevents pending and we
            // want to keep processing them.
            return true;
        }

        buf[n] = '\0';
        buf[n + 1] = '\0';

        *command = ParseUevent();

        return true;
	}
};

static UdfpsHandler* create() {
    return new NubiaUdfpsHander();
}

static void destroy(UdfpsHandler* handler) {
    delete handler;
}

extern "C" UdfpsHandlerFactory UDFPS_HANDLER_FACTORY = {
    .create = create,
    .destroy = destroy,
};
