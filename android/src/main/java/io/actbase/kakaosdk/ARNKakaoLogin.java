
package io.actbase.kakaosdk;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.network.response.AccessTokenInfoResponse;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.AgeRange;
import com.kakao.usermgmt.response.model.Gender;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import io.actbase.kakaosdk.impl.KakaoSDKAdapter;
import io.actbase.kakaosdk.impl.LoginButton;

public class ARNKakaoLogin extends ReactContextBaseJavaModule implements ActivityEventListener {

    private ReactApplicationContext reactContext;
    private KakaoSDKAdapter kakaoSDKAdapter;
    private LoginButton loginButton;

    public ARNKakaoLogin(ReactApplicationContext reactContext) {
        super(reactContext);

        this.reactContext = reactContext;
        if (KakaoSDK.getAdapter() == null)
            this.initKakaoSDK();
        else
            Session.getCurrentSession().clearCallbacks();
        this.reactContext.addActivityEventListener(this);

        this.loginButton = new LoginButton(this.reactContext);
    }

    @Override
    public String getName() {
        return "ARNKakaoLogin";
    }

    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        try {
            if (KakaoSDK.getAdapter() != null && Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
                return;
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    private void initKakaoSDK() {
        this.kakaoSDKAdapter = new KakaoSDKAdapter(this.reactContext);
        try {
            KakaoSDK.init(this.kakaoSDKAdapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String format(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    private void handleKOBoolean(WritableMap map, String key, OptionalBoolean v) {
        if (v == null || v.getBoolean() == null) {
            map.putNull(key);
        } else {
            map.putBoolean(key, v.getBoolean());
        }
    }

    @ReactMethod
    public void login(Promise promise) {

        try {
            this.initKakaoSDK();

            if (!Session.getCurrentSession().isClosed()) {
                Session.getCurrentSession().close();
            }

            Session.getCurrentSession().clearCallbacks();
            loginButton.open(AuthType.KAKAO_LOGIN_ALL, new SessionCallback(promise));
        } catch (Exception ex) {
            ex.printStackTrace();
            promise.reject(ex);
        }
    }


    @ReactMethod
    public void logout(final Promise promise) {

        try {
            UserManagement.getInstance().requestLogout(new LogoutResponseCallback() {
                @Override
                public void onCompleteLogout() {
                    promise.resolve("SUCCESS");
                }

                @Override
                public void onSessionClosed(ErrorResult errorResult) {
                    super.onSessionClosed(errorResult);
                    promise.reject(errorResult.getException());
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            promise.reject(ex);
        }
    }

    @ReactMethod
    public void getAccessToken(Promise promise) {

        try {
            this.initKakaoSDK();
            AccessToken token = Session.getCurrentSession().getTokenInfo();

            if (token.getAccessToken() == null) throw new Exception("Required login..");

            WritableMap map = Arguments.createMap();
            map.putString("accessToken", token.getAccessToken());
            map.putString("refreshToken", token.getRefreshToken());
            map.putString("accessTokenExpiresAt", format(token.accessTokenExpiresAt()));
            map.putString("refreshTokenExpiresAt", format(token.refreshTokenExpiresAt()));
            map.putString("remainingExpireTime", format(new Date(token.getRemainingExpireTime())));

            promise.resolve(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            promise.reject(ex);
        }

    }


    @ReactMethod
    public void getProfile(final Promise promise) {

        List<String> keys = new ArrayList<>();
        keys.add("properties.nickname");
        keys.add("properties.profile_image");
        keys.add("properties.thumbnail_image");
        keys.add("kakao_account.profile");
        keys.add("kakao_account.email");
        keys.add("kakao_account.age_range");
        keys.add("kakao_account.birthday");
        keys.add("kakao_account.gender");

        UserManagement.getInstance().me(keys, new MeV2ResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.d(message);
                promise.reject(errorResult.getException());
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
//                redirectLoginActivity();
                promise.reject("ARNKakaoLogin", "onSessionClosed");
            }

            @Override
            public void onSuccess(MeV2Response response) {
                System.out.println(response);

                UserAccount account = response.getKakaoAccount();

                WritableMap mapAccount = Arguments.createMap();
                handleKOBoolean(mapAccount, "profileNeedsAgreement", account.profileNeedsAgreement());
                handleKOBoolean(mapAccount, "emailNeedsAgreement", account.emailNeedsAgreement());
                handleKOBoolean(mapAccount, "isEmailVerified", account.isEmailVerified());
                mapAccount.putString("email", account.getEmail());
                handleKOBoolean(mapAccount, "ageRangeNeedsAgreement", account.ageRangeNeedsAgreement());

                if (account.getAgeRange() == null || account.getAgeRange() == AgeRange.AGE_RANGE_UNKNOWN) {
                    mapAccount.putNull("ageRange");
                } else {
                    mapAccount.putString("ageRange", account.getAgeRange().toString());
                }

                handleKOBoolean(mapAccount, "birthdayNeedsAgreement", account.birthdayNeedsAgreement());
                mapAccount.putString("birthday", account.getBirthday());
                handleKOBoolean(mapAccount, "genderNeedsAgreement", account.genderNeedsAgreement());

                if (account.getGender() == null || account.getGender() == Gender.UNKNOWN) {
                    mapAccount.putNull("gender");
                } else {
                    mapAccount.putString("gender", account.getGender().toString());
                }

                Profile profile = account.getProfile();
                WritableMap mapProfile = Arguments.createMap();
                mapProfile.putString("nickname", profile.getNickname());
                mapProfile.putString("profileImageURL", profile.getProfileImageUrl());
                mapProfile.putString("thumbnailImageURL", profile.getThumbnailImageUrl());

                mapAccount.putMap("profile", mapProfile);

                WritableMap properties = Arguments.createMap();
                for (String key : response.getProperties().keySet()) {
                    properties.putString(key, response.getProperties().get(key));
                }

                WritableMap map = Arguments.createMap();
                map.putInt("ID", (int) response.getId());
                handleKOBoolean(map, "hasSignedUp", response.hasSignedUp());
                map.putMap("account", mapAccount);
                map.putMap("properties", properties);
                promise.resolve(map);

            }

        });

    }

    private class SessionCallback implements ISessionCallback {
        private Promise promise;

        public SessionCallback(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void onSessionOpened() {
            Log.d(getName(), "SessionCallback.onSessionOpened");
            if (this.promise != null) {
                getAccessToken(this.promise);
                this.promise = null;
            }
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            Log.d(getName(), "SessionCallback.onSessionOpenFailed");
            exception.printStackTrace();
            if (this.promise != null) {
                this.promise.reject(exception);
                this.promise = null;
            }
        }

    }

}
