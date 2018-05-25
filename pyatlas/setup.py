import setuptools

with open("README.md", "r") as fh:
    long_description = fh.read()

setuptools.setup(
    name="pyatlas",
    version="0.0.1",
    author="lucaspcram",
    author_email="lucaspcram@gmail.com",
    description="A simplified Python API for Atlas",
    long_description=long_description,
    long_description_content_type="text/markdown",
    url="https://github.com/osmlab/atlas",
    packages=setuptools.find_packages(),
    classifiers=(
        "Programming Language :: Python :: 2",
        "Operating System :: OS Independent",
    ),
)

