#!/bin/sh
mkdir -p classes/server
mkdir -p classes/api
mkdir -p classes/server/main/java/org/
rsync -raP Sugarcane-Server/src/main/java/org/sugarcanemc/ classes/server/main/java/org/
rsync -raP Sugarcane-API/src/main/java/org/sugarcanemc/ classes/api/main/java/org/

