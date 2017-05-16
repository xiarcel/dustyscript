package dscript.preprocess;
import dscript.*;
import java.io.*;
import java.util.*;


class SourceDataProcessor{

static long INL=0;
static long LIT=0;
static long C_LIT=0;
static Output OUT;
static InputStream IN;
static
{
	try{
		OUT=new Output();
		IN=System.in;
	}
	catch(Exception e)
	{
		System.err.println("Failed at creating OUT/IN\n");
		e.printStackTrace();
	}
}

public static final String TCL= ActionDataProcessor.TCL;
public static final String STOPE =ActionDataProcessor.STOPE;
public static final String ETOPE = ActionDataProcessor.ETOPE;
public static final String STATE = "STATEMENTS";
public static final String LITERALS = "LITERALS";


public static String makeSourceData(String source)
{
	StringBuffer sb =new StringBuffer(700);
	/*need to add a "use" pre-process where it fills the source with
	the sources from all 'use' directives*/
	HashMap inlines = new HashMap(); 
	HashMap chars=new HashMap();
	HashMap strings=new HashMap();
	source = stripComments(source);
	source = PreUse.crunchUses(source);
	//System.out.println("Post PreUse.crunch.length():"+source.length());
	
	source= pullLiterals(inlines,strings,chars,source);
	//System.out.println("Source before replaceAll:\n"+source);
	source = Statement.replaceAll(source);
	source = pullInlines(source,inlines);
	source = fixGlobals(source,inlines);
	fixInlinedGlobals(inlines);
	
	StringBuffer acthings =new StringBuffer((int)(source.length()/2));
	source = Statement.replace(source,"Thing","thing");
	source = Statement.replace(source,"Action","action");
	source = Statement.replace(source,"Global","global");
	source = separateSources(source,acthings);
	String special = acthings.toString();
	System.out.println("Statements:\n"+source+"\n");
	System.out.println("\nSpecial:\n"+special+"\n");
	/*enter preprocess mode for things/actions*/
	ActionContainer ac =new ActionContainer(OUT);
	ThingTypeContainer ttc=new ThingTypeContainer(OUT,IN);
	VarContainer vc =new VarContainer(OUT);
	/*
	StatementProcessor cheat =new StatementProcessor(special,vc,ac,ttc,OUT,IN);
	cheat.suppress();
	cheat.setUserDebug(false);
	*/
	return source;
	
}

public static String pullLiterals(HashMap inlines, HashMap strings, HashMap chars, String base)
{
	StringBuffer sb = new StringBuffer((int)(base.length()/2));
	StringBuffer lit = new StringBuffer(50);
	
	boolean in_literal=false;
	char c= '\0';
	for (int i=0; i<base.length(); i++)
	{
		c = base.charAt(i);
		
		if (in_literal&&(c=='\\') && (i<(base.length()-1)))
		{
		char d=base.charAt(i+1);
		lit.append(makeEscapeChar(d));
		i++;
		continue;
		}
		
		if (c=='\"')
		{
			lit.append(c);
			if (in_literal)
			{
				in_literal=false;
				String lab="_$"+LIT+"$_";
				LIT++;
				strings.put(lab,lit.toString());
				lit.setLength(0);
				sb.append(lab);
				continue;
			}
			in_literal=true;
			continue;
		}
		if (in_literal){lit.append(c);}
		else{sb.append(c);}
	}
	base=sb.toString(); sb.setLength(0); lit.setLength(0);
	in_literal=false;
	for (int i=0; i<base.length(); i++)
	{
		c=base.charAt(i);
		if (in_literal && (c=='\\') && (i<(base.length()-1)))
		{
			char d= base.charAt(i+1);
			lit.append(makeEscapeChar(d));
			i++;
			continue;
		}
		
		if (c=='\'')
		{
			lit.append(c);
			if (in_literal)
			{
				in_literal=false;
				String lab="_&"+C_LIT+"&_";
				C_LIT++;
				chars.put(lab,lit.toString());
				lit.setLength(0);
				sb.append(lab);
				continue;
			}
			in_literal=true;
			continue;
		}
		if (in_literal){lit.append(c);}
		else{sb.append(c);}
	}
	
	/*there should be a last piece for pulling inlines here*/
	
	return sb.toString();
}




public static String makeEscapeChar(char e)
{
	switch(e)
	{
	case 'n': {return "\n";}
	case 'r': {return "\r";}
	case 't': {return "\t";}
	case '\\': {return "\\";}
	case '\"': {return "\"";}
	case '\'': {return "'";}
	default: break;
	}
	return "\\"+e;
}


public static String stripComments(String src)
{
	StringBuffer sb=new StringBuffer(100);
	boolean in_comment=false;
	boolean in_string_literal=false;
	boolean in_char_literal=false;
	
	for (int i=0; i<src.length(); i++)
	{
		char c= src.charAt(i);
		if (c=='\"' && !in_comment)
			{
			in_string_literal = !in_string_literal;			
			sb.append(c);
			continue;
			}
		if (c=='\'')
			{
			if (in_comment){continue;}
			sb.append(c);
			if (!in_string_literal){in_char_literal =!in_char_literal;}
			continue;
			}
		if (in_string_literal || in_char_literal)
			{
			sb.append(c);
			continue;
			}
		if ((c=='/')&&(i<src.length()-1))
			{
				char d = src.charAt(i+1);
				if (d == '*') {i++;in_comment=true; continue;}
			}
		if ((c=='*')&&(i<src.length()-1))
			{
				char d= src.charAt(i+1);
				if (d=='/') {i++;in_comment=false; continue;}
			}
		if (in_comment) {continue;}
		
		sb.append(c);
		}
	return sb.toString().trim();
	}
	
public static String pullInlines(String source, HashMap hm)
{
        boolean in_inline = false;
        StringBuffer holder = new StringBuffer(1000);
        StringBuffer ss = new StringBuffer(1000);
        int brack_count = 0;

        for (int i = 0; i < source.length(); i++)
        {
            char c = source.charAt(i);

            if (c == '{')
            {
                if (in_inline == false)
                {
                    in_inline = true;
                    continue;
                }

                brack_count++;

            }

            if (c == '}')
            { 
                if (in_inline && (brack_count == 0))
                {
                    in_inline = false;
		    String key = "inline_code%"+INL+"%";INL++;
                    hm.put(key,pullInlines(ss.toString().trim(),hm));
                    ss.setLength(0);
                    holder.append(' ').append(key);
                    
                    continue;
                }
		brack_count--;

            }

            if (in_inline == true)
            {
                ss.append(c);
                continue;
            }

            holder.append(c);

        }
	
        return holder.toString().trim();
    }
    
public static String fixGlobals(String source, HashMap inlines)
{	System.out.println("fixing block for globals(inline):\n"+source);
	int start=0; 
	int mark=-1; int index=-1;int end =-1;
	StringBuffer sb=new StringBuffer(source.length());
	do{
		mark = source.indexOf("global",index);
		if (mark < 0) {break;}
		sb.append(source.substring(start,mark));
		mark += 6;
		end = source.indexOf(";",mark);
		if (end < 0) {break;}
		sb.append(splitGlobals(source.substring(mark,end).trim(),inlines));
		index = end + 1;
		start =index;
	}
	while (index < source.length());
	if (start < source.length()-1) {sb.append(source.substring(start,source.length()));}
	return sb.toString();
}

public static String splitGlobals(String key, HashMap inlines)
{
StringBuffer ky =new StringBuffer();
for (int i=0;i<key.length();i++)
{
char c= key.charAt(i);
if ((c=='\n')||(c=='\t')||(c=='\r')||(c==' ')){continue;}
ky.append(c);
}
key = ky.toString();
	System.out.println("split globals for key:"+key);
if (!inlines.containsKey(key)) {return key;}
String backey=key;
key = (String)inlines.get(key);
inlines.remove(backey);
//System.out.println("Processing for globals:\n"+key);
StringBuffer states=new StringBuffer(key.length());
StringBuffer acts =new StringBuffer(key.length());
int index = -1; int start=0; int mark=-1; int end =-1;
do{
	mark = key.indexOf("action",index);
	if (mark <0) {break;}
	end = key.indexOf(";",mark);
	if (end <0) {break;}
	end++;
	acts.append(key.substring(mark,end).trim()).append('\n');
	states.append(key.substring(start,mark).trim()).append('\n');
	index=end;
	start=end;
}
while(index < key.length());
if (start < key.length()-1) {states.append(key.substring(start,key.length()).trim()).append('\n');}
StringBuffer nwsrc = new StringBuffer();
if (states.length()>0){nwsrc.append("global {").append(states.toString().trim()).append("};\n");}
if (acts.length()>0){nwsrc.append("global {").append(acts.toString().trim()).append("};\n");}
//System.out.println("New globals before inlines adjust:\n"+nwsrc.toString());
return pullInlines(nwsrc.toString(),inlines);
}
	
public static void fixInlinedGlobals(HashMap hm)
{
	Set st = hm.keySet();
	Iterator it = st.iterator();
	Vector v=new Vector();
	while (it.hasNext())
	{
		v.addElement(it.next());
	}
	for (int i=0; i<v.size();i++)
	{
		String s = (String)v.elementAt(i);
		String bak = s;
		if (hm.containsKey(s))
		{/*still?*/
		s=(String)hm.get(s);
		hm.remove(bak);
		s = fixGlobals(s,hm);
		hm.put(bak,s);
		}
	}

}

public static String separateSources(String source, StringBuffer stuff)
{
	StringBuffer sb=new StringBuffer((int)(source.length()/2));
	boolean nochar=false;
	for (int i=0; i<source.length();i++)
	{
		char c=source.charAt(i);
		
		if (Character.isWhitespace(c))
		{
			if (nochar){continue;}
			sb.append(c);
			nochar=true;
			continue;
		}
		if (c=='t'){i=appendSpecial(i,"thing",source,sb,stuff);nochar=false;continue;}
		if (c=='a'){i=appendSpecial(i,"action",source,sb,stuff);nochar=false;continue;}
		if (c=='g'){i=appendSpecial(i,"global",source,sb,stuff);nochar=false;continue;}
		sb.append(c);
		nochar=false;
	}
	return sb.toString().trim();
}


public static int appendSpecial(int index, String key, String source, StringBuffer sourcebuff, StringBuffer special)
{
	if ((source.indexOf(key,index) < 0))
	{
	sourcebuff.append(source.charAt(index));
	return index;
	}
	int start = source.indexOf(key,index);
	int end = source.indexOf(";",start);
	if (end < 0) {sourcebuff.append(source.charAt(index));
			return index;
			}
	special.append(source.substring(start,(end+1))).append('\n');
	return end;
}

		

public static void main(String[] args)
{	
	/*this main() will be removed when testing is complete*/
	String src= "";
	boolean loadedsource=false;
	if (args.length > 0)
	{
		try
		{
		
		StringBuffer sb=new StringBuffer();
		String s;
		BufferedReader br=new BufferedReader(new FileReader(new File(args[0])));
		while ((s=br.readLine())!=null)
		{sb.append(s).append('\n');}
		br.close();
		src=sb.toString();
		loadedsource=true;
		}
		catch(Exception e){}
	}
	if (!loadedsource){
	src ="use dusty_system.txt; use usexamp2.txt;\n\nsay \"Hello World\";\n";
	}
	
	System.out.println("SOURCE:\n"+src);
	src = makeSourceData(src);
	System.out.println("SOURCE W/ COMMENTS and STRING-CHAR LITs removed:\n"+src);
}


/*finis*/
}
