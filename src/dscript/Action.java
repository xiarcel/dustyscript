package dscript;


import java.util.Vector;
import java.io.InputStream;
import dscript.connect.JavaConnector;

public class Action
{
	private final static int[] CHECK=new int[]{-1,0,1,2,3,4,7,10,11,15,16};
	
   private boolean is_global=false;
   static int CTR = 0;
   private int[] argtypes;
   private String[] arg_alts;
   private int returntype;
   private String hash="";
   private String ret_string;
   private String name;
   private String[] state_string_a;
   private String[] internal_var_names;
   private String wild="";
   private String statement = "";
   
   private boolean valid=true;
   private boolean synch_bit=false;
   static boolean DEBUG=ActionProcessor.DEBUG;

   private Output OUT; private InputStream IN;

   private ActionContainer ac;


   /* usage:
      action <action name> takes <vartype> <varname>, <vartype> <varname>, ... gives <vartype> {<STATEMENT>};
   */


   public Action(String nam, int[] at, String[] int_v_n, String[] a_a, int rt, String[] ssa,
                 String rs, ActionContainer a, Output O, InputStream I)
   {
	   arg_alts=a_a; //new
      OUT=O;
      IN=I;
      hash = ActionContainer.createHash(nam,at,arg_alts,rt);
      //wild = ActionContainer.createWildHash(nam,at,rt);
	ac=a;
      argtypes=at;
      returntype=rt;
      name=nam;
      state_string_a = ssa;
      ret_string = rs;
      internal_var_names=int_v_n;
      statement = makeStatementString(state_string_a);
      
      if(at.length != int_v_n.length)
      {
         valid=false;
      }

   }
      
 public Action(String nam, int[] at, String[] int_v_n, String[] a_a, int rt, String ssa,
                 String rs, ActionContainer a, Output O, InputStream I)
   {
	  arg_alts=a_a; //new
      OUT=O;
      IN=I;
      hash = ActionContainer.createHash(nam,at,arg_alts,rt);
      //wild = ActionContainer.createWildHash(nam,at,rt);
	ac=a;
      argtypes=at;
      returntype=rt;
      name=nam;
      statement = ssa;
      ret_string = rs;
      internal_var_names=int_v_n;
      
      if(at.length != int_v_n.length)
      {
         valid=false;
      }

   }

   public Action()
   {

      name ="";
      argtypes=new int[0];
      arg_alts=new String[0];
      returntype=-1;
      state_string_a=new String[0];
      internal_var_names=new String[0];
      valid =false;

   }

/*create copy so that different actions are actually referenced during inheritance*/
   public Action copySelf()
	{
	return new Action(name,argtypes,internal_var_names,arg_alts,returntype,statement,ret_string,
				ac,OUT,IN).setSynchd(synch_bit);
	}
	
   public Action setSynchd(boolean b)
   {
	   synch_bit=b;
	   return this;
   }
   
   public boolean getSynchd()
   {
	   return synch_bit;
   }
   
   public String getStatement()
   {return statement;}
   
   
   public boolean is_valid()
   {

      return valid;

   }

   public void setAsGlobal()
	{is_global=true;}
   public boolean isGlobal()
	{return is_global;}

   public String getName()
   {

      return name;
   }


   public int getReturnType()
   {
      
      return returntype;

   }
   
   public String getReturnString(){return ret_string;}

   public String getHash() {return hash;}
   public String getWild() {return wild;}

   public String profile()
   {
   	String message = "Action '"+name+"' profile";
   	message += "\n\tTakes: ";
	for (int i=0; i<argtypes.length;i++)
		{
		String ats = arg_alts[i].equals("") ? Var.typeString(argtypes[i]) : arg_alts[i];
		if (ats.equals("null")){ats="~";}
		message += ats + ",";
		}
	if (message.endsWith(",")){message = message.substring(0,(message.length()-1));}
   	String ts= Var.typeString(returntype);
	if (ts.equals("null")){ts="~";}
	message += "\n\tGives: "+ts;
	message += "\n\tisGlobal?:"+isGlobal();
	message +="\n\tAction-Hash-Key:|"+getHash()+"|\n";
	return message;
   } 

   public static String profile(String[] pts, ActionContainer ac)
   {  String message="";
	int count=0;
	Action[] acts = ac.in();
	for (int i=0;i<acts.length;i++)
		{
		if (acts[i].getName().equals(pts[1]))
			{count++;
			message += "(match # "+count+")\n"+acts[i].profile();
			}
		}
   	message = "There was|were "+count+" action(s) with the name '"+pts[1]+"'\n"+message;
	return message;
   }

   public int[] getArgs()
   {

      return argtypes;
   
   }
   
   public String[] getInternalVarNames()
   {
	   return internal_var_names;
   }
   


   private boolean copy(Var one, Var two, boolean neg_value, VarContainer vc)
   {

      boolean b =copy(one,two,vc);
      if(neg_value)
      {

         one.reverseValue();

      }
	return b;
   }


   private boolean copy (Var one, Var two, VarContainer vc)
   {

      int tp = two.getType();
      int ttp = two.getAbsoluteType();
	int otp = one.getAbsoluteType();
	boolean cast_ok = false;
	/*when using an anyvar, string will always accept the value.
	  also..trying to allow for int - to - decimal cast here*/
	if (one.getType()==Var.STR){cast_ok=true;tp=4;}
	else if (one.getType()==2 && two.getType()==1)
		 {two.setValue((double)two.getInteger()); cast_ok=true; tp=2;}


	//System.out.println("cast_ok:"+cast_ok+",op:"+one.getType()+",otp:"+otp+",tp:"+tp+",ttp:"+ttp);
	
	//1.0 RELEASE CANDIDATE - A alterations
	if ((otp != tp) && otp != 16 && !cast_ok) {return false;}
	if ((ttp == 16) && (tp != one.getType()) && (ttp != otp) && !cast_ok){return false;}
	switch(tp)
	{
	case 0: {one.setValue(two.getYes_No());break;}//copy
	case 1: {
	long l = two.getInteger();
	if (otp == 2) {one.setValue((double)l); break;}
	if (otp == 4) {one.setValue(""+l);break;}
	one.setValue(l);break;
		}//copy
	case 2: {
	double d = two.getDecimal();
	if (otp == 1) {one.setValue((long)d); break;}
	if (otp == 4) {one.setValue(""+d);break;}
	one.setValue(d); break;
	}//copy
	case 3: {
	if (otp == 4) {one.setValue(""+two.getChar());break;}
	one.setValue(two.getChar());break;
	} //copy
	case 4: {one.setValue(two.asString());break;} //copy
	case 7: {one.setValue(two.getVarGroup()); break;} //reference
	case 9: {one.setValue(two.getVCWrapper()); break;}
	case 10: {one.setValue(two.getThing());break;} //reference
	case 11: {one.setValue(two.getJavaConnector());break;} //reference
	case 15: {one.setValue(two.getThread());break;} //reference
	case 16: {one.setValue(new Anyvar(Var.copy(two.getAnyvar().getVar(),one.getName(),vc)));
		break;}
	default: break;
	}    
    return true;
   }
   
   public boolean exec(Var[] arggies, VarContainer global, Var to_assign,ThingTypeContainer ttc, boolean AT)
   {
	   if (synch_bit)
	   {
		   synchronized(this)
		   {
			   return exec_(arggies,global,to_assign,ttc,AT);
		   }
	   }
	
	return exec_(arggies,global,to_assign,ttc,AT);
	
   }
   
		   

   private boolean exec_(Var[] arggies, VarContainer global, Var to_assign,ThingTypeContainer ttc, boolean AT)
   {


      if(DEBUG)
      {

	      	OUT.println("to_assign="+to_assign.getName(),2);
         for(int i=0; i<arggies.length;i++)
         {
            OUT.println("arggies["+i+"]=="+arggies[i].asString(),2);
         }

         /*for (int j=0;j<state_string_a.length;j++)
         {
            OUT.println("state_string_a["+j+"]=="+state_string_a[j],2);
         }*/
	 OUT.println("Statements\n"+statement,2);
      }

      /* args[] passed must equal length of int[] argtypes as well as types */

      if(arggies.length != argtypes.length)
      {
         OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"Action takes different type(s) than what you gave it"),
				global,AT);

         return false;
      }

      VarContainer local = new VarContainer(OUT);


      for (int ii=0; ii<arggies.length;ii++)
      {
         if(arggies[ii].asString().equals(">NOVALUE<"))
         {
            OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"This action was passed a null (non) value"),
			global,AT);
            return false;
         }
      }


      if(to_assign != null)
      {
	   boolean wild = crossReferenceTypes(to_assign.getType(),returntype);
	
	   
         if((to_assign.getType() != returntype)&&(!wild)&&(returntype > -1))
         {
            OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"The type being assigned does not work with" +
                                                          " the type you are assigning"),global,AT);
            return false;
         }

      }

      for(int k=0; k<arggies.length; k++)
      {

         if(arggies[k].getType()==Var.GROUP)
         {
            local.add(new Var(arggies[k].getVarGroup(),internal_var_names[k]));
         }
         else
         {
		 if (StatementProcessor.DEBUG){
            dumparray(internal_var_names);}
            local.add(Var.copy(arggies[k],internal_var_names[k],local));
         }

      }

      /* !!!! NEW !!!!  For Proper variable scoping!!!! */

      /* changed to nest VarContainers */

      try
      {
         local.add(new Var(new VCWrapper(global)));
      }
      catch(Exception e)
      {
         //e.printStackTrace();
         return false;
      }
      StatementProcessor sp=new StatementProcessor(statement,local,ac,ttc,OUT,IN);
sp.suppress();
sp.run();
      if (sp.FAILED) {
      if (StatementProcessor.DEBUG) {OUT.println("SP failed!",2);}
      return false;}
/*
      for(int w=0; w<state_string_a.length; w++)
      {

         StatementProcessor sp = new StatementProcessor(state_string_a[w],local,ac,ttc,OUT,IN);
         sp.suppress();
         sp.run();
	   if (sp.FAILED) {return false;}
	   if (sp.GAVE){break;}
      }
*/
      return setReturn(to_assign,ret_string,local,global,AT);

   }

   private boolean crossReferenceTypes(int toassign, int returntype)
   {
	   if ((toassign == 16)||(returntype==16)||(toassign==returntype)){return true;}
	   switch (toassign)
	   {
		   case 0: {return false;}
		   case 1: 
		   	{
				if (returntype == 2) {return true;}
				break;
		   	}
		   case 2: {
			   if (returntype == 1){return true;}
			   break;
		   }
		   case 4: {return true;}
		   default: {break;}
	   }
	   
	   return false;
   }
   
	   

   private boolean setReturn(Var v, String ret, VarContainer loc,VarContainer global,boolean AT)
   {
	/*'global' and 'AT' for try-catch stuff*/

	if (StatementProcessor.DEBUG)
	{
		System.err.println("setReturn called (ret="+ret+") and (v="+v.getName()+")");
	}
      if(returntype == -1)
      {
         return true;
      }
      if (v==null) {return true;}

      /* THIS DOES NOT USE NEW Var.copy(Var) method */

      boolean neg_value = false;

      if(ret.startsWith("-"))
      {
         neg_value=true;
         ret=ret.substring(1,ret.length());
      }
	
	
	
	
	if (StatementProcessor.DEBUG){System.err.println("returntype is:"+returntype);}
	
	if (loc.is_in(ret) && loc.get(ret).getType()==Var.JAVA)
	{
		if (StatementProcessor.DEBUG)
		{
		System.err.println("loc.is_in() = true");
		}
		v.setValue(loc.get(ret).getJavaConnector());
		return true;
	}
	if (returntype == 16)
		{

		String nmit = "ac_var_"+CTR; CTR++;
		Var putit = new Var(nmit,16);
		
		//if (loc.is_in(ret))
		//{putit.setValue(new Anyvar(loc.get(ret)));}
		
		if (VarContainer.isNumberLiteral(ret))
			{
			if (VarContainer.isDecimalLiteral(ret))
				{
				putit.setValue(Double.parseDouble(ret));
				ret=nmit;
				}
				else {putit.setValue(Long.parseLong(ret));ret=nmit;}
			}
		else if (VarContainer.isStringLiteral(ret))
				{
				putit.setValue(ret.substring(1,ret.length()-1));
				ret=nmit;
				}
		else if (VarContainer.isCharLiteral(ret))
				{
				putit.setValue(ret.substring(1,ret.length()-1).charAt(0));
				ret = nmit;
				}

		loc.add(putit);
		}		
	if (StatementProcessor.DEBUG) {System.err.println("ret="+ret+", before loc.is_in(ret)");}

      if (loc.is_in(ret))
      {
	if (v.getType()==Var.GROUP)
	{
	//VarGroup vg = VarGroup.copy(loc.get(ret).getVarGroup(),v.getName(),loc);
	v.setValue(loc.get(ret).getVarGroup());
	return true;
	}
         return copy(v,loc.get(ret),neg_value,loc);
	  
      }


      if(returntype == Var.YES_NO)
      {
         if((ret.equalsIgnoreCase("YES"))||(ret.equalsIgnoreCase("TRUE")))
         {
            v.setValue(true);return true;
         }
         if((ret.equalsIgnoreCase("NO"))||(ret.equalsIgnoreCase("FALSE")))
         {
            v.setValue(false);return true;
         }

         return false;

      }

      if(returntype == Var.INT)
      {

         try
         {
            long l=Long.parseLong(ret);
            v.setValue(l);
            return true;
         }
         catch(Exception e)
         {
            OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"You are trying to assign '"+ret+"' " +
                                                          "to an integer\nand it is not an integer"),
										global,AT);

            /*WHO IS THIS FOR? DS- Programmer or Dscript-parser-developer???*/
            
            if(DEBUG)
            {
            
               for (int t=0; t<ret.length(); t++)
               {
                  OUT.print((int)ret.charAt(t)+", ",2);
               }

               OUT.println("",2);

              /* CHANGEd to loc.dump(int) - this goes to debug-stream */

              loc.dump(2);

              /* end of catch */

            }

         }

         return false;
 
      }
      
      if(returntype == Var.DEC)
      {
         try
         {
            double d = Double.parseDouble(ret);
            v.setValue(d);
     
            return true;
         }
         catch(Exception e)
         {
         }

         return false;
      }

      if(returntype == Var.STR)
      {

         if((ret.startsWith("\""))&&(ret.endsWith("\"")))
         {
            v.setValue(ret.substring(1,ret.length()-1));
            return true;

         }

         return false;

      }

      if(returntype==Var.CHR)
      {
         if(ret.startsWith("\'")&& ret.endsWith("\'"))
         {
            char c=' ';
            String vl=ret.substring(1,ret.length()-1);

            if(vl.equals(""))
            {
               v.setValue(c);return true;
            }

            c=StringFunct.makeChar(vl);

            if(c != ' ')
            {
               v.setValue(c); return true;
            }

            v.setValue(vl.charAt(0));

            return true;

         }

 

         return false;

      }
	


      if(returntype==-1)
      {
         return true;
      }
	try{
	if (returntype == Var.JAVA) 
		{
		JavaConnector jc = loc.get(ret).getJavaConnector();
		v.setValue(jc);
		return true;
		}

	if (returntype == Var.THING)
		{
		Thing t = loc.get(ret).getThing();
		v.setValue(t);
		return true;
		}
		}catch(Exception e){
			if (DEBUG)
				
			{
			e.printStackTrace();
			OUT.println("Error in setting return value in action~~!\n"+e.toString(),2);}
			return false;
			}

	if (returntype == Var.THREAD)
		{
		try{
			v.setValue(loc.get(ret).getThread());
			return true;
			}
			catch(Exception e){}
		return false;
		}
      return false;

   }

   public static String makeString(int[] a)
   {
	   StringBuffer sb=new StringBuffer();
	   for (int i=0; i<a.length;i++)
	   {
		   
		   sb.append(a[i]).append(',');
	   }
	   return sb.toString();
   }
   
   public static boolean make(Statement s, VarContainer vc, ActionContainer global_act, ThingTypeContainer ttc, Output OUT, InputStream IN)
   {
	   return make(s,vc,global_act,ttc,OUT,IN,false);
   }
   
   public static boolean make(Statement s, VarContainer vc,ActionContainer global_act, ThingTypeContainer ttc, Output OUT, InputStream IN, boolean IFACE)
   {

	boolean AT = s.getAttempting();
      String[] parts = s.getParts();
      String[] pts=new String[parts.length];
      String[] ins = s.getInline();
      Vector state_vec=new Vector();
  
      for(int j=0;j<parts.length;j++)
      {

         if(parts[j].startsWith("inline"))
         {

            String ss="";

            for(int x=0;x<parts[j].length();x++)
            {

               if(Character.isDigit(parts[j].charAt(x)))
               {
                  ss=ss+parts[j].charAt(x);
               }

            }

            try
            {
               int indx = Integer.parseInt(ss);
               state_vec.addElement(ins[indx]);
               pts[j]=ins[indx];
            }
            catch(Exception eez)
            {
            }

         }
         else
         {
            pts[j] = parts[j];
         }

      }

      Object[] o = state_vec.toArray();
      String[] statements = new String[o.length];
      System.arraycopy(o,0,statements,0,o.length);
   
      /*for(int k=0; k<statements.length;k++)
      {
         statements[k] = (String)state_vec.elementAt(k);
      }
      */


      if(DEBUG)
      {

         for (int i=0; i<parts.length;i++)
         {
            OUT.println("parts["+i+"]=="+parts[i],2);
         }

      }
      boolean synchd=false;
      if (parts[0].equalsIgnoreCase("s_action"))
      {
	      parts[0]="action";
	      synchd=true;
      }
      if(!parts[0].equalsIgnoreCase("action"))
      {

         DSOut dso = new DSOut(DSOut.ERR_OUT,DSOut.ACT,"Dustyscript thought this was an action, but it is not",s);
         OUT.println(dso,vc,AT);
         
         return false;
      }

      if(!parts[2].equalsIgnoreCase("takes"))
      {
         OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"You tried to make an action without a 'takes'",s),vc,AT);

         return false;
      }

      boolean nel=false;
      String rstring ="";
      String eval = pts[(pts.length-1)];

      if(DEBUG)
      {

         for(int u=0; u<ins.length;u++)
         {
            OUT.println("ins["+u+"]=="+ins[u],2);
         }
      
      }

      int ed =eval.lastIndexOf("give");

      if(DEBUG)
      {
         OUT.println("'give' at "+ed,2);
      }

      if(ed > -1)
      {
         ed = ed + 5;
         String refstring = eval.substring(ed,eval.length());
	 boolean inquotes=false;
         for(int y=0; y<refstring.length(); y++)
         {
            char c = refstring.charAt(y);
	    /*this fix takes care of spaces in give statement's string literals*/
	    if (c== '\"'){inquotes=!inquotes; rstring+=c; continue;}
	    
            if(!inquotes&&((c==' ')||(c=='\n')||(c=='\r')||(c==';')))
            {
               continue;
            }
            
            rstring=rstring+c;
         }

         rstring.trim();

      }
      else if (!IFACE)
      {

         if(!parts[(parts.length-2)].equals("~")&& rstring.equals(""))
         {
            OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"In the body of your action, there was no 'give'" +
                                                          " statement\n\t..yet 'gives' type was not ~",s),
										vc,AT);
            return false;
         }

      }

      if((IFACE && !parts[(parts.length-2)].equalsIgnoreCase("gives")) || (!IFACE && !parts[(parts.length-3)].equalsIgnoreCase("gives")))
      {

         OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"This part:"+(IFACE?parts[(parts.length-2)]:parts[(parts.length-3)])+" should be 'gives'",s),vc,AT);
         return false;
      }

      String r_s = parts[(parts.length-2)];
      if (IFACE) {r_s = parts[(parts.length-1)];}
      int r_t=Var.typeInt(r_s);
      if (r_t<0) 
      {
	      String alternate="";
	      if (r_s.endsWith("s")) {alternate=r_s.substring(0,r_s.length()-1);}
	      int cod = Var.typePlurals(r_s);
	      if (r_s.equals("~")) {}
	      else if (cod >=0) {r_t = Var.GROUP;}
	      else if (ttc.is_in(r_s) || ttc.allowed(r_s) || ttc.containsInterface(r_s))
	      {
		      r_t = Var.THING;
	      }
	      else if (ttc.is_in(alternate) || ttc.allowed(alternate) || ttc.containsInterface(alternate))
		      
	      {
		      r_t = Var.GROUP;
	      }
		    
	      
	     else
	     {
		     OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"What follows is not a valid 'gives' type:"+r_s+"...",s),vc,AT);
		     ttc.dump();
		     return false;
	     }
	     
      	}


      String act_n = parts[1];
      int end =parts.length-3;
      if (IFACE) {end = parts.length -2;}
      int strt = 3;
      int diff = end-strt;
      boolean no_args=false;

      if ((diff==1)&&(parts[3].equals("~")))
      {
         no_args=true; diff=2;
      }

      int chkit = diff/2;
      
      if((chkit*2) != diff)
      {
         /* uneven # of args */

         OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"The 'takes' statement was not formed correctly",s),vc,AT);
         return false;
      }

      String[] ivn = new String[chkit];
      int[] ivt = new int[chkit];
      String [] iva = new String[chkit];
      
      boolean typ=true;
      int rec=0;

      for(int m = strt; m<end; m++)
      {

         if(no_args)
         {
            ivt=new int[]{-1};
	    iva=new String[]{""};break;
	 }
         try
         {
            if(typ)
            {	 iva[rec] = "";
		 ivt[rec] = Var.typeInt(parts[m]);
		 if (ivt[rec] < 0)
               {
		       String alt_plural="";
		       if (parts[m].endsWith("s")) {alt_plural=parts[m].substring(0,parts[m].length()-1);}
		       
		  int record = Var.typePlurals(parts[m]);
		  if (record >= 0)
		  {
			  ivt[rec] = Var.GROUP;
			  iva[rec] = parts[m];
		  }
		  else if (ttc.is_in(parts[m]) || ttc.containsInterface(parts[m])||ttc.allowed(parts[m]))
		  {
			  ivt[rec]= Var.THING;
			  iva[rec]= parts[m];
		  }

		  else if (ttc.is_in(alt_plural) || ttc.containsInterface(alt_plural) || ttc.allowed(alt_plural))
		  {
			  ivt[rec]=Var.GROUP;
			  iva[rec]=parts[m];
		  }
		  else{
			  OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"Actions don't take this type:"+parts[m],s),vc,AT);
			  return false;
		  }
		  
               }

               typ = !typ; continue;

            }
            if(!typ)
            {
               if(parts[m].endsWith(","))
               {
                  ivn[rec]=parts[m].substring(0,(parts[m].length()-1));
               }
               else
               {
                  ivn[rec]=parts[m];
               }

               rec++;
               typ=!typ;

            }

         }
         catch(Exception e)
         {
            OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"(("+rec+" does not seem to be a type))",s),vc,AT);
            return false;
         }

      }

      
      //ActionHashIterator ahi;
      for (int z=0; z<CHECK.length;z++)
      {
	      
	      //ahi=ActionHashIterator.getActionHashIterator(act_n,ivt,iva,CHECK[z],vc.getSameLevelThingTypeContainer());
	      if (global_act.is_in(act_n,ivt,CHECK[z]))
	      {
		      OUT.println(new DSOut(DSOut.ERR_OUT,DSOut.ACT,"An action already exists with args:"+makeString(ivt)+" and a different gives type ("+CHECK[z]+")",s),vc,AT);
		      return false;
	      }
      }
      
	      
      Action act = new Action(act_n, ivt, ivn, iva, r_t, statements, rstring, global_act, OUT, IN);
      act.setSynchd(synchd);
      global_act.add(act);
      
      if(StatementProcessor.USER_DEBUG)
      {
         OUT.println(new DSOut(DSOut.STD_OUT,DSOut.ACT,"We successfully created the action:"+act_n,true));
      }

      return true;

   }
public String makeStatementString(String[] states)
{
	StringBuffer sb=new StringBuffer(200);
	for (int i=0; i<states.length; i++)
	{
		sb.append(states[i]).append(';').append('\n');
	}
	return sb.toString();
}

public static void dumparray(String[] s)
{
	for (int i=0; i<s.length;i++)
	{
		System.err.println("s["+i+"]="+s[i]);
	}
}

}
