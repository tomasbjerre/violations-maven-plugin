#!/bin/bash
./mvnw versions:update-properties \
 && ./mvnw install \
 && cd violations-maven-plugin-example \
 && ./mvnw versions:update-properties -DallowSnapshots=true \
 && ./mvnw verify

