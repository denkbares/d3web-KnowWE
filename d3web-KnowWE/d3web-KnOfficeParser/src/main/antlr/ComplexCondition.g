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
: a=name type? (eq nod=nameOrDouble {builder.condition( $start.getLine(), $text, $a.value, $type.value, $eq.text, $nod.value);}
| in=IN? intervall  {builder.condition($start.getLine(), $text, $a.value, $type.value, $intervall.a, $intervall.b, (in!=null));}) 
| (KNOWN|c=UNKNOWN) SBO a=name type? SBC {builder.knowncondition($start.getLine(), $text, $a.value, $type.value, c!=null);}
| {List<String> answers= new ArrayList();} a=name type? IN CBO b=name {answers.add($b.value);} (COMMA d=name {answers.add($d.value);})* CBC {builder.in($start.getLine(), $text, $a.value, $type.value, answers);}
| {List<String> answers= new ArrayList();} a=name type? ALL CBO b=name {answers.add($b.value);} (COMMA d=name {answers.add($d.value);})* CBC {builder.all($start.getLine(), $text, $a.value, $type.value, answers);};



intervall returns[Double a, Double b]
: SBO d1=d3double d2=d3double SBC {$a=$d1.value; $b=$d2.value;};
