/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
