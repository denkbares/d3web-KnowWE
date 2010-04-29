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
 * Grammatik fuer Entscheidungsbaeume
 * @author Markus Friedrich
 *
 */
parser grammar DashTree;

options {
	language = Java;
	tokenVocab = DefaultLexer;
}
import BasicParser;
@parser::header {
package de.d3web.KnOfficeParser.dashtree;
import de.d3web.KnOfficeParser.ParserErrorHandler;
}
@members {
  private int dashcount = 0;
  private DashTBuilder builder;
  private ParserErrorHandler eh;
  
  public DashTree(CommonTokenStream tokens, DashTBuilder builder, ParserErrorHandler eh) {
    this(tokens);
    this.builder=builder;
    this.eh=eh;
    gBasicParser.setEH(eh);
    eh.setTokenNames(tokenNames);
  }
  
  public void setBuilder(DashTBuilder builder) {
    this.builder = builder;
  }
  
  public DashTBuilder getBuilder() {
    return builder;
  }
  
  private String delQuotes(String s) {
    s=s.substring(1, s.length()-1);
    s=s.replace("\\\"", "\"");
    return s;
  }
  
  private Double parseGerDouble(String s) {
    s=s.replace(',', '.');
    return Double.parseDouble(s);
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

knowledge
: (line |NL{builder.newLine();})* deslimit? (description NL)* description?;

line
@init{int i=0;}
: (node[0]
|include
|dashes {i=$dashes.i;}(node[i])) NL ;

node [int Dashes]
: a=name (TILDE b=name)? (NS manualref)? (SBO order SBC)? {dashcount = $Dashes; builder.addNode($Dashes, $a.value, $manualref.text, $a.start.getLine(), $b.value, $order.o);};

include
: INCLUDE EQ String G {builder.addInclude(delQuotes($String.text), $String.getLine(), $text);};

deslimit
@init {List<String> allowedNames = new ArrayList<String>();}
: ALLOWEDNAMES EQ CBO (a=ids {allowedNames.add($a.text);}COMMA)* b=ids {allowedNames.add($b.text);}CBC NL {builder.setallowedNames(allowedNames, $start.getLine(), $text);};

order returns [int o]
: INT {$o=Integer.parseInt($INT.text);};

ids
: ID+;

description
: ORS AT a=name ORS c=name ORS b=name ORS destext ORS {builder.addDescription($a.value, $c.text, $b.value, $destext.text, $a.start.getLine(), $text);};

diagvalue returns [String value]
: LP ((MINUS INT | INT DOT)=> d3double {$value=$d3double.value.toString();} |name {$value=$name.value;} | EX {$value="!";} ) RP ;

destext
: ( options {greedy=false;} : ~ORS)*;

link returns [String s1, String s2]
: SBO SBO a=name SBC (SBO b=name SBC)? SBC {$s1=$a.text; $s2=$b.text;};

type
: SBO ID SBC;

dashes returns [int i]
: {i=0;} (MINUS {i++;})+ ;

manualref:
(ID|INT)*;

idlink returns [String s]:
AT name {$s=$name.text;};

dialogannotations returns [List<String> attribute, List<String> value]
@init {$attribute = new ArrayList<String>(); $value = new ArrayList<String>();}
: LP (AT a=name DD b=String SEMI {$attribute.add($a.text); $value.add(delQuotes($b.text));})* AT a=name DD b=String SEMI? RP {$attribute.add($a.text); $value.add(delQuotes($b.text));};
