#!/bin/bash

DESTINATION_FOLDER="java-jars"
echo ""
echo "~~~ Starting Building Jars & storing jars in folder: $DESTINATION_FOLDER"
echo ""
. ./package_store_jars.sh

echo ""
echo "~~~ Starting Building Podman Images & running: $DESTINATION_FOLDER"
echo ""
. ./build_run_podman.sh
