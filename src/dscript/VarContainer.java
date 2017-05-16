package dscript;

import java.util.Vector;
import java.util.HashMap;

public class VarContainer{

private Vector v;
private HashMap hm;
private boolean globalcounter=false;
private boolean creating_global=false;
public boolean attempting = false;
private static int COUNT = 0;
private static boolean DEBUG=false;
private String lastsearch="";
private int numsearch=0;
private int refnum;
private ActionContainer same_level = null;
private ThingTypeContainer same_level_ttc=null;
private Output OUT;
private static HashMap nonKeywordHash = new HashMap(1000);


public VarContainer(Output O)
{v=new Vector();
hm=new HashMap();


OUT=O;
if (!DEBUG){DEBUG=StatementProcessor.DEBUG;}
refnum=COUNT; COUNT++;
}

public void clearTopLevel()
{
	hm.clear();
}

public void clearAll()
{
	v.removeAllElements();
	clearTopLevel();
}

public Output getOutput()
{
return OUT;
}

public void setSameLevelActionContainer(ActionContainer ac)
{
 if (same_level == null) {same_level=ac;}
 //only set once 
 //not sure what this could break
}

public ActionContainer getSameLevelActionContainer()
{
	return same_level;
}
public void setSameLevelThingTypeContainer(ThingTypeContainer tc)
{
if (same_level_ttc==null){same_level_ttc=tc;}
}

public ThingTypeContainer getSameLevelThingTypeContainer()
{
return same_level_ttc;
}

public void setGlobalVarState(boolean b)
{
creating_global=b;
}


public void add(Var var)
{
 if (var == null){return;}
 if (var.getType() == Var.VC) {v.addElement(var);return;}
 if(DEBUG) {System.err.println("Now adding a var named "+var.getName());
 //if (var.getName().equals(">NULL<")) {throw new RuntimeException("ARG!");}
 }
 if (!is_in(var)){hm.put(var.getName(),var);}
 if (creating_global) {var.setAsGlobal();globalcounter=true;}

}

public void wholesaleAdd(Var[] vars)
{
 for (int i=0;i<vars.length;i++) {add(vars[i]);}
}


public int getReference(){return refnum;}

public Var get(String name)
{
return get(name,false);
}

public Var get(String name, boolean useglobals)
{
 if (DEBUG){System.err.println("looking for"+name);}
 if (name.startsWith(":")) {name=name.substring(1,name.length());} //fix for anon-act.
  if (hm.containsKey(name))
	{
	Var vs = (Var)hm.get(name);
	if (vs.isTransient()) {vs.setUsedBit(true);}
	lastsearch=""; numsearch=0;
	return vs;
	}
 if (useglobals && (same_level_ttc != null) && (same_level_ttc.is_in(name)))
	{
	return same_level_ttc.get(name).getGlobalThing();
	}

 if (isKeywordOrLiteral(name)){return new Var("",-1);}

 if (name.equalsIgnoreCase(lastsearch))
		{
		numsearch++;
		if (numsearch > 4) {numsearch=0; lastsearch=""; if (StatementProcessor.DEBUG){OUT.println("searched for this 4 times",2);}
					return new Var("",-1);}
		
		}
		else {lastsearch=name; numsearch=0;}
 name = fixSearchString(name);
 
 int ix=name.indexOf(":");
 if (ix > -1)
	{
	String nx = name.substring((ix+1),name.length());
	nx=pullForVars(nx);
	
	name =name.substring(0,ix);
	if (DEBUG){System.err.println("now looking for "+name);}
	if(is_in(name))
	{
	 Var v = get(name);
	 if ((v.getType() == Var.STR))
		{
		if (nx.equals("size")){return new Var(v.asString().length(),"size");}
		String strpt="";
		if (is_in(nx)) {strpt = get(nx).asString();}
			else {strpt=nx;}
		try{
		   int p = Integer.parseInt(strpt);
		   p--;
		   String strng = v.asString();
		   if (p > (strng.length()-1))
			{throw new Exception();}
		   return new Var(strng.charAt(p),"~");
		   }
		   catch(Exception e) {return new Var('\0',"~");}

		}
	
	 if (v.getType() != Var.GROUP)
		{
		 OUT.println(new DSOut(DSOut.ERR_OUT,-1,"'"+name+":"+nx+" should be part of a group\n\tDustyscript does not think it is"),this,attempting);
		 return new Var("",-1);
		}
 	if(nx.equalsIgnoreCase("size")){
	lastsearch="";numsearch=0;
	return new Var(v.getVarGroup().size(),"size");}
	lastsearch="";numsearch=0;
	return v.getVarGroup().get(nx,OUT,this,attempting);
	}
	name=name+":"+nx;
	}


 if (v.size() == 0) {return new Var("~",-1);}
 Object[] o = v.toArray();
 Var[] innies=new Var[o.length];
 System.arraycopy(o,0,innies,0,o.length);
 for (int i =0; i< innies.length;i++)
	{
	if (innies[i].getVCWrapper().is_in(name)) {return innies[i].getVCWrapper().get(name);}
	}
 return new Var("~",-1);
 
}
public void remove(Var voo)
{
 if (hm.containsValue(voo))
	{
	hm.remove(voo.getName());
	return;
	}
 Object[] o = v.toArray();
 Var[] innies=new Var[o.length];
 System.arraycopy(o,0,innies,0,o.length);
 for (int i=0; i<innies.length;i++)
	{
	if (innies[i] == voo) {v.removeElementAt(i);return;}
	}
}

public void remove(String name)
{
 if (hm.containsKey(name)) {hm.remove(name); return;}
  Object[] o = v.toArray();
 Var[] innies=new Var[o.length];
 System.arraycopy(o,0,innies,0,o.length);
 for (int i=0; i<innies.length;i++)
	{
	if (innies[i].getName().equals(name)) {v.removeElementAt(i); return;}
	}
}

public void replace(String name, Var vv)
{
 
 if (isKeywordOrLiteral(name)){return;}
 if (name.equalsIgnoreCase(lastsearch))
		{
		numsearch++;
		if (numsearch > 10) {numsearch=0; lastsearch=""; return;}
		}
		else {lastsearch=name; numsearch=0;}
/*added this here...why check the other stuff first?*/
 if (hm.containsKey(name)) {remove(name);add(vv);lastsearch="";numsearch=0;}

 int ix = name.indexOf(":");
 String inx="";

 if (ix > -1)
	{
	inx =name.substring((ix+1),name.length());
	if(DEBUG){OUT.println("index == "+inx,2);}
	inx = pullForVars(inx);
	name = name.substring(0,ix);
	
	}
 if (hm.containsKey(name))
	{	/*done above*/
		/*
		if (inx.equals("")) {remove(name); add(vv); return;}
		*/

		Var vi = (Var)hm.get(name);
		/*part from failed fix that succeeded at char replace*/
		if (vi.getType() == Var.STR)
		{
			try{vi.setValue(processCharInString(vi.asString(),Integer.parseInt(inx)-1,vv));}
			catch(Exception e){}
			return;
		}
		
		if (vi.getType() != Var.GROUP) {return;}
		lastsearch=""; numsearch=0;
		vi.getVarGroup().replace(inx,vv);
		return;
	}
 if (!inx.equals("")) {name=name+":"+inx;}

  Object[] o = v.toArray();
 Var[] innies=new Var[o.length];
 System.arraycopy(o,0,innies,0,o.length);
 for (int i=0; i<innies.length;i++)
	{
	VCWrapper vcw = innies[i].getVCWrapper();
	if (vcw.is_in(name))
		{
		vcw.replace(name,vv);
		return;
		}
	}
add(vv);
}



private String pullForVars(String index)
{
 Vector vv=new Vector();
 String put="";
 for (int i=0; i<index.length();i++)
	{
	char c= index.charAt(i);
	if (c==':')
		{
		if (!put.equals("")){vv.addElement(put);
					   put="";
					   continue;}
		}
	put=put+c;
	}
 if (!put.equals("")){vv.addElement(put);}
 
 String to_ret="";
 for (int j=0; j<vv.size(); j++)
	{
	String toke = (String)vv.elementAt(j);
      if (is_in(toke)){to_ret=to_ret+get(toke).asString();}
	else {to_ret=to_ret+toke;}
	if (j != (vv.size()-1)){to_ret=to_ret+":";}
	}
 if(DEBUG){OUT.println("Returning:"+to_ret,2);}

 return to_ret;
}
		     
public boolean is_in(String name)
{
//System.err.println("vc("+refnum+").is_in("+name+")");

return is_in(name,false);			    
}

public boolean is_in(String name, boolean useglobals)
{
	if (name.startsWith(":")) {name=name.substring(1,name.length());} //ugly fix for anon actions ;-)
if (hm.containsKey(name)){lastsearch=""; numsearch=0;return true;}
if (useglobals &&(same_level_ttc != null) && (same_level_ttc.is_in(name)))
	{
	return true;
	}
if (isKeywordOrLiteral(name)){return false;}
if(DEBUG){OUT.println("searching for "+name,2);}
if (name.equals(lastsearch))
		{
//		System.err.println("name.equals(lastsearch)");
		numsearch++;
		if (numsearch > 3) {numsearch=0; lastsearch=""; return false;}
		}
else {lastsearch=name; numsearch=0;}
name = fixSearchString(name);

String indx=""; int ix=name.indexOf(":");
if (ix > -1) {indx=name.substring((ix+1),name.length());
		  indx=pullForVars(indx);
		  name =name.substring(0,ix);
		 }

if (hm.containsKey(name))
	{
	Var vs=(Var)hm.get(name);
	/*done above*/
	/*
	if (indx.equals("")){lastsearch=""; numsearch=0; return true;}
	*/
	if (vs.getType() == Var.STR) {lastsearch=""; numsearch=0; return true;}
	if (vs.getType() != Var.GROUP) {return false;}
	boolean b= vs.getVarGroup().is_in(indx,this);
	if (DEBUG){OUT.println("returning "+b+" when looking for "+name,2);}
	if (b) {lastsearch=""; numsearch=0;}
	return b;
	}
if (!indx.equals("")) {name=name+":"+indx;}
VCWrapper[] vcws = new VCWrapper[v.size()];
for (int y=0; y<vcws.length; y++)
	{
	vcws[y] = ((Var)v.elementAt(y)).getVCWrapper();
	}
return check_inners(vcws,name);
}


private boolean check_inners(VCWrapper[] vcwrap, String name)
{
for (int i=0;i <vcwrap.length; i++)
	{
	if (vcwrap[i].getVarContainer() == this) 
	{
	OUT.println(new DSOut(DSOut.ERR_OUT,-1,"'"+name+"' cannot be found\n\tDustyscript appears to be searching eternally\n\t..this might cause another error elsewhere"),this,attempting);
	if(DEBUG){OUT.println("caught recursive self-search",2);}
	continue;
	}
	if (vcwrap[i].is_in(name))
		{lastsearch="";numsearch=0;return true;}
	}
return false;
}

public boolean is_in_local(Var vv)
{
	return is_in(vv);
}


private boolean is_in(Var vv)
{

if (hm.containsValue(vv)) {return true;}
if (hm.containsKey(vv.getName())) {return true;}

for (int i=0; i<v.size(); i++)
	{
	if (((Var)v.elementAt(i)).getName().equals(vv.getName()) || ((Var)v.elementAt(i)) == vv)
		{return true;}
	}
return false;
} 


public Var[] in()
{
 Object[] o = hm.values().toArray();
 Object[] w = v.toArray();
 Var[] in = new Var[o.length+w.length];
 System.arraycopy(o,0,in,0,o.length);
 System.arraycopy(w,0,in,o.length,w.length);
 return in;
}

public void dump()
{dump(0);}


public void dump(int xx)
{
 if ((xx<0)||(xx>2)){xx=0;}

 Var[] inner = in();
 String[] types = new String[]{"is a Yes_No", "is an Integer", "is a Decimal", "is a Char", "is a String", "is a Fraction", "is Complex", "is a Group", "","","is a Thing", "is a JavaConnector"};
 for (int i=0; i<inner.length;i++)
	{
	if (inner[i].getType() == Var.THREAD)
		{
		String ss= "Variable '"+inner[i].getName()+"' is a Thread with value:"+inner[i].asString();
		OUT.println(ss,xx); continue;
		}

	if ((inner[i].getType() > types.length-1) || (inner[i].getType() < 0))
		{
		String ss= "Variable '"+inner[i].getName()+"' is an undefined type with value:"+inner[i].asString();
		OUT.println(ss,xx); continue;
		}
	if (inner[i].getType() == Var.THING)
	{
		try{
		String ss= "Variable '"+inner[i].getName()+"' is a Thing, of Type '"+inner[i].getThing().getThingType()+"' with value:"+inner[i].asString();
		OUT.println(ss,xx); continue;
		}
		catch (Exception e){}
	}
	

	if (inner[i].getType()==Var.VC)
	{continue;}
	String ss="Variable '"+inner[i].getName()+"' "+types[inner[i].getType()];
	if (inner[i].getType()==7){
		String dtt=inner[i].getVarGroup().getDeclaredThingType();
		if (!dtt.equals(""))
		{
			ss=ss+" .. containing Things of the type:"+dtt+"\n...with the value:\n";
		}
		else {ss=ss+", with the value:\n";}
	}
	else
		{
			ss=ss+", with the value:";
		}
	
	ss=ss+inner[i].asString();
	OUT.println(ss,xx);
	}
}
public boolean isKeywordOrLiteral(String srch)
{
/*pull all literal types*/
 if (isStringLiteral(srch) || isCharLiteral(srch)) {return true;}
 if (srch.startsWith("inline_code")) {return true;}
 if (isNumberLiteral(srch)){return true;}
 
 
 if ((same_level != null) && same_level.is_in(srch))
	{
	return true;
	}
/*COMMENTED OUT...We want to return it.
 if ((same_level_ttc != null) && same_level_ttc.is_in(srch))
	{
	return true;
	}
*/

    // Check whether this string has already been encountered and was _NOT_
    // a keyword (in this case it'll be present in the hash).
    if(nonKeywordHash.containsKey(srch)) {
        // We know it isn't a keyword.
        return false;
    }

    String[] kw = StatementProcessor.keywords;
    for (int i=0; i<kw.length;i++)
	{
	    if (srch.equalsIgnoreCase(kw[i])) {
            return true;
        }
	}

    // Not a keyword - so put an entry into our hash so we won't need to go
    // to so much trouble to tell it's not a keyword next time 'round.
    nonKeywordHash.put(srch, null);

 return false;
}

public void removeAllUsedTransients()
	{
	Var[] vars=in();	
	for (int i=0; i<vars.length;i++)
		{
		if (vars[i].isTransient() && vars[i].usedBit())
			{
			remove(vars[i].getName());
			}
		/*
		This was removed because the transients should leave when the StatementProcessor
		they are being used by directly leaves scope.  Removing transients too early
		screws up certain evaluations...

		if (vars[i].getType()==Var.VC)
			{
			VarContainer varc = vars[i].getVCWrapper().getVarContainer();
			varc.removeAllUsedTransients();
			}
			*/
		}
	}


public static void update(VarContainer global, VarContainer local)
{
Var[] globs=global.in();
for (int i=0; i<globs.length; i++)
	{
	Var comp = local.get(globs[i].getName());
	if (comp.asString().equals(">NOVALUE<")){continue;}
	global.remove(globs[i].getName());
	global.add(comp);
	}
}

public static boolean isNumberLiteral(String chk)
{
 for (int i=0; i<chk.length();i++)
	{
	char c = chk.charAt(i);
	if (!Character.isDigit(c) && (c!='.') && (c!='-'))
		{return false;}
	}
 return true;
}
	
public static boolean isDecimalLiteral(String chk)
{
/*assumes isNumberLiteral has been checked*/
return (chk.indexOf(".") > -1);
}

public static boolean isStringLiteral(String chk)
{
 return ((chk.length()>= 2) && ((chk.startsWith("$") && chk.endsWith("$")) || (chk.startsWith("\"") && chk.endsWith("\""))));
}
 
public static boolean isCharLiteral(String chk)
{
 return ((chk.length()>=2) && ((chk.startsWith("&") && chk.endsWith("&")) || (chk.startsWith("\'") && chk.endsWith("\'"))));
}

public String fixSearchString(String s)
{
 if (s.indexOf(":")<0) {return s;}
 StringBuffer sb=new StringBuffer(s.length());
 StringBuffer lit=new StringBuffer(10);
 boolean ingrouppart=false;
 boolean hitfirst=false;
 for (int i=0; i<s.length();i++)
 {
	 char c = s.charAt(i);
	 if (c==':')
	 {
		 if (!hitfirst) {sb.append(c);hitfirst=true; ingrouppart=true;continue;}
		 sb.append(fixGroupPart(lit.toString())).append(c);lit.setLength(0);
		 continue;
	 }
	 if (!hitfirst)
	 {sb.append(c);continue;}
	 lit.append(c);
 }
 if (lit.length() > 0) {sb.append(fixGroupPart(lit.toString())); lit.setLength(0);}
 //System.out.println("s("+s+")::sb("+sb.toString()+")");
 return sb.toString();
}

public String fixGroupPart(String grpie)
{
	if (is_in(grpie))
	{
		grpie =get(grpie).asString();
	}
	return grpie;
}
	 
 
public VarContainer createGlobalContainer()
{
VarContainer vars=new VarContainer(OUT);
vars.setSameLevelActionContainer(same_level);
vars.setSameLevelThingTypeContainer(same_level_ttc);

Object[] o = hm.values().toArray();
Var[] vs = new Var[o.length];
System.arraycopy(o,0,vs,0,o.length);
for (int i=0; i<vs.length;i++)
	{
	if (vs[i].isGlobal()) {vars.add(vs[i]);}
	}
return vars;
/*create copy of self only containing global vars*/
}
/*method from failed fix VarContainer that succeeded with s:x replace*/
public static String processCharInString(String s, int index, Var v)
{
	//System.out.println("vtype=="+v.getType()+", index =="+index+", s=="+s);
	if (v.getType() != Var.CHR) {return s;}
	char c= v.getChar();
	StringBuffer sb = new StringBuffer(s.length());
	for (int i=0; i<s.length();i++)
	{
		if (i == index) {sb.append(c);}
		else {sb.append(s.charAt(i));}
	}
	return sb.toString();
}
}
