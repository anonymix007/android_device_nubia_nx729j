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
#include <cutils/uevent.h>

#include <vendor/goodix/hardware/biometrics/fingerprint/2.1/IGoodixFingerprintDaemon.h>

#include <sensors/UEvent.h>


#define NOTIFY_FINGER_DOWN 1536
#define NOTIFY_FINGER_UP 1537


#define TP_EVENT_PATH "MODALIAS=platform:zte_touch"
#define SINGLE_TAP_GESTURE "single_tap"
#define DOUBLE_TAP_GESTURE "double_tap"
#define AOD_AREAMEET_DOWN "aod_areameet_down"
#define AREAMEET_DOWN "areameet_down"
#define AREAMEET_UP "areameet_up"
#define UEVENT_BUFFER_SIZE 8192

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

    int ParseUevent() {
        android::hardware::sensors::UEvent event(buf);

        if (!event.contains(TP_EVENT_PATH)) {
            return -1;
        }
        LOG(DEBUG) << "Processing zte_touch uevent!";

        if (event.get(AREAMEET_DOWN, "false") == "true" || event.get(AOD_AREAMEET_DOWN, "false") == "true") {
			return NOTIFY_FINGER_DOWN;
		}

		if (event.get(AREAMEET_UP, "false") == "true") {
			return NOTIFY_FINGER_UP;
		}

        return -1;
    }

    bool ReadUevent(int fd, int *command) {
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
