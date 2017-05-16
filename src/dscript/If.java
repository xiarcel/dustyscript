package dscript;
import java.io.InputStream;

import java.util.Vector;

class If{
static boolean DEBUG=StatementProcessor.DEBUG;
static int ER=DSOut.ERR_OUT;
static int IF=DSOut.IF;
static long IFCOUNTER=0;

public final static String[] comparator = 
{"==","!=",">",">=","<","<=","contains","does_not_contain","is_type","refers_to","&","|","!is_type","!refers_to","is_a","!is_a","exists","!exists"};
/*
0 : ==
1 : !=
2 : >
3 : >=
4 : <
5 : <=
6 : contains (substrings)
7 : does_not_contain (substrings)
8 : is_type
9 : refers_to
10 : &
11 : | 
12 :!is_type
13 :!refers_to
14 :is_a
15 :!is_a
16 :exists
17 :!exists
*/

public static boolean evaluate(Statement s, VarContainer vc, ThingTypeContainer ttc, Output OUT, InputStream IN)
{
boolean AT = s.getAttempting();
/*do something with Output o*/

 String[] reps = s.getRefs();
 String[] pts = s.getParts();
 //for (int x=0;x<pts.length;x++) System.out.println("pts[x]=="+pts[x]);
 String[] chrs = s.getCharRefs();

/*clone to keep from affecting original statement*/


 String[] parts = new String[pts.length];
	for(int w=0; w<parts.length; w++)
		{parts[w] = pts[w];}
 

 if (parts.length < 5)
	{
	OUT.println(new DSOut(ER,IF,"This 'if' statement is too short",s),vc,AT); return false;}
 /*important:put back in string literals*/



 for (int i=0; i<parts.length; i++)
	{
	String mn =""; if (parts[i].startsWith("-")) {mn="-"; parts[i]=parts[i].substring(1,parts[i].length());}
	if (parts[i].startsWith("$")&&parts[i].endsWith("$"))
		{
		try{
		   int index = Integer.parseInt(parts[i].substring(1,(parts[i].length()-1)));
		   parts[i]=mn+reps[index]; continue;
		   }
		   catch(Exception e)
			{OUT.println(new DSOut(ER,IF,"There was an error using a string with this 'if'",s),vc,AT); 
			 return false;}
	       }
	if (parts[i].startsWith("&") && parts[i].endsWith("&") && (parts[i].length()>1))
		{
		try{
		   int index =Integer.parseInt(parts[i].substring(1,(parts[i].length()-1)));
		   parts[i]=mn+chrs[index]; continue;
		   }
		  catch(Exception e)
			{
			OUT.println(new DSOut(ER,IF,"There was an error using a char in this 'if'",s),vc,AT); 
			return false;}
		}
	 parts[i]= mn+parts[i];
	}

 parts = splitOperators(parts); //new
 
	
 if (parts[2].equals("is_type"))
	{
	return is_type(parts[1],vc,parts[3],ttc,OUT);
	}

 if (parts[2].equals("!is_type"))
	{
	return !is_type(parts[1],vc,parts[3],ttc,OUT);
	}

if (isOperator(parts[2]))
{
	parts[2]=thingCompareCheck(parts[1],parts[2],parts[3],vc);
}


 if (parts[2].equals("refers_to"))
	{
	return refers_to(parts[1],vc,parts[3],OUT,AT);
	}
 if (parts[2].equals("!refers_to"))
	{
	return !refers_to(parts[1],vc,parts[3],OUT,AT);
	}


 if (parts[2].equals("exists"))
	{
	return vc.getSameLevelThingTypeContainer().is_in(parts[1]);
	}
 if (parts[2].equals("!exists"))
	{
	return !vc.getSameLevelThingTypeContainer().is_in(parts[1]);
	}

 if (parts[2].equals("thing_equals"))
	{
	return thing_equals(parts[1],vc,parts[3],OUT,AT);
	}

 if (parts[2].equals("is_a"))
	{
	return isA(parts[1],vc,parts[3],OUT,AT);
	}

 if (parts[2].equals("!is_a"))
	{
	return !isA(parts[1],vc,parts[3],OUT,AT);
	}

 if ((!parts[0].equalsIgnoreCase("if"))&&(!parts[0].equalsIgnoreCase("as_long_as")))
	{
	OUT.println(new DSOut(ER,IF,"'if' needs to start with 'if' or 'as long as'",s),vc,AT);
	return false;}

 int which = which(parts[2]);
 if (which == -1)
	{
	OUT.println(new DSOut(ER,IF,"'"+parts[2]+"' is not an evaluator",s),vc,AT);
	return false;
	}
 if (which < 10)
 {parts[1]=replace_with_literal(parts[1],vc,s,OUT);
 parts[3]=replace_with_literal(parts[3],vc,s,OUT);
 }

 switch(which)
      {
      case 0: {return equal_to(parts[1],parts[3],vc);}
      case 1: {return !equal_to(parts[1],parts[3],vc);}
      case 2: {return greater(parts[1],parts[3],vc);}
      case 3: {boolean a = greater(parts[1],parts[3],vc);
		   boolean b = equal_to(parts[1],parts[3],vc);
		   if (a || b) {return true;}
		   return false;
		  }
      
	case 4: {boolean a = !greater(parts[1],parts[3],vc);
		   boolean b = !equal_to(parts[1],parts[3],vc);
		   if (a && b) {return true;}
		   return false;
		   }
	case 5: {return !greater(parts[1],parts[3],vc);}
      case 6: {return contains(parts[1],parts[3],vc);}
	case 7: {return !contains(parts[1],parts[3],vc);}

	default: break;
      }

return false;
} 


public static String negativeValue(String src, int type)
{
 switch(type)
	{
	case 0: 
		{
		if (src.equalsIgnoreCase("true")||src.equalsIgnoreCase("yes"))
			{return "no";}
		if (src.equalsIgnoreCase("false")||src.equalsIgnoreCase("no"))
			{return "yes";}
		break;
		}
	case 1: {
			if(StatementProcessor.DEBUG){System.err.println("c1:"+src);}
			if (src.startsWith("-")){return src.substring(1,src.length());}
			return "-"+src;
		  }
	case 2: { 
			if (src.startsWith("-")){return src.substring(1,src.length());}
			return "-"+src;
		   }

	case 3:
	case 4: {
		  String rev ="";
		  for (int i=(src.length()-1); i>-1; i--)
			{
			rev=rev+src.charAt(i);
			}
		  return rev;
		  }
	case 5:
	case 6:
	case 7: 
	case 8:
	case 9:
	case 10:
	case 11:
	default: {break;}
	}
return src;
}


private static String replace_with_literal(String part, VarContainer vc, Statement s, Output OUT)
{
boolean AT = s.getAttempting();
/*check to see if this is a variable name*/
Var v=null;
boolean minus =false;
//System.out.println("part=="+part);
	if (part.startsWith("-")){part=part.substring(1,part.length()); minus=true;}
if (StatementProcessor.DEBUG){OUT.println("part:"+part,2);}
if (vc.is_in(part))
	{
	v=vc.get(part);
	part = v.asString();
	if (v.getType() == Var.STR) {part = '\"'+ part + '\"';}
	if (v.getType() == Var.CHR) {part = '\'' + part + '\'';}
	if (v.getType() == Var.YES_NO) {part = part.toLowerCase();}
	if (v.getType() == Var.THING) {
		Thing t = v.getThing();
		if (t.getThingType().equals("Fraction"))
			{
			part=""+v.getDecimal();
			}
		}
	if (v.getAbsoluteType() == Var.EQUATE) 
		{
		Equation eq = v.getEquation();
		String dt=eq.getData();
		//added + and -
		if ((dt.indexOf("*")<0)&&(dt.indexOf("/")<0)&&(dt.indexOf("+")<0)&&(dt.indexOf("-")<0))
				{
					part = dt;
					if(part.startsWith("(")){part=part.substring(1,part.length());}
					if(part.endsWith(")")){part=part.substring(0,part.length()-1);}
				}
		else {part = ""+v.getDecimal();}
		}
		if (part.startsWith("-")){minus=true; part=part.substring(1,part.length());}
	if (minus)
		{
		part = negativeValue(part,v.getType());
		}
	return part;
	}
	

part.trim();
/*check if this is already a literal-changed*/
//System.out.println("part == "+part);
if (part.startsWith("-")) {part = part.substring(1,part.length()); minus = true;}

if ((part.startsWith("\""))&&(part.endsWith("\""))||(part.startsWith("\'") && part.endsWith("\'")))
{
/*String ss=part.substring(1,(part.length()-1));*/
if (minus) {return Var.reverseValue(part);}
return part;}


if (part.equalsIgnoreCase("yes")||part.equalsIgnoreCase("true")){if (minus) {return "no";}
												   return "yes";}

if (part.equalsIgnoreCase("no")||part.equalsIgnoreCase("false")){if (minus) {return "yes";}
											return "no";}

if (VarContainer.isNumberLiteral(part))
	{
	if (minus) {part = "-"+part;}
	return part;
	}

if (minus){part = "yes";} else {part="no";} 
boolean replacedpart=true;
if (replacedpart && StatementProcessor.DEBUG) {OUT.println("el -compare: "+part,2);}
if (StatementProcessor.USER_DEBUG && replacedpart)
	{
	OUT.println(new DSOut(ER,IF,"possible compare error\n\t->element not comparable?:(gave-value):"+part,s));
	}
return part;
}




private static boolean equal_to(String left, String right, VarContainer v)
{
if (StatementProcessor.DEBUG){System.err.println("l:"+left+",r:"+right);}
if (left.equals(right)){return true;}

try {long l = Long.parseLong(left);
	long r = Long.parseLong(right);
	return (l==r);
	}
	catch(Exception e){}

try{
    double dl = Double.parseDouble(left);
    double dr = Double.parseDouble(right);
    return (dl == dr);
   }
   catch(Exception ee){}

   
return false;
}


private static boolean greater(String left, String right, VarContainer v)
{
try{
   long l = Long.parseLong(left);
   long r = Long.parseLong(right);
   if (l > r) {return true;}
	else{return false;}
   }catch(Exception e){}

try{
   double dl = Double.parseDouble(left);
   double dr = Double.parseDouble(right);
   if (dl > dr){return true;}
	else{return false;}
   }catch(Exception ee){}

return (left.length() > right.length());

}

public static boolean contains(String left, String right, VarContainer v)
{
int i = left.indexOf(right);
if (i < 0){return false;}
return true;
}

public static int which(String s)
{
for (int i=0; i<comparator.length;i++)
	{
	if (s.equalsIgnoreCase(comparator[i])){return i;}
	}
return -1;
}

public static boolean is_type(String left, VarContainer vc, String right, ThingTypeContainer ttc, Output OUT)
{if (right.startsWith("\"") && right.endsWith("\""))
	{
	right=right.substring(1,right.length()-1);
	}


 if (!vc.is_in(left)){return false;}
 String tp="";
 try{
	tp = vc.get(left).getThing().getThingType();
	if (StatementProcessor.DEBUG){OUT.println("Comparing:"+tp+" to:"+right,2);}
	if (tp.equals(right)){return true;}
	}
	catch(Exception e)
		{return false;}

if ((!ttc.is_in(right) && !ttc.containsInterface(right)) || tp.equals("")){if(StatementProcessor.DEBUG){OUT.println("?!ttc.is_in() || tp.equals(\"\")?",2);}return false;}
//boolean b=true;
ThingType tt=ttc.get(tp);
 if (!tt.hasAncestor() && !tt.hasInterfaces())
 {if(StatementProcessor.DEBUG){OUT.println("!ancestor",2);}return false;}
 String[] ancestors = tt.getAllAncestors();
 for (int i=0; i<ancestors.length;i++)
 {
	 if (StatementProcessor.DEBUG){OUT.println("comparing:"+ancestors[i]+" to:"+right,2);}
	 if (ancestors[i].equals(right)) {return true;}
 }
	
 return false;
}

public static boolean refers_to(String left, VarContainer vc, String right, Output OUT,boolean AT)
{
 if (!vc.is_in(left) || !vc.is_in(right)){return false;}
 try{
    Thing lf = vc.get(left).getThing();
    Thing rt = vc.get(right).getThing();
    if (lf == rt){return true;}
    }
	catch(Exception e){}
 return false;
}

public static boolean thing_equals (String left, VarContainer vc, String right, Output OUT,boolean AT)
{
 if (!vc.is_in(left) || !vc.is_in(right))
	{return false;}
try{
	Thing lf=null;Thing rt=null;
 try{
 lf = vc.get(left).getThing();
 rt = vc.get(right).getThing();}
	catch(Exception ez)
		{
		OUT.println(new DSOut(ER,IF,"Trying to compare a thing\n...but one of the variables is not a 'thing'\n\teither '"+left+"' or '"+right+"'"),
				vc,AT); 
		return false;
		}
 if (lf == rt){return true;}
 VarContainer vl = lf.getVarContainer();
 VarContainer vr = rt.getVarContainer();
 if (vl==vr) {return true;}
 Var[] vlv = vl.in();
 Var[] vrv = vr.in();
 if (vlv.length != vrv.length) {return false;}
 for (int i=0; i<vlv.length; i++)
	{
	if (vlv[i].getType() != vrv[i].getType()) {return false;}
	if (!vlv[i].asString().equals(vrv[i].asString()))
		{return false;}
	}
 
 ActionContainer al = lf.getActionContainer();
 ActionContainer ar = rt.getActionContainer();
 Action[] vla = al.in();
 Action[] vra = ar.in();
 if (vla.length != vra.length){return false;}
 for (int i=0; i<vla.length;i++)
	{
	if (!vla[i].getName().equals(vra[i].getName())){return false;}
	if (vla[i].getReturnType() != vra[i].getReturnType()){return false;}
	int[] vlaa = vla[i].getArgs();
	int[] vraa = vra[i].getArgs();
	if (vlaa.length != vraa.length){return false;}
	for (int j=0; j<vlaa.length;j++)
		{
		if (vlaa[j] != vraa[j]){return false;}
		}
	}
  return true;
  }
	catch(Exception e){
	if (StatementProcessor.DEBUG){OUT.println("Thing left and Thing right failed deep comparison",2);}
				}
return false;
}


public static boolean isOperator(String s)
{
 for (int i=0; i<comparator.length; i++)
	{
	if (s.equals(comparator[i])){return true;}
	}
 return false;
}

public static String thingCompareCheck(String left, String op, String right, VarContainer vc)
{
if (!vc.is_in(left) || !vc.is_in(right) ){return op;}
int lt = vc.get(left).getType();
int rt = vc.get(right).getType();
if ((rt != Var.THING) || (lt != Var.THING))
	{return op;}

if (checkThingOp(left,op,right,vc)) {return op;}

if (is_type(left,vc,"Fraction",vc.getSameLevelThingTypeContainer(),vc.getOutput()) && is_type(right,vc,"Fraction",vc.getSameLevelThingTypeContainer(),vc.getOutput()))
	{
	return op;
	}

if (op.equals("==")){return "refers_to";}
if (op.equals("!=")){return "!refers_to";}
return op;
}

public static boolean checkThingOp(String left, String op, String right, VarContainer vc)
{
	String actname=actName(op);
	if (actname.equals("")){return false;}
	ActionHashIterator AHI=getActionHashIterator(actname,right,vc);
	ActionContainer AC=vc.get(left).getThing().getActionContainer();
	return AC.is_in(AHI);
}

public static String actName(String op)
{
	String actname="";
	if (op.equals("==")||op.equals("!=")) 
	{
		actname="isEqualTo";
	}
	else if (op.equals(">")) 
	{
		actname="isGreaterThan";
	}
	else if (op.equals("<"))
	{
		//this is not a mistake
		// > and equals can combine to make all permutations
		actname="isGreaterThan";
	}
return actname;
}

public static ActionHashIterator getActionHashIterator(String actname, String right, VarContainer vc)
{
	Var acted=vc.get(right);
	//ActionContainer search=actor.getThing().getActionContainer();
	ThingTypeContainer ttypecont=vc.getSameLevelThingTypeContainer();
	if (ttypecont == null) {return null;}
	int[] argy=new int[]{10};
	String[] argy_alts=new String[]{acted.getThing().getThingType()};
	int ret=Var.YES_NO;
	return ActionHashIterator.getActionHashIterator(actname, argy, argy_alts,ret,ttypecont);

}
	
	


public static void exec(String s, VarContainer vc, ActionContainer ac,ThingTypeContainer ttc, Output OUT, InputStream IN, StatementProcessor spawner)
{
	VarContainer outer=new VarContainer(OUT);
	outer.add(new Var(new VCWrapper(vc)));
StatementProcessor sp = new StatementProcessor(s,outer,ac,ttc,OUT,IN);
sp.suppress();

sp.setIsCount(spawner.IS_COUNT).run();
/*these allow Dustyscript to skip one level back in the nest*/
spawner.FAILED=sp.FAILED;
spawner.GAVE=sp.GAVE;
spawner.didBreak=sp.didBreak;
spawner.CONTINUED=sp.CONTINUED;
}

public static void checkForActions(Statement s, VarContainer vc, ActionContainer ac, ThingTypeContainer ttc, Output OUT, InputStream IN)
{
String[] parts = s.getParts();
int opindex=-1; int in_index=-1;
parts = adjustForMinus(parts);
boolean foundright=false;
boolean foundleft=false;
boolean wcheck=true;
boolean leftminus=false;
boolean rightminus=false;
boolean controlminus=false;
/*new check to make sure that since an Action in a Thing might exist, it is not replaced
if it is not a compound action accidentally*/

boolean is_really_act=true;
boolean tgminus=false;

String tgcopy = parts[1];
if (tgcopy.startsWith("-")) {tgminus=true; tgcopy = tgcopy.substring(1,tgcopy.length());}

try{
   if (ttc.is_in(parts[3]) && vc.is_in(tgcopy) && (vc.get(tgcopy).getType()== Var.THING) &&
	(parts[2].indexOf("is_type") > -1)) {is_really_act=false; parts[1]=tgcopy;
							 if(tgminus)
								{parts[2]="!is_type";}
							}
   }
   catch(Exception e) 
		{is_really_act=true;
		}
if (!is_really_act){return;}

/*proceed with other shit*/

	for (int pp=0;pp<parts.length;pp++)
		{
		String ppt = parts[pp];
		if (ppt.startsWith("-")) {ppt=ppt.substring(1,ppt.length());controlminus=true;}
				else {controlminus=false;}
		if (ppt.indexOf(".") > -1)
			{
			String thingy = ppt.substring(0,ppt.indexOf("."));
			String acty = ppt.substring((ppt.indexOf(".")+1),ppt.length());
			try{
			   ActionContainer nwact = vc.get(thingy).getThing().getActionContainer();
			   if (nwact.is_in(acty))
					{
					if (wcheck){foundleft = true; leftminus=controlminus;}
						else{foundright=true; rightminus=controlminus;}
						parts[pp]=ppt;
						continue;
					}
			    }catch(Exception e){}
			}
		if (ac.is_in(ppt)){
						if (wcheck){foundleft =true;
								leftminus=controlminus;}
						else{foundright=true;rightminus=controlminus;}
						parts[pp]=ppt;
						continue;
						}
		if (isOperator(parts[pp])){wcheck=false;}
		
		}
	if ((!foundleft)&&(!foundright)){return;}

for (int i=0; i<parts.length; i++)
	{
	if (isOperator(parts[i])){opindex=i;continue;}
	if (parts[i].indexOf("inline")>-1){in_index=i;}
	if ((opindex>-1)&&(in_index>-1)){break;}
	}

/*if this is a standard 'if'*/
if ((opindex==2)&&(in_index==4)&&!foundleft&&!foundright){return;}

if (opindex<0) 
	{
		if (in_index < 0)
			{return;}
	
	try{parts=rerouteParts(parts,in_index);}catch (Exception e){return;}
	opindex = in_index;
	in_index += 2; 
	}
	
		
	
String[] newstate= new String[((parts.length-in_index)+4)];
newstate[0]=parts[0];
newstate[2]=parts[opindex];
newstate[4]=parts[in_index];
if (newstate.length > 5)
{
for (int i=5; i<newstate.length; i++)
	{
	newstate[i] = parts[(in_index+(i-4))];
	}
}



if (StatementProcessor.DEBUG){for (int i=0; i<newstate.length;i++){OUT.println("ns["+i+"]=="+newstate[i],2);}}



boolean rep_left=false; boolean rep_right=false;

if (foundleft) {rep_left=true; newstate[1] = "%0"; if (leftminus) {newstate[1] = "-"+newstate[1];}
		    }
	else{newstate[1]=parts[1];}

if (foundright) {newstate[3]="%1";rep_right=true;
			if (rightminus) {newstate[3]="-"+newstate[3];}
			}
	else{newstate[3]=parts[opindex+1];}

s.resetParts(newstate);

if (rep_left)
	{
	Vector v=new Vector();
	for (int i=1; i<opindex; i++)
		{
		v.addElement(parts[i]);
		}
	if (v.size() == 1)
		{
		v.addElement("using"); v.addElement("~");
		}
	ActionSplitter.process(ac,vc,ttc,v,s,0,OUT,IN);
	}

if (rep_right)
	{
	Vector v=new Vector();
	for (int i=(opindex+1); i<in_index; i++)
		{
		v.addElement(parts[i]);
		}
	if (v.size() == 1)
		{
		v.addElement("using"); v.addElement("~");
		}
	ActionSplitter.process(ac,vc,ttc,v,s,1,OUT,IN);
	}

/*should be all*/
}


public static String[] adjustForMinus(String[] src)
{
 Vector v =new Vector();
 for (int i=0; i<src.length;i++)
	{
	src[i]=src[i].trim();
	if (src[i].equals("-")&&(i<(src.length-1)))
		{
		String to_add = src[i]+src[(i+1)];
		
		v.addElement(to_add);
		i++;
		continue;
		}
	v.addElement(src[i]);
	}
 String[] toret =new String[v.size()];
 for (int i=0; i<toret.length;i++)
	{
	toret[i]=(String)v.elementAt(i);
	//System.out.println("toret[i]=="+toret[i]);
	}
 return toret;
}

public static int operatorCount(String[] parts)
{
int opcount=0;
for (int i=0; i<parts.length;i++)
	{
	if (isOperator(parts[i])) {opcount++;}
	}

return opcount;
}

/*NEW STUFF, for 'special evaluation'*/

public static boolean evaluateSpecial(Statement s, VarContainer vc, ActionContainer ac,ThingTypeContainer ttc, Output OUT, InputStream IN)
{
/*STUB method...call from outside a true return will invoke a getParts() in statement processor*/
String[] parts = s.getParts();
/*this should never happen*/
if (parts.length < 2) {return false;}

/*count operators*/
int ops = operatorCount(parts);

   /*zero ops means no special eval*/
if (ops == 0) {return false;}


boolean is_complex = false;
if (ops > 1) {is_complex=true;}

/*only if 1 operator is is_complex false, but check now for special case*/
if (!is_complex)
	{
	for (int i=0; i<parts.length; i++)
		{
		is_complex = isIfPart(parts[i],vc);
		if (parts[i].equals("&") || parts[i].equals("|")) {is_complex=true;}
		if (is_complex) {break;}
		
		}
	}

/*if still not special case*/
if (!is_complex) {return false;}

/*
ok, we have a special case, so, create an IfVar :: 
the IfVar will take care of the guts of figuring it out
*/
/*first, get the IfVar guts*/
String ifvar_statement="";
int resume =0;
for (int i=1; i<parts.length; i++)
	{
	if (parts[i].startsWith("inline"))
		{/*we have the evaluation data*/
		 resume=i; /*we will need this stuff later*/
		 break;
		}
	ifvar_statement=ifvar_statement+parts[i]+" ";
	}
ifvar_statement.trim();


/*create the var-name*/
String evname = "__ifvar"+IFCOUNTER; IFCOUNTER++;

/*create the var*/
IfVar iv =new IfVar(ifvar_statement,s,vc,ac,ttc,OUT,IN);
Var nwvar = new Var(iv,evname);

/*add it to VarContainer*/
vc.add(nwvar);

/*set as transient*/
nwvar.setAsTransient();

/*reconstruct statement parts to reflect
pts[0] _ifvar_ == yes pts[next] <-> pts[N]
*/
Vector state_parts =new Vector();
state_parts.addElement(parts[0]);
state_parts.addElement(evname);
state_parts.addElement("==");
state_parts.addElement("yes");
for (int i=resume; i<parts.length;i++)
	{
	state_parts.addElement(parts[i]);
	}
String[] newstate =new String[state_parts.size()];
for (int i=0; i<newstate.length;i++)
	{
	newstate[i]=(String)state_parts.elementAt(i);
	}

/*put parts into statement*/
s.resetParts(newstate);

/* there was a change*/
return true;

}


public static boolean isIfPart(String part, VarContainer vc)
{
 if (part.startsWith("-")){part=part.substring(1,part.length());}
 if (!vc.is_in(part)){return false;}
 Var var = vc.get(part);
 if (var.getAbsoluteType() != Var.EQUATE) {return false;}

 String data = var.getEquation().getData();
 for (int i=0; i<comparator.length;i++)
	{
	if (data.indexOf(comparator[i]) > -1) {return true;}
	}
 return false;
}

public static boolean isA(String left, VarContainer vc, String right, Output OUT, boolean attempting)
{
 int isatype = Var.typeInt(right);
 if (isatype < 0) {return false;}
 if (vc.is_in(left))
	{
	return (vc.get(left).getType() == isatype);
	}
 switch(isatype)
	{
	case 0: {if (left.equalsIgnoreCase("yes")||left.equalsIgnoreCase("no")||
			 left.equalsIgnoreCase("no")||left.equalsIgnoreCase("false"))
				{return true;}
		   return false;
		  }
	case 1: {
		   if (VarContainer.isNumberLiteral(left) && !VarContainer.isDecimalLiteral(left))
			{return true;}
		   return false;
		   }
	case 2: {
		   if (VarContainer.isNumberLiteral(left) && VarContainer.isDecimalLiteral(left))
			{return true;}
		   return false;
		  }
	case 3: {
		  return VarContainer.isCharLiteral(left);
		   }
	case 4: {
		  return VarContainer.isStringLiteral(left);
		  }
	default: {break;}
	}
return false;
}


/*the &|compare stuff was here*/

/*OLD STUFF*/
public static boolean containsOperator(String part)
{
 for (int i=0; i<comparator.length;i++)
	{
	if (part.indexOf(comparator[i])>-1) {return true;}
	}
 return false;
	
}

/*NEWEST STUFF*/
public static String[] rerouteParts(String[] pts, int index)
{
String[] ret = new String[pts.length+2];
System.arraycopy(pts,0,ret,0,index);
ret[index] = "==";
ret[index+1] = "yes";
System.arraycopy(pts,index,ret,(index+2),(pts.length-index));
return ret;
}

public static String[] splitOperators(String[] pts)
{
 Vector v=new Vector();
 for (int i=0; i<pts.length;i++)
 {	if (isOperator(pts[i])) {v.addElement(pts[i]); continue;}
	if (containsOperator(pts[i]))
	{
	String[] splitty = splitOp(pts[i]);
	for (int j=0; j<splitty.length;j++) {v.addElement(splitty[j]);}
	continue;
	}
	v.addElement(pts[i]);
 }
 Object[] o = v.toArray();
 pts = new String[o.length];
 System.arraycopy(o,0,pts,0,o.length);
 return pts;
}



private static String[] splitOp(String part)
{
	StringBuffer opstring = new StringBuffer(4);
	StringBuffer arrstring = new StringBuffer(20);
	Vector v=new Vector();
	for (int i=0; i<part.length(); i++)
	{
		char c = part.charAt(i);
		if (isOpChar(c))
		{
			opstring.append(c);
			if (arrstring.length() > 0) 
			{
				v.addElement(arrstring.toString()); arrstring.setLength(0);
			}
			continue;
		}
		if (opstring.length() > 0) {v.addElement(opstring.toString()); opstring.setLength(0);}
		arrstring.append(c);
	}
	if (arrstring.length() > 0) {v.addElement(arrstring.toString()); arrstring.setLength(0);}
	Object[] o = v.toArray();
	String[] ret = new String[o.length];
	System.arraycopy(o,0,ret,0,o.length);
	return ret;
}

							



private static boolean isOpChar(char c)
{
	if ((c=='<') || (c=='=') || (c=='>') || (c=='&') || (c=='|') || (c=='!')) {return true;}
	return false;
}



}
