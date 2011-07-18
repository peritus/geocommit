#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>

#import "GGCLDelegate.h"


NSString *jsonForCLLocation(CLLocation *location) {
    NSString *result = @"";
    if (location) {
        NSMutableArray *parts = [NSMutableArray array];
        if (location.horizontalAccuracy >= 0.0) {
            [parts addObject:[NSString stringWithFormat:@"\"latitude\":%f", location.coordinate.latitude]];
            [parts addObject:[NSString stringWithFormat:@"\"longitude\":%f", location.coordinate.longitude]];
            [parts addObject:[NSString stringWithFormat:@"\"horizontalAccuracy\":%f", location.horizontalAccuracy]];
        }
        if (location.verticalAccuracy >= 0.0) {
            [parts addObject:[NSString stringWithFormat:@"\"altitude\":%f", location.altitude]];
            [parts addObject:[NSString stringWithFormat:@"\"verticalAccuracy\":%f", location.verticalAccuracy]];
        }
        if (location.speed >= 0.0) {
            [parts addObject:[NSString stringWithFormat:@"\"speed\":%f", location.speed]];
        }
        if (location.course >= 0.0) {
            [parts addObject:[NSString stringWithFormat:@"\"course\":%f", location.course]];
        }
        NSString *partsString = [parts componentsJoinedByString:@",\n    "];
        result = [NSString stringWithFormat:@"{\n    %@\n}\n", partsString];
    }
    return result;
}

int main( int argc, const char* argv[])
{
    int result = EXIT_FAILURE;
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    
    GGCLDelegate *cl = [[GGCLDelegate alloc] init];
    
    NSRunLoop *runLoop = [NSRunLoop currentRunLoop];
    
    while (YES) {
        NSDate *now = [[NSDate alloc] init];
        NSTimer *timer = [[NSTimer alloc] initWithFireDate:now interval:.01 target:cl selector:@selector(start:) userInfo:nil repeats:YES];
        NSDate *terminate = [[NSDate alloc] initWithTimeIntervalSinceNow:1.0];
        
        [runLoop addTimer:timer forMode:NSDefaultRunLoopMode];
        [runLoop runUntilDate:terminate];
        
        [timer invalidate];
        [timer release];
        [now release];
        [terminate release];
        if ([cl goodEnough]) {
            break;
        }
    }
    
    NSString *json = jsonForCLLocation(cl.location);
    [cl release], cl = nil;
    
    if ([json length]) {
        printf("%s\n", [json UTF8String]);
        result = EXIT_SUCCESS;
    }
    
    [pool release], pool = nil;
    return result;
}
