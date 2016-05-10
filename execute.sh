#!/bin/sh

GEN_DIR="examples/gen/"

if [ $# != 1 ]; then
	echo "Specify .dia file to execute"
	exit 
fi

name=$(basename "$1")
genfile="$GEN_DIR/${name%.*}"

java -jar bloqqi-compiler.jar $1 --c --o="$genfile.c"
gcc -std=c99 "$genfile.c" examples/PrintFunction.c -o $genfile
./$genfile
