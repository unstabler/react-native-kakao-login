
#import "ARNKakaoLogin.h"
#import <KakaoOpenSDK/KakaoOpenSDK.h>

@implementation ARNKakaoLogin

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE(ARNKakaoLogin)

- (BOOL)isLogin
{
    return [[KOSession sharedSession] isOpen];
}

- (NSDictionary *)getAccessToken
{
    if ([self isLogin]) {
        KOToken * token = [KOSession sharedSession].token;

        NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
        [formatter setDateFormat:@"yyyy-MM-dd hh:mm:ss"];

        NSNumber * time = [NSNumber numberWithDouble: token.remainingExpireTime];
        NSDictionary * result = @{ @"accessToken": token.accessToken,
                                   @"refreshToken": token.refreshToken,
                                   @"accessTokenExpiresAt": [formatter stringFromDate: token.accessTokenExpiresAt],
                                   @"refreshTokenExpiresAt": [formatter stringFromDate: token.refreshTokenExpiresAt],
                                   @"remainingExpireTime" : time,
                                   @"scopes" : token.scopes
                                   };
        return result;
    }
    else {
        return nil;
    }
}

RCT_REMAP_METHOD(getAccessToken, accessTokenWithResolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject) {
    @try {
        resolve([self getAccessToken]);
    }
    @catch(NSException * e) {
        reject(@"ARNKakaoLogin", e.userInfo.description, nil);
    }
}

RCT_REMAP_METHOD(login, loginWithResolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject) {
    [[KOSession sharedSession] close];
    [[KOSession sharedSession] openWithCompletionHandler:^(NSError *error) {
        if (![[KOSession sharedSession] isOpen]) {
            reject(@"ARNKakaoLogin", error.userInfo.description, nil);
        }
        else {
            resolve([self getAccessToken]);
        }
     }];
}

RCT_REMAP_METHOD(logout, logoutWithResolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject) {
    [[KOSession sharedSession] close];
    [[KOSession sharedSession] logoutAndCloseWithCompletionHandler:^(BOOL success, NSError *error) {
        if (!success) {
            reject(@"ARNKakaoLogin", error.userInfo.description, nil);
        }
        else {
            resolve(@"SUCCESS");
        }
    }];
}

@end

