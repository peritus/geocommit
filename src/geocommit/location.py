import re

class Location(object):
    def __init__(self, lat=None, long=None, src=None):
        self.optional_keys = [
            "alt",
            "speed",
            "dir",
            "hacc",
            "vacc"
        ]

        self.lat = lat
        self.long = long
        self.src = src

    def format_geocommit(self, keyval_separator, entry_separator):
        """ Formats the location values separating keys, values and k/v pairs

        >>> l = Location(42.1, 23.5, "test")
        >>> l.format_geocommit(":", ",")
        'lat:42.1,long:23.5,src:test'
        >>> l.alt = 257
        >>> l.format_geocommit(" ", "; ")
        'lat 42.1; long 23.5; alt 257; src test'
        """
        end = entry_separator
        sep = keyval_separator

        msg  = "lat"  + sep + str(self.lat)  + end
        msg += "long" + sep + str(self.long) + end

        for attr in self.optional_keys:
            if hasattr(self, attr):
                val = getattr(self, attr)
                if not val is None:
                    msg += attr + sep + str(val) + end

        # no value separator after last value
        msg += "src" + sep + str(self.src)

        return msg

    def format_long_geocommit(self):
        """ Formats the location using the long geocommit format

        >>> l = Location(42.1, 23.5, "test")
        >>> l.format_long_geocommit()
        'geocommit (1.0)\\nlat: 42.1\\nlong: 23.5\\nsrc: test\\n\\n'
        """
        geocommit = "geocommit (1.0)\n"
        geocommit += self.format_geocommit(": ", "\n")
        geocommit += "\n\n"

        return geocommit

    def format_short_geocommit(self):
        """ Formats the location using the long geocommit format

        >>> l = Location(42.1, 23.5, "test")
        >>> l.format_short_geocommit()
        'geocommit(1.0): lat 42.1, long 23.5, src test;'
        """
        geocommit = "geocommit(1.0): "
        geocommit += self.format_geocommit(" ", ", ")
        geocommit += ";"

        return geocommit

    def __repr__(self):
        return '<Location(%s)>' % self.format_short_geocommit()

    @staticmethod
    def from_short_format(data):
        """ Parses a string in short format to create an instance of the class.

        >>> l = Location.from_short_format(
        ...     "geocommit(1.0): lat 1, long 2, alt 3, src a;")
        >>> l.format_short_geocommit()
        'geocommit(1.0): lat 1, long 2, alt 3, src a;'
        """
        m = re.search("geocommit\(1\.0\): ((?:[a-zA-Z0-9_-]+ [^,;]+, )*)([a-zA-Z0-9_-]+ [^,;]+);", data)

        if m is None:
            return None

        values = m.group(1) + m.group(2)

        data = dict()

        for keyval in re.split(",\s+", values):
            key, val = re.split("\s+", keyval, 1)
            data[key] = val

        if not data.has_key("lat") or not data.has_key("long") or not data.has_key("src"):

            return None

        l = Location(data["lat"], data["long"], data["src"])

        for key in l.optional_keys:
            if data.has_key(key):
                setattr(l, key, data[key])

        return l

