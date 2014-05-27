//
//  MyScene.m
//  Feed
//
//  Created by Shacklette, Justin on 5/26/14.
//  Copyright (c) 2014 Saturnboy. All rights reserved.
//

#import "MyScene.h"
#import "SKHelper.h"
#import "SRWebSocket.h"

#define URL @"ws://10.10.10.17:7007/feed/"

#define DT_MAX 1.0f
#define V_MAX 100.0f
#define V_MIX 0.1f
#define T_MIN 1.0f
#define T_MED 25.0f

#define X_SZ 320.0f
#define Y_SZ 320.0f

@interface MyScene () <SRWebSocketDelegate>

@property (nonatomic,strong) SRWebSocket *webSocket;

@property (nonatomic, strong) SKSpriteNode *red;
@property (nonatomic, strong) SKSpriteNode *target;
@property (nonatomic, strong) SKTexture *blueTex;
@property (nonatomic, strong) NSMutableArray *blues;
@property (nonatomic, strong) SKTexture *foodTex;
@property (nonatomic, strong) NSMutableArray *foods;
@property (nonatomic, strong) SKAction *eatAction;

@property (nonatomic, assign) BOOL started;
@property (nonatomic, assign) CGFloat D2;
@property (nonatomic, assign) CFTimeInterval lastTime;

@end

@implementation MyScene

-(id)initWithSize:(CGSize)size {    
    if (self = [super initWithSize:size]) {
         NSLog(@"MyScene: sz=%.0fx%.0f", size.width, size.height);
        
        _started = NO;
        self.backgroundColor = COLOR(0.2f,0.2f,0.2f,1.0f);
        
        _red = SPRITE(@"red");
        _red.position = CGPointMake(size.width * 0.5f, size.height * 0.5f);
        _red.userData = [@{ @"v":[NSNumber valueWithCGPoint:CGPointZero] } mutableCopy];
        _red.zPosition = 1.0f;
        [self addChild:_red];
        
        _target = SPRITE(@"target");
        _target.position = _red.position;
        _target.alpha = 0.0f;
        _target.zPosition = 3.0f;
        [self addChild:_target];
        
        _blueTex = TEX(@"blue");
        _blues = [@[] mutableCopy];
        
        _foodTex = TEX(@"food");
        _foods = [@[] mutableCopy];
        
        CGFloat D = (_red.size.width + _foodTex.size.width) * 0.5f;
        _D2 = D * D;
        
        _eatAction = [SKAction sequence:@[[SKAction group:@[[SKAction scaleTo:0.1f duration:0.6f],
                                                            [SKAction fadeAlphaTo:0.0f duration:0.6f]]],
                                          [SKAction removeFromParent]]];
        
        //connect to server (after 1s)
        [self performSelector:@selector(connect) withObject:nil afterDelay:1.0f];
    }
    return self;
}

-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    if (!_started) {
        return;
    }
    
    UITouch *touch = [touches anyObject];
    CGPoint pos = [touch locationInNode:self];
    
    _target.position = pos;
    _target.alpha = 1.0f;
}

-(void)update:(CFTimeInterval)currentTime {
    if (!_started || self.paused == YES) {
        return;
    }
    
    // compute delta time
    CFTimeInterval dt = currentTime - _lastTime;
    _lastTime = currentTime;
    if (dt > DT_MAX) {
        dt = 0.0166667f;
    }
    
    // move food
    for (SKSpriteNode *food in _foods) {
        CGPoint vF = [food.userData[@"v"] CGPointValue];
        food.position = CGPointMake(food.position.x + vF.x * dt,
                                    food.position.y + vF.y * dt);
    }
    
    // compute red's velocity
    CGPoint v = CGPointZero;
    CGPoint pD = SUB(_target.position, _red.position);
    CGFloat mag = MAG(pD);
    if (mag > T_MIN) {
        CGPoint vN = NORM(pD);
        CGPoint vO = NORM([_red.userData[@"v"] CGPointValue]);
        v = ADD(MULT(vO, mag < T_MED ? mag/T_MED : 1.0f), MULT(vN, V_MIX));
        v = MULT(v, V_MAX);
    } else {
        _target.alpha = 0.0f;
    }
    _red.userData[@"v"] = [NSNumber valueWithCGPoint:v];
    
    // move red towards target
    _red.position = CGPointMake(_red.position.x + v.x * dt,
                                _red.position.y + v.y * dt);
    
    // update server pos
    [self.webSocket send:[NSString stringWithFormat:@"POS|%1.4f %1.4f", _red.position.x/X_SZ, _red.position.y/Y_SZ]];
    
    // eat food?
    for (SKSpriteNode *food in _foods) {
        if (food.alpha > 0.0f && DIST2(food.position, _red.position) < _D2) {
            //red eats food!
            NSInteger foodId = [food.userData[@"id"] integerValue];
            [self.webSocket send:[NSString stringWithFormat:@"EAT|%ld", (long)foodId]];
            
            [_foods removeObject:food];
            [food runAction:_eatAction];
            break;
        }
    }
}

# pragma mark - scene lifecycle

- (void)willMoveFromView:(SKView *)view {
    [super willMoveFromView:view];
    [self disconnect];
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
    NSLog(@"websocket : open");
    
    [self.webSocket send:[@"NAME|" stringByAppendingString:[UIDevice currentDevice].name]];
}

- (void)webSocket:(SRWebSocket *)webSocket didFailWithError:(NSError *)error {
    NSLog(@"websocket : error %@", error);
    self.webSocket = nil;
}

- (void)webSocket:(SRWebSocket *)webSocket didCloseWithCode:(NSInteger)code reason:(NSString *)reason wasClean:(BOOL)wasClean {
    NSLog(@"websocket : close %ld %@", (long)code, reason);
    self.webSocket = nil;
}

- (void)webSocket:(SRWebSocket *)webSocket didReceiveMessage:(id)message {
    NSString *msg = (NSString *)message;
    if ([msg hasPrefix:@"START|"]) {
        //game start
        _started = YES;
        
        NSLog(@"websocket : START");
    } else if ([msg hasPrefix:@"END|"]) {
        //game over
        _started = NO;
        _target.alpha = 0.0f;
        NSString *data = [msg substringFromIndex:4];
        NSArray *parts = [data componentsSeparatedByString:@" "];
        
        NSLog(@"websocket : END %@", [parts componentsJoinedByString:@","]);
    } else if ([msg hasPrefix:@"WELCOME|"]) {
        //init red player
        NSString *data = [msg substringFromIndex:8];
        NSArray *parts = [data componentsSeparatedByString:@" "];
        NSInteger playerId = [parts[0] integerValue];
        CGFloat x = [parts[1] floatValue];
        CGFloat y = [parts[2] floatValue];
        
        CGPoint pos = CGPointMake(x * X_SZ, y * Y_SZ);
        //_red.position = pos;
        [_red runAction:[SKAction moveTo:pos duration:0.3f]];
        _target.position = pos;
        _red.userData = [@{ @"id":@(playerId),
                            @"v":[NSNumber valueWithCGPoint:CGPointZero] } mutableCopy];
        
        NSLog(@"websocket : WELCOME %ld %1.4f,%1.4f", (long)playerId, pos.x, pos.y);
    } else if ([msg hasPrefix:@"FOOD|"]) {
        //spawn food
        NSString *data = [msg substringFromIndex:5];
        NSArray *parts = [data componentsSeparatedByString:@" "];
        NSInteger foodId = [parts[0] integerValue];
        CGFloat x = [parts[1] floatValue];
        CGFloat y = [parts[2] floatValue];
        CGFloat vx = [parts[3] floatValue];
        CGFloat vy = [parts[4] floatValue];

        SKSpriteNode *food = [SKSpriteNode spriteNodeWithTexture:_foodTex];
        food.alpha = 0.0f;
        food.position = CGPointMake(x * X_SZ, y * Y_SZ);
        food.userData = [@{ @"id":@(foodId),
                            @"v":[NSNumber valueWithCGPoint:CGPointMake(vx,vy)] } mutableCopy];
        food.zPosition = 2.0f;
        [_foods addObject:food];
        [self addChild:food];
        [food runAction:[SKAction fadeAlphaTo:1.0f duration:0.3f]];
        
        NSLog(@"websocket : FOOD %ld %1.4f,%1.4f", (long)foodId, food.position.x, food.position.y);
    } else if ([msg hasPrefix:@"YUM|"]) {
        //remove food
        NSString *data = [msg substringFromIndex:4];
        NSInteger foodId = [data integerValue];
        
        for (SKSpriteNode *food in _foods) {
            if (foodId == [food.userData[@"id"] integerValue]) {
                [_foods removeObject:food];
                [food runAction:_eatAction];
                break;
            }
        }
        
        NSLog(@"websocket : YUM %ld", (long)foodId);
    } else if ([msg hasPrefix:@"OTHER|"]) {
        //move blue
        NSString *data = [msg substringFromIndex:6];
        NSArray *parts = [data componentsSeparatedByString:@" "];
        NSInteger blueId = [parts[0] integerValue];
        CGFloat x = [parts[1] floatValue];
        CGFloat y = [parts[2] floatValue];
        
        BOOL found = NO;
        for (SKSpriteNode *blue in _blues) {
            if (blueId == [blue.userData[@"id"] integerValue]) {
                blue.position = CGPointMake(x * X_SZ, y * Y_SZ);
                found = YES;
                break;
            }
        }
        
        if (found == NO) {
            //didn't find it in the list, must be first time, so add it
            SKSpriteNode *blue = [SKSpriteNode spriteNodeWithTexture:_blueTex];
            blue.alpha = 0.0f;
            blue.position = CGPointMake(x * X_SZ, y * Y_SZ);
            blue.userData = [@{ @"id":@(blueId),
                                @"v":[NSNumber valueWithCGPoint:CGPointZero] } mutableCopy];
            blue.zPosition = 0.0f;
            [_blues addObject:blue];
            [self addChild:blue];
            [blue runAction:[SKAction fadeAlphaTo:1.0f duration:0.3f]];
        }
    }
}

@end