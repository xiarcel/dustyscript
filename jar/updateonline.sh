#!/bin/sh
./refreshjar
echo "PW required--updating online testapplet (jar)"
if [ "$1" != "test" ]; then
scp dustyscript.jar  xiarcel@dustyscript.sourceforge.net:/home/groups/d/du/dustyscript/htdocs/ 
fi

DIR_LIST=""
for file in `ls ../bin/libraries | grep dusty_`
	{
	file="../bin/libraries/$file"
	if [ -d $file ]; then

		if [ "$DIR_LIST" = "" ]; then
			DIR_LIST="$file"
		else
			DIR_LIST="$DIR_LIST $file"
		fi
	fi
	}

echo -e "Using dirs: $DIR_LIST" 
if [ "$1" != "test" ]; then
scp -r ../bin/libraries/*.txt $DIR_LIST xiarcel@dustyscript.sourceforge.net:/home/groups/d/du/dustyscript/htdocs/libraries/
fi 
