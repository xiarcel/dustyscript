package dscript.preprocess;
import dscript.*;
import java.io.*;
import java.util.Vector;
import java.util.HashMap;

class PreUse {

public static String BASE = Use.base;
public static String FS = Use.fs;
public static String USER = Use.usrspath;
public static String LIB= Use.libspath;
public static HashMap used=new HashMap();

public static String crunchUses(String source)
{
StringBuffer sb =new StringBuffer(source.length()+300);
StringBuffer os = new StringBuffer(source.length()+10000);
Vector uses =new Vector();

int index = -1; int end = -1; int mark = -1;
int start=0;
do{
	mark = source.indexOf("use ",index);
	end = source.indexOf(";",mark);
	if ((end<0) || (mark<0)) {break;}
	os.append(source.substring(start,mark));
	index = end +1;
	start =index;
	uses.addElement(source.substring((mark+3),end).trim());
}
while ((index<source.length()-1) && (mark>-1));
if (start<source.length()-1) {os.append(source.substring(start,source.length()));}

/*now we should have a StringBuffer with the old source, an unused SB for
the new source, and a Vector containing all of the use files name...in order
of appearance*/

for (int i=0; i<uses.size();i++)
{
	String src = useFile((String)uses.elementAt(i));
	//System.out.println("using:"+uses.elementAt(i));
	sb.append(crunchUses(src));
	//System.out.println("sb.length()=="+sb.length());
}
//System.out.println("sb.length()-prior to append():"+sb.length());
sb.append(os.toString());
//System.out.println("sb.length()-post append():"+sb.length());
return sb.toString().trim();
}

public static String useFile(String fname)
{
if (used.containsKey(fname)) {return "";}

	String userfile="";
	String s;
	StringBuffer read=new StringBuffer(1000);
	
	if (fname.startsWith("dusty_")){userfile=LIB+fname;}
	else {userfile=USER+fname;}
	try{
		BufferedReader br = new BufferedReader(new FileReader(new File(userfile)));
		while ((s=br.readLine())!=null)
		{
		read.append(s).append('\n');
		}
		br.close();
	}
	catch(Exception e)
	{
		return "";
	}
	
	used.put(fname,"");
	
	if (fname.endsWith(".ds"))
		{
		return "<PREPROCESSED>"+
		SourceDataProcessor.stripComments(read.toString()).trim()+
		"</PREPROCESSED>";
		}
	return SourceDataProcessor.stripComments(read.toString()).trim();
}
}

