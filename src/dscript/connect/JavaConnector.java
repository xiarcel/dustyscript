package dscript.connect;
import dscript.*;
import java.lang.reflect.*;
import java.io.InputStream;
import java.util.HashMap;

public class JavaConnector{
	
private static long COUNTER =0;
private long count;
public static final boolean DEBUG=false;

//this refers specifically to the scope that 'ALL_ACTIONS' add was made.	
private ActionContainer actionhooks=null;
private VarContainer dustyables_vars=null;

private Dustyable dusty; private VarContainer vc; private ActionContainer ac;
private ThingTypeContainer ttc; private String action = "";
private Output OUT; private InputStream IN;


public JavaConnector(Dustyable d, VarContainer v, ActionContainer a, ThingTypeContainer t, Output O, InputStream I)
	{
	count = COUNTER; COUNTER++;
	OUT=O; IN=I;
	dusty=d; dusty.setJavaConnector(this);
	vc=v; ac=a; ttc=t;
	dustyables_vars=new VarContainer(OUT);
	dustyables_vars.add(new Var(new VCWrapper(vc)));
	actionhooks=new ActionContainer(OUT);
	}
	
public String getHashValue()
{
	return "java:"+count;
}
	
public void connectAllActions(ActionContainer a)
{
	actionhooks=a;
}

public void setActionHook(String act)
	{
	action = act;
	}

public void addVariableToContainer(Var v)
{
	dustyables_vars.add(v);
}

public void removeVariableFromContainer(Var v)
{
	dustyables_vars.remove(v);
}


public void runProcessor(String command, ActionContainer ac, VarContainer vac)
{
	//if (ac == null) {System.out.println("ac == null");}
	//if (ttc == null) {System.out.println("ttc == null");}
	//if (vac == null) {System.out.println("vac==null");}
	
	StatementProcessor sproc = new StatementProcessor(command,vac,ac,ttc,OUT,IN);
	sproc.suppress().setAttempting(vc.attempting).run();
}

public void runProcessor(String command)
{
	StatementProcessor sproc = new StatementProcessor(command,dustyables_vars,actionhooks,ttc,OUT,IN);
	sproc.suppress().setAttempting(vc.attempting).run();
}
	
public Dustyable getDustyable() {return dusty;}
public VarContainer getVarContainer() {return vc;}
public ActionContainer getActionContainer() {return ac;}
public ThingTypeContainer getThingTypeContainer() {return ttc;}
public Output getOutput(){return OUT;}
public InputStream getInputStream(){return IN;}
public void sendActionMessage(String s)
	{
	int rndy = 10000+(int)(Math.random()*9999);
	String vr = rndy+"_"+action;
	String mssg = action+ " using "+vr+";";
	Var v = new Var(s,vr);
	vc.add(v);
	StatementProcessor dispatch = new StatementProcessor(mssg,vc,ac,ttc,OUT,IN);
	dispatch.suppress();
	dispatch.run();
	vc.remove(v);

	}

public boolean sendCommand(String command)
	{
	return dusty.processCommand(command);
	}

public boolean sendCommand(String command, String[] args)
	{
	return dusty.processCommand(command,args);
	}

public boolean sendDustyable(Dustyable d)
	{
	boolean debug=false;	
	if ((DEBUG||debug) && d != null)
	{
		System.err.println("sending dustyable : "+d.getClass());
	}
	if (DEBUG && dusty != null)
	{
		System.err.println("dustyable being sent to : "+dusty.getClass());
	}
	
	boolean b=dusty.processDustyable(d);
	if (DEBUG) {
		System.err.println("dusty.sendDustyable(d) returns:"+b);
	}
	return b;
	}

public boolean sendDustyable(Dustyable d, String[] args)
	{
	boolean debug=false;//true;
	if ((DEBUG||debug) && d != null)
	{
		System.err.println("sending dustyable (with args) : "+d.getClass());
		for (int j=0;j<args.length;j++) { System.err.println("sendDustyable(d,args):args["+j+"]=="+args[j]);}
	}
	if (DEBUG && dusty != null)
	{
		System.err.println("dustyable being sent to (with args) : "+dusty.getClass());
	}
	boolean b= dusty.processDustyable(d,args);
	if (DEBUG) {
		System.err.println("dusty.processDusty(d) - with args, returning:"+b);
	}
	return b;
	}
	
public boolean sendVariable(Var v)
{
	return dusty.processVar(v);
}

public boolean sendVariable(Var v, String[] args)
{
	return dusty.processVar(v,args);
}

public boolean getVariable(Var setvar)
{
	Var v = dusty.getVar();
	if (v == Dustyable.NOT_THERE)
	{
		return false;
	}
	setvar.setValue(v);
	return true;
}

public boolean getVariable(Var setvar,String[] args)
{
	Var v = dusty.getVar(args);
	if (v == Dustyable.NOT_THERE)
	{
		return false;
	}
	setvar.setValue(v);
	return true;
}


public static boolean initialize(String varname, VarContainer vc, ActionContainer ac, ThingTypeContainer ttc, Output OUT, InputStream IN)
	{
	JavaConnector njc =new JavaConnector(new Dustyable(),vc,ac,ttc,OUT,IN);
	if (vc.is_in(varname))
		{
		vc.get(varname).setValue(njc);
		}
		else
			{vc.add(new Var(njc,varname));}

	/*
	Var v=new Var(new JavaConnector(new Dustyable(),vc,ac,ttc,OUT,IN),varname);
	if (vc.is_in(varname)) {
	vc.replace(varname,v);
	}
	else {vc.add(v);}
	*/

	if (StatementProcessor.USER_DEBUG){vc.getOutput().println("initialized a JavaConnector named "+varname);}
	return true;
	}

private static HashMap classes = new HashMap();

public static boolean connect (String varname, String classname, VarContainer vc, ActionContainer ac, ThingTypeContainer ttc, Output OUT, InputStream IN)
	{
	if (classname.startsWith("\"")){classname=classname.substring(1,classname.length()-1);}
	try{
		Class c;
		if(classes.containsKey(classname)) {
			c = (Class)classes.get(classname);
		} else {
			c = Class.forName(classname);
			classes.put(classname, c);
		}
		Object o = c.newInstance();
		Dustyable d = null;
		if (o instanceof Dustyable) {d = (Dustyable)o;}
		else{
			d = new Dustyable((Dustializable)o);
		}
		JavaConnector jc = new JavaConnector(d,vc,ac,ttc,OUT,IN);
		Var v=new Var(jc,varname);
		if (vc.is_in(varname)){
			vc.replace(varname,v);
			}
		else{vc.add(v);}
		if (StatementProcessor.USER_DEBUG) {vc.getOutput().println("Connected "+varname+" to "+classname);}
		return true;
	   }
		catch(Exception e)
			{
			vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,classname+" does not seem to be Dustyable:"),vc,vc.attempting);
			//e.printStackTrace();
			}
	return false;
	}

public static boolean hook (String varname, String action, VarContainer vc)
	{
	if (vc.is_in(action)){action = vc.get(action).asString();}
	if (!vc.is_in(varname)) {vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,
			varname+" is not a defined Dustyable"),vc,vc.attempting); 
			return false;}
	if (action.startsWith("\"")){action=action.substring(1,action.length()-1);}
	Var v = vc.get(varname);
	if (v.getType()!= Var.JAVA)
		{vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,varname+" is not a JavaConnector"),vc,vc.attempting);
		return false;}

	try{	
	JavaConnector jc = v.getJavaConnector();
	jc.setActionHook(action);
	if (StatementProcessor.USER_DEBUG) {vc.getOutput().println("Hooked "+action+" to "+varname);}
	return true;
	}
	catch(Exception e)
		{vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"(weird!):Not a JavaConnector but reporting as so"),vc,vc.attempting);}
	return false;

	}


public static boolean sendDustyVar (String varname,VarContainer vc,String jcname)
	{
		if (DEBUG)
		{
			System.err.println("sendDustyVar("+varname+",vc,"+jcname);
		}
	if (!vc.is_in(jcname))
		{vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,jcname+
				" is not a java-connector"),vc,vc.attempting);
		return false;}
	if (!vc.is_in(varname))
		{vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,varname+
			" is not an assigned variable"),vc,vc.attempting);
		return false;}
	Var v = vc.get(varname);
	Var jc = vc.get(jcname);
	if (jc.getType()!=Var.JAVA) 
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Cannot send to a non JavaConnector"),vc,vc.attempting); 
		return false;
		}
	if (v.getType()!=Var.JAVA) 
		{
		 vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Cannot send non JavaConnector to JavaConnector"),vc,vc.attempting);
		 return false;
		}
	try{
	JavaConnector sendto = jc.getJavaConnector();
	Dustyable sendit = v.getJavaConnector().getDustyable();
	boolean st = sendto.sendDustyable(sendit);
	if (StatementProcessor.DEBUG)
		{
		vc.getOutput().println("sent "+varname+"("+sendit.getClass().getName()+") to "+jcname+"("+sendto.getDustyable().getClass().getName()+"):"+st,2);
		}   
	return st;
	}
	  catch(Exception e)
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Some error in sending "+varname+
				" to "+jcname),vc,vc.attempting);
		}
	return false;
	}

public static boolean sendDustyVar (String varname, VarContainer vc, String jcname, String[] args)
	{
	boolean debug=false;//true;
	if (debug) 
	{
		for (int i=0;i<args.length;i++)
		{
		System.err.println("JavaConnector.sendDustyVar(Str,VC,Str,Str[] args):args["+i+"]=="+args[i]);
		}
	}
	if (!vc.is_in(jcname))
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,jcname+" is not a java-connector"),
					vc,vc.attempting);
		return false;
		}
	if (!vc.is_in(varname))
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,varname+" is not an assigned variable"),
					vc,vc.attempting);
		return false;
		}
	Var v = vc.get(varname);
	Var jc = vc.get(jcname);
	if (jc.getType()!=Var.JAVA) 
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Cannot send to a non JavaConnector"),
						vc,vc.attempting); 
		return false;
		}
	if (v.getType()!=Var.JAVA) 
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Cannot send non JavaConnector to JavaConnector"),
				vc,vc.attempting); 
		return false;
		}
	try{
	JavaConnector sendto = jc.getJavaConnector();
	Dustyable sendit = v.getJavaConnector().getDustyable();
	return sendto.sendDustyable(sendit,args);
	   }
	  catch(Exception e)
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,
			"Some error in sending "+varname+" to "+jcname),vc,vc.attempting);
		}
	return false;
	}

public static boolean sendCommand (String jcname, VarContainer vc, String command)
	{
	if (vc.is_in(command)){command=vc.get(command).asString();}
	if (command.startsWith("\"")){command=command.substring(1,command.length()-1);}

	if (!vc.is_in(jcname)) {vc.getOutput().println("dserror>> cannot send a command to non-existant variable",1); return false;}
	Var v = vc.get(jcname);
	if (v.getType()!=Var.JAVA) 
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Cannot send a command to Non-JavaConnector"),
				vc,vc.attempting); 
		return false;
		}
	try{
	JavaConnector jc = v.getJavaConnector();
	return jc.sendCommand(command);
	   }
	   catch(Exception e)
		{
			
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Could not send "+command+" to "+jcname),
		vc,vc.attempting);
		e.printStackTrace();
		}
	return false;
	}

public static boolean sendCommand (String jcname, VarContainer vc, String command, String[] args)
	{
	if (vc.is_in(command)){command=vc.get(command).asString();}
	if (command.startsWith("\"")){command=command.substring(1,command.length()-1);}

	if (!vc.is_in(jcname)) 
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Cannot send a command to non-existant variable"),
				vc,vc.attempting); 
		return false;
		}
	Var v = vc.get(jcname);
	if (v.getType()!=Var.JAVA) 
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Cannot send a command to Non-JavaConnector"),
					vc,vc.attempting); 
		return false;
		}
	try{
	JavaConnector jc = v.getJavaConnector();
	return jc.sendCommand(command,args);
	   }
	   catch(Exception e)
		{
		vc.getOutput().println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"Could not send "+command+" to "+jcname),
				vc,vc.attempting);
		}
	return false;
	}
	
public static boolean sendVariable(String vname, VarContainer vc, String jcname, String[] args)
{	
	
	/*
	for (int z =0; z<args.length;z++)
	{
		if(args[z].endsWith(","))
		{
			args[z]=args[z].substring(0,args[z].length()-1);
		}
		
	}
	*/
	
	Output OUT = vc.getOutput();
	if (!vc.is_in(vname))
	{
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"'"+vname+"' does not appear to exist"),vc,vc.attempting);
		return false;
	}
	if (!vc.is_in(jcname))
	{
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"'"+jcname+"' does not appear to exist"),vc,vc.attempting);
		return false;
	}

	JavaConnector javaconnect = null;
	Var jcin = vc.get(jcname);
	javaconnect = jcin.getJavaConnector();
	if ((jcin.getType()  != Var.JAVA)|| (javaconnect == null))
	{
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.JAV, "'"+jcname+"' exists, but is not a JavaConnector"),vc,vc.attempting);
		return false;
	}
	Var tosend = vc.get(vname);
	if (args.length > 0)
	{
		return javaconnect.sendVariable(tosend,args);
	}
	
	return javaconnect.sendVariable(tosend);
}

public static boolean getVariable(String vname, VarContainer vc, String jcname, String[] args)
{
	//System.out.println("Javaconnector.getVariable() called!");
	/*
	for (int z =0; z<args.length;z++)
	{
		if(args[z].endsWith(","))
		{
			args[z]=args[z].substring(0,args[z].length()-1);
		}
	
	}
	*/
	Output OUT = vc.getOutput();

	if (!vc.is_in(jcname))
	{
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"'"+jcname+"' does not appear to exist"),vc,vc.attempting);
		return false;
	}
	
	JavaConnector javaconnect = null;
	Var jcin = vc.get(jcname);
	javaconnect = jcin.getJavaConnector();
	if ((jcin.getType()  != Var.JAVA)|| (javaconnect == null))
	{
		OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.JAV, "'"+jcname+"' exists, but is not a JavaConnector"),vc,vc.attempting);
		return false;
	}
	Var toget = null; boolean already=false;
	if (vc.is_in(vname))
	{toget = vc.get(vname);
		already=true;
	}
	else{
		toget=new Var(vname,16);
	}
	
	
	boolean ex=false;
	if (args.length > 0)
	{
		ex=javaconnect.getVariable(toget,args);
	}
	else {ex=javaconnect.getVariable(toget);}
	
	if (ex && !already)
	{vc.add(toget);}
	return ex;
}

public static boolean hookAll(ActionContainer act, String jcon, VarContainer vc)
{
	if (!vc.is_in(jcon)){return false;}
	try{
		vc.get(jcon).getJavaConnector().connectAllActions(act);
	return true;
	}
	catch (Exception e){}
	return false;
}


public static JavaConnector copy(JavaConnector jc) throws Exception
	{
	boolean flag = false;
 	if (flag) {return jc;}
	Dustyable dustyable = jc.getDustyable();
	Class c = dustyable.getClass();
	Object o = c.newInstance();
	Dustyable nd= null;
	if (o instanceof Dustyable){nd = (Dustyable)o;}
	else 
	{
		nd = new Dustyable((Dustializable)o);
	}
	JavaConnector njc = new JavaConnector(nd,jc.getVarContainer(),jc.getActionContainer(),
				jc.getThingTypeContainer(), jc.getOutput(), jc.getInputStream());
	if (StatementProcessor.DEBUG) {jc.getOutput().println("->deep copy of javaconnector succeeded",2);}
	
	return njc;
	}

}
