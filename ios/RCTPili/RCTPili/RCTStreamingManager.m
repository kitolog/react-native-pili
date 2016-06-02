//
//  RCTStreamingManager.m
//  RCTPili
//
//  Created by guguyanhua on 16/5/26.
//  Copyright © 2016年 pili. All rights reserved.
//

#import "RCTStreamingManager.h"
#import "RCTStreaming.h"

@implementation RCTStreamingManager
RCT_EXPORT_MODULE();

@synthesize bridge = _bridge;

- (UIView *)view
{
    return [[RCTStreaming alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_VIEW_PROPERTY(stream, NSDictionary);
RCT_EXPORT_VIEW_PROPERTY(profile, NSDictionary);


@end
