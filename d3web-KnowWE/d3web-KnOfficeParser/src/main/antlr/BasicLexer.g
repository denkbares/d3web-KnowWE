/**
 * Lexer zum Import in andere Grammatiken, stellt grundlegende Token bereit
 * @author Markus Friedrich
 *
 */
lexer grammar BasicLexer;

options {
	language = Java;
}
@lexer::members {
  private LexerErrorHandler eh;
  private boolean newline=false;
  
  public void setNewline(boolean newline) {
    this.newline=newline;
  }
  
  public void setLexerErrorHandler(LexerErrorHandler eh) {
    this.eh = eh;
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
String : '"' (options {greedy=false;} : .)* ~'\\' '"';

INT : '0'..'9'+;

DOT : '.';
DD : ':';
COMMA : ',';
SEMI : ';';
EX : '!';
AT : '@';
ORS : '|';
NS : '#';
TILDE : '~';

//Klammern
LP : '(';
RP : ')';
CBO : '{';
CBC : '}';
SBO : '[';
SBC : ']';

//Vergleiche
LE : '<=';
L : '<';
GE : '>=';
G : '>';
EQ : '=';

//Operatoren
PLUS : '+';
MINUS : '-';
PROD : '*';
DIV : '/'; 

//Leerzeichen, Zeilenumbr�che und Kommentare werden ignoriert
//Zeilenumbr�che sind �ber Parameter aktivierbar
WS  : (' '|'\t') {$channel=HIDDEN;};
COMMENT : '//' ( options {greedy=false;} : .)*  '\n' {$channel=HIDDEN;}; 
NL : '\r'? '\n' {if (!newline) $channel=HIDDEN;};

//Schl�sselw�rter
IF : 'WENN'|'IF';
THEN : 'DANN'|'THEN';
AND : 'UND'|'AND';
OR : 'ODER'|'OR';
NOT : 'NICHT'|'NOT';
HIDE : 'VERBERGE'|'HIDE';
EXCEPT: 'AUSSER'|'EXCEPT';
UNKNOWN: 'UNBEKANNT'|'UNKNOWN';
KNOWN: 'BEKANNT'|'KNOWN';
INSTANT: 'INSTANT'|'SOFORT';
MINMAX: 'MINMAX';
IN: 'IN';
INTER: 'INTER';
ALL: 'ALLE'|'ALL';
ALLOWEDNAMES: '##allowedNames';
INCLUDE: '<include src';
DEFAULT: '<default>';
INIT: '<init>';
ABSTRACT: '<abstrakt>'|'<abstract>';
SET: 'SET';
REF: '&REF';
FUZZY: 'FUZZY';
DIVTEXT: 'DIV';
DIVNORM: 'DIV-NORM';

ID: ('A'..'Z'|'a'..'z'|'\u00a1'..'\uEFFF'|'%'|'$'|'&'|'\''|'?'|'_')+;
