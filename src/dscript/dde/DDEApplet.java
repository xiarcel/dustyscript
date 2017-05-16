package dscript.dde;
import dscript.*;
import java.applet.*;

public class DDEApplet extends Applet {
public String urlpath="";

public DDEApplet()
{
DApplet.using_as_applet=true;
}

public void init()
{
	 	urlpath=getCodeBase().toString();
		ConsoleDusty.urlpath=urlpath;
		Use.urlpath=ConsoleDusty.urlpath;
}

public void start()
{
	try{
		new Thread(new DustyDevEnv()).start();
	}
	catch (Exception e) {e.printStackTrace();}
}


}

