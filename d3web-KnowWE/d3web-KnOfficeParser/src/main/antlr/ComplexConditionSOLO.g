parser grammar ComplexConditionSOLO;

options {
	language = Java;
	tokenVocab=DefaultLexer;
}
import ComplexCondition;
@header {
package de.d3web.KnOfficeParser.complexcondition;
import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;
}
@members {
  private ParserErrorHandler eh;
  
  public void setEH(ParserErrorHandler eh) {
    this.eh=eh;
    gComplexCondition.setEH(eh);
  }
  
  public void setBuilder(ConditionBuilder builder) {
    gComplexCondition.setBuilder(builder);
  }
  
  public ConditionBuilder getBuilder() {
    return gComplexCondition.getBuilder();
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
knowledge: complexcondition;
