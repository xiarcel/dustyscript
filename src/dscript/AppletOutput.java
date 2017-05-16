package dscript;
import java.awt.*;

public class AppletOutput extends Output{
	
	private TextArea text=null;
public AppletOutput(TextArea t) throws Exception
{
	
	text=t;
}
	
public void print(String what, int type)
{
	if (what == null){return;}
	if (type == 2) {what="DEBUG:\n======\n"+what+"\n======\n";}
	text.append(what);
}
}

