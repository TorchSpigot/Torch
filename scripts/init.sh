#!/usr/bin/env bash

(
set -e
basedir="$(cd "$1" && pwd -P)"
workdir="$basedir/work"
paperbasedir="$basedir/work/Paper"
paperworkdir="$basedir/work/Paper/work"

if [ "$2" == "--init" ]; then
	echo "Initial Paper..."
	(
		cd "$paperbasedir"
		scripts/build.sh "$basedir" "--jar"
	)
fi

echo "Building Torch..."
(
	echo "Replacing sources..."
	cp -r "$basedir/sources/src" "$paperbasedir/Paper-Server"
	cp -f "$basedir/sources/pom.xml" "$paperbasedir/Paper-Server/pom.xml"
	
	cd "$paperbasedir"
	mvn clean install
	
	minecraftversion=$(cat "$paperworkdir/BuildData/info.json"  | grep minecraftVersion | cut -d '"' -f 4)
	rawjar="$paperbasedir/Paper-Server/target/paper-$minecraftversion.jar"
	cp "$rawjar" "$basedir/Torchpowered-$minecraftversion.jar"
	
	echo ""
	echo "Torch Build success!"
	echo "Copied final jar to $basedir/Torchpowered-$minecraftversion.jar"
)

)
