package widget;
import java.awt.*;
import java.awt.event.*;
import dscript.connect.*;

public class DKeyTest extends Dustyable {

Frame f=new Frame("DKeyTest");
Panel p=new Panel(new BorderLayout());
TextArea one=new TextArea("",10,10);
TextArea two=new TextArea("",10,40);


public DKeyTest()
{
//System.out.println(Dustyable.addListener(this,one,'k'));
DButton db=new DButton();
System.out.println(Dustyable.addListener(this,db,'a'));
one.addKeyListener(this);
p.add(one,BorderLayout.WEST);
p.add(two,BorderLayout.EAST);
f.add(p);
f.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent we){System.exit(0);}});
f.pack();
f.setVisible(true);
}

public void message(String s)
{
two.append(s+"\n");
}

}

