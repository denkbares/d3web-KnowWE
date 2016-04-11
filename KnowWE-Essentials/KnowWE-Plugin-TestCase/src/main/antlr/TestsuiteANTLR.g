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
 * Grammatik für Testsuites
 * @author Sebastian Furth
 *
 */
parser grammar TestsuiteANTLR;

options {
	language = Java;
	tokenVocab = TestsuiteLexer;
}
import BasicParser;
@parser::header{
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

package de.d3web.we.testsuite;
import de.d3web.we.testsuite.TestsuiteBuilder;
import de.d3web.KnOfficeParser.ParserErrorHandler;
}
@members{
  private TestsuiteBuilder builder;
  private ParserErrorHandler eh;
  
  public TestsuiteANTLR(CommonTokenStream tokens, TestsuiteBuilder builder, ParserErrorHandler eh) {
    this(tokens);
    this.builder=builder;
    this.eh=eh;
  }
  
  public void setBuilder(TestsuiteBuilder builder) {
    this.builder=builder;
  }
  
  public TestsuiteBuilder getBuilder() {
    return builder;
  }
  
  private String delQuotes(String s) {
    s=s.substring(1, s.length()-1);
    s=s.replace("\\\"", "\"");
    return s;
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
: sqtestcase* {builder.createTestSuite();};

sqtestcase
@init {int i = 0;}
: n=name {builder.addSequentialTestCase($n.value);} CBO ({i++;} ratedtestcase[i])+ CBC {builder.finishCurrentSequentialTestCase();};

ratedtestcase[int i]
: {builder.addRatedTestCase(i, $start.getLine(), $text);} findings DD solutions? SEMI {builder.finishCurrentRatedTestCase();};

findings
: (q=name EQ a=name COMMA {builder.addFinding($q.value, $a.value, $start.getLine(), $text);})* 
  (q=name EQ a=name {builder.addFinding($q.value, $a.value, $start.getLine(), $text);});

solutions
: ((heuristic_solution|xcl_solution|normalsolution) COMMA)* (heuristic_solution|xcl_solution|normalsolution);

heuristic_solution
: (n=name (LP HEURISTIC DD r=name RP) {builder.addHeuristicSolution($n.value, $r.value, $start.getLine(), $text);});	

xcl_solution
: (n=name (LP XCL DD r=name RP) {builder.addXCLSolution($n.value, $r.value, $start.getLine(), $text);});

normalsolution
: (n=name (LP r=name RP) {builder.addSolution($n.value, $r.value, $start.getLine(), $text);});

name returns [String value]
: (ID|d3double)+ {$value=$text;}
| String {$value=delQuotes($String.text);};
