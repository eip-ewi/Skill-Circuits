# Contributing to Skill Circuits

## Contributing to Skill Circuits
This guide has the following sections:
 1. Getting Access to the Repository
 1. Running Skill Circuits
 1. Skill Circuits Architecture
 1. Development Guidelines
 1. Submitting your changes

## Getting Access to the Repository
Skill Circuits has two repositories: one [hosted on the TU Delft EWI GitLab instance](https://gitlab.ewi.tudelft.nl/skill-circuits/skill-circuits) and one [hosted on GitHub](https://github.com/eip-ewi/Skill-Circuits). The easiest way to contribute is to make a fork from the GitHub repository. It is also possible to get a fork on the TU Delft EWI GitLab if you have a TU Delft account, but this is a longer process.

## Running Skill Circuits
Skill Circuits requires Java 17 to run, so make sure you have Java 17 or higher installed. Skill Circuits also requires a running instance of [LabraCore](https://gitlab.ewi.tudelft.nl/eip/labrador/labracore), the setup of which is explained in the following section.

Before you can run Skill Circuits you will need to copy the `application.template.yaml` file and name it `application.yaml`, i.e. `src/main/resources` should look as follows:
```
src/
  main/
    resources/
      ...
       application.template.yaml
       application.yaml (copy of application.template.yaml)
       ...
```

### Setting up LabraCore
To set up LabraCore, clone the development branch from the [repository](https://gitlab.ewi.tudelft.nl/eip/labrador/labracore). Afterwards, copy the `application.template.yaml` just like for Skill Circuits. Then, you can either run the project from IntelliJ or run the Jar. If you are not planning to contribute to LabraCore, it might be easiest to compile the project and run the Jar. This can be done with the folloing commands:
```
LABRACORE=... # Insert location here

cd $LABRACORE
./gradlew clean
./gradlew assemble

java -jar $LABRACORE/build/libs/labracore-1.1.0.jar
```

After LabraCore is running, you can run Skill Circuits.

## Development Guidelines

TOOD

## Submitting Your Changes
To submit your changes, either make a merger request or a pull request depending on what repository you are using. Please make sure to follow the template. In order for a merge request / pull request to be accepted at least the following needs to be done:
 - A clear description is written on what has been changed
 - You have run your branch and ensured all functionality is still working as intended
 - You have added tests for new or changed functionality
 - If you changed entities, you have added migrations
 - You have run `./gradlew spotlessApply`
 - You have added the necessary changelog entries

