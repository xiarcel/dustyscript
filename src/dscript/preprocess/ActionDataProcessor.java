package dscript.preprocess;
import dscript.*;
import java.io.InputStream;


class ActionDataProcessor{

public static final String TCL= ">";
public static final String STOPE = "<";
public static final String ETOPE = "</";
public static final String LS = System.getProperty("line.separator");
public static final String ARG = "ARG";
public static final String AT = "ATP";
public static final String VN = "VNM";
public static final String GV = "GIVE";
public static final String NM = "NAME";
public static final String ACT= "ACTION";
public static final String GLB= "GLOBAL";
public static final String SRC= "SRC";
public static final String RS = "RS";

public static String getStartTag(String key)
{
return STOPE+key+TCL;
}

public static String getEndTag(String key)
{
return ETOPE+key+TCL;
}

public static String makeActionData(ActionContainer act)
{
Action[] actions = act.in();
StringBuffer sb= new StringBuffer(1000);

for (int i=0; i<actions.length; i++)
	{
	sb.append(LS).append(getStartTag(ACT)).append(LS).append('\t');

	int[] args = actions[i].getArgs();
	int give = actions[i].getReturnType();
	String name = actions[i].getName();
	boolean global = actions[i].isGlobal();
	String[] varnames = actions[i].getInternalVarNames();
	String src = SourceDataProcessor.makeSourceData(actions[i].getStatement());
	String rstring = actions[i].getReturnString();
	sb.append(getStartTag(NM)).append(name).append(getEndTag(NM)).append(LS).append('\t');
	
	for (int j=0; j<args.length; j++)
		{
		sb.append(getStartTag(ARG)).append(getStartTag(AT)).append(args[j]).append(getEndTag(AT)).append(LS).append('\t');
		sb.append(getStartTag(VN)).append(varnames[j]).append(getEndTag(VN)).append(getEndTag(ARG)).append(LS).append('\t');
		}
	sb.append(getStartTag(GV)).append(give).append(getEndTag(GV)).append(LS).append('\t');
	sb.append(getStartTag(GLB)).append(global).append(getEndTag(GLB)).append(LS).append('\t');
	sb.append(getStartTag(RS)).append(rstring).append(getEndTag(RS)).append(LS).append('\t');
	sb.append(getStartTag(SRC)).append(src).append(getEndTag(SRC)).append(LS);
	sb.append(getEndTag(ACT)).append(LS);
	}
return sb.toString();
}

public static int[] makeArgArray(String[] argg)
{
 int[] args = new int[argg.length];
 for (int i=0; i<args.length;i++)
	{
	args[i] = makeArg(argg[i]);
	}
 return args;
}

public static int makeArg(String arg)
{
	return Integer.parseInt(arg);
}

public static boolean isGlobal(String bool)
{
return (bool.equals("true")||(bool.indexOf("true")>-1));
}

public static ActionContainer makeActionContainer(String act_source, Output OUT, InputStream IN)
{
	LineSplit line = new LineSplit();
	String[] actions = line.split(act_source);
	ActionContainer act = new ActionContainer(OUT);
	for (int i=0; i<actions.length; i++)
	{
		String name = line.ssplit(NM,actions[i]);
		String ars = line.ssplit(ARG,actions[i]);
		int[] args = makeArgArray(line.split(AT,ars));
		String[] ivn = line.split(VN,ars);
		int give = makeArg(line.ssplit(GV,actions[i]));
		String src = line.ssplit(SRC,actions[i]);
		String rstring = line.ssplit(RS,actions[i]);
		boolean global = isGlobal(line.ssplit(GLB,actions[i]));
		//below needs changing
		Action a = new Action(name,args,ivn,new String[0],give,src,rstring,act,OUT,IN);
		//above needs changing
		if (global){a.setAsGlobal();}
		act.add(a);
	}
	return act;
}

}

