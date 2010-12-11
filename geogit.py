#!/usr/bin/env python

import sys
from subprocess import Popen, STDOUT, PIPE
from shlex import split
import tempfile

def system(cmd):
    process = Popen(split(cmd), stderr=STDOUT, stdout=PIPE)
    # from http://stackoverflow.com/questions/1388753/how-to-get-output-from-subprocess-popen
    value = ""
    while True:
        read = process.stdout.read(1)
        value += read
        if read == '' and process.poll() != None:
            return value

class GeoGit(object):

    def __init__(self):
        pass

    def attach_note(self):
        note_file = tempfile.NamedTemporaryFile()
        note_file.write(self.format_location())
        note_file.flush()

        system("git notes --ref=geogit add -f " + note_file.name + "")

        note_file.close()

    def format_location(self):
        return "Berlin (0.0, 0.0)"

def main(argv=None):
    if argv is None:
        argv = sys.argv

    geogit = GeoGit()
    geogit.attach_note()

if __name__ == "__main__":
    sys.exit(main())
