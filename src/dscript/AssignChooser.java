package dscript;
import java.io.InputStream;


class AssignChooser
{

     /*mod to make this go away in commits :-) */
    final static int AS = DSOut.ASS;
    final static int ER = DSOut.ERR_OUT;

    public static void dump(String[] a)
    {
	    for (int i=0; i<a.length;i++)
	    {
		    System.out.println("array["+i+"]:"+a[i]);
	    }
    }
    

    public static Var assign(String[] parts, String name, String type, VarContainer vc, Output OUT, InputStream IN, Statement STATE)
    {
	    //dump(parts);
	ThingTypeContainer ttc=vc.getSameLevelThingTypeContainer();
	int tp=-1;
	if (ttc.is_in(type)) {tp=10;}
	else
	{
	tp=Var.typeInt(type);
	}
	
	
	//System.out.println("type:"+type+",tp:"+tp);
	//if (tp == -1)
	//{
	//System.err.println("name:"+name);
	//}
        if (tp==10)
        {

            if (vc.is_in(parts[0]))
            {
		//System.err.println(parts[0]);
		name = ThingType.pullGroupStuff(name,vc);
		Var tocopy=vc.get(parts[0]);
		if (checkType(type,tocopy.getThing().getThingType(),ttc))
		{
                return Var.copy(vc.get(parts[0]),name);
		}
            }

            tp=-1;

        }


        return process(tp,name,parts,vc,OUT,IN,STATE);

    }

    private static boolean checkType(String ltype,String rtype,ThingTypeContainer ttc)
    {
	    if (ltype.equals("thing")){return true;}
	    
	    //System.err.println("ltype="+ltype+",rtype="+rtype);
	    if ((!ttc.is_in(rtype) && !rtype.equals("thing")) || (!ttc.is_in(ltype)&&!ltype.equals("thing")))
	    {
	    //System.err.println("ttc.is_in() false");
	    return false;}
  	    if (rtype.equals(ltype)){return true;}
	    ThingType tt=ttc.get(rtype);
	    String[] inties=tt.getEveryInterface();
	    String[] ancestors=tt.getAllAncestors();
	    String[] comp=new String[inties.length+ancestors.length];
	    System.arraycopy(ancestors,0,comp,0,ancestors.length);
	    System.arraycopy(inties,0,comp,ancestors.length,inties.length);
	    
	    for (int i=0; i<comp.length;i++)
	    {
		    //System.err.println("compare (ltype:"+ltype+") and (comp:"+comp[i]+")");
		    if (ltype.equals(comp[i])){return true;}
	    }
	    return false;
    }
    
	    

    private static Var process(int type, String nm, String[] pts,VarContainer varc,Output OUT, InputStream IN,Statement STATE)
    {
	    //if (type==3){dump(pts);}
	boolean attempting = STATE.getAttempting();
	if (type==16) {
	//System.out.println("anyvar!");
	//dump(pts);
	}
        if (type ==Var.YES_NO)
        {

            if (pts.length > 2)
            {
                String m = "...a reconstructed statement is:\n\t";

                for (int i=0; i<pts.length;i++)
                {
                    m=m+pts[i]+" ";
                }

                OUT.println(new DSOut(ER,AS,"There are two many values here for a yes_no\n"+m,STATE),
			varc,attempting);

                return new Var(nm,-1);

            }

            if ((pts[0].equalsIgnoreCase("yes"))|(pts[0].equalsIgnoreCase("true")))
            {
                return new Var(true,nm);
            }

            if ((pts[0].equalsIgnoreCase("no"))|(pts[0].equalsIgnoreCase("false")))
            {
                return new Var(false,nm);
            }

            boolean negate = false;

            if (pts[0].equals("+"))
            {
                pts[0]=pts[1];
            }

            if (pts[0].startsWith("-"))
            {
                negate=true;
                pts[0]=pts[0].substring(1,pts[0].length());
            }

            try
            {
                Var v = varc.get(pts[0]);

                if ((v.asString().equalsIgnoreCase("yes"))|(v.asString().equalsIgnoreCase("no")))
                {

                    if (negate)
                    {
                        return new Var(!v.getYes_No(),nm);
                    }
                    else
                    {
                        return new Var(v.getYes_No(),nm);
                    }

                }

            }
            catch(Exception ee)
            {
                boolean can_proceed=false;

                for (int pp=0; pp<pts.length;pp++)
                {

                    if (pts[pp].equalsIgnoreCase("ask"))
                    {
                        can_proceed=true;break;
                    }

                }

                if(!can_proceed)
                {
                    String m = "A reconstructed statement is:\n\t";

                    for (int i=0;i<pts.length;i++)
                    {
                        m=m+pts[i]+" ";
                    }

                    OUT.println(new DSOut(ER,AS,"There is no yes_no value here\n"+m,STATE),
				varc,attempting);

                    return new Var(nm,-1);

                }

                /*end catch*/
            }

            /*end if(Var.YES_NO)*/
        }

        for (int mm=0; mm<pts.length;mm++)
        {

            if (pts[mm].equalsIgnoreCase("ask"))
            {
		    if (type == 16) {type=4;}
                OUT.print("ds<<- ");
                pts[mm]=Ask.ask(type,OUT, IN);

                if (type==3)
                {
                    pts[mm]=new String("\'"+pts[mm]+"\'");
                }

                if (type==4)
                {
                    pts[mm]="\""+pts[mm]+"\"";
                }

            }

        }

        /* Instead of using numerical constants, we should use their names. I'll fix this latter -DN*/
        
	/*need to add this here: check for single anyvar assign*/
	if ((type == 16)&&(pts.length==1)&&varc.is_in(pts[0]))
	{
			return new Var(new Anyvar(varc.get(pts[0])),nm);
	}
	
	/*
	if ((type == 16) && (pts.length==1) && varc.isCharLiteral(pts[0]))
	{
		type=3;
	}
	else if ((type == 16) && (pts.length==1) && varc.isStringLiteral(pts[0]))
	{
		type=4;
	}
	else if ((type==16) && allCharData(pts,varc))
	{
		type=4;
	}
	*/
	
	/*above should fix anyvar assignment*/
		//System.out.println("switch("+type+")");

	switch(type)
        {

        case 0:
            {
                for (int ii=0; ii<pts.length; ii++)
                {

                    if (pts[ii].equalsIgnoreCase("yes")|pts[ii].equalsIgnoreCase("true"))
                    {
                        return new Var(true,nm);
                    }

                    if (pts[ii].equalsIgnoreCase("no")|pts[ii].equalsIgnoreCase("false"))
                    {
                        return new Var(false,nm);
                    }

                }

            }


        case 1:
            {
                try
                {
                    long ll = (long)DoubleVal(pts,varc,OUT,attempting);
		    //long ll = LongVal(pts,OUT,attempting);
		    
                    return new Var(ll,nm);
                }
                catch(Exception e)
                {
                    String m = "";
                    for (int i=0; i<pts.length;i++)
                    {
                        m=m+pts[i]+" ";
                    }
                    OUT.println(new DSOut(ER,AS,"This statement does not make an integer\nA reconstructed statement is:\n\t"+m,STATE),
				varc,attempting);
                    return new Var(nm,-1);
                }

            }
        case 2:
            {
                try
                {
                    double dd = DoubleVal(pts,varc,OUT,attempting);return new Var(dd,nm);
                }
                catch(Exception e)
                {
                    String m = "";

                    for (int i=0; i<pts.length;i++)
                    {
                        m=m+pts[i]+" ";
                    }

                    OUT.println(new DSOut(ER,AS,"This statement does not make a decimal\nA reconstructed statement is:\n\t"+m,STATE),
					varc,attempting);
                    return new Var(nm,-1);

                }

            }
        case 4:
            {
                String ss= StringVal(pts,varc,OUT,attempting);

                if (ss.equals(">>NULL<<"))
                {
                    String m =""; for (int i=0; i<pts.length;i++) {m=m+pts[i]+" ";
                    }

                    OUT.println(new DSOut(ER,AS,"This statement does not make a string\nA reconstructed statement is:\n\t"+m,STATE),
				varc,attempting);
                    return new Var(nm,-1);

                }

                return new Var(ss,nm);

            }

        case 3:
            {
                String c = CharVal(pts,varc,OUT,attempting);

                if(StatementProcessor.DEBUG)
                {
                    OUT.println(pts[0]+"::"+c,2);
                }

                if (c.equals(">>NULL<<"))
                {
                    return new Var('\0',nm);
                }

                return new Var(c.charAt(0),nm);

            }

	  case 16:{
			Var vo = new Var(nm,16);
			String[] oldparts=new String[pts.length];
			System.arraycopy(pts,0,oldparts,0,oldparts.length);
			
			try {double d = DoubleVal(pts,varc,OUT,true);
				  long l = (long)d;
				  if ((double)l == d)
			          {vo.setValue(l); return vo;}
			     vo.setValue(d);return vo;
				}
				catch(Exception e){}
			pts=oldparts;
			vo.setValue(StringVal(pts,varc,OUT,attempting)); return vo;
		     }

        default:
            {
                
		String m="";

                for (int i=0; i<pts.length;i++)
                {
                    m=m+pts[i]+" ";
                }

                OUT.println(new DSOut(ER,AS,"These variable types do not match up\nA reconstructed statement is:\n\t"+m,STATE),
				varc,attempting);

            }

        }

        return new Var(nm,-1);

    }
    public static boolean allCharData(String[] pts, VarContainer vc)
    {
	    for (int i=0; i<pts.length;i++)
	    {
		if (vc.isCharLiteral(pts[i]) || vc.isStringLiteral(pts[i]))
		{
			continue;
		}
		if (vc.is_in(pts[i]))
		{
			Var h=vc.get(pts[i]);
			if (h.getType()==Var.STR) {continue;}
			if (h.getType()==Var.CHR) {continue;}
			return false;
		}
	    }
	    return true;
    }

    private static String CharVal(String[] pts,VarContainer varc,Output OUT, boolean AT)
    {
	/*use AT for any error outputting*/

        if(StatementProcessor.DEBUG )
        {
            OUT.println("pts.length=="+pts.length,2);
        }

        if (pts.length != 1)
        {
            return ">>NULL<<";
        }

        if (varc.is_in(pts[0]))
        {
            Var v = varc.get(pts[0]);

            if (v.getType()==Var.CHR)
            {
                return ""+v.getChar();
            }

            if (v.getType()==Var.STR)
            {
                try
                {
                    return ""+v.asString().charAt(0);
                }
                catch(Exception e)
                {
                    return ">>NULL<<";
                }

            }

        }

        if (pts[0].length() > 1 && pts[0].startsWith("\'") && pts[0].endsWith("\'"))
        {
            pts[0]=pts[0].substring(1,pts[0].length()-1);

            if (pts[0].length() == 1)
            {
                return pts[0];
            }
	    //System.err.println("char assign, pts[0]=="+pts[0]);

            char ret = StringFunct.makeChar(pts[0]);

            if (ret != ' ')
            {
                return ""+ret;
            }

        }

        return ">>NULL<<";

    }

//New automatic double conversion first, then to long if necessary, probably makes this 
//section following eliminatable


    public static double DoubleVal(String[] pts,VarContainer varc,Output OUT, boolean AT) throws Exception
    {


        double d = 0.00;
        boolean look_for_plus=false;
        int declength=0;

        for (int i=0; i<pts.length;i++)
        {

            if (look_for_plus == false)
            {
		    //below was commented out...not sure how it screwed things up
		    
		if ((pts[i].startsWith("\"")&&pts[i].endsWith("\"")) || 
			(pts[i].startsWith("\'")&&pts[i].endsWith("\'")))
			{
			pts[i]=pts[i].substring(1,(pts[i].length()-1));
			}
	
                try
                {

                    int dl = DoubleFix.getDecLength(pts[i]);

                    if (dl > declength)
                    {
                        declength=dl;
                    }

                    d=d+DoubleFix.fix(Double.parseDouble(pts[i]),declength);look_for_plus=true;continue;

                }
                catch(Exception e)
                {

                    if (StatementProcessor.DEBUG)
                    {
                        OUT.println(e.toString(),2);
                    }

                    try
                    {

                        double dd=procDecVar(pts[i],varc,OUT);
                        int dll =DoubleFix.getDecLength(dd);

                        if (dll > declength)
                        {
                            declength=dll;
                        }


                        d=DoubleFix.fix((d+dd),declength); look_for_plus=true;continue;

                    }
                    catch(Exception ee)
                    {
                        String m ="";

                        for (int k=0; k<pts.length;k++)
                        {
                            m=m+pts[k]+" ";
                        }

                        OUT.println(new DSOut(ER,AS,"'"+pts[i]+"' is not a decimal\nA reconstructed statement is:\n\t"+m),
						varc,AT);
                        throw e;

                    }

                }

            }

            if ((look_for_plus==true)&(pts[i].equalsIgnoreCase("+")==false))
            {

                boolean true_error = true;

                if (pts[i].startsWith("-"))
                {
                    /*assume a +*/
                    true_error=false;
                    i--; /*push back*/
                }

                if (true_error)
                {
                    String m = "";

                    for (int k=0; k<pts.length;k++)
                    {
                        m=m+pts[k]+" ";
                    }

                    OUT.println(new DSOut(ER,AS,"'"+pts[i]+"' should be 'plus' or '+'\nA reconstructed statement is:\n\t"+m),
					varc,AT);

                    if (StatementProcessor.DEBUG)
                    {
                        for (int xx=0;xx<pts.length;xx++)
                        {
                            OUT.println("pts["+xx+"]]=="+pts[xx],2);
                        }
                    }

                }

            }

            look_for_plus=!look_for_plus;

        }

        return d;

    }


    private static String StringVal(String[] pts, VarContainer varc,Output OUT, boolean AT) //throws Exception
    {

	    //System.err.println("StringVal()");

        String s="";
        boolean look_for_plus = false;

        for (int i=0;i<pts.length;i++)
        {

            pts[i]=pts[i].trim();

            if ((look_for_plus==true)&&!pts[i].equals("+"))
            {

                String m="";

                for (int k=0; k<pts.length;k++)
                {
                    m=m+pts[k]+" ";
                }

                OUT.println(new DSOut(ER,AS,"'"+pts[i]+"' should be 'plus' or '+'\nA reconstructed statement is:\n\t"+m),
			varc,AT);

                if (StatementProcessor.DEBUG)
                {

                    for (int xx=0;xx<pts.length;xx++)
                    {
                        OUT.println("pts["+xx+"]=="+pts[xx],2);
                    }
                }

                return new String(">>NULL<<");

            }

            if (look_for_plus)
            {
                look_for_plus = !look_for_plus; continue;
            }

            if (!look_for_plus)
            {
		   if (pts[i].equalsIgnoreCase("yes")||pts[i].equalsIgnoreCase("no"))
		     {s=s+pts[i]; look_for_plus = !look_for_plus;
		      continue;
		     }
                if (pts[i].length()>1 && pts[i].startsWith("\"")&&pts[i].endsWith("\""))
                {
                    s=s+pts[i].substring(1,(pts[i].length()-1));
                    look_for_plus=!look_for_plus;continue;
                }

                if (pts[i].startsWith("\'")&&pts[i].endsWith("\'"))
                {
                    String ff=pts[i].substring(1,pts[i].length()-1);
                    char c = StringFunct.makeChar(ff);

                    if (c == ' ')
                    {
                        if (ff.equals("")){c='\0';
                        }
                        else
                        {
                            c=ff.charAt(0);
                        }

                    }

                    s=s+c;
                    look_for_plus=!look_for_plus;continue;
                }
                try
                {
		    //System.out.println("procStringVar to be called for ("+pts[i]+")");
                    String ss= procStringVar(pts[i],varc);
                    s=s+ss;look_for_plus=!look_for_plus;
                    continue;
                }
                catch(Exception ee)
                {
                    return ">>NULL<<";
                }

            }

        }

        return s;

    }

    //LongVarStuff was here
    
    public static double procDecVar(String varname, VarContainer varc, Output OUT) throws Exception
    {
	//System.out.println("Assignchooser.procDecVar:"+varname);
        double negative = 1.0;
        boolean neg=false;

        if (varname.startsWith("-"))
        {
            neg =true;
            negative = -1.0;
            varname=varname.substring(1,varname.length());
        }

        Var v = null;
	if (varc.is_in(varname))
	{
		v=varc.get(varname);
	}
	else{
		//System.out.println("!!"+varname+" not found");
		throw new Exception();
        }

	boolean thingdec=false;
	boolean thingint=false;
	if (v.getType() == Var.THING)
	{
		//System.err.println("v("+v.getName()+") is a thing");
		ActionContainer tact=v.getThing().getActionContainer();
		if (tact.is_in("toDecimal",new int[]{-1},2))
		{
			thingdec=true;
		}
		else if (tact.is_in("toInteger",new int[]{-1},1))
		{
			thingint=true;
		}
	}
	//System.out.println("thingdec="+thingdec+",thingint="+thingint);	
        if ((v.getType() == 2)||(v.getType()==6) || thingdec)
        {
            int z = DoubleFix.getDecLength(v.asString());
            double doub = v.getDecimal();
            double mtch = DoubleFix.matchLength(doub);

            if (neg ==true)
            {
                negative = (-1)*mtch;
            }
            else
            {
                negative=mtch;
            }

            return (negative*doub);

        }

        if (v.getType() == 1 || thingint)
        {
            return (negative*(double)v.getInteger());
        }

	  if (v.getType() == 3)
	   {
           char c = v.getChar();
		int inty = -1;
		if (c == '0') {return 0.0;}
		else if (c == '1') {return 1.0;}
		else if (c == '2') {return 2.0;}
		else if (c == '3') {return 3.0;}
		else if (c == '4') {return 4.0;}
		else if (c == '5') {return 5.0;}
		else if (c == '6') {return 6.0;}
		else if (c == '7') {return 7.0;}
		else if (c == '8') {return 8.0;}
		else if (c == '9') {return 9.0;}
		
		throw new Exception();
	   }


        if (v.getType() == 4)
        {

            try
            {
                double dd = Double.parseDouble(v.asString().trim());
                double match=DoubleFix.matchLength(dd);

                if (neg==true)
                {
                    negative=-match;
                }
                else
                {
                    negative=match;
                }

                return (negative*dd);
            }
            catch(Exception e)
            {
                throw e;
            }

        }
	

        boolean b=true;

        if(b)
        {
            throw new Exception();
        }

        return -1.0;

    }

    public static String procStringVar(String varname,VarContainer varc) throws Exception
    {
	if (VarContainer.isNumberLiteral(varname)){return varname;}
	
        boolean minus = false;

        if (varname.startsWith("-"))
        {
            varname=varname.substring(1,varname.length());minus=true;
        }

        if (varc.is_in(varname))
		{
      	Var v = varc.get(varname);
		if (minus) {return If.negativeValue(v.asString(),v.getType());}
		else {return v.asString();}
		}
		else if (true) {throw new Exception();}
	  return ">>NOVALUE<<";
	

    }

	

}




