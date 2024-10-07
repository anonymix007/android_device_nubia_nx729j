/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "FingerprintEngineNX729J"

#include <android/log.h>
#include <log/log.h>

#include <Session.h>
#include <HwFingerprintEngine.h>
#include <Legacy2Aidl.h>

#include "fingerprint_device_nx729j.h"

namespace aidl::android::hardware::biometrics::fingerprint {

constexpr int32_t kSensorPositionCenterR = 94;
constexpr int32_t kSensorPositionCenterX = 558;
constexpr int32_t kSensorPositionCenterY = 1858;

class FingerprintEngineNX729J : public HwFingerprintEngine {
public:
    FingerprintEngineNX729J();
    ~FingerprintEngineNX729J() override;

    int32_t getCenterPositionR() const override {
        return kSensorPositionCenterR;
    }

    int32_t getCenterPositionX() const override {
        return kSensorPositionCenterX;
    }

    int32_t getCenterPositionY() const override {
        return kSensorPositionCenterY;
    }

    void onPointerDownImpl(int32_t pointerId, int32_t x, int32_t y, float minor, float major);
    void onPointerUpImpl(int32_t pointerId);
    void onUiReadyImpl();

private:
    fingerprint_device_gf95xx *mDevice;
};

const std::vector<HwFingerprintModule> kModules = {
    {"fingerprint.gf95xx", nullptr, FingerprintSensorType::UNDER_DISPLAY_OPTICAL},
};

FingerprintEngineNX729J::FingerprintEngineNX729J()
    : HwFingerprintEngine(kModules), mDevice(reinterpret_cast<fingerprint_device_gf95xx*>(getDevice())) {
    if (mDevice == nullptr) {
        ALOGE("No valid device");
        abort();
    }
}

FingerprintEngineNX729J::~FingerprintEngineNX729J() {
    ALOGD("~FingerprintEngineNX729J");
    if (mDevice == nullptr) {
        ALOGE("No valid device");
        return;
    }
    mDevice = nullptr;
}


void FingerprintEngineNX729J::onPointerDownImpl(int32_t /*pointerId*/, int32_t /*x*/, int32_t /*y*/, float /*minor*/, float /*major*/) {
    ALOGI("onPointerDownImpl");
    if (mDevice->sendCustomizedCommand) {
        mDevice->sendCustomizedCommand(mDevice, 10, 1, CUSTOMIZED_COMMAND, CUSTOMIZED_COMMAND_LEN);
    } else {
        ALOGW("onPointerDownImpl: sendCustomizedCommand is nullptr");
    }
}

void FingerprintEngineNX729J::onPointerUpImpl(int32_t /*pointerId*/) {
    ALOGI("onPointerUpImpl");
    if (mDevice->sendCustomizedCommand) {
        mDevice->sendCustomizedCommand(mDevice, 10, 0, CUSTOMIZED_COMMAND, CUSTOMIZED_COMMAND_LEN);
    } else {
        ALOGW("onPointerUpImpl: sendCustomizedCommand is nullptr");
    }
}

void FingerprintEngineNX729J::onUiReadyImpl() {
    ALOGI("onUiReadyImpl: stub");
}

std::shared_ptr<FingerprintEngine> makeFingerprintEngine() {
    return std::make_shared<FingerprintEngineNX729J>();
}

}
