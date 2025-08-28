#!/usr/bin/env bash

# Check if there were any arguments
if [ -z "$1" ]
then
  # If not, run Gradle directly
  ./gradlew run
else
  # If so, pass them to Gradle
  ./gradlew run --args="$*"
fi