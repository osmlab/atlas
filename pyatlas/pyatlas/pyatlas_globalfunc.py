"""Global utility functions for pyatlas"""

import zipfile


def hello_atlas():
    print "Hello Atlas!"


def print_zipentries(protofile):
    with zipfile.ZipFile(protofile, 'r') as myzip:
        for name in myzip.namelist():
            print name


def read_zipentry(protofile, entry):
    with zipfile.ZipFile(protofile, 'r') as myzip:
        return myzip.read(entry)
