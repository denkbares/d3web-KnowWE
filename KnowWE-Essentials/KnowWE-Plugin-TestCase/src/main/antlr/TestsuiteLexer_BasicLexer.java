// $ANTLR 3.1.1 BasicLexer.g 2010-08-18 21:19:36

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


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/**
 * Lexer zum Import in andere Grammatiken, stellt grundlegende Token bereit
 * @author Markus Friedrich
 *
 */
public class TestsuiteLexer_BasicLexer extends Lexer {
    public static final int HIDE=38;
    public static final int RP=16;
    public static final int ORS=12;
    public static final int FUZZY=54;
    public static final int LP=15;
    public static final int ABSTRACT=51;
    public static final int NOT=37;
    public static final int ID=57;
    public static final int AND=35;
    public static final int EXCEPT=39;
    public static final int EOF=-1;
    public static final int DD=7;
    public static final int IF=33;
    public static final int AT=11;
    public static final int IN=44;
    public static final int THEN=34;
    public static final int UNKNOWN=40;
    public static final int EX=10;
    public static final int COMMA=8;
    public static final int INCLUDE=48;
    public static final int ALL=46;
    public static final int PROD=28;
    public static final int TILDE=14;
    public static final int PLUS=26;
    public static final int String=4;
    public static final int NL=32;
    public static final int EQ=25;
    public static final int DOT=6;
    public static final int COMMENT=31;
    public static final int HEURISTIC=58;
    public static final int GE=23;
    public static final int G=24;
    public static final int SBC=20;
    public static final int ALLOWEDNAMES=47;
    public static final int L=22;
    public static final int INSTANT=42;
    public static final int NS=13;
    public static final int MINMAX=43;
    public static final int DEFAULT=49;
    public static final int INTER=45;
    public static final int KNOWN=41;
    public static final int SET=52;
    public static final int INT=5;
    public static final int DIVNORM=56;
    public static final int MINUS=27;
    public static final int Tokens=60;
    public static final int SEMI=9;
    public static final int XCL=59;
    public static final int REF=53;
    public static final int WS=30;
    public static final int CBC=18;
    public static final int OR=36;
    public static final int SBO=19;
    public static final int DIVTEXT=55;
    public static final int DIV=29;
    public static final int INIT=50;
    public static final int CBO=17;
    public static final int LE=21;

      private LexerErrorHandler eh;
      private boolean newline=false;
      
      public void setNewline(boolean newline) {
        this.newline=newline;
      }
      
      public void setLexerErrorHandler(LexerErrorHandler eh) {
        this.eh = eh;
      }
      
      @Override
      public void reportError(RecognitionException re) {
        if (eh!=null) {
          eh.lexererror(re);
        } else {
          super.reportError(re);
        }
      }


    // delegates
    // delegators
    public TestsuiteLexer gTestsuiteLexer;
    public TestsuiteLexer gParent;

    public TestsuiteLexer_BasicLexer() {;} 
    public TestsuiteLexer_BasicLexer(CharStream input, TestsuiteLexer gTestsuiteLexer) {
        this(input, new RecognizerSharedState(), gTestsuiteLexer);
    }
    public TestsuiteLexer_BasicLexer(CharStream input, RecognizerSharedState state, TestsuiteLexer gTestsuiteLexer) {
        super(input,state);

        this.gTestsuiteLexer = gTestsuiteLexer;
        gParent = gTestsuiteLexer;
    }
    public String getGrammarFileName() { return "BasicLexer.g"; }

    // $ANTLR start "String"
    public final void mString() throws RecognitionException {
        try {
            int _type = String;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:52:8: ( '\"' ( options {greedy=false; } : . )* ~ '\\\\' '\"' )
            // BasicLexer.g:52:10: '\"' ( options {greedy=false; } : . )* ~ '\\\\' '\"'
            {
            match('\"'); 
            // BasicLexer.g:52:14: ( options {greedy=false; } : . )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='\u0000' && LA1_0<='[')||(LA1_0>=']' && LA1_0<='\uFFFF')) ) {
                    int LA1_1 = input.LA(2);

                    if ( (LA1_1=='\"') ) {
                        alt1=2;
                    }
                    else if ( ((LA1_1>='\u0000' && LA1_1<='!')||(LA1_1>='#' && LA1_1<='\uFFFF')) ) {
                        alt1=1;
                    }


                }
                else if ( (LA1_0=='\\') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // BasicLexer.g:52:41: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            if ( (input.LA(1)>='\u0000' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFF') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "String"

    // $ANTLR start "INT"
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:54:5: ( ( '0' .. '9' )+ )
            // BasicLexer.g:54:7: ( '0' .. '9' )+
            {
            // BasicLexer.g:54:7: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // BasicLexer.g:54:7: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INT"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:56:5: ( '.' )
            // BasicLexer.g:56:7: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "DD"
    public final void mDD() throws RecognitionException {
        try {
            int _type = DD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:57:4: ( ':' )
            // BasicLexer.g:57:6: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DD"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:58:7: ( ',' )
            // BasicLexer.g:58:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "SEMI"
    public final void mSEMI() throws RecognitionException {
        try {
            int _type = SEMI;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:59:6: ( ';' )
            // BasicLexer.g:59:8: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMI"

    // $ANTLR start "EX"
    public final void mEX() throws RecognitionException {
        try {
            int _type = EX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:60:4: ( '!' )
            // BasicLexer.g:60:6: '!'
            {
            match('!'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EX"

    // $ANTLR start "AT"
    public final void mAT() throws RecognitionException {
        try {
            int _type = AT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:61:4: ( '@' )
            // BasicLexer.g:61:6: '@'
            {
            match('@'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AT"

    // $ANTLR start "ORS"
    public final void mORS() throws RecognitionException {
        try {
            int _type = ORS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:62:5: ( '|' )
            // BasicLexer.g:62:7: '|'
            {
            match('|'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ORS"

    // $ANTLR start "NS"
    public final void mNS() throws RecognitionException {
        try {
            int _type = NS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:63:4: ( '#' )
            // BasicLexer.g:63:6: '#'
            {
            match('#'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NS"

    // $ANTLR start "TILDE"
    public final void mTILDE() throws RecognitionException {
        try {
            int _type = TILDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:64:7: ( '~' )
            // BasicLexer.g:64:9: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TILDE"

    // $ANTLR start "LP"
    public final void mLP() throws RecognitionException {
        try {
            int _type = LP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:67:4: ( '(' )
            // BasicLexer.g:67:6: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LP"

    // $ANTLR start "RP"
    public final void mRP() throws RecognitionException {
        try {
            int _type = RP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:68:4: ( ')' )
            // BasicLexer.g:68:6: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RP"

    // $ANTLR start "CBO"
    public final void mCBO() throws RecognitionException {
        try {
            int _type = CBO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:69:5: ( '{' )
            // BasicLexer.g:69:7: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CBO"

    // $ANTLR start "CBC"
    public final void mCBC() throws RecognitionException {
        try {
            int _type = CBC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:70:5: ( '}' )
            // BasicLexer.g:70:7: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CBC"

    // $ANTLR start "SBO"
    public final void mSBO() throws RecognitionException {
        try {
            int _type = SBO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:71:5: ( '[' )
            // BasicLexer.g:71:7: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SBO"

    // $ANTLR start "SBC"
    public final void mSBC() throws RecognitionException {
        try {
            int _type = SBC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:72:5: ( ']' )
            // BasicLexer.g:72:7: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SBC"

    // $ANTLR start "LE"
    public final void mLE() throws RecognitionException {
        try {
            int _type = LE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:75:4: ( '<=' )
            // BasicLexer.g:75:6: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LE"

    // $ANTLR start "L"
    public final void mL() throws RecognitionException {
        try {
            int _type = L;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:76:3: ( '<' )
            // BasicLexer.g:76:5: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "L"

    // $ANTLR start "GE"
    public final void mGE() throws RecognitionException {
        try {
            int _type = GE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:77:4: ( '>=' )
            // BasicLexer.g:77:6: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GE"

    // $ANTLR start "G"
    public final void mG() throws RecognitionException {
        try {
            int _type = G;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:78:3: ( '>' )
            // BasicLexer.g:78:5: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "G"

    // $ANTLR start "EQ"
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:79:4: ( '=' )
            // BasicLexer.g:79:6: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQ"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:82:6: ( '+' )
            // BasicLexer.g:82:8: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:83:7: ( '-' )
            // BasicLexer.g:83:9: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "PROD"
    public final void mPROD() throws RecognitionException {
        try {
            int _type = PROD;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:84:6: ( '*' )
            // BasicLexer.g:84:8: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PROD"

    // $ANTLR start "DIV"
    public final void mDIV() throws RecognitionException {
        try {
            int _type = DIV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:85:5: ( '/' )
            // BasicLexer.g:85:7: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DIV"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:89:5: ( ( ' ' | '\\t' ) )
            // BasicLexer.g:89:7: ( ' ' | '\\t' )
            {
            if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:90:9: ( '//' ( options {greedy=false; } : . )* '\\n' )
            // BasicLexer.g:90:11: '//' ( options {greedy=false; } : . )* '\\n'
            {
            match("//"); 

            // BasicLexer.g:90:16: ( options {greedy=false; } : . )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\n') ) {
                    alt3=2;
                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='\t')||(LA3_0>='\u000B' && LA3_0<='\uFFFF')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // BasicLexer.g:90:44: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('\n'); 
            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMENT"

    // $ANTLR start "NL"
    public final void mNL() throws RecognitionException {
        try {
            int _type = NL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:91:4: ( ( '\\r' )? '\\n' )
            // BasicLexer.g:91:6: ( '\\r' )? '\\n'
            {
            // BasicLexer.g:91:6: ( '\\r' )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0=='\r') ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // BasicLexer.g:91:6: '\\r'
                    {
                    match('\r'); 

                    }
                    break;

            }

            match('\n'); 
            if (!newline) _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NL"

    // $ANTLR start "IF"
    public final void mIF() throws RecognitionException {
        try {
            int _type = IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:94:4: ( 'WENN' | 'IF' )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0=='W') ) {
                alt5=1;
            }
            else if ( (LA5_0=='I') ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // BasicLexer.g:94:6: 'WENN'
                    {
                    match("WENN"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:94:13: 'IF'
                    {
                    match("IF"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IF"

    // $ANTLR start "THEN"
    public final void mTHEN() throws RecognitionException {
        try {
            int _type = THEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:95:6: ( 'DANN' | 'THEN' )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='D') ) {
                alt6=1;
            }
            else if ( (LA6_0=='T') ) {
                alt6=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // BasicLexer.g:95:8: 'DANN'
                    {
                    match("DANN"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:95:15: 'THEN'
                    {
                    match("THEN"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "THEN"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:96:5: ( 'UND' | 'AND' )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='U') ) {
                alt7=1;
            }
            else if ( (LA7_0=='A') ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // BasicLexer.g:96:7: 'UND'
                    {
                    match("UND"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:96:13: 'AND'
                    {
                    match("AND"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:97:4: ( 'ODER' | 'OR' )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0=='O') ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1=='D') ) {
                    alt8=1;
                }
                else if ( (LA8_1=='R') ) {
                    alt8=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // BasicLexer.g:97:6: 'ODER'
                    {
                    match("ODER"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:97:13: 'OR'
                    {
                    match("OR"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:98:5: ( 'NICHT' | 'NOT' )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0=='N') ) {
                int LA9_1 = input.LA(2);

                if ( (LA9_1=='I') ) {
                    alt9=1;
                }
                else if ( (LA9_1=='O') ) {
                    alt9=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // BasicLexer.g:98:7: 'NICHT'
                    {
                    match("NICHT"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:98:15: 'NOT'
                    {
                    match("NOT"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "HIDE"
    public final void mHIDE() throws RecognitionException {
        try {
            int _type = HIDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:99:6: ( 'VERBERGE' | 'HIDE' )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='V') ) {
                alt10=1;
            }
            else if ( (LA10_0=='H') ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // BasicLexer.g:99:8: 'VERBERGE'
                    {
                    match("VERBERGE"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:99:19: 'HIDE'
                    {
                    match("HIDE"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HIDE"

    // $ANTLR start "EXCEPT"
    public final void mEXCEPT() throws RecognitionException {
        try {
            int _type = EXCEPT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:100:7: ( 'AUSSER' | 'EXCEPT' )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='A') ) {
                alt11=1;
            }
            else if ( (LA11_0=='E') ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // BasicLexer.g:100:9: 'AUSSER'
                    {
                    match("AUSSER"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:100:18: 'EXCEPT'
                    {
                    match("EXCEPT"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXCEPT"

    // $ANTLR start "UNKNOWN"
    public final void mUNKNOWN() throws RecognitionException {
        try {
            int _type = UNKNOWN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:101:8: ( 'UNBEKANNT' | 'UNKNOWN' )
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0=='U') ) {
                int LA12_1 = input.LA(2);

                if ( (LA12_1=='N') ) {
                    int LA12_2 = input.LA(3);

                    if ( (LA12_2=='B') ) {
                        alt12=1;
                    }
                    else if ( (LA12_2=='K') ) {
                        alt12=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 12, 2, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // BasicLexer.g:101:10: 'UNBEKANNT'
                    {
                    match("UNBEKANNT"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:101:22: 'UNKNOWN'
                    {
                    match("UNKNOWN"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNKNOWN"

    // $ANTLR start "KNOWN"
    public final void mKNOWN() throws RecognitionException {
        try {
            int _type = KNOWN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:102:6: ( 'BEKANNT' | 'KNOWN' )
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0=='B') ) {
                alt13=1;
            }
            else if ( (LA13_0=='K') ) {
                alt13=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;
            }
            switch (alt13) {
                case 1 :
                    // BasicLexer.g:102:8: 'BEKANNT'
                    {
                    match("BEKANNT"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:102:18: 'KNOWN'
                    {
                    match("KNOWN"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "KNOWN"

    // $ANTLR start "INSTANT"
    public final void mINSTANT() throws RecognitionException {
        try {
            int _type = INSTANT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:103:8: ( 'INSTANT' | 'SOFORT' )
            int alt14=2;
            int LA14_0 = input.LA(1);

            if ( (LA14_0=='I') ) {
                alt14=1;
            }
            else if ( (LA14_0=='S') ) {
                alt14=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }
            switch (alt14) {
                case 1 :
                    // BasicLexer.g:103:10: 'INSTANT'
                    {
                    match("INSTANT"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:103:20: 'SOFORT'
                    {
                    match("SOFORT"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INSTANT"

    // $ANTLR start "MINMAX"
    public final void mMINMAX() throws RecognitionException {
        try {
            int _type = MINMAX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:104:7: ( 'MINMAX' )
            // BasicLexer.g:104:9: 'MINMAX'
            {
            match("MINMAX"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINMAX"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:105:3: ( 'IN' )
            // BasicLexer.g:105:5: 'IN'
            {
            match("IN"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "INTER"
    public final void mINTER() throws RecognitionException {
        try {
            int _type = INTER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:106:6: ( 'INTER' )
            // BasicLexer.g:106:8: 'INTER'
            {
            match("INTER"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INTER"

    // $ANTLR start "ALL"
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:107:4: ( 'ALLE' | 'ALL' )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0=='A') ) {
                int LA15_1 = input.LA(2);

                if ( (LA15_1=='L') ) {
                    int LA15_2 = input.LA(3);

                    if ( (LA15_2=='L') ) {
                        int LA15_3 = input.LA(4);

                        if ( (LA15_3=='E') ) {
                            alt15=1;
                        }
                        else {
                            alt15=2;}
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 15, 2, input);

                        throw nvae;
                    }
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 15, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // BasicLexer.g:107:6: 'ALLE'
                    {
                    match("ALLE"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:107:13: 'ALL'
                    {
                    match("ALL"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ALL"

    // $ANTLR start "ALLOWEDNAMES"
    public final void mALLOWEDNAMES() throws RecognitionException {
        try {
            int _type = ALLOWEDNAMES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:108:13: ( '##allowedNames' )
            // BasicLexer.g:108:15: '##allowedNames'
            {
            match("##allowedNames"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ALLOWEDNAMES"

    // $ANTLR start "INCLUDE"
    public final void mINCLUDE() throws RecognitionException {
        try {
            int _type = INCLUDE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:109:8: ( '<include src' )
            // BasicLexer.g:109:10: '<include src'
            {
            match("<include src"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INCLUDE"

    // $ANTLR start "DEFAULT"
    public final void mDEFAULT() throws RecognitionException {
        try {
            int _type = DEFAULT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:110:8: ( '<default>' )
            // BasicLexer.g:110:10: '<default>'
            {
            match("<default>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DEFAULT"

    // $ANTLR start "INIT"
    public final void mINIT() throws RecognitionException {
        try {
            int _type = INIT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:111:5: ( '<init>' )
            // BasicLexer.g:111:7: '<init>'
            {
            match("<init>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "INIT"

    // $ANTLR start "ABSTRACT"
    public final void mABSTRACT() throws RecognitionException {
        try {
            int _type = ABSTRACT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:112:9: ( '<abstrakt>' | '<abstract>' )
            int alt16=2;
            alt16 = dfa16.predict(input);
            switch (alt16) {
                case 1 :
                    // BasicLexer.g:112:11: '<abstrakt>'
                    {
                    match("<abstrakt>"); 


                    }
                    break;
                case 2 :
                    // BasicLexer.g:112:24: '<abstract>'
                    {
                    match("<abstract>"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ABSTRACT"

    // $ANTLR start "SET"
    public final void mSET() throws RecognitionException {
        try {
            int _type = SET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:113:4: ( 'SET' )
            // BasicLexer.g:113:6: 'SET'
            {
            match("SET"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SET"

    // $ANTLR start "REF"
    public final void mREF() throws RecognitionException {
        try {
            int _type = REF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:114:4: ( '&REF' )
            // BasicLexer.g:114:6: '&REF'
            {
            match("&REF"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "REF"

    // $ANTLR start "FUZZY"
    public final void mFUZZY() throws RecognitionException {
        try {
            int _type = FUZZY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:115:6: ( 'FUZZY' )
            // BasicLexer.g:115:8: 'FUZZY'
            {
            match("FUZZY"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FUZZY"

    // $ANTLR start "DIVTEXT"
    public final void mDIVTEXT() throws RecognitionException {
        try {
            int _type = DIVTEXT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:116:8: ( 'DIV' )
            // BasicLexer.g:116:10: 'DIV'
            {
            match("DIV"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DIVTEXT"

    // $ANTLR start "DIVNORM"
    public final void mDIVNORM() throws RecognitionException {
        try {
            int _type = DIVNORM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:117:8: ( 'DIV-NORM' )
            // BasicLexer.g:117:10: 'DIV-NORM'
            {
            match("DIV-NORM"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DIVNORM"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // BasicLexer.g:119:3: ( ( 'A' .. 'Z' | 'a' .. 'z' | '\\u00a1' .. '\\uEFFF' | '%' | '$' | '&' | '\\'' | '?' | '_' )+ )
            // BasicLexer.g:119:5: ( 'A' .. 'Z' | 'a' .. 'z' | '\\u00a1' .. '\\uEFFF' | '%' | '$' | '&' | '\\'' | '?' | '_' )+
            {
            // BasicLexer.g:119:5: ( 'A' .. 'Z' | 'a' .. 'z' | '\\u00a1' .. '\\uEFFF' | '%' | '$' | '&' | '\\'' | '?' | '_' )+
            int cnt17=0;
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);

                if ( ((LA17_0>='$' && LA17_0<='\'')||LA17_0=='?'||(LA17_0>='A' && LA17_0<='Z')||LA17_0=='_'||(LA17_0>='a' && LA17_0<='z')||(LA17_0>='\u00A1' && LA17_0<='\uEFFF')) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // BasicLexer.g:
            	    {
            	    if ( (input.LA(1)>='$' && input.LA(1)<='\'')||input.LA(1)=='?'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||(input.LA(1)>='\u00A1' && input.LA(1)<='\uEFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt17 >= 1 ) break loop17;
                        EarlyExitException eee =
                            new EarlyExitException(17, input);
                        throw eee;
                }
                cnt17++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ID"

    public void mTokens() throws RecognitionException {
        // BasicLexer.g:1:8: ( String | INT | DOT | DD | COMMA | SEMI | EX | AT | ORS | NS | TILDE | LP | RP | CBO | CBC | SBO | SBC | LE | L | GE | G | EQ | PLUS | MINUS | PROD | DIV | WS | COMMENT | NL | IF | THEN | AND | OR | NOT | HIDE | EXCEPT | UNKNOWN | KNOWN | INSTANT | MINMAX | IN | INTER | ALL | ALLOWEDNAMES | INCLUDE | DEFAULT | INIT | ABSTRACT | SET | REF | FUZZY | DIVTEXT | DIVNORM | ID )
        int alt18=54;
        alt18 = dfa18.predict(input);
        switch (alt18) {
            case 1 :
                // BasicLexer.g:1:10: String
                {
                mString(); 

                }
                break;
            case 2 :
                // BasicLexer.g:1:17: INT
                {
                mINT(); 

                }
                break;
            case 3 :
                // BasicLexer.g:1:21: DOT
                {
                mDOT(); 

                }
                break;
            case 4 :
                // BasicLexer.g:1:25: DD
                {
                mDD(); 

                }
                break;
            case 5 :
                // BasicLexer.g:1:28: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 6 :
                // BasicLexer.g:1:34: SEMI
                {
                mSEMI(); 

                }
                break;
            case 7 :
                // BasicLexer.g:1:39: EX
                {
                mEX(); 

                }
                break;
            case 8 :
                // BasicLexer.g:1:42: AT
                {
                mAT(); 

                }
                break;
            case 9 :
                // BasicLexer.g:1:45: ORS
                {
                mORS(); 

                }
                break;
            case 10 :
                // BasicLexer.g:1:49: NS
                {
                mNS(); 

                }
                break;
            case 11 :
                // BasicLexer.g:1:52: TILDE
                {
                mTILDE(); 

                }
                break;
            case 12 :
                // BasicLexer.g:1:58: LP
                {
                mLP(); 

                }
                break;
            case 13 :
                // BasicLexer.g:1:61: RP
                {
                mRP(); 

                }
                break;
            case 14 :
                // BasicLexer.g:1:64: CBO
                {
                mCBO(); 

                }
                break;
            case 15 :
                // BasicLexer.g:1:68: CBC
                {
                mCBC(); 

                }
                break;
            case 16 :
                // BasicLexer.g:1:72: SBO
                {
                mSBO(); 

                }
                break;
            case 17 :
                // BasicLexer.g:1:76: SBC
                {
                mSBC(); 

                }
                break;
            case 18 :
                // BasicLexer.g:1:80: LE
                {
                mLE(); 

                }
                break;
            case 19 :
                // BasicLexer.g:1:83: L
                {
                mL(); 

                }
                break;
            case 20 :
                // BasicLexer.g:1:85: GE
                {
                mGE(); 

                }
                break;
            case 21 :
                // BasicLexer.g:1:88: G
                {
                mG(); 

                }
                break;
            case 22 :
                // BasicLexer.g:1:90: EQ
                {
                mEQ(); 

                }
                break;
            case 23 :
                // BasicLexer.g:1:93: PLUS
                {
                mPLUS(); 

                }
                break;
            case 24 :
                // BasicLexer.g:1:98: MINUS
                {
                mMINUS(); 

                }
                break;
            case 25 :
                // BasicLexer.g:1:104: PROD
                {
                mPROD(); 

                }
                break;
            case 26 :
                // BasicLexer.g:1:109: DIV
                {
                mDIV(); 

                }
                break;
            case 27 :
                // BasicLexer.g:1:113: WS
                {
                mWS(); 

                }
                break;
            case 28 :
                // BasicLexer.g:1:116: COMMENT
                {
                mCOMMENT(); 

                }
                break;
            case 29 :
                // BasicLexer.g:1:124: NL
                {
                mNL(); 

                }
                break;
            case 30 :
                // BasicLexer.g:1:127: IF
                {
                mIF(); 

                }
                break;
            case 31 :
                // BasicLexer.g:1:130: THEN
                {
                mTHEN(); 

                }
                break;
            case 32 :
                // BasicLexer.g:1:135: AND
                {
                mAND(); 

                }
                break;
            case 33 :
                // BasicLexer.g:1:139: OR
                {
                mOR(); 

                }
                break;
            case 34 :
                // BasicLexer.g:1:142: NOT
                {
                mNOT(); 

                }
                break;
            case 35 :
                // BasicLexer.g:1:146: HIDE
                {
                mHIDE(); 

                }
                break;
            case 36 :
                // BasicLexer.g:1:151: EXCEPT
                {
                mEXCEPT(); 

                }
                break;
            case 37 :
                // BasicLexer.g:1:158: UNKNOWN
                {
                mUNKNOWN(); 

                }
                break;
            case 38 :
                // BasicLexer.g:1:166: KNOWN
                {
                mKNOWN(); 

                }
                break;
            case 39 :
                // BasicLexer.g:1:172: INSTANT
                {
                mINSTANT(); 

                }
                break;
            case 40 :
                // BasicLexer.g:1:180: MINMAX
                {
                mMINMAX(); 

                }
                break;
            case 41 :
                // BasicLexer.g:1:187: IN
                {
                mIN(); 

                }
                break;
            case 42 :
                // BasicLexer.g:1:190: INTER
                {
                mINTER(); 

                }
                break;
            case 43 :
                // BasicLexer.g:1:196: ALL
                {
                mALL(); 

                }
                break;
            case 44 :
                // BasicLexer.g:1:200: ALLOWEDNAMES
                {
                mALLOWEDNAMES(); 

                }
                break;
            case 45 :
                // BasicLexer.g:1:213: INCLUDE
                {
                mINCLUDE(); 

                }
                break;
            case 46 :
                // BasicLexer.g:1:221: DEFAULT
                {
                mDEFAULT(); 

                }
                break;
            case 47 :
                // BasicLexer.g:1:229: INIT
                {
                mINIT(); 

                }
                break;
            case 48 :
                // BasicLexer.g:1:234: ABSTRACT
                {
                mABSTRACT(); 

                }
                break;
            case 49 :
                // BasicLexer.g:1:243: SET
                {
                mSET(); 

                }
                break;
            case 50 :
                // BasicLexer.g:1:247: REF
                {
                mREF(); 

                }
                break;
            case 51 :
                // BasicLexer.g:1:251: FUZZY
                {
                mFUZZY(); 

                }
                break;
            case 52 :
                // BasicLexer.g:1:257: DIVTEXT
                {
                mDIVTEXT(); 

                }
                break;
            case 53 :
                // BasicLexer.g:1:265: DIVNORM
                {
                mDIVNORM(); 

                }
                break;
            case 54 :
                // BasicLexer.g:1:273: ID
                {
                mID(); 

                }
                break;

        }

    }


    protected DFA16 dfa16 = new DFA16(this);
    protected DFA18 dfa18 = new DFA18(this);
    static final String DFA16_eotS =
        "\12\uffff";
    static final String DFA16_eofS =
        "\12\uffff";
    static final String DFA16_minS =
        "\1\74\1\141\1\142\1\163\1\164\1\162\1\141\1\143\2\uffff";
    static final String DFA16_maxS =
        "\1\74\1\141\1\142\1\163\1\164\1\162\1\141\1\153\2\uffff";
    static final String DFA16_acceptS =
        "\10\uffff\1\1\1\2";
    static final String DFA16_specialS =
        "\12\uffff}>";
    static final String[] DFA16_transitionS = {
            "\1\1",
            "\1\2",
            "\1\3",
            "\1\4",
            "\1\5",
            "\1\6",
            "\1\7",
            "\1\11\7\uffff\1\10",
            "",
            ""
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "112:1: ABSTRACT : ( '<abstrakt>' | '<abstract>' );";
        }
    }
    static final String DFA18_eotS =
        "\12\uffff\1\56\7\uffff\1\63\1\65\4\uffff\1\67\2\uffff\21\54\14"+
        "\uffff\1\54\1\122\1\125\10\54\1\140\14\54\1\uffff\1\54\1\uffff\2"+
        "\54\1\uffff\1\54\1\164\1\54\1\166\2\54\1\166\1\54\1\173\1\54\1\uffff"+
        "\1\54\1\176\6\54\1\u0085\3\54\2\uffff\1\122\2\54\1\u008b\2\uffff"+
        "\1\u008b\1\uffff\3\54\1\173\1\uffff\1\140\1\54\1\uffff\1\54\1\u0091"+
        "\4\54\1\uffff\1\54\1\u0097\2\54\1\u009a\1\uffff\3\54\1\176\1\54"+
        "\1\uffff\2\54\1\u00a1\2\54\1\uffff\1\u00a4\1\54\1\uffff\2\54\1\u00a8"+
        "\1\54\1\u00a8\1\54\1\uffff\1\u00ab\1\u00ac\1\uffff\1\u00ab\1\54"+
        "\1\u00ae\1\uffff\1\54\1\u00a1\2\uffff\1\54\1\uffff\1\u0091\1\u00ae";
    static final String DFA18_eofS =
        "\u00b1\uffff";
    static final String DFA18_minS =
        "\1\11\11\uffff\1\43\7\uffff\2\75\4\uffff\1\57\2\uffff\1\105\1\106"+
        "\1\101\1\110\1\116\1\114\1\104\1\111\1\105\1\111\1\130\1\105\1\116"+
        "\1\105\1\111\1\122\1\125\4\uffff\1\156\7\uffff\1\116\2\44\1\116"+
        "\1\126\1\105\1\102\1\104\1\123\1\114\1\105\1\44\1\103\1\124\1\122"+
        "\1\104\1\103\1\113\1\117\1\106\1\124\1\116\1\105\1\132\1\143\1\116"+
        "\1\uffff\1\124\1\105\1\uffff\1\116\1\44\1\116\1\44\1\105\1\116\1"+
        "\44\1\123\1\44\1\122\1\uffff\1\110\1\44\1\102\2\105\1\101\1\127"+
        "\1\117\1\44\1\115\1\106\1\132\2\uffff\1\44\1\101\1\122\1\44\2\uffff"+
        "\1\44\1\uffff\1\113\1\117\1\105\1\44\1\uffff\1\44\1\124\1\uffff"+
        "\1\105\1\44\1\120\2\116\1\122\1\uffff\1\101\1\44\1\131\1\116\1\44"+
        "\1\uffff\1\101\1\127\1\122\1\44\1\122\1\uffff\1\124\1\116\1\44\1"+
        "\124\1\130\1\uffff\1\44\1\124\1\uffff\2\116\1\44\1\107\1\44\1\124"+
        "\1\uffff\2\44\1\uffff\1\44\1\116\1\44\1\uffff\1\105\1\44\2\uffff"+
        "\1\124\1\uffff\2\44";
    static final String DFA18_maxS =
        "\1\uefff\11\uffff\1\43\7\uffff\1\151\1\75\4\uffff\1\57\2\uffff"+
        "\1\105\1\116\1\111\1\110\1\116\1\125\1\122\1\117\1\105\1\111\1\130"+
        "\1\105\1\116\1\117\1\111\1\122\1\125\4\uffff\1\156\7\uffff\1\116"+
        "\2\uefff\1\116\1\126\1\105\1\113\1\104\1\123\1\114\1\105\1\uefff"+
        "\1\103\1\124\1\122\1\104\1\103\1\113\1\117\1\106\1\124\1\116\1\105"+
        "\1\132\1\151\1\116\1\uffff\1\124\1\105\1\uffff\1\116\1\uefff\1\116"+
        "\1\uefff\1\105\1\116\1\uefff\1\123\1\uefff\1\122\1\uffff\1\110\1"+
        "\uefff\1\102\2\105\1\101\1\127\1\117\1\uefff\1\115\1\106\1\132\2"+
        "\uffff\1\uefff\1\101\1\122\1\uefff\2\uffff\1\uefff\1\uffff\1\113"+
        "\1\117\1\105\1\uefff\1\uffff\1\uefff\1\124\1\uffff\1\105\1\uefff"+
        "\1\120\2\116\1\122\1\uffff\1\101\1\uefff\1\131\1\116\1\uefff\1\uffff"+
        "\1\101\1\127\1\122\1\uefff\1\122\1\uffff\1\124\1\116\1\uefff\1\124"+
        "\1\130\1\uffff\1\uefff\1\124\1\uffff\2\116\1\uefff\1\107\1\uefff"+
        "\1\124\1\uffff\2\uefff\1\uffff\1\uefff\1\116\1\uefff\1\uffff\1\105"+
        "\1\uefff\2\uffff\1\124\1\uffff\2\uefff";
    static final String DFA18_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\uffff\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\2\uffff\1\26\1\27\1\30\1\31\1\uffff\1"+
        "\33\1\35\21\uffff\1\66\1\54\1\12\1\22\1\uffff\1\56\1\60\1\23\1\24"+
        "\1\25\1\34\1\32\32\uffff\1\36\2\uffff\1\51\12\uffff\1\41\14\uffff"+
        "\1\55\1\57\4\uffff\1\65\1\64\1\uffff\1\40\4\uffff\1\53\2\uffff\1"+
        "\42\6\uffff\1\61\5\uffff\1\37\5\uffff\1\43\5\uffff\1\62\2\uffff"+
        "\1\52\6\uffff\1\46\2\uffff\1\63\3\uffff\1\44\2\uffff\1\47\1\50\1"+
        "\uffff\1\45\2\uffff";
    static final String DFA18_specialS =
        "\u00b1\uffff}>";
    static final String[] DFA18_transitionS = {
            "\1\31\1\32\2\uffff\1\32\22\uffff\1\31\1\7\1\1\1\12\2\54\1\52"+
            "\1\54\1\14\1\15\1\27\1\25\1\5\1\26\1\3\1\30\12\2\1\4\1\6\1\22"+
            "\1\24\1\23\1\54\1\10\1\40\1\46\1\54\1\35\1\45\1\53\1\54\1\44"+
            "\1\34\1\54\1\47\1\54\1\51\1\42\1\41\3\54\1\50\1\36\1\37\1\43"+
            "\1\33\3\54\1\20\1\uffff\1\21\1\uffff\1\54\1\uffff\32\54\1\16"+
            "\1\11\1\17\1\13\42\uffff\uef5f\54",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\55",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\57\43\uffff\1\62\2\uffff\1\61\4\uffff\1\60",
            "\1\64",
            "",
            "",
            "",
            "",
            "\1\66",
            "",
            "",
            "\1\70",
            "\1\71\7\uffff\1\72",
            "\1\73\7\uffff\1\74",
            "\1\75",
            "\1\76",
            "\1\101\1\uffff\1\77\6\uffff\1\100",
            "\1\102\15\uffff\1\103",
            "\1\104\5\uffff\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\111",
            "\1\112",
            "\1\114\11\uffff\1\113",
            "\1\115",
            "\1\116",
            "\1\117",
            "",
            "",
            "",
            "",
            "\1\120",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\121",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\4\54\27\uffff\1\54\1\uffff\22\54\1\123\1\124\6\54\4\uffff"+
            "\1\54\1\uffff\32\54\46\uffff\uef5f\54",
            "\1\126",
            "\1\127",
            "\1\130",
            "\1\132\1\uffff\1\131\6\uffff\1\133",
            "\1\134",
            "\1\135",
            "\1\136",
            "\1\137",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\141",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\145",
            "\1\146",
            "\1\147",
            "\1\150",
            "\1\151",
            "\1\152",
            "\1\153",
            "\1\154",
            "\1\155\5\uffff\1\156",
            "\1\157",
            "",
            "\1\160",
            "\1\161",
            "",
            "\1\162",
            "\4\54\5\uffff\1\163\21\uffff\1\54\1\uffff\32\54\4\uffff\1"+
            "\54\1\uffff\32\54\46\uffff\uef5f\54",
            "\1\165",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\167",
            "\1\170",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\171",
            "\4\54\27\uffff\1\54\1\uffff\4\54\1\172\25\54\4\uffff\1\54"+
            "\1\uffff\32\54\46\uffff\uef5f\54",
            "\1\174",
            "",
            "\1\175",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\177",
            "\1\u0080",
            "\1\u0081",
            "\1\u0082",
            "\1\u0083",
            "\1\u0084",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u0086",
            "\1\u0087",
            "\1\u0088",
            "",
            "",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u0089",
            "\1\u008a",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "",
            "",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "",
            "\1\u008c",
            "\1\u008d",
            "\1\u008e",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u008f",
            "",
            "\1\u0090",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "",
            "\1\u0096",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u0098",
            "\1\u0099",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "",
            "\1\u009b",
            "\1\u009c",
            "\1\u009d",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u009e",
            "",
            "\1\u009f",
            "\1\u00a0",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u00a2",
            "\1\u00a3",
            "",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u00a5",
            "",
            "\1\u00a6",
            "\1\u00a7",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u00a9",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u00aa",
            "",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\1\u00ad",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "",
            "\1\u00af",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "",
            "",
            "\1\u00b0",
            "",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54",
            "\4\54\27\uffff\1\54\1\uffff\32\54\4\uffff\1\54\1\uffff\32"+
            "\54\46\uffff\uef5f\54"
    };

    static final short[] DFA18_eot = DFA.unpackEncodedString(DFA18_eotS);
    static final short[] DFA18_eof = DFA.unpackEncodedString(DFA18_eofS);
    static final char[] DFA18_min = DFA.unpackEncodedStringToUnsignedChars(DFA18_minS);
    static final char[] DFA18_max = DFA.unpackEncodedStringToUnsignedChars(DFA18_maxS);
    static final short[] DFA18_accept = DFA.unpackEncodedString(DFA18_acceptS);
    static final short[] DFA18_special = DFA.unpackEncodedString(DFA18_specialS);
    static final short[][] DFA18_transition;

    static {
        int numStates = DFA18_transitionS.length;
        DFA18_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA18_transition[i] = DFA.unpackEncodedString(DFA18_transitionS[i]);
        }
    }

    class DFA18 extends DFA {

        public DFA18(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 18;
            this.eot = DFA18_eot;
            this.eof = DFA18_eof;
            this.min = DFA18_min;
            this.max = DFA18_max;
            this.accept = DFA18_accept;
            this.special = DFA18_special;
            this.transition = DFA18_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( String | INT | DOT | DD | COMMA | SEMI | EX | AT | ORS | NS | TILDE | LP | RP | CBO | CBC | SBO | SBC | LE | L | GE | G | EQ | PLUS | MINUS | PROD | DIV | WS | COMMENT | NL | IF | THEN | AND | OR | NOT | HIDE | EXCEPT | UNKNOWN | KNOWN | INSTANT | MINMAX | IN | INTER | ALL | ALLOWEDNAMES | INCLUDE | DEFAULT | INIT | ABSTRACT | SET | REF | FUZZY | DIVTEXT | DIVNORM | ID );";
        }
    }
 

}