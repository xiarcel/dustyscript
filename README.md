# dustyscript

The last release of this project was almost 12 years ago...and that was an update primarily in packaging, organization, etc....
Originally, this project was hosted at sourceforge (http://dustyscript.sourceforge.net) ..

It was written, if my memory serves correctly, to run on Java 1.3.1 ...but this evening (5-15-17) I ran it on a modern jdk (1.8.x)...

## caveats

1) In no way should this project be considered a good reference for anything relating to "how to program in Java" .. or good practices..
(It may have never been that...but certainly is not that for modern development)
For example, lack of generics back then led to some very interesting "wrap" classes (most notably for Map functions) ..  An example of 
naievete' is the use of  Vector extensively instead of ArrayList .. although I am fuzzy as to when ArrayList appeared on the scene.

2) Dustyscript started as a project to teach programming to children, specifically, one child.  It became more an exercise in
reaching new heights of basic java knowledge, and reaching new distances as far as how far I could take it... It taught me way more than
it taught that one child, about programming.

3) Also..simple concept of absolute vs. relative paths, etc.. seemed to have eluded me at the time.

## basic executions.
If you download the source/etc...  and navigate to the bin/ directory...  the following should work

### java -classpath ../jar/dustyscript.jar dscript.VisualShell
This will run a basic shell for interactive execution of Dustyscript commands.

### java -classpath ../jar/dustyscript.jar dscript.dde.DustyDevEnv
This will run the IDE.  It allows for authoring/saving of Dustyscript source..

### java -classpath ../jar/dustyscript.jar dscript.StatementProcessor \<file\>.txt
This will execute the Dustyscript program saved in ./users/\<file\>.txt relative to the bin directory 





