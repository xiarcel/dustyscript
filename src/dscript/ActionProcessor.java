package dscript;

import java.util.Vector;
import java.io.InputStream;

public class ActionProcessor
{
	public static final int[] NORETURN={0,2,1,3,4,7,10,11,15,16};

	static Var system = null;
    static boolean DEBUG=StatementProcessor.DEBUG;

    /*these are for reporter output... shorten the typing for output-info*/

    static int ACT = DSOut.ACT;
    static int ERR = DSOut.ERR_OUT;

    public static boolean process(ActionContainer ac, Statement s, VarContainer vc,
                                  ThingTypeContainer ttc, Output OUT, InputStream IN)
    {
	if (ttc.is_in("System") && (system == null))
	{
		system = ttc.get("System").getGlobalThing();
	}
	
	boolean AT = s.getAttempting();
        String[] parts = s.getParts();
	if (parts.length == 1 && ac.is_in(parts[0]))
	{
		String m=parts[0];
		parts=new String[]{m,"using","~"};

	}
        boolean assign=false;
        boolean reassign=false;
        int apstart=0;
        String action_name="";
        String vname="";
        String[] string_refs = s.getRefs();
        String[] char_refs = s.getCharRefs();

        if (StatementProcessor.DEBUG)
        {

            for (int i=0; i<string_refs.length; i++)
            {
                OUT.println("sr["+i+"]="+string_refs[i],2);
            }

        }

        int type=-1;
        boolean requires_thing=false;
        String thactname="";
        ActionContainer other = null;
        VarContainer othervars=null;

        if (vc.is_in(parts[0]))
        {
            vname=parts[0];

            if (parts[1].equalsIgnoreCase("is_now")==false)
            {

                OUT.println(new DSOut(ERR,ACT,"You tried to re-assign a variable with an action\n\t"+
                                      "...but you did not use 'is now'",s),vc,AT);
                return false;

            }

            reassign=true;

            if (!ac.is_in(parts[2],true))
            {

                int ix=parts[2].indexOf(".");

		if ((ix < 0) && checkSystem(parts[2]))
		{
			parts[2]="System."+parts[2];
			ix=parts[2].indexOf(".");
		}
			
		
                if (ix > -1)
                {
                    String ck = parts[2].substring(0,ix);
                    thactname = parts[2].substring(ix+1,parts[2].length());

                    if (vc.is_in(ck,true))
                    {

                        try
                        {
				Thing ting = vc.get(ck,true).getThing();
                            other=ting.getActionContainer();
                            othervars=ting.getVarContainer();

                            if (!other.is_in(thactname,true))
                            {
                                throw new Exception();
                            }

                        }
                        catch(Exception e)
                        {
                            OUT.println(new DSOut(ERR,ACT,"Dustyscript could not process this thing's"+
                                                 " action\n\t..or it does not exist",s),vc,AT);
                            return false;
                        }


                        requires_thing=true;parts[2]=thactname;

                    }
	            else
                    {
                        OUT.println(new DSOut(ERR,ACT,"Dustyscript thought '"+ck+
                                              "' was a thing\n\t..but it does not appear to be",s),vc,AT);

                        return false;
                    }

                }	
			
                else
                {
                    OUT.println(new DSOut(ACT,"Either this 'action' is not defined\n\t\t"+
                                          "..or this is some other error",s),vc,AT);

                    return false;
                }

            }

            action_name=parts[2];
            apstart=3;
            type = vc.get(parts[0]).getType();

        }

        if (type <0)
        {
           if (parts.length >=2 && parts[1].equals("using"))
	   {type=a_type(parts[0]);}
   	   else{
	   type = a_type(parts[0],ttc);
	   }
        }

        if (type < 0)
        {

            if ((!ac.is_in(parts[0],true))&&(!reassign))
            {

                int ix =parts[0].indexOf(".");

		if ((ix<0) && checkSystem(parts[0]))
		{
		parts[0]="System."+parts[0];
		ix=parts[0].indexOf(".");
		}
		
                if (ix >-1)
                {
                    String ck = parts[0].substring(0,ix);
                    thactname =parts[0].substring(ix+1,parts[0].length());

                    if (vc.is_in(ck,true))
                    {

                        try
                        {
                            other =vc.get(ck,true).getThing().getActionContainer();
                            othervars=vc.get(ck,true).getThing().getVarContainer();

                            if (!other.is_in(thactname,true))
                            {
                                throw new Exception();
                            }

                        }
                        catch(Exception e)
                        {
                            OUT.println(new DSOut(ERR,ACT,"Dustyscript could not process this"+
                                                  " thing's action\n\t..or it does not exist",s),
									vc,AT);

                            return false;
                        }

                        requires_thing=true;parts[0]=thactname;apstart=1;
                        action_name=parts[0];

                    }
                    else
                    {
                        OUT.println(new DSOut(ERR,ACT,"Dustyscript thought '"+ck+
                                             "' was a thing\n\t..but it does not appear to be",s),
								vc,AT);

                        return false;
                    }


                }
                else
                {
                    OUT.println(new DSOut(ERR,ACT,"Dustyscript treated this as an assignment,\n\t"+
                                          "..but the type to assign was not valid type\n\t..or the action name is in the wrong place",s),
							vc,AT);

                    return false;
                }

            }
            else
            {

                action_name=parts[0];
                apstart=1;

            }

        }
        else if (!reassign)
        {
		//OUT.println(parts.length+"==parts.length");
		//System.out.println(parts[0]);
            vname = parts[1];
	    
            if ((parts[2].equalsIgnoreCase("is")==false)&&(parts[2].equalsIgnoreCase("=")==false))
            {
                OUT.println(new DSOut(ERR,ACT,"Dustyscript was expecting 'is' instead of '"+parts[2]+"'",s),
				vc,AT);
                return false;
            }

            if (!ac.is_in(parts[3],true))
            {
                int ix =parts[3].indexOf(".");

		if ((ix<0)&&checkSystem(parts[3]))
		{
		parts[3]="System."+parts[3];
		ix=6; //parts[3].indexOf(".")
		}
		
		
                if (ix >-1)
                {

                    String ck = parts[3].substring(0,ix);
                    thactname =parts[3].substring(ix+1,parts[3].length());
		    if (StatementProcessor.DEBUG){System.out.println("MARK:");}
		 
                    if (vc.is_in(ck,true))
                    {
			if (StatementProcessor.DEBUG){System.out.println("AP(vc.is_in()) returned true for '"+ck+"'");}
                        try
                        {
                            other =vc.get(ck,true).getThing().getActionContainer();
                            othervars=vc.get(ck,true).getThing().getVarContainer();

                            if (!other.is_in(thactname,true))
                            {
                                throw new Exception();
                            }

                        }
                        catch(Exception e)
                        {
                            OUT.println(new DSOut(ERR,ACT,"Dustyscript could not process"+
                                        " this thing's action\n\t..or it does not exist",s),
							vc,AT);
                            return false;
                        }

                        requires_thing=true;parts[3]=thactname;

                    }
                    else
                    {
                        OUT.println(new DSOut(ERR,ACT,"Dustyscript thought '"+ck+
                                              "' was a thing\n\t..but it does not appear to be",s),
								vc,AT);
                        return false;
                    }

                    action_name=parts[3];

                }
                else
                {

                    OUT.println(new DSOut(ERR,ACT,"Dustyscript expected that '"+parts[3]+
                                "' would be an action name",s),vc,AT);
                    return false;

                }

            }

            action_name=parts[3];
            apstart=4;
            assign=true;

        }

        String[] bak = parts;

        /*PUT AUTOFIX for no "using" here*/
        try
        {

            if (apstart > (parts.length-1))
            {

                String[] nwparts = new String[parts.length+2];

                for (int ii=0;ii<parts.length;ii++)
                {
                    nwparts[ii]=parts[ii];
                }

                nwparts[apstart]="using";
                nwparts[apstart+1]="~";
                parts=null; parts=nwparts;

            }

        }
        catch(Exception e)
        {
            parts=bak;
        }

        if (parts[apstart].equalsIgnoreCase("using")==false)
        {
            OUT.println(new DSOut(ERR,ACT,"Dustyscript expected 'using', not '"+parts[apstart]+"'",s),vc,AT);

		return false;
        }

        apstart++;
        parts=check_for_minus(parts);
        int[] usingtypes = new int[parts.length-apstart];
	String[] usingalts = new String[parts.length-apstart];
	
        for (int i=0; i<usingtypes.length;i++)
        {
		usingalts[i]="";
		String varref = parts[(i+apstart)];
		//This comma thing SUCKS.  It should be taken to task EARLY..like in Statement.
		if (varref.endsWith(",")) {varref=varref.substring(0, varref.length()-1);}
		if (varref.startsWith(":")) {varref=varref.substring(1,varref.length());} //for passing actions anon
            usingtypes[i] = getPartType(varref,s,vc);
	    if (usingtypes[i] == Var.GROUP)
	    {
		    
		    
		    
		    try{
		    VarGroup veegy = vc.get(varref).getVarGroup();
		    if (!veegy.getDeclaredThingType().equals(""))
		    {
			    usingalts[i]=veegy.getDeclaredThingType()+"s";
		    }
		    
		    if (!usingalts[i].equals(""))
		    {
			    usingalts[i]=usingalts[i]+":"+Var.typePlurals(veegy.getType());
		    }
		    else {
			    usingalts[i]=Var.typePlurals(veegy.getType());
		    }
		    
		    }
		    catch(Exception e){usingalts[i]="";}
	    }
	    
	    if (usingtypes[i] == Var.THING)
	    {
		   // System.out.println("varref=="+varref);
		    try {usingalts[i] = vc.get(varref).getThing().getThingType();}
		    catch(Exception e){usingalts[i]="";//e.printStackTrace();
		    }
		  //  System.out.println("ua["+i+"]=="+usingalts[i]);
	    }
     
	  }

        Action act=null;
        Var container = null;

        if (requires_thing && (other !=null))
        {
            ac=other;
            container = new Var(new VCWrapper(vc));
            othervars.add(container);
        }
	//if (action_name.indexOf("exec") > -1) {System.out.println("Searching for "+action_name);}
	ActionHashIterator ahi = ActionHashIterator.getActionHashIterator(action_name,usingtypes,usingalts,type,ttc);
	if (ac.is_in(ahi)) {act = ac.get(ahi);}
	//else {ac.dump();ahi.dump();}
	//System.out.println("req_tg=="+requires_thing);
     //OLDER WAYS:: We are relying on AHI to find ALL permutations
     /*
        else if (ac.is_in(action_name,usingtypes,type))
        {
            act = ac.get(action_name,usingtypes,type);
        }
	  else if (ac.inByWild(action_name,usingtypes,type))
		{
		act= ac.getByWild(action_name,usingtypes,type);
		}
	  else if (ac.inByWild(action_name,usingtypes,16))
		{
		act=ac.getByWild(action_name,usingtypes,16);
		}
       */
       if (act==null && type == -1)
       {
	       //calling a return type action with no assignment
	       for (int z=0; z<NORETURN.length;z++)
	       {
		       ahi=ActionHashIterator.getActionHashIterator(action_name,usingtypes,usingalts,NORETURN[z],ttc);
		       if (ac.is_in(ahi)) {act=ac.get(ahi); break;}
	       }
       }
       if (act==null)
        {

            if (StatementProcessor.DEBUG) //||true)
            {
                for (int k=0; k<usingtypes.length;k++)
                {
                    OUT.println("usingtypes["+k+"]=="+usingtypes[k],2);
		    OUT.println("usingalts["+k+"]=="+usingalts[k],2);
                    ac.dump(2);
                    OUT.println("as compared to action_name:"+action_name,2);
                }
            }

            OUT.println(new DSOut(ERR,ACT,"Dustyscript found no matching prepared action",s),vc,AT);
            return false;

        }
	s.resetParts(parts);
	return process (act,vname,ac,s,vc,othervars,ttc,container,OUT,IN,apstart,assign,reassign,requires_thing);
    }
    
    public static int getUsingStart(String[] s)
    {
	    for (int i=0; i<s.length;i++)
	    {
		    if (s[i].equals("using")) {return i;}
	    }
	    return -1;
    }
    
    
    
    private static boolean process(Action act, String vname, ActionContainer ac, Statement s, VarContainer vc, VarContainer othervars,
    		ThingTypeContainer ttc, Var container, Output OUT, InputStream IN, int apstart, boolean assign, boolean reassign,
		boolean requires_thing)
	{
	int type = act.getReturnType();
	String[] parts = s.getParts();
	
	String[] string_refs = s.getRefs();
	String[] char_refs = s.getCharRefs();
	boolean AT = s.getAttempting();
	if (apstart < 0) {apstart = getUsingStart(parts)+1;}	
		
        int tlngth = parts.length-apstart;

        if (act.getArgs().length != tlngth)
        {
            OUT.println(new DSOut(ERR,ACT,"This action takes a different number of args\n\t...then what you gave it",s),
			vc,AT);
            return false;
        }

        if ((act.getReturnType()<0)&&(assign||reassign))
        {
            OUT.println(new DSOut(ERR,ACT,"You cannot reassign a variable with this action\n\t"+
                                  "..it 'gives' nothing (~) to assign with",s),vc,AT);
            return false;
        }

        //Vector tempy=new Vector();
        int[] arggieint=act.getArgs();
        Var[] args =new Var[(parts.length-apstart)];
        for (int w=apstart; w<parts.length; w++)
        {   //tempy.addElement(prep_var(parts[w],(w-apstart),arggieint,vc,string_refs,char_refs,OUT,AT));
            args[w-apstart]=prep_var(parts[w],(w-apstart),arggieint,vc,string_refs,char_refs,OUT,AT);
        }

        //Var[] args = new Var[tempy.size()];
	 // System.arraycopy(tempy.toArray(),0,args,0,tempy.size());
   
   	/*if (!assign && !reassign)
	{
		assign=true;
		vname="__TEMPY_ACT_HOLDER__";
	}
	*/
        if (assign)
        {	int setvartype=16;
		try {setvartype = Var.typeInt(parts[0]);}
		catch(Exception e){setvartype=16;}
		
		int srtp = act.getReturnType();
		if (srtp==16) {srtp = type;}
		switch (setvartype)
		{
			case 1: 
			case 2:
			case 4:
			{srtp = setvartype;break;}
			default: {break;}
		}
		
            Var nw = new Var(vname,srtp);
	    if (DEBUG){OUT.println("vname="+vname+",srtp="+srtp,2);}
            vc.add(nw);

            if (requires_thing)
            {
                vc = othervars;
            }
	    //vc.add(nw);
            boolean b= act.exec(args,vc,nw,ttc,AT);

            if (container != null)
            {
                vc.remove(container);
            }
	    //new to keep variable from being created on error
	    if (!b && vc.is_in_local(nw)) {vc.remove(nw);}
	    if ( vc.is_in("__TEMPY_ACT_HOLDER__") ) {vc.remove("__TEMPY_ACT_HOLDER__");}
	    
            return b;

        }


        if (reassign)
        {
            Var old = vc.get(vname);

            if (requires_thing)
            {
                vc=othervars;
            }

            boolean b = act.exec(args,vc,old,ttc,AT);

            if (container != null)
            {
                vc.remove(container);
            }
	 
            return b;

        }

        if (requires_thing)
        {
            vc=othervars;
        }

        boolean b=act.exec(args,vc,null,ttc,AT);

        if (container != null)
        {
            vc.remove(container);
        }
	if (vc.is_in("__TEMPY_ACT_HOLDER__")) {vc.remove("__TEMPY_ACT_HOLDER__");}
        return b;

    }

    public static boolean processFromSplitter(Action a, String vname,ActionContainer ac, Statement s, VarContainer vc, ThingTypeContainer ttc,
    		Var container, Output OUT, InputStream IN)
	{
		return process(a,vname,ac,s,vc,null,ttc,container,OUT,IN,-1,false,true,false);
	}
	
    

    public static Var prep_var(String s, int i, int[] argie, VarContainer vc, String[] in_string, String[] in_char, Output OUT,boolean AT)
    {


        if (StatementProcessor.DEBUG)
        {
            OUT.println("Processing "+s+" for variable",2);
        }

        if (s.endsWith(","))
        {
            s=s.substring(0,(s.length()-1));
        }
	if (s.startsWith(":"))
	{
		s=s.substring(1,s.length());
	}
        boolean use_n = false;
	String nstr = "";

        if (s.startsWith("-")){s=s.substring(1,s.length());nstr="-";use_n=true;}

        if (argie[i]==Var.INT)
        {

            try
            {

                return new Var(Long.parseLong(nstr+s),"_"+s);
            }
            catch(Exception e)
            {
                Var v = vc.get(s);

                if (use_n)
                {
                    return new Var((-1*v.getInteger()),"_"+s);
                }
                else
                {
                    return new Var(v.getInteger(),"_"+s);
                }

            }
        }

        if (argie[i]==Var.DEC)
        {

            try
            {
                return new Var(Double.parseDouble(nstr+s),"_"+s);
            }
            catch(Exception e)
            {
                Var v= vc.get(s);

                if (use_n)
                {
                    return new Var((-1.0*v.getDecimal()),"_"+s);
                }
                else
                {
                    return new Var(v.getDecimal(),"_"+s);
                }

            }
        }

        if (argie[i]==Var.STR)
        {
            s.trim();

            if (StatementProcessor.DEBUG)
            {
                OUT.println("s.length()== "+s.length(),2);
                OUT.println("s.charAt(0) == "+s.charAt(0)+", s.charAt(n-1)=="+s.charAt(s.length()-1),2);
            }

            if (s.startsWith("$")&& s.endsWith("$"))
            {
                try
                {
                    int arint = Integer.parseInt(s.substring(1,s.length()-1));
                    s= in_string[arint];
                }
                catch(Exception e)
                {
                    OUT.println(new DSOut(ERR,ACT,"Dustyscript tried to use this string '"+s+
                                          "'\n\t..but the replace did not go well\n\t..This might cause a future error"),vc,AT);
                }
            }

            if (s.startsWith("\"")&& s.endsWith("\""))
            {

                try
                {
                    return new Var(s.substring(1,(s.length()-1)),"_"+s);
                }
                catch(Exception e){}

            }

            if (vc.is_in(s))
            {
                Var str = new Var(vc.get(s).asString(),"_"+s);
		if (use_n){str.reverseValue();}
		return str;
            }

            Var wop= new Var(s,"_"+s);
	    if (use_n){wop.reverseValue();}
	    return wop;
        }
        if (argie[i]==Var.YES_NO)
        {

            if (s.equalsIgnoreCase("YES")||s.equalsIgnoreCase("true"))
            {  if (use_n) {return new Var(false,"__"+s);}
                return new Var(true,"__"+s);
            }
            if (s.equalsIgnoreCase("NO")||s.equalsIgnoreCase("false"))
            {   if (use_n){return new Var(true,"__"+s);}
                return new Var(false,"__"+s);
            }

	    Var woop = Var.copy(vc.get(s),"__"+s,vc);
	    if (use_n){woop.reverseValue();}
            return woop;

        }

        if (argie[i]==Var.CHR)
        {
            if (s.startsWith("&") && s.endsWith("&"))
            {

                try
                {
                    int cint = Integer.parseInt(s.substring(1,s.length()-1));
                    s =new String(""+in_char[cint]);
                }
                catch(Exception e)
                {
                    OUT.println(new DSOut(ERR,ACT,"Dustyscript tried to use this character '"+s+
                                          "'\n\t..the replace did not go well\n\tThis might cause a future error"),vc,AT);
                }

            }

            if (s.startsWith("\'") && s.endsWith("\'"))
            {

                String ss = s.substring(1,s.length()-1);
                char c = StringFunct.makeChar(ss);

                if (c == ' ')
                {
                    if (ss.equals("")){c='\0';
                    }
                    else
                    {
                        c = ss.charAt(0);
                    }

                }

                return new Var(c,"_"+ss);

            }
	    Var pup = vc.get(s);
	    if (use_n){pup.reverseValue();}
            return pup;

        }
	  if (argie[i]==Var.ANYVAR)
		{
		if (VarContainer.isNumberLiteral(s))
			{
			if (VarContainer.isDecimalLiteral(s))
				{
				return new Var(Double.parseDouble(nstr+s),"_"+s);
				}
				else{
				    return new Var(Long.parseLong(nstr+s),"_"+s);
				    }
			}
		if (VarContainer.isStringLiteral(s))
				{
				if (use_n) {return Var.makeStringVar(s,in_string,true).reverseValue();}
				return Var.makeStringVar(s,in_string,true);
				}
		if (VarContainer.isCharLiteral(s))
				{
				return Var.makeCharVar(s,in_char,true);
				}
		Var pop = new Var(new Anyvar(Var.copy(vc.get(s),"_"+s,vc)),"_"+s);
		if (use_n){pop.reverseValue();}
		return pop;
		}

        if (argie[i]==-1)
        {
            return new Var("~",-1);
        }
	Var pup =vc.get(s);
	if (use_n) {pup.reverseValue();}
        return pup;

    }

    
    public static boolean checkSystem(String proposed_action)
    {
	    if (system == null) {return false;}
	    try{
		    if (system.getThing().getActionContainer().is_in(proposed_action,true))
		    {			    
			    return true;
		    }
	    }
	    catch(Exception e){}
	    return false;
	    
    }
	    
    
    
    public static String[] check_for_minus(String[] s)
    {

        String[] to_ret;
        Vector v=new Vector();

        for (int i=0; i<s.length;i++)
        {
            if (s[i].equals("-"))
            {
                try
                {
                    v.addElement(s[i]+s[i+1]);
                    i++;continue;

                }
                catch(Exception e){}

            }

            v.addElement(s[i]);

        }

        to_ret=new String[v.size()];

        for (int x=0; x<to_ret.length;x++)
        {
            to_ret[x]=(String)v.elementAt(x);
        }

        return to_ret;

    }
    
    public static int a_type(String s, ThingTypeContainer ttc)
    {
	    //System.out.println("a_type(s,ttc):"+s+", "+ttc.is_in(s));
	    if (ttc.is_in(s)) {
	    	//System.out.println("ttc.in("+s+")");
	    	return Var.THING; }
	    else {return a_type(s);}
    }

    public static int a_type(String s)
    {
	 
	return Var.typeInt(s);
	}




    public static int getPartType(String s, Statement state, VarContainer varc)
    {

        /* The if if if thing. */
        if (s.endsWith(","))
        {
            s=s.substring(0,s.length()-1);
        }

        if (s.equals("~"))
        {
            return -1;
        }

        if (s.equals(">NOVALUE<"))
        {
            return -5;
        }

        String ckvar=s;

        if (ckvar.startsWith("-"))
        {
            ckvar=ckvar.substring(1,ckvar.length());
        }

        if (varc.is_in(ckvar))
        {
            return varc.get(s).getType();
        }

        if (s.startsWith("&"))
        {
            return Var.CHR;
        }

        if (s.startsWith("$"))
        {
            return Var.STR;
        }

        if (s.startsWith("\""))
        {
            return Var.STR;
        }

        if (s.startsWith("\'"))
        {
            return Var.CHR;
        }

        if (s.equalsIgnoreCase("yes")||s.equalsIgnoreCase("true")||s.equalsIgnoreCase("no")||s.equalsIgnoreCase("false"))
        {
            return Var.YES_NO;
        }

	  if (VarContainer.isNumberLiteral(s))
	    {
           try
           {
            long l = Long.parseLong(s);
            return Var.INT;
           }
           catch(Exception e)
            {}
           try
           {
            double d = Double.parseDouble(s);
            return Var.DEC;
           }
           catch(Exception e){}
	    }
        return -1;

    }

 public static void crunchShorthand(Statement s)
	{
	String[] parts =s.getParts();
	String[] newparts = new String[parts.length+3];

	newparts[0] = "action";
	newparts[1]= parts[1].substring(0,parts[1].length()-2);
	newparts[2] = "takes";
	newparts[newparts.length-1] = parts[parts.length-1];
	newparts[newparts.length-3] = "gives";
	newparts[newparts.length-2] = parts[0].substring(0,parts[0].length()-2);
	System.arraycopy(parts,2,newparts,3,(parts.length-3));
	s.resetParts(newparts);
	}

}
