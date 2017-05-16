#!/bin/sh
if [ ! -d ./dscript ]; then
	cd /usr/cvs/dustyscript/dustyscript1.0/bin
fi

java dscript.ConsoleDusty $*
