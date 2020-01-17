
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

RCT_EXPORT_METHOD(accessTokenWithResolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject) {
    @try {
        resolve([self getAccessToken]);
    }
    @catch(NSException * e) {
        NSLog(@"%@", e);
        NSLog(@"ERRORR....");
        reject(@"RNCKakaoSDK", e.userInfo.description, nil);
    }
}

RCT_EXPORT_METHOD(loginWithResolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject) {
    
    [[KOSession sharedSession] close];
    [[KOSession sharedSession] openWithCompletionHandler:^(NSError *error) {
        if (![[KOSession sharedSession] isOpen]) {
            reject(@"RNCKakaoSDK", error.userInfo.description, nil);
        }
        else {
            resolve([self getAccessToken]);
        }
     }];
    
}

RCT_EXPORT_METHOD(logoutWithResolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject) {
    
    [[KOSession sharedSession] close];
    [[KOSession sharedSession] logoutAndCloseWithCompletionHandler:^(BOOL success, NSError *error) {
        if (!success) {
            reject(@"RNCKakaoSDK", error.userInfo.description, nil);
        }
        else {
            resolve(@"SUCCESS");
        }
    }];
    
}

@end
  
