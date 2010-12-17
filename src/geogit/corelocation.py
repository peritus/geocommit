import sys
import os

from datetime import datetime
from dateutil.parser import parse

import objc
from objc import YES, NO, NULL
from Foundation import *
from AppKit import *
from geogit.locationprovider import LocationProvider
from geogit.location import Location

errlog = open('/tmp/geogiterr.log','w')
stdlog = open('/tmp/geogitstd.log','w')
sys.stderr = errlog
sys.stdout = stdlog

import CoreLocation
myLocMgr = CoreLocation.CLLocationManager.alloc().init()

class GeoCommitFifo(object):
    def __init__(self, name, path=None):
        if path == None:
             path = os.environ["HOME"] + "/." + name + ".sock"

        self.path = path
        self.reader = None

        try:
            os.mkfifo(path)
        except OSError, e:
            # already created
            pass

    def request(self):
        self.reply("req")

    def waitRequest(self):
        if self.reader is None:
            self.reader = open(self.path, "r")

        content = self.reader.readline().strip('\n\r ')

        return True

    def reply(self, reply):
        writer = open(self.path, "w")

        print >> writer, reply

        writer.close()

    def waitReply(self):
        if self.reader is None:
            self.reader = open(self.path, "r")

        content = self.reader.readline().strip('\n\r ')

        return content

    def __del__(self):
        if not self.reader is None:
            self.reader.close()

class CoreLocationWrapper(LocationProvider):
    def get_location(self):
        fifo_request = GeoCommitFifo("geocommit-req")
        fifo_request.request()
        fifo_reply = GeoCommitFifo("geocommit-reply")
        location = Location.from_short_format(fifo_reply.waitReply())
        return location

class MacLocation(NSObject):

    def last_time(self, timestamp_string, reference_time=None):
        if reference_time == None:
            reference_time = datetime.utcnow()

        then = parse(timestamp_string)
        delta = reference_time - datetime.fromtimestamp(int((then - then.utcoffset()).strftime('%s')))

        return delta.seconds

    def get_location(self):
        ''' Implements LocationProvider.get_location '''

        l = self.last_known_location

        location = Location(
            l.coordinate().latitude,
            l.coordinate().longitude,
            "cl")

        location.alt = l.altitude()
        location.speed = l.speed()
        location.dir = l.course()
        location.hacc = l.horizontalAccuracy()
        location.vacc = l.verticalAccuracy()

        return location

    @objc.signature("v@:@@@")
    def locationManager_didUpdateToLocation_fromLocation_(self, manager, newlocation, oldlocation):
        print 'got new location2'
        stdlog.flush()

        self.last_known_location = newlocation
        location_age = self.last_time(str(newlocation.timestamp()))

        print "location age: ", location_age

        stdlog.flush()

        if location_age > 60:
            return # too old

        myLocMgr.stopUpdatingLocation()

        location = self.get_location().format_short_geocommit()
        fifo_reply = GeoCommitFifo("geocommit-reply")
        fifo_reply.reply(location.format_short_geocommit())

        fifo_request = GeoCommitFifo("geocommit-req")
        fifo_request.waitRequest()
        myLocMgr.startUpdatingLocation()

        #NSApplication.sharedApplication().terminate_(None)

    @objc.signature("v@:@")
    def applicationSuspend_(self, event):
        print "suspend called"
        stdlog.flush()

    @objc.signature("v@:@")
    def applicationDidFinishLaunching_(self, unused):
        print "finished launching"
        stdlog.flush()

        #fifo_request = GeoCommitFifo("geocommit-req")
        #fifo_request.waitRequest()

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
    sys.exit(main())
