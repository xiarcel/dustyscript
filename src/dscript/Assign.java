/*changed class to static methods*/

package dscript;

   import java.util.Vector;
   import java.io.InputStream;

   class Assign{
	final static Exception excep = new Exception();
   	final static int ER = DSOut.ERR_OUT;
	final static int AS = DSOut.ASS;
      final static String[] vartypes = new String[]{"integer","string","decimal","yes_no","character","thing","javaconnector","anyvar"};
   	final static String[] falseplus=new String[]{"now","using","is","=","is_now"};
   	final static String[] err_control=new String[0];

      public static int type(String key, VarContainer vc)
      {
	      ThingTypeContainer ttc=vc.getSameLevelThingTypeContainer();
	      int m = type(key);
	      if (m == -1 && ttc.is_in(key))
	      {return 5;}
      return m;
      }
      
      public static int type(String key) 
      {
         for (int i=0; i< vartypes.length;i++)
         {
            if (key.equalsIgnoreCase(vartypes[i]))
            {	
               return i;
            }
         }
         return -1;
      }
   
      public static void exec(VarContainer vc, Statement state, Output OUT, InputStream IN) throws Exception
      {
	     // StatementProcessor.DEBUG=true;
	 /*below caused MAJOR error*/
	 //AssignChooser.dump(state.getParts());
	 state.resetParts(Statement.replaceLiterals(state,state.getParts()));
	 //AssignChooser.dump(state.getParts());
	 boolean reassign=false;
	 boolean attempting = state.getAttempting();
         String[] control =preprocess(state,vc,OUT);
         if (control.length < 3)
		{OUT.println(new DSOut(ER,AS,"This statement is too short",state),vc,attempting);
		 throw excep;
		}
         int j=type(control[0],vc);
         int wheretype=-1;
         int whattype=-1;
         String varname="";
         String varvalue="";
      
         if (j==-1)
         {
            int k=type(control[2]);
            if (k==-1)
            {
		if (!control[1].equals("is_now"))
		{
		OUT.println(new DSOut(ER,AS,"Dustyscript expected 'is now', and got '"+control[1]+"'",state),
			vc,attempting);
		if (StatementProcessor.DEBUG)
		{
		for (int ii=0; ii<control.length;ii++)
		{OUT.println("control["+ii+"]:"+control[ii],2);}
		}
		throw excep;
		}
		else{
		 Var v= vc.get(control[0]);
		 int abtpy = v.getAbsoluteType();
		 if (abtpy == Var.ANYVAR)
			{whattype=7;}
		 else{
		 int tpy = v.getType();
		 if (tpy == 1){whattype=0;}
		 else if (tpy == 0){whattype=3;}
		 else if (tpy == 4){whattype=1;}
		 else if (tpy == 2){whattype=2;}
		 else if (tpy == 3){whattype=4;}
		 else if (tpy == Var.THING){whattype=5;}
		 else if (tpy == Var.JAVA) {whattype=6;}
		    }
		 reassign=true;}
             }
            wheretype=2;if (whattype==-1){whattype=k;}
         }
         else{wheretype=0;whattype=j;}
         int start =-1;
         if (wheretype==2)
         {
            String s = control[1];
		s=s.trim();
		boolean b=false;
	    if(s.equalsIgnoreCase("is_now"))
		{b=true;}
		else{b=false;}
            /*
	    if ((s.equalsIgnoreCase("is_an")==false)&&(s.equalsIgnoreCase("is_a")==false)&&(s.equals("=")==false)&&(b==false))
            {
               OUT.println(new DSOut(ER,AS,"Dustyscript expected 'is a' and got '"+s+"'",state),vc,attempting);
               throw excep;
            }
	    */
            varname = control[0];
	    /*
	    if (reassign==false)
	    {
            if (!control[3].equalsIgnoreCase("that_is") && !b)
            {OUT.println(new DSOut(ER,AS,"Dustyscript expected 'that is', and got '"+control[3]+"'",state),vc,attempting);
               throw excep;
	       
            }
            
	    }*/
	   if (reassign==true)
		{start=2;}
	   else{start=4;}

         }
         if (wheretype ==0)
         {
            varname=control[1];
            if ((control[2].equalsIgnoreCase("=")==false)&&(control[2].equalsIgnoreCase("is")==false))
            {
               OUT.println(new DSOut(ER,AS,"Dustyscript expected 'is' or '=' and got '"+control[2]+"'",state),vc,attempting);
               throw excep;
            }
            start = 3;
         }
         String[] values = new String[(control.length-start)];
	 
      
	try{
         for (int n = start; n<control.length;n++)
         {
         
            values[(n-start)]=control[n];
         }
	  }catch(Exception e){if (StatementProcessor.DEBUG){OUT.println("cl="+control.length+"::st="+start,2);}}
	  String vtype=vartypes[whattype];
	  //System.err.println("vtype=="+vtype+", whattype="+whattype);
	  String stype="";
	  
	  if (reassign && (whattype==5))
	  {
		  stype=vc.get(control[0]).getThing().getThingType();
	  }
	  else if (whattype==5)
	  {
		  stype=control[0];
	  }
	  
	  
	  if (vtype.equals("thing") && !stype.equals("") && !vtype.equals(stype))
	  {
		  String oldvtype=vtype;
		  vtype=stype;
		  if (vc.is_in(vtype))
		  {
			  Var X=vc.get(vtype);
			  if (X.getType() == Var.THING) 
	         		{
		  		vtype=X.getThing().getThingType();
	  			}
			
		  }
	}
	
	if (vtype.equals("anyvar") && ! reassign)
	{
		reassign=true;
		vc.add(new Var(varname,16));
	}
	
	
        Var v= AssignChooser.assign(values,varname,vtype,vc,OUT,IN,state);
	   if ((v.getType() < 0)||v.asString().equals(">NOVALUE<")||v.asString().equals(">>NULL<<"))
		{throw excep;}

	
         if (reassign==true)
	     {if (!vc.is_in(varname)){throw excep;}
		
		Var already=vc.get(varname);
		if (already.getType()==Var.CHR &&!already.isGlobal())
		{
		vc.replace(varname,v);
		}
		already.setValue(v);
		if (already.isGlobal()){v.setAsGlobal();}
		return;
		}
	
	 vc.add(v);
      }
   
   
   
      private static String[] preprocess(Statement state, VarContainer vc,Output OUT)
      {
         String[] parts = state.getParts();
         boolean attempting = state.getAttempting();
      
         String[] refs = state.getRefs();
      /*we need String literals back in...probably need to put this in statement*/
      
         for (int i=0;i<refs.length;i++)
         {
            for (int j=0;j<parts.length;j++)
            {
               if (parts[j].equals("$"+i+"$"))
               {parts[j]=refs[i];}

	    
            }
         }
	   String[] charrefs=state.getCharRefs();
	   for (int k=0; k<charrefs.length;k++)
		{
		for (int m=0;m<parts.length;m++)
			{
			if (parts[m].equals("&"+k+"&"))
			{parts[m]=charrefs[k];}
			}
		}

         Vector v=new Vector();
      /*vector for adding altered parts*/
      
         for (int k=0;k<parts.length;k++)
         {boolean bb=true;
            if (parts[k].equalsIgnoreCase("is"))
            {
               try{
                  String s=parts[(k+1)];
		  if (s.equalsIgnoreCase("now"))
		  {
		  v.addElement("is_now");
		  k++;
		  continue;
		  }
               }
                  catch(Exception e)
                  {}
            }
          /*removed 'that is'*/

	       if (parts[k].equals("-"))
		{

		bb=eval_plus(vc,parts,parts[(k-1)],(k-1));
		try{
		parts[k]=new String("+");
		parts[(k+1)]=new String("-"+parts[(k+1)]);
		}catch(Exception e)
			{
			OUT.println(new DSOut(ER,AS,"Dustyscript failed at this 'assignment'\n\t..because of a dangling (or extra) '-'",state),
			vc,attempting);
			return err_control;}
		}
            if(bb){v.addElement(parts[k]);}
         }
         String[] control=new String[v.size()];
         for (int w=0; w<control.length;w++)
         {
            control[w]=(String)v.elementAt(w);
           
         }
	 return control;
      }
   

public static boolean eval_plus(VarContainer vc,String[] pts, String pt, int index)
{
for (int i=0; i<falseplus.length;i++)
	{
	if (pt.equalsIgnoreCase(falseplus[i])){return false;}
	}

try{
   String s = pts[index-1];
   if (s.endsWith(",")){return false;}
   if (vc.is_in(s))
	{
	for (int x=(index-2);x>-1;x--)
	 {
	 if (pts[x].equalsIgnoreCase("using")){return false;}
	 }
      }
   }
   catch(Exception e){/*this could be a null, or whatever, we don't care*/}
return true;
}


   
   }


