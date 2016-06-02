//
//  RCTStreaming.h
//  RCTPili
//
//  Created by guguyanhua on 16/5/26.
//  Copyright © 2016年 pili. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "RCTView.h"
@class RCTEventDispatcher;

@interface RCTStreaming : UIView

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

@end
