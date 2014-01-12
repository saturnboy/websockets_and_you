//
//  ChatCell.m
//  Chat
//
//  Created by Justin on 1/10/14.
//  Copyright (c) 2014 Saturnboy. All rights reserved.
//

#import "ChatCell.h"

@implementation ChatCell

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        // THIS IS NOT CALLED!
    }
    return self;
}

- (void)setupCell:(NSString *)message {
    self.messageLbl.text = message;
}

@end
