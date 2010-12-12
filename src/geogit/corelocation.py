import sys

import objc
from objc import YES, NO, NULL
from Foundation import *
from AppKit import *

errlog = open('/tmp/geogiterr.log','w')
stdlog = open('/tmp/geogitstd.log','w')
sys.stderr = errlog
sys.stdout = stdlog

import CoreLocation
myLocMgr = CoreLocation.CLLocationManager.alloc().init()

class MacLocation(NSObject):
    @objc.signature("v@:@@@")
    def locationManager_didUpdateToLocation_fromLocation_(self, manager, newlocation, oldlocation):
        print 'got new location'
        print newlocation
        stdlog.flush()

    @objc.signature("v@:@")
    def applicationSuspend_(self, event):
        print "suspend called"
        stdlog.flush()

    @objc.signature("v@:@")
    def applicationDidFinishLaunching_(self, unused):
        print "finished launching"
        myLocMgr.setDelegate_(self)
        accuracy = -1.0
        myLocMgr.setDesiredAccuracy_(accuracy)
        myLocMgr.startUpdatingLocation()
        stdlog.flush()
        self.setApplicationBadge_("On")

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

