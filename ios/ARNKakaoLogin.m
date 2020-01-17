
#import "ARNKakaoLogin.h"
#import <KakaoOpenSDK/KakaoOpenSDK.h>

@implementation ARNKakaoLogin

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE(ARNKakaoLogin)

NSObject* handleNullableString(NSString *_Nullable string)
{
    return string != nil ? string : [NSNull null];
}

NSObject* handleKOBoolean(KOOptionalBoolean boolean)
{
    switch(boolean){
        case KOOptionalBooleanTrue : return @(YES);
        case KOOptionalBooleanFalse: return @(NO);
        case KOOptionalBooleanNull : return [NSNull null];
    }
}

NSObject* handleKOGender(KOUserGender gender)
{
    switch(gender){
        case KOUserGenderNull : return [NSNull null];
        case KOUserGenderMale : return @"MALE";
        case KOUserGenderFemale : return @"FEMALE";
    }
}

NSObject* handleKOAgeRange(KOUserAgeRange range)
{
    switch(range) {
        case KOUserAgeRangeNull : return [NSNull null];
        case KOUserAgeRangeType0 : return @"0-9";
        case KOUserAgeRangeType10 : return @"10-14";
        case KOUserAgeRangeType15 : return @"15-19";
        case KOUserAgeRangeType20 : return @"20-29";
        case KOUserAgeRangeType30 : return @"30-39";
        case KOUserAgeRangeType40 : return @"40-49";
        case KOUserAgeRangeType50 : return @"50-59";
        case KOUserAgeRangeType60 : return @"60-69";
        case KOUserAgeRangeType70 : return @"70-79";
        case KOUserAgeRangeType80 : return @"80-89";
        case KOUserAgeRangeType90 : return @"90-";
    }
}

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
    [[KOSession sharedSession] logoutAndCloseWithCompletionHandler:^(BOOL success, NSError *error) {
        if (!success) {
            reject(@"ARNKakaoLogin", error.userInfo.description, nil);
        }
        else {
            resolve(@"SUCCESS");
        }
    }];
}

RCT_REMAP_METHOD(unlink, unlinkWithResolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject) {
    [KOSessionTask unlinkTaskWithCompletionHandler:^(BOOL success, NSError * _Nullable error) {
        if (!success) {
            reject(@"ARNKakaoLogin", error.userInfo.description, nil);
        }
        else {
            resolve(@"SUCCESS");
        }
    }];
}

RCT_REMAP_METHOD(getProfile, getProfileWithResolver: (RCTPromiseResolveBlock)resolve rejecter: (RCTPromiseRejectBlock)reject) {
    [KOSessionTask userMeTaskWithCompletion:^(NSError *error, KOUserMe *me) {
        if (me) {
            NSDictionary * result = @{
                @"ID": handleNullableString(me.ID),
                @"hasSignedUp": handleKOBoolean(me.hasSignedUp),
                @"account": @{
                    @"profileNeedsAgreement": me.account.profileNeedsAgreement ? @YES : @NO,
                    @"profile": @{
                        @"nickname": handleNullableString(me.account.profile.nickname),
                        @"profileImageURL": handleNullableString(me.account.profile.profileImageURL.absoluteString),
                        @"thumbnailImageURL": handleNullableString(me.account.profile.thumbnailImageURL.absoluteString),
                    },
                    @"emailNeedsAgreement": me.account.emailNeedsAgreement ? @YES : @NO,
                    @"isEmailVerified": handleKOBoolean(me.account.isEmailVerified),
                    @"email": handleNullableString(me.account.email),
                    @"ageRangeNeedsAgreement": me.account.ageRangeNeedsAgreement ? @YES : @NO,
                    @"ageRange": handleKOAgeRange(me.account.ageRange),
                    @"birthdayNeedsAgreement": me.account.birthdayNeedsAgreement ? @YES : @NO,
                    @"birthday": handleNullableString(me.account.birthday),
                    @"genderNeedsAgreement": me.account.genderNeedsAgreement ? @YES : @NO,
                    @"gender": handleKOGender(me.account.gender)
                },
                @"properties": me.properties,
            };
            resolve(result);
        } else {
            reject(@"ARNKakaoLogin", error.userInfo.description, nil);
        }
    }];
}


@end

