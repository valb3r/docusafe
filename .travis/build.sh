#!/usr/bin/env bash
set -e

mvn --settings .travis/settings.xml clean verify -DUGLY_KEYSTORE_CACHE -B -V
