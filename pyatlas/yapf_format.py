import sys
import os
from yapf.yapflib.yapf_api import FormatFile
from yapf.yapflib.yapf_api import FormatCode

def main(argv):
    srcdir = argv[1]
    mode = argv[2]
    for file in os.listdir(srcdir):
        if file.endswith('.py'):
            filepath = os.path.join(srcdir, file)
            if mode == "CHECK":
                original = read_file_contents(filepath)
                reformatted = FormatFile(filepath)
                if original != reformatted[0]:
                    print str(argv[0]) + ": ERROR: formatting violation detected in " + str(file)
                    print(FormatCode(original, filename=filepath, print_diff=True)[0])
                    exit(1)
            elif mode == "APPLY":
                print "re-formatting " + str(file)
                FormatFile(filepath, in_place=True)
            else:
                print "ERROR: invalid mode " + str(mode)
                exit(1)

def read_file_contents(filepath):
    with open(filepath, 'r') as srcfile:
        filecontents = srcfile.read()
    return filecontents

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print "usage: " + str(sys.argv[0]) + " <srcpath> <mode>"
        exit(1)
    main(sys.argv)
