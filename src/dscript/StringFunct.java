package dscript;
	
class StringFunct{

private final static String[] ESCAPES = {"\\n","\\r","\\","\\t","\""};
private final static char[] REPLACE = {'\n','\r','\\','\t','\"'};

public static char makeChar(String s)
	{
	for (int i=0; i<ESCAPES.length;i++)
		{
		if (s.equals(ESCAPES[i]))
			{return REPLACE[i];}
		}
	return ' ';
	}

public static String pullControlChars(String base)
	{

	for (int i=0; i<ESCAPES.length;i++)
		{
		base=replaceString(base,ESCAPES[i],REPLACE[i]);
		}
	return base;
	}

public static String replaceString(String in, String sch, char rep)
	{
	StringBuffer out=new StringBuffer(150);
	int start=0;
	int mark =-1;
	while(true)
		{
		mark=in.indexOf(sch,start);
		if (mark < 0)
			{
			out.append(in.substring(start,in.length()));
			break;
			}
		out.append(in.substring(start,mark));
		out.append(rep);
		start=mark+sch.length();
		mark=-1;
		}
	return out.toString();
	}

public static Var charsToString(Var v, String varname)
	{
	
	if (v.getType()!=Var.GROUP)
		{
		return new Var(v.asString(),varname);
		}
	VarGroup vg = v.getVarGroup();
	
	if (vg.getType() > 4)
		{
		return new Var("group->"+v.getName(),varname);
		}
	Var[] innies = vg.in();
	StringBuffer contents=new StringBuffer(50);
	for (int i=0; i<innies.length; i++)
		{
            String ias = innies[i].asString();
	    //System.out.println("innies["+i+"]=="+innies[i]);
		if (!ias.equals(">NOVALUE<"))
			{
			contents.append(ias);
			}
			else {contents.append("~");}
		}
	return new Var(contents.toString(),varname);
	}

public static Var charsToString(Var v, String varname, boolean minus)
	{
	Var voov = charsToString(v,varname);
	if (minus) {try{voov.reverseValue();}catch(Exception e) {e.printStackTrace(); System.err.println(voov.getName()+"=="+voov.asString()+"=="+voov.getType());}}
	return voov;
	}
public static Var stringToChars(Var v, String grpname, VarContainer VC)
	{
	StringBuffer toturn=new StringBuffer(50);
	if (v.getType() != Var.STR)
		{
		toturn.append(charsToString(v,"").asString());
		}
		else{
			toturn.append(v.asString());}
	VarGroup vg=new VarGroup(grpname,Var.CHR,VC);
      String tturn = toturn.toString();
      //System.out.println("tturn==("+tturn+"), grpname ==("+grpname+")");
	for (int i=0; i<tturn.length(); i++)
		{
		char c = tturn.charAt(i);
		vg.add(new Var(c,""+(i+1)),VC);
		}
	return new Var(vg,grpname);
	}

public static Var stringToChars(Var v, String grpname, boolean minus,VarContainer VC)
	{
	Var voo = stringToChars(v,grpname,VC);
	if (minus) {voo.reverseValue();}
	return voo;
	}
	


public static Var stringToChars(String s, String varname,VarContainer VC)
	{
	if (s.endsWith("\"")){s=s.substring(0,(s.length()-1));}
	if (s.startsWith("\"")){s=s.substring(1,s.length());}
	VarGroup vg = new VarGroup(varname,Var.CHR,VC);
	
	for (int i=0; i<s.length(); i++)
		{
		char c = s.charAt(i);
		vg.add(new Var(c,""+(i+1)),VC);
		}
	return new Var(vg,varname);
	}



public static boolean copyCommand(Statement st, VarContainer vc, Output OUT)
	{
	boolean attempt=st.getAttempting();
	String[] strefs=st.getRefs();
	String[] s = st.getParts();
	s = If.adjustForMinus(s);
	if ((s.length <3)|(s.length>4))
			{
			OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"This 'copy' statement is the wrong size",st),
					vc,attempt);
			return false;
			}
	String[] parts=new String[s.length];
	for (int i=0;i<parts.length;i++)
			{parts[i] = s[i];
			 if (parts[i].startsWith("$")& parts[i].endsWith("$"))
				{
				try{
				   int p = Integer.parseInt(parts[i].substring(1,(parts[i].length()-1)));
				   parts[i]=strefs[p];
				   }
				   catch(Exception e){}
				}
			}
	boolean string_literal=false;
	String rvs = parts[(parts.length-1)];
	if(StatementProcessor.DEBUG){OUT.println("rvs=="+rvs,2);}
	if (rvs.startsWith("\"")& rvs.endsWith("\""))
		{string_literal=true;
		 rvs = rvs.substring(1,(rvs.length()-1));
		 parts[parts.length-1] =rvs;
		}
	if (string_literal & (parts[0].equalsIgnoreCase("group")))
		{
		parts[0]=new String("characters");
		}

	if (!string_literal & vc.is_in(rvs))
		{
		if (parts[0].equalsIgnoreCase("group"))
			{
			if (vc.get(rvs).getType()==Var.STR)
				{parts[0] = "characters";}
			//not sure why below was there... it was altered
			//else{parts[0] = Var.PLURALS[vc.get(rvs).getType()];}
			}
		}
	return processCopy(parts,vc,string_literal,OUT,attempt);
	}

private static boolean processCopy(String[] parts, VarContainer vc, boolean is_literal, Output OUT, boolean attempt)
	{
	if (parts.length == 4)
		{
		if (is_literal)
			{
			if (parts[0].equals("characters"))
				{
				vc.add(stringToChars(parts[3],parts[1],vc));
				if(StatementProcessor.USER_DEBUG)
				{
				OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+parts[3]+"' to '"+parts[1]+"'",true));
				}
				return true;
				}
			OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"The types were mismatched in a 'copy' statement"),vc,attempt);
			return false;
			}
		boolean minus =false;
		if (parts[3].startsWith("-")){minus=true; parts[3]=parts[3].substring(1,parts[3].length());}

		//System.out.println("parts[3]=="+parts[3]);
		if (vc.is_in(parts[3]))
			{
			//System.out.println("parts[3] is_in");
			Var right = vc.get(parts[3]);
			if ((parts[0].equals("characters")||parts[0].equals("group"))&&(right.getType()==Var.STR))
				{
				vc.add(stringToChars(right,parts[1],minus,vc));
				if(StatementProcessor.USER_DEBUG)
					{
					OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+right.getName()+"' to '"+parts[1]+"'",true));
					}
				return true;
				}
			
			int rtype = right.getType();
			int rgtype=-1;
			if (rtype == Var.GROUP)
				{
				rgtype=right.getVarGroup().getType();
				}
			//System.out.println("parts[0]=="+parts[0]);
			int ltype = Var.typeInt(parts[0]);
			//System.out.println("ltype=="+ltype+",rtype=="+rtype);
			if (rtype == ltype)
				{
				Var voov = Var.copy(right,parts[1],vc);
				if (minus){voov.reverseValue();}
				vc.add(voov);
				if (StatementProcessor.USER_DEBUG)
					{
					OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+right.getName()+"' to '"+parts[1]+"'",true));
					}
				 return true;
				}
			if ((ltype == rgtype) & (rtype==Var.GROUP))
				{
				vc.add(charsToString(right,parts[1],minus));
				if (StatementProcessor.USER_DEBUG)
					{
					OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+right.getName()+"' to '"+parts[1]+"'",true));
					}
				return true;
				}
			/*
			if ((ltype==Var.STR) & (rgtype==Var.CHR))
			*/
			if (ltype == Var.STR)
				{
				vc.add(charsToString(right,parts[1],minus));
				if (StatementProcessor.USER_DEBUG)
				{
				OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+right.getName()+"' to '"+parts[1]+"'",true));
				}
				return true;
				}
			}
			OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"Either a variable to be copied to/from was not there(??)\n\t...or this statement is too short"),
				vc,attempt);
			return false;
		}

		if (!vc.is_in(parts[0]))
			{
			OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"'"+parts[0]+"' does not seem to be a variable\n\t...and you can't copy to nothing"),
				vc,attempt);
			return false;
			}			
		Var left = vc.get(parts[0]);
		if (is_literal)
			{
			if (StatementProcessor.DEBUG){OUT.println("left.type()=="+left.getType(),2);}
			
			if (left.getType() == Var.STR)
				{
				Var nw = new Var(parts[2],parts[0]);
				vc.replace(parts[0],nw);
				if(StatementProcessor.USER_DEBUG)
				{
				OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+parts[0]+"' to '"+parts[0]+"'",true));
				}
				return true;}

			if (left.getType() == Var.GROUP)
				{			
				if(StatementProcessor.DEBUG){OUT.println("left.vg.type()=="+left.getVarGroup().getType(),2);}	
				int lvgt = left.getVarGroup().getType();
				if ((lvgt != Var.CHR)&&(lvgt != -1) )
					{
					OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"A String can only be 'copied' to Group of characters\n\tor a group that does not have the type contained defined!"),
						vc,attempt);
					return false;						
					}
				Var nw = stringToChars(parts[2],parts[0],vc);
				vc.replace(parts[0],nw);
				if(StatementProcessor.USER_DEBUG)
					{
					OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+parts[2]+"' to '"+parts[0]+"'",true));
					}
				return true;
				}
			OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"Non-string, Non-character groups cannot copy a string-literal"),
				vc,attempt);
			return false;
			}
		boolean minus =false;
		if (parts[2].startsWith("-")) {parts[2]=parts[2].substring(1,parts[2].length()); minus = true;}

		if (!vc.is_in(parts[2]))
			{
			OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"'"+parts[2]+"' is not defined as a variable!"),
					vc,attempt);
			return false;
			}
		Var right = vc.get(parts[2]);
		if (left.getType()==Var.STR & right.getType()==Var.GROUP)
			{
			VarGroup vg = right.getVarGroup();
			/*if (vg.getType() != Var.CHR)
				{
				OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"A single String can only copy a group of Characters!"),vc,attempt);
				return false;
				}*/
			vc.replace(parts[0],charsToString(right,parts[0],minus));
			if (StatementProcessor.USER_DEBUG)
			{
			OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+right.getName()+"' to '"+parts[0]+"'",true));
			}
			return true;
			}
		if (left.getType()==Var.GROUP && right.getType()==Var.STR)
			{
			VarGroup vg = left.getVarGroup();
			int lvgt = vg.getType();
			if ((lvgt!= Var.CHR)&&(lvgt != -1))
				{
				OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"A single String can only copy a group of Characters\n\t"+
					"or a group that does not have what it contains defined yet!"),
					vc,attempt);
				return false;
				}
			vc.replace(parts[0],stringToChars(right,parts[0],minus,vc));
			if (StatementProcessor.USER_DEBUG)
			{
			OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+right.getName()+"' to '"+parts[0]+"'",true));
			}
			return true;
			}
		if (left.getType() != right.getType())
			{
			OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"'"+left.getName()+"' and '"+right.getName()+"' are not compatible for this copy"),
				vc,attempt);
			return false;
			}
		Var voov = Var.copy(right,parts[0],vc);
		if (minus) {voov.reverseValue();}
		vc.replace(parts[0],voov);
		if(StatementProcessor.USER_DEBUG)
			{
			OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ASS,"We successfully copied '"+right.getName()+"' to '"+parts[0]+"'",true));
			}
		return true;
		}


/*intended for all future string-char literal pulls*/

public static String[] pullStringLiterals(Statement state, VarContainer vc)
{
 String[] parts = state.getParts();
 String[] nwparts =new String[parts.length];
 for (int i=0;i<parts.length;i++)
	{
	nwparts[i]=pullStringLiteral(parts[i],state,vc);
	}
 return nwparts;
}

public static String pullStringLiteral(String s, Statement state, VarContainer vc)
{
 if (s.startsWith("\"")){return pullControlChars(s.substring(1,s.length()-1));}
 if (s.startsWith("\'")){return pullControlChars(s.substring(1,s.length()-1));}


 if (s.startsWith("$") & s.endsWith("$"))
	{
       s=s.substring(1,s.length()-1);
 	 try{
		int i = Integer.parseInt(s);
		String[] strs=state.getRefs();
		return strs[i];
		}
		catch(Exception e){return "";}
	}

 if (s.startsWith("&") & s.endsWith("&"))
	{
	s=s.substring(1,s.length()-1);
	try{
	      int i = Integer.parseInt(s);
		String[] chrs = state.getCharRefs();
	      return pullControlChars(chrs[i]);
	   }
	   catch(Exception e){return "";}
	}

 if (vc.is_in(s)){return vc.get(s).asString();}
 return s;
}

/*LOCATION-OF (indexOf)*/
public static String locationOf(String[] parts,Statement state, VarContainer vc,Output OUT)
{

	
	
 if (parts.length < 3)
	{if(StatementProcessor.DEBUG) {for (int i=0;i<parts.length;i++){OUT.println("parts["+i+"]:"+parts[i],2);}}
	 OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"This 'location of' statement is not the right size",state),vc,state.getAttempting());
	 return "-1";
	}

 if (!parts[1].equals("in"))
	{
	OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"Dustyscript expected 'in', and got '"+parts[1]+"'",state),vc,state.getAttempting());
	return "-1";}
 int sindex=0;
  if (parts.length==5)
	{if (!parts[3].equals("starting_at"))
	{
	OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"Dustyscript was expecting 'starting at' but got '"+parts[3]+"'",state),
			vc,state.getAttempting());
	return "-1";
	}
	
       try {
           if (vc.is_in(parts[4]))
			{sindex=Integer.parseInt(vc.get(parts[4]).asString())-1;
			}
           		else{sindex=Integer.parseInt(parts[4])-1;}
		}
		catch(Exception e){sindex=0;}
	}
 parts[0] = pullStringLiteral(parts[0],state,vc);
 if (parts[0].startsWith("\"")&&parts[0].endsWith("\"")) {parts[0]=parts[0].substring(1,parts[0].length()-1);}
 parts[2] = pullStringLiteral(parts[2],state,vc);
 if (parts[2].startsWith("\"")&&parts[2].endsWith("\"")) {parts[2]=parts[2].substring(1,parts[2].length()-1);}
 if (StatementProcessor.DEBUG || true) 
{
for (int i=0; i<parts.length;i++) {OUT.println("parts["+i+"]=="+parts[i],2);}
}
 int i = parts[2].indexOf(parts[0],sindex);
 if (i >= 0) {i++;}
 return ""+i;
}



 



/*finis*/

}
