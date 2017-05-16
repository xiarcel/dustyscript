package dscript;
import java.util.HashMap;
import java.util.Vector;


public class ActionHashIterator{
	private static HashMap hm =new HashMap();
	private ThingTypeContainer ttc;
	private String[] arg_alts;
	private int[] args;
	private int ret;
	private String name;
	private String[] hash_array;
	private int last_success=0; /*used to try to keep record of where searches have been successful*/
	public static boolean USE_BEST_MATCH=true;
	
private ActionHashIterator(String nm, int[] ags, String[] a_a, int rt, ThingTypeContainer tt)
{
	/*can only construct a AHI through the static method:
	ActionHashIterator.getActionHashIterator(String, int[], String[], int, ThingTypeContainer);
	*/
	ttc=tt; 
	name=nm; 
	args=ags; 
	arg_alts=a_a;
	ret=rt;
	hash_array = getStringArray(nm,ags,a_a,rt,tt);
}

public int getLength()
{
	return hash_array.length;
}

public int bestSearch()
{
	
	return USE_BEST_MATCH?last_success:(-1);
}

public void foundAt(int i)
{
	if ((i>hash_array.length-1)||(i<0))
	{
	last_success=0;
	}
	else {last_success=i;}
}

public void dump()
{
	System.out.println("Hashes in '"+name+"'");
	for (int i=0; i<hash_array.length;i++)
	{
		System.out.println("["+i+"]:"+hash_array[i]);
	}
}

public String get(int i)
{
	if ((i<0)||(i>hash_array.length-1)){return "";}
	return hash_array[i];
}


private static String getActionHashHash(String nm, int[] ags, String[] a_a, int rt)
{
	StringBuffer sb=new StringBuffer(100);
	sb.append(nm);
	for (int i=0; i<ags.length;i++)
	{
		sb.append(ags[i]).append(a_a[i]);
	}
	sb.append(rt);
	return sb.toString();
}

private static String[] getStringArray(String name, int[] args, String[] args_alt, int ret, ThingTypeContainer tt)
{
	String[][] permutes = getStringArrays(name,args,args_alt,ret,tt);
	String[] rets = Var.getVarAlternates(ret);
	Vector perms=new Vector();
	permute(permutes,perms,name,rets);
	Object[] o = perms.toArray();
	String[] ss = new String[o.length];
	System.arraycopy(o,0,ss,0,o.length);
	return ss;
}

private static void permute(String[][] s, Vector v, String name, String[] rets)
{
	String[] combs = new String[s.length];
	permute(combs,s,v,name,rets,0);
}

private static void permute(String[] combs, String[][] s, Vector v, String name, String[] rets, int i)
{
	if (s.length == i)
	{
		putHashes(name,combs,rets,v);
	}
	else{
		for (int j=0; j<s[i].length;j++)
		{
			combs[i]=s[i][j];
			permute(combs,s,v,name,rets,i+1);
		}
	}
}




private static String[][] getStringArrays(String name, int[] args, String[] args_alt, int ret,  ThingTypeContainer tt)
{
	String[][] bigArray = new String[args.length][];
	for (int i=0; i<args.length;i++)
	{
		if (!args_alt[i].equals(""))
		{
			
			if (args[i] == Var.GROUP) {
				String primary="";
				int aaio=args_alt[i].indexOf(":");
				if (aaio > -1 && aaio !=args_alt.length)
				{
					primary=args_alt[i].substring(0,aaio);
					args_alt[i]=args_alt[i].substring(aaio+1,args_alt[i].length());
					//System.out.println("primary="+primary+"\nargs_alt["+i+"]="+args_alt[i]);
				}
				
				String[] vgga=Var.getGroupAlternates(args_alt[i],tt); 
				if (!primary.equals(""))
				{
					bigArray[i]=new String[vgga.length+1];
					bigArray[i][0]=primary;
					System.arraycopy(vgga,0,bigArray[i],1,vgga.length);
				}
				else
				{
					bigArray[i]=vgga;
				}
				continue;
			}
			else if (args[i] == Var.THING) {bigArray[i] = tt.get(args_alt[i]).getSearchAncestors(); continue;}
		}
		
		bigArray[i] = Var.getVarAlternates(args[i]);
	}
	return bigArray;
}

private static void putHashes(String name, String[] args, String[] rets, Vector v)
{
	for (int i=0; i<rets.length;i++)
	{
		v.addElement(getHash(name,args,rets[i]));
	}
}


public static String getHash(String name, String[] args, String ret)
{
	StringBuffer sb=new StringBuffer(100);
	sb.append(name);
	sb.append(":args");
	for (int i=0; i<args.length;i++)
	{
		sb.append(':').append(args[i]);
	}
	sb.append(":ret:").append(ret);
	return sb.toString();
}



public static ActionHashIterator getActionHashIterator(String nm, int[] ags, String[] a_a, int rt, ThingTypeContainer tt)
{
	/*this is likely to be an expensive object, so if we happen across hashes we've created already..*/
	String ahh = getActionHashHash(nm,ags,a_a,rt);
	if (hm.containsKey(ahh)) {return (ActionHashIterator)hm.get(ahh);}
	ActionHashIterator nw = new ActionHashIterator(nm,ags,a_a,rt,tt);
	hm.put(ahh,nw);
	//if (nm.equals("exec")) {nw.dump();}
	return nw;
}	

}

