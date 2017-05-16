
package dscript;

public final class StrBuff{

private char[] value;
private int count;
private static final StrBuff NULL = new StrBuff("null");
private static final char[] TRUE = new char[]{'t','r','u','e'};
private static final char[] FALSE = new char[]{'f','a','l','s','e'};


public StrBuff()
{
this(16);
}

public StrBuff(int length)
{
value = new char[length];
}

public StrBuff(String str)
{
this(str.length()+16);
append(str);
}

public int length()
{
return count;
}

public int capacity()
{
return value.length;
}

public void ensureCapacity(int minimumCapacity)
{
if (minimumCapacity > value.length)
	{
	expandCapacity(minimumCapacity);
	}
}

private void expandCapacity(int minCap)
{
int newcap = (value.length + 1) * 2;
if (newcap < 0) {newcap= Integer.MAX_VALUE;}
	else if (minCap > newcap)
		{
		newcap = minCap;
		}
char[] newvals = new char[newcap];
System.arraycopy(value,0,newvals,0,count);
value=newvals;
}


public void setLength(int newlength)
{
if (newlength <0) {throw new RuntimeException("String index out of bounds:"+newlength);}
if (newlength > value.length) {expandCapacity(newlength);}
if (count < newlength)
	{
	for (; count <newlength;count++)
		{
		value[count]='\0';
		}
	}
	else{
		count=newlength;
		}
}

public char charAt(int index)
{
if ((index<0) || (index>=count))
	{
	throw new RuntimeException("String index out of bounds:"+index);
	}
return value[index];
}

public StrBuff append(Object obj)
{
return append(String.valueOf(obj));
}

public StrBuff append(String str)
{
if (str == null) {str= String.valueOf(str);}
return append(str.toCharArray());
}

public StrBuff append(char[] c)
{
int len = c.length;
int newcount = count + len;
if (newcount > value.length)
	{
	expandCapacity(newcount);
	}
System.arraycopy(c,0,value,count,len);
count=newcount;
return this;
}

public StrBuff append(StrBuff sb)
{
if (sb==null){sb=NULL;}
return append(sb.toCharArray());

}


public char[] toCharArray()
{
char[] cop = new char[count];
System.arraycopy(value,0,cop,0,count);
return cop;
}

public StrBuff append(boolean b)
{
if (b) {return append(TRUE);}
return append(FALSE);
}

public StrBuff append(char c)
{
int newcount = count + 1;
if (newcount > value.length)
	{
	expandCapacity(newcount);
	}
value[count] = c; count++;
return this;
}

public StrBuff append(int i)
{
return append(String.valueOf(i));
}

public StrBuff append(long l)
{
return append(String.valueOf(l));
}

public StrBuff append(float f)
{
return append(String.valueOf(f));
}

public StrBuff append(double d)
{
return append(String.valueOf(d));
}

public String toString()
{
return new String(toCharArray());
}

}
