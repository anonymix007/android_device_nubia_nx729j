/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#define LOG_TAG "FingerprintEngineNX729J"

#include <android/log.h>
#include <log/log.h>

#include <Session.h>
#include <HwFingerprintEngineCommon.h>
#include <Legacy2Aidl.h>

#include "fingerprint_device_nx729j.h"

namespace aidl::android::hardware::biometrics::fingerprint {

constexpr char kHwModuleName[] = "fingerprint.gf95xx";
constexpr FingerprintSensorType kSensorType = FingerprintSensorType::UNDER_DISPLAY_OPTICAL;
constexpr int32_t kSensorPositionCenterR = 94;
constexpr int32_t kSensorPositionCenterX = 558;
constexpr int32_t kSensorPositionCenterY = 1858;

class FingerprintEngineNX729J : public FingerprintEngine {
public:
    FingerprintEngineNX729J();
    ~FingerprintEngineNX729J() override;

    FingerprintSensorType getSensorType() const {
        return kSensorType;
    }

    int32_t getCenterPositionR() const {
        return kSensorPositionCenterR;
    }

    int32_t getCenterPositionX() const {
        return kSensorPositionCenterX;
    }

    int32_t getCenterPositionY() const {
        return kSensorPositionCenterY;
    }

    void setSession(std::shared_ptr<Session> session) {
        mSession = session;
    }

    void setActiveGroup(int userId) {
        mUserId = userId;
        char path[256];
        snprintf(path, sizeof(path), "/data/vendor_de/%d/fpdata/", userId);
        mDevice->set_active_group(mDevice, mUserId, path);
    }

    void generateChallengeImpl();
    void revokeChallengeImpl(int64_t challenge);
    void enrollImpl(const keymaster::HardwareAuthToken& hat);
    void authenticateImpl(int64_t operationId);
    void detectInteractionImpl();
    void enumerateEnrollmentsImpl();
    void removeEnrollmentsImpl(const std::vector<int32_t>& enrollmentIds);
    void getAuthenticatorIdImpl();
    void invalidateAuthenticatorIdImpl();
    void onPointerDownImpl(int32_t pointerId, int32_t x, int32_t y, float minor, float major);
    void onPointerUpImpl(int32_t pointerId);
    void onUiReadyImpl();
    ndk::ScopedAStatus cancelImpl();

private:
    static void notify(const fingerprint_msg_t* msg);
    static Error VendorErrorFilter(int32_t error, int32_t* vendorCode);
    static AcquiredInfo VendorAcquiredFilter(int32_t info, int32_t* vendorCode);
    fingerprint_device_gf95xx *mDevice;

    int32_t mUserId;
    uint64_t mAuthId;
    uint64_t mChallengeId;
};

static FingerprintEngineNX729J* sInstance = nullptr;

FingerprintEngineNX729J::FingerprintEngineNX729J()
    : mDevice(nullptr), mAuthId(0), mChallengeId(0) {
    sInstance = this;  // keep track of the most recent instance
    mDevice = reinterpret_cast<fingerprint_device_gf95xx*>(open_hw_module(kHwModuleName, NULL));
    int err;
    if (mDevice != nullptr && (err = mDevice->set_notify(mDevice, FingerprintEngineNX729J::notify)) != 0) {
        ALOGE("Can't register fingerprint module callback, error: %d", err);
        mDevice = nullptr;
    }

    if (!mDevice) {
        ALOGE("Can't open HAL module, id %s, class %s", kHwModuleName, nullptr);
        abort();
    }
    ALOGI("Opened fingerprint HAL, id %s, class %s", kHwModuleName, nullptr);
}


FingerprintEngineNX729J::~FingerprintEngineNX729J() {
    if (mDevice == nullptr) {
        ALOGE("No valid device");
        return;
    }
    int err;
    if (0 != (err = mDevice->common.close(reinterpret_cast<hw_device_t*>(mDevice)))) {
        ALOGE("Can't close fingerprint module, error: %d", err);
        return;
    }
    mDevice = nullptr;
}

void FingerprintEngineNX729J::generateChallengeImpl() {
    uint64_t challenge = mDevice->pre_enroll(mDevice);
    ALOGI("generateChallengeImpl: %ld", challenge);

    WEAK_SESSION_CALLBACK_OR_LOG_ERROR(mSession, onChallengeGenerated, challenge);
}

void FingerprintEngineNX729J::revokeChallengeImpl(int64_t challenge) {
    ALOGI("revokeChallengeImpl: %ld", challenge);

    mDevice->post_enroll(mDevice);
    WEAK_SESSION_CALLBACK_OR_LOG_ERROR(mSession, onChallengeRevoked, challenge);
}

void FingerprintEngineNX729J::enrollImpl(const keymaster::HardwareAuthToken& hat) {
    ALOGI("enrollImpl");

    hw_auth_token_t authToken;
    translate(hat, authToken);
    int error = mDevice->enroll(mDevice, &authToken, mUserId, 60);
    if (error) {
        ALOGE("enroll failed: %d", error);
        WEAK_SESSION_CALLBACK_OR_LOG_ERROR(mSession, onError, Error::UNABLE_TO_PROCESS, error);
    }
}

void FingerprintEngineNX729J::authenticateImpl(int64_t operationId) {
    ALOGI("authenticateImpl(%lu)", operationId);

    int error = mDevice->authenticate(mDevice, operationId, mUserId);
    if (error) {
        ALOGE("authenticate failed: %d", error);
        WEAK_SESSION_CALLBACK_OR_LOG_ERROR(mSession, onError, Error::UNABLE_TO_PROCESS, error);
    }
}

void FingerprintEngineNX729J::detectInteractionImpl() {
    ALOGE("detectInteractionImpl: not supported");

    WEAK_SESSION_CALLBACK_OR_LOG_ERROR(mSession, onError, Error::UNABLE_TO_PROCESS, 0 /* vendorCode */);
}

void FingerprintEngineNX729J::enumerateEnrollmentsImpl() {
    ALOGI("enumerateEnrollmentsImpl");

    int error = mDevice->enumerate(mDevice);
    if (error) {
        ALOGE("enumerate failed: %d", error);
    }
}

void FingerprintEngineNX729J::removeEnrollmentsImpl(const std::vector<int32_t>& enrollmentIds) {
    ALOGI("removeEnrollmentsImpl, size: %zu", enrollmentIds.size());

    for (int32_t fid : enrollmentIds) {
        int error = mDevice->remove(mDevice, mUserId, fid);
        if (error) {
            ALOGE("remove failed: %d", error);
        }
    }
}

void FingerprintEngineNX729J::getAuthenticatorIdImpl() {
    mAuthId = mDevice->get_authenticator_id(mDevice);
    ALOGI("getAuthenticatorIdImpl: %ld", mAuthId);

    WEAK_SESSION_CALLBACK_OR_LOG_ERROR(mSession, onAuthenticatorIdRetrieved, mAuthId);
}

void FingerprintEngineNX729J::invalidateAuthenticatorIdImpl() {
    ALOGI("invalidateAuthenticatorIdImpl: %ld", mAuthId);

    WEAK_SESSION_CALLBACK_OR_LOG_ERROR(mSession, onAuthenticatorIdInvalidated, mAuthId);
    mAuthId = 0;
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

ndk::ScopedAStatus FingerprintEngineNX729J::cancelImpl() {
    ALOGI("cancelImpl");

    int ret = mDevice->cancel(mDevice);

    if (ret == 0) {
        WEAK_SESSION_CALLBACK_OR_LOG_ERROR(mSession, onError, Error::CANCELED, 0 /* vendorCode */);
        return ndk::ScopedAStatus::ok();
    } else {
        return ndk::ScopedAStatus::fromServiceSpecificError(ret);
    }
}

// Translate from errors returned by traditional HAL (see fingerprint.h) to AIDL-compliant Error
Error FingerprintEngineNX729J::VendorErrorFilter(int32_t error, int32_t* vendorCode) {
    *vendorCode = 0;

    switch (error) {
        case FINGERPRINT_ERROR_HW_UNAVAILABLE:
            return Error::HW_UNAVAILABLE;
        case FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
            return Error::UNABLE_TO_PROCESS;
        case FINGERPRINT_ERROR_TIMEOUT:
            return Error::TIMEOUT;
        case FINGERPRINT_ERROR_NO_SPACE:
            return Error::NO_SPACE;
        case FINGERPRINT_ERROR_CANCELED:
            return Error::CANCELED;
        case FINGERPRINT_ERROR_UNABLE_TO_REMOVE:
            return Error::UNABLE_TO_REMOVE;
        case FINGERPRINT_ERROR_LOCKOUT: {
            *vendorCode = FINGERPRINT_ERROR_LOCKOUT;
            return Error::VENDOR;
        }
        default:
            if (error >= FINGERPRINT_ERROR_VENDOR_BASE) {
                // vendor specific code.
                *vendorCode = error - FINGERPRINT_ERROR_VENDOR_BASE;
                return Error::VENDOR;
            }
    }
    ALOGE("Unknown error from fingerprint vendor library: %d", error);
    return Error::UNABLE_TO_PROCESS;
}

// Translate acquired messages returned by traditional HAL (see fingerprint.h) to AIDL-compliant AcquiredInfo
AcquiredInfo FingerprintEngineNX729J::VendorAcquiredFilter(int32_t info, int32_t* vendorCode) {
    *vendorCode = 0;

    switch (info) {
        case FINGERPRINT_ACQUIRED_GOOD:
            return AcquiredInfo::GOOD;
        case FINGERPRINT_ACQUIRED_PARTIAL:
            return AcquiredInfo::PARTIAL;
        case FINGERPRINT_ACQUIRED_INSUFFICIENT:
            return AcquiredInfo::INSUFFICIENT;
        case FINGERPRINT_ACQUIRED_IMAGER_DIRTY:
            return AcquiredInfo::SENSOR_DIRTY;
        case FINGERPRINT_ACQUIRED_TOO_SLOW:
            return AcquiredInfo::TOO_SLOW;
        case FINGERPRINT_ACQUIRED_TOO_FAST:
            return AcquiredInfo::TOO_FAST;
        default:
            if (info >= FINGERPRINT_ACQUIRED_VENDOR_BASE) {
                // vendor specific code.
                *vendorCode = info - FINGERPRINT_ACQUIRED_VENDOR_BASE;
                return AcquiredInfo::VENDOR;
            }
    }
    ALOGE("Unknown acquired message from fingerprint vendor library: %d", info);
    return AcquiredInfo::INSUFFICIENT;
}

void FingerprintEngineNX729J::notify(const fingerprint_msg_t* msg) {
    FingerprintEngineNX729J* thisPtr = sInstance;

    if (thisPtr == nullptr) {
        ALOGE("Receiving callbacks before the engine is initialized");
        return;
    }

    if (auto session = thisPtr->mSession.lock()) {
        LockoutTracker& lockoutTracker = session->mLockoutTracker;
        auto cb = session->mCb;
        switch (msg->type) {
            case FINGERPRINT_ERROR: {
                int32_t vendorCode = 0;
                Error result = VendorErrorFilter(msg->data.error, &vendorCode);
                ALOGD("onError(%d, %d)", result, vendorCode);
                cb->onError(result, vendorCode);
            } break;
            case FINGERPRINT_ACQUIRED: {
                int32_t vendorCode = 0;
                AcquiredInfo result = VendorAcquiredFilter(msg->data.acquired.acquired_info, &vendorCode);
                if (result != AcquiredInfo::VENDOR) {
                    ALOGD("onAcquired(%d, %d)", result, vendorCode);
                    cb->onAcquired(result, vendorCode);
                } else {
                    ALOGW("onAcquired(AcquiredInfo::VENDOR, %d)", vendorCode);
                    // Do not send onAcquired or illumination will be turned off prematurely
                }
            } break;
            case FINGERPRINT_TEMPLATE_ENROLLING: {
                ALOGD("onEnrollResult(fid=%d, gid=%d, rem=%d)", msg->data.enroll.finger.fid,
                      msg->data.enroll.finger.gid, msg->data.enroll.samples_remaining);
                cb->onEnrollmentProgress(msg->data.enroll.finger.fid, msg->data.enroll.samples_remaining);
            } break;
            case FINGERPRINT_TEMPLATE_REMOVED: {
                ALOGD("onRemove(fid=%d, gid=%d, rem=%d)", msg->data.removed.finger.fid,
                      msg->data.removed.finger.gid, msg->data.removed.remaining_templates);
                std::vector<int> enrollments;
                enrollments.push_back(msg->data.removed.finger.fid);
                cb->onEnrollmentsRemoved(enrollments);
            } break;
            case FINGERPRINT_AUTHENTICATED: {
                ALOGD("onAuthenticated(fid=%d, gid=%d)", msg->data.authenticated.finger.fid,
                    msg->data.authenticated.finger.gid);
                if (msg->data.authenticated.finger.fid != 0) {
                    const hw_auth_token_t hat = msg->data.authenticated.hat;
                    HardwareAuthToken authToken;
                    translate(hat, authToken);
                    cb->onAuthenticationSucceeded(msg->data.authenticated.finger.fid, authToken);
                    lockoutTracker.reset(true);
                } else {
                    cb->onAuthenticationFailed();
                    lockoutTracker.addFailedAttempt();
                    session->checkSensorLockout();
                }
            } break;
            case FINGERPRINT_TEMPLATE_ENUMERATING: {
                ALOGD("onEnumerate(fid=%d, gid=%d, rem=%d)", msg->data.enumerated.finger.fid,
                      msg->data.enumerated.finger.gid, msg->data.enumerated.remaining_templates);
                static std::vector<int> enrollments;
                enrollments.push_back(msg->data.enumerated.finger.fid);
                if (msg->data.enumerated.remaining_templates == 0) {
                    cb->onEnrollmentsEnumerated(enrollments);
                    enrollments.clear();
                }
            } break;

            default:
                ALOGE("%s: Unknown message: %u", __func__, msg->type);
                abort();
                break;
        }
    } else {
        ALOGE("Receiving callbacks before a session is opened");
    }
}

std::shared_ptr<FingerprintEngine> makeFingerprintEngine() {
    return std::make_shared<FingerprintEngineNX729J>();
}

}
