import sys

import objc
from objc import YES, NO, NULL
from Foundation import *
from AppKit import *
from geogit import GeoGitFifo, GeoGit

errlog = open('/tmp/geogiterr.log','w')
stdlog = open('/tmp/geogitstd.log','w')
sys.stderr = errlog
sys.stdout = stdlog

import CoreLocation
myLocMgr = CoreLocation.CLLocationManager.alloc().init()


class MacLocation(NSObject, GeoGit):

    def format_location(self):
        ''' Implements GeoGit.format_location '''

        l = self.last_known_location

        return '''\
Generator: GeoGit v0.9
Source: CoreLocation
Altitude: '''            + str(l.altitude())             +  '''
Course: '''              + str(l.course())               +  '''
Horizontal-Accuracy: ''' + str(l.horizontalAccuracy())   +  '''
Latitute: '''            + str(l.coordinate().latitude)  +  '''
Longitude: '''           + str(l.coordinate().longitude) +  '''
Speed: '''               + str(l.speed())                +  '''
Timestamp: '''           + str(l.timestamp())            +  '''
Vertical-Accuracy: '''   + str(l.verticalAccuracy())     +  '''
'''

    @objc.signature("v@:@@@")
    def locationManager_didUpdateToLocation_fromLocation_(self, manager, newlocation, oldlocation):
        print 'got new location2'
        stdlog.flush()

        self.last_known_location = newlocation
        print self.format_location()
        stdlog.flush()

        myLocMgr.stopUpdatingLocation()

        self.attach_note()

    @objc.signature("v@:@")
    def applicationSuspend_(self, event):
        print "suspend called"
        stdlog.flush()

    @objc.signature("v@:@")
    def applicationDidFinishLaunching_(self, unused):
        print "finished launching"
        stdlog.flush()

        ggf = GeoGitFifo()
        self.rev, self.git_dir = ggf.read()

        print "read fifo", locals()
        stdlog.flush()

        myLocMgr.setDelegate_(self)
        accuracy = -1.0
        myLocMgr.setDesiredAccuracy_(accuracy)
        myLocMgr.startUpdatingLocation()

        print "start updating location"
        stdlog.flush()

    @objc.signature("v@:")
    def applicationDidResume(self):
        self.terminate()

    @objc.signature("v@:")
    def applicationWillTerminate(self):
        print "will terminate"
        myLocMgr.stopUpdatingLocation()
        self.removeApplicationBadge()

def main():
    app = NSApplication.sharedApplication()

    delegate = MacLocation.alloc().init()
    NSApp().setDelegate_(delegate)

    app.run()

if __name__ == "__main__":
    main()
