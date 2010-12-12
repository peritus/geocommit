#!/usr/bin/env python

import sys
import os
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

class GeoGitFifo(object):
    def __init__(self, path):
        self.path = path
        self.reader = None
        self.writer = None

        try:
            os.mkfifo(path)
        except OSError, e:
            # already created
            pass

    def read(self):
        if self.reader is None:
            self.reader = open(self.path, "r")

        values = self.reader.readline().split(" ", 1)

        return values

    def write(self, rev, path):
        if self.writer is None:
            self.writer = open(self.path, "w")

        print >> self.writer, rev + " " + path

    def __del__(self):
        if not self.reader is None:
            self.reader.close()
        if not self.writer is None:
            self.writer.close()

class GeoGit(object):

    def __init__(self):
        pass

    def writeToFifo(self):
        rev = system("git rev-parse HEAD").strip()
        path = system("git rev-parse --show-toplevel").strip()

        fifo = GeoGitFifo(os.environ["HOME"] + "/.geogit.sock")
        fifo.write(rev, path)

    def attach_note(self):
        note_file = tempfile.NamedTemporaryFile()
        note_file.write(self.format_location())
        note_file.flush()

        system("git notes --ref=geogit add -F " + note_file.name + " HEAD")

        note_file.close()

    def format_location(self):
        return "Berlin (0.0, 0.0)"

def main(argv=None):
    if argv is None:
        argv = sys.argv

    geogit = GeoGit()
    geogit.writeToFifo()
    #geogit.attach_note()

if __name__ == "__main__":
    sys.exit(main())
