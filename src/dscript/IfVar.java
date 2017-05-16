package dscript;
import java.io.*;
import java.util.Vector;


public class IfVar{

private boolean empty=false;

private String data ="";
private VarContainer vc; private Statement state;
private ActionContainer ac;
private ThingTypeContainer ttc; 
private Output OUT; private InputStream IN;
static long IFVARCOUNTER=0;
private boolean ATTEMPT = false;

public IfVar(String dat, Statement s, VarContainer v, ActionContainer a,ThingTypeContainer t, Output O, InputStream I)
{
ATTEMPT=s.getAttempting();
empty=false;
OUT=O; IN=I; vc=v; state=s; ttc=t; ac=a;
data=dat;
}

public IfVar()
{
empty=true;
}

public String getData(){return data;}

public boolean evaluate()
{
if (empty) {return false;}

String[] parts = split(data);
Vector ors_only = new Vector();
Vector ands_only=new Vector();

boolean skip_til_or=false;

for (int i=0; i<parts.length;i++)
	{
	if (parts[i].equals("&")) 
	{
	    if (ands_only.size()>0 && !skip_til_or)  
		{
		String s = (String)ands_only.elementAt(ands_only.size()-1);
		if (s.equals("no"))
			{ors_only.addElement("no"); 
			skip_til_or=true;
			ands_only.removeAllElements();
			}
		}
	continue;
	}

	if (parts[i].equals("|") && (ands_only.size() > 0))
			{
			if (ands_only.size()>0)
				{
				 String dat = evaluateAnds(ands_only);
				 if (dat.equals("yes")) {return true;} 
				 ands_only.removeAllElements();
				 ors_only.addElement(dat);
				 }
			 skip_til_or=false;
			 continue;
			}

	if (skip_til_or) {continue;}

	if (!isLiteral(parts[i])){parts[i]=makeLiteral(parts[i]);}
	ands_only.addElement(parts[i]);
	}
/*one last check*/
if (ands_only.size() >0) 
	{ors_only.addElement(evaluateAnds(ands_only));
	 /*we had to add the last one, or it would be skipped*/
	}			


/*we have a vector containing BOOL | BOOL | BOOL | ...*/

/*final eval to determine true or false*/
boolean finality = false;
for (int i=0; i<ors_only.size(); i++)
	{
	String comp = (String)ors_only.elementAt(i);
	if (comp.equalsIgnoreCase("yes")) {finality=true;break;}
	/*we break because this is a logical short-circuited OR.  If we get one YES, we are true*/
	
	}	

return finality;
}

public String evaluateAnds(Vector ands)
{
 if (ands.size() == 0) {return "no";}

 boolean andbool =true;
/*true UNTIL we see a false*/

 for (int i=0; i<ands.size(); i++)
	{
	String comp = (String)ands.elementAt(i);
	if (comp.equalsIgnoreCase("no")) {andbool=false; break;}
	/*we break, because, with the logical AND short-circuit, one NO makes it all false*/

	}

 if (andbool) {return "yes";}
 return "no";
}


public String[] split(String stuff)
{

Vector v=new Vector();
String collector="";
for (int i=0; i<stuff.length(); i++)
	{
	char c = stuff.charAt(i);
	if (c == '-')
		{
		try{
		   char d = stuff.charAt(i+1);
		   if (d == ' ')
			{
			i++;
			}
			collector = collector + c;
			continue;
		   }
		   catch(Exception e){}
		}
	if (c == ' ') 
		{
		if (!collector.equals("")) 
			{v.addElement(collector); collector = "";}
		continue;
		}
	if (c == '&' || c == '|')
		{
		if (!collector.equals(""))
			{
			v.addElement(collector); collector="";
			}
		v.addElement(""+c);
		continue;
		}
	collector = collector + c;
	}
if (!collector.equals("")){v.addElement(collector);}

String[] to_ret=new String[v.size()];
for (int j=0; j<to_ret.length; j++)
	{to_ret[j]=(String)v.elementAt(j);	
	}
return to_ret;
}  

public boolean isLiteral(String s)
{
if (s.equalsIgnoreCase("yes")||s.equalsIgnoreCase("true")||s.equalsIgnoreCase("no")||s.equalsIgnoreCase("false"))
	{return true;}
return false;
}

public String makeLiteral(String s)
{

 
 VarContainer outtie =new VarContainer(OUT);
 outtie.add(new Var(new VCWrapper(vc)));
 

 String varname = "_tmp_"+IFVARCOUNTER; IFVARCOUNTER++;
 Var var = new Var(false,varname);
	
 if (s.startsWith("eq_"))
	{
	Var eqqy = vc.get(s);
	eqqy.getEquation().replaceAnyLiterals();
	eqqy.setUsedBit(false);
	}

 outtie.add(var);
 String statement="if "+s+" {"+varname+" is now yes;};";
 
 StatementProcessor sp =new StatementProcessor(statement,outtie,ac,ttc,OUT,IN);
 sp.suppress();
 sp.setAttempting(ATTEMPT);
 sp.run();
 boolean b = outtie.get(varname).getYes_No();

 outtie.remove(varname);

 if (b) {return "yes";}
 return "no";

}

}
