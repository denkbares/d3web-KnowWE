/**
 * Grammatik zum Einlesen von Visiofiles und �berf�hren selbiger in die Standard ANTLR-Baumrepr�senation
 * @author Markus Friedrich
 *
 */
grammar Visio;
options {
	language = Java;
	output=AST;
}
import BasicLexer, BasicParser;
tokens {
  Page;
  Shape;
  Xcoord;
  Ycoord;
  Width;
  Height;
  Text;
  MyDouble;
  Shapetext;
  Picture;
  Box;
  QID;
  Textboxtext;
  Popup;
  Aidtext;
  Knowledge;
}
@lexer::header {
package de.d3web.KnOfficeParser.visio;
import de.d3web.KnOfficeParser.LexerErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;
}
@parser::header {
package de.d3web.KnOfficeParser.visio;
import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;
}
@lexer::members {
      private LexerErrorHandler eh;
      
     public VisioLexer(ANTLRInputStream input, LexerErrorHandler eh) {
        this(input);
        this.eh=eh;
        gBasicLexer.setLexerErrorHandler(eh);
      }
      
      public void setLexerErrorHandler(LexerErrorHandler eh) {
        this.eh = eh;
        gBasicLexer.setLexerErrorHandler(eh);
      }
      
      @Override
      public void reportError(RecognitionException re) {
        if (eh!=null) {
          eh.lexererror(re);
        } else {
          super.reportError(re);
        }
      }
}
@members {
  private ParserErrorHandler eh;
  
  public VisioParser(CommonTokenStream tokens, ParserErrorHandler eh) {
    this(tokens);
    this.eh=eh;
    gBasicParser.setEH(eh);
    if (eh!=null) eh.setTokenNames(tokenNames);
  }
  
  @Override
  public void reportError(RecognitionException re) {
    if (eh!=null) {
      eh.parsererror(re);
    } else {
      super.reportError(re);
    }
  }
}
knowledge : Start  page* '</Pages>' End -> ^(Knowledge page*);
page: Pagestart Pagesheet shapes '</Page>' -> ^(Page shapes);
shapes: '<Shapes>' (pic+=picture|shape|tex+=textbox)* '</Shapes>' {($tex.size()==1)&&($pic.size()==1)}? -> textbox* picture* shape*;
shape : x y width height shapetext -> ^(Shape x y width height shapetext);
picture : x y width height -> ^(Picture x y width height);
textbox : x y width height textboxtext -> ^(Box x y width height textboxtext);
x : Shapestart mydouble '</PinX>' -> ^(Xcoord mydouble);
y : '<PinY>' mydouble -> ^(Ycoord mydouble);
width: YtoWith c=mydouble '</Width>' -> ^(Width mydouble);
height: '<Height>' d=mydouble HeighttoText -> ^(Height mydouble);
shapetext: text '</Text></Shape>' -> ^(Shapetext text);
textboxtext: 'Bildname:' file 'Gr��e:' a=INT 'x' b=INT 'Frage:' questionid 'Folgefragen:' popup* '</Text></Shape>' -> ^(Textboxtext file INT INT questionid popup*);
questionid : name -> ^(QID name);
popup : (text ':' text ';') -> ^(Popup text text);
text : name -> ^(Text name);
file : name DOT name -> ^(Text name DOT name);
mydouble : d3double -> ^(MyDouble d3double);

Misc : '<cp IX=\'' (options {greedy=false;} : .)*  '\'/>' {$channel=HIDDEN;};
Misc2 : '<tp IX=\'' (options {greedy=false;} : .)* '\'/>' {$channel=HIDDEN;};
Misc3: '<pp IX=\'' (options {greedy=false;} : .)* '\'/>' {$channel=HIDDEN;};

Start : '<?xml version=\'1.0\' encoding=\'utf-8\' ?>' (options {greedy=false;} : .)*  '<Pages>';
Pagesheet: '<PageSheet' (options {greedy=false;} : .)* '</PageSheet>';
Pagestart: '<Page ID' (options {greedy=false;} : .)* '>';

Shapestart: '<Shape ID' (options {greedy=false;} : .)* '<PinX>';

YtoWith: '</PinY>' (options {greedy=false;} : .)* '<Width>';

HeighttoText: '</Height>' (options {greedy=false;} : .)* ('</Shape>'|'<Text>');

End: '<Windows' (options {greedy=false;} : .)* '</VisioDocument>';


