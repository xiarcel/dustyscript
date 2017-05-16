package dscript;

import java.util.Vector;


class Equation
{

private static long ectr=0;
private String[] strings = new String[0];
private String[] chars = new String[0];


private String data,name;
private VarContainer vc;
private ActionContainer ac; private ActionContainer bak=null;
private MathParser mp;

public Equation(String n, String d, VarContainer v)
	{
	data=d; name=n; vc=v; ac=vc.getSameLevelActionContainer();
	}


public double asDecimal()
	{
	
	
         String[] repcands=replaceCandidates(data);
	 //if (true) {for (int x=0; x<repcands.length;x++){System.out.println(repcands[x]+"==rc");}}
	 
	 data= replace(repcands, data);
	 
	 mp=new MathParser(data,vc);
	 try{
		 double d = Double.parseDouble(mp.evaluate());
		return d;
	 }
	 catch(Exception e){e.printStackTrace();}
	 return -1.0;
	}

public void setLiterals(String[] s, String[] c)
{
if (s == null) {strings=new String[0];}
	else{strings = s;}

if (chars == null) {chars=new String[0];}
	else{chars=c;}

}

public void replaceAnyLiterals()
{/*see if this is the cause*/


/*replace the literals on request... important for complex if stuff*/
for (int i=0; i<chars.length; i++)
	{
	String srch = "&"+i+"&";
	String rep =""+chars[i];
	if (!rep.startsWith("'")){rep="'"+rep;}
	if (!rep.endsWith("'")){rep=rep+"'";}
	data = Statement.replace(data,srch,rep);
	}
for (int i=0; i<strings.length; i++)
	{
	String srch = "$"+i+"$";
	String rep = ""+strings[i];
	if (!rep.startsWith("\"")) {rep="\""+rep;}
	if (!rep.endsWith("\"")) {rep=rep+"\"";}
	data = Statement.replace(data,srch,rep);
	}

}



private String[] replaceCandidates(String src)
	{
	Vector v = new Vector();
	String cand =""; boolean in_cand=false;

	for (int i=0; i<src.length(); i++)
		{
		char c = src.charAt(i);
		if (checkChar(c))
			{
			if (in_cand)
				{in_cand=false;
				 if (!cand.equals("")){v.addElement(cand);}
			 	 cand="";
				}
			continue;
			}
			else{
			    in_cand=true;
			    cand=cand+c;
			    }
		}
		if (!cand.equals("")){v.addElement(cand);}

	String[] ret=new String[v.size()];
	for (int j=0; j<ret.length; j++)
		{
		ret[j]=(String)v.elementAt(j);
		}
	return ret;
	}

private boolean checkChar(char c)
	{
	if ((c=='(')||(c==')')||(c=='%')||(c=='&')||(c=='^')||(c=='+')||(c=='-')||(c=='*')||(c=='/')||/*(c=='~')|*/(c=='!')||(c==' ')||(c=='\n'))
		{return true;}
	return false;
	}

private String replace(String cand, String vstring, String src)
	{
	int indx = src.indexOf(cand);
	if (indx < 0) {return src;}
	String rep = src.substring(0,indx);
	indx = indx + cand.length();
	rep = rep +vstring;
	rep = rep +src.substring(indx,src.length());
	return rep;
	}

private String replace(String[] cands, String src)
	{
	for (int i=0; i<cands.length; i++)
		{
		if (vc.is_in(cands[i]))
			{
			//System.err.println("cands["+i+"]:"+cands[i]);
			Var v =vc.get(cands[i]);
			String torep="";
			boolean thingrepped=false;
			if (v.getType()==Var.THING)
				{
				ActionContainer acty=v.getThing().getActionContainer();
				if (acty.is_in("toDecimal",new int[]{-1},2))
					{
					torep=""+v.getDecimal();
					thingrepped=true;
					}
				}
			if (!thingrepped){torep = v.asString();}
			src = replace(cands[i],torep,src);
			continue;
			}
		
		if (cands[i].indexOf(".")>-1)
		{
			int cd = cands[i].indexOf(".");
			String tg= cands[i].substring(0,cd);
			cands[i] = cands[i].substring((cd+1),cands[i].length());
			if(vc.is_in(tg))
			{
				bak=ac;
				try {ac=vc.get(tg).getThing().getActionContainer();}
				catch(Exception e) {ac=bak;continue;}
			}
		}
		if (ac.is_in(cands[i]))
		{
			String vr = "__eq_ac_"+ectr; ectr++;
			StringBuffer sb =new StringBuffer(vr).append(" is_now ").append(cands[i]);
			int j=i+1;
			String candy=cands[i];
			for (; j<cands.length;j++)
			{
				src=replace(cands[j],"",src);
				try {if (cands[j-1].equals(candy) && cands[j].equals("~"))
					{break;}
				}
				catch (Exception e){}	
				sb.append(' ').append(cands[j]);
				if (cands[j].equals("~")){break;}
			}
			sb.append(';');
			Var v =new Var(vr,Var.DEC);
			vc.add(v);
			ThingTypeContainer ttc= vc.getSameLevelThingTypeContainer();
			StatementProcessor sp = new StatementProcessor(sb.toString(),vc,ac,ttc,vc.getOutput(),ttc.getInputStream());
			sp.suppress();
			sp.setAttempting(vc.attempting);
			sp.run();
			v = vc.get(vr);
			src = replace(cands[i],v.asString(),src);
			v.setAsTransient(); v.setUsedBit(true);
			vc.remove(v);
			if (bak !=null) {ac=bak;bak=null;}
			i=j; continue;
		    }		
			
		}
	return src;
	}


public String getData(){return data;}


}
