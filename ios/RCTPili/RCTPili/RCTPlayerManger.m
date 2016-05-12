//
//  RCTPlayerManger.m
//  RCTPili
//
//  Created by buhe on 16/5/12.
//  Copyright © 2016年 pili. All rights reserved.
//

#import "RCTPlayerManger.h"
#import "RCTPlayer.h"

@implementation RCTPlayerManger
RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (UIView *)view
{
    return [[RCTPlayer alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}

RCT_EXPORT_VIEW_PROPERTY(source, NSDictionary);


@end
