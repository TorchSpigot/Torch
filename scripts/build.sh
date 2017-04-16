#!/usr/bin/env bash

(
set -e
basedir="$pwd"

(git submodule update --init && ./scripts/init.sh "$basedir" "$2") || (
	echo "Failed to build Torch"
	exit 1
) || exit 1

)
