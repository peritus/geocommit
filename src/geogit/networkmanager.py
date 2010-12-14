import sys
import dbus
import json
import urllib2
from geogit import GeoGit


#class MacLocation(NSObject):
    #@objc.signature("v@:@@@")
    #def locationManager_didUpdateToLocation_fromLocation_(self, manager, newlocation, oldlocation):
        #print 'got new location'
        #print newlocation
        #stdlog.flush()

##    @objc.signature("v@:@")
##    def locationManager_didFailWithError_(self, error):
##        print 'error'
##        print error
##        stdlog.flush()

    #@objc.signature("v@:@")
    #def applicationSuspend_(self, event):
        #print "suspend called"
        #stdlog.flush()

    #@objc.signature("v@:@")
    #def applicationDidFinishLaunching_(self, unused):
        #print "finished launching"
        #myLocMgr.setDelegate_(self)
        #accuracy = -1.0
        #myLocMgr.setDesiredAccuracy_(accuracy)
        #myLocMgr.startUpdatingLocation()
        #stdlog.flush()
        #self.setApplicationBadge_("On")

    #@objc.signature("v@:")
    #def applicationDidResume(self):
        #self.terminate()

    #@objc.signature("v@:")
    #def applicationWillTerminate(self):
        #print "will terminate"
        #myLocMgr.stopUpdatingLocation()
        #self.removeApplicationBadge()

# This function was adapted from Google Chrome Code licensed under 3 clause BSD.
# Copyright (c) 2010 The Chromium Authors. All rights reserved.
def frequency_in_khz_to_channel(frequency_khz):
    if frequency_khz >= 2412000 and frequency_khz <= 2472000: # Channels 1-13,
        return (frequency_khz - 2407000) / 5000
    if frequency_khz == 2484000:
        return 14
    if frequency_khz > 5000000 and frequency_khz < 6000000: # .11a bands.
        return (frequency_khz - 5000000) / 5000
    # Ignore everything else.
    return -12345 # invalid channel


class NetworkManager(GeoGit):

    def __init__(self):
        self.git_dir = None
        self.service_name = "org.freedesktop.NetworkManager";
        self.path = "/org/freedesktop/NetworkManager";
        self.interface = "org.freedesktop.NetworkManager";
        self.ns_properties = "org.freedesktop.DBus.Properties";
        # http://projects.gnome.org/NetworkManager/developers/spec.html
        self.wifi_type = 2
        self.access_token = None

        self.bus = dbus.SystemBus()
        self.nm = self.bus.get_object(self.service_name, self.path)

    def get_devices(self):
        devices = self.nm.GetDevices(dbus_interface=self.interface)

        device_list = []

        for device_path in devices:
            device = self.bus.get_object(self.service_name, device_path)

            device_type = device.Get(
                self.interface + ".Device",
                "DeviceType",
                dbus_interface=self.ns_properties)

            if device_type == self.wifi_type:
                device_list.append(device_path)

        return device_list

    def get_access_points_for_device(self, device_path, ap_map):
        device = self.bus.get_object(self.service_name, device_path)
        aps = device.GetAccessPoints(
            dbus_interface=self.interface + ".Device.Wireless")

        for ap_path in aps:
            ap = self.bus.get_object(self.service_name, ap_path)

            ap_data = dict()
            ap_data["ssid"] = str(ap.Get(
                self.interface + ".AccessPoint",
                "Ssid",
                dbus_interface=self.ns_properties,
                byte_arrays=True))

            ap_data["mac_address"] = str(ap.Get(
                self.interface + ".AccessPoint",
                "HwAddress",
                dbus_interface=self.ns_properties,
                byte_arrays=True))

            strength = ap.Get(
                self.interface + ".AccessPoint",
                "Strength",
                dbus_interface=self.ns_properties)
            # convert into dB percentage
            ap_data["signal_strength"] = -100 + strength / 2;

            frequency = ap.Get(
                self.interface + ".AccessPoint",
                "Frequency",
                dbus_interface=self.ns_properties)
            ap_data["channel"] = frequency_in_khz_to_channel(frequency * 1000)

            ap_map[ap_data["mac_address"]] = ap_data

    def get_access_points(self):
        ap_map = dict()

        for device_path in self.get_devices():
            self.get_access_points_for_device(device_path, ap_map)

        return ap_map

    def get_json_request(self):
        ap_map = self.get_access_points()

        request = dict()

        request["version"] = "1.1.0"
        request["host"] = "localhost"
        request["request_address"] = True
        request["address_language"] = "en_GB"
        request["wifi_towers"] = ap_map.values()

        if self.access_token:
            request["access_token"] = self.access_token

        return json.dumps(request, indent=4)

    def get_location(self):
        req = self.get_json_request()

        try:
            result = urllib2.urlopen("https://www.google.com/loc/json", req).read()
        except urllib2.URLError, e:
            print e
            return None

        res = json.loads(result)

        if res.has_key("access_token"):
            self.access_token = res["access_token"]

        return res

    def format_location(self):
        l = self.get_location()["location"]
        print l
        #l["altitude"]
        #l["altitude_accuracy"]
        return '''\
Generator: GeoGit v0.9
Source: NetworkManager with Google Geolocation
Altitude: '''            + str("")          +  '''
Horizontal-Accuracy: ''' + str(l["accuracy"])          +  '''
Latitude: '''            + str(l["latitude"])          +  '''
Longitude: '''           + str(l["longitude"])         +  '''
Timestamp: '''           + str("")                     +  '''
Vertical-Accuracy: '''   + str("") +  '''
''' # address


def main():
    nm = NetworkManager()

    location = nm.get_location()
    import ipdb; ipdb.set_trace()

if __name__ == "__main__":
    sys.exit(main())
