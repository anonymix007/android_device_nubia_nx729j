/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#include <aidl/vendor/aospa/power/BnPowerFeature.h>
#include <android-base/file.h>
#include <android-base/logging.h>
#include <sys/ioctl.h>

#define WAKEGESTURE_PATH "/proc/touchscreen/wake_gesture"

namespace aidl {
namespace vendor {
namespace aospa {
namespace power {

bool setDeviceSpecificFeature(Feature feature, bool enabled) {
    switch (feature) {
        case Feature::DOUBLE_TAP: {
            int fd = open(WAKEGESTURE_PATH, O_RDWR);
            char v = enabled ? '1' : '0';
            write(fd, &v, sizeof(v));
            close(fd);
            return true;
        }
        default:
            return false;
    }
}

}  // namespace power
}  // namespace aospa
}  // namespace vendor
}  // namespace aidl