#!/usr/bin/env python

import sys
import os
import tempfile
import shutil
import stat
from subprocess import Popen, PIPE

from geocommit.locationprovider import LocationProvider

from geocommit.util import system, forward_system

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

    def git_forward(self, name, argv, read_stdin=False):
        forward_system(["git", name] + argv, read_stdin)

    def install_hooks(self, directory):
        git_dir = system("git rev-parse --show-toplevel", directory).strip('\n\r ') + "/"
        git_dir += system("git rev-parse --git-dir", directory).strip('\n\r ')

        hooks = {
            "post-commit": ["#!/bin/sh\n", "git geo note\n"]
        }

        for hook, code in hooks.iteritems():
            self.install_hook(git_dir, hook, code)

    def install_hook(self, git_dir, hook_name, code):
        hook = git_dir + "/hooks/" + hook_name

        if os.path.exists(hook):
            # is geo commit hook?
            f = open(hook, "r")
            lines = f.readlines()
            if lines[1] != code[1]:
                print "Moving existing hook to " + hook + "-partial"
                print "Installing geocommit hook in " + hook
                shutil.move(hook, hook + "-partial")
                code += [hook + "-partial\n"]
            else:
                print "Replacing existing geocommit hook in " + hook
                if os.path.exists(hook + "-partial"):
                    code += [hook + "-partial\n"]
        else:
            print "Installing geocommit hook in " + hook

        f = open(hook, "w")
        f.writelines(code)
        f.close()
        mode = os.stat(hook).st_mode
        os.chmod(hook, mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH)

    def cmd_note(self, argv):
        note = self.get_note()

        if note is None:
            print >> sys.stderr, "Geocommit: No location available."
            print >> sys.stderr, "           Retry annotating your commit with"
            print >> sys.stderr, "           git geo note " + self.git_rev
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


    def cmd_setup(self, argv):
        where = ""
        if len(argv) == 0:
            print "geocommit setup"
            self.install_hooks(".")

        elif len(argv) == 1 and argv[0] == "--global":
            print "geocommit global setup"
            #system("git config --global --add init.templatedir \"/usr/share/geocommit/gittemplatedir/\"")

        else:
            usage("setup")

    def cmd_fetch(self, argv):
        print "Fetching geocommit notes"

    def cmd_push(self, argv):
        print "Pushing geocommit notes"

def usage(cmd):
    print >> sys.stderr, "Usage: git geo " + cmd
    sys.exit(1)

def git_geo():
    '''
    Attaches a the current location to the given object
    '''
    if len(sys.argv) < 2:
        usage("")

    geogit = GeoGit()

    f = getattr(geogit, "cmd_" + sys.argv[1])
    if f and callable(f):
        f(sys.argv[2:])
        sys.exit(0)

    usage(None)
    sys.exit(1)

