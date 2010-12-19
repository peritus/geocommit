import sys
from subprocess import Popen, STDOUT, PIPE
from shlex import split

def system(cmd, cwd=None):
    ret, value = system_exit_code(cmd, cwd)
    return value

def system_exit_code(cmd, cwd=None):
    if isinstance(cmd, basestring):
        cmd = split(cmd)

    process = Popen(cmd, stderr=STDOUT, stdout=PIPE, cwd=cwd)
    # from http://stackoverflow.com/questions/1388753/how-to-get-output-from-subprocess-popen
    value = ""
    while True:
        read = process.stdout.read(1)
        value += read
        if read == '':
            result = process.poll()
            if result != None:
                return result, value

def forward_system(cmd, read_stdin=False):
    if isinstance(cmd, basestring):
        cmd = split(cmd)

    process = Popen(cmd, stdin=PIPE, stdout=PIPE, stderr=PIPE)

    in_data = None

    while True:
        if read_stdin:
            in_data = sys.stdin.read(4096)
            if not in_data:
                read_stdin = False
                in_data = None

        output = process.communicate(in_data)
        if output[0]:
            sys.stdout.write(output[0])
            sys.stdout.flush()
        if output[1]:
            sys.stderr.write(output[1])
            sys.stderr.flush()

        ret = process.poll()

        if ret != None:
            if ret != 0:
                sys.exit(ret)
            break
