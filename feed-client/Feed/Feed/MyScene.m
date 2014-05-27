//
//  MyScene.m
//  Feed
//
//  Created by Shacklette, Justin on 5/26/14.
//  Copyright (c) 2014 Saturnboy. All rights reserved.
//

#import "MyScene.h"
#import "SKHelper.h"

#define DT_MAX 1.0f
#define V_MAX 100.0f
#define V_MIX 0.1f
#define T_MIN 1.0f
#define T_MED 25.0f

@interface MyScene ()

@property (nonatomic, strong) SKSpriteNode *red;

@property (nonatomic, strong) SKSpriteNode *target;

@property (nonatomic, strong) SKTexture *blueTex;
@property (nonatomic, strong) NSArray *blues;

@property (nonatomic, strong) SKTexture *foodTex;
@property (nonatomic, strong) NSArray *foods;

@property (nonatomic, assign) CGFloat D2;
@property (nonatomic, assign) CFTimeInterval lastTime;

@end

@implementation MyScene

-(id)initWithSize:(CGSize)size {    
    if (self = [super initWithSize:size]) {
         NSLog(@"MyScene: sz=%.0fx%.0f", size.width, size.height);
        
        self.backgroundColor = COLOR(0.2f,0.2f,0.2f,1.0f);
        
        _red = SPRITE(@"red");
        _red.position = CGPointMake(size.width * 0.5f, size.height * 0.5f);
        _red.userData = [@{ @"v":[NSNumber valueWithCGPoint:CGPointZero] } mutableCopy];
        [self addChild:_red];
        
        _target = SPRITE(@"target");
        _target.position = _red.position;
        _target.alpha = 0.0f;
        _target.zPosition = 10.0f;
        [self addChild:_target];
        
        _blueTex = TEX(@"blue");
        _blues = @[];
        
        _foodTex = TEX(@"food");
        _foods = @[[SKSpriteNode spriteNodeWithTexture:_foodTex],
                   [SKSpriteNode spriteNodeWithTexture:_foodTex],
                   [SKSpriteNode spriteNodeWithTexture:_foodTex],
                   [SKSpriteNode spriteNodeWithTexture:_foodTex],
                   [SKSpriteNode spriteNodeWithTexture:_foodTex]];
        
        for (SKSpriteNode *food in _foods) {
            food.alpha = 0.0f;
            food.userData = [@{ @"v":[NSNumber valueWithCGPoint:CGPointZero] } mutableCopy];
            [self addChild:food];
        }
        
        ((SKSpriteNode *)_foods[0]).position = CGPointMake(100.0f, 100.0f);
        ((SKSpriteNode *)_foods[0]).alpha = 1.0f;
        ((SKSpriteNode *)_foods[0]).userData = [@{ @"v":[NSNumber valueWithCGPoint:CGPointMake(0.0f, 10.0f)] } mutableCopy];
        ((SKSpriteNode *)_foods[1]).position = CGPointMake(140.0f, 140.0f);
        ((SKSpriteNode *)_foods[1]).alpha = 1.0f;
        ((SKSpriteNode *)_foods[2]).position = CGPointMake(180.0f, 180.0f);
        ((SKSpriteNode *)_foods[2]).alpha = 1.0f;
        
        CGFloat D = (_red.size.width + _foodTex.size.width) * 0.5f;
        _D2 = D * D;
    }
    return self;
}

-(void)touchesBegan:(NSSet *)touches withEvent:(UIEvent *)event {
    UITouch *touch = [touches anyObject];
    CGPoint pos = [touch locationInNode:self];
    
    _target.position = pos;
    _target.alpha = 1.0f;
}

-(void)update:(CFTimeInterval)currentTime {
    if (self.paused == YES) {
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
    
    // eat food?
    for (SKSpriteNode *food in _foods) {
        if (food.alpha > 0.0f && DIST2(food.position, _red.position) < _D2) {
            //red eats food!
            food.alpha = 0.0f;
            food.userData[@"v"] = [NSNumber valueWithCGPoint:CGPointZero];
            
            SKSpriteNode *f = [SKSpriteNode spriteNodeWithTexture:_foodTex];
            f.position = food.position;
            [self addChild:f];
            [f runAction:[SKAction sequence:@[[SKAction fadeAlphaTo:0.0f duration:1.0f],
                                              [SKAction removeFromParent]]]];
        }
    }
}

@end
