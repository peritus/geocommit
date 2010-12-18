
from geocommit.wifilocationprovider import WifiLocationProvider

# This function was adapted from Google Chrome Code licensed under 3 clause BSD.
# Copyright (c) 2010 The Chromium Authors. All rights reserved.
def frequency_in_khz_to_channel(frequency_khz):
    '''
    >>> frequency_in_khz_to_channel(2412000)
    1
    '''
    if frequency_khz >= 2412000 and frequency_khz <= 2472000: # Channels 1-13,
        return (frequency_khz - 2407000) / 5000
    if frequency_khz == 2484000:
        return 14
    if frequency_khz > 5000000 and frequency_khz < 6000000: # .11a bands.
        return (frequency_khz - 5000000) / 5000
    # Ignore everything else.
    return -12345 # invalid channel


try:
    import dbus


    class NetworkManager(WifiLocationProvider):
        """ Retrieves a list of access points from wifi cards for geolocation

        This test just makes sure get_location does not throw.
        >>> nm = NetworkManager()
        >>> void = nm.get_location()
        """
        def __init__(self):
            super(NetworkManager, self).__init__()
            self.name = "nmg"

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

except ImportError:
    pass # no dbus available on this system

