import sys
import os
from yapf.yapflib.yapf_api import FormatFile
from yapf.yapflib.yapf_api import FormatCode

# NOTE: If a source file does not end with a newline, the formatter will
# complain, but fail to actually fix the issue. To resolve, manually
# insert a newline at the end of the file.

def main(argv):
    srcdir = argv[1]
    mode = argv[2]
    violation_detected = False
    for file_to_style in os.listdir(srcdir):

        if file_to_style.endswith('.py'):
            filepath = os.path.join(srcdir, file_to_style)

            if mode == "CHECK":
                if detect_formatting_violation(filepath):
                    print str(argv[0]) + ": ERROR: formatting violation detected in " + str(file_to_style)
                    violation_detected = True

            elif mode == "APPLY":
                if detect_formatting_violation(filepath):
                    print str(file_to_style) + ": found issue, reformatting..."
                    FormatFile(filepath, in_place=True, style_config='style.yapf')
                    violation_detected = True

            else:
                print "ERROR: invalid mode " + str(mode)
                exit(1)

    if mode == 'CHECK' and violation_detected:
        exit(1)
    elif not violation_detected:
        print str(argv[0]) + " INFO: all formatting for targets in " + str(argv[1]) + " OK!"

def detect_formatting_violation(filepath):
    original = read_file_contents(filepath)
    reformatted = FormatFile(filepath, style_config='style.yapf')
    if original != reformatted[0]:
        print(FormatCode(original, filename=filepath, print_diff=True, style_config='style.yapf')[0])
        return True
    return False


def read_file_contents(filepath):
    with open(filepath, 'r') as srcfile:
        filecontents = srcfile.read()
    return filecontents

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print "usage: " + str(sys.argv[0]) + " <srcpath> <mode>"
        exit(1)
    main(sys.argv)
