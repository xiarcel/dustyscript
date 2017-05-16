
package dscript;
import java.io.*;

public class Output {

public static final Output DEF=new Output();

private PrintStream error,stdout,debug;
public final static String LS =System.getProperty("line.separator");
private boolean ERR=true;
public Output() //throws Exception
{
error = stdout = debug = System.out;
}

public Output(OutputStream o, OutputStream e,OutputStream d) throws Exception
{
 error=new PrintStream(e); stdout=new PrintStream(o); debug=new PrintStream(d);
}

public Output(PrintStream o,PrintStream e, PrintStream d) throws Exception
{
 error=e; stdout=o; debug=d;
}

public void toggleErrorStream(boolean nw)
{
ERR = nw;
}


public boolean setErrorStream(PrintStream e) throws Exception
{
error = e;
return true;
}

public boolean setErrorStream(OutputStream e) throws Exception
{
 error = new PrintStream(e);
 return true;
}

public boolean setStandardStream(PrintStream o) throws Exception
{
 stdout=o;
 return true;
}


public boolean setStandardStream(OutputStream o) throws Exception
{
 stdout =new PrintStream(o);
 return true;
}


public boolean setDebugStream(PrintStream d) throws Exception
{
 debug =d; 
 return true;
}

public boolean setDebugStream(OutputStream d) throws Exception
{
 debug =new PrintStream(d); 
 return true;
}

public PrintStream getDebugStream()
{
 return debug;
}

public void println(String what) {println(what,0);}
public void print(String what){print(what,0);}


public void print(String what, int type)
	{
	if (what == null){return;}
	if ((type < 0)||(type>2)){type=0;}
	switch(type)
		{
		case 0: {stdout.print(what);stdout.flush();break;}
		case 1: {if (ERR){error.print(what);error.flush();}break;}
		case 2: {debug.print(what);debug.flush();break;}
		default:{ break;}
		}
	}

public void println(String what, int type)
	{
	print(what+LS,type);
	}


public void print(DSOut dsout)
	{
	print(dsout.getMessage(),dsout.getStreamType());
	}

public void println(DSOut dsout)
	{
	println(dsout.getMessage(),dsout.getStreamType());
	}

public void println(DSOut dsout, VarContainer vc, boolean attempting)
	{
	if (!attempting) {println(dsout);}
		else{setErrorCodes(vc,dsout);}
	}

public static void setErrorCodes(VarContainer vc, DSOut dso)
	{
	/*this method should provide a traceback for error-responses, as well
		as allow the suppression of output to the end user if the
		dscript programmer wraps something in a try-catch block
		*/

	if (vc.is_in("ATTEMPT_MESSAGE"))
		{
		Var am = vc.get("ATTEMPT_MESSAGE");
		if (!am.USER_SET){am.setValue(dso.getMessage()+"\n::\n"+am.asString());}
		}
	if (vc.is_in("DUSTY_ERROR_CODE"))
		{
		String err_code="";
		int catty = dso.getCategory();
		if ((catty<0) || (catty>9))
			{
			err_code="UNDEFINED";
			}
			else {err_code = DSOut.errorValues[catty];}
		Var ec = vc.get("DUSTY_ERROR_CODE");
		ec.setValue(err_code+"::"+ec.asString());
		}
	}

}
