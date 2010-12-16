
class Location(object):
    def __init__(self, long=None, lat=None, src=None):
        self.optional_keys = [
            "alt",
            "speed",
            "dir",
            "hacc",
            "vacc"
        ]

        self.long = long
        self.lat = lat
        self.src = src

    def format_geocommit(self, keyval_separator, entry_separator):
        """ Formats the location values separating keys, values and k/v pairs

        >>> l = Location(23.5, 42.1, "test")
        >>> l.format_geocommit(":", ",")
        'long:23.5,lat:42.1,src:test'
        >>> l.alt = 257
        >>> l.format_geocommit(" ", "; ")
        'long 23.5; lat 42.1; alt 257; src test'
        """
        end = entry_separator
        sep = keyval_separator

        msg  = "long" + sep + str(self.long) + end
        msg += "lat"  + sep + str(self.lat)  + end

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

        >>> l = Location(23.5, 42.1, "test")
        >>> l.format_long_geocommit()
        'geocommit (1.0)\\nlong: 23.5\\nlat: 42.1\\nsrc: test\\n\\n'
        """
        geocommit = "geocommit (1.0)\n"
        geocommit += self.format_geocommit(": ", "\n")
        geocommit += "\n\n"

        return geocommit

    def format_short_geocommit(self):
        """ Formats the location using the long geocommit format

        >>> l = Location(23.5, 42.1, "test")
        >>> l.format_short_geocommit()
        'geocommit(1.0): long 23.5, lat 42.1, src test;'
        """
        geocommit = "geocommit(1.0): "
        geocommit += self.format_geocommit(" ", ", ")
        geocommit += ";"

        return geocommit

if __name__ == "__main__":
    import doctest
    doctest.testmod()
