//
//  ChatCell.h
//  Chat
//
//  Created by Justin on 1/10/14.
//  Copyright (c) 2014 Saturnboy. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChatCell : UICollectionViewCell

@property (nonatomic,weak) IBOutlet UILabel *messageLbl;

- (void)setupCell:(NSString *)message;

@end
