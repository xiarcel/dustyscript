package dscript;

import java.util.HashMap;
import java.util.Vector;

public class VarGroup {

	private String declared_thing_type="";
	private static long COUNTER=0;
	private long count;
private String name;
private int size;
private int type;
private static VarContainer outer;
private HashMap in=new HashMap();
private Var sz =new Var(0,"size");
private VarContainer vc;

final static Var none = new Var(">NULL<",-1);
public static boolean DEBUG = StatementProcessor.DEBUG;
//public static boolean DEBUG = true;

public VarGroup(String n, int t, VarContainer vc)
	{
	count = COUNTER;
	COUNTER++;
	type=t;
	name=n;
	this.vc=vc;
	}
public String getHashValue()
{
	return "group:"+name+":"+count;
}

public void setDeclaredThingType(String s)
{
	declared_thing_type=s;
}

public String getDeclaredThingType()
{
	
	//System.err.println("getDeclaredThingType(), type=="+type);
	if (type==10 && declared_thing_type.equals(""))
	{
		return "thing";
	}
	
	return declared_thing_type;
}

	
public void add(Var v)
{add(v,vc);}

public void add_internal(String key, Var v)
{
	if (in.containsKey(key)) {in.remove(key);}
	in.put(key,v);
}
public void add(Var v, VarContainer varc)
	{
	if (DEBUG){System.out.println("VarGroup.add("+v.getName()+")");}
	String vname=v.getName();
	/*this is symptomatic of some bigger problem-quick fix?*/
	int ix = vname.indexOf(":"); //at this stage, we are not 'adding' any identifiers
	//varc.dump();
	
	if (ix > -1)
	{
		ix++;
		vname = vname.substring(ix,vname.length());
		ix = vname.indexOf(":");
		if (varc.is_in(vname)){vname=varc.get(vname).asString();}
		if (vname.equalsIgnoreCase("last")) {vname = ""+(size()+1);}
		if (DEBUG){System.out.println("varc.is_in():"+varc.is_in(vname));}
		v.ensureNameIs(vname);
	}
	String indx="";
	if (ix >-1) {indx=vname.substring((ix+1),vname.length());
		     vname = vname.substring(0,ix);
	}
	if (in.containsKey(vname)&&!indx.equals(""))
	{
		Var old = (Var)in.get(vname);
		if (old.getType() == Var.GROUP)
		{	v.ensureNameIs(indx);
			old.getVarGroup().add(v);
			return;
		}
	}
	
	//if (v.getName().startsWith(name+":")) {v.ensureNameIs(vname.substring(name.length()+1,vname.length()));}
	if ((v.getType() != type)&&(type != 16)) {return;}
	if (in.containsKey(vname)) {in.remove(vname);}
	in.put(vname,v);
	if(DEBUG){System.out.println(v.getName()+":vgn");}
	sz.setValue(in.size());
	}

public Var getSizeVar() {return sz;}

public Var get(String name, Output OUT, VarContainer vc, boolean AT)
	{
	if (DEBUG) {System.out.println("Searching for "+name+" in "+this.name);}
	if (name.equalsIgnoreCase("size")) {return sz;}
	if (name.equalsIgnoreCase("last")) {return get(sizeRep(),OUT,vc,AT);}
	String index="";
	int ix = name.indexOf(":");
	if (ix > -1) {index=name.substring((ix+1),name.length()).trim();
			  name=name.substring(0,ix).trim();
			 }
	
	if (name.indexOf(this.name)>-1)
	{
		name = index; if (DEBUG){System.out.println(this.name+":"+name+" called for inside of VG");}
	}
	/*
	if (name.equals("size"))
	{
	name = ""+size();
	}
	*/
	if (in.containsKey(name))
		{
		Var grp= (Var)in.get(name);
		if (index.equals("")) {return grp;}
		
		if (grp.getType() != Var.GROUP)
			{
			OUT.println(new DSOut(DSOut.ERR_OUT,-1,"indexes, such as "+index+" are only for groups\n\t"+
					name+" does not appear to be a group"),vc,AT);
			return none;
			}
		if (index.equalsIgnoreCase("size")) {return grp.getVarGroup().getSizeVar();}
		//if (vc.is_in(index)) {index = new String(""+vc.get(index).getInteger());}
		
		return grp.getVarGroup().get(index,OUT,vc,AT);
		}
	return none;
	}

public void replace(String name, Var v)
	{
	if (DEBUG){System.out.println("replacing:"+name+" in "+this.name);}
	int x = name.indexOf(":");
	if (x < 0) {v.ensureNameIs(name);replace(v); return;}
	String index = name.substring((x+1),name.length());
	name=name.substring(0,x);
	if (name.equals(this.name)) 
	{
				    int p = index.indexOf(":");
				    if (p<0) {v.ensureNameIs(index); replace(v); return;}
				    
				    name = index.substring(0,p);
				    index = index.substring((p+1),index.length());
	}
	
	if (in.containsKey(name))
		{
		Var grp = (Var)in.get(name);
		if (index.equals("")){replace(grp);return;}
		if (grp.getType() != Var.GROUP) {return;}
		grp.getVarGroup().replace(index,v);
		}
	}

private boolean replace(Var v)
	{
	String nm=v.getName();
	if (in.containsKey(nm))
		{
		in.remove(nm);
		in.put(nm,v);
		return true;
		}
	return false;
	}

public boolean is_in(String name, VarContainer vc)
	{
	if (name.equalsIgnoreCase("size")||name.equalsIgnoreCase("last")) {return true;}
	String indx="";
	int ix =name.indexOf(":");
	if (ix > -1)
		{
		indx=name.substring((ix+1),name.length());
		name=name.substring(0,ix);
		}
	
	if (name.indexOf(this.name)>-1)
	{
		int p = indx.indexOf(":");
		if (p < 0) {name=indx;}
		else
		{name = indx.substring(0,p);
		 indx = indx.substring((p+1),indx.length());
		}
	}
	/*
	if (name.equals("size")){return true;}
	*/
	
	if (in.containsKey(name))
		{
		if (indx.equals("")) {return true;}
		Var grp = (Var)in.get(name);
		if (grp.getType() != Var.GROUP) {return false;}
		//if (vc.is_in(indx)) {indx = new String(""+vc.get(indx).getInteger());}
		return grp.getVarGroup().is_in(indx,vc);
		}
	return false;
	}


public Var[] in()
	{
	Var[] innies = new Var[in.size()];
	for (int i=0; i<innies.length;i++)
		{innies[i] = (Var)in.get(""+(i+1));}
	return innies;
	}

public String asString()
	{
	String s="";
	Var[] interns = in();
	if (interns.length < 1) {return "empty group";}
	for(int i=0; i<interns.length;i++)
		{
		s=s+(i+1)+":"+interns[i].asString()+"   ";
		}
	s.trim();
	return s;
	}

public int getType() {return type;}
public String getName() {return name;}
public VarContainer getVarContainer(){return vc;}

public static Var make(Statement s, VarContainer outtie, Output OUT)
	{outer=outtie; return make(s,OUT);}

public static Var make(Statement s, Output OUT)
	{
		ThingTypeContainer ttc=outer.getSameLevelThingTypeContainer();
	boolean AT = s.getAttempting();
	String[] parts = s.getParts();
	if (parts.length < 4)
		{
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"This group statement is too short",s),
			outer,AT);
		return none;
		}
	if (!parts[2].equalsIgnoreCase("contains"))
		{
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"Dustyscript expected 'contains' but got '"+parts[2]+"'",s),
			outer,AT);
		return none;
		}
	if (parts[3].startsWith(":")){parts[3]=parts[3].substring(1,parts[3].length());} //fix for anon actions
	int type = Var.typePlurals(parts[3]);
	long begin =0;
	String typefour=""; String typethree="";
	String thing_type="";
	
	if (parts.length >= 4 && parts[3].endsWith("s")) {typethree=parts[3].substring(0,parts[3].length()-1);}
	if (parts.length >= 5 && parts[4].endsWith("s")) {typefour=parts[4].substring(0,parts[4].length()-1);}
	
	if (type == -1 && parts[3].endsWith("s"))
	{
		if (ttc.is_in(typethree)||ttc.containsInterface(typethree)||ttc.allowed(typethree))
		{
		
			type=Var.typePlurals("things");
			thing_type=typethree;
		}
	}
	if (type == -1)
		{
		if ((outer !=null) && outer.is_in(parts[3]))
			{
			type=Var.typePlurals(parts[4]);
			if (type == -1 && parts[4].endsWith("s"))
			{
				if (ttc.is_in(typefour)||ttc.containsInterface(typefour)||ttc.allowed(typefour))
				{
					type=Var.typePlurals("things");
					thing_type=typefour;
				}
			}
			
			try{
			   begin = Long.parseLong(outer.get(parts[3]).asString());
			    }
			    catch(Exception e) {begin=-1; type=-1;}
			}
		if (type == -1)
			{
			try{
			   type = Var.typePlurals(parts[4]);
			   begin = Long.parseLong(parts[3]);
			   }
			   catch(Exception e) {type=-1;}
			   if (type == -1 && parts[4].endsWith("s"))
			   {
				   if (ttc.is_in(typefour)||ttc.containsInterface(typefour)||ttc.allowed(typefour))
				   {
					   type=Var.typePlurals("things");
					   thing_type=typefour;
				   }
			   }
			}
		if (type == -1)
			{
				//for (int i=0; i<parts.length;i++) {System.out.println("["+i+"]:"+parts[i]);}
			OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ASS,"'"+parts[3]+
					"' is not a variable type, or in the wrong location",s),outer,AT);
			return none;
			}
		}
	VarGroup vg = new VarGroup(parts[1],type,outer);
	vg.setDeclaredThingType(thing_type);
	long i = 1;
	
	while( i<= begin )
		{
			//loop with long
		vg.add(new Var(""+i,type).setDeclaredThingType(vg.declared_thing_type),outer);
		i++;
		}
	return new Var(vg,parts[1]);
	}

public static VarGroup copy(VarGroup vg, String nwname)
{return copy(vg,nwname,null);}

	
public static VarGroup copy(VarGroup vg, String nwname, VarContainer varc)
	{
	//System.out.println(vg.getName()+":"+nwname);
	
	VarContainer VC=null;
	if (varc !=null){VC=varc;}
	else {VC = vg.getVarContainer();}
	
	VarGroup cp = new VarGroup(nwname,vg.getType(),VC);
	Var[] in = vg.in();
	for (int i=0; i<in.length;i++)
		{
		if (DEBUG) {System.out.println("VarGroup.copy:in["+i+"]=="+in[i].getType()+" ::class("+in[i].getClass()+")");}
		cp.add(Var.copy(in[i]));
		}
	if (DEBUG){System.out.println("cp.size():"+cp.size());}
	return cp;
	}
public static VarGroup copy(VarGroup vg)
{return copy(vg,vg.getName(),null);}
	
	
public static VarGroup copy (VarGroup vg,VarContainer vc)
	{
	return copy(vg,vg.getName(),vc);
	}

public String sizeRep()
	{
	return ""+size();
	}

public int size()
	{
	return in.size();
	}

public void trim(int i)
	{
	Var[] v=in();
	int keep=0;
	keep = v.length - i;
	if (keep < 0) {keep=0;}
	in.clear();
	for (int x=0; x<keep; x++)
		{
		in.put(v[x].getName(),v[x]);
		}
	}

public static VarGroup createReverse(VarGroup original, String name)
	{
	VarGroup copy = new VarGroup(name,original.getType(),original.getVarContainer());
	Var[] vars = original.in();
	for (int i=vars.length-1; i>-1; i--)
		{
		String nm = ""+(vars.length-i);
		copy.add(Var.copy(vars[i],nm),original.getVarContainer());
		}
	return copy;
	}
	
public String[] groupStrings()
{
Var[] vs = in();
Vector v = new Vector();
for (int i=0; i<vs.length;i++)
{
	if (vs[i].getType() == Var.GROUP)
	{
	VarGroup vg = vs[i].getVarGroup();
	String[] s = vg.groupStrings();
	for (int j=0; j<s.length;j++)
	{
		v.addElement(s[j]);
	}
	}
	else {
		v.addElement(vs[i].asString());
	}
}
Object[] o = v.toArray();
String[] ret = new String[o.length];
System.arraycopy(o,0,ret,0,o.length);
return ret;
}

public static Var createArgArray(VarContainer vc, String[] args, Statement s)
{	
	if (s != null)
	{
	String[] srefs = s.getRefs();
	String[] crefs = s.getCharRefs();
	for (int i=0; i<args.length;i++)
	{
		if (args[i].startsWith("$")&&args[i].endsWith("$"))
		{
			try {int m = Integer.parseInt(args[i].substring(1,args[i].length()-1));
			     args[i] = srefs[m];
			     if (args[i].startsWith("\"") && args[i].endsWith("\"")) {args[i]=args[i].substring(1,args[i].length()-1);}
			}
			catch(Exception e){}
			continue;
		}
		if (args[i].startsWith("&")&&args[i].endsWith("&"))
		{
			try{
				int m = Integer.parseInt(args[i].substring(1,args[i].length()-1));
				args[i]= crefs[m];
				if (args[i].startsWith("\'") && args[i].endsWith("\'"))
				{
					args[i]=args[i].substring(1,args[i].length()-1);
				}
			}
			catch(Exception e){}
			continue;
		}
	}
	}
	return createArgArray(vc,args);
}


	
	
public static Var createArgArray(VarContainer vc, String[] args)
{
Vector str =new Vector();
for (int i=0; i<args.length;i++)
	{
	if (vc.is_in(args[i]))	
		{
		
		Var v = vc.get(args[i]);
		if (v.getType()==Var.GROUP)
			{
			String[] s = v.getVarGroup().groupStrings();
			for (int j=0; j<s.length;j++)
				{
				str.addElement(s[j]);
				}
			}
			else {str.addElement(v.asString());}
		}
	else {str.addElement(args[i]);}
	}
VarGroup voog =new VarGroup("ARGUMENTS",4,vc);
for (int k=0; k<str.size();k++)
{
	String ss = (String)str.elementAt(k);
	voog.add(new Var(ss,""+(k+1)));
}
return new Var(voog,"ARGUMENTS");
}

	

			


}
