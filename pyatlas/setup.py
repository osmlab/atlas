import setuptools

with open("README.md", "r") as fh:
    long_description = fh.read()

# The version field is left blank, and is populated automatically by
# the 'packagePyatlas' gradle target at build time. The target then resets
# the field to blank before completing.
setuptools.setup(
    name="pyatlas",
    version=
    author="lucaspcram",
    author_email="lucaspcram@gmail.com",
    license="BSD License",
    description="A simplified Python API for Atlas",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/osmlab/atlas",
    packages=setuptools.find_packages(exclude=("unit_tests",)),
    install_requires=[
        'protobuf==2.6.1',
        'shapely==1.6.4'
    ],
    classifiers=(
        "Programming Language :: Python :: 2.7",
        "Operating System :: OS Independent",
        "License :: OSI Approved :: BSD License"
    ),
)
