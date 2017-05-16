package dscript;
import java.io.InputStream;
import java.util.Vector;

class ActionSplitter
{


    static boolean DEBUG=StatementProcessor.DEBUG;
    static int AC = DSOut.ACT;
    static int ER = DSOut.ERR_OUT;

    public static void split(Statement s, ActionContainer ac, VarContainer vc,
                             ThingTypeContainer ttc, Output OUT, InputStream IN)
    {

	boolean AT = s.getAttempting();
        String[] parts = s.getParts();
	parts = If.adjustForMinus(parts);
	s.resetParts(parts);
        int inner_states=0;
        Vector nws = new Vector();
	
        for (int i=0; i<parts.length;i++)
        {

            int ix=parts[i].indexOf(".");
		boolean aci = ac.is_in(parts[i]); //check by name
            if (!aci && ix<0)
            {
                nws.addElement(parts[i]);continue;
            }
            else
            {

                if (ix > -1)
                {
                    String ck=parts[i].substring(0,ix);

                    if (!vc.is_in(ck,true))
                    {
                        nws.addElement(parts[i]);continue;
                    }

                }
		if (ttc.is_in(parts[i])) {nws.addElement(parts[i]);}
                Vector v=new Vector();

                for (int m=i; m<parts.length;m++)
                {

                    if (parts[m].equals("+")|(m==(parts.length-1)))
                    {
                        if (m==(parts.length-1))
                        {
                            v.addElement(parts[m]);
                        }

                        process(ac,vc,ttc,v,s,inner_states,OUT,IN);
                        nws.addElement("%"+inner_states);

                        if(parts[m].equals("+"))
                        {
                            nws.addElement("+");
                        }

                        inner_states++;i=m;break;
                    }

                    v.addElement(parts[m]);

                }

            }

        }

        /*now we have vars %x put in the container, and a new Statement to create*/
        String[] newstate=new String[nws.size()];

        for (int k=0; k<newstate.length; k++)
        {

            newstate[k]=(String)nws.elementAt(k);

            if(StatementProcessor.DEBUG)
            {
                OUT.println("newstate["+k+"]=="+newstate[k],2);
            }

        }

        s.resetParts(newstate);
    }


    public static void process(ActionContainer ac, VarContainer vc, ThingTypeContainer ttc,
                               Vector v, Statement master, int varindex, Output OUT, InputStream IN)
    {
	boolean AT = master.getAttempting();
	Var reffy = null; //reference var for inner VC
        String[] pts=new String[v.size()+2];
        pts[0]="%"+varindex;
        pts[1]="is_now";


        for (int i=2; i<pts.length;i++)
        {
            pts[i] = (String)v.elementAt((i-2));
        }

	pts = fixForUsing(pts);
	
        if (StatementProcessor.DEBUG)
        {

            for (int jj=0;jj<pts.length;jj++)
            {
                OUT.println("pts["+jj+"]=="+pts[jj],2);
            }

        }

        /*guarantees we reference the pointer to the outer VC*/
        VarContainer ref=vc;


	
	boolean minus = false;
	if (pts[2].startsWith("-"))
	{
		pts[2] = pts[2].substring(1,pts[2].length());
		minus=true; //added
	}
	if (ActionProcessor.checkSystem(pts[2]))
	{
		pts[2]="System."+pts[2];
	}		
			
        if (pts[2].indexOf(".")>-1)
        {	

            vc=new VarContainer(OUT);
            /* the VCWrap for ref was originally here - we want the thing's inner to be seen 1st*/

            try
            {

                Thing t=ref.get(pts[2].substring(0,pts[2].indexOf(".")),true).getThing();
                vc.add(new Var(new VCWrapper(t.getVarContainer())));
                ac = t.getActionContainer();
                pts[2]=pts[2].substring(pts[2].indexOf(".")+1,pts[2].length());
		    if (minus) {pts[2] = "-"+pts[2];}


            }
            catch(Exception e)
            {
                OUT.println(new DSOut(ER,AC,"This statement most likely contained many actions"+
                                      "\n\t..Dustyscript is pretty sure it failed",master),vc,AT);
            }

            reffy = new Var(new VCWrapper(ref));
	    vc.add(reffy);

        }

        int[] usingtypes=new int[]{-1};
	String[] usingalts = null;
        try
        {

            usingtypes =new int[pts.length-4];
	    usingalts = new String[pts.length-4];
            for (int u =0;u<usingtypes.length;u++)
            {

                try
                {
		String ck = pts[(u+4)];
                    usingtypes[u] =ActionProcessor.getPartType(ck,master,ref);
		    if (usingtypes[u] == Var.GROUP)
		    {
			    try{usingalts[u] = Var.typePlurals(vc.get(ck).getVarGroup().getType());}
			    catch(Exception ez) {usingalts[u]="";}
		    }
		    else if (usingtypes[u] == Var.THING)
		    {
			    try{usingalts[u] = vc.get(ck).getThing().getThingType();}
			    catch(Exception ez) {usingalts[u]="";}
		    }
		    else {usingalts[u]="";}
                }
                catch(Exception e)
                {
                    usingtypes[u]=-1;
		    usingalts[u]="";
                }

            }

        }
        catch(Exception e)
        {
            usingtypes=new int[]{-1};
	    usingalts=new String[]{""};
        }

	  Action a=null;
	  ActionHashIterator ahi=null;
	  for (int z=0; z<ActionProcessor.NORETURN.length; z++)
	  {
	  ahi = ActionHashIterator.getActionHashIterator(pts[2],usingtypes,usingalts,ActionProcessor.NORETURN[z],ttc);
	  	if (ac.is_in(ahi)) 
			{
				a = ac.get(ahi);
				break;
			}
	  
	  }
	  if (a ==null)  {throw new RuntimeException("Compound action not found!");}
	  
	  //commented out, old way
	  /*
	  if (!ac.is_in(pts[2],usingtypes))
		{
		if (ac.inByWild(pts[2],usingtypes,16))
			{a= ac.getByWild(pts[2],usingtypes,16);}
		else if (ac.inByWild(pts[2],usingtypes))
			{a = ac.getByWild(pts[2],usingtypes);}

		}
            else{ a= ac.get(pts[2],usingtypes);}
	  */
        Var vv=new Var(pts[0],a.getReturnType());
        ref.add(vv);
        Statement s =new Statement();
        s.setInline(master.getInline());
        s.setRefs(master.getRefs());
        s.setEquations(master.getEquations());
        s.resetParts(pts);
	  s.setAttempting(AT);
        
	ActionProcessor.processFromSplitter(a,vv.getName(),ac,s,vc,ttc,reffy,OUT,IN);
	//ActionProcessor.process(ac,s,vc,ttc,OUT,IN);
        vv.setAsTransient();

    }
private static String[] fixForUsing(String[] s)
{
	int i = ActionProcessor.getUsingStart(s);
	if (i<0)
	{
		String[] cpy = new String[s.length+2];
		System.arraycopy(s,0,cpy,0,s.length);
		cpy[s.length]= "using";
		cpy[s.length+1] = "~";
		s = cpy;
	}
	return s;
}

    
}



