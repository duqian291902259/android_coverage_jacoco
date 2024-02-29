#!/bin/bash
#note by dq

#./gradlew coverage-plugin:publish
#./gradlew coverage-plugin:publishToMavenLocal

./gradlew coverage-no-op:publish
./gradlew coverage-no-op:publishToMavenLocal

./gradlew coverage-library:publish
./gradlew coverage-library:publishToMavenLocal
