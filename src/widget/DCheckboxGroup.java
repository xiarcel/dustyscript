package widget;

import java.awt.*;
import dscript.connect.*;

public class DCheckboxGroup extends Dustyable{

private CheckboxGroup cbg;

public DCheckboxGroup()
	{cbg=new CheckboxGroup();}

public boolean isComponent(){return false;}
public CheckboxGroup getCheckboxGroup(){return cbg;}
public boolean isOnlyContainer() {return true;}

/*finis*/
}
