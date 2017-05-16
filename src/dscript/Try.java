package dscript;

import java.io.InputStream;

public class Try{

static long TRYCOUNTER=0;

public static boolean tryIt(Statement s, VarContainer vc, ActionContainer ac, ThingTypeContainer ttc, Output OUT, InputStream IN)
	{
	String[] parts = s.getParts();
	if (parts.length != 4) {return false;}
	if (!parts[1].startsWith("inline") || !parts[3].startsWith("inline") || !parts[2].equals("catch"))
		{return false;}

	String[] inlines = s.getInline();
	String trystring = parts[1];
	String catchstring = parts[3];
	

	for (int i=0; i<inlines.length; i++)
		{
		trystring = Statement.replace(trystring,"inline_code%"+i+"%",inlines[i]);
		catchstring = Statement.replace(catchstring,"inline_code%"+i+"%",inlines[i]);
		}

	String tryvar = new String("_try_"+TRYCOUNTER);
	TRYCOUNTER++;
	vc.add(new Var(false,tryvar));
	/*for 'fail with'... when in a catch-block the dustyscript programmer 
	specifies 'what went wrong' it looks for ATTEMPT_MESSAGE*/
	Var dam = new Var("","ATTEMPT_MESSAGE");
	dam.USER_SET=false;
	vc.add(dam);

	/*for dustyscript-level fails.. when an error occurs, it might set the DUSTY_ERROR_CODE
	when a dustyscript programmer specifies 'the mistake' or 'the error type', it
	accesses this variable*/
	Var dec = new Var("","DUSTY_ERROR_CODE");
	dec.USER_SET=false;
	vc.add(dec);
	trystring += "\n"+tryvar+" is now yes;\n";
	StatementProcessor tr =new StatementProcessor(trystring,vc,ac,ttc,OUT,IN);
	tr.suppress();
	/*keep from breaking too far out*/
	StatementProcessor.FAILPOINT = tr.getRunLevel(); 
	tr.setAttempting(true);
	tr.run();
	Var success = vc.get(tryvar);
	vc.remove(success);

	boolean b = success.getYes_No();
	/*we don't execute the catch if the variable has been changed*/
	if (b) {
	/*remove vars from local container-perhaps they should be nested instead?*/
	vc.remove("ATTEMPT_MESSAGE"); vc.remove("DUSTY_ERROR_CODE");
	return true;}

	StatementProcessor ctch =new StatementProcessor(catchstring,vc,ac,ttc,OUT,IN);
	ctch.suppress();
	ctch.run();
	/*once we are leaving the catch, we remove the error-vars*/
	vc.remove("ATTEMPT_MESSAGE");
	vc.remove("DUSTY_ERROR_CODE");
	return false;
	}

}

		