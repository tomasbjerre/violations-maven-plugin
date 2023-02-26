#!/bin/bash

gpg -o /tmp/dummy --sign .gitignore \
 && ./mvnw se.bjurr.gitchangelog:git-changelog-maven-plugin:semantic-version \
 release:prepare release:perform -B \
 se.bjurr.gitchangelog:git-changelog-maven-plugin:git-changelog \
  && git commit -a -m "chore: updating changelog" \
  && git push \
  || git clean -f