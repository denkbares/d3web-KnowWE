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

// $ANTLR 3.1.1 /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g 2008-10-28 15:11:09

package de.d3web.textParser.xclPatternParser;


import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

public class xclLexer extends Lexer {
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int AND=10;
    public static final int EOF=-1;
    public static final int T__19=19;
    public static final int ANY=6;
    public static final int WS=5;
    public static final int T__16=16;
    public static final int T__15=15;
    public static final int IN=12;
    public static final int T__18=18;
    public static final int NEWLINE=7;
    public static final int T__17=17;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int ALL=11;
    public static final int SL_COMMENT=8;
    public static final int OR=9;
    public static final int STRING=4;

    // delegates
    // delegators

    public xclLexer() {;} 
    public xclLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public xclLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g"; }

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:7:7: ( '[' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:7:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__13"

    // $ANTLR start "T__14"
    public final void mT__14() throws RecognitionException {
        try {
            int _type = T__14;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:8:7: ( ',' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:8:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__14"

    // $ANTLR start "T__15"
    public final void mT__15() throws RecognitionException {
        try {
            int _type = T__15;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:9:7: ( ']' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:9:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__15"

    // $ANTLR start "T__16"
    public final void mT__16() throws RecognitionException {
        try {
            int _type = T__16;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:10:7: ( '~' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:10:9: '~'
            {
            match('~'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__16"

    // $ANTLR start "T__17"
    public final void mT__17() throws RecognitionException {
        try {
            int _type = T__17;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:11:7: ( '{' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:11:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__17"

    // $ANTLR start "T__18"
    public final void mT__18() throws RecognitionException {
        try {
            int _type = T__18;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:12:7: ( '}' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:12:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__18"

    // $ANTLR start "T__19"
    public final void mT__19() throws RecognitionException {
        try {
            int _type = T__19;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:13:7: ( '--' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:13:9: '--'
            {
            match("--"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__19"

    // $ANTLR start "T__20"
    public final void mT__20() throws RecognitionException {
        try {
            int _type = T__20;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:14:7: ( '[++]' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:14:9: '[++]'
            {
            match("[++]"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__20"

    // $ANTLR start "T__21"
    public final void mT__21() throws RecognitionException {
        try {
            int _type = T__21;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:15:7: ( '[!]' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:15:9: '[!]'
            {
            match("[!]"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__21"

    // $ANTLR start "T__22"
    public final void mT__22() throws RecognitionException {
        try {
            int _type = T__22;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:16:7: ( '=' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:16:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__22"

    // $ANTLR start "T__23"
    public final void mT__23() throws RecognitionException {
        try {
            int _type = T__23;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:17:7: ( '>=' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:17:9: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__23"

    // $ANTLR start "T__24"
    public final void mT__24() throws RecognitionException {
        try {
            int _type = T__24;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:18:7: ( '>' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:18:9: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__24"

    // $ANTLR start "T__25"
    public final void mT__25() throws RecognitionException {
        try {
            int _type = T__25;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:19:7: ( '<' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:19:9: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__25"

    // $ANTLR start "T__26"
    public final void mT__26() throws RecognitionException {
        try {
            int _type = T__26;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:20:7: ( '<=' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:20:9: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__26"

    // $ANTLR start "T__27"
    public final void mT__27() throws RecognitionException {
        try {
            int _type = T__27;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:21:7: ( 'establishedThreshold' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:21:9: 'establishedThreshold'
            {
            match("establishedThreshold"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__27"

    // $ANTLR start "T__28"
    public final void mT__28() throws RecognitionException {
        try {
            int _type = T__28;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:22:7: ( 'suggestedThreshold' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:22:9: 'suggestedThreshold'
            {
            match("suggestedThreshold"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__28"

    // $ANTLR start "T__29"
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:23:7: ( 'minSupport' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:23:9: 'minSupport'
            {
            match("minSupport"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__29"

    // $ANTLR start "ALL"
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:419:9: ( 'ALL' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:419:17: 'ALL'
            {
            match("ALL"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ALL"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:421:9: ( 'IN' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:421:17: 'IN'
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

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:423:9: ( 'OR' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:423:17: 'OR'
            {
            match("OR"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:424:4: ( 'AND' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:424:5: 'AND'
            {
            match("AND"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:426:9: ( ( 'a' .. 'z' | 'A' .. 'Z' | '.' | '-' | '0' .. '9' | '+' | ':' | 'Ã¤' | 'Ã¶' | 'Ã¼' | 'Ã' | 'Ã' | 'Ã' | 'Ã' | '_' | '?' | '(' | ')' | '/' | ';' | '!' )+ )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:426:17: ( 'a' .. 'z' | 'A' .. 'Z' | '.' | '-' | '0' .. '9' | '+' | ':' | 'Ã¤' | 'Ã¶' | 'Ã¼' | 'Ã' | 'Ã' | 'Ã' | 'Ã' | '_' | '?' | '(' | ')' | '/' | ';' | '!' )+
            {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:426:17: ( 'a' .. 'z' | 'A' .. 'Z' | '.' | '-' | '0' .. '9' | '+' | ':' | 'Ã¤' | 'Ã¶' | 'Ã¼' | 'Ã' | 'Ã' | 'Ã' | 'Ã' | '_' | '?' | '(' | ')' | '/' | ';' | '!' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='!'||(LA1_0>='(' && LA1_0<=')')||LA1_0=='+'||(LA1_0>='-' && LA1_0<=';')||LA1_0=='?'||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')||LA1_0=='\u00C4'||LA1_0=='\u00D6'||LA1_0=='\u00DC'||LA1_0=='\u00DF'||LA1_0=='\u00E4'||LA1_0=='\u00F6'||LA1_0=='\u00FC') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:
            	    {
            	    if ( input.LA(1)=='!'||(input.LA(1)>='(' && input.LA(1)<=')')||input.LA(1)=='+'||(input.LA(1)>='-' && input.LA(1)<=';')||input.LA(1)=='?'||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z')||input.LA(1)=='\u00C4'||input.LA(1)=='\u00D6'||input.LA(1)=='\u00DC'||input.LA(1)=='\u00DF'||input.LA(1)=='\u00E4'||input.LA(1)=='\u00F6'||input.LA(1)=='\u00FC' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "ANY"
    public final void mANY() throws RecognitionException {
        try {
            int _type = ANY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:428:4: ( '\"' (~ '\"' )* '\"' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:428:5: '\"' (~ '\"' )* '\"'
            {
            match('\"'); 
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:428:9: (~ '\"' )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='\u0000' && LA2_0<='!')||(LA2_0>='#' && LA2_0<='\uFFFF')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:428:10: ~ '\"'
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='!')||(input.LA(1)>='#' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            match('\"'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ANY"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:431:5: ( ( ' ' | '\\t' )+ )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:431:9: ( ' ' | '\\t' )+
            {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:431:9: ( ' ' | '\\t' )+
            int cnt3=0;
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\t'||LA3_0==' ') ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:
            	    {
            	    if ( input.LA(1)=='\t'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt3 >= 1 ) break loop3;
                        EarlyExitException eee =
                            new EarlyExitException(3, input);
                        throw eee;
                }
                cnt3++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "NEWLINE"
    public final void mNEWLINE() throws RecognitionException {
        try {
            int _type = NEWLINE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:434:9: ( ( ( '\\r' )? '\\n' )+ )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:434:17: ( ( '\\r' )? '\\n' )+
            {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:434:17: ( ( '\\r' )? '\\n' )+
            int cnt5=0;
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\n'||LA5_0=='\r') ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:434:18: ( '\\r' )? '\\n'
            	    {
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:434:18: ( '\\r' )?
            	    int alt4=2;
            	    int LA4_0 = input.LA(1);

            	    if ( (LA4_0=='\r') ) {
            	        alt4=1;
            	    }
            	    switch (alt4) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:434:19: '\\r'
            	            {
            	            match('\r'); 

            	            }
            	            break;

            	    }

            	    match('\n'); 

            	    }
            	    break;

            	default :
            	    if ( cnt5 >= 1 ) break loop5;
                        EarlyExitException eee =
                            new EarlyExitException(5, input);
                        throw eee;
                }
                cnt5++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NEWLINE"

    // $ANTLR start "SL_COMMENT"
    public final void mSL_COMMENT() throws RecognitionException {
        try {
            int _type = SL_COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:11: ( '//' (~ ( '\\n' | '\\r' ) )* ( '\\n' | '\\r' ( '\\n' ) ) )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:16: '//' (~ ( '\\n' | '\\r' ) )* ( '\\n' | '\\r' ( '\\n' ) )
            {
            match("//"); 

            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:20: (~ ( '\\n' | '\\r' ) )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( ((LA6_0>='\u0000' && LA6_0<='\t')||(LA6_0>='\u000B' && LA6_0<='\f')||(LA6_0>='\u000E' && LA6_0<='\uFFFF')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:21: ~ ( '\\n' | '\\r' )
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\t')||(input.LA(1)>='\u000B' && input.LA(1)<='\f')||(input.LA(1)>='\u000E' && input.LA(1)<='\uFFFF') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:36: ( '\\n' | '\\r' ( '\\n' ) )
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='\n') ) {
                alt7=1;
            }
            else if ( (LA7_0=='\r') ) {
                alt7=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:37: '\\n'
                    {
                    match('\n'); 

                    }
                    break;
                case 2 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:42: '\\r' ( '\\n' )
                    {
                    match('\r'); 
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:46: ( '\\n' )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:436:47: '\\n'
                    {
                    match('\n'); 

                    }


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SL_COMMENT"

    public void mTokens() throws RecognitionException {
        // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:8: ( T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | ALL | IN | OR | AND | STRING | ANY | WS | NEWLINE | SL_COMMENT )
        int alt8=26;
        alt8 = dfa8.predict(input);
        switch (alt8) {
            case 1 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:10: T__13
                {
                mT__13(); 

                }
                break;
            case 2 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:16: T__14
                {
                mT__14(); 

                }
                break;
            case 3 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:22: T__15
                {
                mT__15(); 

                }
                break;
            case 4 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:28: T__16
                {
                mT__16(); 

                }
                break;
            case 5 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:34: T__17
                {
                mT__17(); 

                }
                break;
            case 6 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:40: T__18
                {
                mT__18(); 

                }
                break;
            case 7 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:46: T__19
                {
                mT__19(); 

                }
                break;
            case 8 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:52: T__20
                {
                mT__20(); 

                }
                break;
            case 9 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:58: T__21
                {
                mT__21(); 

                }
                break;
            case 10 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:64: T__22
                {
                mT__22(); 

                }
                break;
            case 11 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:70: T__23
                {
                mT__23(); 

                }
                break;
            case 12 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:76: T__24
                {
                mT__24(); 

                }
                break;
            case 13 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:82: T__25
                {
                mT__25(); 

                }
                break;
            case 14 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:88: T__26
                {
                mT__26(); 

                }
                break;
            case 15 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:94: T__27
                {
                mT__27(); 

                }
                break;
            case 16 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:100: T__28
                {
                mT__28(); 

                }
                break;
            case 17 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:106: T__29
                {
                mT__29(); 

                }
                break;
            case 18 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:112: ALL
                {
                mALL(); 

                }
                break;
            case 19 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:116: IN
                {
                mIN(); 

                }
                break;
            case 20 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:119: OR
                {
                mOR(); 

                }
                break;
            case 21 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:122: AND
                {
                mAND(); 

                }
                break;
            case 22 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:126: STRING
                {
                mSTRING(); 

                }
                break;
            case 23 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:133: ANY
                {
                mANY(); 

                }
                break;
            case 24 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:137: WS
                {
                mWS(); 

                }
                break;
            case 25 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:140: NEWLINE
                {
                mNEWLINE(); 

                }
                break;
            case 26 :
                // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:1:148: SL_COMMENT
                {
                mSL_COMMENT(); 

                }
                break;

        }

    }


    protected DFA8 dfa8 = new DFA8(this);
    static final String DFA8_eotS =
        "\1\uffff\1\30\5\uffff\1\25\1\uffff\1\33\1\35\7\25\7\uffff\1\46\4"+
        "\uffff\5\25\1\54\1\55\1\25\1\uffff\3\25\1\63\1\64\2\uffff\1\25\1"+
        "\uffff\3\25\2\uffff\21\25\1\111\2\25\1\uffff\15\25\1\131\1\25\1"+
        "\uffff\1\133\1\uffff";
    static final String DFA8_eofS =
        "\134\uffff";
    static final String DFA8_minS =
        "\1\11\1\41\5\uffff\1\55\1\uffff\2\75\1\163\1\165\1\151\1\114\1\116"+
        "\1\122\1\57\7\uffff\1\41\4\uffff\1\164\1\147\1\156\1\114\1\104\2"+
        "\41\1\0\1\uffff\1\141\1\147\1\123\2\41\2\uffff\1\0\1\uffff\1\142"+
        "\1\145\1\165\2\uffff\1\154\1\163\1\160\1\151\1\164\1\160\1\163\1"+
        "\145\1\157\1\150\1\144\1\162\1\145\1\124\1\164\1\144\1\150\1\41"+
        "\1\124\1\162\1\uffff\1\150\1\145\1\162\1\163\1\145\1\150\1\163\1"+
        "\157\1\150\1\154\1\157\1\144\1\154\1\41\1\144\1\uffff\1\41\1\uffff";
    static final String DFA8_maxS =
        "\1\u00fc\1\53\5\uffff\1\55\1\uffff\2\75\1\163\1\165\1\151\2\116"+
        "\1\122\1\57\7\uffff\1\u00fc\4\uffff\1\164\1\147\1\156\1\114\1\104"+
        "\2\u00fc\1\uffff\1\uffff\1\141\1\147\1\123\2\u00fc\2\uffff\1\uffff"+
        "\1\uffff\1\142\1\145\1\165\2\uffff\1\154\1\163\1\160\1\151\1\164"+
        "\1\160\1\163\1\145\1\157\1\150\1\144\1\162\1\145\1\124\1\164\1\144"+
        "\1\150\1\u00fc\1\124\1\162\1\uffff\1\150\1\145\1\162\1\163\1\145"+
        "\1\150\1\163\1\157\1\150\1\154\1\157\1\144\1\154\1\u00fc\1\144\1"+
        "\uffff\1\u00fc\1\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\2\1\3\1\4\1\5\1\6\1\uffff\1\12\11\uffff\1\27\1\30\1\31"+
        "\1\26\1\10\1\11\1\1\1\uffff\1\13\1\14\1\16\1\15\10\uffff\1\7\5\uffff"+
        "\1\23\1\24\1\uffff\1\32\3\uffff\1\22\1\25\24\uffff\1\21\17\uffff"+
        "\1\20\1\uffff\1\17";
    static final String DFA8_specialS =
        "\45\uffff\1\1\10\uffff\1\0\55\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\23\1\24\2\uffff\1\24\22\uffff\1\23\1\25\1\22\5\uffff\2\25"+
            "\1\uffff\1\25\1\2\1\7\1\25\1\21\14\25\1\12\1\10\1\11\1\25\1"+
            "\uffff\1\16\7\25\1\17\5\25\1\20\13\25\1\1\1\uffff\1\3\1\uffff"+
            "\1\25\1\uffff\4\25\1\13\7\25\1\15\5\25\1\14\7\25\1\5\1\uffff"+
            "\1\6\1\4\105\uffff\1\25\21\uffff\1\25\5\uffff\1\25\2\uffff\1"+
            "\25\4\uffff\1\25\21\uffff\1\25\5\uffff\1\25",
            "\1\27\11\uffff\1\26",
            "",
            "",
            "",
            "",
            "",
            "\1\31",
            "",
            "\1\32",
            "\1\34",
            "\1\36",
            "\1\37",
            "\1\40",
            "\1\41\1\uffff\1\42",
            "\1\43",
            "\1\44",
            "\1\45",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\25\6\uffff\2\25\1\uffff\1\25\1\uffff\17\25\3\uffff\1\25"+
            "\1\uffff\32\25\4\uffff\1\25\1\uffff\32\25\111\uffff\1\25\21"+
            "\uffff\1\25\5\uffff\1\25\2\uffff\1\25\4\uffff\1\25\21\uffff"+
            "\1\25\5\uffff\1\25",
            "",
            "",
            "",
            "",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\1\25\6\uffff\2\25\1\uffff\1\25\1\uffff\17\25\3\uffff\1\25"+
            "\1\uffff\32\25\4\uffff\1\25\1\uffff\32\25\111\uffff\1\25\21"+
            "\uffff\1\25\5\uffff\1\25\2\uffff\1\25\4\uffff\1\25\21\uffff"+
            "\1\25\5\uffff\1\25",
            "\1\25\6\uffff\2\25\1\uffff\1\25\1\uffff\17\25\3\uffff\1\25"+
            "\1\uffff\32\25\4\uffff\1\25\1\uffff\32\25\111\uffff\1\25\21"+
            "\uffff\1\25\5\uffff\1\25\2\uffff\1\25\4\uffff\1\25\21\uffff"+
            "\1\25\5\uffff\1\25",
            "\41\57\1\56\6\57\2\56\1\57\1\56\1\57\17\56\3\57\1\56\1\57\32"+
            "\56\4\57\1\56\1\57\32\56\111\57\1\56\21\57\1\56\5\57\1\56\2"+
            "\57\1\56\4\57\1\56\21\57\1\56\5\57\1\56\uff03\57",
            "",
            "\1\60",
            "\1\61",
            "\1\62",
            "\1\25\6\uffff\2\25\1\uffff\1\25\1\uffff\17\25\3\uffff\1\25"+
            "\1\uffff\32\25\4\uffff\1\25\1\uffff\32\25\111\uffff\1\25\21"+
            "\uffff\1\25\5\uffff\1\25\2\uffff\1\25\4\uffff\1\25\21\uffff"+
            "\1\25\5\uffff\1\25",
            "\1\25\6\uffff\2\25\1\uffff\1\25\1\uffff\17\25\3\uffff\1\25"+
            "\1\uffff\32\25\4\uffff\1\25\1\uffff\32\25\111\uffff\1\25\21"+
            "\uffff\1\25\5\uffff\1\25\2\uffff\1\25\4\uffff\1\25\21\uffff"+
            "\1\25\5\uffff\1\25",
            "",
            "",
            "\41\57\1\56\6\57\2\56\1\57\1\56\1\57\17\56\3\57\1\56\1\57\32"+
            "\56\4\57\1\56\1\57\32\56\111\57\1\56\21\57\1\56\5\57\1\56\2"+
            "\57\1\56\4\57\1\56\21\57\1\56\5\57\1\56\uff03\57",
            "",
            "\1\65",
            "\1\66",
            "\1\67",
            "",
            "",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\73",
            "\1\74",
            "\1\75",
            "\1\76",
            "\1\77",
            "\1\100",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\105",
            "\1\106",
            "\1\107",
            "\1\110",
            "\1\25\6\uffff\2\25\1\uffff\1\25\1\uffff\17\25\3\uffff\1\25"+
            "\1\uffff\32\25\4\uffff\1\25\1\uffff\32\25\111\uffff\1\25\21"+
            "\uffff\1\25\5\uffff\1\25\2\uffff\1\25\4\uffff\1\25\21\uffff"+
            "\1\25\5\uffff\1\25",
            "\1\112",
            "\1\113",
            "",
            "\1\114",
            "\1\115",
            "\1\116",
            "\1\117",
            "\1\120",
            "\1\121",
            "\1\122",
            "\1\123",
            "\1\124",
            "\1\125",
            "\1\126",
            "\1\127",
            "\1\130",
            "\1\25\6\uffff\2\25\1\uffff\1\25\1\uffff\17\25\3\uffff\1\25"+
            "\1\uffff\32\25\4\uffff\1\25\1\uffff\32\25\111\uffff\1\25\21"+
            "\uffff\1\25\5\uffff\1\25\2\uffff\1\25\4\uffff\1\25\21\uffff"+
            "\1\25\5\uffff\1\25",
            "\1\132",
            "",
            "\1\25\6\uffff\2\25\1\uffff\1\25\1\uffff\17\25\3\uffff\1\25"+
            "\1\uffff\32\25\4\uffff\1\25\1\uffff\32\25\111\uffff\1\25\21"+
            "\uffff\1\25\5\uffff\1\25\2\uffff\1\25\4\uffff\1\25\21\uffff"+
            "\1\25\5\uffff\1\25",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__13 | T__14 | T__15 | T__16 | T__17 | T__18 | T__19 | T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | ALL | IN | OR | AND | STRING | ANY | WS | NEWLINE | SL_COMMENT );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA8_46 = input.LA(1);

                        s = -1;
                        if ( (LA8_46=='!'||(LA8_46>='(' && LA8_46<=')')||LA8_46=='+'||(LA8_46>='-' && LA8_46<=';')||LA8_46=='?'||(LA8_46>='A' && LA8_46<='Z')||LA8_46=='_'||(LA8_46>='a' && LA8_46<='z')||LA8_46=='\u00C4'||LA8_46=='\u00D6'||LA8_46=='\u00DC'||LA8_46=='\u00DF'||LA8_46=='\u00E4'||LA8_46=='\u00F6'||LA8_46=='\u00FC') ) {s = 46;}

                        else if ( ((LA8_46>='\u0000' && LA8_46<=' ')||(LA8_46>='\"' && LA8_46<='\'')||LA8_46=='*'||LA8_46==','||(LA8_46>='<' && LA8_46<='>')||LA8_46=='@'||(LA8_46>='[' && LA8_46<='^')||LA8_46=='`'||(LA8_46>='{' && LA8_46<='\u00C3')||(LA8_46>='\u00C5' && LA8_46<='\u00D5')||(LA8_46>='\u00D7' && LA8_46<='\u00DB')||(LA8_46>='\u00DD' && LA8_46<='\u00DE')||(LA8_46>='\u00E0' && LA8_46<='\u00E3')||(LA8_46>='\u00E5' && LA8_46<='\u00F5')||(LA8_46>='\u00F7' && LA8_46<='\u00FB')||(LA8_46>='\u00FD' && LA8_46<='\uFFFF')) ) {s = 47;}

                        else s = 21;

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA8_37 = input.LA(1);

                        s = -1;
                        if ( (LA8_37=='!'||(LA8_37>='(' && LA8_37<=')')||LA8_37=='+'||(LA8_37>='-' && LA8_37<=';')||LA8_37=='?'||(LA8_37>='A' && LA8_37<='Z')||LA8_37=='_'||(LA8_37>='a' && LA8_37<='z')||LA8_37=='\u00C4'||LA8_37=='\u00D6'||LA8_37=='\u00DC'||LA8_37=='\u00DF'||LA8_37=='\u00E4'||LA8_37=='\u00F6'||LA8_37=='\u00FC') ) {s = 46;}

                        else if ( ((LA8_37>='\u0000' && LA8_37<=' ')||(LA8_37>='\"' && LA8_37<='\'')||LA8_37=='*'||LA8_37==','||(LA8_37>='<' && LA8_37<='>')||LA8_37=='@'||(LA8_37>='[' && LA8_37<='^')||LA8_37=='`'||(LA8_37>='{' && LA8_37<='\u00C3')||(LA8_37>='\u00C5' && LA8_37<='\u00D5')||(LA8_37>='\u00D7' && LA8_37<='\u00DB')||(LA8_37>='\u00DD' && LA8_37<='\u00DE')||(LA8_37>='\u00E0' && LA8_37<='\u00E3')||(LA8_37>='\u00E5' && LA8_37<='\u00F5')||(LA8_37>='\u00F7' && LA8_37<='\u00FB')||(LA8_37>='\u00FD' && LA8_37<='\uFFFF')) ) {s = 47;}

                        else s = 21;

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 8, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

}