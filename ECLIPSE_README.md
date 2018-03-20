# Working with Atlas in Eclipse

## First time setup
1. Clone the Atlas repository from your fork into the desired local location.

2. Import the project into Eclipse using "File" -> "Import" -> "Gradle" -> "Existing Gradle Project"

3. From the command line, run
```
$ cd /path/to/local/Atlas/repo
$ ./gradlew clean build -x test -x integrationTest
```

4. Back in Eclipse, right click the Atlas project in Package Explorer and click "Refresh"
  1. NOTE: You should now see a "gen" folder appear in the package explorer

5. Again, right click on the Atlas project in Package Explorer and select "Build Path" -> "Configure Build Path"

6. Select "Add Folder" then expand "gen" -> "main" and select the box next to "java"

7. Click "Apply and close"

8. The red lines and X's should all be resolved now. You're good to go!

## How to rebuild the protobuf templates
1. You're here because you wanted to change the .proto template files, and now you need to regenerate and rebuild the code in "gen". Easy!

2. Make the necessary adjustment to the .proto files first (in src/main/proto), but do not touch any of the dependant source code. Otherwise Eclipse will start complaining.

3. Once you are done adjusting the .proto files, open a command line and run
```
$ cd /path/to/local/Atlas/repo
$ ./gradlew clean build -x test -x integrationTest
```

4. Back in Eclipse, right click on the Atlas project in Package Explorer and click "Refresh"

5. Now you can make changes to your actual source, which depends on the code generated from the .proto files. The Eclipse play button should work once again.

