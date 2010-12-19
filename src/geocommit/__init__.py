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

        command = "%(git_bin)s notes --ref=geocommit add -F %(note_filename)s %(git_rev)s" % {
          'git_bin': self.git_bin,
          'git_rev': self.git_rev,
          'note_filename': note_file.name,
        }

        system(command, self.git_dir)

        note_file.close()

def git_geo():
    '''
    Attaches a the current location to the given object
    '''
    if len(sys.argv) < 2:
        print >> sys.stderr, "Usage: git geo note"
        sys.exit(1)

    if sys.argv[1] == 'note':
        geocommit = GeoGit()
        geocommit.attach_note()
        exit(0)

    elif sys.argv[1] == 'commit':
        raise NotImplementedError()

    elif sys.argv[1] == 'push':
        raise NotImplementedError()

    elif sys.argv[1] == 'pull':
        raise NotImplementedError()

    elif sys.argv[1] == 'init':
        raise NotImplementedError()

    print >> sys.stderr, "Usage: git geo note"
    sys.exit(1)

