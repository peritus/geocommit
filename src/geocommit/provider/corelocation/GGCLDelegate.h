//
//  GGCLDelegate.h
//  git-gps
//
//  Created by Andrew Wooster on 7/2/11.
//  Copyright 2011 Andrew Wooster. All rights reserved.
//

#import <Cocoa/Cocoa.h>
#import <CoreLocation/CoreLocation.h>


@interface GGCLDelegate : NSObject <CLLocationManagerDelegate> {
    CLLocation *location;
@private
    BOOL isTracking;
    BOOL dirtied;
    NSUInteger timerCount;
    NSUInteger updateCount;
    CLLocationManager *locationManager;
    BOOL didFail;
}
@property (nonatomic, retain) CLLocation *location;
- (void)start:(NSTimer *)timer;
- (BOOL)goodEnough;
@end
