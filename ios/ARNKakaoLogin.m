
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

        NSNumber * time = [NSNumber numberWithDouble: token.remainingExpireTime];
        NSDictionary * result = @{
            @"accessToken": token.accessToken,
            @"scopes" : token.scopes,
            @"remainingExpireTime" : time
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

