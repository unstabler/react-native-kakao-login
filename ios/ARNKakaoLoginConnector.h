//
//  ARNKakaoLoginConnector.h
//  ARNKakaoLogin
//
//  Created by Suhan Moon on 2020/08/27.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ARNKakaoLoginConnector : NSObject

+ (BOOL)isKakaoTalkLoginUrl:(NSURL *)url;
+ (BOOL)handleOpenUrl:(NSURL *)url;

@end

NS_ASSUME_NONNULL_END

