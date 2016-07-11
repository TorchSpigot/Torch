#!/bin/bash

./rebuildPatches.sh && ./applyPatches.sh && mvn clean install
