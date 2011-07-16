//
//  GGCLDelegate.m
//  git-gps
//
//  Created by Andrew Wooster on 7/2/11.
//  Copyright 2011 Andrew Wooster. All rights reserved.
//

#import "GGCLDelegate.h"


@implementation GGCLDelegate 
@synthesize location;

- (id)init {
    if ((self = [super init])) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
    }
    return self;
}

- (void)dealloc {
    self.location = nil;
    locationManager.delegate = nil;
    [locationManager stopUpdatingLocation];
    [locationManager release], locationManager = nil;
    [super dealloc];
}

- (void)start:(NSTimer *)timer {
    if (!isTracking) {
        updateCount = 0;
        isTracking = YES;
        [locationManager startUpdatingLocation];
    }
    timerCount++;
}

- (BOOL)goodEnough {
    if (![locationManager locationServicesEnabled]) return YES;
    if (!dirtied) return NO;
    if (didFail) return YES;
    if (updateCount >= 2 && timerCount >= 20) return YES;
    return NO;
}

#pragma mark CLLocationManagerDelegate
- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {
    dirtied = YES;
    updateCount++;
    self.location = newLocation;
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    dirtied = YES;
    isTracking = NO;
    didFail = YES;
}

@end
