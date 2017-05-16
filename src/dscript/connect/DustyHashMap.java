package dscript.connect;

import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import dscript.*;

public class DustyHashMap extends Dustyable
{
	private boolean defined = false;
	private HashMap hm;
	private Var var, key;
	private Var WRAPPER=null;
	private VarContainer OUTER;
	private ActionContainer THING_ACT;
	
	//private Var capacity;
	private Var size=new Var(0,"size");
	private Var containskey=new Var(false,"containskey");
	
	public DustyHashMap()
	{	
		//System.out.println("DustyHashMap:"+(this instanceof Dustyable));
		hm=new HashMap();
		//capacity = new Var(hm.capacity(),"capacity");
		//OUTER = new VarContainer(getJavaConnector().getOutput());	
	}
	public boolean processVar(Var v)
	{
		if (v.getType() == Var.THING)
		{
			WRAPPER = v;

			return true;
		}
		return false;
	}
	
	public boolean processVar(Var v, String[] args)
	
	{
		if (!defined && !define()){return false;}
		
		if (args.length ==1 && args[0].equals("remove"))
		{
			if (hm.containsKey(v.getHashValue()))
			{
				hm.remove(v.getHashValue());
			}
			return true;
		}
		return false;
	}
	
	public boolean define()
	{
		if (WRAPPER == null)
		{return false;}
		defined = true;
		if (OUTER == null)
		{
			OUTER = new VarContainer(getJavaConnector().getOutput());
			OUTER.add(new Var(new VCWrapper(WRAPPER.getThing().getVarContainer())));
			OUTER.add(var);
			OUTER.add(key);
			THING_ACT = WRAPPER.getThing().getActionContainer();
		}
		return true;
	}
	
	
	public boolean processCommand(String command)
	{
		if (!defined && !define()) {return false;}
		if (command.equals("clear"))
		{
			hm.clear();
			return true;
		}
		else if (command.equals("size"))
		{
			getJavaConnector().runProcessor("int_setSize using "+hm.size()+";",THING_ACT,OUTER);
			return true;
		}
		else 
		{
		var = new Var("_VALUE_",16); key =new Var("_KEY_",16);
		if (OUTER.is_in_local(var)) {OUTER.replace("_VALUE_", var);} else {OUTER.add(var);}
		if (OUTER.is_in_local(key)) {OUTER.replace("_KEY_",key);} else {OUTER.add(key);}
		
		if (command.equals("get"))
		{
			getJavaConnector().runProcessor("_KEY_ is now getGetKey;",THING_ACT,OUTER);
			//System.out.println("key.getHashValue():"+key.getHashValue());
			String hv = key.getHashValue();
			
			if (hm.containsKey(hv))
			{	
				var.setValue((Var)hm.get(hv));
				getJavaConnector().runProcessor("int_setGet using _VALUE_;",THING_ACT,OUTER);
				return true;
			}
			else {
				var.setValue(Dustyable.NOT_THERE);
				getJavaConnector().runProcessor("int_setGet using _VALUE_;",THING_ACT,OUTER);
				return true;
			}
		}
		else if (command.equals("put"))
		{
			getJavaConnector().runProcessor("_KEY_ is now getPutKey; _VALUE_ is now getPut;",THING_ACT,OUTER);
			//System.out.println("getHashValue()-put:"+key.getHashValue());
			//System.out.println("getType()-put:"+key.getType());
			//System.out.println("getAbsoluteType()-put:"+key.getAbsoluteType());
			hm.put(key.getHashValue(),var);
			return true;
		}
		else if (command.equals("contains"))
		{
			getJavaConnector().runProcessor("_KEY_ is now getContainsKey");
			getJavaConnector().runProcessor("int_setContains using "+ckey(key.getHashValue())+";",THING_ACT,OUTER);
			return true;
		}
		else if (command.equals("setvalues"))
		{
			Object[] o = hm.values().toArray();
			//System.out.println("o.length=="+o.length);
			Var[] v = new Var[o.length];
			System.arraycopy(o,0,v,0,o.length);
			VarGroup vg =new VarGroup("_VALUES_",16,getJavaConnector().getVarContainer());
			for (int i=0; i<v.length;i++)
			{
				String inx = "" + (i+1);
				Var put = new Var(inx,v[i].getType());
				put.setValue(v[i]);
				vg.add_internal(inx,put);
			}
			var =new Var(vg, "_VALUES_");
			if (OUTER.is_in_local(var)) {OUTER.replace("_VALUES_",var);} else {OUTER.add(var);}
			
			getJavaConnector().runProcessor("int_setValues using _VALUES_;",THING_ACT,OUTER);
			return true;
		}
		else if (command.equals("setkeys"))
		{
			VarGroup vg=new VarGroup("_VALUES_",16,getJavaConnector().getVarContainer());
			Iterator i = hm.keySet().iterator();
			int count =1;
			while (i.hasNext())
			{
				String ky = (String)i.next();
				//System.out.println("setkeys():ky=="+ky+":count=="+count);
				String cnt = ""+count;
				vg.add_internal(cnt, new Var(ky,cnt));
				count++;
			}
			var =new Var(vg, "_VALUES_");
			if (OUTER.is_in_local(var)) {OUTER.replace("_VALUES_",var);} else {OUTER.add(var);}
			getJavaConnector().runProcessor("int_setKeys using _VALUES_;",THING_ACT,OUTER);
			return true;
		}
				
	
	        }
		return false;
	}

	private String ckey(String ky)
	{
		return hm.containsKey(ky)? "YES" : "NO";
	}
	

}

						
