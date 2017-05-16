package dscript;

import java.util.Vector;
import java.io.InputStream;

class Count{
private static final boolean DEBUG = false;

/*syntax

count using <varname> from <literal> through <literal>{
*/
 final static int CT = DSOut.CNT;
 final static int ER = DSOut.ERR_OUT;
 final static int STD = DSOut.STD_OUT;


public static void count(Statement state, VarContainer global, ActionContainer ac_global, ThingTypeContainer ttc, Output OUT, InputStream IN, boolean asserting, boolean direction,StatementProcessor SP) throws Exception
{
boolean AT = state.getAttempting();
int start=-1; int end=-1;
String[] parts = state.getParts();
boolean using_var=false;
if ((parts.length != 8)&(parts.length != 6))
	{
	parts = stripPlusses(parts);
	}

if (parts.length < 6)
	{OUT.println(new DSOut(ER,CT,"This statement is too short for a count",state),
			global,AT);throw new Exception();
	}


if (parts[1].equalsIgnoreCase("using"))
	{
	using_var=true;
	
	if (parts.length != 8)
		{OUT.println(new DSOut(ER,CT,"This statement is too short for a count",state),
				global,AT);
		if (DEBUG){for (int q=0;q<parts.length;q++){OUT.println("parts["+q+"]=="+parts[q],2);}}
		throw new Exception();
		}
	
	}

int fr=-1;int _to=-1;

if (using_var)
	{
	fr = 4; _to=6;
	}
	else
	    {
	    fr = 2; _to=4;
	    }

if ((parts[(fr-1)].equalsIgnoreCase("from")==false)|(parts[(_to-1)].equalsIgnoreCase("through")==false))
		{
		OUT.println(new DSOut(ER,CT,"This 'count' is missing a 'from' or a 'through'",state),
			global,AT);
		throw new Exception();
		}
try{
   start=Integer.parseInt(parts[fr]);
   }
	catch(Exception e)
       {
	try{
	   start = Integer.parseInt(global.get(parts[fr]).asString());
	   }
	   catch(Exception eee)
		{
		OUT.println(new DSOut(ER,CT,"The variable '"+parts[fr]+"' is not defined, or not an integer",state),
				global,AT);
		throw new Exception();				}

	  }
 try{
   end = Integer.parseInt(parts[_to]);
   }
   catch(Exception ee)
       {
	try{
	   end = Integer.parseInt(global.get(parts[_to]).asString());
	   }
	   catch(Exception eee)
		{
		OUT.println(new DSOut(ER,CT,"The variable '"+parts[_to]+"' is not defined",state),
				global,AT);
		throw new Exception();	
		}

	  }
VarContainer local=new VarContainer(OUT);
local.add(new Var(new VCWrapper(global)));
String[] inners = state.getInline();
String pc="";
String ref="";
if (using_var){ref=parts[7];}
else{ref=parts[5];}

for (int w=0; w<ref.length(); w++)
	{
	if (!Character.isDigit(ref.charAt(w)))
	{continue;}
	pc=pc+ref.charAt(w);
	}
int index_for_replace;
try{index_for_replace=Integer.parseInt(pc);}
	catch(Exception ez)
	{index_for_replace=-1;}
String execbody="";
if (index_for_replace >= 0){execbody=inners[index_for_replace];}
StatementProcessor sp =new StatementProcessor(execbody,local,ac_global,ttc,OUT,IN);
sp.suppress(); sp.setNextUp(SP);
 
long uvl = (long)start;
Var uv = new Var(uvl,parts[2]);

if (asserting && ((direction && (end < start)) || (!direction && (end > start))))
	{
	if (StatementProcessor.USER_DEBUG)
		{
		OUT.println(new DSOut(STD,CT,"We ignored this count because:\nEither we wanted to count up and this would count down\n-or-\nWe wanted to count down and this would count up\n",true));
		}
	/*not an error*/
	return;
	} 
if (start <= end)
{
for (int z = start; z <=end; z++)
    { if (SP.nextUpStop()){return;}
	   local.clearTopLevel();
	/*populate local variable table with global variables*/
	
	if (using_var)
		{
		
		local.add(uv);
		}
	//old way	
	/*
	StatementProcessor sp=null;
	if (index_for_replace != -1)
	{
	sp=new StatementProcessor(inners[index_for_replace],local,ac_global,ttc,OUT,IN);
	sp.setNextUp(SP);
	}
	else {sp =new StatementProcessor ("",local,ac_global,ttc,OUT,IN);
		sp.setNextUp(SP);}	
    	sp.suppress();
	*/
	sp.setIsCount(true);
	sp.run();
	
	SP.GAVE = sp.GAVE;
	SP.FAILED=sp.FAILED;
	if (sp.GAVE || sp.FAILED || sp.didBreak) {return;}

    if (using_var)
	{
	//changed

	long in = uv.getInteger();
	uv.setValue(++in);

	/*check to see if this increment combined with inner stuff
	has counted it past*/


	/*if (in > end ){break;}*/

	}
	/*refresh Vars in global*/

    }
  }
else{

   for (int z = start; z >=end; z--)
    {if (SP.nextUpStop()){return;}

	 local.clearTopLevel();
	/*populate local variable table with global variables*/
	
	if (using_var)
		{
		
		local.add(uv);
		}
	//old way
	/*
	StatementProcessor sp=new StatementProcessor(inners[index_for_replace],local,ac_global,ttc,OUT,IN);
	sp.setNextUp(SP);	
    	sp.suppress();
	*/
	sp.setIsCount(true);
	sp.run();
	SP.GAVE = sp.GAVE;
	SP.FAILED=sp.FAILED;
	if (sp.GAVE || sp.FAILED || sp.didBreak) {return;}
    if (using_var)
	{
	long in = uv.getInteger();
	uv.setValue(--in);
	
	/*check to see if this increment combined with inner stuff
	has counted it past*/

	/*if (in < end){break;}*/
	}
	/*refresh Vars in global*/


    }
   }


}
private static String[] stripPlusses(String[] states)
{
 Vector v =new Vector();
 for (int i=0; i<states.length; i++)
	{
	if (states[i].equals("+")){continue;}
	if (states[i].equals("-"))
		try{
		   states[(i+1)]=new String("-"+states[(i+1)]);
		   continue;
		   }
		   catch(Exception e){}
	v.addElement(states[i]);
	}
 String[] to_ret = new String[v.size()];
 for (int j=0; j<to_ret.length; j++)
	{to_ret[j]=(String)v.elementAt(j);}
 return to_ret;
}


/*end*/
}






