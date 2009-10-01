/**
 * Grammatik für komplexe Regeln
 * @author Markus Friedrich
 *
 */
parser grammar Complexrules;

options {
	language = Java;
	tokenVocab = DefaultLexer;
}
import ComplexCondition;
@members {
  private RuleBuilder builder;
  private ParserErrorHandler eh;
  
  public Complexrules(CommonTokenStream tokens, RuleBuilder builder, ParserErrorHandler eh, ConditionBuilder cb) {
    this(tokens);
    this.builder=builder;
    this.eh=eh;
    gComplexCondition.setEH(eh);
    if (eh!=null) eh.setTokenNames(tokenNames);
    gComplexCondition.setBuilder(cb);
  }
  
  public void setBuilder(RuleBuilder builder) {
    this.builder = builder;
  }
  
  public RuleBuilder getBuilder() {
    return builder;
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
@header {
package de.d3web.KnOfficeParser.rule;
import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;
}
knowledge
: complexrule*;

complexrule
: IF complexcondition (a=EXCEPT complexcondition NL?)? THEN ruleaction[($a!=null)];

ruleaction[boolean except]
: indicationrule[except] | suppressrule[except] | abstractionrule[except];

indicationrule [boolean except]
:  (n=names | a=INSTANT SBO n=names SBC | b=NOT SBO n=names SBC) {builder.indicationrule($n.start.getLine(), $text, $n.nlist, $n.tlist, except, (a!=null), (b!=null));};

scoreOrName returns[String value]
: name {$value=$name.value;} | EX {$value=$text;};

suppressrule [boolean except]
: HIDE a=name type? EQ SBO names SBC {builder.suppressrule($a.start.getLine(), $text, $a.value, $type.value, $names.nlist, except);};

abstractionrule [boolean except]
: a=name type? {builder.questionOrDiagnosis($a.start.getLine(), $text, $a.value, $type.value);} eqncalc ((formulaOrName (PLUS|MINUS|DIV|PROD)) =>formulawithoutP {builder.numValue($a.start.getLine(), $text, except, $eqncalc.text);}
| scoreOrName {builder.choiceOrDiagValue($a.start.getLine(), $text, $eqncalc.text, $scoreOrName.value, except);} 
|  formula  {builder.numValue($a.start.getLine(), $text, except, $eqncalc.text);}
);

formula
: LP formulawithoutP RP;

formulawithoutP
: name {builder.formula($name.start.getLine(), $text, $name.value);}
| (MINUS INT | INT DOT)=> d3double {builder.formula($start.getLine(), $text, $d3double.value.toString());}
| formulaOrName (PLUS formulaOrName {builder.formulaAdd();}
|MINUS formulaOrName {builder.formulaSub();}
| PROD formulaOrName {builder.formulaMult();}
| DIV formulaOrName {builder.formulaDiv();}
);

formulaOrName
:formula|(MINUS INT | INT DOT)=> d3double {builder.formula($start.getLine(), $text, $d3double.value.toString());}|name {builder.formula($name.start.getLine(), $text, $name.value);};

names returns[List<String> nlist, List<String> tlist]
@init {$nlist = new ArrayList<String>(); $tlist = new ArrayList<String>();}
:a=name c=type? {$nlist.add($a.value); $tlist.add($c.value);} (SEMI b=name d=type? {$nlist.add($b.value); $tlist.add($d.value);})*;

intervall returns[Double a, Double b]
: SBO d1=d3double d2=d3double SBC {$a=$d1.value; $b=$d2.value;};
