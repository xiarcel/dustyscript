package dscript;

import java.util.HashMap;
import java.io.InputStream;

public class ThingTypeContainer{

private Output OUT; private InputStream IN;
private HashMap things;
private HashMap INTERFACES;
private HashMap ALLOW=new HashMap();

public ThingTypeContainer(Output O, InputStream I)
{
	INTERFACES=new HashMap();
	things=new HashMap();
OUT=O; IN=I;
things.put("~",new ThingType("~","",this,OUT,IN));
}

public void allow(String allowed)
{
	ALLOW.put(allowed,null);
}

public boolean allowed(String allowed)
{
	return ALLOW.containsKey(allowed);
}

public void disallow()
{
	ALLOW.clear();
}

public void disallow(String s)
{
	ALLOW.remove(s);
}

public boolean tempAddInterface(String iname)
{
	if (!INTERFACES.containsKey(iname)) {INTERFACES.put(iname,null);return true;}
	return false;
}

public boolean tempAddThingType(String tname)
{
	if (!things.containsKey(tname)) {things.put(tname,null); return true;}
	return false;
}

public boolean replace(String name, Object o)
{
	if (o instanceof ThingType)
	{
		if (things.containsKey(name) && (things.get(name)==null)) {things.remove(name); things.put(name,o);return true;}
		else if (things.containsKey(name)) {return false;}
		else{add((ThingType)o);}
		return true;
	}
	else if (o instanceof ThingInterface)
	{
		if (INTERFACES.containsKey(name) && (INTERFACES.get(name)==null)) {INTERFACES.remove(name); INTERFACES.put(name,o);return true;}
		else if (INTERFACES.containsKey(name)) {return false;}
		else {addInterface(name,(ThingInterface)o);}
	}
	return false;
}
public boolean addInterface(String iname, ThingInterface ti)
{
	if (INTERFACES.containsKey(iname)||things.containsKey(iname)) 
		{
		//put in OUT.println(...)--error
		return false;
		}
	INTERFACES.put(iname,ti);
	return true;
}
public boolean containsInterface(String iname)
{
	return INTERFACES.containsKey(iname);
}

public ThingInterface getInterface(String iname)
{
return INTERFACES.containsKey(iname)? ((ThingInterface)INTERFACES.get(iname)) : null;
}

public ThingInterface[] getAllInterfaces()
{
	Object[] o = INTERFACES.values().toArray();
	ThingInterface[] tia = new ThingInterface[o.length];
	System.arraycopy(o,0,tia,0,tia.length);
	return tia;
}

public void add(ThingType tt)
{
 if (things.containsKey(tt.getName()))
	{
	OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.THG,"'"+tt.getName()+"' not defined, it was defined already"));
	return;
	}
 things.put(tt.getName(),tt);

 }

public ThingType get(String name)
{
 if (things.containsKey(name)) {return (ThingType)things.get(name);}
 return new ThingType("","",this,OUT,IN);
}

public boolean is_valid_interface(String name)
{
	return INTERFACES.containsKey(name);
}

public boolean is_in(String name)
{
 return (things.containsKey(name));
}

public ThingType[] in()
{
 Object[] o = things.values().toArray();
 ThingType[] toret=new ThingType[o.length];
 System.arraycopy(o,0,toret,0,o.length);
 return toret;
}

public void dump()
{
	ThingType[] tt=in();
	for (int i=0; i<tt.length; i++)
	{System.out.println(tt[i].getName());}
}

public InputStream getInputStream()
{
	return IN;
}


}
