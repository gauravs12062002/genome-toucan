#!/bin/sh

# Before running this script, run launch4j to create the Windows toucan.exe file.
# The launch4j configuration file can be found at toucan-client-launch4j.xml.


# Change directory to the directory that contains this script.
cd "${0%/*}";

# Remove old toucan.zip and toucan.tar.gz archive
rm toucan.zip toucan.tar.gz

# Create new Toucan zip archive.
zip toucan.zip toucan/toucan.exe toucan/toucan.sh toucan/jars/*.jar

# Create new Toucan tar.gz archvie.
tar czvf toucan.tar.gz toucan/toucan.exe toucan/toucan.sh toucan/jars/*.jar
