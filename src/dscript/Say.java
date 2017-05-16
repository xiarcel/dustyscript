package dscript;
class Say{
final static String yny = "\"yes\"";
final static String ynn = "\"no\"";

public static boolean exec(VarContainer vc, Statement state, Output OUT)
{

boolean AT = state.getAttempting();
 String[] parts=state.getParts();
 String[] refs= state.getRefs();String[] charrefs=state.getCharRefs();
 StringBuffer tosay=new StringBuffer(600);
 if (!parts[0].equalsIgnoreCase("say"))
	{
	OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.SAY,"This is an odd error\nDustyscript expected that this was a 'say' command\n..but it does not appear to be",state),
				vc,AT);
	return false;}
 if (parts.length < 2)
	{
	OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.SAY, "This 'say' command is too short",state),
			vc,AT);
	return false;}
 
/* put the string literals back*/
/*
for (int i=0; i<refs.length; i++)
	{
	for (int j=1; j<parts.length;j++)
		{
		if (parts[j].equals("$"+i+"$"))
		{parts[j]=refs[i];}
		if (parts[j].startsWith(":")) {parts[j]=parts[j].substring(1,parts[j].length());} //fix for anon-actions
		}
	}
*/
boolean cont=false;
for (int i=1; i<parts.length;i++)
{
	cont=false;
	if (parts[i].startsWith(":")) {parts[i]=parts[i].substring(1,parts[i].length());cont=true;}
	if (parts[i].equalsIgnoreCase("yes")) {parts[i]=yny; cont=true;}
	else if (parts[i].equalsIgnoreCase("no")) {parts[i]=ynn; cont=true;}
	
	if (cont) {continue;}
	
	for (int j=0;j<refs.length;j++)
	{
		if (parts[i].equals("$"+j+"$"))
		{
			parts[i]=refs[j];
			cont=true;
			break;
		}
	}
	if (cont) {continue;}
	for (int k=0;k<charrefs.length;k++)
	{
		if (parts[i].equals("&"+k+"&"))
		{
			parts[i]=charrefs[k];
			break;
		}
	}
}

/*put the char literals back*/
/*
for (int i=0; i<charrefs.length;i++)
	{
	for (int j=1; j<parts.length;j++)
		{
		if(parts[j].equals("&"+i+"&"))
		{parts[j]=charrefs[i];}
		}
	}

/*make OK for yes or no*/
/*for (int i=0; i<parts.length;i++)
	{
	if (parts[i].equalsIgnoreCase("yes"))
		{
		parts[i] = yny;
		}
	else if (parts[i].equalsIgnoreCase("no"))
		{
		parts[i] = ynn;
		}
	}

*/

/*construct tosay*/

boolean looking_for_plus=false;

for (int k=1; k<parts.length;k++)
	{
	//System.out.println(parts[k]);
	if(StatementProcessor.DEBUG){OUT.println("evaluating:"+parts[k],2);}
	if (looking_for_plus)
		{
		if (parts[k].equals("+")==false)
		{
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.SAY,"Dustyscript expected a '+' or a 'plus', not '"+parts[k]+"'",state),
			vc,AT);
		return false;}
		looking_for_plus=false;
		continue;
		}

	if (parts[k].startsWith("\""))
		{
		parts[k]=parts[k].substring(1,(parts[k].length()-1));
		tosay.append(StringFunct.pullControlChars(parts[k]));
		looking_for_plus=true;
		continue;
		}

	if (parts[k].startsWith("\'"))
		{
		parts[k] = StringFunct.pullControlChars(parts[k].substring(1,(parts[k].length()-1)));
		tosay.append(parts[k]);
		looking_for_plus=true;
		continue;
		}

	/*check for number literal*/
	if (VarContainer.isNumberLiteral(parts[k]))
		{
		tosay.append(parts[k]);
		continue;
		}
	
      try{
	boolean minus=false;
	if (parts[k].startsWith("-")){parts[k]=parts[k].substring(1,parts[k].length());minus=true;}
	//System.out.println("("+parts[k]+")");
	if(vc.is_in(parts[k]))
	{
	Var v=vc.get(parts[k]);
	if (v.getType()==Var.GROUP)
	{	
		v = Var.copy(v,vc);
		if (minus){v.reverseValue();}
		tosay.append(v.asString());
		looking_for_plus=true;
		continue;
	}
	String addit = v.asString();
	addit=StringFunct.pullControlChars(addit);

	if (minus) {
			addit = If.negativeValue(addit,v.getType());}
	
	tosay.append(addit);
	} //else {throw new RuntimeException("say value not found!");}
	else {tosay.append("|VALUE NOT FOUND:"+parts[k]+"|");}
      	}

	   catch(Exception ez)
		{
		if (StatementProcessor.DEBUG){ez.printStackTrace();}
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.SAY,"'"+parts[k]+"' not found!",state),vc,AT); return false;}

	looking_for_plus=true;
	}

    OUT.println(tosay.toString());
  return true;
  }


}

	

 

