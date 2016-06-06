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


@implementation RCTStreaming{
    RCTEventDispatcher *_eventDispatcher;
}

const char *stateNames[] = {
    "Unknow",
    "Connecting",
    "Connected",
    "Disconnecting",
    "Disconnected",
    "Error"
};

const char *networkStatus[] = {
    "Not Reachable",
    "Reachable via WiFi",
    "Reachable via CELL"
};



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
        [PLStreamingEnv initEnv];
        _eventDispatcher = eventDispatcher;
//        _started = YES;
//        _muted = NO;
//        [[AVAudioSession sharedInstance] setCategory:AVAudioSessionCategoryPlayback error:nil];
//        self.reconnectCount = 0;
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(reachabilityChanged:) name:kReachabilityChangedNotification object:nil];
        self.internetReachability = [Reachability reachabilityForInternetConnection];
        [self.internetReachability startNotifier];
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(handleInterruption:)
                                                     name:AVAudioSessionInterruptionNotification
                                                   object:[AVAudioSession sharedInstance]];
        CGSize videoSize = CGSizeMake(480 , 640);
        UIDeviceOrientation orientation = [[UIDevice currentDevice] orientation];
        if (orientation <= AVCaptureVideoOrientationLandscapeLeft) {
            if (orientation > AVCaptureVideoOrientationPortraitUpsideDown) {
                videoSize = CGSizeMake(640 , 480);
            }
        }
        self.videoStreamingConfigurations = @[
                                              [[PLVideoStreamingConfiguration alloc] initWithVideoSize:videoSize expectedSourceVideoFrameRate:15 videoMaxKeyframeInterval:45 averageVideoBitRate:800 * 1000 videoProfileLevel:AVVideoProfileLevelH264Baseline31],
                                              [[PLVideoStreamingConfiguration alloc] initWithVideoSize:CGSizeMake(800 , 480) expectedSourceVideoFrameRate:24 videoMaxKeyframeInterval:72 averageVideoBitRate:800 * 1000 videoProfileLevel:AVVideoProfileLevelH264Baseline31],
                                              [[PLVideoStreamingConfiguration alloc] initWithVideoSize:videoSize expectedSourceVideoFrameRate:30 videoMaxKeyframeInterval:90 averageVideoBitRate:800 * 1000 videoProfileLevel:AVVideoProfileLevelH264Baseline31],
                                              ];
        self.videoCaptureConfigurations = @[
                                            [[PLVideoCaptureConfiguration alloc] initWithVideoFrameRate:15 sessionPreset:AVCaptureSessionPresetiFrame960x540 horizontallyMirrorFrontFacingCamera:YES horizontallyMirrorRearFacingCamera:NO],
                                            [[PLVideoCaptureConfiguration alloc] initWithVideoFrameRate:24 sessionPreset:AVCaptureSessionPresetiFrame960x540 horizontallyMirrorFrontFacingCamera:YES horizontallyMirrorRearFacingCamera:NO],
                                            [[PLVideoCaptureConfiguration alloc] initWithVideoFrameRate:30 sessionPreset:AVCaptureSessionPresetiFrame960x540 horizontallyMirrorFrontFacingCamera:YES horizontallyMirrorRearFacingCamera:NO]
                                            ];
        self.sessionQueue = dispatch_queue_create("pili.queue.streaming", DISPATCH_QUEUE_SERIAL);
    }
    
    return self;
};

- (void) setStream:(NSDictionary *)source
{
    [source setValue:@(NO) forKey:@"disabled"];
    [source setValue:@[@"480p", @"720p"] forKey:@"profiles"];
    PLStream *stream = [PLStream streamWithJSON:source];
    void (^permissionBlock)(void) = ^{
        dispatch_async(self.sessionQueue, ^{
            PLVideoCaptureConfiguration *videoCaptureConfiguration = [self.videoCaptureConfigurations lastObject];
            PLAudioCaptureConfiguration *audioCaptureConfiguration = [PLAudioCaptureConfiguration defaultConfiguration];
            // 视频编码配置
            PLVideoStreamingConfiguration *videoStreamingConfiguration = [self.videoStreamingConfigurations lastObject];
            // 音频编码配置
            PLAudioStreamingConfiguration *audioStreamingConfiguration = [PLAudioStreamingConfiguration defaultConfiguration];
            AVCaptureVideoOrientation orientation = (AVCaptureVideoOrientation)(([[UIDevice currentDevice] orientation] <= UIDeviceOrientationLandscapeRight && [[UIDevice currentDevice] orientation] != UIDeviceOrientationUnknown) ? [[UIDevice currentDevice] orientation]: UIDeviceOrientationPortrait);
            // 推流 session
            self.session = [[PLCameraStreamingSession alloc] initWithVideoCaptureConfiguration:videoCaptureConfiguration audioCaptureConfiguration:audioCaptureConfiguration videoStreamingConfiguration:videoStreamingConfiguration audioStreamingConfiguration:audioStreamingConfiguration stream:stream videoOrientation:orientation];
            self.session.delegate = self;
            self.session.bufferDelegate = self;
//            UIImage *waterMark = [UIImage imageNamed:@"qiniu.png"];
//            PLFilterHandler handler = [self.session addWaterMark:waterMark origin:CGPointMake(100, 300)];
//            self.filterHandlers = [@[handler] mutableCopy];//TODO -  水印暂时注释
            dispatch_async(dispatch_get_main_queue(), ^{
                UIView *previewView = self.session.previewView;
                [self addSubview:previewView];
                [previewView setTranslatesAutoresizingMaskIntoConstraints:NO];
                
                NSLayoutConstraint *centerX = [NSLayoutConstraint constraintWithItem:previewView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0];
                NSLayoutConstraint *centerY = [NSLayoutConstraint constraintWithItem:previewView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0];
                NSLayoutConstraint *width = [NSLayoutConstraint constraintWithItem:previewView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1.0 constant:0];
                NSLayoutConstraint *height = [NSLayoutConstraint constraintWithItem:previewView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0 constant:0];
                
                NSArray *constraints = [NSArray arrayWithObjects:centerX, centerY,width,height, nil];
                [self addConstraints: constraints];
                
                NSString *log = [NSString stringWithFormat:@"Zoom Range: [1..%.0f]", self.session.videoActiveFormat.videoMaxZoomFactor];
                NSLog(@"%@", log);
                [self startSession];
            });
        });
    };
    void (^noAccessBlock)(void) = ^{
        UIAlertView *alertView = [[UIAlertView alloc] initWithTitle:NSLocalizedString(@"No Access", nil)
                                                            message:NSLocalizedString(@"!", nil)
                                                           delegate:nil
                                                  cancelButtonTitle:NSLocalizedString(@"Cancel", nil)
                                                  otherButtonTitles:nil];
        [alertView show];
    };
    
    switch ([PLCameraStreamingSession cameraAuthorizationStatus]) {
        case PLAuthorizationStatusAuthorized:
            permissionBlock();
            break;
        case PLAuthorizationStatusNotDetermined: {
            [PLCameraStreamingSession requestCameraAccessWithCompletionHandler:^(BOOL granted) {
                granted ? permissionBlock() : noAccessBlock();
            }];
        }
            break;
        default:
            noAccessBlock();
            break;
    }
}

- (void)streamingSessionSendingBufferDidFull:(id)session {
    NSString *log = @"Buffer is full";
    NSLog(@"%@", log);
//    self.textView.text = [NSString stringWithFormat:@"%@\%@", self.textView.text, log];
}

- (void)streamingSession:(id)session sendingBufferDidDropItems:(NSArray *)items {
    NSString *log = @"Frame dropped";
    NSLog(@"%@", log);
//    self.textView.text = [NSString stringWithFormat:@"%@\%@", self.textView.text, log];
}



- (void)stopSession {
    dispatch_async(self.sessionQueue, ^{
        [self.session stop];
    });
}

- (void)startSession {
    dispatch_async(self.sessionQueue, ^{
        [self.session startWithCompleted:^(BOOL success) {
            NSLog(@"success ");
        }];
    });
}

- (void)cameraStreamingSession:(PLCameraStreamingSession *)session streamStatusDidUpdate:(PLStreamStatus *)status {
    NSString *log = [NSString stringWithFormat:@"Stream Status: %@", status];
    NSLog(@"%@", log);
}

- (void)cameraStreamingSession:(PLCameraStreamingSession *)session streamStateDidChange:(PLStreamState)state {
    NSString *log = [NSString stringWithFormat:@"Stream State: %s", stateNames[state]];
    NSLog(@"%@", log);
//    self.textView.text = [NSString stringWithFormat:@"%@\%@", self.textView.text, log];
    
    // 除 PLStreamStateError 外的其余状态会回调在这个方法
    // 这个回调会确保在主线程，所以可以直接对 UI 做操作
//    if (PLStreamStateConnected == state) {
//        [self.actionButton setTitle:NSLocalizedString(@"Stop", nil) forState:UIControlStateNormal];
//    } else if (PLStreamStateDisconnected == state) {
//        [self.actionButton setTitle:NSLocalizedString(@"Start", nil) forState:UIControlStateNormal];
//    }
}
- (void)cameraStreamingSession:(PLCameraStreamingSession *)session didDisconnectWithError:(NSError *)error {
    NSString *log = [NSString stringWithFormat:@"Stream State: Error. %@", error];
    NSLog(@"%@", log);
//    self.textView.text = [NSString stringWithFormat:@"%@\%@", self.textView.text, log];
    // PLStreamStateError 都会回调在这个方法
    // 尝试重连，注意这里需要你自己来处理重连尝试的次数以及重连的时间间隔
//    [self.actionButton setTitle:NSLocalizedString(@"Reconnecting", nil) forState:UIControlStateNormal];
    [self startSession];
}

- (void)reachabilityChanged:(NSNotification *)notif{
    Reachability *curReach = [notif object];
    NSParameterAssert([curReach isKindOfClass:[Reachability class]]);
    NetworkStatus status = [curReach currentReachabilityStatus];
    
    if (NotReachable == status) {
        // 对断网情况做处理
        [self stopSession];
    }
    
    NSString *log = [NSString stringWithFormat:@"Networkt Status: %s", networkStatus[status]];
    NSLog(@"%@", log);
//    self.textView.text = [NSString stringWithFormat:@"%@\%@", self.textView.text, log];
}

- (void)handleInterruption:(NSNotification *)notification {
    if ([notification.name isEqualToString:AVAudioSessionInterruptionNotification]) {
        NSLog(@"Interruption notification");
        
        if ([[notification.userInfo valueForKey:AVAudioSessionInterruptionTypeKey] isEqualToNumber:[NSNumber numberWithInt:AVAudioSessionInterruptionTypeBegan]]) {
            NSLog(@"InterruptionTypeBegan");
        } else {
            // the facetime iOS 9 has a bug: 1 does not send interrupt end 2 you can use application become active, and repeat set audio session acitve until success.  ref http://blog.corywiles.com/broken-facetime-audio-interruptions-in-ios-9
            NSLog(@"InterruptionTypeEnded");
            AVAudioSession *session = [AVAudioSession sharedInstance];
            [session setActive:YES error:nil];
        }
    }
}
@end
