//
//  ChatController.h
//  Chat
//
//  Created by Justin on 1/10/14.
//  Copyright (c) 2014 Saturnboy. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ChatController : UIViewController <UICollectionViewDataSource, UICollectionViewDelegate>

@property (nonatomic,strong) IBOutlet UICollectionView *collectionView;
@property (nonatomic,weak) IBOutlet UIBarButtonItem *connectBtn;
@property (nonatomic,weak) IBOutlet UITextField *nameTxt;
@property (nonatomic,weak) IBOutlet UITextField *messageTxt;
@property (nonatomic,weak) IBOutlet UIButton *sendBtn;
@property (nonatomic,weak) IBOutlet UIView *wrapperView;
@property (nonatomic,weak) IBOutlet NSLayoutConstraint *bottomConstraint;

- (IBAction)connectClick:(id)sender;
- (IBAction)sendClick:(id)sender;
- (IBAction)nextClick:(id)sender;
- (IBAction)hideKeyboard:(id)sender;

@end
