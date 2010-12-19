#import "geocommitdelegate.h"

#import <CoreLocation/CoreLocation.h>
#import <CoreLocation/CLLocationManager.h>
#import <CoreLocation/CLLocationManagerDelegate.h>

@implementation GeoCommitDelegate

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation {

  NSLog(newLocation.description);

  count++;

  if(count >= 2) {
    exit(0);
  }
}

@end

