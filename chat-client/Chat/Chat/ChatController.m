//
//  ChatController.m
//  Chat
//
//  Created by Justin on 1/10/14.
//  Copyright (c) 2014 Saturnboy. All rights reserved.
//

#import "ChatController.h"
#import "ChatCell.h"
#import "SRWebSocket.h"

#define URL @"ws://10.10.10.17:7006/chat/"

@interface ChatController () <SRWebSocketDelegate>

@property (nonatomic,strong) NSMutableArray *messages;
@property (nonatomic,strong) SRWebSocket *webSocket;

@end

@implementation ChatController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    //init messages
    self.messages = [NSMutableArray array];
    
    //listen for all the keyboard events
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardDidShow:) name:UIKeyboardDidShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyboardWillHide:) name:UIKeyboardWillHideNotification object:nil];
}

-(void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    //connect to websocket!
    [self connect];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - collection view datasource

- (NSInteger)numberOfSectionsInCollectionView:(UICollectionView*)collectionView {
    return 1;
}

- (NSInteger)collectionView:(UICollectionView*)collectionView numberOfItemsInSection:(NSInteger)section {
    return self.messages.count;
}

- (UICollectionViewCell *)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath {
    ChatCell *cell = [self.collectionView dequeueReusableCellWithReuseIdentifier:@"ChatCell" forIndexPath:indexPath];
    
    NSLog(@"CHAT %ld -- collView.frame=%.0fx%.0f cell.frame=%.0fx%.0f",
              (long)indexPath.item,
              self.collectionView.frame.size.width, self.collectionView.frame.size.height,
              cell.frame.size.width, cell.frame.size.height);
    
    NSString *msg = self.messages[indexPath.item];
    [cell setupCell:msg];
    
    if ([msg hasPrefix:[NSString stringWithFormat:@"%@|", self.nameTxt.text]]) {
        cell.backgroundColor = [UIColor colorWithRed:0.2f green:0.6f blue:1.0f alpha:0.2f];
    } else {
        cell.backgroundColor = [UIColor colorWithRed:0.0f green:0.0f blue:0.0f alpha:0.2f];
    }
    
    return cell;
}

#pragma mark - collection view cell size

- (CGSize)collectionView:(UICollectionView *)collectionView layout:(UICollectionViewLayout *)collectionViewLayout sizeForItemAtIndexPath:(NSIndexPath *)indexPath {
    // expand cell to fill the entire view width
    return CGSizeMake(self.collectionView.frame.size.width, 40.0f);
}

#pragma mark - keyboard events

- (void)keyboardWillShow:(NSNotification*)notification {
    NSLog(@"WILL_SHOW");
    
    //compute keyboard size
    CGSize sz = [[[notification userInfo] objectForKey:UIKeyboardFrameBeginUserInfoKey] CGRectValue].size;
    
    //increase bottom constraint to keep text field above keyboard (check portrait vs landscape)
    self.bottomConstraint.constant = (self.view.bounds.size.width < self.view.bounds.size.height ? sz.height : sz.width);
}

- (void)keyboardDidShow:(NSNotification *)notification {
    NSLog(@"DID_SHOW");
    
    //scroll chat to bottom
    NSIndexPath* idx = [NSIndexPath indexPathForItem:(self.messages.count-1) inSection:0];
    [self.collectionView scrollToItemAtIndexPath:idx atScrollPosition:UICollectionViewScrollPositionBottom animated:YES];
}

- (void)keyboardWillHide:(NSNotification*)notification {
    NSLog(@"WILL_HIDE");
    
    //keyboard hidden, so bottom constraint back to zero
    self.bottomConstraint.constant = 0;
}

#pragma mark - callbacks

- (IBAction)connectClick:(id)sender {
    NSLog(@"connectClick");
    if ([self.connectBtn.title isEqualToString:@"DISCONNECT"]) {
        [self disconnect];
    } else {
        [self connect];
    }
}

- (IBAction)sendClick:(id)sender {
    NSLog(@"sendClick");
    
    //send msg to server
    NSString *msg = [NSString stringWithFormat:@"%@|%@", self.nameTxt.text, self.messageTxt.text];
    [self.webSocket send:msg];
    self.messageTxt.text = nil;
}

-(IBAction)nextClick:(id)sender {
    NSLog(@"nextClick");
    if ([self.nameTxt isFirstResponder]) {
        [self.messageTxt becomeFirstResponder];
    }
}

- (IBAction)hideKeyboard:(id)sender {
    NSLog(@"hideKeyboard");
    if ([self.nameTxt isFirstResponder]) {
        [self.nameTxt resignFirstResponder];
    } else if ([self.messageTxt isFirstResponder]) {
        [self.messageTxt resignFirstResponder];
    }
}

#pragma mark - web socket

- (void)disconnect {
    [self.webSocket close];
}

- (void)connect {
    [self disconnect];
    self.webSocket = [[SRWebSocket alloc] initWithURLRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:URL]]];
    self.webSocket.delegate = self;
    [self.webSocket open];
}

#pragma mark - web socket delegate

- (void)webSocketDidOpen:(SRWebSocket *)webSocket {
    NSLog(@"websocket open");
    self.connectBtn.title = @"DISCONNECT";
    self.messages = [NSMutableArray array];
}

- (void)webSocket:(SRWebSocket *)webSocket didFailWithError:(NSError *)error {
    NSLog(@"websocket err: %@", error);
    self.connectBtn.title = @"CONNECT";
    self.webSocket = nil;
}

- (void)webSocket:(SRWebSocket *)webSocket didReceiveMessage:(id)message {
    NSLog(@"websocket msg: %@", message);
    [self.messages addObject:(NSString *)message];
    
    [self.collectionView reloadData];
    NSIndexPath* idx = [NSIndexPath indexPathForItem:(self.messages.count-1) inSection:0];
    [self.collectionView scrollToItemAtIndexPath:idx atScrollPosition:UICollectionViewScrollPositionBottom animated:NO];
}

- (void)webSocket:(SRWebSocket *)webSocket didCloseWithCode:(NSInteger)code reason:(NSString *)reason wasClean:(BOOL)wasClean {
    NSLog(@"websocket close: %ld %@", (long)code, reason);
    self.connectBtn.title = @"CONNECT";
    self.webSocket = nil;
}

@end