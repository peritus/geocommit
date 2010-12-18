#import <CoreLocation/CLLocationManagerDelegate.h>

@interface GeoCommitDelegate : NSObject <CLLocationManagerDelegate> {
  NSInteger count;
}

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation;

@end
