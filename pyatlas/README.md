# PyAtlas
####A simplified Atlas API for Python

----
##Dependency Information
This project is developed with Python 2.7 and Protocol Buffers 2.6.1.

----
## Getting Started
It is highly recommended that you develop `pyatlas` based projects in a Python virtual environment - you may need to install `virtualenv` if you have not already. To get setup in a new project folder, run:

    $ mkdir newproj && cd newproj
    $ virtualenv venv --python=python2.7
    $ source venv/bin/activate

Now that you have your virtual environment set up, you need to install the Python Protocol Buffers runtime as well as `pyatlas`.

    (venv) $ pip install protobuf==2.6.1
    (venv) $ pip install pyatlas

If `pip` could not find the `pyatlas` module, you may need to build it from source yourself. Check the next section for more info.

To test that everything went smoothly, create a file `helloatlas.py` with the following code:

    import pyatlas
    pyatlas.pyatlas_test()

Now run:

    (venv) $ python helloatlas.py

You should see

    Hello Atlas!

At this point, you're good to go!

----
## Building the `PyAtlas` module
To build the `PyAtlas` module from source, run:

    $ cd /path/to/atlas
    $ ./gradlew buildPyatlas

This will generate a wheel file at `pyatlas/dist`. You can now install this with `pip` like

    $ pip install /path/to/atlas/pyatlas/dist/pyatlas-VERSION.whl

Again, it is recommended that you do this in a desired virtual environment.
