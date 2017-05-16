
package dscript;

import java.util.Vector;
import java.io.InputStream;

public class Statement
{ 	private static char[] chrs = new char[2];
	private boolean attempting = false; /*for use with other parts of dscript*/
  final static String pls ="+"; final static String min = "-";
  final static String spls=" + "; final static String smin = " - ";
  final static String inow=" is now ";
  final static String one = "1";
  static long IX_IX=0;
  private final static String[] originals = new String[]{"codeconnector","<<","->","<-",","," int ", " char ", "benchmark using",
		"ask for","Ask for","count up","is a child of"," is an ", " is a ", " is not an ", " is not a ",
		"Count up", "count down", "Count down", " plus ", " minus ", " times ",
		"multiplied by", " over ", "divided by", "is now", "is equal to", " equals ",
		"is not equal to", "does not equal", "does not contain", "as long as", "As long as",
		"is greater than or equal to", "is greater than", "is less than or equal to",
		"is less than", "attempt the following", "and if it fails", "what failed",
		"what went wrong", "the error code", "the mistake", "fail with", " and ", ")and(",
		")or(", " or ", "&&", "||", "!=", "==", "|", "+", "forever", " ~,", 
		"do separately", "run separately", "make separate", "location of",
		"starting at", "is a child of", "descends from", "is the thing type", "is type",
		"adds to", "thing type", "type of thing", " ME ", " this ", " THIS ", " Me ", " This ",
		"is identical to", "is referring to", "is not referring to", "refers to", " means ",
		"} ;", "{", "}"," exists "," !exists ","does not exist", "continue","synchronized action", "$;", "&;"};

  private final static String[] replacements = new String[] {"javaconnector","[[ "," using ", " using ",", "," integer ", " character ", "bm_with",
		"ask_for", "ask_for", "pcount","extends"," is_a "," is_a "," !is_a "," !is_a ",
		"pcount", "ncount", "ncount", " + ", " - ", " * ", "*", " / ", "/", "is_now", "==",
		" == ", "!=", "!=", "does_not_contain", "as_long_as", "as_long_as", ">=", ">", "<=",
		"<", "try", "catch", "ATTEMPT_MESSAGE", "ATTEMPT_MESSAGE", "DUSTY_ERROR_CODE",
		"DUSTY_ERROR_CODE", "fail_with", " & ", ")&(", ")|(", " | ", "&", "|", " != ", " == ",
		" | ", " + ", "as_long_as yes == yes", " ~ ",  "thread", "thread",
		"thread", "location_of", "starting_at", "extends", "extends", "is_type", "is_type", "extends",
		"thing", "thing", " me ",
		" me ", " me ", " me ", " me ", "thing_equals", "refers_to", "!refers_to", "thing_refer",
		" thing_refer ", "};", " { ", " } "," exists already "," !exists already ","!exists already", "next","s_action", "$ ;" , "& ;"};






    private final static String[] nostring= new String[0];

    private String line="original actually not available";
    private String[] parts= nostring;
    private String[] refs=nostring;
    private String[] inlines=nostring;
    private String[] equations=nostring;
    private String[] charrefs=nostring;
    //private HashMap inlines =null;

    public Statement()
    {
        line="";
    }


    public Statement(String ln)
    {
        line=ln;
    }

    public static String[] getOriginals()
    {
	    return originals;
    }
    
    public static String[] getReplacements()
    {
	    return replacements;
    }
    
    

    public String[] getEquations()
    {
        return equations;
    }


    public String[] getCharRefs()
    {
        return charrefs;
    }


    public void create()
    {

        /*preprocess*/

        /*line=pullComments(line);-removed-done in SP*/

	 String nw = line; /*this is the altered 'new' string*/

	 //nw = replace(nw,"#",":"); //doesn't this cause problems w/ string literals?
	 //REALLY WICKED HACK
	 nw=replace(nw,"\\\"","_:LIT_STR:_");
	 nw=replace(nw,"\\\'","_:LIT_CHR:_");
	 nw=replace(nw,"\\;","_:LIT_SC:_");
	 
	 /*PULLING STRING LITERALS*/
	 
	 
	 if (nw.indexOf("\"") > 0)
		{
		nw = pullStringVals(nw,this);
		}


        /*PULLING CHARACTER LITERALS!*/
	 if (nw.indexOf("\'") > 0)
		{
		nw = pullCharVals(nw,this);
		}

	nw=replace(nw,"#",":"); //used to  be above--see if this breaks.
	nw = fixOp(nw);
	
	 /*!!final static string[]s for replace now*/

	nw = replaceAll(nw);
		
        /*!!!!new for pulling equations*/
        nw=pullEquations(nw);
	/*sets parts through method...this can be over-ridden now*/
	makeParts(this,nw);
	setLiteralRefs();
	
    }
    
private void setLiteralRefs()
{
	for (int i=0;i<charrefs.length;i++)
	{
		charrefs[i]=replace(charrefs[i],"_:LIT_STR:_","\"");
		charrefs[i]=replace(charrefs[i],"_:LIT_CHR:_","\'");
		charrefs[i]=replace(charrefs[i],"_:LIT_OB:_","{");
		charrefs[i]=replace(charrefs[i],"_:LIT_CB:_","}");
		charrefs[i]=replace(charrefs[i],"_:LIT_SC:_",";");
	}
	for (int i=0;i<refs.length;i++)
	{
		refs[i]=replace(refs[i],"_:LIT_STR:_","\"");
		refs[i]=replace(refs[i],"_:LIT_CHR:_","\'");
		refs[i]=replace(refs[i],"_:LIT_OB:_","{");
		refs[i]=replace(refs[i],"_:LIT_CB:_","}");
		refs[i]=replace(refs[i],"_:LIT_SC:_",";");
	}
}

  
public String fixOp(String nw)
{
	if ((nw.endsWith("++"))||(nw.endsWith("--")))
        {
            String tt = nw.substring(0,nw.length()-2);
            String ed =nw.substring(nw.length()-2,nw.length());
            nw ="";
            nw = tt+inow+tt;

            if(ed.equals("--"))
            {
                nw=nw+smin;
            }

            else
            {
                nw=nw+spls;
            }

            nw=nw+one;
        }
return nw;
}


public static void makeParts(Statement s, String nw)
{
        Vector v=new Vector();
	StringBuffer ss=new StringBuffer(300);
	  nw.trim();

        for (int k=0; k<nw.length();k++)
        {

            if (nw.charAt(k)==' ')
            {
                if(ss.length() > 0)
                {
                    v.addElement(ss.toString());
                    ss.setLength(0);
                }

                continue;
            }

            if (nw.charAt(k)=='+')
            {
                if(ss.length()>0)
                {
                    v.addElement(ss.toString());
                }

                v.addElement(pls);
                ss.setLength(0);
                continue;
            }

            if (nw.charAt(k)=='-')
            {
                if(ss.length()>0)
                {
                    v.addElement(ss.toString());
                }

                v.addElement(min);
                ss.setLength(0);

                continue;
            }

            if((nw.charAt(k)=='*')||(nw.charAt(k)=='/'))
            {
                String pullback="";

                if(nw.charAt((k-1))==' ')
                {
                    pullback = (String)v.elementAt((v.size()-1));
                	  v.removeElementAt((v.size()-1));
			}

                if (ss.length() > 0)
                {
                    StringBuffer new_one = new StringBuffer(200);

                    for (int m=0; m<ss.length();m++)
                    {
				char c= ss.charAt(m);
                        if(c != ' ')
                        {
                            new_one.append(c);
                        }

                    }

                    ss.setLength(0); ss.append(new_one.toString());
                }

                ss.insert(0,pullback);


                if(nw.charAt(k+1)==' ')
                {
                    ss.append(nw.charAt(k)); k++;continue;
                }

            }

            ss.append(nw.charAt(k));

        }
	  if (ss.length()>0){v.addElement(ss.toString());}

	  Object[] o = v.toArray();
        String[] pts=new String[o.length];
	  System.arraycopy(o,0,pts,0,o.length);

	  s.resetParts(pts);
	  
    }
    
    public String pullEquations(String src)
    {
	    return pullEquations(src,this);
    }
    


    public static String pullEquations(String src, Statement state)
    {
	   
	 /*try to avoid doing this when un-necessary*/
	 if ((src.indexOf("(") < 0)||(src.indexOf(")")< 0))
		{
		/*no equation*/
		return src;
		}

        boolean in_eq=false;
        Vector eqs=new Vector();
        int parcount=0;
        int eqcount=0;
        StringBuffer ret=new StringBuffer(300);
        StringBuffer eq=new StringBuffer(300);

        for (int i=0; i<src.length();i++)
        {
            char c = src.charAt(i);

            if (c=='(')
            {
                in_eq=true;
                eq.append(c);
                parcount++;
                continue;
            }

            if (c==')')
            {
                eq.append(c);
                parcount--;

                if (parcount==0)
                {
                    in_eq=false;
                    eqs.addElement(eq.toString());
                    eq.setLength(0);

                    ret.append("eq_temp_"+eqcount);
                    eqcount++;
                }

                continue;

            }

            if (in_eq)
            {
                eq.append(c);
            }
            else
            {
                ret.append(c);
            }

        }

	  Object[] o = eqs.toArray();
        String[] equaties=new String[o.length];
	  System.arraycopy(o,0,equaties,0,o.length);
	  state.setEquations(equaties);
        return ret.toString();
    }

    public void replace(String key, String rep)
	{
	for (int i=0; i<parts.length;i++)
		{
		if (parts[i].equals(key)) {parts[i] = rep;}
		}

	}
	
    public static String[] replaceLiterals(Statement s, String[] parts)
    {
	    String[] cpy=new String[parts.length];
	    System.arraycopy(parts,0,cpy,0,parts.length);
	    
	    String[] srefs = s.getRefs();
	    String[] crefs = s.getCharRefs();
	    
	    for (int i=0; i<cpy.length;i++)
	    {
		    for (int j=0; j<srefs.length;j++)
		    {
			    if (cpy[i].equals("$"+j+"$"))
			    {cpy[i] = srefs[j];
			    //if (cpy[i].length() > 1 && cpy[i].startsWith("\"")&&cpy[i].endsWith("\"")){cpy[i]=cpy[i].substring(1,(cpy[i].length()-1));}
			    break;}
		    }
		    for (int k=0; k<crefs.length;k++)
		    {
			    if (cpy[i].equals("&"+k+"&"))
			    {cpy[i] = crefs[k];
			   // System.err.println(i+":"+cpy[i]);
				//    if (cpy[i].length() > 1 && cpy[i].startsWith("\'")&&cpy[i].endsWith("\'")){cpy[i]=cpy[i].substring(1,(cpy[i].length()-1));}
			    break;}
		    }
	    }
	    return cpy;
    }
    

    public static String replaceAll(String old)
    {
	    String[] orig=getOriginals();
	    String[] rep = getReplacements();
	    for (int i=0; i<orig.length;i++)
	    {
		    old = replace(old,orig[i],rep[i]);
	    }
	    return old;
    }
    

	
    public static String replace(String old, String key, String rep)
    {
        String original = old;
        if ((key.length() > 0) && (key.charAt(0) == ' '))
        {
            // May need to prepend a space to the string we're searching through.
            // Prepending is surprisingly slow (this routine is called many times),
            // so a 'quick' check to see whether a 'spaceless' prefix from the key
            // can be found within the original string is performed.

            // Example:
            // old = "integer myInt", key = " integer "
            // search for "in" (chars 1..2 of key) within 'old' and finds it ->
            // worth continuing with replace - key _MAY_ appear within 'old'.
            // old = "integer myInt", key = " char "
            // search for "ch" (chars 1..2 of key) within 'old' and cannot find it ->
            // not worth continuing - key _CANNOT_ appear within 'old'.

            // Experimentation showed that 3 was the best offset here.
            final int QUICK_SEARCH_CHAR_OFFSET = 3;

            int quick_search_offset = (key.length() >= QUICK_SEARCH_CHAR_OFFSET) ?
                                       QUICK_SEARCH_CHAR_OFFSET : key.length();

            if (old.indexOf(key.substring(1, quick_search_offset)) < 0)
            {
                return original;
            }

            old = " " + old;
        }

        if (old.indexOf(key) < 0)
        {
            return original;
        }
	
	int index=-1;
	int mark=-1;
	int start=0;
	StringBuffer repd=new StringBuffer(old.length());
	do{
		mark = old.indexOf(key,index);
		if (mark < 0) {break;}
		index =mark + key.length();
		repd.append(old.substring(start,mark));
		repd.append(rep);
		start=index;
	}
	while (mark>=0);
	if (start<old.length()) {repd.append(old.substring(start,old.length()));}
	return repd.toString().trim();
	
	
	/*Below commented out because a replace that involves adding a whitespace
		character will only do it once*/
	/*
        char[] reppy = rep.toCharArray();

        int index = -1;
        char[] oldy = null;
        boolean do_once = false;
        if (rep.indexOf(key) > -1)
        {
            do_once = true;
        }
        do
        {
            index = old.indexOf(key);
            if (index > -1)
            {
                oldy = old.toCharArray();
                char[] newy = new char[oldy.length + reppy.length - key.length()];
                System.arraycopy(oldy, 0, newy, 0, index);
                System.arraycopy(reppy, 0, newy, index, reppy.length);
                System.arraycopy(oldy, index + key.length(), newy, index + reppy.length, (oldy.length - (index + key.length())));
                old = new String(newy);
            }
            else
            {
                break;
            }

        } while ((index > -1) && !do_once);

        return old.trim();
	*/
    }


    public String[] getParts()
    {
        return parts;
    }


    public String[] getRefs()
    {
        return refs;
    }


    public String[] getInline()
    {
        return inlines;
    }


    public String getLine()
    {
        return line;
    }


    public void setRefs(String[] refers)
    {
        refs = null;
        refs = refers;
    }


    public void setInline(String[] i)
    {
        inlines=i;
    }


    public void resetParts(String[] p)
    {
        parts=p;
        StringBuffer buffer = new StringBuffer(100);

        for (int i=0; i < p.length;i++)
        {
            buffer.append(p[i]);
            buffer.append(' ');
        }

        buffer.append(';');

        line = new String(buffer);
    }


    public void setEquations(String[] e)
    {
        equations =e;
    }


    public void setCharRefs(String[] c)
    {
        charrefs=c;
    }

    public void setAttempting(boolean b)
	{attempting=b;}

    public boolean getAttempting()
	{return attempting;}

    private static String pullCharVals(String line, Statement state)
    {

        StringBuffer nw=new StringBuffer(300);StringBuffer this_literal=new StringBuffer(300);
	  int charnum=0;
        boolean in_literal=false;
        Vector it=new Vector();

        for (int i=0; i<line.length();i++)
        {
            char c= line.charAt(i);
	    if (c=='\\' && in_literal)
		{
		try {
		    chrs[0] = c;
		    chrs[1] = line.charAt(i+1);
			
		     char e = isSpecialChar(new String(chrs));
			if (e != '\0') {this_literal.append(e); i++; continue;}
			}	
		catch(Exception um){}
		}

            if (c=='\'')
            {
		    this_literal.append(c);
                if (in_literal==false)
                {
                    in_literal=true;
                    continue;
                }
                else
                {
                    in_literal=false;
                    it.addElement(this_literal.toString());
                    this_literal.setLength(0);
                    nw.append('&').append(charnum).append('&');//.append(' ');
                    charnum++;
                    continue;

                }
            }

            if (in_literal)
            {
                this_literal.append(c);continue;
            }

            nw.append(c);

        }

	  Object[] o = it.toArray();
        String[] charrfs=new String[o.length];

	
	System.arraycopy(o,0,charrfs,0,o.length);
	  state.setCharRefs(charrfs);
        return nw.toString();

    }


    public static String pullStringVals(String line, Statement state)
	{
	  StringBuffer nw =new StringBuffer(300);
        Vector it=new Vector();
        boolean in_literal=false;
        StringBuffer this_literal =new StringBuffer(300);
        int stringnum=0;

        for (int i=0; i<line.length();i++)
        {
            char c= line.charAt(i);

		if (c=='\\' && in_literal)
		{
		try {
		    chrs[0] = c;
		    chrs[1] = line.charAt(i+1);
			
		     char e = isSpecialChar(new String(chrs));
			if (e != '\0') {this_literal.append(e); i++; continue;}
			}	
		catch(Exception um){}
		}
            if (c=='\"')
            {
                this_literal.append(c);
                if (in_literal==false)
                {
                    in_literal=true;
                    continue;
                }
                else
                {
                    in_literal=false;
                    it.addElement(this_literal.toString());
                    this_literal.setLength(0);
                    nw.append('$').append(stringnum).append('$');//.append(' ');
                    stringnum++;
                    continue;
                }

            }

            if(in_literal)
            {
                this_literal.append(c);continue;
            }

            nw.append(c);
        }
	  Object[] o = it.toArray();
        String[] rfs=new String[o.length];
	  
	System.arraycopy(o,0,rfs,0,o.length);
	state.setRefs(rfs);
	return nw.toString();
	}

	public static char isSpecialChar(String s)
	{

	// THIS SHOULD BE DONE MORE GRACEFULLY!
	//System.out.println("s.len=="+s.length()+":"+s);
        if (s.equals("\\n")) {return '\n';}
	if (s.equals("\\{")) {return '{';}
	if (s.equals("\\}")) {return '}';}
	if (s.equals("\\r")) {return '\r';}
	if (s.equals("\\\"")) {return '\"';}
	if (s.equals("\\\'")) {return '\'';}
	if (s.equals("\\\\")) {return '\\';}
	if (s.equals("\\\t")) {return '\t';}
	//if (s.equals("\\;")) {return ';';}

	return '\0';
	}

    public String originalUserStatement()
    {
        /*for use w. 'reporter' feature*/

        String cpy = line;

        for (int i=0; i<equations.length;i++)
        {
            if (cpy.indexOf("eq_"+i) > -1)
            {
                cpy= replace(cpy,"eq_"+i,equations[i]);
            }
        }

        for (int i=0; i<charrefs.length;i++)
        {
            if (cpy.indexOf("&"+i+"&")>-1)
            {
                cpy = replace(cpy,"&"+i+"&",charrefs[i]);
            }
        }

        for (int i=0; i<refs.length;i++)
        {
            if (cpy.indexOf("$"+i+"$")>-1)
            {
                cpy = replace(cpy,"$"+i+"$",refs[i]);
            }
        }

        for (int i=0; i<inlines.length;i++)
        {
            if (cpy.indexOf("inline_code%"+i+"%")>-1)
            {
                cpy = replace(cpy,"inline_code%"+i+"%","{"+inlines[i]+"};");
            }
        }

        return cpy;
    }

    public static boolean containsArrayPart(String s)
    {
	    //slight misnomer now..also anonymous actions
	    return ((s.indexOf("[")>-1)&&(s.indexOf("]")>-1));
    }
    
    
    public static String replaceArrayElements(String s, VarContainer vc, ActionContainer ac, ThingTypeContainer ttc, Output OUT, InputStream IN, boolean attempting)
    {	
	    //change following lines to StringBuffer if StringBuffer isn't working
	    StringBuffer nw = new StringBuffer(s.length());
	    StringBuffer ix = new StringBuffer(20);
	    boolean in_index=false;
	    char last='\0';
	    for (int i=0; i<s.length();i++)
	    {	
		    char c= s.charAt(i);
		
		    if (c=='[' && last != '\"') {in_index=true;
		    if (last != ' ' && last != ',') {nw.append(':');last=':';} //will no longer return  with ':'
		    continue;}
		    
		    if (c==']' && in_index) 
		    {
			    in_index=false;
			    nw.append(replaceArrayElement(ix.toString(),vc,ac,ttc,OUT,IN,attempting,last));ix.setLength(0);
			    last=c;
			    continue;
		    }
		    
		    if (in_index) {ix.append(c);continue;}
		    nw.append(c);
		    last=c;
	    }
	    return nw.toString();
    }
    
    public static String replaceArrayElement(String s, VarContainer vc,ActionContainer ac, ThingTypeContainer ttc, Output OUT, InputStream IN, boolean AT, char last)
    {
	    
	    if (s.equalsIgnoreCase("size")||s.equalsIgnoreCase("last")||VarContainer.isNumberLiteral(s)||vc.is_in(s))
	    {return s;}
	    String nm = "AR_EL_"+IX_IX; IX_IX++;
	    String lab=null;
	    if (last == ':') /*index for group*/ {lab="integer ";}
	    else {lab="anyvar ";} /*anon-action*/
	    //s = "integer "+nm+" is "+s;
	    s = lab+nm+" is "+s;
	    StatementProcessor sp=new StatementProcessor(s,vc,ac,ttc,OUT,IN);
	    sp.suppress();
	    
	    sp.setAttempting(vc.attempting); //errors should spit out for these embedded actions
	    sp.run();
	    if (sp.FAILED) {return s;}
	   
	   try{vc.get(nm).setAsTransient();} catch(Exception e) {return s;}
	    //vc.remove(v);
	    //return ":"+v.getInteger();
	    //return v.asString();
	    return nm;
    }
    
}
