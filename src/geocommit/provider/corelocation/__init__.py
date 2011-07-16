
from os.path import dirname, join, exists

from geocommit.locationprovider import LocationProvider
from geocommit.location import Location

from geocommit.util import system_exit_code

try:
    import json
except ImportError:
    import simplejson as json

EXECUTABLE = join(dirname(__file__), 'geocommit')

if exists(EXECUTABLE):
    ''' I'm on a mac! '''

    class CoreLocationProvider(LocationProvider):
        def location_from_dict(self, data):
            """ Converts JSON response into a location object
            
            >>> clp = CoreLocationProvider()
            >>> clp.location_from_dict({"latitude":1.2, "longitude":3.4}).format_geocommit(" ", ", ")
            'lat 1.2, long 3.4, src cl'
            """
            if not data or not isinstance(data, dict) or not data.get('latitude') or not data.get('longitude'):
                return None
            
            location = Location(data['latitude'], data['longitude'], src='cl')
            optional_keys = {
                'altitude': 'alt',
                'verticalAccuracy': 'vacc',
                'horizontalAccuracy': 'hacc',
                'speed': 'speed',
                'course': 'dir'
            }
            for json_key, loc_key in optional_keys.iteritems():
                if data.has_key(json_key):
                    setattr(location, loc_key, data[json_key])
            return location
        
        def get_location(self):
            ret, value = system_exit_code(EXECUTABLE)
            if ret != 0:
                return None
            
            try:
                parsed_value = json.loads(value)
                return self.location_from_dict(parsed_value)
            except ValueError as e:
                return None
            return None

