# PyAtlas
#### A simplified Atlas API for Python

----
## Getting Started
To get setup in a new project folder, run:

    $ mkdir newproj && cd newproj
    $ virtualenv venv --python=python2.7
    $ source venv/bin/activate

NOTE: `pyatlas` will automatically install the dependencies it needs, including the Protocol Buffers Python runtime - `protobuf-2.6.1`. 
Therefore, it is highly recommended that you develop `pyatlas` based projects in a Python virtual environment - you may need to install `virtualenv` if you have not already. 
(If you want to create a `pyatlas` distribution that does not automatically pull in dependencies, see the next section.)

Now that you have your virtual environment set up, you can install `pyatlas` with:

    (venv) $ pip install pyatlas

If `pip` can not find the `pyatlas` module, you may need to build it from source yourself. Check the next section for more info.

To test that everything went smoothly, create a file `helloatlas.py` with the following code:
```python
import pyatlas
pyatlas.hello_atlas()
```
Now run:

    (venv) $ python helloatlas.py

If you see:

    Hello Atlas!

then you're good to go!

----
## Building the `pyatlas` module
To build the `pyatlas` module from source, run:

    $ cd /path/to/atlas
    $ ./gradlew buildPyatlas

This will generate a wheel file at `pyatlas/dist`. You can now install this with `pip` like

    $ pip install /path/to/atlas/pyatlas/dist/pyatlas-VERSION.whl

Again, it is recommended that you do this in the desired virtual environment.

If you want to build a `pyatlas` wheel file that does not automatically pull dependencies, open up `setup.py` and remove the lines that say
    
    install_requires=[
    .
    .
    .
    ],

Then re-run the `./gradlew buildPyatlas` command from above and reinstall using `pip`. Note that you will now need to manage the required dependencies manually.

### Note on the formatter
`pyatlas` uses the `yapf` formatting library to check for code format issues when building. If you are running into issues after modifying `pyatlas`, try running

    ./gradlew applyFormatPyatlas

Now `pyatlas` should make it past the `CHECK` format step!

Note there is an issue that causes the formatter to goof if a source file does not end with a newline (\n) character.
If the `CHECK` format step is consistently failing after repeated `APPLY` steps, and you are seeing a message like the following:

    atlas.py: found issue, reformatting...

with no formatter diff being displayed, check to make sure that the file has an ending newline. 
