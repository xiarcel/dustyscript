package dscript;
import dscript.connect.JavaConnector;

public class Thing{

public static final Thing UNDEF = new Thing("thing",">>NULL<<",new ActionContainer(Output.DEF),new VarContainer(Output.DEF));
private static long COUNTER=0;
private long count;
private String ttype,name;
private ActionContainer ac;
private VarContainer vc;
private ThingType thingtype;
private final static int[] narg = new int[]{-1};
private final static String tstr = "__tmp__ is now toString;";
private final static String hstr = "__tmp__ is now hashValue;";

private final static String tointstr="__tmpint__ is now toInteger;";
private final static String todecstr="__tmpdec__ is now toDecimal;";
private StatementProcessor woo;
private Var tovar = new Var("","__tmp__");
private Var toint = new Var(0,"__tmpint__");
private Var todec = new Var(0.0,"__tmpdec__");

public Thing(String tp, String n, ActionContainer a, VarContainer v)
{
count = COUNTER; count++;
 vc=v;
 if (a != null)
 {
 ac=a.createSafeCopy(); // probably the best place for this--synchronization issue 
 }
 else {ac=a;}
 
 name=n; 
 ttype=tp;
// vc.add(new Var(this,"me"));
}

public void setName(String s){name=s;}

public void setThingTypeGlobal(ThingType tt)
{thingtype=tt;}
public ThingType getThingTypeGlobal()
{return thingtype;}
public Thing setThingType(String s)
{
	ttype=s;
	return this;
}
public String getThingType(){return ttype;}
public String getName(){return name;}
public ActionContainer getActionContainer(){return ac;}
public VarContainer getVarContainer(){return vc;}


public String getHashValue()
{
	if (ac.is_in("hashValue",narg,4))
	{
		woo =new StatementProcessor(hstr,vc,ac,vc.getSameLevelThingTypeContainer(),vc.getOutput(),System.in);
		if (!vc.is_in("__tmp__")) {vc.add(tovar);}
		woo.suppress().setAttempting(vc.attempting).run();
		return vc.get("__tmp__").asString();
	}
	return name+":"+ttype+":"+count;
}

public String getToString()
{
	if ((ac != null) && ac.is_in("toString",narg,4)){
		woo = new StatementProcessor(tstr,vc,ac,vc.getSameLevelThingTypeContainer(),vc.getOutput(),System.in);
		woo.suppress();
		if (!vc.is_in("__tmp__")){vc.add(tovar);}
		woo.run();
		return vc.get("__tmp__").asString();
	}
	return getName();
}

public double getToDecimal()
{
	if ((ac!=null) && (ac.is_in("toDecimal")))
	{
		woo=new StatementProcessor(todecstr,vc,ac,vc.getSameLevelThingTypeContainer(),vc.getOutput(),System.in);
		woo.suppress();
		if (! vc.is_in("__tmpdec__"))
		{
			vc.add(todec);
		}
		woo.run();
		return vc.get("__tmpdec__").getDecimal();
	}
	return 0.0;
}

public long getToInteger()
{
	if ((ac!=null) && (ac.is_in("toInteger")))
	{
		woo=new StatementProcessor(tointstr,vc,ac,vc.getSameLevelThingTypeContainer(),vc.getOutput(),System.in);
		woo.suppress();
		if (! vc.is_in("__tmpint__"))
		{
			vc.add(toint);
		}
		woo.run();
		return vc.get("__tmpint__").getInteger();
	}
	return 0L;
}	



public static Thing copy(Thing orig, String newname)
	{
	
	Var[] vars= orig.getVarContainer().in();
	VarContainer cpy = new VarContainer(orig.getVarContainer().getOutput());
	for (int i=0; i<vars.length;i++)
		{
		if (vars[i].getType()==Var.THING)
			{
			Thing ting = vars[i].getThing();
			if (ting == orig) {continue;}
			cpy.add(new Var(Thing.copy(ting,ting.getName()),newname));
			continue;
			}
		if (vars[i].getType()==Var.JAVA)
			{
			JavaConnector jav = vars[i].getJavaConnector();
			try{
			   cpy.add(new Var(JavaConnector.copy(jav),newname));
			   continue;
			    }
				catch(Exception e)
					{/*next line will add it regular*/}
			}
		cpy.add(Var.copy(vars[i],vars[i].getName()));
		}
	Thing thingy = new Thing(orig.getThingType(),newname,orig.getActionContainer(),cpy);
	if (cpy.is_in("me"))
		{
		cpy.replace("me",new Var(thingy,"me"));
		}
		else {cpy.add(new Var(thingy,"me"));}
	thingy.setThingTypeGlobal(orig.getThingTypeGlobal());
	return thingy;

	}

}
