
from os.path import dirname, join, exists

from geocommit.locationprovider import LocationProvider
from geocommit.location import Location

from geocommit.util import system

EXECUTABLE = join(dirname(__file__), 'geocommit')

if exists(EXECUTABLE):
    ''' I'm on a mac! '''

    class CoreLocationProvider(LocationProvider):

        @classmethod
        def parse_funky_description_dump(self, string):
            '''
            Output of the objective-c code looks like this:

            >>> line1 = "2010-12-19 01:25:49.101 geocommit[42427:903] <+53.46278416, +6.32637026> +/- 151.00m (speed -1.00 mps / course -1.00) @ 2010-12-19 01:24:41 +0100\\n"
            >>> line2 = "2010-12-19 01:25:50.244 geocommit[42427:903] <+53.46278817, +12.32638062> +/- 149.00m (speed 0.00 mps / course -1.00) @ 2010-12-19 01:25:49 +0100\\n"
            >>> CoreLocationProvider.parse_funky_description_dump(line1 + line2)
            <Location(geocommit(1.0): lat 53.46278817, long 12.32638062, speed 0.00, dir 0.00, hacc 149.00, src CoreLocation;)>

            This function will always parse the latter location

            >>> CoreLocationProvider.parse_funky_description_dump(line2 + line1)
            <Location(geocommit(1.0): lat 53.46278416, long 6.32637026, speed -1.00, dir -1.00, hacc 151.00, src CoreLocation;)>
            '''

            for line in string.split('\n'):
                tokens = line.split(' ')

                if len(tokens) < 16:
                    # newline at end of output
                    continue

                # [0] '2010-12-19',
                # [1] '01:25:49.101',
                # [2] 'geocommit[42427:903]',
                # [3] '<+53.46278416,',
                lat = tokens[3].lstrip('<+').rstrip(',')
                # [4] '+12.32637026>',
                lng = tokens[4].lstrip('+').rstrip('>')
                # [5] '+/-',
                # [6] '151.00m',
                hacc = tokens[6].rstrip('m')
                # [7] '(speed',
                # [8] '-1.00',
                speed = tokens[8]
                # [9] 'mps',
                # [10] '/',
                # [11] 'course',
                # [12] '-1.00)',
                _dir = tokens[8]
                # [13] '@',
                # [14] '2010-12-19',
                # [15] '01:24:41',
                # [16] '+0100',

            loc = Location(lat, lng, src='cl')
            loc.hacc = hacc
            loc.speed = speed
            loc.dir = _dir
            return loc

        def get_location(self):
            parseme = system(EXECUTABLE)

            return self.parse_funky_description_dump(parseme)

