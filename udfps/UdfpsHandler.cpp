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


#include "fingerprint_device_nx729j.h"

class NubiaUdfpsHander : public UdfpsHandler {
  public:
    void init(fingerprint_device_t *device) {
        mDevice = reinterpret_cast<fingerprint_device_gf95xx *>(device);
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
    fingerprint_device_gf95xx *mDevice;
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
