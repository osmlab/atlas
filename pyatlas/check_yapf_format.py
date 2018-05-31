import sys
import os
from yapf.yapflib.yapf_api import FormatFile

def main(argv):
    srcdir = argv[1]
    for file in os.listdir(srcdir):
        if file.endswith('.py'):
            with open(os.path.join(srcdir, file), 'r') as srcfile:
                original = srcfile.read()
            reformatted = FormatFile(os.path.join(srcdir, file))
            if original != reformatted[0]:
                print "formatting error in " + str(file)
                exit(1)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print "ERROR: requires source directory argument"
        exit(1)
    main(sys.argv)
