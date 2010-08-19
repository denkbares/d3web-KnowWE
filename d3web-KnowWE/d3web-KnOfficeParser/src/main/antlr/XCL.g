/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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
 * Grammatik fuer XCL Grammatiken
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
/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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
