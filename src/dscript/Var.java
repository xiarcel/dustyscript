package dscript;
import dscript.connect.JavaConnector;



/*This is the wrapper class for all primitives, as well as for
the user-defined types*/


public class Var{


/*these two String[]s used to have the 5th element as 'fraction' .. this caused trouble
  with the "Thing Type" 'Fraction' in the math library
  I feel that language references to 'fraction' could be removed safely now
  */
  
  private boolean mutability=true;
  
public static final String[] PLURALS = {"yes_nos","integers","decimals","characters","strings","","complexes","groups","equations","","things","javaconnectors","voids","","ifvars","threads","anyvars"};
public static final String[] VARTYPES = {"yes_no","integer","decimal","character","string","","complex","group","equation","","thing","javaconnector","void","","ifvar","thread","anyvar"};
public static final String GP = "7"; public final static String AV = "16";
public static final String GPA = "anyvars";
public static final String[][] VAR_ALTS = {{"0","16"},{"1","16","2"},{"2","16","1"},{"3","16"},{"4","16","3","2","1"},{},{},{"7","16"},{},{},{"10","16"},{"11","16"},{"12","16"},{},{},{"15","16"},
				{"16","0","1","2","3","4","7","10","11","15"}};
				//last entry above consider swapping "0" and "1" (ie, integer before yes_no)
public static final String[][] GRP_ALTS = {{"yes_nos",GPA,GP,AV}, {"integers",GPA,GP,AV},{"decimals",GPA,GP,AV},{"characters",GPA,GP,AV},{"strings",GPA,GP,AV},{},{},{"groups",GPA,GP,AV},{},{},{"things",GPA,GP,AV},
{"javaconnectors",GPA,GP,AV}, {"voids",GPA,GP,AV}, {},{},{"threads",GPA,GP,AV},{"anyvars",GP,AV}};
 
public static final String[] NO_ALTS = {"-1"};
private String declared_thing_type="";

public static Thing nobody = null;
/*one String[] for re-use in profiling*/
private static String[] prof_array = new String[]{"",""};

/*place-holders for all potential var-types
static ints 0-5 represent the 6 'primitives' (5 was fractions..now gone)
static int 6 represented "Complex" (such as MultDiv)--gone
static int 7 represents "Groups"
static int 8 represents "Equations"
static int 9 represents the VarContainer wrapper (VCWrapper) for nested variable tables
*/

/*new change...planning to add in Array support, static int 7 is for GROUPs*/
public final static int NUL =-1;
public final static int VOD =12;

/*for TRANSIENT*/
private boolean used_bit=false;

private boolean bool; public final static int YES_NO=0;
private long lng; public final static int INT=1;
private double doub; public final static int DEC=2;
private char chr; public final static int CHR=3;
private String str=""; public final static int STR=4;
 /*no implementation-placeholder*/public final static int FRACT = 5;
 /*no implementation-placeholder*/ public final static int COMPLEX=6;
private VarGroup grp; public static final int GROUP = 7;
private Equation eq; public static final int EQUATE=8;
private VCWrapper vcw; public static final int VC=9;


private Thing thg; public static final int THING=10;
private JavaConnector jcon; public static final int JAVA=11;
private IfVar iffy; public static final int IFVAR = 14; /*13 is unlucky :-) */

private DThread thread; public static final int THREAD = 15;
private Anyvar any; public static final int ANYVAR = 16;

private int declength;
private boolean transvar=false;
private int absolute_type;
private boolean is_global;
private Var aliasvar=null;
private boolean aliasing=false;
public boolean USER_SET=true;
/*declarations for other variables*/

/*trying to see if this will work correctly with the rns*/

private String string_val; 
private int type;
private String name;

/*constructors*/


public Var(String n, int typ)
{
absolute_type=typ;
/*this constructor is for declared Variables with no value*/
if ((typ<0) || (typ>11 && ((typ <14)||(typ > 16))) )
	{
	type = NUL;
	}
	else{type=typ;}
name=n;
switch(type)
	{
	/*all these cases we might wish not to do-default values*/
	case YES_NO: {setValue(false);break;}
	case INT: {setValue(0L); break;}
	case DEC: {setValue(0.0); break;}
	case CHR: {setValue('\0'); break;}
	case STR: {setValue(""); break;}
	case THING: {setValue(Thing.UNDEF); break;}
	
	/*these need to stay*/
	case NUL: {string_val = ">NO_VALUE_GIVEN<";break;} 
	case GROUP: {
		grp = new VarGroup(name,NUL,null);//hopefully this doesn't break it
		string_val = grp.asString();
		break;
		}
	case IFVAR: {
		iffy = new IfVar();
		boolean bb = iffy.evaluate();
		if (bb) {string_val="YES";}
		else{string_val="NO";}
		break;
		}
	//case JAVA: {setValue(new JavaConnector(name,null,null,null,null,null));
	//	break;
	//}
	case THREAD: {
		thread = new DThread();
		string_val="Thread:"+n;
		break;
		}
	case ANYVAR: 
		{
		any = new Anyvar(new Var(false,name));
		string_val="NO";
		break;
		}	
	default: {string_val = ">NOVALUE<";break;}
	}
}


public Var(boolean b, String n)
{
name=n;
bool=b;
if (b==true){string_val="YES";}
else{string_val="NO";}
type = YES_NO;

}

public Var(long l, String n)
{
name=n;
lng=l;
string_val=""+l;
type = INT;
}

public Var(double d, String n)
{
name=n;
doub=d;
string_val=""+d;
String sub = new String(string_val.substring(string_val.indexOf("."),string_val.length()));
declength=sub.length();
type=DEC;
}

public Var(char c, String n)
{
name=n;
chr=c;
string_val=""+c;
type=CHR;
}

public Var(String s, String n)
{
name=n;
str=s;
string_val=s;
type = STR;
}


public Var(VarGroup vg, String n)
{
name=n;
grp = vg;
string_val=vg.asString();
type = GROUP;
}

public Var(Equation e, String n)
{
name=n;
eq=e;
string_val="equation";
type = EQUATE;
}

public Var(VCWrapper v)
{
vcw = v; 
name = v.getName();
string_val=v.asString();
type=VC;
}

public Var(Thing t, String nm)
{
thg=t;
name=nm;
string_val=t.getThingType()+"::"+t.getName();
type=THING;
}

public Var(JavaConnector j, String nm)
{
name =nm; jcon=j; 
//System.out.println("javaconnector j for var "+nm);
try{
   string_val= jcon.getDustyable().getClass().getName()+"::"+nm;
    }
	catch(Exception e){string_val= "javaconnector:"+nm;}
type=JAVA;
}


public Var(IfVar i, String nm)
{
name=nm; iffy=i;
string_val="";
type=IFVAR;
}

public Var(DThread t, String nm)
{
name = nm; thread= t;
string_val="Thread:"+name;
type=THREAD;
}

public Var(Anyvar av, String nm)
{
name = nm; any = av;
string_val = av.getVar().asString();
type=ANYVAR;
absolute_type=ANYVAR;
}

public String getDeclaredThingType()
{
	return declared_thing_type;
}

public Var setDeclaredThingType(String s)
{
	if (type == 10)
	{
	declared_thing_type=s;
	setValue(new Thing(declared_thing_type,">>NULL<<",null,null));
	}
	return this;
}

/*this Var will return the name (label) given to it by the programmer*/

public String getName()
{
if (aliasing){return aliasvar.getName();}
return name;}

/*this Var will return a String rep of itself*/

public String asString()
{
if (aliasing){return aliasvar.asString();}

switch (type)
	{
	case GROUP: {return grp.asString();}
	case EQUATE: {return ""+eq.asDecimal();}
	case IFVAR: {
			boolean bb= iffy.evaluate();
			if (bb){return "YES";}
			return "NO";
			}
	case THING: {
		return thg.getToString();
	}
	case ANYVAR:{
		try {return any.getVar().asString();}
		catch(Exception e){return "";}
			}
	default: {break;}
	}
return string_val;
}


/*the program will need to check type before it calls for a particular
variable...  So that if this Var is registered as a char, it does not
have a null something-else returned*/

public int getType()
{
if (aliasing) {return aliasvar.getType();}
/*substitution for how var is treated as necessary*/
switch (type)
{
case EQUATE: {return DEC;}
case IFVAR: {return YES_NO;}
case ANYVAR: {try{return any.getVar().getType();}catch (Exception e){return ANYVAR;}}
default: {break;}
}
return type;
}

public void setMutability(boolean b)
{
	mutability=b;
}

public boolean isMutable()
{
	return mutability;
}

public int getAbsoluteType() {
if (aliasing) {return aliasvar.getAbsoluteType();}
if (absolute_type == 16){return 16;}
return type;}

public int getDecLength(){
if (aliasing) {return aliasvar.getDecLength();}
return declength;}

/*what follows are the getter methods*/

public boolean getYes_No()
{
if (aliasing){return aliasvar.getYes_No();}
if (type==ANYVAR) {return any.getVar().getYes_No();}

if (type == IFVAR) {return iffy.evaluate();}
return bool;}

public long getInteger()
{
if (aliasing) {return aliasvar.getInteger();}
if (type==EQUATE) {return (long)eq.asDecimal();}
if (type==ANYVAR)
	{
	return any.getVar().getInteger();
	}
if (type == THING)
{
	return thg.getToInteger();
}

return lng;
}

public double getDecimal()
	{
	if (aliasing){return aliasvar.getDecimal();}
	if(type==EQUATE){return eq.asDecimal();}
	if(type==ANYVAR)
		{return any.getVar().getDecimal();}
	if (type == THING)
	{
	//System.out.println("thg.getToDecimal() called");	
	return thg.getToDecimal();
	}
	return doub;
	}

public char getChar()
{
if (aliasing) {return aliasvar.getChar();}
if (type == ANYVAR)
	{
	return any.getVar().getChar();
	}
return chr;}

public String getString()
{
if (aliasing) {return aliasvar.getString();}
if(type == ANYVAR) {return any.getVar().getString();}
if(type==VC){return vcw.asString();}
return str;}


public VarGroup getVarGroup()
{
if (aliasing) {return aliasvar.getVarGroup();}

if(type == ANYVAR){return any.getVar().getVarGroup();}
return grp;
}
public Equation getEquation()
{
if (aliasing) {return aliasvar.getEquation();}

if(type == ANYVAR){return any.getVar().getEquation();}
return eq;
}

public VCWrapper getVCWrapper()
{
if (aliasing) {return aliasvar.getVCWrapper();}

if (type == ANYVAR){return any.getVar().getVCWrapper();}
return vcw;
}

public Thing getThing()
{
if (aliasing) {return aliasvar.getThing();}

if (type== ANYVAR){return any.getVar().getThing();}
return thg;
}

public JavaConnector getJavaConnector()
{
if (aliasing) {return aliasvar.getJavaConnector();}
if (type == ANYVAR){return any.getVar().getJavaConnector();}
return jcon;
}

public IfVar getIfVar()
{
if (aliasing) {return aliasvar.getIfVar();}
if (type == ANYVAR){return any.getVar().getIfVar();}
return iffy;
}

public DThread getThread()
{
if (aliasing) {return aliasvar.getThread();}
if (type == ANYVAR){return any.getVar().getThread();}
return thread;
}

public Anyvar getAnyvar()
{
if (aliasing) {return aliasvar.getAnyvar();}
return any;
}


/*the following alter the values... as they stand now,one could simply 
switch to another var-type by assigning this var that type.  Haven't decided
if this is good or bad...  Also, this would then have you taking the 
values, computing the changes, and then setting the value in this 
Var.  Not having "add" or "subtract" methods and/or string methods, etc..
is possibly cleaner...this is a 'data-type'..

A final use would be for when a Var(String,int) constructor was used..
because a Variable with a name but no value was created.  The value can
be set with those methods

*/

public void ensureNameIs(String n)
{
if (n.length()==0){return;}
if (aliasing){aliasvar.ensureNameIs(n);}
name=n;
}

public void setType(int i)
{
	if (!isMutable()) {return;}
	type=i;
	aliasing=false;
}

public void setValue(boolean b)
{
	if (!isMutable()) {return;}
	
if (aliasing){aliasvar.setValue(b);}
if (type == ANYVAR) {any.getVar().setValue(b); return;}
bool=b; 
if (b==true){string_val="YES";}
else{string_val="NO";}

 type=YES_NO;}

public void setValue(long l)
{
	if (!isMutable()) {return;}
if (aliasing){aliasvar.setValue(l);}
if (absolute_type == ANYVAR) {any.getVar().setValue(l); return;}
lng=l; string_val=""+l; type=INT;
}

public void setValue(double d)
{
	if (!isMutable()) {return;}
if (aliasing){aliasvar.setValue(d);}
if (absolute_type == ANYVAR) {any.getVar().setValue(d); return;}
doub=d; string_val=""+d;
String sub = new String(string_val.substring(string_val.indexOf("."),string_val.length()));
declength=sub.length();
type = DEC;
}

public void setValue(char c)
{
	if (!isMutable()) {return;}
if (aliasing) {aliasvar.setValue(c);}
if (absolute_type == ANYVAR) {any.getVar().setValue(c); return;}
chr=c; string_val = ""+c; type = CHR;}

public void setValue(String s)
{
	if (!isMutable()) {return;}
if (aliasing) {aliasvar.setValue(s);}
if (absolute_type == ANYVAR) {
any.getVar().setValue(s);return;}
str=s; string_val=s; type=STR;}



public void setValue(VarGroup v)
{
	if (!isMutable()) {return;}
 if (aliasing) {aliasvar.setValue(v);}
 if (absolute_type==ANYVAR) {any.getVar().setValue(v); return;}
 grp = v; string_val=v.asString(); type=GROUP;
}

public void setValue(VCWrapper vwrap)
{
	if (!isMutable()) {return;}
 if(aliasing){aliasvar.setValue(vwrap);}
 if (absolute_type == ANYVAR) {any.getVar().setValue(vwrap); return;}
 vcw= vwrap; string_val=new String(vcw.getName()); type = VC;
}

public void setValue(Thing t)
{
	if (!isMutable()) {return;}
	//System.out.println("Setting thing\nt=="+t.getThingType()+"\nmy name:"+name);
 if (aliasing)
 {
	 aliasvar.setValue(t);
	// System.out.println("aliasing");
 }
 if (absolute_type == ANYVAR) {any.getVar().setValue(t); return;}
 thg=t; string_val=thg.getThingType()+"::"+name; type =THING;
}

public void setValue (JavaConnector j)
{
	if (!isMutable()) {return;}
if (aliasing) {aliasvar.setValue(j);}
if (absolute_type == ANYVAR) {any.getVar().setValue(j); return;}
jcon=j; 
if (j != null )
{
	string_val=j.getDustyable().getClass().getName()+":"+name;
}
else{
	
string_val="javaconnector:"+name;
}
type=JAVA;
}

public void setValue (IfVar i)
{
	if (!isMutable()) {return;}
if (aliasing){aliasvar.setValue(i);}
if (absolute_type == ANYVAR) {any.getVar().setValue(i); return;}
iffy=i;
string_val="";
type = IFVAR;
}

public void setValue (DThread t)
{
	if (!isMutable()) {return;}
if (aliasing){aliasvar.setValue(t);}
if (absolute_type == ANYVAR) {any.getVar().setValue(t); return;}
thread=t;
string_val="Thread:"+name;
type = THREAD;
thread.setVarReference(this);
}

public void setValue (Anyvar av)
{
	if (!isMutable()) {return;}
 if (aliasing) {aliasvar.setValue(av);}
 any = av; 
}

public void setValue (Var v)
{
	if (!isMutable()) {return;}
	int T=v.getType();
	boolean def=false;

	switch(T)
	{
		case YES_NO: {setValue(v.getYes_No());break;}
		case INT: {setValue(v.getInteger());break;}
		case DEC: {setValue(v.getDecimal());break;}
		case CHR: {setValue(v.getChar());break;}
		case STR: {
		//System.out.println("Setting string value");
		setValue(v.getString()); break;}
		case 5:
		case 6:
		case GROUP: {setValue(v.getVarGroup()); break;}
		case 8:
		case 9:
		case THING: {setValue(v.getThing());break;}
		case JAVA: {setValue(v.getJavaConnector());break;}
		case THREAD: {setValue(v.getThread());break;}
		case ANYVAR: {setValue(v.getAnyvar());break;}
		default: {def=true; break;}
	}
if (def){aliasvar=v; aliasing=true;}	
	
/*anyvars-aliasvars cannot just use the original in case of primitives*/
//OLD WAY:
//if (v.getType()<=4) {v= Var.copy(v);/*probably does not need explicit Var. there*/}
//aliasvar=v;
//aliasing=true;
}

public static String reverseValue(String s)
{
	StringBuffer sb = new StringBuffer(s.length());
	for (int i= (s.length()-1); i> -1; i--)
	{
	sb.append(s.charAt(i));
	}
	return sb.toString();
}

public Var reverseValue()
{
 if (aliasing) {aliasvar.reverseValue();return this;}

 switch(type)
	{
	case 0: {setValue(!bool); break;}
	case 1: {setValue(-lng); break;}
	case 2: {setValue(-doub); break;}
	case 3: {break;}
	case 4: {
		  String cpy = "";
		  for (int i=str.length()-1; i>-1; i--)
			{
			cpy=cpy+str.charAt(i);
			}
		  setValue(cpy);
		  break;
		  }
	case 7:
		{
		setValue(VarGroup.createReverse(grp,grp.getName()));
		break;
		}
	case ANYVAR:
		{
		any.getVar().reverseValue();
		break;
		}
	default: {break;}
	}
return this;
}			

/*static methods*/



public void setAsTransient()
{

if (aliasing) {aliasvar.setAsTransient();}
transvar=true;used_bit=false;
}

public void setAsGlobal()
{
if (aliasing) {aliasvar.setAsGlobal();}
is_global=true;
}

public boolean isGlobal()
{
if (aliasing) {return aliasvar.isGlobal();}
return is_global;
}


public void setUsedBit(boolean b)
{
if (aliasing) {aliasvar.setUsedBit(b);}
used_bit = b;
}
public boolean usedBit() 
	{
	if (aliasing) {return aliasvar.usedBit();}
	return used_bit;
	}


public boolean isTransient()
{
if (aliasing) {return aliasvar.isTransient();}
return transvar;}

public String getHashValue()
{	
	switch(type)
	{
		case 0: case 1: case 2: case 3: case 4: {return asString();}
		case 7: {return grp.getHashValue();}
		case 10: {return thg.getHashValue();}
		case 11: {return jcon.getHashValue();}
		case 15: {return thread.getHashValue();}
		case 16: {
			if (aliasing) {return aliasvar.getHashValue();}
			return any.getVar().getHashValue();
		}
		default: break;
	}
	return asString();
}


/*STATIC section*/

public static Var copy (Var v, String newname)
{return copy(v,newname,null);}


public static Var copy(Var v, String newname,VarContainer vc)
{
if (StatementProcessor.DEBUG)
{
		System.err.println("newname:"+newname+" for copy");
}
int t= v.getType();
if (t != v.getAbsoluteType()){t=v.getAbsoluteType();}
switch(t)
	{
	case 0: {return new Var(v.getYes_No(),newname);}
	case 1: {return new Var(v.getInteger(),newname);}
	case 2: {return new Var(v.getDecimal(),newname);}
	case 3: {return new Var(v.getChar(),newname);}
	case 4: {return new Var(v.getString(),newname);}
	case 5: {return new Var(newname,5);}
	case 6: {return new Var(newname,6);}
	case 7: {
		//if (vc !=null) {return new Var(VarGroup.copy(v.getVarGroup(),newname,vc),newname);}
		return new Var(VarGroup.copy(v.getVarGroup(),newname),newname);}
	case 8: {return new Var(v.getEquation(),newname);}
	case 9: {return new Var(VCWrapper.copy(v.getVCWrapper()));}
	case 10: {return new Var(v.getThing(),newname);}
	case 11: {return new Var(v.getJavaConnector(),newname);}
	case 15: {return new Var(v.getThread(),newname);}
	case 16: {return new Var(new Anyvar(copy(v.getAnyvar().getVar())),newname);}
	default : {break;}

	}

return new Var(">NULL<",-1);
}

public static Var copy(Var v, VarContainer vc)
{
return copy(v,v.getName(),vc);
}

public static Var copy(Var v)
{
 return copy(v,v.getName());
}

public static String typeString(int i)
	{
	if (i == -1) {return "null";}
	try {return VARTYPES[i];}
		catch(Exception e){}
	return "undefined type";
	}

public static int typePlurals(String s)
	{
	for (int i=0; i< PLURALS.length; i++)
		{
		if (s.equalsIgnoreCase(PLURALS[i]))
			{
			return i;
			}
		}
	return -1;
	}
public static String typePlurals(int i)
	{
	if (i == -1) {return "no defined type";}
	try {return PLURALS[i];}
		catch(Exception e){}
	return "undefined type";
	}

public static int typeInt(String s)
	{
	if (s.equals("")){return -1;}
	for (int i=0; i<VARTYPES.length;i++)
		{
		if (s.equalsIgnoreCase(VARTYPES[i])) // || s.equalsIgnoreCase(PLURALS[i]) )
			{return i;}
		}
	return -1;
	}

public static Var declare(String type, String name, Output OUT,VarContainer VC)
{
 int tp = -1;
 String tgtype="";
 
 ThingTypeContainer TTC=VC.getSameLevelThingTypeContainer();
 String alty="";
 if (type.endsWith("s"))
 {
	 alty=type.substring(0,type.length()-1);
 }
 if (type.endsWith("s") && (TTC.is_in(alty)||TTC.containsInterface(alty)||TTC.allowed(alty)))
 {
	 tgtype=alty;
	 type="things";
 }
 else if (TTC.is_in(type) || TTC.containsInterface(type) || TTC.allowed(type))
 {
	 tgtype=type;
	 type="thing";
 }
 
 int grouptype=typePlurals(type);
 
 if (grouptype > -1) {tp = GROUP;}
	else
		{
		tp = typeInt(type);
		}
//System.err.println("type:"+type+",tp:"+tp);
 
 Var dec = new Var(name,tp);
 switch(tp)
	{
	case 10: {Thing t = new Thing(type,name,new ActionContainer(OUT), new VarContainer(OUT));
		    dec.setValue(t);
		    dec.setDeclaredThingType(tgtype);
		    return dec;
		   }
	case 7:  {
		    VarGroup vg = new VarGroup(name,grouptype,VC);
		    dec.setDeclaredThingType(tgtype);
		    vg.setDeclaredThingType(tgtype);
		    dec.setValue(vg);
		    return dec;
		   }
	default: {break;}
	}
return dec;
}	


public static String profile (String[] pts, VarContainer vc, ThingTypeContainer ttc)
	{
	return profile(pts,vc,ttc,"");
	}

public static String profile(String[] pts, VarContainer vc, ThingTypeContainer ttc, String outer)
	{
	   Var prof = vc.get(pts[1]);
	   //System.err.println(prof.getName()+"::profiled name");
	   int vartype = prof.getType();
	   String instring="";
	   if (!outer.equals("")){instring = " in '"+outer+"'";}
	   StringBuffer message = new StringBuffer(100).append("\n===================\nProfiling var '").append(pts[1]).append("'").append(instring).append("\n====================");
	   message.append("\nType:").append(typeString(vartype));
	   message.append("\nValue(inside of '()'):(").append(prof.asString()).append(")");
	   message.append("\nis Transient?:").append(prof.isTransient());
	   message.append("\nis Global:?:").append(prof.isGlobal());
	   if (vartype == Var.GROUP)
		{
		VarGroup invargrp = prof.getVarGroup();
		message.append("\nit contains ").append(Var.typePlurals(invargrp.getType()));
		message.append("\nit contains ").append(invargrp.size()).append(" elements");
		}
	   else if (vartype == Var.THING)
		{
		Thing inthing=prof.getThing();
		ActionContainer inacts=inthing.getActionContainer();
		VarContainer intvc=inthing.getVarContainer();
		
		if (pts[1].equals("me"))
			{
			message.append("\nThis var is a self reference");
			}
		else if ((inacts==null) || (intvc==null))
		{
		message.append("\nThis var is of type:"+inthing.getThingType()+" and is unassigned");
		}
		else
		{

		Action[] inactions=inacts.in();
	
		message.append("\nit is Thing Type: ").append(inthing.getThingType());
		message.append("\nit descends from: ");
		ThingType this_thing = ttc.get(inthing.getThingType());
		ThingType backup = this_thing;
		if (this_thing.hasAncestor())
			{
			ThingType anc = this_thing.getAncestor();
			while (anc != this_thing)
				{
				message.append(anc.getName()).append("\n\twhich descends from: ");
				ThingType holder = anc;
				anc = anc.getAncestor();
				this_thing = holder;
				}
			}
		message.append("nothing");
		message.append("\nit implements the following interfaces:");
		String[] anky = backup.getEveryInterface();
		if (anky.length == 0) {message.append("none");}
		else {
			for (int j=0; j<anky.length;j++)
			{
				message.append('\n').append('\t').append(anky[j]);
			}
		}
		
		message.append("\n").append(pts[1]).append(" contains ").append(inactions.length).append(" actions");
		message.append("\nprofiling actions in '").append(pts[1]).append("'").append(instring).append("\n===============\n");
		for (int y=0; y<inactions.length; y++)
			{
			message.append(inactions[y].profile());
			}
		message.append("\nend profile for actions in '").append(pts[1]).append("'").append(instring).append("\n===============\n");

		Var[] intvars = intvc.in();
		message.append(pts[1]).append(" contains ").append(intvars.length).append(" vars\n");
		message.append("profiling vars in '").append(pts[1]).append("'\n==============\n");
		
		for (int y=0; y<intvars.length; y++)
			{
			/*one array, re-assign string refs*/
			prof_array[1] = intvars[y].getName();
			message.append(profile(prof_array,intvc,ttc,pts[1]));
			}
			
		}			
		}
	message.append("\n====================\nEnd profile for '").append(pts[1]).append("'").append(instring).append("\n==================\n");
	return message.toString();
	}

public static Var convert(VarContainer vc, String oldval, int tp, String vname) throws Exception
	{
	if (vc.is_in(oldval)) {
					if (tp == ANYVAR)
						{return new Var(new Anyvar(vc.get(oldval)),vname);}
					oldval=vc.get(oldval).asString();
					}
	if ((oldval.startsWith("\"") && oldval.endsWith("\"")) || (oldval.startsWith("\'") && oldval.endsWith("\'")))
		{oldval=oldval.substring(1,(oldval.length()-1));}


	switch(tp)
		{
		case 0: {
			  if (oldval.equalsIgnoreCase("yes") || oldval.equalsIgnoreCase("true"))
				{
				return new Var(true,vname);
				}
			  if (oldval.equalsIgnoreCase("no") || oldval.equalsIgnoreCase("false"))
				{
				return new Var(false,vname);
				}
			  break;
			  }
		case 1: {
			  return new Var(Long.parseLong(oldval),vname);
			  }
		case 2: {
			  return new Var(Double.parseDouble(oldval),vname);
			  }
		case 3:
			  {
			  if (oldval.length() != 1)
				{throw new Exception();}
			  return new Var(oldval.charAt(0),vname);
			  }
		case 4:
			{
			return new Var(oldval,vname);
			}
			
		default: {break;}
		}

boolean b = true;
if (b) {throw new Exception();}

return new Var("~",-1);			
}			     
public static Var makeStringVar(String dat, String[] srefs,boolean any)
{
 String vdat = new StringBuffer().append("_").append(dat.substring(1,dat.length()-1)).toString();
 Var svar=null;
 if (dat.startsWith("$"))
	{
	dat = dat.substring(1,dat.length()-1);
	int ix = Integer.parseInt(dat);
	svar = new Var(srefs[ix].substring(1,srefs[ix].length()-1),vdat);
	}
      else {dat=dat.substring(1,dat.length()-1);
		svar =new Var(dat,vdat);
		}
 if (any) {return new Var(new Anyvar(svar),vdat);}
 return svar;
}

public static Var makeCharVar(String dat, String[] crefs, boolean any)
{
 String vdat = new StringBuffer().append("_").append(dat.substring(1,dat.length()-1)).toString();
 Var cvar =null;
 if (dat.startsWith("&"))
	{
	dat=dat.substring(1,dat.length()-1);
	int ix =Integer.parseInt(dat);
	cvar =new Var(crefs[ix].substring(1,crefs[ix].length()-1),vdat);
	}
	else {dat=dat.substring(1,dat.length()-1);
		cvar = new Var(dat,vdat);
		}
 if (any) {return new Var(new Anyvar(cvar),vdat);}
 return cvar;
}

public static String[] getVarAlternates(int i)
{
	if ((i<0) || (i>16)) {return NO_ALTS;}
	return VAR_ALTS[i];
}



public static String[] getGroupAlternates(int vgtype)
{
	if ((vgtype<0)||(vgtype>16)) {return GRP_ALTS[16];}
	return GRP_ALTS[vgtype];
}

public static String[] getGroupAlternates(String vgtype,ThingTypeContainer ttc)
{
	//System.err.println("vgtype="+vgtype);
	return getGroupAlternates(typePlurals(vgtype));
}

/*finis*/
/*please let me commit this now!*/
}


