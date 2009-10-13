/**
 * Grammatik für XCL Grammatiken
 * @author Markus Friedrich
 *
 */
parser grammar XCL;

options {
	language = Java;
	tokenVocab=DefaultLexer;
}
import ComplexCondition;
@header{
package de.d3web.KnOfficeParser.xcl;
import de.d3web.KnOfficeParser.ConditionBuilder;
import de.d3web.KnOfficeParser.ParserErrorHandler;
}
@members{
  private XCLBuilder builder;
  private ParserErrorHandler eh;
  
  public XCL(CommonTokenStream tokens, XCLBuilder builder, ParserErrorHandler eh, ConditionBuilder cb) {
    this(tokens);
    this.builder=builder;
    this.eh=eh;
    gComplexCondition.setEH(eh);
    eh.setTokenNames(tokenNames);
    gComplexCondition.setBuilder(cb);
  }
  
  public void setBuilder(XCLBuilder builder) {
    this.builder=builder;
  }
  
  public XCLBuilder getBuilder() {
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
knowledge
: solutiondescription*;

solutiondescription
: name {builder.solution($start.getLine(), $text, $name.value);} CBO ( finding COMMA)+ CBC (SBO thr (COMMA thr)* SBC)?;

finding : complexcondition weight? {builder.finding($weight.text);};

weight
: SBO (PLUS PLUS|MINUS MINUS|EX|INT) SBC;

thr: name EQ d3double {builder.threshold($start.getLine(), $text, $name.value, $d3double.value);};
