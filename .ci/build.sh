#!/bin/sh

set -e
set -x

export BUILD_DIR=`pwd`
cp /root/.gradle/gradle.properties gradle/

set +x
echo "Setting internalNexusUsername..."
echo "internalNexusUsername=$INTERNAL_NEXUS_USERNAME" >> gradle/gradle.properties
echo "Setting internalNexusPassword..."
echo "internalNexusPassword=$INTERNAL_NEXUS_PASSWORD" >> gradle/gradle.properties

set -x
export GRADLE_USER_HOME="${BUILD_DIR}/gradle"
cd source


version=`./gradlew -q printVersion`
isSnapshot=`./gradlew -q printVersion|grep "\-SNAPSHOT" || true`

echo "FYI: version is $version"

if [ "$BUILD_TYPE" = "release" ] ; then
  if [ ! -z "$isSnapshot" ] ; then
    echo "not a release, aborting!"
    exit 1
  fi
  itch_channel="sirnuke/outrogue:win-linux-mac-stable"
  mkdir -p github-release
  echo "$version" > github-release/github-version
elif [ "$BUILD_TYPE" = "snapshot" ] ; then
  if [ -z "$isSnapshot" ] ; then
    echo "not a snapshot, aborting!"
    exit 1
  fi
  itch_channel="sirnuke/outrogue:win-linux-mac-snapshot"
else
  echo "Unknown BUILD_TYPE '$BUILD_TYPE', aborting!"
  exit 1
fi

./gradlew clean outrogue-frontend:shadowJar publish

mv outrogue-frontend/build/libs/outrogue-$version.jar .

butler push outrogue-$version.jar $itch_channel

if [ "$BUILD_TYPE" = "release" ] ; then
  mv outrogue-$version.jar github-release
fi

