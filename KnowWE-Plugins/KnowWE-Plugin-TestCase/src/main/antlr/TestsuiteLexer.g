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
 * Lexer fuer Testsuite-Section, stellt spezielle Testsuite-Tokens bereit
 * @author Sebastian Furth
 *
 */
lexer grammar TestsuiteLexer;


options {
	language = Java;
}
import BasicLexer;
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

package de.d3web.we.testsuite;
import de.d3web.KnOfficeParser.LexerErrorHandler;
}
@lexer::members {
      private LexerErrorHandler eh;
      
      public TestsuiteLexer(ANTLRInputStream input, LexerErrorHandler eh) {
        this(input);
        this.eh=eh;
        gBasicLexer.setLexerErrorHandler(eh);
      }
      
      public void setNewline(boolean newline) {
        gBasicLexer.setNewline(newline);
      }
      
      public void setLexerErrorHandler(LexerErrorHandler eh) {
        this.eh = eh;
        gBasicLexer.setLexerErrorHandler(eh);
      }
      
      @Override
      public void reportError(RecognitionException re) {
        if (eh!=null) {
          eh.lexererror(re);
        } else {
          super.reportError(re);
        }
      }
}

HEURISTIC : 'heuristic'|'HEURISTIC'|'Heuristic';
XCL : 'xcl'|'XCL'|'Xcl';





