package dscript;

import java.util.HashMap;
import java.util.Vector; /*still needed for non HM stuff*/

public class ActionContainer
{
	/*if a new return-type for actions is added, its int value needs to be added below*/
	final static int[] piggybacks = new int[]{0,1,2,3,4,7,10,11};

	final static Action none =new Action();
    private HashMap hm =new HashMap();
    private HashMap namelookup =new HashMap();
   // private HashMap wildhash = new HashMap();
    public Output OUT;
    private boolean creating_global=false;
    //private final Vector sync_actions=new Vector();
    
    public ActionContainer(Output O)
    {
        OUT=O;
    }
	
   public void setGlobalActionState(boolean b)
	{
	creating_global=b;
	}

    public void remove(Action a)
	{
	String hsh = a.getHash();
	String whsh = a.getWild();
	String nm = a.getName();
	if (hm.containsKey(hsh)){hm.remove(hsh);}
	if (namelookup.containsKey(nm)){namelookup.remove(nm);}
	/*if (wildhash.containsKey(whsh)){wildhash.remove(whsh);}*/
	}


    public void add(Action a)
    {
	    //System.out.println("a-hash == "+a.getHash());
	if (!hm.containsKey(a.getHash())) 
		{
		hm.put(a.getHash(),a);
		namelookup.put(a.getName(),a);
		/*wildhash.put(a.getWild(),a);*/
		if (creating_global) {a.setAsGlobal();}
		}
        
    }
    
    public static String createHash(String name, int[] args, String[] args_alt, int gives)
    {
	  StringBuffer sb=new StringBuffer(50);
	  sb.append(name).append(":args:");
	  for (int i=0; i<args.length;i++)
	  {
		  if (!args_alt[i].equals(""))
		  {
			  sb.append(args_alt[i]);
		  }
		  else {sb.append(args[i]);}
		  sb.append(':');
	  }
	  sb.append("ret:").append(gives);
	  return sb.toString();
    }
    
    
    

	public static String createHash(String name, int[] args, int gives)
	{
	StringBuffer sb=new StringBuffer(50);
	sb.append(name).append(":args:");
	for (int i=0;i<args.length;i++)
		{sb.append(args[i]).append(":");}
	sb.append("ret:").append(gives);
	return sb.toString();
	}
	/*
    public Action getByWild(String name, int[] args, int gives)
	{
	String hash = createWildHash(name,args,gives);
	if (wildhash.containsKey(hash)) {return (Action)wildhash.get(hash);}
	return none;
	}

    public Action getByWild(String name, int[] args)
	{
	for (int i=0; i<piggybacks.length;i++)
		{
		if (inByWild(name,args,piggybacks[i]))
			{
			return getByWild(name,args,piggybacks[i]);
			}
		}
	return none;
	}
*/

    public Action get(String name, int[] args, int gives)
    {
	String hash = createHash(name,args,gives);

	if (hm.containsKey(hash)) {return (Action)hm.get(hash);}
	return none;
    }
/*
    public Action get(String name, int[] args)
    {
	for (int i=0; i<piggybacks.length;i++)
		{
		if (is_in(name,args,piggybacks[i])) {return get(name,args,piggybacks[i]);}
		}
	return none;
    }
    public boolean inByWild(String name, int[] args, int gives)
    {

    return wildhash.containsKey(createWildHash(name,args,gives));
    }	

   public boolean inByWild(String name, int[] args)
    {
    for (int i=0; i<piggybacks.length;i++)
		{
		if (inByWild(name,args,piggybacks[i])) {return true;}
		}
    return false;
    }
*/  
  /*NEWEST!*/
    public boolean is_in(ActionHashIterator ahi)
    {
	    int best = ahi.bestSearch();
	    if (hm.containsKey(ahi.get(best))) {return true;}
	    for (int i=0; i<ahi.getLength(); i++)
	    {
		    if (i == best){continue;}
		    String h = ahi.get(i);
		    if (hm.containsKey(h))
		    {
			    ahi.foundAt(i);
			    return true;
		    }
		   // System.out.println("hm.containsKey("+h+")"+false);
	    }
	    return false;
    }
    
    public Action get(ActionHashIterator ahi)
    {
	    int best = ahi.bestSearch();
	    String bs = ahi.get(best);
	    if (hm.containsKey(bs)) {return (Action)hm.get(bs);}
	    for (int i=0; i<ahi.getLength(); i++)
	    {
		    if (i == best) {continue;}
		    bs = ahi.get(i);
		    if (hm.containsKey(bs)) {ahi.foundAt(i); return (Action)hm.get(bs);}
	    }
	    return none;
    }
    
    public boolean is_in(Action compare)
	{
	return hm.containsKey(compare.getHash());
	}

    public boolean is_in(String name,int[] args, int gives)
    {
	return hm.containsKey(createHash(name,args,gives));
    
    }

   public boolean is_in(String name, int[] args)
	{
	for (int i=0; i<piggybacks.length;i++)
		{
		if (is_in(name,args,piggybacks[i])){return true;}
		}
	return false;
	}

	public boolean is_in_hash(String hashe)
	{
		return hm.containsKey(hashe);
	}
	
	public boolean is_in(String name)
	{
	return is_in(name,false);
	}
	
    public boolean is_in(String name, boolean from_ap)
    {
	    boolean b =namelookup.containsKey(name);
	    if (from_ap) {return b;}
	    return b? b : ActionProcessor.checkSystem(name);
    }

    public Action[] in()
    {
	Object[] o = hm.values().toArray();
        Action[] ret = new Action[o.length];
	  System.arraycopy(o,0,ret,0,o.length);
	  return ret;
    }


    public void dump()
    {
        dump(0);
    }


    public void dump(int xx)
    {
        Action[] ack = in();
        String mssg="";

        for (int i=0; i<ack.length; i++)
        {
            int[] ags = ack[i].getArgs();
            String nm = ack[i].getName();
            int rt = ack[i].getReturnType();
            mssg=mssg+"action "+nm+" takes ";

            for (int j=0; j<ags.length;j++)
            {
                mssg=mssg+ags[j]+", ";
            }

            mssg=mssg+"gives "+rt+"\n";
	    mssg=mssg+"hash for "+nm+":|"+ack[i].getHash()+"\n";
        }

        OUT.println(mssg,xx);
    }

    public Action[] pullConstructors(String constructorname)
    {
        Action[] ack=in();
        Vector voov =new Vector();

        for (int i=0;i<ack.length;i++)
        {

            if (ack[i].getName().equals(constructorname))
            {
                voov.addElement(ack[i]);
            }
        }
	  Object[] o = voov.toArray();
        Action[] constru = new Action[o.length];
	  System.arraycopy(o,0,constru,0,o.length);
       
        return constru;

    }
    /*
 public static String createWildHash(String name, int[] args, int ret)
 {
 return new StringBuffer().append(name).append(":args:").append(args.length).append(":ret:").append(ret).toString();
 }
 */
 

	
public ActionContainer createGlobalContainer()
{
 ActionContainer glob = new ActionContainer(OUT);
 Action[] acts = in(); 
 for (int i=0; i<acts.length;i++)
	{
	if (acts[i].isGlobal())
		{
		glob.add(acts[i]);
		}
	}
 return glob;
 /*returns a copy of self only with global actions*/
}


public ActionContainer createSafeCopy()
{
	ActionContainer newact= new ActionContainer(OUT);
	Action[] acts=in();
	for (int i=0; i<acts.length;i++)
	{
		if (acts[i].getSynchd() && ! acts[i].isGlobal())
		{
			newact.add(acts[i].copySelf());
		}
		else
		{
			newact.add(acts[i]);
		}
	}
	return newact;
}

		

}
