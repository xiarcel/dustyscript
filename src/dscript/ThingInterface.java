package dscript;
import java.io.InputStream;

public class ThingInterface{


private String interface_name;
private String[] actionhashes;

private ThingInterface(String interface_name, String[] actionhashes)
{
this.interface_name=interface_name;
this.actionhashes=actionhashes;
}

public String getName()
{
	return interface_name;
}

public String[] getHashes()
{
	return actionhashes;
}
public boolean does_implement(ActionContainer ac)
{
	for (int i = 0; i<actionhashes.length;i++)
	{
		if (!ac.is_in_hash(actionhashes[i]))
		{
		//System.out.println("Failed at actionhash:"+actionhashes[i]);
		return false;}
	}
	return true;
}
public boolean does_implement(Thing t)
{
	if (t == null) {return false;}
	ActionContainer ac = t.getActionContainer();
	return does_implement(ac);
}

private static String getInline(Statement s,String[] parts)
{
	String part = parts[parts.length-1];
	if (!part.startsWith("inline_code%")) {return "";}
	String[] inlines = s.getInline();
	String pt="";
	for (int i=0; i<part.length();i++)
	{
		char c = part.charAt(i);
		if (Character.isDigit(c))
		{pt = pt + c;}
	}
	String ret =null;
	try {ret = inlines[Integer.parseInt(pt)];} catch(Exception e) {ret = "";}
	return ret;
}
public static String[] interfaces(String[] parts, int index)
{
	String[] ret =new String[parts.length-index-1];
	System.arraycopy(parts,index,ret,0,ret.length);
	for (int i=0; i<ret.length;i++)
	{
		if (ret[i].endsWith(",")) {ret[i]=ret[i].substring(0,ret[i].length()-1);}
	}
	return ret;
}

public static boolean createInterface(Statement s, ThingTypeContainer ttc, Output OUT, InputStream IN) 
{
String[] parts = s.getParts();
if (parts.length < 3) {return false;}
String NAME = parts[1];
String BODY = getInline(s,parts);
if (!ttc.tempAddInterface(NAME)) {return false;}
ActionContainer our_interfaces =new ActionContainer(OUT);
VarContainer our_vc =new VarContainer(OUT);
StatementProcessor sproc =new StatementProcessor(BODY,our_vc,our_interfaces,ttc,OUT,IN).suppress().setAsInterface(true).runOnlyActions();
sproc.run();
if (sproc.encounteredError()) {return false;}
Action[] acts = our_interfaces.in();
String[] actions = new String[acts.length];
for (int i=0; i<acts.length;i++)
{
	actions[i] = acts[i].getHash();
}
//ttc.disallow(); //this is sloppy--see same in SP...
return ttc.replace(NAME,new ThingInterface(NAME,actions));
}
	
}
