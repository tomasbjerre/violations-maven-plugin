#!/bin/bash
mvn clean generate-resources eclipse:eclipse install || exit 1
cd violations-maven-plugin-example
mvn versions:update-properties -DallowSnapshots=true
mvn verify
