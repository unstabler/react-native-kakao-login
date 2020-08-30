//
//  ARNKakaoLoginConnector.m
//  ARNKakaoLogin
//
//  Created by Suhan Moon on 2020/08/27.
//

#import "ARNKakaoLoginConnector.h"
#import <ARNKakaoLogin-Swift.h>

@implementation ARNKakaoLoginConnector

+ (BOOL)isKakaoTalkLoginUrl:(NSURL *)url {
    return [ARNKakaoLogin isKakaoTalkLoginUrl: url];
}

+ (BOOL)handleOpenUrl:(NSURL *)url {
    return [ARNKakaoLogin handleOpenUrl: url];
}

@end
