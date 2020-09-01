package io.actbase.kakaosdk;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.kakao.sdk.auth.LoginClient;
import com.kakao.sdk.common.KakaoSdk;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ARNKakaoLogin extends ReactContextBaseJavaModule implements ActivityEventListener {

  private ReactApplicationContext context;

  public ARNKakaoLogin(ReactApplicationContext context) {
    super(context);
    this.context = context;

    try {
      ApplicationInfo ai = context
          .getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      Bundle bundle = ai.metaData;
      KakaoSdk.init(context, bundle.getString("com.kakao.sdk.AppKey"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "ARNKakaoLogin";
  }

  @Override
  public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
    try {
//            if (KakaoSDK.getAdapter() != null && Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
//                return;
//            }
    } catch (Exception ex) {
    }
  }

  @Override
  public void onNewIntent(Intent intent) {

  }

  private String format(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return sdf.format(date);
  }


  private void loginWithKakaoAccount(Promise promise) {
    LoginClient.getInstance().loginWithKakaoAccount(context, (token, error) -> {
      try {
        if (error != null) {
          throw new Exception(error.getMessage());
        }
        WritableMap map = Arguments.createMap();
        map.putString("accessToken", token.getAccessToken());
        map.putString("refreshToken", token.getRefreshToken());
        map.putString("accessTokenExpiresAt", format(token.getAccessTokenExpiresAt()));
        map.putString("refreshTokenExpiresAt", format(token.getRefreshTokenExpiresAt()));

        WritableArray scopes = Arguments.createArray();
        for (String scope : token.getScopes()) {
          scopes.pushString(scope);
        }
        map.putArray("scopes", scopes);

        promise.resolve(map);
      } catch (Throwable ex) {
        promise.reject(ex);
      }
      return null;
    });
  }

  @ReactMethod
  public void login(final Promise promise) {
    if (LoginClient.getInstance().isKakaoTalkLoginAvailable(context)) {
      LoginClient.getInstance().loginWithKakaoTalk(context.getCurrentActivity(), (token, error) -> {
        try {
          if (error != null) {
            throw new Exception(error.getMessage());
          }

          WritableMap map = Arguments.createMap();
          map.putString("accessToken", token.getAccessToken());
          map.putString("refreshToken", token.getRefreshToken());
          map.putString("accessTokenExpiresAt", format(token.getAccessTokenExpiresAt()));
          map.putString("refreshTokenExpiresAt", format(token.getRefreshTokenExpiresAt()));

          WritableArray scopes = Arguments.createArray();
          for (String scope : token.getScopes()) {
            scopes.pushString(scope);
          }
          map.putArray("scopes", scopes);

          promise.resolve(map);
        } catch (Throwable ex) {
//          loginWithKakaoAccount(promise);
          promise.reject(ex);
        }
        return null;
      });
    } else {
      loginWithKakaoAccount(promise);
    }
  }

  @ReactMethod
  public void loginWithNewScopes(ReadableArray permissions, final Promise promise) {
    List<String> perms = new ArrayList<String>();
    for (int i = 0; i < permissions.size(); i++) {
      perms.add(permissions.getString(i));
    }

    LoginClient.getInstance().loginWithNewScopes(context, perms, (token, error) -> {
      try {
        if (error != null) {
          throw new Exception(error.getMessage());
        }
        WritableMap map = Arguments.createMap();
        map.putString("accessToken", token.getAccessToken());
        map.putString("refreshToken", token.getRefreshToken());
        map.putString("accessTokenExpiresAt", format(token.getAccessTokenExpiresAt()));
        map.putString("refreshTokenExpiresAt", format(token.getRefreshTokenExpiresAt()));

        WritableArray scopes = Arguments.createArray();
        for (String scope : token.getScopes()) {
          scopes.pushString(scope);
        }
        map.putArray("scopes", scopes);

        promise.resolve(map);
      } catch (Throwable ex) {
        promise.reject(ex);
      }
      return null;
    });
  }
  @ReactMethod
  public void logout(final Promise promise) {
    UserApiClient.getInstance().logout((error) -> {
      if (error != null) {
        promise.reject(error);
      } else {
        promise.resolve("SUCCESS");
      }
      return null;
    });
  }

  @ReactMethod
  public void unlink(final Promise promise) {
    UserApiClient.getInstance().unlink((error) -> {
      if (error != null) {
        promise.reject(error);
      } else {
        promise.resolve("SUCCESS");
      }
      return null;
    });
  }

  @ReactMethod
  public void getAccessToken(final Promise promise) {
    UserApiClient.getInstance().accessTokenInfo((tokenInfo, error) -> {
      try {
        if (error != null) {
          throw new Exception(error.getMessage());
        }

        WritableMap map = Arguments.createMap();
        map.putDouble("id", tokenInfo.getId());
        map.putDouble("expiresIn", tokenInfo.getExpiresIn());
        promise.resolve(map);

      } catch (Throwable ex) {
        promise.reject(ex);
      }
      return null;
    });
  }

  @ReactMethod
  public void getProfile(final Promise promise) {
    UserApiClient.getInstance().me((user, error) -> {
      try {
        if (error != null) {
          throw new Exception(error.getMessage());
        }

        WritableMap map = Arguments.createMap();
        map.putDouble("id", user.getId());
        map.putString("connectedAt", format(user.getConnectedAt()));

        {
          WritableMap kakaoAccount = Arguments.createMap();
          Account origin = user.getKakaoAccount();
          if (origin.getEmailNeedsAgreement() != null) {
            kakaoAccount.putString("email", origin.getEmail());
            kakaoAccount.putBoolean("emailNeedsAgreement", origin.getEmailNeedsAgreement());
            kakaoAccount.putBoolean("isEmailValid", origin.isEmailValid());
            kakaoAccount.putBoolean("isEmailVerified", origin.isEmailVerified());
          }

          if (origin.getBirthdayNeedsAgreement() != null) {
            kakaoAccount.putString("birthday", origin.getBirthday());
            kakaoAccount.putBoolean("birthdayNeedsAgreement", origin.getBirthdayNeedsAgreement());
          }

          if (origin.getBirthyearNeedsAgreement() != null) {
            kakaoAccount.putString("birthyear", origin.getBirthyear());
            kakaoAccount.putBoolean("birthyearNeedsAgreement", origin.getBirthyearNeedsAgreement());
          }

          if (origin.getGenderNeedsAgreement() != null) {
            kakaoAccount.putString("gender", origin.getGender().toString());
            kakaoAccount.putBoolean("genderNeedsAgreement", origin.getGenderNeedsAgreement());
          }

          if (origin.getCiNeedsAgreement() != null) {
            kakaoAccount.putString("ci", origin.getCi().toString());
            kakaoAccount.putString("ciAuthenticatedAt", format(origin.getCiAuthenticatedAt()));
            kakaoAccount.putBoolean("ciNeedsAgreement", origin.getCiNeedsAgreement());
          }

          if (origin.getLegalBirthDateNeedsAgreement() != null) {
            kakaoAccount.putString("legalBirthDate", origin.getLegalBirthDate());
            kakaoAccount.putBoolean("legalBirthDateNeedsAgreement",
                origin.getLegalBirthDateNeedsAgreement());
          }

          if (origin.getLegalGenderNeedsAgreement() != null) {
            kakaoAccount.putString("legalGender", origin.getLegalGender().toString());
            kakaoAccount
                .putBoolean("legalGenderNeedsAgreement", origin.getLegalGenderNeedsAgreement());
          }

          if (origin.getLegalNameNeedsAgreement() != null) {
            kakaoAccount.putString("legalName", origin.getLegalName());
            kakaoAccount.putBoolean("legalNameNeedsAgreement", origin.getLegalNameNeedsAgreement());
          }

          if (origin.getAgeRangeNeedsAgreement() != null) {
            kakaoAccount.putString("ageRange", origin.getAgeRange().toString());
            kakaoAccount.putBoolean("ageRangeNeedsAgreement", origin.getAgeRangeNeedsAgreement());
          }

          if (origin.getPhoneNumberNeedsAgreement() != null) {
            kakaoAccount.putString("phoneNumber", origin.getPhoneNumber());
            kakaoAccount
                .putBoolean("phoneNumberNeedsAgreement", origin.getPhoneNumberNeedsAgreement());
          }

          if (origin.getProfileNeedsAgreement() != null) {
            WritableMap profile = Arguments.createMap();
            profile.putString("nickname", origin.getProfile().getNickname());
            profile.putString("profileImageUrl", origin.getProfile().getProfileImageUrl());
            profile.putString("thumbnailImageUrl", origin.getProfile().getThumbnailImageUrl());
            kakaoAccount.putMap("profile", profile);
            kakaoAccount.putBoolean("profileNeedsAgreement", origin.getProfileNeedsAgreement());
          }

          map.putMap("kakaoAccount", kakaoAccount);
        }

        {
          WritableMap properties = Arguments.createMap();
          Map<String, String> origin = user.getProperties();
          for (String key : origin.keySet()) {
            properties.putString(key, origin.get(key));
          }
          map.putMap("properties", properties);
        }
        promise.resolve(map);

      } catch (Throwable ex) {
        promise.reject(ex);
      }
      return null;
    });
  }


}
