//
//  SKHelper.h
//  Feed
//
//  Created by Justin on 5/26/14.
//  Copyright (c) 2014 Saturnboy. All rights reserved.
//

#define IS_IPAD      (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad)
#define IMG(name)    [NSString stringWithFormat:@"%@%@", name, (IS_IPAD ? @"@2x" : @"")]
#define TEX(name)    [SKTexture textureWithImageNamed:IMG(name)]
#define SPRITE(name) [SKSpriteNode spriteNodeWithImageNamed:IMG(name)]

#define COLOR(r,g,b,a) [SKColor colorWithRed:r green:g blue:b alpha:a]
#define COLOR_RGB(r,g,b,a) [SKColor colorWithRed:r/255.0f green:g/255.0f blue:b/255.0f alpha:a]

static inline CGPoint ADD(CGPoint a, CGPoint b) {
    return CGPointMake(a.x + b.x, a.y + b.y);
}

static inline CGPoint SUB(CGPoint a, CGPoint b) {
    return CGPointMake(a.x - b.x, a.y - b.y);
}

static inline CGFloat DIST2(CGPoint a, CGPoint b) {
    CGFloat dx = a.x - b.x;
    CGFloat dy = a.y - b.y;
    return dx*dx + dy*dy;
}

static inline CGFloat DIST(CGPoint a, CGPoint b) {
    return sqrtf(DIST2(a,b));
}

static inline CGFloat MAG(CGPoint p) {
    return sqrtf(p.x * p.x + p.y * p.y);
}

static inline CGPoint NORM(CGPoint p) {
    CGFloat m = MAG(p);
    return CGPointMake(m == 0.0f ? 0.0f : p.x/m,
                       m == 0.0f ? 0.0f : p.y/m);
}

static inline CGPoint MULT(CGPoint p, CGFloat f) {
    return CGPointMake(p.x * f, p.y * f);
}
