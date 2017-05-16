#!/bin/sh

find_java()
{
POT=`find / 2>/dev/null | grep java`
for pot in $POT
	{
 	echo $pot
	return
	}
echo "<NONE>"
}
	
	


javac -d ../bin dscript/*.java dscript/connect/*.java dscript/dde/*.java widget/*java widget/swing/*java dscript/preprocess/*.java dscript/intl/*.java network/*.java


 
