//
//  RCTStreaming.m
//  RCTPili
//
//  Created by guguyanhua on 16/5/26.
//  Copyright © 2016年 pili. All rights reserved.
//

#import "RCTStreaming.h"
#import "RCTBridgeModule.h"
#import "UIView+React.h"
#import "RCTEventDispatcher.h"
#import "PLCameraStreamingKit.h"

@implementation RCTStreaming{
    RCTEventDispatcher *_eventDispatcher;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    if ((self = [super init])) {
        _eventDispatcher = eventDispatcher;
//        _started = YES;
//        _muted = NO;
//        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
//        self.reconnectCount = 0;
    }
    
    return self;
};

- (void) setStream:(NSDictionary *)source
{
    
}


-(void) setupUI
{
    
}
@end
