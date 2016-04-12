// $ANTLR 3.1.1 D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g 2010-08-18 21:19:36

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
 * Lexer fuer Testsuite-Section, stellt spezielle Testsuite-Tokens bereit
 * @author Sebastian Furth
 *
 */
public class TestsuiteLexer extends Lexer {
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


    // delegates
    public TestsuiteLexer_BasicLexer gBasicLexer;
    // delegators

    public TestsuiteLexer() {;} 
    public TestsuiteLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public TestsuiteLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
        gBasicLexer = new TestsuiteLexer_BasicLexer(input, state, this);
    }
    public String getGrammarFileName() { return "D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g"; }

    // $ANTLR start "HEURISTIC"
    public final void mHEURISTIC() throws RecognitionException {
        try {
            int _type = HEURISTIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:85:11: ( 'heuristic' | 'HEURISTIC' | 'Heuristic' )
            int alt1=3;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='h') ) {
                alt1=1;
            }
            else if ( (LA1_0=='H') ) {
                int LA1_2 = input.LA(2);

                if ( (LA1_2=='E') ) {
                    alt1=2;
                }
                else if ( (LA1_2=='e') ) {
                    alt1=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 1, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 1, 0, input);

                throw nvae;
            }
            switch (alt1) {
                case 1 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:85:13: 'heuristic'
                    {
                    match("heuristic"); 


                    }
                    break;
                case 2 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:85:25: 'HEURISTIC'
                    {
                    match("HEURISTIC"); 


                    }
                    break;
                case 3 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:85:37: 'Heuristic'
                    {
                    match("Heuristic"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HEURISTIC"

    // $ANTLR start "XCL"
    public final void mXCL() throws RecognitionException {
        try {
            int _type = XCL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:86:5: ( 'xcl' | 'XCL' | 'Xcl' )
            int alt2=3;
            int LA2_0 = input.LA(1);

            if ( (LA2_0=='x') ) {
                alt2=1;
            }
            else if ( (LA2_0=='X') ) {
                int LA2_2 = input.LA(2);

                if ( (LA2_2=='C') ) {
                    alt2=2;
                }
                else if ( (LA2_2=='c') ) {
                    alt2=3;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 2, 2, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:86:7: 'xcl'
                    {
                    match("xcl"); 


                    }
                    break;
                case 2 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:86:13: 'XCL'
                    {
                    match("XCL"); 


                    }
                    break;
                case 3 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:86:19: 'Xcl'
                    {
                    match("Xcl"); 


                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "XCL"

    public void mTokens() throws RecognitionException {
        // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:1:8: ( HEURISTIC | XCL | BasicLexer. Tokens )
        int alt3=3;
        alt3 = dfa3.predict(input);
        switch (alt3) {
            case 1 :
                // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:1:10: HEURISTIC
                {
                mHEURISTIC(); 

                }
                break;
            case 2 :
                // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:1:20: XCL
                {
                mXCL(); 

                }
                break;
            case 3 :
                // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteLexer.g:1:24: BasicLexer. Tokens
                {
                gBasicLexer.mTokens(); 

                }
                break;

        }

    }


    protected DFA3 dfa3 = new DFA3(this);
    static final String DFA3_eotS =
        "\1\uffff\4\5\1\uffff\11\5\3\25\3\5\1\uffff\14\5\3\45\1\uffff";
    static final String DFA3_eofS =
        "\46\uffff";
    static final String DFA3_minS =
        "\1\11\1\145\1\105\1\143\1\103\1\uffff\1\165\1\125\1\165\1\154\1"+
        "\114\1\154\1\162\1\122\1\162\3\44\1\151\1\111\1\151\1\uffff\1\163"+
        "\1\123\1\163\1\164\1\124\1\164\1\151\1\111\1\151\1\143\1\103\1\143"+
        "\3\44\1\uffff";
    static final String DFA3_maxS =
        "\1\uefff\2\145\2\143\1\uffff\1\165\1\125\1\165\1\154\1\114\1\154"+
        "\1\162\1\122\1\162\3\uefff\1\151\1\111\1\151\1\uffff\1\163\1\123"+
        "\1\163\1\164\1\124\1\164\1\151\1\111\1\151\1\143\1\103\1\143\3\uefff"+
        "\1\uffff";
    static final String DFA3_acceptS =
        "\5\uffff\1\3\17\uffff\1\2\17\uffff\1\1";
    static final String DFA3_specialS =
        "\46\uffff}>";
    static final String[] DFA3_transitionS = {
            "\2\5\2\uffff\1\5\22\uffff\50\5\1\2\17\5\1\4\3\5\1\uffff\1\5"+
            "\1\uffff\1\5\1\uffff\7\5\1\1\17\5\1\3\6\5\42\uffff\uef5f\5",
            "\1\6",
            "\1\7\37\uffff\1\10",
            "\1\11",
            "\1\12\37\uffff\1\13",
            "",
            "\1\14",
            "\1\15",
            "\1\16",
            "\1\17",
            "\1\20",
            "\1\21",
            "\1\22",
            "\1\23",
            "\1\24",
            "\4\5\27\uffff\1\5\1\uffff\32\5\4\uffff\1\5\1\uffff\32\5\46"+
            "\uffff\uef5f\5",
            "\4\5\27\uffff\1\5\1\uffff\32\5\4\uffff\1\5\1\uffff\32\5\46"+
            "\uffff\uef5f\5",
            "\4\5\27\uffff\1\5\1\uffff\32\5\4\uffff\1\5\1\uffff\32\5\46"+
            "\uffff\uef5f\5",
            "\1\26",
            "\1\27",
            "\1\30",
            "",
            "\1\31",
            "\1\32",
            "\1\33",
            "\1\34",
            "\1\35",
            "\1\36",
            "\1\37",
            "\1\40",
            "\1\41",
            "\1\42",
            "\1\43",
            "\1\44",
            "\4\5\27\uffff\1\5\1\uffff\32\5\4\uffff\1\5\1\uffff\32\5\46"+
            "\uffff\uef5f\5",
            "\4\5\27\uffff\1\5\1\uffff\32\5\4\uffff\1\5\1\uffff\32\5\46"+
            "\uffff\uef5f\5",
            "\4\5\27\uffff\1\5\1\uffff\32\5\4\uffff\1\5\1\uffff\32\5\46"+
            "\uffff\uef5f\5",
            ""
    };

    static final short[] DFA3_eot = DFA.unpackEncodedString(DFA3_eotS);
    static final short[] DFA3_eof = DFA.unpackEncodedString(DFA3_eofS);
    static final char[] DFA3_min = DFA.unpackEncodedStringToUnsignedChars(DFA3_minS);
    static final char[] DFA3_max = DFA.unpackEncodedStringToUnsignedChars(DFA3_maxS);
    static final short[] DFA3_accept = DFA.unpackEncodedString(DFA3_acceptS);
    static final short[] DFA3_special = DFA.unpackEncodedString(DFA3_specialS);
    static final short[][] DFA3_transition;

    static {
        int numStates = DFA3_transitionS.length;
        DFA3_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA3_transition[i] = DFA.unpackEncodedString(DFA3_transitionS[i]);
        }
    }

    class DFA3 extends DFA {

        public DFA3(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 3;
            this.eot = DFA3_eot;
            this.eof = DFA3_eof;
            this.min = DFA3_min;
            this.max = DFA3_max;
            this.accept = DFA3_accept;
            this.special = DFA3_special;
            this.transition = DFA3_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( HEURISTIC | XCL | BasicLexer. Tokens );";
        }
    }
 

}