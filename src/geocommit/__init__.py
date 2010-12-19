#!/usr/bin/env python

import sys
import os
import tempfile
from geocommit.locationprovider import LocationProvider

from geocommit.util import system

class GeoGit(object):

    def __init__(self):
        '''
        >>> 1*2
        2
        '''
        self.git_bin = system("which git").strip()
        self.git_dir = system("git rev-parse --show-toplevel").strip('\n\r ')
        self.git_rev = system("git rev-parse HEAD").strip('\n\r ')

    def get_note(self):
        provider = LocationProvider.new()
        location = provider.get_location()
        if location is None:
            return None

        return location.format_long_geocommit()

    def attach_note(self):
        note = self.get_note()

        if note is None:
            print >> sys.stderr, "GeoCommit: No location available"
            return

        note_file = tempfile.NamedTemporaryFile()
        note_file.write(note)
        note_file.flush()

        command = self.git_bin + " "

        command += "notes --ref=geocommit add -F " + note_file.name + " "
        command += self.git_rev


        system(command, self.git_dir)

        note_file.close()

def git_geonote():
    '''
    Attaches a the current location to the given object
    '''
    geocommit = GeoGit()
    geocommit.attach_note()

def git_geocommit():
    '''
    git commit && git geonote
    '''
    raise NotImplementedError()

def git_geosync():
    '''
    Syncs geonotes between your local and a remote repository
    '''
    raise NotImplementedError()

