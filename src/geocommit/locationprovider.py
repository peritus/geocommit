from distutils.util import get_platform

class LocationProvider(object):
    """ Base class for all location providers which share
    """
    def __init__(self):
        self.name = None

    def get_location(self):
        """ Retrieves a location using this LocationProvider.

        Should be overwritten in specialisations of this class.
        """
        return None

    @staticmethod
    def new():
        if get_platform().startswith("macosx"):
            from geocommit.provider.corelocation import CoreLocationProvider
            return CoreLocationProvider()
        else:
            from geocommit.networkmanager import NetworkManager
            return NetworkManager()
