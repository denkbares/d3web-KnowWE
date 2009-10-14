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
parser grammar DecisionTree;

options {
	language = Java;
	tokenVocab = DefaultLexer;
}
import BasicParser;
@parser::header {
package de.d3web.KnOfficeParser.decisiontree;
import de.d3web.KnOfficeParser.ParserErrorHandler;
}
@members {
  private int dashcount = 0;
  private DTBuilder builder;
  private ParserErrorHandler eh;
  
  public DecisionTree(CommonTokenStream tokens, DTBuilder builder, ParserErrorHandler eh) {
    this(tokens);
    this.builder=builder;
    this.eh=eh;
    gBasicParser.setEH(eh);
    eh.setTokenNames(tokenNames);
  }
  
  public void setBuilder(DTBuilder builder) {
    this.builder = builder;
  }
  
  public DTBuilder getBuilder() {
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
: (questionclass {builder.finishOldQuestionsandConditions(0);}
|include
|dashes {i=$dashes.i;}{(i<=dashcount+1)}? {builder.finishOldQuestionsandConditions(i);}(numeric[i]|answer[i]|question[i]|diagnosis[i]|manyQCLinks[i])) NL ;

questionclass
: name dialogannotations? {dashcount = 0;} {builder.addQuestionclass($name.value, $start.getLine(), $text, $dialogannotations.attribute, $dialogannotations.value);};

question [int Dashes]
: (REF h=name {builder.addQuestionLink($Dashes, $h.value, $h.start.getLine(), $text);}
//referenzierte Frage oder neue Frage definiert
| a=name synonyms? (TILDE b=name)? SBO ID SBC (CBO e=name CBC)? (LP f=d3double g=d3double RP)? c=ABSTRACT? idlink? dialogannotations? (NS manualref)? {builder.addQuestion($Dashes, $a.value, $b.value, ($c!=null), $ID.text, $manualref.text, (f!=null?f.value:null), (g!=null?g.value:null), $e.value, $synonyms.syn, $a.start.getLine(), $text, $idlink.s, $dialogannotations.attribute, $dialogannotations.value);}) {dashcount = $Dashes;};

//lie�t Antworten und Links zu weiteren Fragen
answer [int Dashes]
: name synonyms? idlink? (NS manualref)? a=DEFAULT? b= INIT? {dashcount = $Dashes; builder.addAnswerOrQuestionLink($Dashes, $name.value, $manualref.text, $synonyms.syn, ($a!=null), ($b!=null), $name.start.getLine(), $text, $idlink.s);};

diagnosis [int Dashes]
@init {List<String> diags = new ArrayList<String>();}
: a=name {diags.add($a.value);} (NS a=name {diags.add($a.value);})* b=SET? diagvalue link? idlink? {dashcount = $Dashes; builder.addDiagnosis($Dashes, diags, ($b!=null), $diagvalue.value, $link.s1, $link.s2 ,$a.start.getLine(), $text, $idlink.s);};

numeric [int Dashes]
: (op=eq d1=d3double|SBO d1=d3double d2=d3double SBC) {dashcount = $Dashes; builder.addNumericAnswer($Dashes, $d1.value, $d2.value, $op.text, $start.getLine(), $text);};

manyQCLinks [int Dashes]
@init {List<String> qcs = new ArrayList<String>();}
: (a=name {qcs.add($a.value);} SEMI)+ a=name {qcs.add($a.value); dashcount = $Dashes; builder.addManyQuestionClassLink($Dashes, qcs, $start.getLine(), $text);};

include
: INCLUDE EQ String G {builder.addInclude(delQuotes($String.text), $String.getLine(), $text);};

deslimit
@init {List<String> allowedNames = new ArrayList<String>();}
: ALLOWEDNAMES EQ CBO (a=ids {allowedNames.add($a.text);}COMMA)* b=ids {allowedNames.add($b.text);}CBC NL {builder.setallowedNames(allowedNames, $start.getLine(), $text);};

ids
: ID+;

description
: ORS AT a=name ORS c=name ORS (ID DD DD)? b=name ORS destext ORS {builder.addDescription($a.value, $c.text, $b.value, $destext.text, $a.start.getLine(), $text, $ID.text);};

synonyms returns [List<String> syn]
@init {syn = new ArrayList<String>();}
:  (CBO(a=name SEMI {syn.add($a.text);})* b=name {syn.add($b.text);}CBC);

diagvalue returns [String value]
: LP ((MINUS INT | INT DOT)=> d3double {$value=$d3double.value.toString();} |name {$value=$name.value;} | EX {$value="!";} ) RP ;

destext
: ( options {greedy=false;} : ~ORS)*;

//res returns [Double low, Double high]: (eq d3double) {$high=$d3double.value;}|(SBO a=d3double b=d3double SBC){$high=$b.value; $low=$a.value;};

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
