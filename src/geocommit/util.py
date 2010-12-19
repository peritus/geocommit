from subprocess import Popen, STDOUT, PIPE
from shlex import split

def system(cmd, cwd=None):
    process = Popen(split(cmd), stderr=STDOUT, stdout=PIPE, cwd=cwd)
    # from http://stackoverflow.com/questions/1388753/how-to-get-output-from-subprocess-popen
    value = ""
    while True:
        read = process.stdout.read(1)
        value += read
        if read == '' and process.poll() != None:
            return value
