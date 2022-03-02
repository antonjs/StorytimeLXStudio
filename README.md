# Storytime LX
This is the lighting control system for the [Storytime Mutant Vehicle](https://antonjs.notion.site/Build-Updates-366fd7b37c43462e96c3f94eaa21da2f), based on LX and LXStudio by Mark Slee.

Storytime is a Burning Man Mutant Vehicle, created by friends from the Friend Manufacturing Co in the Bay Area. It first drove the playa in 2019, and has since returned for the 2021 Renegade Burn and other off-season playa activities. It will return to Black Rock City in 2022.

More  documentation for the overall lighting system is [in the Storytime Notion](https://www.notion.so/antonjs/Lighting-System-c79426162e374903a5bcb27244416b84).

![Storytime](assets/storytime.jpg)

# Development
## Installation
1. Clone this repository
2. Open in IntelliJ IDEA or Eclipse. 
   1. @antonjs develops in IDEA so that path is more well-trodden
   2. Select or download a Java SDK
      1. File -> Project Structure -> SDKs
      2. Processing is developed with Eclipse Temerin Java 11, Slee suggests that
      3. It appears to work with openjdk-17 on Linux, but _not_ on OS X due to a bug in JOGL (the OpenGL library)
      4. AdoptOpenJDK 11 seems to work on OS X
   3. Fix the Maven errors
      1. File -> Project Structure -> Problems
      2. Hit 'Fix' on the processing libraries with the wrong paths
3. Install the processing poms so that you can package with Maven
   1. Clone https://github.com/heronarts/P4LX.git
   2. Enter the repository and run `mvn validate`

## Building
You can run LXStudio from inside an IDE, but to deploy to the car you need to build a JAR.

1. Run `mvn install` in the project directory
   1. If this fails due to missing a processing dependency, check you installed them from P4LX, above.
2. The JAR will be built in `target/lxstudio-ide-0.4.-jar-with-dependencies.jar`

This JAR does not actually contain all the dependencies, because processing, jogamp and gluegen aren't packaged in this way. They will need to be deployed alongside the JAR, instructions tbd.

## Model
The model has three key components:
1. Lampshade
2. Books
3. Pole

Each is tagged with the appropriate name for use in the channel selectors.

## Channels


## Patterns

# MV Setup
## Install

## Startup