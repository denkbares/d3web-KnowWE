/**
 * Baumgrammatik um aus einer eingelesenen Visiodatei ein XML Output zu erzeugen 
 * @author Markus Friedrich
 *
 */
tree grammar VisiotoXML;

options {
	language = Java;
	output = template;
	tokenVocab=Visio;
	ASTLabelType=CommonTree;
}
//Memberfunktionen dienen lediglich zum Berechnen der Koordinaten und speichern der Strings 
@members{
  private double x,y;
  
  private int ox,oy;
  
  private double resizeX, resizeY;
  
  String picname = new String();
  String questionid = new String();
  
  private void setCorner(double x, double y, double width, double height) {
    this.x=x-(width/2);
    this.y=y+(height/2);
    resizeX=(ox/width);
    resizeY=(oy/height);
  }
  
  private long getXstart(double x, double width) {
    return Math.round((x-(width/2)-this.x)*resizeX);
  }
  
  private long getXend(double x, double width) {
    return Math.round((x+(width/2)-this.x)*resizeX);
  }
  
  private long getYstart(double y, double height) {
    return Math.round((this.y-y-(height/2))*resizeY);
  }
  
  private long getYend(double y, double height) {
    return Math.round((this.y-y+(height/2))*resizeY);
  }
  
  private String delQuotes(String s) {
    s=s.substring(1, s.length()-1);
    s=s.replace("\\\"", "\"");
    return s;
  }
  
  private String delXML(String s) {
        int i = s.indexOf('<');
        while (i>=0) {
          int j = s.indexOf(">");
          String t = s.substring(i, j+1);
          s=s.replace(t, "");
          i = s.indexOf('<');
        }
        return s;
      }
}

@treeparser::header {
package de.d3web.KnOfficeParser.visio;
}
knowledge : ^(Knowledge pages+=page*) -> foo(i={$pages});
page : ^(Page textbox picture i+=shape*) -> root(it={$i}, qid={questionid}, popq={$textbox.st}, picname={picname});
picture : ^(Picture x y width height) {setCorner($x.d,$y.d, $width.d, $height.d);};
textbox: ^(Box x y width height textboxtext)-> string(w={$textboxtext.st});
shape : ^(Shape x y width height shapetext) -> shape(xstart={getXstart($x.d, $width.d)}, xend={getXend($x.d, $width.d)}, ystart={getYstart($y.d, $height.d)}, yend={getYend($y.d, $height.d)}, text={$shapetext.st});
x returns [double d]: ^(Xcoord mydouble) {$d=Double.parseDouble($mydouble.text);};
y returns [double d]: ^(Ycoord mydouble) {$d=Double.parseDouble($mydouble.text);};
width returns [double d]: ^(Width mydouble) {$d=Double.parseDouble($mydouble.text);};
height returns [double d]: ^(Height mydouble) {$d=Double.parseDouble($mydouble.text);};
shapetext: ^(Shapetext text) -> string(w={delXML($text.text)});
textboxtext: ^(Textboxtext a=file o1=INT o2=INT questionid pops+=popup*) {questionid = $questionid.text; ox = Integer.parseInt($o1.text); oy =Integer.parseInt($o2.text); picname = $a.text;}-> foo(i={$pops});
file : ^(Text name DOT name);
questionid : ^(QID name);
popup : ^(Popup a=text b=text) -> popupquestion(target={$b.text}, answer={$a.text});
text : ^(Text name) -> string(w={$name.value});

mydouble : ^(MyDouble MINUS? INT DOT INT);
name returns [String value]
: String* (ID|INT) (ID|INT|String)* {$value=$text;}
| String {$value=delQuotes($String.text);};