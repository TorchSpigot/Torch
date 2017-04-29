#!/usr/bin/env bash

(
set -e
basedir="$pwd"

(git submodule update --init --remote && ./scripts/init.sh "$basedir" "$1" "$2") || (
	echo "Failed to build Torch"
	exit 1
) || exit 1

)
