package dscript;

import java.util.Vector;
import java.util.HashMap;
import java.io.*;
import dscript.intl.LanguageConverter;
import dscript.connect.*;

public class StatementProcessor extends SPThread implements Killable, Dustializable
{
	//public HashMap inline_hash=new HashMap();
	public static boolean BREAK_AT_STATEMENTS=false;
	
	public boolean didBreak=false;
	public boolean IS_COUNT=false;
	//for disenfranchised {} blocks
	private final static String[] IFSTR = new String[]{"if","yes","==","yes"};
//these blocks might be a bad idea..
	private Dustyable dustyable = null;
	public boolean AS_INTERFACE = false;
	
	final public static String VERSION = "\nStatementProcessor, 1.1 -- RC-E.3 {current as of 8-30-05, 1:44am EDT}) \n";
	
	static double secdev = 1000.00000000;
    public static boolean STOP_AT_ERROR=false;
    private static final DSOut oomerr = new DSOut(DSOut.STD_OUT, -1, "Dustyscript has run out of memory");
    private static final DSOut stckov = new DSOut(DSOut.STD_OUT, -1, "Dustyscript has over-run its memory buffer");
    private static final DSOut othererr = new DSOut(DSOut.STD_OUT, -1,
                                                    "Dustyscript has encountered an unexpected error with the interpreter");
						    
						
    private static final String FEEDBACK_MESSAGE = new String("We would greatly appreciate your feedback,\nplease email dustyscript-devel@lists.sourceforge.net");
    
   //first run pullInlines
    private boolean first_run=true;
    
    static long lastGC = System.currentTimeMillis();
    public boolean encountered_error = false;
    final public static String[] keywords = new String[]{"if", "as_long_as", "yes", "no", "string",
                                                         "integer", "decimal", "character", "group",
                                                         "javaconnector", "thing", "yes_no", "yes_nos",
                                                         "integers", "decimals", "characters", "groups",
                                                         "things", "javaconnectors", "strings",
                                                         "==", "!=", ">", "<", ">=", "<=", "~", "contains",
                                                         "location_of", "copies", "refers_to", "is_now",
                                                         "is", "count", "using", "use", "load", "save",
                                                         "action", "takes", "gives", "give", 
                                                         "from", "through", "grow", "by", "shrink",
                                                         "thread", "pause", "second", "seconds", "minute",
                                                         "minutes", "say", "hook", "connect", "send", "ask",
                                                         "declare", "try", "catch", "&", "|", "profile", "dump",
                                                         "remove", "destroy", "fail", "break", "fail_with", "ask_for",
                                                         "pcount", "ncount", "+", "-", "~", "extends", "is_type",
                                                         "to", "ask", "convert", "into","is_a","!is_a","!is_type",
    							 "sendvar","getvar"};

    //private static boolean run_proceed = false;
    private static boolean DEV_DEB = false;

    private String source; 
    private boolean suppress = false;
    private Statement[] statements;
    private VarContainer vc;
    private ActionContainer ac;
    private String[] inlines;
    public static long RUNLEVEL = 0;
    private long runlevel;
    public long breakpoint;
    public static long BREAKPOINT = 0;
    public static long FAILPOINT = 0;
    public static boolean DEBUG = false; //ALOT OF OUTPUT when TRUE
    public static boolean USER_DEBUG = true; //Standard "help" messages, togglable
    private static long EQUACOUNT = 0;
    public static long THREADCOUNT = 0;
    private ThingTypeContainer ttc;
    private Output OUT = null;
    private InputStream IN = System.in;
    private boolean attempting = false;
    public boolean OK_RUN = true;
    public boolean THREADING = false;
    public boolean BREAKOUT = false;
    public boolean CONTINUED = false;
    private StatementProcessor nextUp = null;
    /*this flag is for things' creation*/
    private boolean ignore_actions = false;
    /*for removing thread that implicitly references self*/
    private Var threadvar = null;
    private String original_source;
    private boolean only_actions = false;
    private boolean ignore_globals = false;
    public boolean FAILED=false;
    public boolean GAVE = false;
    public static boolean BENCHMARK = false;
    static long ELAPSED = 0;
    static long ITEMS = 0;
    private long startbm = 0;
    
    public StatementProcessor() throws Exception
    {
	    //intended for use with Dustializable
	    OUT = null;
	    IN=null;
	    //vc, ac, ttc will be set when an Output and Input are passed in
	    source="";
	    original_source="";
	    RUNLEVEL++;
	    runlevel=RUNLEVEL;
	    
    }
	    


    
    public StatementProcessor(String s, VarContainer v, ActionContainer a, ThingTypeContainer t, Output o, InputStream in)
    {
        OUT = o;
        IN = in;
        original_source = s;
        if (DEBUG)
        {
            OUT.println("VC reference:" + v.getReference(), 2);
        }
	source=s.trim();
        vc = v;
        ttc = t;
        ac = a;
        vc.setSameLevelActionContainer(ac);
        vc.setSameLevelThingTypeContainer(ttc);
        RUNLEVEL++;
        runlevel = RUNLEVEL;
	setVarThing();
    }

    public StatementProcessor(String s, VarContainer v, ActionContainer a, ThingTypeContainer t)
    {

        if (OUT == null)
        {
            try
            {
                OUT = new Output();
            }
            catch (Exception e)
            {
                System.err.println("Output not set..cannot continue");
                System.exit(1);
            }
        }

        if (DEBUG)
        {
            OUT.println("VC reference:" + v.getReference(), 2);
        }

        source = s.trim();
        vc = v;
        ttc = t;
        ac = a;
        vc.setSameLevelActionContainer(ac);
	setVarThing();
        RUNLEVEL++;
        runlevel = RUNLEVEL;

    }

    public StatementProcessor setAsInterface(boolean b)
    {
	    AS_INTERFACE = b;
	    return this;
    }
    
    public StatementProcessor setIsCount(boolean b)
    {
	    IS_COUNT=b;
	    return this;
    }
    
    public StatementProcessor getNextUp()
    {
	    return nextUp;
    }
    public void setVarThing()
    {
	    if (Var.nobody == null)
	    {Var.nobody = new Thing("thing","undef",ac,vc);}
    }
    
    public void setNextUp(StatementProcessor sp)
    {
        nextUp = sp;
    }
    
    public ThingTypeContainer getThingTypeContainer()
    {
	return ttc;
    }

    public boolean nextUpStop()
    {
        if (!THREADING)
        {
            return false;
        }
        if (BREAKOUT)
        {
            return true;
        }
        if (nextUp != null)
        {
            return nextUp.nextUpStop();
        }
        return false;
    }

    public void setThreadVar(Var v)
    {
        threadvar = v;
    }

    public static void reset()
    {
        RUNLEVEL = 0;
        BREAKPOINT = 0;
    }

    public long getRunLevel()
    {
        return runlevel;
    }

    public static void setUserDebug(boolean b)
    {
        USER_DEBUG = b;
    }

    public void ignoreGlobals()
    {
	ignore_globals=true;
    }

    public void ignoreActions()
    {
        ignore_actions = true;
    }

    public StatementProcessor runOnlyActions()
    {
        only_actions = true;
	return this;
    }

    public void kill()
    {
        RUNLEVEL = -1;
        BREAKPOINT = -1;
	/*this will kill ALL processes, so*/
	Use.clear();

    }
    
    public String fixSourceForBrackets(String s)
    {
	    s=Statement.replace(s,"\\{","_:LIT_OB:_");
	    s=Statement.replace(s,"\\}","_:LIT_CB:_");
	    return s;
    }
		    

    public StatementProcessor setAttempting(boolean b)
    {
        attempting = b;
	return this;
    }

    public String getSource()
    {
        return original_source;
    }

    public boolean encounteredError()
    {
	    return encountered_error;
    }
    public void run()
    {
	    if (!ttc.is_in("System"))
	    {		
		    boolean holder = USER_DEBUG;
		    USER_DEBUG=false;
		    boolean ue =Use.exec("dusty_system.txt",vc,ac,ttc,OUT,IN,attempting);
		    USER_DEBUG=holder;
		    if (!ue)
		    {		
			    if (DEBUG) {OUT.println("Error at using dusty_system.txt!",2);}
		    }
	    }
	    
	    if ((source.indexOf("/*LANG:")>-1) && (source.indexOf("/*LANG:ENGLISH/")<0))
	    {
		    source=LanguageConverter.convertToEnglish(source);
	    }
	    
	    
	    Statement last_state=null;//debugging
        if (BENCHMARK)
        {
            startbm = System.currentTimeMillis();
        }
	
	//source = original_source;
        try
        {	
            
	    if (first_run){
	    source=fixSourceForBrackets(source);
	    pullInlines();}
	    	else{GAVE=false;CONTINUED=false;} //multiple runs using same SP

            if (DEBUG)
            {
                OUT.println("passed:pulled inlines", 2);
            }

            /*split source into statements..ignore linebreaks*/
            Vector tostates = split_into_statements();

	    if (BREAK_AT_STATEMENTS && (runlevel==1))
	    {
		    String[] statements=new String[tostates.size()];
		    Object[] o = tostates.toArray();
		    System.arraycopy(o,0,statements,0,o.length);
		    for (int i=0; i<statements.length;i++)
		    {
			    System.out.println("statements["+i+"]:");
			    System.out.println("\t"+statements[i]);
		    }
		    System.exit(0);
	    }
            /*output*/
            if (!suppress)
            {
                OUT.println("Running Dustyscript source\n===========================\n" +
                            source + "\n===========================\n\n" +
                            FEEDBACK_MESSAGE + "\n");
			    //System.err.println("Runlevel:"+runlevel);
            }
	    first_run=false;
            for (int m = 0; m < tostates.size(); m++)
            {
	     if (GAVE||CONTINUED)
                {
                    return;
                }

                while (!nextUpStop() && hibernating())
                {
                }

                if (nextUpStop())
                {
                    return;
                }

                if ((RUNLEVEL < runlevel) || (attempting && encountered_error))
                {
                    return;
                }
		
		if (STOP_AT_ERROR && encountered_error)
		{
			if (nextUp != null)
			{
				nextUp.encountered_error=true;
			}
			String ostate="";
			try {
				String oldstate=(String)tostates.elementAt(m-1);
				ostate=oldstate;
			}
			catch (Exception e){}
			RUNLEVEL=-1;
			OUT.println("Encountered an error in previous statement: Stopping\n"+ostate);
			return;
		}
		
		

                vc.removeAllUsedTransients();
                vc.attempting = attempting;
		
		/*new thing??!?--array [] index for group*/
		String statie = (String)tostates.elementAt(m);
		//statie=clean_statement(statie);
		if (Statement.containsArrayPart(statie)) {statie = Statement.replaceArrayElements(statie,vc,ac,ttc,OUT,IN,attempting);}
                Statement s = new Statement(statie);

                s.create();
                s.setAttempting(attempting);
                s.setInline(inlines);
                String[] pts = fixVC(s.getParts());
                String[] eqs = s.getEquations();
		last_state=s;
                /*this might not make sense to do... I think this is done in like 3 places
                but still, some empty strings exist*/

                pts = clean_parts(pts);
                if (pts.length == 0) {continue;}

                /*above? Something happened with Action*/
                s.resetParts(pts);

                if (DEV_DEB)
                {
                    for (int i = 0; i < pts.length; i++)
                    {
                        System.err.println("pts[" + i + "] == " + pts[i]);
                    }
                }

                process_equations(eqs, s);

                if (pts[0].endsWith("[["))
                {
                    ActionProcessor.crunchShorthand(s);
                    pts = s.getParts();
                }
		
		//shortform 'Fraction f is 1/7' --disabled now
		//pts=fixForFractions(pts,vc,ac);

                if (pts[0].equalsIgnoreCase("action")||pts[0].equalsIgnoreCase("s_action"))
                {
                    if (!ignore_actions)
                    {
                        do_action(s);
                    }
                    continue;
                }
		
		if ( pts[0].equalsIgnoreCase("dumpall"))
		{
			OUT.println("ActionContainer");
			try{ac.dump();}catch(Exception e) {OUT.println("failed at dumping Actions");}
			OUT.println("\nThingTypeContainer");
			try{ttc.dump();} catch(Exception e) {OUT.println("failed at dumping ThingTypes");}
			OUT.println("\nVarContainer");
			try{vc.dump();} catch(Exception e) {OUT.println("failed at dumping VarContainer");}
			continue;
		}
		if (pts[0].equalsIgnoreCase("allow"))
		{
			if (pts.length < 2) {OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.THG,"allow requires at least one interface/thing!", s),vc,attempting);continue;}
			for (int j=1; j<pts.length;j++)
			{
				ttc.allow(stripComma(pts[j]));
			}
			continue;
		}
		
		if (pts[0].equalsIgnoreCase("disallow"))
		{
			if (pts.length == 1) {ttc.disallow(); continue;}
			for (int i=1; i<pts.length;i++)
			{
				ttc.disallow(stripComma(pts[i]));
			}
			continue;
		}
		
		if (pts[0].equalsIgnoreCase("interface")||pts[0].equalsIgnoreCase("mask"))
		{
			do_interface(s,pts);
			continue;
		}
		
                if (pts[0].equalsIgnoreCase("global"))
                {
                    if (!ignore_globals)
                    {
                        do_global(pts, s);
                    }
                    continue;
                }

                if (only_actions)
                {
                    continue;
                }

                if (pts[0].equals("bm_with"))
                {
                    do_benchmark(pts, s);
                    continue;
                }
		
		if (pts[0].equalsIgnoreCase("next")){CONTINUED=true;return;}

                if (pts[0].equalsIgnoreCase("hibernate") || pts[0].equalsIgnoreCase("sleep"))
                {
                    do_hibernate(pts, s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("awaken"))
                {
                    do_wake(pts, s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("restart") || pts[0].equalsIgnoreCase("resurrect"))
                {
                    do_resurrect(pts, s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("kill"))
                {
                    do_kill(pts, s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("give"))
                {
                    GAVE = true;
                    return;
			        /*used to be continue;*/
                }

                if (pts[0].equalsIgnoreCase("try"))
                {
                    do_try(s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("dump"))
                {
                    do_dump(pts, (String) tostates.elementAt(m));
                    continue;
                }
		/*
                if (pts[0].equalsIgnoreCase("end") && (pts.length == 1))
                {
                    if (DEBUG)
                    {
                        OUT.println("Explicit 'end' called", 2);
                    }
                    if (USER_DEBUG || true)
                    {
                        OUT.println("Ending dustyscript program:\nFeature 'end;' scheduled for removal", 0);
                    }
                    break;
                }
		*/
		//removed 'end'
		
		if (pts[0].equals("untype"))
		{
			for (int i=1; i<pts.length;i++)
			{
				if (pts[i].endsWith(",")) {pts[i]=pts[i].substring(0,pts[i].length()-1);}
				if (vc.is_in(pts[i]))
				{
					Var ut=vc.get(pts[i]);
					ut.setType(16);
				}
			}
			continue;
		}
		
					
		
		if (pts[0].indexOf("inline_code")>-1)
		{
			pts = new String[]{IFSTR[0],IFSTR[1],IFSTR[2],IFSTR[3],pts[0]};
			s.resetParts(pts);
		}
		
                if ((pts.length >= 2) && pts[0].equalsIgnoreCase("profile"))
                {
                    do_profile(pts);
                    continue;
                }

                if ((pts.length == 3) && pts[0].equalsIgnoreCase("declare"))
                {
                    if(!pts[1].equalsIgnoreCase("javaconnector"))
                    {
                        do_declare(pts, s);
                        continue;
                    }
                    else
                    {
                        // It's a declare of a JavaConnector - deal with
                        // differently to a 'normal' declare.
                        pts = new String[]{"initialize", pts[2]};
                        s.resetParts(pts);
                    }
                }

                /*new advanced spot for JavaConnector*/
                /*The Java Connector commands*/

                if (pts[0].equalsIgnoreCase("initialize"))
                {
                    do_initialize(pts, s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("connect"))
                {
                    do_connect(pts, s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("hook"))
                {
                    do_hook(pts, s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("send"))
                {
                    do_send_dustyable(pts, s);
                    continue;
                }
		
		if (pts[0].equalsIgnoreCase("sendvar"))
		{
			do_send_regvar(pts,s);
			continue;
		}
	
		if (pts[0].equalsIgnoreCase("getvar"))
		{
			do_get_var(pts,s);
			//remove when done debugging:
			//System.out.println("do_get_var done");
			continue;
		}

                /*moved some pts[0] stuff here*/
                if (pts[0].equalsIgnoreCase("use"))
                {
                    do_use(pts, s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("break"))
                {
                    if (!IS_COUNT) {RUNLEVEL = BREAKPOINT;}
		    didBreak=true;
		    //System.out.println("dB:"+didBreak);
		    //System.out.println("RL:"+RUNLEVEL);
		    //System.out.println("rl:"+runlevel);
		    //System.out.println("IC:"+IS_COUNT);
		    //System.out.println("BP:"+BREAKPOINT);
		    return;
                }

                if (pts[0].equalsIgnoreCase("fail"))
                {
                    do_fail();
                    return;
                }

                if (pts[0].equalsIgnoreCase("fail_with"))
                {
                    do_fail_with(pts, s);
                    return;
                }

                boolean regthread = true;

                if (pts.length >= 3 && pts[2].equalsIgnoreCase("is"))
                {
                    regthread = false;
                }

                /*this is for multi-threaded*/
                if (pts[0].equalsIgnoreCase("thread") && regthread)
                {
                    do_thread(pts, s);
                    continue;
                }

                /*load/SAVE*/
                if (pts[0].equalsIgnoreCase("load"))
                {
                    do_load(s);
                    continue;
                }

                if (pts[0].equalsIgnoreCase("save"))
                {
                    do_save(s);
                    continue;
                }

                if ((pts.length >= 4) && (pts.length < 6) && pts[0].equalsIgnoreCase("convert") && pts[2].equalsIgnoreCase("into"))
                {
                    if(!do_convert(pts, s))
                    {
                        // Fatal error resulted from attempt at conversion.
                        // Bail out immediately.
                        return;
                    }
                    continue;
                }

                /*CAUTION!! This should ONLY really be used in large dustyscript programs.
                    This is a 'deconstructor' implementation.  It will remove ANY variable from
                    the VarContainer, and therefore, the Variable table.  It will not look for it
                    anywhere but in its immediate scope!!  */

                if (pts[0].equalsIgnoreCase("destroy") || pts[0].equalsIgnoreCase("remove"))
                {
                    do_remove(pts, s);
                    continue;
                }

                /*this is for the 'ask for' option*/
                if (pts[0].equals("ask_for"))
                {
                    pts = do_ask_for(pts, s);
                }

                if (pts.length > 2)
                {
                    if (pts[2].equals("thing_refer") || pts[2].equals("thing_copy") ||
                            pts[1].equals("thing_refer") || pts[1].equals("thing_copy"))
                    {

                        String leftname = "";
                        String rtname = "";
                        boolean reassign = false;
                        String do_what = "";

                        if (pts[0].equals("thing"))
                        {
                            if (pts.length < 4)
                            {
                                encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "This statement is too short", s), vc, attempting);
                                continue;
                            }

                            leftname = pts[1];
                            rtname = pts[3];
                            do_what = pts[2];
                            reassign = false;
                        }
                        else
                        {
                            leftname = pts[0];
                            rtname = pts[2];
                            do_what = pts[1];
                            reassign = true;
                        }

                        Var thingop = null;
                        Thing rtthing = null;

                        if (DEBUG)
                        {
                            OUT.println("rtname==" + rtname, 2);
                        }

                        try
                        {
                            rtthing = vc.get(rtname).getThing();
                            String rn = rtthing.getName();
                        }
                        catch (NullPointerException nope)
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "'" + rtname + "' is not a thing!", s), vc, attempting);
                            continue;
                        }

                        if (do_what.equals("thing_copy"))
                        {
                            thingop = new Var(Thing.copy(rtthing, leftname), leftname);
                            try
                            {
                                thingop.getThing().getVarContainer().get("me").setValue(thingop.getThing());
                            }
                            catch (Exception eev)
                            {
                                encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "We failed at setting 'me' when copying a thing", s), vc, attempting);
                            }

                        }

                        if (do_what.equals("thing_refer"))
                        {
				//System.out.println("parts[0]=="+pts[0]);
                            thingop = new Var(rtthing, leftname);
                        }

                        if (vc.is_in(leftname))
                        {
                            vc.replace(leftname, thingop);
                        }
                        else
                        {
                            vc.add(thingop);
                        }

                        continue;

                    }

                    if (pts[0].equalsIgnoreCase("thing") && !pts[2].equals("is") &&
                            !pts[2].equals("=") && !pts[2].equals("copies"))
                    {

                        String mk = pts[pts.length - 1];

                        if (mk.startsWith("inline_code%"))
                        {
                            mk = mk.substring(12, mk.length() - 1);

                            try
                            {
                                mk = inlines[Integer.parseInt(mk)];
                            }
                            catch (Exception e)
                            {
                                encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "Thing had a body, we could not use it", s), vc, attempting);
                                continue;
                            }

                        }
                        else
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "This 'thing' is missing a body", s), vc, attempting);
                            continue;
                        }

                        ThingType tt = new ThingType(pts[1], mk, ttc, OUT, IN);
			/*
			thing t extends LALA implements one, two, three {}
			*/
			
			tt.frontload(ttc,this);
			
                        if (pts.length >= 5)
			{
				if (pts[2].equalsIgnoreCase("extends"))
				{

					if (ttc.is_in(pts[3]))
					{
						ThingType ancest = ttc.get(pts[3]);
						tt.setAncestor(ancest);
					}
					else
					{
						encountered_error = true;
						OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "This thing's ancestor '" + pts[3] +
                                                                                "' is not defined yet\n->no 'thing' created due to questionable ancestry", s), vc, attempting);
						continue;
					}
				}
				else if (pts[2].equalsIgnoreCase("implements")||pts[2].equalsIgnoreCase("wears"))
				{
					String[] interfaces = ThingInterface.interfaces(pts,3);
					tt.setInterfaces(interfaces);
				}

                        }
			if ((pts.length >= 5) 
				&& (!pts[2].equalsIgnoreCase("implements")&&!pts[2].equalsIgnoreCase("wears"))
				&& (pts[4].equalsIgnoreCase("implements")||pts[4].equalsIgnoreCase("wears"))
				)
			{
				tt.setInterfaces(ThingInterface.interfaces(pts,5));
			}
				StringBuffer extra =new StringBuffer();
				boolean baba=false;
			if (!ttc.tempAddThingType(pts[1]))
			{
				OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.THG,"This thing type exists already:"+pts[1]),vc,attempting);
				continue;
			}
                        try{baba = tt.createPrototype(attempting);
					}
					catch(Exception woops)
						{
						extra.append("The error was:").append(woops.toString());
						baba=false;
						}
                        if (!baba)
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "We were unable to create the thing '" + pts[1] + "'\n"+extra.toString(), s), vc, attempting);
                        }
                        else
                        {
                            ttc.replace(pts[1],tt);

                        if (USER_DEBUG)
                            {
                                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.THG, "Created thing-type '" + pts[1] + "'", true));
                            }
                        }
			//ttc.disallow(); //removes "OK" for undefined interfaces/thing-types
                        continue;

                    }

                    if (pts[0].equalsIgnoreCase("create"))
                    {
                        if (pts.length <= 3)
                        {
                            String[] nwparts = new String[pts.length + 2];
                            for (int i = 0; i < pts.length; i++)
                            {
                                nwparts[i] = pts[i];
                            }
                            nwparts[pts.length] = "using";
                            nwparts[pts.length + 1] = "~";
                            s.resetParts(nwparts);
                        }
                        if (ttc.is_in(pts[1]))
                        {
                            ThingType t = ttc.get(pts[1]);
                            Var thingvar = t.createThing(s, vc,this);

                            if (thingvar.getName().equals("~"))
                            {
                                encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "Failed at creating a thing of type '" + pts[1] + "'", s), vc, attempting);

                                continue;
                            }

                            Var me = new Var(thingvar.getThing(), "me");
                            if (!vc.is_in(thingvar.getName()))
                            {
                                vc.add(thingvar);
                            }
                            else
                            {
                                Var tgy = vc.get(thingvar.getName());
				String tgys = tgy.asString(); //better than stating it three times in else-if below
                                if (tgy.getType() != Var.THING)
                                {
                                    encountered_error = true;
                                    OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "Cannot use create with a non-thing var", s), vc, attempting);
                                    continue;
                                }
                                else if (tgys.equals(">NOVALUE<") || tgys.equals("thing::" + thingvar.getName())||(tgy.getThing() == Thing.UNDEF)/*||tgy.equals(">>NULL<<")*/)
                                {
                                    vc.replace(thingvar.getName(), thingvar);
                                    continue;
                                }
                                else
                                {	//System.out.println(thingvar.getName()+":name:"+thingvar.getThing().getName()+":thingname:"+thingvar.getThing().getThingType()+":ttype");
                                    encountered_error = true;
                                    OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "Cannot re-assign a thing with 'create'", s), vc, attempting);
                                    continue;
                                }
                            }
			    //old way... thing's now set
			    //their 'me' var internally, early
                            //thingvar.getThing().setMeVar(me);

                            continue;

                        }
                        else
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "Thing-type '" + pts[1] + "' has not been defined", s), vc, attempting);

                            continue;
                        }

                    }

                    boolean copied = false;

                    if (pts[1].equals("copies") || pts[2].equals("copies"))
                    {
                        boolean reassign = false;

                        if (pts[1].equals("copies"))
                        {
                            reassign = true;
                        }

                        int lt = -1;
                        int rt = -1;

                        if (reassign)
                        {
                            lt = 0;
                            rt = 2;
                        }
                        else
                        {
                            lt = 1;
                            rt = 3;
                        }

                        if (vc.is_in(pts[rt]))
                        {

                            Var rg = vc.get(pts[rt]);

                            if (rg.getType() == Var.THING)
                            {
                                Thing right = vc.get(pts[rt]).getThing();
                                Var nw = new Var(Thing.copy(right, pts[lt]), pts[lt]);

                                try
                                {
                                    Var rthg = nw.getThing().getVarContainer().get("me");
                                    rthg.setValue(nw.getThing());
                                }
                                catch (Exception eev)
                                {
                                    encountered_error = true;
                                    OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THG, "We failed at setting 'me' in a copy of a thing", s),
                                                vc, attempting);
                                }

                                if (reassign)
                                {
                                    if (vc.is_in(pts[lt]))
                                    {
                                        vc.replace(pts[lt], nw);
                                        copied = true;
                                    }
                                }
                                else
                                {
                                    vc.add(nw);
                                    copied = true;
                                }

                            }

                        }

                    }

                    if (copied)
                    {
                        continue;
                    }
		    //System.out.println("pts[0]=="+pts[0]);
                    if (pts[1].equals("copies") || pts[2].equals("copies"))
                    {

                        boolean b = StringFunct.copyCommand(s, vc, OUT);
                        if (!b)
                        {
                            encountered_error = true;
                        }
                        continue;
                    }

                }

                if ((pts.length >= 3) && (pts[1].equals("is_now") || pts[2].equalsIgnoreCase("is")))
                {
                    if (pts.length == 4)
                    {
                        if (pts[0].equalsIgnoreCase("group") && vc.is_in(pts[3]))
                        {
                            Var right = vc.get(pts[3]);
                            if (right.getType() == Var.GROUP)
                            {
                                vc.add(new Var(right.getVarGroup(), pts[1]));
                                continue;
                            }
                        }
                        /*do nothing else for now "group g is othergroup;*/
                    }

                    else if (pts.length == 3)
                    {
                        if (vc.is_in(pts[0]) && vc.is_in(pts[2]))
                        {
                            Var left = vc.get(pts[0]);
                            Var right = vc.get(pts[2]);
                            if ((left.getType() == Var.GROUP) && (right.getType() == Var.GROUP))
                            {
                                left.setValue(right.getVarGroup());
                                continue;
                            }
                        }
                        /*again, don't assume this is a bad statement yet*/
                    }
                }

                if (pts.length >= 4)
                {

                    if (DEBUG)
                    {
                        OUT.println("pts[0]==" + pts[0] + ", pts[3]==" + pts[3] + " and ac.is_in():" + ac.is_in(pts[3]), 2);
                    }

                    if (pts[0].equalsIgnoreCase("group") && ac.is_in(pts[3]))
                    {
                        String mm = "Processing the action '" + pts[3] + "' ";
                        boolean bap = ActionProcessor.process(ac, s, vc, ttc, OUT, IN);

                        if (bap == true)
                        {
                            mm = mm + "succeeded.";
                        }
                        else
                        {
                            encountered_error = true;
                            mm = mm + "did not succeed.";
                        }

                        if (USER_DEBUG)
                        {
                            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.ACT, mm, true));
                        }

                        continue;

                    }

                }

                if ((pts.length >= 3) && vc.is_in(pts[0]) && ac.is_in(pts[2]))
                {

                    String mm = "Processing the action '" + pts[2] + "' ";
                    boolean bap = ActionProcessor.process(ac, s, vc, ttc, OUT, IN);

                    if (bap == true)
                    {
                        mm = mm + "succeeded.";
                    }
                    else
                    {
                        encountered_error = true;
                        mm = mm + "did not succeed.";
                    }

                    if (USER_DEBUG)
                    {
                        OUT.println(new DSOut(DSOut.STD_OUT, DSOut.ACT, mm, true));
                    }

                    continue;
                }

                for (int z = 0; z < pts.length; z++)
                {
                    if (pts[z].equals("location_of"))
                    {
                        String[] locstr = new String[pts.length - z - 1];

                        for (int y = (z + 1); y < pts.length; y++)
                        {
                            locstr[y - (z + 1)] = pts[y];
                        }

                        String rp = StringFunct.locationOf(locstr, s, vc, OUT);
                        String[] nwstr = new String[(z + 1)];

                        for (int w = 0; w < z; w++)
                        {
                            nwstr[w] = pts[w];
                        }

                        nwstr[z] = rp;
                        pts = nwstr;
                        s.resetParts(pts);

                        if (DEBUG)
                        {

                            for (int mm = 0; mm < pts.length; mm++)
                            {
                                OUT.println("pts[" + mm + "]:" + pts[mm], 2);
                            }
                        }

                        break;
                    }
                }

                /*RANDOM INTEGER--removed*/	
                
                /*Whole section starts the IF blocks*/

                /*currently done twice, once here, one after parantheses pull*/
		//AssignChooser.dump(pts);
                if (pts[0].equalsIgnoreCase("if") || pts[0].equalsIgnoreCase("as_long_as"))
                {
                    /*this is a comparator, we are now checking for unusual circumstances*/
                    if (If.evaluateSpecial(s, vc, ac, ttc, OUT, IN))
                    {
                        pts = s.getParts();
                    }

                    /*THIS IS TO ALLOW () for if statements*/

                    if ((pts.length > 1) && pts[1].startsWith("eq_"))
                    {
                        Var vv = null;
                        String dat = "";
                        if (vc.is_in(pts[1]))
                        {
                            vv = vc.get(pts[1]);
                            if (vv.getAbsoluteType() == Var.EQUATE)
                            {
                                dat = vv.getEquation().getData();
				//added + and -
                                if (((dat.indexOf("*") > 0) || (dat.indexOf("/") > 0) || (dat.indexOf("+")>0) || (dat.indexOf("-")>0)) && (pts.length > 3))
                                {
                                    dat = "BAD";
                                }
                            }
                        }
                        if (dat.equals("BAD") && (pts.length > 3))
                        {
                            if (DEBUG)
                            {
                                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, pts[1] + ", " + pts[3], s));
                            }
                        }
                        else
                        {
                            if (DEBUG)
                            {
                                for (int gg = 0; gg < pts.length; gg++)
                                {
                                    OUT.println(pts[gg], 2);
                                }
                            }
                            Equation eqqy = vc.get(pts[1]).getEquation();
                            eqqy.replaceAnyLiterals();
                            String eq_stuff = eqqy.getData();

                            if (eq_stuff.startsWith("("))
                            {
                                eq_stuff = eq_stuff.substring(1, eq_stuff.length());
                            }
                            if (eq_stuff.endsWith(")"))
                            {
                                eq_stuff = eq_stuff.substring(0, eq_stuff.length() - 1);
                            }

                            String altered_state = pts[0] + " ";
                            altered_state = altered_state + eq_stuff;

                            for (int kk = 2; kk < pts.length; kk++)
                            {
                                altered_state = altered_state + " " + pts[kk];
                            }

                            Statement eq_ = new Statement(altered_state);
                            eq_.create();
                            eq_.setInline(s.getInline());
                            eq_.setAttempting(attempting);
                            s = null;
                            s = eq_;
                            pts = s.getParts();

                            String[] eq_refs = s.getEquations();

                            for (int u = 0; u < eq_refs.length; u++)
                            {
                                String eqname = "eq_" + EQUACOUNT;
                                EQUACOUNT++;
                                s.replace("eq_temp_" + u, eqname);
                                Var eqvar = new Var(new Equation(eqname, eq_refs[u], vc), eqname);
                                eqvar.getEquation().setLiterals(s.getRefs(), s.getCharRefs());
                                vc.add(eqvar);
                                eqvar.setAsTransient();
                            }
                        }

                    }

                    /*this is a comparator, we are now checking for unusual circumstances-second time*/
		    //AssignChooser.dump(pts);
		    If.evaluateSpecial(s, vc, ac, ttc, OUT, IN);
                    pts = s.getParts();


                    pts = If.adjustForMinus(pts);
		    /*??*/
		    s.resetParts(pts);
                    /*THIS SECTION SHOULD INSERT FOR A SINGLE VALUE COMPARATOR*/
                    if ((pts.length > 2) && (pts[2].startsWith("inline")))
                    {
                        String[] newalt = new String[pts.length + 2];
                        newalt[0] = pts[0];
                        newalt[1] = pts[1];
                        newalt[2] = "==";
                        newalt[3] = "yes";
                        for (int g = 4; g < newalt.length; g++)
                        {
                            newalt[g] = pts[(g - 2)];
                        }
                        pts = newalt;
                        s.resetParts(pts);
                    }

                    /*!!!~ IF-COMPARATIVES~'AS_LONG_AS'~!!!!*/
                    if (pts[0].equalsIgnoreCase("as_long_as"))
                    {
                        /*needed to ensure that original statements w/ actions etc are pristine*/

                        String[] state_master = new String[pts.length];
			System.arraycopy(pts,0,state_master,0,pts.length);

                        /*
			for (int wv = 0; wv < state_master.length; wv++)
                        {
                            state_master[wv] = pts[wv];
                        }
			*/
                        If.checkForActions(s, vc, ac, ttc, OUT, IN);
			/*put in here as well*/
			If.evaluateSpecial(s, vc, ac, ttc, OUT, IN);
                        /*they've either been changed, or are the same, but if changed, below is NECESSARY*/
                        pts = s.getParts();
                        pts = If.adjustForMinus(pts);
                        s.resetParts(pts);
                        BREAKPOINT = runlevel;
                        breakpoint = runlevel;
                        if (pts.length < 5)
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, "This 'as long as' statement is too short", s),
                                        vc, attempting);
                            continue;
                        }

                        boolean hmm = If.evaluate(s, vc, ttc, OUT, IN);
                        String to_exec = "";

                        if (!hmm)
                        {
                            continue;
                        }

                        if (pts[4].indexOf("inline") < 0)
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, "This 'as long as' statement has no execution(body)!", s)
                                        , vc, attempting);

                            continue;
                        }

                        String tp = "";

                        for (int x = 0; x < pts[4].length(); x++)
                        {
                            char c = pts[4].charAt(x);

                            if (Character.isDigit(c))
                            {
                                tp = tp + c;
                            }

                        }

                        try
                        {
                            int indx = Integer.parseInt(tp);
                            to_exec = inlines[indx];
                        }
                        catch (Exception e)
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, "We expected this 'as long as' statement\n\t.." +
                                                                           "to have a body but it didn't", s), vc, attempting);
                            to_exec = "";
                        }

                        if (USER_DEBUG)
                        {
                            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.IF, "We are now entering an 'as_long_as' loop", true));
                        }
			VarContainer wrap=new VarContainer(OUT);
			wrap.add(new Var(new VCWrapper(vc)));
			
			StatementProcessor texec = new StatementProcessor(to_exec,wrap,ac,ttc,OUT,IN);
			texec.suppress(); texec.setNextUp(this);
			
                        while (hmm)
                        {
				//AssignChooser.dump(pts);
				
                            while (!nextUpStop() && hibernating())
                            {
                            }
                            if (nextUpStop())
                            {
                                hmm = false;
                                break;
                            }
			    //new
			    texec.run();
			    wrap.clearTopLevel();
			    GAVE=texec.GAVE;
			    didBreak=texec.didBreak;
			    FAILED=texec.FAILED;
			    if (GAVE || didBreak || FAILED){break;}
			    //if (didBreak) {break;}
			  
                            s.resetParts(state_master);
                            If.checkForActions(s, vc, ac, ttc, OUT, IN);
			    If.evaluateSpecial(s, vc, ac, ttc, OUT, IN);
                            pts = s.getParts();
                            pts = If.adjustForMinus(pts);
                            s.resetParts(pts);
                            hmm = If.evaluate(s, vc, ttc, OUT, IN);

                            if (RUNLEVEL <= runlevel || nextUpStop())
                            {
                                if (USER_DEBUG)
                                {
                                    OUT.println(new DSOut(DSOut.STD_OUT, DSOut.IF, "We have forcibly broken out of a loop!", true));
                                }
                                hmm = false;
                            }

                        }

                        if (USER_DEBUG)
                        {
                            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.IF, "We are now leaving an 'as_long_as' loop", true));
                        }

                        continue;

                    }

                    if (pts[0].equalsIgnoreCase("if"))
                    {
                        if (DEBUG)
                        {
                            for (int g = 0; g < pts.length; g++)
                            {
                                OUT.println("pts[" + g + "]:" + pts[g], 2);
                            }
                        }
                        If.checkForActions(s, vc, ac, ttc, OUT, IN);
                        /*added two thirds of this, one in 'if' one in 'as long as'*/
			If.evaluateSpecial(s,vc,ac,ttc,OUT,IN);

			/*they've either been changed, or are the same, but if changed, below is NECESSARY*/
                        pts = s.getParts();

                        pts = If.adjustForMinus(pts);
                        s.resetParts(pts);

                        if (pts.length < 5)
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, "This 'if' statement is too short", s),
                                        vc, attempting);
                            continue;
                        }

                        boolean hmm = If.evaluate(s, vc, ttc, OUT, IN);
                        String to_exec = "";
                        if (DEBUG)
                        {
                            OUT.println("If statement eval'd as " + hmm, 2);
                        }
                        if (hmm)
                        {

                            if (pts[4].indexOf("inline") < 0)
                            {
                                encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, "This 'if' statement has no execution(body)", s),
                                            vc, attempting);
                                continue;
                            }

                            String tp = "";

                            for (int x = 0; x < pts[4].length(); x++)
                            {
                                char c = pts[4].charAt(x);

                                if (Character.isDigit(c))
                                {
                                    tp = tp + c;
                                }

                            }

                            try
                            {
                                int indx = Integer.parseInt(tp);
                                to_exec = inlines[indx];
                            }
                            catch (Exception e)
                            {
                                encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, "This 'if' statement has no execution(body!)", s),
                                            vc, attempting);
                                to_exec = "";
                            }

                        }
                        else
                        {

                            if (pts.length == 7)
                                try
                                {

                                    if (pts[6].indexOf("inline") < 0)
                                    {
                                        encountered_error = true;
                                        OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, "This 'else' statement has no execution!", s),
                                                    vc, attempting);
                                        continue;
                                    }

                                    String tp = "";

                                    for (int x = 0; x < pts[6].length(); x++)
                                    {
                                        char c = pts[6].charAt(x);

                                        if (Character.isDigit(c))
                                        {
                                            tp = tp + c;
                                        }

                                    }

                                    int indx = Integer.parseInt(tp);
                                    to_exec = inlines[indx];

                                }
                                catch (Exception ee)
                                {
                                    encountered_error = true;
                                    to_exec = "";
                                    OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.IF, "There is an empty execution in 'else'", s),
                                                vc, attempting);
                                }

                        }

                        if (!to_exec.equals(""))
                        {
                            If.exec(to_exec, vc, ac, ttc, OUT, IN,this);
                        }

                        continue;

                    }
                }

                /*PAUSE statements*/
                if (pts[0].equalsIgnoreCase("pause"))
                {

                    if (pts.length != 4)
                    {
                        encountered_error = true;
                        OUT.println(new DSOut(DSOut.ERR_OUT, -1, "This is an incomplete (too short) 'pause' statement", s),
                                    vc, attempting);
                        continue;
                    }

                    if (pts[1].equalsIgnoreCase("for") == false)
                    {
                        encountered_error = true;
                        OUT.println(new DSOut(DSOut.ERR_OUT, -1, "Dustyscript was expecting 'for' and got '" + pts[1] + "'", s),
                                    vc, attempting);
                        continue;
                    }

                    double d = 0.00;

                    try
                    {
                        d = Double.parseDouble(pts[2]);
                    }
                    catch (Exception e)
                    {
                        try
                        {
                            Var vv = vc.get(pts[2]);
                            d = Double.parseDouble(vv.asString());
                        }
                        catch (Exception ee)
                        {
                            encountered_error = true;
                            d = 0.00;
                            OUT.println(new DSOut(DSOut.ERR_OUT, -1, "Pause requires an integer or decimal value for seconds|minutes'", s),
                                        vc, attempting);
                            continue;
                        }
                    }

                    if ((pts[3].equalsIgnoreCase("seconds")) || (pts[3].equalsIgnoreCase("second")))
                    {
                        Pause.spause(d);
                        continue;
                    }

                    if ((pts[3].equalsIgnoreCase("minutes")) || (pts[3].equalsIgnoreCase("minute")))
                    {
                        Pause.mpause(d);
                        continue;
                    }

                    encountered_error = true;
                    OUT.println(new DSOut(DSOut.ERR_OUT, -1, "Dustyscript was expecting 'seconds' or 'minutes' and got '" + pts[3] + "'", s),
                                vc, attempting);
                    continue;
                }

                boolean asserting = false;
                boolean direction = false;

                if (pts[0].equalsIgnoreCase("pcount"))
                {
                    pts[0] = "count";
                    asserting = true;
                    direction = true;
                }

                if (pts[0].equalsIgnoreCase("ncount"))
                {
                    pts[0] = "count";
                    asserting = true;
                    direction = false;
                }

                if (pts[0].equalsIgnoreCase("count"))
                {
                    try
                    {
                        Count.count(s, vc, ac, ttc, OUT, IN, asserting, direction, this);
                        if (USER_DEBUG)
                        {
                            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.CNT, "This count was successful", true));
                        }

                    }
                    catch (Exception badcount)
                    {
                        if (USER_DEBUG)
                        {
                            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.CNT, "We did not succeed with this count", true));
                        }
                    }

                    continue;
                }

                boolean growitok = true;

                if (pts.length < 4)
                {
                    growitok = false;
                }

                if (DEBUG)
                {
                    OUT.println("Entering compound if (true & string[]eval) statements!", 2);
                }
		
		/*GROUP WAS HERE*/
		

		
                boolean proc_act = false;
                boolean sayop = false;
                int d = 0;
                if (pts[0].equalsIgnoreCase("say"))
                {
                    sayop = true;
                    d = 1;
                }
                for (; d < pts.length; d++)
                {

                    if (pts[d].indexOf(".") > -1)
                    {

                        String ck = pts[d].substring(0, pts[d].indexOf("."));

                        if (vc.is_in(ck,true))
                        {
                            proc_act = true;
                        }

                    }

                    if (ac.is_in(pts[d]))
                    {
                        proc_act = true;
                    }

                    if (pts[d].equals("+"))
                    {
                        sayop = true;
                    }

                    if (proc_act && sayop)
                    {
                        break;
                    }

                }

                if (DEBUG)
                {
                    OUT.println("processing action??:" + proc_act, 2);
		    for (int i=0; i<pts.length;i++)
		    {
			    OUT.println("pts["+i+"]=="+pts[i],2);
		    }
                }

                if (proc_act)
                {
                    boolean is_really_act = true;
                    try
                    {
                        if(pts.length < 4) {
                            is_really_act = true;
                        } else if (ttc.is_in(pts[3]) && vc.is_in(pts[1]) && (vc.get(pts[1]).getType() == Var.THING) &&
                                   (pts[2].indexOf("is_type") > -1)) {
                            is_really_act = false;
                        }
                    }
                    catch (Exception e)
                    {
                        is_really_act = true;
                    }

                    if (!sayop && is_really_act)
                    {
                        String mm = "Processing of action ";
                        boolean bap = ActionProcessor.process(ac, s, vc, ttc, OUT, IN);

                        if (bap == true)
                        {
                            mm = mm + "succeeded.";
                        }
                        else
                        {
                            encountered_error = true;
                            mm = mm + "did not succeed.";
                        }

                        if (USER_DEBUG)
                        {
                            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.ACT, mm, true));
                        }
				if (encountered_error)
					{
					OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ACT, mm, s),vc,attempting);
					}
                        continue;

                    }
                    if (is_really_act)
                    {
                        ActionSplitter.split(s, ac, vc, ttc, OUT, IN);
                        pts = s.getParts();
                    }
                }

                pts = If.adjustForMinus(pts);
                s.resetParts(pts);
		/*NEW SPOT FOR GROUP*/
				/*START:GROUP*/
		
                try
                {

                    if (growitok)
                    {
                        if (pts[0].equalsIgnoreCase("grow") && pts[2].equalsIgnoreCase("by"))
                        {
                            Var vv = vc.get(pts[1]);

                            if (vv.getType() != Var.GROUP)
                            {
                                encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, -1, "You can only 'grow' a group variable", s),
                                            vc, attempting);
                                continue;
                            }

                            VarGroup vg = vv.getVarGroup();
                            int start = vg.size();
                            int qt = 0;

                            try
                            {
                                qt = Integer.parseInt(pts[3]);
                            }
                            catch (Exception e)
                            {
                                try
                                {
                                    qt = Integer.parseInt(vc.get(pts[3]).asString());
                                }
                                catch (Exception ez)
                                {
                                    encountered_error = true;
                                    OUT.println(new DSOut(DSOut.ERR_OUT, -1, "An integer is required for 'grow'ing", s),
                                                vc, attempting);
                                    continue;
                                }

                            }

                            for (int x = 0; x < qt; x++)
                            {
                                vg.add(new Var("" + (start + x + 1), vg.getType()),vc);
                            }

                            continue;

                        }

                        if (pts[0].equalsIgnoreCase("shrink") && (pts.length > 3) && pts[2].equalsIgnoreCase("by"))
                        {
                            Var vv = vc.get(pts[1]);

                            if (vv.getType() != Var.GROUP)
                            {
                                encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, -1, "You can only 'shrink' a group variable", s),
                                            vc, attempting);
                                continue;
                            }

                            VarGroup vg = vv.getVarGroup();
                            int qt = 0;

                            try
                            {
                                qt = Integer.parseInt(pts[3]);
                            }
                            catch (Exception e)
                            {
                                try
                                {
                                    qt = Integer.parseInt(vc.get(pts[3]).asString());
                                }
                                catch (Exception ez)
                                {
                                    encountered_error = true;
                                    OUT.println(new DSOut(DSOut.ERR_OUT, -1, "You must 'shrink..by' an integer value", s),
                                                vc, attempting);
                                    continue;
                                }

                            }

                            vg.trim(qt);
                            continue;

                        }

                        String[] bakk = pts;

                        pts = If.adjustForMinus(pts);
                        if (pts.length >= 3 && pts[1].equalsIgnoreCase("is_now") && vc.is_in(pts[0]) &&
                                vc.get(pts[0]).getType() == Var.GROUP && !pts[2].equals(pts[0]))
                        {

                            DSOut this_error = new DSOut(DSOut.ERR_OUT, DSOut.ASS, "Error at light-copying a group var", s);
                            DSOut this_success = new DSOut(DSOut.STD_OUT, DSOut.ASS, "We successfully copied this group var", true);

                            Var vvg = vc.get(pts[0]);
                            if (vvg.getType() == Var.GROUP)
                            {

                                boolean reverse = false;

                                if (pts[2].startsWith("-"))
                                {
                                    reverse = true;
                                    pts[2] = pts[2].substring(1, pts[2].length());
                                }

                                if (!vc.is_in(pts[2]) || (vc.get(pts[2]).getType() != Var.GROUP))
                                {
                                    encountered_error = true;
                                    OUT.println(this_error, vc, attempting);
                                    continue;
                                }

                                if (reverse)
                                {
                                    vc.replace(pts[0], new Var(VarGroup.copy(vc.get(pts[2]).getVarGroup(), pts[0],vc), pts[0]).reverseValue());
                                }
                                else
                                {
                                    vc.replace(pts[0], Var.copy(vc.get(pts[2]), pts[0],vc));
                                }

                                if (USER_DEBUG)
                                {
                                    OUT.println(this_success);
                                }
                            }
                            else
                            {
                                encountered_error = true;
                                OUT.println(this_error, vc, attempting);
                            }

                            continue;

                        }
                        else
                        {
                            pts = bakk;
                        }

                    }

                    if ((pts.length > 1) && pts[1].equalsIgnoreCase("contains"))
                    {
                        Var vv = vc.get(pts[0]);

                        if (vv.getType() != Var.GROUP)
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "You cannot use 'contains' w/ a non-group variable", s),
                                        vc, attempting);
                            continue;
                        }

                        String[] states = s.getParts();

                        String[] newstates = new String[states.length + 1];
                        newstates[0] = new String("group");

			System.arraycopy(states,0,newstates,1,states.length);
                      //  for (int i = 0; i < states.length; i++)
                       // {
                        //    newstates[(i + 1)] = states[i];
                       // }

                        s.resetParts(newstates);
                        Var other = VarGroup.make(s, vc, OUT);

                        if (other.getType() != Var.GROUP)
                        {
                            encountered_error = true;
                            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "You cannot mix 'group' with 'non-group' this way", s),
                                        vc, attempting);
                            continue;
                        }

                        vv.setValue(other.getVarGroup());

                        if (USER_DEBUG)
                        {
                            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.ASS, "We successfully set this group's type", true));
                        }

                        continue;

                    }

                    if (pts[0].equalsIgnoreCase("group"))
                    {
			boolean group_bool=true;
                        if (pts.length > 3 && pts[2].equalsIgnoreCase("is"))
                        {
                            pts = If.adjustForMinus(pts);
                            boolean reverse = false;

                            if (pts[3].startsWith("-"))
                            {
                                pts[3] = pts[3].substring(1, pts[3].length());
                                reverse = true;
                            }

                            if (vc.is_in(pts[3]))
                            {
                                VarGroup vg = vc.get(pts[3]).getVarGroup();
                                if (reverse)
                                {
                                    vc.add(new Var(VarGroup.createReverse(vg, pts[1]), pts[1]));
                                }
                                else
                                {
                                    vc.add(new Var(vg, pts[1]));
                                }

                                if (USER_DEBUG)
                                {
                                    OUT.println(new DSOut(DSOut.STD_OUT, DSOut.ASS, "We successfully light-copied this group", true));
                                }

                                continue;

                            }
                            else
                            {
				if (vc.is_in(pts[3],true)){group_bool=false;}
                                if(group_bool)
				{
				encountered_error = true;
                                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "Failed at light-copy of group var " + pts[3], s),
                                            vc, attempting);
					    continue;
				}
			    }
                            /*continue;*/

                        }
			
			if (group_bool)
			{
                        Var vv = VarGroup.make(s, vc, OUT);

                        if (!vv.getName().equalsIgnoreCase(">NULL<"))
                        {
                            vc.add(vv);

                            if (USER_DEBUG)
                            {
                                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.ASS, "Successfully created group", true));
                            }

                            continue;
                        }
                        encountered_error = true;
                        OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "We could not create a group due to previous mistake", s),
                                    vc, attempting);
                        continue;

			}
		      /*end if(group_bool)*/
		     }
                }
                catch (Exception wow)
                {
                    encountered_error = true;
                    OUT.println("There was an error from outside of your program\n");
                    wow.printStackTrace();

                    if (DEBUG)
                    {
                        OUT.println(wow.toString(), 2);
                    }

                }
		/*END GROUP SECTION*/
		
		
                if (!pts[0].equalsIgnoreCase("say"))
                {

                    try
                    {
                        Assign.exec(vc, s, OUT, IN);

                        if (USER_DEBUG)
                        {
                            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.ASS, "Successfully assigned a variable", true));
                        }

                    }
                    catch (Exception e)
                    {
                        encountered_error = true;
                        OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "Failed at assigning a variable", s),
                                    vc, attempting);
				if (attempting) {RUNLEVEL = FAILPOINT; return;}
                    }

                    continue;

                }

                if (DEBUG)
                {
                    OUT.println("Preparing to 'say'", 2);
                }

                boolean sayb = Say.exec(vc, s, OUT);
		    if (!sayb && attempting)
				{
				RUNLEVEL=FAILPOINT;
				return;
				}
            }

            if (!suppress)
            {
                OUT.println("\n============================\n...finished running" +
                            " this program\n============================\n");
            }
        }
        catch (OutOfMemoryError oom)
        {	FAILED=true;
            try{OUT.println(oomerr,vc,attempting);}catch(Exception e){OUT.println(oomerr);}
        }
        catch (StackOverflowError sof)
        {	FAILED=true;
            try{OUT.println(stckov,vc,attempting);}catch(Exception e){OUT.println(stckov);}
        }
        catch (Exception oth_)
        {	FAILED=true;
            OUT.println(othererr);
            OUT.println("The error is:\n" + oth_.toString());
	    OUT.println(new DSOut(DSOut.ERR_OUT,-1,oth_.toString(),last_state));
	    oth_.printStackTrace();
        }
        if ((threadvar != null) && threadvar.getName().startsWith("Anonymous"))
        {
            threadvar.setAsTransient();
            threadvar.setUsedBit(true);
            vc.remove(threadvar);
        }

        if (BENCHMARK)
        {
            long endbm = System.currentTimeMillis();
            ELAPSED += (endbm - startbm);
            ITEMS++;
            double d = 0.0;
            if (ITEMS != 0)
            {  double e = (double)ELAPSED;
		   double i = (double)ITEMS*secdev;
                d = e/i;
            }
            String mssg = new StringBuffer().append("Average time per SP.run() in seconds:").append(d).append(" over ").append(ITEMS).append(" runs").toString();
            System.out.println(mssg);
        }
    }

    /*
        Returns 'false' to indicate fatal error.
    */
    private boolean do_convert(String[] pts, Statement s)
    {
        boolean failed = false;

        int tp = -1;
        String vname = "";

        if (pts.length == 5)
        {
            vname = pts[4];
            tp = Var.typeInt(pts[3]);
        }
        else
        {
            vname = pts[3];
            if (vc.is_in(pts[3]))
            {
                tp = vc.get(pts[3]).getType();
            }
            else
            {
                tp = -1;
            }
        }

        try
        {
            Var cvt = Var.convert(vc, pts[1], tp, vname);
            if (vc.is_in(vname))
            {
                vc.replace(vname, cvt);
            }
            else
            {
                vc.add(cvt);
            }
            failed = false;
        }
        catch (Exception cvtexcep)
        {
            encountered_error = true;
            failed = true;
        }
        if (failed)
        {
            DSOut dso = new DSOut(DSOut.ERR_OUT, DSOut.ASS, "these types are incompatible", s);
            if (attempting)
            {
                Var am = new Var(dso.getMessage(), "ATTEMPT_MESSAGE");
                if (vc.is_in("ATTEMPT_MESSAGE"))
                {
                    vc.get("ATTEMPT_MESSAGE").setValue(dso.getMessage());
                }
                if (vc.is_in("DUSTY_ERROR_CODE"))
                {
                    vc.get("DUSTY_ERROR_CODE").setValue("ASSIGN");
                }
                RUNLEVEL = FAILPOINT;
                return false;
            }

            OUT.println(dso);
        }
        else if (USER_DEBUG)
        {
            OUT.println(new DSOut(DSOut.STD_OUT, DSOut.ASS, "We sucessfully converted from one var type to another", true));
        }

        return true;
    }

    private void do_fail_with(String[] pts, Statement s)
    {
        RUNLEVEL = FAILPOINT;
        FAILED = true;

        Var flvar = vc.get("ATTEMPT_MESSAGE");

        if (vc.is_in("DUSTY_ERROR_CODE"))
        {
            Var flecvar = vc.get("DUSTY_ERROR_CODE");
            flecvar.setValue("UNKNOWN::" + flecvar.asString());
        }

        boolean us = true;

        if (pts.length == 1)
        {
            flvar.setValue("Error not available");
            us = false;
        }
        else if ((pts[1].startsWith("\"") && pts[1].endsWith("\"")) || (pts[1].startsWith("\'") && pts[1].endsWith("\'")))
        {
            pts[1] = pts[1].substring(1, pts[1].length() - 1);
            flvar.setValue(pts[1]);
        }
        else if (vc.is_in(pts[1]))
        {
            flvar.setValue(vc.get(pts[1]).asString());
        }
        else if (pts[1].startsWith("$") && pts[1].endsWith("$"))
        {
            String mess = pts[1].substring(1, pts[1].length() - 1);
            try
            {
                int ref = Integer.parseInt(mess);
                String[] refs = s.getRefs();
                flvar.setValue(refs[ref]);
            }
            catch (Exception e)
            {
                flvar.setValue(pts[1]);
            }
        }
        else if (pts[1].startsWith("&") && pts[1].endsWith("&") && (pts[1].length() > 2))
        {
            String mess = pts[1].substring(1, pts[1].length() - 1);
            try
            {
                int ref = Integer.parseInt(mess);
                String[] chrrefs = s.getCharRefs();
                flvar.setValue(chrrefs[ref]);
            }
            catch (Exception e)
            {
                flvar.setValue(pts[1]);
            }
        }
        else
        {
            flvar.setValue(pts[1]);
        }

        flvar.USER_SET = us;
    }

    private void do_fail()
    {
        RUNLEVEL = FAILPOINT;
        FAILED = true;
        if (vc.is_in("ATTEMPT_MESSAGE"))
        {
            Var flvar = vc.get("ATTEMPT_MESSAGE");
            flvar.setValue("Error not available\n" + flvar.asString());
        }
        if (vc.is_in("DUSTY_ERROR_CODE"))
        {
            Var flecvar = vc.get("DUSTY_ERROR_CODE");
            flecvar.setValue("UNKNOWN::" + flecvar.asString());
        }
    }

    public void do_send_regvar(String[] pts, Statement s)
    {
	    if (pts.length < 4) 
	    {
		    OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"This 'sendvar' statement too short",s),vc,attempting);
		    return;
	    }
	    String[] ags = null;
	    if (pts.length > 5)
	    {
		    ags = new String[pts.length-5];
		    System.arraycopy(pts,5,ags,0,ags.length);
		    for (int m=0; m<ags.length;m++)
		    {
			 if (ags[m].endsWith(",")){ags[m]=ags[m].substring(0,ags[m].length()-1);}
			    ags[m]=StringFunct.pullStringLiteral(ags[m],s,vc);
			    
				if (ags[m].startsWith("\"")&&ags[m].endsWith("\""))
			 	{
				ags[m]=ags[m].substring(1,ags[m].length()-1);
				}
		    }
	    }
	    else{ags =new String[0];}
	    boolean b = JavaConnector.sendVariable(pts[1],vc,pts[3],ags);
	    if (!b) {encountered_error=true;}
	    if (USER_DEBUG)
	    {	String m = "at sending var '"+pts[1]+"' to java-connector '"+pts[3]+"'";
		    if (!b) {OUT.println(new DSOut(DSOut.STD_OUT,DSOut.JAV,"We failed "+m,true),vc,attempting);}
		    else {OUT.println(new DSOut(DSOut.STD_OUT,DSOut.JAV,"We succeeded "+m,true),vc,attempting);}
	    }
   
	    
	    /*sending var to dustyable*/
    }
    
    public void do_get_var(String[] pts, Statement s)
    {
	//System.out.println("do_get_var() called!");
	    //getvar VAR from JC
	    if (pts.length <4 )
	    {
		    OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.JAV,"This 'getvar' statement too short",s),vc,attempting);
		    return;
	    }
	    String[] ags=null;
	    if (pts.length > 5)
	    {	
		    ags=new String[pts.length-5];
		    System.arraycopy(pts,5,ags,0,ags.length);
		    //remove comma
		    for (int m=0; m<ags.length;m++) {if (ags[m].endsWith(",")){ags[m]=ags[m].substring(0,ags[m].length()-1);}
		    ags[m]=StringFunct.pullStringLiteral(ags[m],s,vc);
		    if ((ags[m].startsWith("\"")&&ags[m].endsWith("\""))||(ags[m].startsWith("\'")&&ags[m].endsWith("\'")))
			{
			ags[m]=ags[m].substring(1,ags[m].length()-1);
			//System.out.println("ags["+m+"]=="+ags[m]);
			}
		    }
		    	    
	    }
	    else {ags =new String[0];}
	    boolean b = JavaConnector.getVariable(pts[1],vc,pts[3],ags);
	    if (!b) {encountered_error=true;}
	    if (USER_DEBUG)
	    {
		    String m = "at getting var '"+pts[1]+"' from java-connector '"+pts[3]+"'";
		    if (!b) {OUT.println(new DSOut(DSOut.STD_OUT,DSOut.JAV,"We failed "+m,true),vc,attempting);}
		    else{
		    OUT.println(new DSOut(DSOut.STD_OUT,DSOut.JAV,"We succeeded "+m,true),vc,attempting);
		    }
	    }
	    /*getting a var (or creating it) from a java-connector*/
	    
    }
    
    public String clean_statement(String s)
    {
	    if (s.startsWith(";")) { s=s.substring(1,s.length());}
	    if (s.endsWith(";")) {s=s.substring(0,s.length()-1);}
	    return s;
    }
    
    
    
    public void do_send_dustyable(String[] pts, Statement s)
    {
        if (pts.length < 4)
        {
            encountered_error = true;
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.JAV, "This is too short a 'send' statement", s), vc, attempting);
            return;
        }

        boolean com_or_dusty = true;
        if (vc.is_in(pts[1]))
        {
            if (vc.get(pts[1]).getType() == Var.JAVA)
            {
                com_or_dusty = false;
            }
            else
            {
                com_or_dusty = true;
            }
        }

        Vector arggies = new Vector();

        if (pts.length > 5)
        {
            if (!pts[4].equals("using"))
            {
                encountered_error = true;
                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.JAV, "Dustyscript was expected 'using' but got '" + pts[4] + "'!", s), vc, attempting);
                return;
            }

            for (int i = 5; i < pts.length; i++)
            {
		if (pts[i].trim().equals(",")) {continue;}
                if (pts[i].endsWith(","))
                {
                    pts[i] = pts[i].substring(0, pts[i].length() - 1);
                }

                pts[i] = StringFunct.pullStringLiteral(pts[i], s, vc);
		if (pts[i].startsWith("\"") && pts[i].endsWith("\"") && (pts[i].length()>2))
		{
			pts[i]=pts[i].substring(1,pts[i].length()-1);
		}
		arggies.addElement(pts[i]);

            }
        }

        String[] arg = new String[arggies.size()];
	boolean debuggy=false;//true;
	
        for (int i = 0; i < arg.length; i++)
        {
            arg[i] = (String) arggies.elementAt(i);
	    if (debuggy) {System.err.println("Pre-JC call, arg["+i+"]=="+arg[i]);}
	
	}
        boolean b = false;

        if (com_or_dusty)
        {
            pts[1] = StringFunct.pullStringLiteral(pts[1], s, vc);

            if (arg.length < 1)
            {
                b = JavaConnector.sendCommand(pts[3], vc, pts[1]);
            }
            else
            {
                b = JavaConnector.sendCommand(pts[3], vc, pts[1], arg);
            }
        }
        else
        {
            if (arg.length < 1)
            {
                b = JavaConnector.sendDustyVar(pts[1], vc, pts[3]);
            }
            else
            {
                b = JavaConnector.sendDustyVar(pts[1], vc, pts[3], arg);
            }
        }

        if (b)
        {
            if (USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "Successfully sent '" + pts[1] + "' to '" + pts[3] + "'", true));
            }
        }
        else
        {
            encountered_error = true;

            if (USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "We failed at sending '" + pts[1] + "' to '" + pts[3] + "'", true));
            }
        }
    }

    private void do_benchmark(String[] pts, Statement s)
    {
        if (pts.length < 3)
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "Too short a benchmark statement", s));
            return;
        }

        Var bmvar = null;
        if (vc.is_in(pts[1]))
        {
            bmvar = vc.get(pts[1]);
            if (bmvar.getType() != Var.DEC)
            {
                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "'" + bmvar.getName() + "' is not a decimal" +
                                                                "\n\t..benchmark requires a decimal", s));
                return;
            }
        }
        else
        {
            bmvar = new Var(pts[1], Var.DEC);
            vc.add(bmvar);
        }

        if (!pts[2].startsWith("inline_co"))
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "This benchmark is missing a body to run", s));
            return;
        }

        String exec = "";

        try
        {
            int ix = Integer.parseInt(pts[2].substring(12, pts[2].length() - 1));
            exec = inlines[ix];
        }
        catch (Exception oe)
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.ASS, "This benchmark has a corrupted body", s));
            return;
        }

        StatementProcessor nw = new StatementProcessor(exec,vc,ac,ttc,OUT,IN);
        nw.suppress();
        nw.setAttempting(attempting);
        nw.THREADING=THREADING;
        nw.setNextUp(this);
        long start=System.currentTimeMillis();
        nw.run();
        long total = System.currentTimeMillis() - start;
        bmvar.setValue((double)(((double)total)/secdev));

        if (USER_DEBUG)
        {
            OUT.println("-->Successfully benchmarked a code-block");
        }
    }

    private String[] fixForFractions(String[] parts, VarContainer vc, ActionContainer ac)
    {
	if (parts.length < 3) {return parts;}
	
	boolean could_be=false;
	int mark=-1;
	
	if (parts[0].equalsIgnoreCase("fraction") && !parts[1].equalsIgnoreCase("using")) 
	{
		could_be=true;
		mark=3;
	}
	else if (vc.is_in(parts[0]) && (vc.get(parts[0]).getType()==Var.THING) && vc.get(parts[0]).getThing().getThingType().equals("Fraction") )
	{
		could_be=true;
		mark=2;
	}
	if ((mark >= parts.length) || (mark < 0)) {return parts;}

	if (parts[mark].indexOf(".") > -1) 
	{return parts; //already a compound action
	}
	
	if (vc.is_in(parts[mark]))
	{
		Var fr=vc.get(parts[mark]);
		int frtype=fr.getType();
		if (frtype == Var.THING)
		{
			if (fr.getThing().getThingType().equals("Fraction"))
			{
				return parts;
			}
		}
	}
	if (ttc.is_in("Fraction"))
		{
			ThingType fractt=ttc.get("Fraction");
			ActionContainer fracac=fractt.getActionContainer();
			String[] subparts=new String[(parts.length-mark)];
			//System.out.println(subparts.length+"spl");
			//System.out.println(parts.length+"pl");
			/*for (int xx=0;xx<parts.length;xx++)
			{System.out.println("\tparts["+xx+"]:"+parts[xx]);}
			System.out.println(mark+"mk");
			*/
			System.arraycopy(parts,mark,subparts,0,subparts.length);
			int[] argus=new int[subparts.length];
			for (int i=0; i<argus.length; i++)
			{
			argus[i]=ActionProcessor.getPartType(subparts[i],null,vc);
			}
			if (fracac.is_in("Fraction",argus,-1))
			{
				could_be=true;
			}
		}
	
	
	if (!could_be || (mark >= parts.length)) {return parts;}
	
	
	
	//OUT.println("parts[mark]="+parts[mark]);
	if (only_one_div(parts[mark]) )
	{
		String argu="";
		for (int i=0;i<parts[mark].length();i++)
		{
			char c=parts[mark].charAt(i);
			if ((c == '(') || (c == ')'))
			{
				continue;
			}
			else if (c=='/')
			{
				argu=argu+" ";
			}
			else
			{
				argu=argu+c;
			}
		}
		return replaceAsFraction(parts,mark,argu);
	}
	if ( vc.is_in(parts[mark]) && (vc.get(parts[mark]).getAbsoluteType() == Var.EQUATE))
	{
	parts[mark]=""+vc.get(parts[mark]).getDecimal();
	}
	/*	String datastring = vc.get(parts[mark]).getEquation().getData();
		//if (only_one_div(datastring)) 
		//{
		String argu="";
		
		for (int i=0;i<datastring.length();i++)
		{
			char c=datastring.charAt(i);
			if ((c == '(') || (c == ')'))
			{
				continue;
			}
			else if (c=='/')
			{
				argu=argu+" ";
			}
			else
			{
				argu=argu+c;
			}
		//}
		return replaceAsFraction(parts,mark,argu);
		}
		else {return parts;}
	}
*/
	if (ac.is_in(parts[mark]))
	{
		return parts;
	}
	String argu="";
	for (int i=mark; i<parts.length;i++)
	{
		argu=argu+parts[i];
		if (i != (parts.length-1))
		{
			argu=argu+" ";
		}
	}
	
	return replaceAsFraction(parts,mark,argu);
    }
    private boolean no_paren(String s)

	{
	if (s.indexOf("(") > -1 && s.indexOf(")") > -1 ) {return false;}
	return true;
	}

    private String[] replaceAsFraction(String[] parts,int mark, String argu)
    {
	    //OUT.println("argu=="+argu);
	    String st="create Fraction __TEMP_FRACT__ using "+argu+";";
	    StatementProcessor statie=new StatementProcessor(st,vc,ac,ttc,OUT,IN);
	    statie.suppress();
	    statie.setAttempting(attempting);
	    statie.run();
	    if (vc.is_in("__TEMP_FRACT__"))
	    {
		    vc.get("__TEMP_FRACT__").setAsTransient();
	    }
	    String[] newparts=new String[mark+1];
	    System.arraycopy(parts,0,newparts,0,mark);
	    newparts[mark]="__TEMP_FRACT__";
	    return newparts;
    }
    
    private boolean only_one_div(String check)
    {
	    int divcount=0;
	    for (int i=0; i<check.length();i++)
	    {
		    char c = check.charAt(i);
		    if ( (c=='*') || (c=='-') || (c=='+'))
		    {
			    return false;
		    }
		    if (c=='/')
		    {
			    divcount++;
		    }
	    }
	    if (divcount != 1 ) {return false;}
	    return true;
    }
    
	    
	    
    
    private void process_equations(String[] eqs, Statement s)
    {
        for (int bb = 0; bb < eqs.length; bb++)
        {
            String eqname = "eq_" + EQUACOUNT;
            EQUACOUNT++;
            s.replace("eq_temp_" + bb, eqname);
            Var eqvar = new Var(new Equation(eqname, eqs[bb], vc), eqname);
            eqvar.getEquation().setLiterals(s.getRefs(), s.getCharRefs());
            eqvar.setAsTransient();
            vc.add(eqvar);
        }
    }

    private String[] clean_parts(String[] pts)
    {
        Vector cleanparts = new Vector();
        for (int clean = 0; clean < pts.length; clean++)
        {
		
		//4-8-05
	    if (pts[clean].startsWith(";"))
	    {
		    pts[clean]=pts[clean].substring(1,pts[clean].length());
	    }
	    if (pts[clean].endsWith(";"))
	    {
		    pts[clean]=pts[clean].substring(0,pts[clean].length()-1);
	    }
            if (!pts[clean].equals("") && !pts[clean].equals(";"))
            {
                cleanparts.addElement(pts[clean]);
            }
        }
        Object[] cpo = cleanparts.toArray();
        pts = new String[cpo.length];
        System.arraycopy(cpo, 0, pts, 0, cpo.length);
        cleanparts.removeAllElements();
        return pts;
    }

    private Vector split_into_statements()
    {
        Vector tostates = new Vector();
        StringBuffer tis = new StringBuffer(1000);
        boolean in_comment = false;
        boolean in_plus = false;
	  boolean in_lit=false;
        for (int i = 0; i < source.length(); i++)
        {
            char c = source.charAt(i);

            /*function to ignore comments*/
            if (c == '/')
            {
                if ((i + 1) < source.length())
                {
                    char d = source.charAt(i + 1);

                    if (d == '*')
                    {
                        in_comment = true;
                        i++;
                        continue;
                    }
                }
            }

            if (c == '*')
            {
                if ((i + 1) < source.length())
                {
                    char d = source.charAt(i + 1);

                    if (d == '/')
                    {
                        in_comment = false;
                        i++;
                        continue;
                    }
                }
            }

            if ((c == '\n') || (c == '\t') || (c == '\r') || (c == '\0'))
            {
                continue;
            }
		if ((c==';')&&!in_comment)
			{
			try {if (source.charAt(i-1)=='\\') {tis.append(c);continue;}
				}
			catch(Exception e){}
			}
            if ((c == ';') /*&& !in_plus*/ && !in_comment)
            {
                tostates.addElement(tis.toString());
                tis.setLength(0);
                continue;
            }

            if (c == '\"' )
            {
                in_plus = !in_plus;
            }

            if (!in_comment)
            {
                tis.append(c);
            }
        }

        if (tis.length() != 0)
        {
            tostates.addElement(tis.toString());
        }

        return tostates;
    }

    private void do_thread(String[] pts, Statement s)
    {
        if (DEBUG)
        {
            OUT.println("Processing thread statement", 2);
        }

        int in_ix = -1;
        for (int trd = 0; trd < pts.length; trd++)
        {
            if (pts[trd].indexOf("inline") > -1)
            {
                in_ix = trd;
                break; /*we have our inline*/
            }
        }

        if (in_ix < 0)
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THD,
                                  "This thread statement is missing a body"), vc, attempting);
        }
        else
        {
            String threadname = "";
            String tp = "";
            long TCOUNT = THREADCOUNT;
            THREADCOUNT++;
            try
            {
                if ((pts.length == 4) && pts[1].equals("as"))
                {
                    threadname = pts[2];
                }
                else
                {
                    threadname = "Anonymous_" + TCOUNT;
                }

                for (int x = 0; x < pts[in_ix].length(); x++)
                {
                    char c = pts[in_ix].charAt(x);
                    if (Character.isDigit(c))
                    {
                        tp = tp + c;
                    }

                }

                int indx = Integer.parseInt(tp);
                /*call static spawn method*/
                spawn(inlines[indx], vc, ac, ttc, OUT, IN, attempting, threadname, TCOUNT, this);

                if (DEBUG)
                {
                    OUT.println("Spawner vc = " + vc.getReference(), 2);
                }

            }
            catch (Exception e)
            {
                encountered_error = true;
                OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THD, "This thread statement is incorrect", s), vc, attempting);
            }
        }
    }

    private void do_declare(String[] pts, Statement s)
    {
        Var[] ins = vc.in();
        boolean groupelement = false;
        if (pts[2].indexOf(":") > -1)
        {
            pts[2] = pts[2].substring(0, pts[2].indexOf(":"));
            groupelement = true;
        }
        boolean already_dec = false;
        for (int u = 0; u < ins.length; u++)
        {
            if (pts[2].equals(ins[u].getName()))
            {
                already_dec = true;
                break;
            }
        }
        if (!already_dec && !groupelement)
        {
	    Var deccy=Var.declare(pts[1],pts[2],OUT,vc);
	    if (deccy.getType() == -1)
	    {
		    OUT.println(
		    new DSOut(DSOut.ERR_OUT,DSOut.ASS, 
		    	"Failed to declare a Var of type:"+pts[1]+"\nIs this a valid type?\nHave you executed the appropriate 'use' statement?",s)
				,vc,attempting);
		    return;
	    }
	    
            vc.add(deccy);
            if (USER_DEBUG)
            {
                OUT.println(new DSOut(
                        DSOut.STD_OUT, DSOut.ASS,
                        "We successfully declared a variable",
                        s, true));
            }
        }
        else
        {
            String mess = "Cannot 'declare' when the variable exists";
            if (groupelement)
            {
                mess += "\nCannot use 'declare' for individual vars in a group";
            }
            DSOut dso = new DSOut(DSOut.ERR_OUT, DSOut.ASS, mess, s);
            OUT.println(dso, vc, attempting);
            encountered_error = true;
        }
    }

    private void do_global(String[] pts, Statement s)
    {
        try
        {
            int ix = Integer.parseInt(pts[1].substring(12, pts[1].length() - 1));
            String exec = inlines[ix];
            doGlobal(exec);
        }
        catch (Exception e)
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, -1, "Corrupted global exec block", s));
        }
    }

    private String[] do_ask_for(String[] pts, Statement s)
    {
        if (pts.length == 2)
        {
            String[] rep = new String[3];
            rep[0] = pts[1];
            rep[1] = "is_now";
            rep[2] = "ask";
            s.resetParts(rep);
            pts = rep;
        }
        else if (pts.length == 3)
        {
            String[] rep = new String[4];
            rep[0] = pts[1];
            rep[1] = pts[2];
            rep[2] = "is";
            rep[3] = "ask";
            s.resetParts(rep);
            pts = rep;
        }

        return pts;
    }

    private void do_remove(String[] pts, Statement s)
    {
        if (pts.length < 2)
        {
            encountered_error = true;
            OUT.println(new DSOut(DSOut.ERR_OUT, -1, "Destroy what?  No variable specified", s), vc, attempting);
        }
        else
        {
            boolean remd = false;

            if (vc.is_in(pts[1]))
            {
                vc.remove(pts[1]);
                remd = true;
            }

            if (remd && USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, -1, "We successfully removed '" + pts[1] + "'", true));
            }

            else if (!remd && USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, -1, "We tried to remove a variable named '" + pts[1] +
                                                         "'\n\tIt does not exist!!"));
            }
        }
    }

    private void do_save(Statement s)
    {
        String mg = "Saving to a file ";
        boolean b = FileOp.writeFile(s, vc);

        if (b)
        {
            mg = mg + "succeeded.";
        }
        else
        {
            encountered_error = true;
            mg = mg + "failed!";
        }

        if (USER_DEBUG)
        {
            OUT.println(new DSOut(DSOut.STD_OUT, -1, mg, true));
        }
    }

    private void do_load(Statement s)
    {
        String mg = "Loading from a file ";
        boolean b = FileOp.readFile(s, vc);

        if (b)
        {
            mg = mg + "succeeded.";
        }
        else
        {
            encountered_error = true;
            mg = mg + "failed!";
        }

        if (USER_DEBUG)
        {
            OUT.println(new DSOut(DSOut.STD_OUT, -1, mg, true));
        }
    }

    private void do_use(String[] pts, Statement s)
    {
        if (DEBUG)
        {
            OUT.println("processing use statement", 2);
        }
	String[] arggy = new String[0];
	
	if ( (pts.length > 2) && pts[2].equalsIgnoreCase("with"))
	{
		arggy = new String[pts.length-3];
		System.arraycopy(pts,3,arggy,0,arggy.length);
		pts = new String[]{pts[0],pts[1]};
	}
		

        if (pts.length != 2)
        {
            encountered_error = true;
            OUT.println(new DSOut(DSOut.ERR_OUT, -1, "Improper length for a use statement", s), vc, attempting);
        }
        else if (arggy.length == 0)
        {
            boolean success = Use.exec(pts[1], vc, ac, ttc, OUT, IN, attempting);
            if (!success)
            {
                encountered_error = true;
            }
            if ((!success) && USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, -1, "There was an error using " + pts[1], true));
            }
            else if (USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, -1, "We are now including " + pts[1], true));
            }
        }
	else {
		boolean success = Use.exec(pts[1],vc,ac,ttc,OUT,IN,attempting,s,arggy);
		if (!success) {encountered_error = true;}
		if (!success && USER_DEBUG)
		{
			OUT.println(new DSOut(DSOut.STD_OUT,-1,"There was an error using "+pts[1],true));
		}
		else if (USER_DEBUG)
		{
			OUT.println(new DSOut(DSOut.STD_OUT,-1,"We are now including "+pts[1],true));
		}
	}
	
    }

    private void do_connect(String[] pts, Statement s)
    {
        if (pts.length < 4)
        {
            encountered_error = true;
            DSOut dso = new DSOut(DSOut.ERR_OUT, DSOut.JAV, "This JavaConnector 'connect' statement is too short", s);
            OUT.println(dso, vc, attempting);
            return;
        }

        if (vc.is_in(pts[3]))
        {
            Var jcopy = vc.get(pts[3]);

            if (jcopy.getType() == Var.JAVA)
            {
                Var nw = Var.copy(jcopy, pts[1]);

                if (vc.is_in(pts[1]))
                {
                    vc.replace(pts[1], nw);
                }
                else
                {
                    vc.add(nw);
                }

                if (USER_DEBUG)
                {
                    OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "'" + pts[1] + "' now references '" + pts[3] + "'", true));
                }

                return;
            }
        }

        pts[3] = StringFunct.pullStringLiteral(pts[3], s, vc);

        boolean b = JavaConnector.connect(pts[1], pts[3], vc, ac, ttc, OUT, IN);
        if (b)
        {
            if(USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "We successfully connected a javaconnector: '" + pts[1] + "'", true));
            }
        }
        else
        {
            encountered_error = true;

            if(USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "We failed at connecting the javaconnector '" + pts[1] + "'", true));
            }
        }
    }

    private void do_hook(String[] pts, Statement s)
    {
	   boolean b=false;
        if (pts.length < 4)
        {
            encountered_error = true;
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.JAV, "This is too short a 'hook' statement", s), vc, attempting);
        }
	else if (pts[1].equals("ALL_ACTIONS"))
	{
	b=JavaConnector.hookAll(ac,pts[3],vc);
	}
		
        else
        {
            pts[1] = StringFunct.pullStringLiteral(pts[1], s, vc);
            
	    b = JavaConnector.hook(pts[3], pts[1], vc);
 
        }
        
	if (!b)
            {
                encountered_error = true;
            }
            if (b && USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "We successfully hooked action to javaconnector", true));
            }

            if (!b && USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "We failed at hooking this action to a javaconnector.", true));
            }
	}

    private void do_dump(String[] pts, String runningSourceSet)
    {
        boolean force_error = false;
        if (pts.length > 1)
        {
            if (pts[1].equalsIgnoreCase("standard"))
            {
                pts[1] = "0";
            }
            else if (pts[1].equalsIgnoreCase("error"))
            {
                pts[1] = "1";
            }
            else if (pts[1].equalsIgnoreCase("debug"))
            {
                pts[1] = "2";
            }
            else if (pts[1].equalsIgnoreCase("java_err_out"))
            {
                pts[1] = "3";
                force_error = true;
            }
        }
        int dump_to = -1;
        try
        {
            dump_to = Integer.parseInt(pts[1]);
        }
        catch (Exception wq)
        {
            dump_to = -1;
        }
        if ((dump_to < 0) || (dump_to > 2))
        {
            dump_to = 2;
        }
        PrintStream temp_error = OUT.getDebugStream();
        if (force_error)
        {
            try
            {
                OUT.setDebugStream(System.err);
            }
            catch (Exception osdb)
            {
            }
        }
        ac.dump(dump_to);
        vc.dump(dump_to);
        OUT.println("Currently running the following source-set:\n==============================\n" + runningSourceSet + "\n===============================\n", 2);
        if (force_error && (temp_error != null))
        {
            try
            {
                OUT.setDebugStream(temp_error);
            }
            catch (Exception sdb)
            {
            }
        }
    }

    private void do_kill(String[] pts, Statement s)
    {
        String ptname = "not-specified";
        try
        {
            ptname = pts[1];
            Var voov = vc.get(pts[1]);
            /*
             voov.setAsTransient();
             voov.setUsedBit(true);
             EXPLICIT 'destroy' should probably be required
             */

            voov.getThread().kill();
            if (USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.THD,
                                      "Successfully killed the thread '" + pts[1] + "'!", true));
            }
        }
        catch (Exception eek)
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THD,
                                  "Some failure finding &| killing the thread '" + ptname + "'", s), vc, attempting);
        }
    }

    private void do_action(Statement s)
    {
       boolean b= Action.make(s, vc, ac, ttc, OUT, IN,AS_INTERFACE);
       if (!b) {encountered_error=true;}
    }

    private void do_initialize(String[] pts, Statement s)
    {
        if (pts.length < 2)
        {
            DSOut dso = new DSOut(DSOut.ERR_OUT, DSOut.JAV, "This is too short an 'initialize' statement", s);
            OUT.println(dso, vc, attempting);
            encountered_error = true;
        }
        else
        {
            boolean b = JavaConnector.initialize(pts[1], vc, ac, ttc, OUT, IN);
            if (!b)
            {
                encountered_error = true;
            }
            if (b && USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "Successfully initialized a javaconnector '" + pts[1] + "'", true));
            }

            if (!b && USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.JAV, "'" + pts[1] + "' javaconnector could not be initialized!", true));
            }
        }
    }

    private void do_profile(String[] pts)
    {
        String proffy = "";
        if (vc.is_in(pts[1]))
        {
            proffy = "{{{A Variable's Profile}}}\n" + Var.profile(pts, vc, ttc) + "{{{End of variable profile}}}\n";
        }
        else if (ac.is_in(pts[1]))
        {
            proffy = "{{{An Action Profile}}}\n" + Action.profile(pts, ac) + "{{{End of Action Profile}}}\n";
        }

	  else if (ttc.is_in(pts[1]))
		{
		proffy = ttc.get(pts[1]).getProfile();
		}

        int outspot = 0;
        if ((pts.length == 4) && pts[2].equals("to"))
        {
            if (pts[3].equals("standard"))
            {
                pts[3] = "0";
            }
            else if (pts[3].equals("error"))
            {
                pts[3] = "1";
            }
            else if (pts[3].equals("debug"))
            {
                pts[3] = "2";
            }

            try
            {
                outspot = Integer.parseInt(pts[3]);
            }
            catch (Exception e)
            {
                outspot = 0;
            }
        }

        OUT.println(proffy, outspot);
    }

    private void do_try(Statement s)
    {
        boolean b = Try.tryIt(s, vc, ac, ttc, OUT, IN);
        String message = "->Executing an 'attempt' block ->";
        if (b)
        {
            message += "succeeded";
        }
        else
        {
            message += "failed!";
        }
        if (USER_DEBUG)
        {
            OUT.println(message, 1);
        }
    }

    private void do_resurrect(String[] pts, Statement s)
    {
        String ptname = pts[1];

        try
        {
            Var tdvar = vc.get(pts[1]);
            if (tdvar.getType() != Var.THREAD)
            {
                throw new Exception();
            }
            StatementProcessor copy = tdvar.getThread().getStatementProcessor().copySelf();
            DThread dt = new DThread(copy, copy, tdvar.getThread().getThreadCount());
            tdvar.setValue(dt);
            new Thread((SPThread) copy).start();

            if (USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.THD, "We successfully resurrected '" + ptname + "'", true));
            }
        }
        catch (Exception e)
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THD, "'" + ptname + "' could not be found &| resurrected", s), vc, attempting);
        }
    }

    private void doGlobal(String glob)
	{
	try{
	   StatementProcessor nw=new StatementProcessor(glob,vc,ac,ttc,OUT,IN);
	   nw.suppress();
	   nw.setAttempting(attempting);
	   nw.THREADING=THREADING;
	   nw.setNextUp(this);
	   vc.setGlobalVarState(true);
	   ac.setGlobalActionState(true);
	   nw.run();
	   /*create global-block stuff*/
	   }
	   catch(Exception e) {/*we want to make sure it turns
					global off in the case of an error,
					hence the try block*/
					}
		vc.setGlobalVarState(false);
		ac.setGlobalActionState(false);
	}
	//NEW STUFF FOR INTERFACES

    private void do_interface(Statement s, String[] parts)
    {
	    if (parts.length < 3) {OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.THG,"This interface definition is too short",s),vc,attempting);
		    		return;
	    }
	   
	    boolean b = ThingInterface.createInterface(s,ttc,OUT,IN);
	    if (b && USER_DEBUG) {OUT.println(new DSOut(DSOut.STD_OUT,DSOut.THG,"We successfully created the interface '"+parts[1]+"'",true));
	    }
	    if (!b) {OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.THG,"We failed at creating this interface '"+parts[1]+"'",s),vc,attempting);
	    }
   }
   
    private void do_wake(String[] pts, Statement s)
    {
        String ptname = "not-specified";
        try
        {
            ptname = pts[1];
            vc.get(pts[1]).getThread().getStatementProcessor().wake();
            if (USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.THD, "'" + pts[1] + "' is now awake", true));
            }
        }
        catch (Exception eeky)
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THD, "'" + ptname + "' could not be found &| woken up", s),
                        vc, attempting);
        }
    }

    private void do_hibernate(String[] pts, Statement s)
    {
        String ptname = "not-specified";
        try
        {
            ptname = pts[1];
            vc.get(pts[1]).getThread().getStatementProcessor().hibernate();
            if (USER_DEBUG)
            {
                OUT.println(new DSOut(DSOut.STD_OUT, DSOut.THD, "'" + pts[1] + "' is now hibernating", true));
            }
        }
        catch (Exception eek)
        {
            OUT.println(new DSOut(DSOut.ERR_OUT, DSOut.THD, "'" + ptname + "' could not be found &| hibernated",
                                  s), vc, attempting);
        }
    }
    public String[] fixVC(String[] parts)
    {
	    //4-9-05
	   /*
	   for (int i=0; i<parts.length;i++)
	    {
		    parts[i]=vc.fixSearchString(parts[i]);
	    }
	    */
    return parts;
    }
    
    
    public StatementProcessor copySelf()
    {

        StatementProcessor me = new StatementProcessor(original_source, vc, ac, ttc, OUT, IN);
        if (suppress)
        {
            me.suppress();
        }
        me.setAttempting(attempting);
        if (nextUp != null)
        {
            me.setNextUp(nextUp);
        }
        if (ignore_actions)
        {
            me.ignoreActions();
        }
        me.THREADING = THREADING;
        return me;
    }

    public StatementProcessor suppress()
    {
        suppress = true;
	return this;
    }

    public void pullInlines()
    {

        boolean in_inline = false;
        Vector v = new Vector();
        StringBuffer holder = new StringBuffer(1000);
        StringBuffer ss = new StringBuffer(1000);
        int space = 0;
        int brack_count = 0;

        for (int i = 0; i < source.length(); i++)
        {
            char c = source.charAt(i);

            if (c == '{')
            {
                if (in_inline == false)
                {
                    in_inline = true;
                    continue;
                }

                brack_count++;

            }

            if (c == '}')
            {
                if (in_inline && (brack_count == 0))
                {
                    in_inline = false;
                    v.addElement(ss.toString().trim());
                    ss.setLength(0);
                    holder.append(" inline_code%");
                    holder.append(space);
                    holder.append('%');
                    space++;
                    continue;
                }

                brack_count--;
            }

            if (in_inline == true)
            {
                ss.append(c);
                continue;
            }

            holder.append(c);

        }

        inlines = new String[v.size()];
	
        for (int j = 0; j < inlines.length; j++)
        {
            inlines[j] = (String) v.elementAt(j);
	    //inline_hash.put("inline_code%"+j+"%",inlines[j]);
	    
	}

        source = holder.toString();
    }

    /*NEW ADD AS OF 4-3-05
    Object wrapper (java.lang.Object)
    */
    
    public void externalVarInsert(Object o, String name, boolean mutable)
    {
	    JavaConnector addjc=new JavaConnector(new AdvancedDustyable(o),vc,ac,ttc,OUT,IN);
	    Var addme=new Var(addjc,name);
	    //addme.setAsGlobal();
	    addme.setMutability(mutable);
	    vc.add(addme);
    }
    
    public void externalVarInsert(Object o, String name)
    {
	    externalVarInsert(o,name,true);
    }
    
    //following two methods for use w/ hashmap of inlines.. option
    //for later...
    /*
    public boolean isInline(String key)
    {
	    return inline_hash.containsKey(key);
    }
    */
    
    /*
    public String getInline(String key)
    {
	    return (String)inline_hash.get(key);
    }
    */
    
    public static void spawn(String s, VarContainer v, ActionContainer a, ThingTypeContainer t, Output o, InputStream i, boolean AT, String tname, long tcount, StatementProcessor nu)
    {

        if (DEBUG)
        {
            v.getOutput().println("->running separate (spawner == " + v.getReference() + ")", 2);
        }
	
        VarContainer vv = new VarContainer(o);
        vv.add(new Var(new VCWrapper(v)));
        StatementProcessor state = new StatementProcessor(s, vv, a, t, o, i);
        /*unsure if this is wise, removed for now.
         This is during 'threading'.  If this statement fails, its errors should
         be wrapped in 'attempt' blocks in its own stuff
        state.setAttempting(AT);
         */
        state.suppress();
        state.setNextUp(nu);
        state.THREADING = true;

        DThread thready = new DThread(state, state, tcount);
        if (v.is_in(tname))
        {
            v.get(tname).getThread().kill();
            v.get(tname).setValue(thready);
            state.setThreadVar(v.get(tname));
        }
        else
        {
            Var davar = new Var(thready, tname);
            state.setThreadVar(davar);
            v.add(davar);
        }
        ((SPThread) state).start();

    }
    
    public void processArgArray(String[] sargs)
    {
	    	/*currently->put args in inner VC*/
		VarContainer innermost = new VarContainer(OUT);
		innermost.add(VarGroup.createArgArray(vc,sargs));
		vc.add(new Var(new VCWrapper(innermost)));
    }
    
    public void processArgArray(String[] sargs, Statement statie)
    {
	    VarContainer innermost =new VarContainer(OUT);
	    innermost.add(VarGroup.createArgArray(vc,sargs,statie));
	    vc.add(new Var(new VCWrapper(innermost)));
    }
    
	    

    public static void main(String[] args)
    {

        if (args.length < 1)
        {
            System.err.println("Requires a filename");
            System.exit(0);
        }

        String src = "";
        String file = new String(System.getProperty("user.dir") + System.getProperty("file.separator") + "users" +
                                 System.getProperty("file.separator") + args[0]);

        try
        {

            BufferedReader br = new BufferedReader(new FileReader(new File(file)));

            String s;

            while ((s = br.readLine()) != null)
            {
                src = src + s + "\n";
            }

            br.close();

        }
        catch (Exception e)
        {
            System.err.println("Fileread error");
            System.exit(0);
        }

        Output OUT = null;
        InputStream IN = null;

        try
        {
            OUT = new Output();
            IN = System.in;
        }
        catch (Exception e)
        {
            System.err.println("Failed at creating IO streams! cannot continue");
            System.exit(1);
        }

        OUT.println("Source==\n" + src);

        VarContainer vc = new VarContainer(OUT);
        ActionContainer ac = new ActionContainer(OUT);
        ThingTypeContainer ttc = new ThingTypeContainer(OUT, IN);
        StatementProcessor sp = null;

        try
        {
            sp = new StatementProcessor(src, vc, ac, ttc, OUT, IN);
        }
        catch (Exception e)
        {
            sp = new StatementProcessor(src, vc, ac, ttc);
        }

	boolean arggiebool = false; Vector arguments = new Vector();
        // Iterate through remaining arguments.
        for(int i = 1; i < args.length; ++i)
        {
            String currArg = args[i];
	    
	    if (arggiebool)
	    {
		    arguments.addElement(currArg);
	    }
	    
	    else if (currArg.equalsIgnoreCase("-args")||currArg.equalsIgnoreCase("-with"))
	    {arggiebool=true;}
	    
            else if (currArg.equalsIgnoreCase("-quiet"))
            {
                sp.setUserDebug(false);
            }
            else if (currArg.equalsIgnoreCase("-benchmark"))
            {
                StatementProcessor.BENCHMARK = true;
            }
	    else if (currArg.equalsIgnoreCase("-debug"))
	    {
		    StatementProcessor.DEBUG=true;
	    }
	    else if (currArg.equals("-stop_at_statements")) 
	    {
		    BREAK_AT_STATEMENTS=true;
	    }
        }
	/*add even NO args..*/
		Object[] o =arguments.toArray();
		String[] sargs = new String[o.length];
		System.arraycopy(o,0,sargs,0,o.length);
		sp.processArgArray(sargs);

	
        new Thread(sp).start();
    }
    
    
    
    //FOLLOWING ARE THE DUSTIALIZABLE interface methods...
public java.awt.Component getAsComponent()
{
	return Dustyable.p;
}

public boolean isComponent(){
	return false;
}

public boolean isOnlyContainer()
{
	return false;
}

/*receive a Dustyable from Dustyscript in your Dustyable subclass, without any args*/


public boolean processDustyable(Dustyable d)
{
	return false;
}

/*receive a Dustyable from Dustyscript in your Dustyable subclass, with args*/
public boolean processDustyable(Dustyable d, String[] args)
{
//might need to gut this out to get streams/etc
	return false;
}

/*receive a single string message from Dustyscript, no args*/
public boolean processCommand(String command)
{
//put guts of implementation for no-arg command here	
return false; //for now
}

/*receive a single string message/command from Dustyscript, with args*/
public boolean processCommand(String command, String[] args)
{
	//put guts of implementation for using a SP from within Dustyscript
return false; //for now
}

	
	
/*receive a Var (dscript.Var) from Dustyscript, no args*/
public boolean processVar(Var v)
{
	return false;
}

/*receive a Var (dscript.Var) from Dustyscript, with args*/
public boolean processVar(Var v, String[] args)
{
	return false;
}

/*send (return to) Dustyscript a dscript.Var, require no args*/
public Var getVar()
{
	return Dustyable.NOT_THERE;
}


/*send (return to) Dustyscript a dscript.Var, with args passed in*/
public Var getVar(String[] args)
{
	return Dustyable.NOT_THERE;
}

/*register the Dustyable that wraps this Dustializable*/

public void setDustyable(Dustyable d)
{
	dustyable=d;
}

public static String stripComma(String s)
{
	if (!s.endsWith(",")) {return s;}
	return s.substring(0,s.length()-1);
}


    /*end*/

}
