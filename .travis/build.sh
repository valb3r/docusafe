#!/usr/bin/env bash
set -e

# mvn --settings .travis/settings.xml clean verify -B -V

mvn --settings .travis/settings.xml clean install -Dmaven.javadoc.skip=true -B -V
mvn clean install -DSC-AMAZONS3=http://minio-psp-docusafe-performancetest.cloud.adorsys.de,simpleAccessKey,simpleSecretKey,us-east-1,travis.docusafe.${TRAVIS_BRANCH}
