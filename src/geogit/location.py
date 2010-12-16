
class Location(object):
    def __init__(self, long, lat, src):
        self.long = long
        self.lat = lat
        self.src = src
        self.alt = None
        self.speed = None
        self.dir = None
        self.hacc = None
        self.vacc = None

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

        if not self.alt is None:
            msg += "alt"   + sep + str(self.alt)   + end
        if not self.speed is None:
            msg += "speed" + sep + str(self.speed) + end
        if not self.dir is None:
            msg += "dir"   + sep + str(self.dir)   + end
        if not self.hacc is None:
            msg += "hacc"  + sep + str(self.hacc)  + end
        if not self.vacc is None:
            msg += "vacc"  + sep + str(self.vacc)  + end

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
