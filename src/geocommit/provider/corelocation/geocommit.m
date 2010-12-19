#import <Foundation/foundation.h>
#import <CoreLocation/CoreLocation.h>

#import "geocommitdelegate.h"

int main( int argc, const char* argv[])
{
  NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

  GeoCommitDelegate *gcd = [[[GeoCommitDelegate alloc] init] autorelease];

  CLLocationManager *locationManager = [[[CLLocationManager alloc] init] autorelease];
  locationManager.delegate = gcd;

  if (locationManager.locationServicesEnabled == NO) {
    NSLog(@"location services disabled!");
    exit(5);
  }

  locationManager.desiredAccuracy = 1;

  [locationManager startUpdatingLocation];

  NSRunLoop* myRunLoop = [NSRunLoop currentRunLoop];
  [myRunLoop run];

  [pool release];
  return 0;
}
