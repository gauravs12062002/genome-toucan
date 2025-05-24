#!/bin/sh

# Change the directory to the location of this script.
cd "${0%/*}";

# Check if java is installed.
if [ $(type java 2>&1 >/dev/null; echo $?) -ne 0 ] ; then
    echo 'To run Toucan, Java needs to be installed.' > /dev/stderr;
    echo 'Java can be downloaded from: http://java.com/download'  > /dev/stderr;
    exit 1;
fi

# Start Toucan.
java -jar jars/toucan-client.jar

exit $?
