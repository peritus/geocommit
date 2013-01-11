#!/usr/bin/env python

import sys
import os
import tempfile
import shutil
import stat
from subprocess import Popen, PIPE

from geocommit.locationprovider import LocationProvider

from geocommit.util import system, system_exit_code, forward_system

version = "0.9.2"

class GeoGit(object):

    def __init__(self):
        '''
        >>> 1*2
        2
        '''
        self.git_bin = system("which git").strip()
        self.git_dir = system("git rev-parse --show-toplevel").strip('\n\r ')
        self.git_rev = system("git rev-parse HEAD").strip('\n\r ')

    def getargs(self, opt_definition, argv):
        in_opt = False
        in_arg = False
        arguments = []

        for arg in argv:
            if not in_opt:
                if (opt_definition.has_key(arg)):
                    if opt_definition[arg] == "string":
                        in_opt = True
                else:
                    in_arg = True
                    arguments.append(arg)
            elif in_arg:
                arguments.append(arg)
            else:
                in_opt = False

        return arguments

    def get_note(self):
        provider = LocationProvider.new()
        location = provider.get_location()
        if location is None:
            return None

        return location.format_long_geocommit()

    def git_forward(self, name, argv, read_stdin=False):
        forward_system(["git", name] + argv, read_stdin)

    def install_hooks(self, directory):
        git_dir = os.path.join(
                system("git rev-parse --show-toplevel", directory).strip('\n\r '),
                system("git rev-parse --git-dir", directory).strip('\n\r ')
        )

        hooks = {
            "post-commit": ["#!/bin/sh\n", "git geo note\n"],
            "post-merge": ["#!/bin/sh\n", "git geo note\n"],
            "post-rewrite": ["#!/bin/sh\n", "git geo postrewrite $@\n"]
        }

        for hook, code in hooks.iteritems():
            self.install_hook(git_dir, hook, code)

    def install_hook(self, git_dir, hook_name, code):
        hook = git_dir + "/hooks/" + hook_name

        if os.path.exists(hook):
            # is geo commit hook?
            f = open(hook, "r")
            lines = f.readlines()
            if len(lines) > 3 or (len(lines) > 1 and lines[1] != code[1]):
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

    def fetch_notes(self, remote):
        print "Fetching geocommit notes"
        code, output = system_exit_code("git fetch " + remote + " refs/notes/geocommit")
        if code != 0:
            print >> sys.stderr, output
            if output.find("fatal: Couldn't find remote ref refs/notes/geocommit") == -1:
                sys.exit(code)

    def fetch_and_merge_notes(self, remote):
        self.fetch_notes(remote)

        #local_changes = system("git rev-list --max-count=1 FETCH_HEAD..refs/notes/geocommit").strip('\n\r ')
        remote_changes = system("git rev-list --max-count=1 refs/notes/geocommit..FETCH_HEAD").strip('\n\r ')

        if remote_changes:
            print "Merging geocommit notes"
            system_exit_code("git update-ref refs/notes/geocommit-remote FETCH_HEAD")
            system_exit_code("git notes --ref geocommit merge --strategy=theirs geocommit-remote")
        else:
            print "Already up-to-date."

    def add_note(self, rev, note):
        note_file = tempfile.NamedTemporaryFile()
        note_file.write(note)
        note_file.flush()

        command = "%(git_bin)s notes --ref=geocommit add -F %(note_filename)s %(git_rev)s" % {
          'git_bin': self.git_bin,
          'git_rev': rev,
          'note_filename': note_file.name,
        }

        system(command, self.git_dir)

        note_file.close()

    def get_remote(self):
        branch = system("git symbolic-ref -q HEAD").strip('\n\r ')
        remote = ""
        if branch:
            branch = branch.replace("refs/heads/", "", 1)
            remote = system("git config --get branch." + branch + ".remote").strip('\n\r ')
            if remote:
                return remote
        return None

    def cmd_note(self, argv):
        print "Retrieving location ...",
        sys.stdout.flush()
        note = self.get_note()
        print "found."

        git_rev = self.git_rev

        if len(argv) > 0:
            git_rev = argv[0]

        if note is None:
            print >> sys.stderr, "Geocommit: No location available."
            print >> sys.stderr, "           Retry annotating your commit with"
            print >> sys.stderr, "           git geo note " + git_rev
            return

        self.add_note(git_rev, note)

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
        if len(argv) == 0:
            remote = self.get_remote()
            if remote:
                argv = [remote]
            else:
                print >> sys.stderr, "Cannot fetch geo data: No remote specified"
                usage("fetch")
        elif len(argv) >= 2:
            usage("fetch")

        self.fetch_notes(argv[0])

    def cmd_pull(self, argv):
        if len(argv) == 0:
            remote = self.get_remote()
            if remote:
                argv = [remote]
            else:
                print >> sys.stderr, "Cannot pull geo data: No remote specified"
                usage("pull")
        elif len(argv) >= 2:
            usage("pull")

        self.fetch_and_merge_notes(argv[0])

    def cmd_sync(self, argv, push=True):
        if len(argv) == 0:
            remote = self.get_remote()
            if remote:
                argv = [remote]
            else:
                print >> sys.stderr, "Cannot sync geo data: No remote specified"
                usage("sync")
        elif len(argv) >= 2:
            usage("sync")

        self.fetch_and_merge_notes(argv[0])

        if push:
            print "Syncing geocommit notes"
            forward_system("git push " + argv[0] + " refs/notes/geocommit")

    def cmd_push(self, argv):
        opt_definition = {
            "-v" : "bool",
            "--verbose" : "bool",
            "--repo": "string",
            "--all": "bool",
            "--mirror": "bool",
            "--delete": "bool",
            "--tags": "bool",
            "-n": "bool",
            "--dry-run": "bool",
            "--porcelain": "bool",
            "-f": "bool",
            "--force": "bool",
            "--thin": "bool",
            "--no-thin": "bool",
            "--receive-pack": "string",
            "--exec": "string",
            "-u": "bool",
            "--set-upstream": "bool",
            "--progress": "bool",
        }

        arguments = self.getargs(opt_definition, argv)

        if len(arguments) == 0:
            remote = self.get_remote()
            if remote:
                arguments = [remote]
                argv += [remote]
            else:
                print >> sys.stderr, "Cannot push geo data: No remote specified"
                usage("push")
        if len(arguments) == 1:
            arguments += [":"]
            argv += [":"]

        argv += ["refs/notes/geocommit"]

        self.cmd_sync([arguments[0]], False)

        forward_system(["git", "push"] + argv)

    def cmd_version(self, argv):
        print "version: " + version

    def cmd_postrewrite(self, argv):
        if len(argv) < 1:
            usage("postrewrite")

        cause = argv[0] # rebase or amend

        for line in sys.stdin:
            parts = line.split(" ", 2)
            old_sha1 = parts[0]
            new_sha1 = parts[1]

            if len(parts) > 2:
                extrainfo = parts[2]

            old_note = system("git notes --ref geocommit show " + old_sha1).strip("\n\r ")
            if old_note:
                self.add_note(new_sha1, old_note + "\n\n") # silently fails if already exists


def usage(cmd = None):
    print >> sys.stderr, "Usage: git geo <command>"
    print >> sys.stderr, ""
    sys.exit(1)

def git_geo():
    '''
    Attaches a the current location to the given object
    '''
    if len(sys.argv) < 2:
        usage("")

    geogit = GeoGit()

    cmd = "cmd_" + sys.argv[1]
    if hasattr(geogit, cmd):
        f = getattr(geogit, cmd)
        if callable(f):
            f(sys.argv[2:])
            sys.exit(0)

    usage()
    sys.exit(1)
