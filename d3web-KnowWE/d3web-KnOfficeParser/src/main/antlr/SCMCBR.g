/**
 * Grammatik f�r SCMCBR Dateien
 * @author Markus Friedrich
 *
 */
parser grammar SCMCBR;

options {
	language = Java;
	tokenVocab = DefaultLexer;
}
import BasicParser;
@parser::header {
package de.d3web.KnOfficeParser.scmcbr;
import de.d3web.KnOfficeParser.ParserErrorHandler;
}
@members {
  private SCMCBRBuilder builder;
  private ParserErrorHandler eh;
  
  public SCMCBR(CommonTokenStream tokens, SCMCBRBuilder builder, ParserErrorHandler eh) {
    this(tokens);
    this.builder=builder;
    this.eh=eh;
    gBasicParser.setEH(eh);
    eh.setTokenNames(tokenNames);
  }
  
  public void setBuilder(SCMCBRBuilder builder) {
    this.builder = builder;
  }
  
  public SCMCBRBuilder getBuilder() {
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
knowledge:
(solution|NL)*;

solution:
name {builder.solution($start.getLine(), $text, $name.value);}DD NL (line NL)*;

line:
name {builder.question($start.getLine(), $text, $name.value);} (assign SEMI)* assign
| name DD d3double {builder.setAmount($start.getLine(), $text, $name.value, $d3double.value);}
| name CBO a=d3double b=d3double CBC {builder.threshold($start.getLine(), $text, $name.value, $a.value, $b.value);}
| name SBO INT SBC {builder.questionclass($start.getLine(), $text, $name.value);};

assign:
eq name weight? {builder.answer($start.getLine(), $text, $name.value, $weight.value, $eq.text);}
| IN (LP|c=SBO) a=values (RP|d=SBC) SBO b=values SBC {builder.in($start.getLine(), $text, $a.values, $b.values, ($c!=null), ($d!=null));}
| INTER (LP|c=SBO) a=values (RP|d=SBC) CBO b=values CBC {builder.into($start.getLine(), $text, $a.values, $b.values, ($c!=null), ($d!=null));}
| (AND|c=OR) LP names RP weight? {builder.and($start.getLine(), $text, $names.values, $weight.value, ($c!=null));}
| NOT name weight? {builder.not($start.getLine(), $text, $name.value, $weight.value);};

names returns [List<String> values]
@init {values = new ArrayList<String>();}
:  (a=name COMMA {values.add($a.text);})* b=name {values.add($b.text);};

values returns [List<Double> values]
@init {values = new ArrayList<Double>();}
: (a=d3double {values.add($a.value);})+;

weight returns [String value]:
SBO (PLUS|MINUS|EX)? INT? SBC {$value=$text.substring(1,$text.length()-1);};