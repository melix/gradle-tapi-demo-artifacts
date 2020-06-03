## Tooling API demo

A simple demo of the Gradle Tooling API with a custom model.
This model will retrieve the list of all artifacts produced by the target build.

The project consists of 3 subprojects:

- the `model` project is our custom Tooling API model, which will store what we want to know about the build. In particular, here, we want to know about the artifacts.
- the `plugin` project is a plugin which will be injected to builds via the Tooling API, in order to register the custom model builder
- the `inspector` project is the main application which will call our model builder on any build

The Tooling API will use whatever Gradle version is declared in the project, if it uses the Gradle wrapper.

## Testing

You can test this custom model builder in two steps:

1. install the application using `./gradlew install`
2. execute it using `inspector/build/install/inspector/bin/inspector`

You can pass an extra argument to the inspector, the path to _any_ Gradle build, which will dump the artifact list for any build.

