import sys
import os
from yapf.yapflib.yapf_api import FormatFile
from yapf.yapflib.yapf_api import FormatCode

def main(argv):
    srcdir = argv[1]
    mode = argv[2]
    violation_detected = False
    for file_to_style in os.listdir(srcdir):

        if file_to_style.endswith('.py'):
            filepath = os.path.join(srcdir, file_to_style)

            if mode == "CHECK":
                original = read_file_contents(filepath)
                reformatted = FormatFile(filepath, style_config='pep8')
                if original != reformatted[0]:
                    print str(argv[0]) + ": ERROR: formatting violation detected in " + str(file_to_style)
                    print(FormatCode(original, filename=filepath, print_diff=True, style_config='pep8')[0])
                    violation_detected = True

            elif mode == "APPLY":
                print "re-formatting " + str(file_to_style)
                FormatFile(filepath, in_place=True, style_config='pep8')

            else:
                print "ERROR: invalid mode " + str(mode)
                exit(1)

    if violation_detected:
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
