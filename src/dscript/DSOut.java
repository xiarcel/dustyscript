
package dscript;

public class DSOut {

/*static constants*/
private final static String LS = System.getProperty("line.separator");
private final static String ls = LS;

/*for access as DSOut.STD_OUT...etc..*/

public final static int STD_OUT = 0;
public final static int ERR_OUT = 1;
public final static int DBG_OUT = 2;


private final static String errfirst = new String("->Dustyscript came across an error while ");
private final static String errsecond = new String("--The specific error is:"+LS);
private final static String errnotavail = new String("--The specified error was not available");
private final static String succnotavail=new String("doing something you asked it to");
private final static String errorstate = new String("--The specific statement was available, and is:"+ls);
private final static String noerror = new String("->Dustyscript reporting the result when ");

public final static int SAY = 0;
public final static int ASK = 1;
public final static int ASS = 2;
public final static int CNT = 3;
public final static int IF = 4;
public final static int IFBLOCK=5;
public final static int ACT = 6;
public final static int THD = 7;
public final static int THG = 8;
public final static int JAV = 9;
/*for error-codes in auto:try-catch*/
public final static String[] errorValues = new String[]
	{
	"SAY","ASK","ASSIGN","COUNT","IF","IF-BLOCK",
	"ACTION","THING-TYPE","THING","JAVA-CONNECTOR"
	};

private final static String[] categories = new String[]{"saying something",
			"ask'ing for something","assigning a variable","'count'ing",
			"comparing something to something else",
		      "running a statement after after comparing",
			"working with an 'action'","running something separately",
			"defining or using a 'Thing'","working with a 'JavaConnector'"};


/*member variables*/

private int streamtype,category;
private String specific_message="";
private boolean specifier = false;
private boolean not_error=false;
private Statement state = null;
private boolean error_not_available = false;


public DSOut(int CAT, String SPEC)
{
streamtype= ERR_OUT; category=CAT; specific_message = SPEC; specifier=true; not_error=false;
}

public DSOut(int OSTREAM, int CAT, String SPEC, boolean n_e)
{
streamtype=OSTREAM; category=CAT; specific_message=SPEC;
specifier = true; 
not_error=n_e;
}

public DSOut(int OSTREAM, int CAT, String SPEC)
{
streamtype=OSTREAM; category=CAT; specific_message=SPEC; specifier=true; not_error=false;
}


public DSOut(int OSTREAM, int CAT, String SPEC, Statement STATE, boolean n_e)
{
streamtype=OSTREAM; category=CAT; specific_message=SPEC; state=STATE;
specifier = true; 
not_error=n_e;
}

public DSOut(int CAT, String SPEC, Statement STATE)
{
streamtype=ERR_OUT; category=CAT; specific_message=SPEC; state=STATE;
specifier=true;
not_error=false;
}

public DSOut(int OSTREAM, int CAT, String SPEC, Statement STATE)
{
streamtype=OSTREAM; category=CAT; specific_message=SPEC; state= STATE;
specifier=true;
not_error=false;
}


public DSOut(int OSTREAM, int CAT, Statement STATE)
{
streamtype=OSTREAM; category=CAT; state=STATE; specifier=false;
not_error=false;
}


public DSOut(int OSTREAM, int CAT, Statement STATE, boolean n_e)
{
not_error=n_e;
streamtype=OSTREAM; category=CAT; state=STATE; specifier=false;
}


public DSOut(int OSTREAM, int CAT)
{
streamtype=OSTREAM; category=CAT; specifier=false; 
not_error=false;
}

public DSOut(int OSTREAM, int CAT, boolean n_e)
{
streamtype=OSTREAM; category=CAT; specifier=false; 
not_error=n_e;
}



public String getMessage()
{
 String statestring="";
 if (state != null) {statestring = getStatementString(state);}

 String message = "";
 if (not_error) {message=message+noerror;}
	else{message=message+errfirst;}

 if ((category<0) | (category>(categories.length-1)))
	{
	message = message + succnotavail + LS;
	}
	else
		{
		message = message + categories[category] + LS;
		}

 if ((!specifier) & !not_error)
	{
	message = message + errnotavail + LS;
	}
 else if (specifier & !not_error)
	{
	message = message + errsecond + specific_message + LS;
	}
 else if (specifier & not_error)
	{
	message = message + specific_message+LS;
	}


 if (!not_error & (state != null))
	{
	message = message + errorstate + statestring + LS;
	}
 else if (not_error & (state != null))
	{
	message = message + "The specific statement where successful:"+LS+statestring;
	}

 return message;
}

public int getStreamType(){return streamtype;}

private String getStatementString(Statement s)
{
 /*for now*/
 return s.originalUserStatement();
}

public int getCategory(){return category;}
public static void main (String[] args)
{
 
System.out.println(new DSOut(1,ASS,"Variables not present").getMessage());
System.out.println(new DSOut(1,SAY).getMessage());
}

}
 