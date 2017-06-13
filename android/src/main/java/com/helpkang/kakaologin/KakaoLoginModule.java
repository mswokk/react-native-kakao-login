package com.helpkang.kakaologin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;


public class KakaoLoginModule extends ReactContextBaseJavaModule implements ActivityEventListener {
    private static final String TAG = "KAKAO";

    private boolean isInitialized = false;

    public KakaoLoginModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "KakaoLoginModule";
    }

    @ReactMethod
    public void login(Promise promise) {
        init();
        Session.getCurrentSession().addCallback(new SessionCallback(promise));
        Session.getCurrentSession().open(AuthType.KAKAO_TALK, getCurrentActivity());
    }

    @ReactMethod
    public void logout(final Promise promise) {
        init();
        UserManagement.requestLogout(new LogoutResponseCallback() {
            /**
             * Always success regardless result
             */
            @Override
            public void onCompleteLogout() {
                WritableMap response = Arguments.createMap();
                response.putString("success", "true");
                promise.resolve(response);
            }
        });
    }

    private void init(){
        if(!isInitialized){
            KakaoSDK.init(new KakaoSDKAdapter());
            getReactApplicationContext().addActivityEventListener(this);
            isInitialized = true;
         }
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    /**
     * Class KakaoSDKAdapter
     */
    private class KakaoSDKAdapter extends KakaoAdapter {
        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[]{AuthType.KAKAO_TALK};
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return false;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSaveFormData() {
                    return false;
                }
            };
        }

        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Activity getTopActivity() {
                    return getCurrentActivity();
                }

                @Override
                public Context getApplicationContext() {
                    return getReactApplicationContext();
                }
            };
        }
    }

    /**
     * Class SessonCallback
     */
    private class SessionCallback implements ISessionCallback {
        private final Promise promise;

        public SessionCallback(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onSessionOpened() {
            Log.v(TAG, "kakao : SessionCallback.onSessionOpened");
            UserManagement.requestMe(new MeResponseCallback() {
                @Override
                public void onSuccess(UserProfile userProfile) {
                    Log.v(TAG, "kakao : handleResult");

                    removeCallback();

                    WritableMap map = Arguments.createMap();
                    map.putString("id", userProfile.getId() + "");
                    map.putString("nickname", userProfile.getNickname());
                    map.putString("profile_image", userProfile.getProfileImagePath());

                    map.putString("access_token", Session.getCurrentSession().getAccessToken());

                    promise.resolve(map);
                }

                @Override
                public void onFailure(ErrorResult errorResult) {
                    removeCallback();
                    promise.reject(String.valueOf(errorResult.getErrorCode()), errorResult.getErrorMessage());
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    Log.v(TAG, "kakao : SessionCallback.onSessionOpened.requestMe.onSessionClosed - " + errorResult);
                    Session.getCurrentSession().checkAndImplicitOpen();
                }

                @Override
                public void onNotSignedUp() {
                    removeCallback();
                    promise.reject("onNotSignedUp", "로그인 취소");
                }

                private void removeCallback() {
                    Session.getCurrentSession().removeCallback(SessionCallback.this);
                }
            });
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if (exception != null) {
                Log.v(TAG, "kakao : onSessionOpenFailed" + exception.toString());
            }
        }
    }
}

