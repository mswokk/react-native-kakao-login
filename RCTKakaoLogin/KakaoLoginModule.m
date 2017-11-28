//
//  KakaoLoginModule.m
//  KakaoLoginModule
//
//  Created by 강성일 on 2016. 8. 24..
//  Copyright © 2016년 강성일. All rights reserved.
//

#import "KakaoLoginModule.h"
#import <KakaoOpenSDK/KakaoOpenSDK.h>

@implementation KakaoLoginModule

RCT_EXPORT_MODULE();

RCT_REMAP_METHOD(login,
            resolver:
            (RCTPromiseResolveBlock) resolve
            rejecter:
            (RCTPromiseRejectBlock) reject) {

    dispatch_async(dispatch_get_main_queue(), ^{
        [[KOSession sharedSession] close];
        [[KOSession sharedSession] openWithCompletionHandler:^(NSError *error) {
            if ([[KOSession sharedSession] isOpen]) {
                [self loginProcessResolve:resolve rejecter:reject];
            } else {
                // failed
                NSLog(@"login cancel.");
                reject(@"KAKAO_LOGIN_CANCEL", @"CANCEL", nil);
            }
        }];
    });
}

- (void)loginProcessResolve:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    [KOSessionTask meTaskWithCompletionHandler:^(KOUser *result, NSError *error) {
        if (result) {
            NSLog(@"login succeed");
            // success
            NSDictionary *userSession = @{
                    @"id": result.ID,
                    @"access_token": [KOSession sharedSession].accessToken,
                    @"nickname": [result propertyForKey:@"nickname"],
                    @"profile_image": [result propertyForKey:@"profile_image"] != nil ? [result propertyForKey:@"profile_image"] : @""
            };
            resolve(userSession);
        } else {
            NSLog(@"login failed.");
            reject(@"KAKAO_LOGIN_FAIL", @"FAIL", nil);
        }

    }];
}

RCT_REMAP_METHOD(logout,
            resolver1:
            (RCTPromiseResolveBlock) resolve
            rejecter1:
            (RCTPromiseRejectBlock) reject) {
    [[KOSession sharedSession] close];
    NSDictionary *status = @{@"success": @"true"};
    resolve(status);
}
@end
