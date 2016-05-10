#!/bin/sh

GEN_DIR="examples/gen/"
JAR_FILE="bloqqi-compiler.jar"

if [ $# != 1 ]; then
	echo "Specify .dia file to execute"
	exit 
fi

if [ ! -f $JAR_FILE ]; then
	ant jar
fi

name=$(basename "$1")
genfile="$GEN_DIR/${name%.*}"

java -jar $JAR_FILE $1 --c --o="$genfile.c"
gcc -std=c99 "$genfile.c" examples/PrintFunction.c -o $genfile
./$genfile
