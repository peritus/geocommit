from geocommit.locationprovider import LocationProvider
from geocommit.location import Location
import json
import urllib2
import sys


class WifiLocationProvider(LocationProvider):
    """ Base class for providers using wifi data and google geolocation

    Documentation of the protocol can be found at:
    http://code.google.com/apis/gears/geolocation_network_protocol.html
    """
    def __init__(self):
        super(WifiLocationProvider, self).__init__()
        self.webservice = "https://www.google.com/loc/json"
        self.access_token = None

    def get_access_points(self):
        """ Retrieves all nearby access points.

        Should be overwritten in specialisations of this class.
        """
        return {"invalid mac": {"mac": "invalid mac", "ssid": "none"}}

    def request_dict(self):
        """ Creates a JSON request string for location information from google.

        The access points are a map from mac addresses to access point
        information dicts.

        >>> wlp = WifiLocationProvider()
        >>> wlp.request_dict()["wifi_towers"]
        [{'mac': 'invalid mac', 'ssid': 'none'}]
        """
        ap_map = self.get_access_points()

        if not ap_map:
            return None

        request = dict()

        request["version"] = "1.1.0"
        request["host"] = "localhost"
        request["request_address"] = True
        request["address_language"] = "en_GB"
        request["wifi_towers"] = ap_map.values()

        if self.access_token:
            request["access_token"] = self.access_token

        return request

    def location_from_dict(self, data):
        """ Converts a Google JSON response into a location object

        >>> wlp = WifiLocationProvider()
        >>> wlp.location_from_dict({"location":
        ...     {"latitude": 1.2, "longitude": 3.4}}).format_geocommit(" ", ", ")
        'lat 1.2, long 3.4, src None'
        """
        if not data.has_key("location"):
            return None

        ldata = data["location"]

        location = Location(ldata["latitude"], ldata["longitude"], self.name)

        optional_keys = {
            "altitude": "alt",
            "accuracy": "hacc",
            "altitude_accuracy": "vacc"
        }

        for json_key, loc_key in optional_keys.iteritems():
            if ldata.has_key(json_key):
                setattr(location, loc_key, ldata[json_key])

        return location

    def json_request(self, data):
        """ Sends a JSON request to google geolocation and parses the response

        >>> wlp = WifiLocationProvider()
        >>> wlp.webservice = "http://unresolvable"
        >>> wlp.json_request({})
        """
        json_request = json.dumps(data, indent=4)

        try:
            result = urllib2.urlopen(self.webservice, json_request).read()
        except urllib2.URLError, e:
            return None

        try:
            response = json.loads(result)
        except ValueError, e:
            return None

        return response

    def get_location(self):
        """ Retrieves a location from Google Geolocation API based on Wifi APs.
        """
        request = self.request_dict()

        if not request:
            return None

        location = self.json_request(request)

        if not location:
            return None

        if location.has_key("access_token"):
            self.access_token = location["access_token"]

        return self.location_from_dict(location)

