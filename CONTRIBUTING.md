# Contributing to Atlas

Thanks for taking the time to contribute!

## Where to ask a question

This project uses [StackOverflow](https://stackoverflow.com/) to handle all software related questions. If unsure about an issue, please first ask a question on StackOverflow, and then file an issue if necessary. Do not ask questions in GitHub issues as those will be closed and redirected to StackOverflow.

## Suggestions and bug reports

### Reporting bugs

If you have found a bug to report, that is great! Please search for similar bugs in GitHub issues, as your bug might have been filed already. If not, filing a GitHub issue is the next thing to do!

### Filing a Github issue

When submitting an issue, here is the information to include:

* Title: Use a title that is as self explanatory as possible
* Summary: Detailed description of the issue, including screenshots and/or stack traces
* Steps to reproduce: Do not forget to include links to data samples that can help in reproducing the issue
* Actual vs. Expected: Describe the results and how they differ from the expected behavior
* Workaround: If you have found a temporary workaround the issue, please also include it there!

### Suggesting enhancements

Enhancements are also handled with GitHub issues. Make sure to include the following:

* Title: Use a title that is as self explanatory as possible
* Summary: Use-case description of the proposed enhancement
* Desired: Describe the desired behavior of the proposed enhancement
* Implementation proposal: If you have an idea of how to implement the enhancement

## Submitting code

### Requirements

* Oracle JDK 8
* Gradle

### First contribution

The first step would be to fork the project in your own github account, then clone the project locally using `git clone`. Then use gradle to build the code, and run tests:

```
cd <my clone location>
./gradlew build
```

Atlas is serialized using protobuf. The proto-generated sources are not checked-in to the repository, so any IDE setup requires to run `./gradlew build` to generate the proto-generated sources in the project. At that point, the IDE of your choice will be able to resolve all the necessary classes.

To start contributing to your fork, the best way is to make sure that your IDE is setup to follow the [code formatting template](config/format/code_format.xml).

Once you have fixed an issue or added a new feature, it is time to submit [a pull request](#pull-request-guidelines)!

### Building

You can build a shaded JAR that will allow you to execute atlas. Make sure you first have a `log4j.properties` file in `src/main/resources`.

https://github.com/osmlab/atlas-checks/blob/dev/config/log4j/log4j.properties

Then, you can build it with:

``` 
./gradlew shaded
```

From there, you can run command line tools in atlas, like the following:

``` 
java -cp atlas-5.1.8-SNAPSHOT-shaded.jar org.openstreetmap.atlas.geography.atlas.delta.AtlasDeltaGenerator <args...>
```

Also note that you will need a `log4j.properties` to run properly in IntelliJ IDEA.


### Code formatting

The project's code is checked by Checkstyle as part of the `gradle check` step.

There also is an eclipse code formatting template [here](config/format/code_format.xml) that is used by [Spotless](https://github.com/diffplug/spotless) to check that the formatting rules are being followed. In case those are not, the `gradle spotlessCheck` step will fail and the build will not pass. Spotless provides an easy fix though, with `gradle spotlessApply` which will refactor your code so it follows the formatting guidelines.

### Testing

The codebase contains an extensive range of unit tests, and integration tests. Unit tests are supposed to run fairly fast. If the test takes a long time to run, we put it in the integrationTest repository, to allow users to run them only when wanted. All the tests will be run for every pull request build, though! When contributing new code, make sure to not break existing tests (or modify them and explain why the modification is needed) and to add new tests for new features.

### Pull Request Guidelines

Pull requests comments should follow the template below:

* An as extensive as reasonable description of the change proposed, in easy to read [Markdown](https://guides.github.com/features/mastering-markdown/), with as many code snippet examples, screen captures links and diagrams as possible
* A Benefit/Drawback analysis: what does this improve, and at what cost? Is the performance impacted or improved?
* Label: If applicable, apply one of the available labels to the pull request
