
from os.path import dirname, join, exists

from geocommit.locationprovider import LocationProvider
from geocommit.location import Location

from geocommit.util import system

EXECUTABLE = join(dirname(__file__), 'geocommit')

if exists(EXECUTABLE):
    ''' I'm on a mac! '''

    class CoreLocationProvider(LocationProvider):
        def parse_funky_description_dump(self, string):
            lat = 52.0
            long = 6.0
            return Location(lat, long, src='CoreLocation')

        def get_location(self):
            parseme = system(EXECUTABLE)

            return self.parse_funky_description_dump(parseme)

