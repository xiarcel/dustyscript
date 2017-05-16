package dscript;

import java.util.Vector;
import java.io.*;
import dscript.connect.JavaConnector;


class ThingType{

private String[] all_interfaces=null;	
private Var global_thing=null;
static StringBuffer sb=new StringBuffer(150);
private static String[] prof_parts={"",""};
static String udir="";
private boolean has_interfaces=false;
static long TTCTR=0;
private VarContainer vcg;
private String inner;
private int[] argtypes;
private String[] argnames;
private String name;
private ActionContainer act;
private VarContainer vc;
private ThingTypeContainer ttc;
private ThingType ancestor=null;
private String[] interfaces = new String[0];
private boolean has_ancestors=false;
private InputStream IN; private Output OUT;
private String PROFILE="";
private String[] all_ancestors=null;
private String[] search_ancestors=null;

public ThingType(String nam, String innr, ThingTypeContainer outer, Output O, InputStream I)
	{OUT=O; IN=I;
	vcg=new VarContainer(OUT);
	inner=innr; name=nam;
	act = new ActionContainer(OUT);
	vc =new VarContainer(OUT);
	ttc =outer;
	}


public String getInner(){return inner;}
public String getName(){return name;}
public ThingType setInterfaces(String[] s)
{
	interfaces=s;
	has_interfaces=true;
	return this;
}

public ActionContainer getActionContainer()
{
return act;
}

public void frontload(ThingTypeContainer TTC, StatementProcessor SP)
{
ThingType[] tta = TTC.in();
for (int i=0; i<tta.length;i++)
{
	if (! ttc.is_in(tta[i].getName()) ) {ttc.add(tta[i]);}
}
StatementProcessor holder=SP;
while ((SP.getNextUp() != null) && (SP.getNextUp() != SP)) 
{
	SP=SP.getNextUp();
	frontload(SP.getThingTypeContainer(),SP);
}
		
}

	

public void setAncestor(ThingType tt)
{
ancestor=tt;has_ancestors=true;
}

public ThingType getAncestor()
{
if (ancestor !=null) {return ancestor;}
return this;
}

public String getAncestorName()
{
if(ancestor != null){return ancestor.getName();}
return "~";
}

public void addInterfacesToAncestors(Vector v)
{
	for (int i=0; i<interfaces.length;i++)
	{
		if (!v.contains(interfaces[i])){v.addElement(interfaces[i]);}
	}
}

public String[] getEveryInterface()
{
if (all_interfaces == null)
{
	Vector v = new Vector();
	Object[] o = getEveryInterface(v).toArray();
	all_interfaces = new String[o.length];
	System.arraycopy(o,0,all_interfaces,0,all_interfaces.length);
}
	return all_interfaces;
}
public Vector getEveryInterface(Vector v)	
{
	for (int i=0; i<interfaces.length;i++) 
	{
		if (!v.contains(interfaces[i]))
		{v.addElement(interfaces[i]);
		//System.out.println("adding interface:"+interfaces[i]);
		}
	}
	if (has_ancestors)
	{
		ThingType tt = getAncestor();
		if (tt != this)
		{
			v=tt.getEveryInterface(v);
		}
	}
	return v;
}


private String[] createAllAncestors()
{

if (has_ancestors || has_interfaces)
{
	Vector v = new Vector();
	v.addElement(name);
	addInterfacesToAncestors(v);
	ThingType this_one = getAncestor();
	String last_check="";
	while ((this_one != this) && !this_one.getName().equals(last_check))
	{	last_check = this_one.getName();
		v.addElement(last_check);
		this_one.addInterfacesToAncestors(v);
		this_one = this_one.getAncestor();
	}
	Object[] o = v.toArray();
	String[] nms = new String[o.length];
	System.arraycopy(o,0,nms,0,o.length);
	//if (name.equals("TextArea")) {dump(nms);}
	return nms;
}

return new String[]{name};
}

private void dump(String[] s)
{
	for (int i=0; i<s.length;i++)
	{
		System.out.println("s["+i+"]=="+s[i]);
	}
}

public String[] getAllAncestors()
{
	if ((all_ancestors == null)||(search_ancestors==null)) 
		{all_ancestors=createAllAncestors();
			
			search_ancestors = new String[all_ancestors.length+2];
			System.arraycopy(all_ancestors,0,search_ancestors,0,all_ancestors.length);
			search_ancestors[all_ancestors.length]="10"; //thing generic
			search_ancestors[all_ancestors.length+1] = "16"; //anyvar
			
		}
	return all_ancestors;
}

public String[] getSearchAncestors()
{
	if ((all_ancestors == null)||(search_ancestors==null)) 
		{all_ancestors=createAllAncestors();
			
			search_ancestors = new String[all_ancestors.length+2];
			System.arraycopy(all_ancestors,0,search_ancestors,0,all_ancestors.length);
			search_ancestors[all_ancestors.length]="10";
			search_ancestors[all_ancestors.length+1] = "16";
			
		}
	return search_ancestors;
}


public boolean hasAncestor(){return has_ancestors;}
public boolean hasInterfaces(){return has_interfaces;}
public VarContainer getVarContainer()
{
return vc;
}



public boolean createPrototype(VarContainer vc, ActionContainer act, ThingTypeContainer ttc, boolean ignore, boolean ignore_globals,boolean AT)
{
 StatementProcessor sp=new StatementProcessor(inner,vc,act,ttc,OUT,IN);
 sp.suppress();
 sp.setAttempting(AT);
 if(!ignore){sp.runOnlyActions();}
 else{sp.ignoreActions();}
 if (ignore_globals) {sp.ignoreGlobals();}
 sp.run();
 //if (sp.encounteredError()){return false;}
 if (has_ancestors) {getAncestor().processInheritance(vc,act,AT);}
 if (has_interfaces)
 {
	 for (int i=0; i<interfaces.length;i++)
	 {
		 ThingInterface ti = ttc.getInterface(interfaces[i]);
		 if (ti == null) {continue;}
		 if (!ti.does_implement(act)) {
		 OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.THG,"This thing is supposed to provide implementation for interface '"+interfaces[i]+"', and doesn't!"),vc,AT);
		 return false;}
	 }
 }
 if (global_thing == null)
	{
	ActionContainer acg = act.createGlobalContainer();
	vcg = vc.createGlobalContainer();
	
	global_thing=new Var(new Thing(name,name,acg,vcg),name);
	}

 return true;
}

public Var getGlobalThing()
{
return global_thing;
}


public boolean createPrototype(boolean AT)
{
 return createPrototype(vc,act,ttc,false,false,AT);
}


public void processInheritance(VarContainer vars, ActionContainer acts,boolean AT)
{
 VarContainer varc = new VarContainer(OUT);
 createPrototype(varc,act,ttc,true,true,AT);

 //hmm...is super(?) this easy
 //vars.add(new Var(new Thing(name,"parent",act,varc), "parent"));
 
 Object[] o = vc.in(); Object[] t = varc.in();
 Var[] vs =new Var[o.length+t.length];
 //I don't know anymore what the difference between vc/varc is here
 System.arraycopy(o,0,vs,0,o.length);
 System.arraycopy(t,0,vs,o.length,t.length);

 Action[] ac = act.in();
 VarContainer parentvars=new VarContainer(OUT);
 ActionContainer parentacts=new ActionContainer(OUT);
 vars.add(new Var(new Thing(name,"parent",parentacts,parentvars),"parent"));

 for (int i=0; i<vs.length; i++)
	{
	String nm=vs[i].getName();
	if (nm.equals("parent")){continue;
				//skip --do not add 'grandparent' to father's son!
	}
	if (vs[i].isGlobal() && vars.is_in(nm))
		{
		if (!vars.get(nm).isGlobal())
			{throw new RuntimeException("Cannot override a global var with non-global");
			}
		parentvars.add(vs[i]);
		continue;
		}
        Var to_put=null;
	if (vs[i].isGlobal()) {to_put = vs[i];}
	else {to_put=Var.copy(vs[i]);}
	if (!vars.is_in(vs[i].getName()))
	{
		vars.add(to_put);
	}
	else {to_put = vars.get(to_put.getName());}
	parentvars.add(to_put);

	}
	
 for (int j=0; j<ac.length;j++)
	{
	boolean ig = ac[j].isGlobal();
	if (ig && acts.is_in(ac[j].getName(),ac[j].getArgs(),ac[j].getReturnType()))
		{
		Action acct = acts.get(ac[j].getName(),ac[j].getArgs(),ac[j].getReturnType());
		if (!acct.isGlobal())
			{
			throw new RuntimeException("Cannot override global action with non-global");
			}
		parentacts.add(ac[j]);
		continue;
		}	
	if (!acts.is_in(ac[j].getName(),ac[j].getArgs(),ac[j].getReturnType()))
		{Action newact=null;
			if (!ig){newact=ac[j].copySelf();}
			else{newact=ac[j];}
			acts.add(newact);
			parentacts.add(newact);
		}
		else {parentacts.add(ac[j]);}
		
		
	}

	//if (vars.is_in("parent")) {vars.get("parent").setValue(new Thing(name,"parent",parentacts,parentvars));}
	
}



public Var createThing(Statement s,VarContainer varc, StatementProcessor SP)
{
//act.dump(); 
//System.exit(0);

//new call--makes copies of any action that is synch'd but not 'global'
//moveed to Thing constructor.

ActionContainer my_actions=act; //act.createSafeCopy();
 
boolean AT = s.getAttempting();
VarContainer cop = new VarContainer(OUT);
//add self-reference early.
Var ME = new Var(new Thing(name,"me",my_actions,cop),"me");
cop.add(ME);

/*now a mis-nomer for method-name*/
/*we ignore action creating statements to save processing time*/

boolean ok_new = createPrototype(cop,my_actions,ttc,true,true,AT);

if (!ok_new && StatementProcessor.DEBUG){OUT.println("Failed at creating thing",2);}
if (!ok_new) {OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "Could not create thing from thing type",s),
			vc,AT);}

VarContainer outer=new VarContainer(OUT);
outer.add(new Var(new VCWrapper(cop)));
outer.add(new Var(new VCWrapper(varc)));

String[] parts=s.getParts();
if (parts.length < 5)
	{
	OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.THG,"Too short a statement for a thing type definition",s),
			vc,AT); 
	return new Var("~",-1);
	}
String nam = parts[2];
String[] varparts=new String[parts.length-2];
varparts[0] = parts[1];
for (int i=1; i<varparts.length;i++)
	{
	varparts[i]=parts[(i+2)];
	}
//ttc.dump();
Statement constructor_state =new Statement();
constructor_state.resetParts(varparts);
constructor_state.setInline(s.getInline());
constructor_state.setRefs(s.getRefs());
constructor_state.setCharRefs(s.getCharRefs());
constructor_state.setAttempting(AT);
//ActionProcessor.DEBUG=true;
boolean b= ActionProcessor.process(my_actions,constructor_state,outer,ttc,OUT,IN);
//ActionProcessor.DEBUG=false;
/*
THIS IS THE SECTION
THAT CALLS the ActionProcessor (2 linea above)
SOMEHOW--a Thing creating a Thing of its own type is failing...
*/

if (!b) {
	OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.THG,"Failed at making this thing\n\t.."+parts[1]+" "+parts[2]+"...",s),
				vc,AT);
	//ttc.dump();
	return new Var("~",-1);
	}

nam = pullGroupStuff(nam, varc);


/*here we need to create a Thing with our VC, AC and ref to name*/
Thing t = new Thing(name,nam,my_actions,cop);
ME.setValue(t); //make sure it is a true "me" once done..
processGlobalHierarchy(my_actions,cop);

return new Var(t,nam);

}

public void processGlobalHierarchy(ActionContainer a, VarContainer v)
{
if (a != act)
{
Action[] actions = act.in();
for (int i=0;i<actions.length;i++)
	{
	if (!a.is_in(actions[i]))
		{
		a.add(actions[i]);
		}
	}
}

if (v !=vc)
{
Var[] vars = vc.in();
for (int j=0; j<vars.length;j++)
	{
	if (!v.is_in(vars[j].getName()))
		{
		v.add(vars[j]);
		}
	}
}

if (hasAncestor())
	{
	getAncestor().processGlobalHierarchy(a,v);
	}
}

public static String pullGroupStuff(String old, VarContainer vc)
	{
	if (old.indexOf(":") < 0) {return old;}
	StringBuffer nwprt =new StringBuffer(50);
	StringBuffer prt=new StringBuffer(20);
	boolean firstpt = true;
	for (int i=0; i<old.length(); i++)
		{
		char c= old.charAt(i);
		if (c == ':')
			{
			if (!firstpt)
				{
				String ptst=prt.toString();
				if(vc.is_in(ptst)) 
					{
					prt.setLength(0);
					prt.append(vc.get(ptst).asString());
					}
				}
			nwprt.append(prt).append(c);
			prt.setLength(0);
			firstpt=false;
			continue;
			}
		prt.append(c);
		}
	if ((prt.length()>0) && !firstpt)
		{
		String ptstr = prt.toString();
		if (vc.is_in(ptstr)){nwprt.append(vc.get(ptstr).asString());}
		else{nwprt.append(ptstr);}
		}
	return nwprt.toString();
	}

public void createProfile()
{
sb.setLength(0);
VarContainer nwvc = new VarContainer(OUT);
createPrototype(nwvc,act,ttc,true,true,true);
String instring = " in the Thing-Type:"+name;
sb.append("Profile of the Thing-Type::").append(name);
sb.append("\n================\n================\n");
Var[] nwvs = nwvc.in();
Var[] glvs = vcg.in();
Var[] varries = new Var[nwvs.length+glvs.length];
System.arraycopy(glvs,0,varries,0,glvs.length);
System.arraycopy(nwvs,0,varries,glvs.length,nwvs.length);

/*this MUST be able to be done better*/
VarContainer prof_vc=new VarContainer(OUT);
prof_vc.wholesaleAdd(varries);

Action[] actions= act.in();
sb.append(name).append(" descends from:");
if (hasAncestor())
	{
	ThingType anc = getAncestor();
	ThingType this_thing=this;
	while (anc != this_thing)
		{
		sb.append(anc.getName()).append("\n\twhich descends from: ");
		ThingType holder = anc;
		anc = anc.getAncestor();
		this_thing = holder;
		}
	}
sb.append("nothing\n");
sb.append("\nimplements the following interfaces:");
String[] ss = getEveryInterface();
if (ss.length ==0) {sb.append("none\n");}
else{
for (int i=0; i<ss.length;i++)
{
	sb.append('\n').append('\t').append(ss[i]);
}
}
sb.append("\n{{Profile of Variables in::").append(name).append("}}\n");
sb.append("\t").append(name).append(" contains ").append(varries.length).append(" variables..\n");
for (int i=0; i<varries.length;i++)
	{
	prof_parts[1]=varries[i].getName();
	sb.append(Var.profile(prof_parts,prof_vc,ttc,instring));
	}
sb.append("{{End of Variables in::").append(name).append("}}\n");
sb.append("{{Profile of Actions in::").append(name).append("}}\n");
sb.append("\t").append(name).append(" contains ").append(actions.length).append(" actions..\n");
for (int j=0; j<actions.length;j++)
	{
	sb.append(actions[j].profile());
	}
sb.append("{{End of Actions in::").append(name).append("}}\n");
sb.append("{------end of profile for Thing-Type::").append(name).append("------}\n");
PROFILE=sb.toString();
/*THIS MIGHT be necessary for memory-save, will confer
PROFILE=new String(udir+"tt_"+TTCTR);TTCTR++;
try{
	FileWriter fw= new FileWriter(new File(PROFILE));
	fw.write(sb.toString());
	fw.close();
	}
	catch(Exception e){}
*/

sb.setLength(0);
}

public String getProfile()
{
if (PROFILE.equals("")){createProfile();}
return PROFILE;

/*THIS MIGHT BE NECESSARY FOR MEMORY SAVER, NEED TO CONFER
try{
	StringBuffer sbf=new StringBuffer(100);
	BufferedReader br=new BufferedReader(new FileReader(new File(PROFILE)));
	String s;
	while ((s=br.readLine())!=null)
		{
		sbf.append(s).append("\n");
		}
	br.close();
	return sbf.toString();
	}
	catch(Exception e){}
 return "Profile not available for:"+name;
*/
}
//move this into callable method--applet issue
public static void setUDir()
	{udir=System.getProperty("user.dir")+System.getProperty("file.separator")+"swap"+System.getProperty("file.separator");}



}






