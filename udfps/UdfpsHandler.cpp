/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "UdfpsHandler.nubia"

#include "UdfpsHandler.h"

#include <android-base/logging.h>
#include <log/log.h>
#include <fcntl.h>
#include <poll.h>
#include <thread>
#include <unistd.h>
#include <cstring>

typedef struct fingerprint_device_gf95xx{
    hw_device_t common;
    fingerprint_notify_t notify;
    int (*set_notify)(struct fingerprint_device *dev, fingerprint_notify_t notify);
    uint64_t (*pre_enroll)(struct fingerprint_device *dev);
    int (*enroll)(struct fingerprint_device *dev, const hw_auth_token_t *hat, uint32_t gid, uint32_t timeout_sec);
    int (*post_enroll)(struct fingerprint_device *dev);
    uint64_t (*get_authenticator_id)(struct fingerprint_device *dev);
    int (*cancel)(struct fingerprint_device *dev);
    int (*enumerate)(struct fingerprint_device *dev);
    int (*remove)(struct fingerprint_device *dev, uint32_t gid, uint32_t fid);
    int (*set_active_group)(struct fingerprint_device *dev, uint32_t gid, const char *store_path);
    int (*authenticate)(struct fingerprint_device *dev, uint64_t operation_id, uint32_t gid);
    int (*sendCustomizedCommand)(fingerprint_device_gf95xx *device, uint32_t cmd, uint32_t extras, const char *msg, ssize_t len);
    void *reserved[4];
} fingerprint_device_gf95xx_t;

#define CUSTOMIZED_COMMAND "FingerService"
#define CUSTOMIZED_COMMAND_LEN strlen("FingerService")

class NubiaUdfpsHander : public UdfpsHandler {
  public:
    void init(fingerprint_device_t *device) {
        mDevice = reinterpret_cast<fingerprint_device_gf95xx_t *>(device);
    }

    void onFingerDown(uint32_t x, uint32_t y, float /*minor*/, float /*major*/) {
        if (mDevice->sendCustomizedCommand) {
            mDevice->sendCustomizedCommand(mDevice, 10, 1, CUSTOMIZED_COMMAND, CUSTOMIZED_COMMAND_LEN);
        }
    }

    void onFingerUp() {
        if (mDevice->sendCustomizedCommand) {
            mDevice->sendCustomizedCommand(mDevice, 10, 0, CUSTOMIZED_COMMAND, CUSTOMIZED_COMMAND_LEN);
        }
    }

    void onAcquired(int32_t result, int32_t vendorCode) {
        // nothing
    }

    void cancel() {
        // nothing
    }

  private:
    fingerprint_device_gf95xx_t *mDevice;
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
