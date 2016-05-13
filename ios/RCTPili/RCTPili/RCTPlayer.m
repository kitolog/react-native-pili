//
//  RCTPlayer.m
//  RCTPili
//
//  Created by buhe on 16/5/12.
//  Copyright © 2016年 pili. All rights reserved.
//

#import "RCTPlayer.h"
#import "RCTBridgeModule.h"
#import "RCTEventDispatcher.h"

@implementation RCTPlayer{
    RCTEventDispatcher *_eventDispatcher;
    PLPlayer *_plplayer;
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
        //
        //        _rate = 1.0;
        //        _volume = 1.0;
        //        _resizeMode = @"AVLayerVideoGravityResizeAspectFill";
        //        _pendingSeek = false;
        //        _pendingSeekTime = 0.0f;
        //        _lastSeekTime = 0.0f;
        //        _progressUpdateInterval = 250;
        //        _controls = NO;
        //
        //        [[NSNotificationCenter defaultCenter] addObserver:self
        //                                                 selector:@selector(applicationWillResignActive:)
        //                                                     name:UIApplicationWillResignActiveNotification
        //                                                   object:nil];
        //
        //        [[NSNotificationCenter defaultCenter] addObserver:self
        //                                                 selector:@selector(applicationWillEnterForeground:)
        //                                                     name:UIApplicationWillEnterForegroundNotification
        //                                                   object:nil];
        // 初始化 PLPlayerOption 对象
            }
    
    return self;
};

- (void) setSource:(NSDictionary *)source
{
    NSString *uri = source[@"uri"];
    
    PLPlayerOption *option = [PLPlayerOption defaultOption];
    
    // 更改需要修改的 option 属性键所对应的值
    [option setOptionValue:@15 forKey:PLPlayerOptionKeyTimeoutIntervalForMediaPackets];
    _plplayer = [PLPlayer playerWithURL:[[NSURL alloc] initWithString:uri] option:option];

//    _plplayer.delegate = self;
    _plplayer.delegateQueue = dispatch_get_main_queue();
//    _plplayer.backgroundPlayEnable = enableBackgroundPlay;
#if !enableBackgroundPlay
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(startPlayer) name:UIApplicationWillEnterForegroundNotification object:nil];
#endif
    [self setupUI];
    
    [self startPlayer];
    
}

- (void)setupUI {
    if (_plplayer.status != PLPlayerStatusError) {
        // add player view
        UIView *playerView = _plplayer.playerView;
        [self addSubview:playerView];
         [playerView setTranslatesAutoresizingMaskIntoConstraints:NO];
        
        NSLayoutConstraint *centerX = [NSLayoutConstraint constraintWithItem:playerView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0];
        NSLayoutConstraint *centerY = [NSLayoutConstraint constraintWithItem:playerView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0];
        NSLayoutConstraint *width = [NSLayoutConstraint constraintWithItem:playerView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0 constant:0];
        NSLayoutConstraint *height = [NSLayoutConstraint constraintWithItem:playerView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0 constant:0];
        
        NSArray *constraints = [NSArray arrayWithObjects:centerX, centerY,width,height, nil];
        [self addConstraints: constraints];
//        if (!playerView.superview) {
//            playerView.contentMode = UIViewContentModeScaleAspectFit;
//            playerView.autoresizingMask = UIViewAutoresizingFlexibleBottomMargin | UIViewAutoresizingFlexibleTopMargin | UIViewAutoresizingFlexibleLeftMargin | UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleWidth;
//            [self addSubview:playerView];
//            
//            
////            UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(tap:)];
////            [self.view addGestureRecognizer:tap];
//        }
    }
    
}

- (void)startPlayer {
    [self addActivityIndicatorView]; //用户自己通过事件加  //TODO
    [UIApplication sharedApplication].idleTimerDisabled = YES;
    [_plplayer play];
}

- (void)addActivityIndicatorView {
    if (self.activityIndicatorView) {
        return;
    }
    UIActivityIndicatorView *activityIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
    activityIndicatorView.center = CGPointMake(CGRectGetMidX(self.bounds), CGRectGetMidY(self.bounds));
    [self addSubview:activityIndicatorView];
    [activityIndicatorView stopAnimating];
    
    self.activityIndicatorView = activityIndicatorView;
}


@end
