parser grammar ComplexCondition;

options {
	language = Java;
}
import BasicParser;

@members {
  private ConditionBuilder builder;
  private ParserErrorHandler eh;
  
  public void setEH(ParserErrorHandler eh) {
    this.eh=eh;
    gBasicParser.setEH(eh);
  }
  
  public void setBuilder(ConditionBuilder builder) {
    this.builder = builder;
  }
  
  public ConditionBuilder getBuilder() {
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
//Verhindert die Warnung das keine Startregel vorhanden ist
startruleComplexCondition: ;

complexcondition
:
dnf
| {int i=1;} MINMAX LP a=INT b=INT RP CBO complexcondition (SEMI complexcondition {i++;})* CBC {builder.minmax($start.getLine(), $text, Integer.parseInt($a.text), Integer.parseInt($b.text), i);};

dnf
: disjunct (OR disjunct {builder.orcond($text);})*;

disjunct
: conjunct (AND conjunct {builder.andcond($text);})*;

conjunct	
: condition
| LP complexcondition RP {builder.complexcondition($text);}
| NOT conjunct {builder.notcond($text);};

condition
: a=name type? (eq b=name {builder.condition( $start.getLine(), $text, $a.value, $type.value, $eq.text, $b.value);}
| in=IN? intervall  {builder.condition($start.getLine(), $text, $a.value, $type.value, $intervall.a, $intervall.b, (in!=null));}) 
| (KNOWN|c=UNKNOWN) SBO a=name type? SBC {builder.knowncondition($start.getLine(), $text, $a.value, $type.value, c!=null);}
| {List<String> answers= new ArrayList();} a=name type? IN CBO b=name {answers.add($b.value);} (COMMA d=name {answers.add($d.value);})* CBC {builder.in($start.getLine(), $text, $a.value, $type.value, answers);}
| {List<String> answers= new ArrayList();} a=name type? ALL CBO b=name {answers.add($b.value);} (COMMA d=name {answers.add($d.value);})* CBC {builder.all($start.getLine(), $text, $a.value, $type.value, answers);};

intervall returns[Double a, Double b]
: SBO d1=d3double d2=d3double SBC {$a=$d1.value; $b=$d2.value;};
