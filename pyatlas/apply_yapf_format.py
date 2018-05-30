import sys
import os
from yapf.yapflib.yapf_api import FormatFile

def main(argv):
    srcdir = argv[1]
    for file in os.listdir(srcdir):
        if file.endswith('.py'):
            print "re-formatting " + str(file)
            FormatFile(os.path.join(srcdir, file), in_place=True)

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print "ERROR: requires source directory argument"
        exit(1)
    main(sys.argv)
