lexer grammar DefaultLexer;

options {
	language = Java;
}
import BasicLexer;
@header{
package de.d3web.KnOfficeParser;
}
@lexer::members {
      private LexerErrorHandler eh;
      
      public DefaultLexer(ANTLRInputStream input, LexerErrorHandler eh) {
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
BLUB:;
