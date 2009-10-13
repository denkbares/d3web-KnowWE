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

// $ANTLR 3.1 D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g 2009-01-10 19:06:19

package de.d3web.KnOfficeParser.visio;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;

import de.d3web.KnOfficeParser.LexerErrorHandler;

public class VisioLexer extends Lexer {
    public static final int Aidtext=67;
    public static final int LP=15;
    public static final int Textboxtext=65;
    public static final int NOT=37;
    public static final int Page=53;
    public static final int EXCEPT=39;
    public static final int EOF=-1;
    public static final int DD=7;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int T__90=90;
    public static final int EX=10;
    public static final int INCLUDE=47;
    public static final int NL=32;
    public static final int EQ=25;
    public static final int COMMENT=31;
    public static final int Shapestart=73;
    public static final int GE=23;
    public static final int G=24;
    public static final int T__80=80;
    public static final int SBC=20;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int L=22;
    public static final int NS=13;
    public static final int KNOWN=41;
    public static final int INT=5;
    public static final int T__85=85;
    public static final int T__84=84;
    public static final int T__87=87;
    public static final int T__86=86;
    public static final int T__89=89;
    public static final int T__88=88;
    public static final int Picture=62;
    public static final int Xcoord=55;
    public static final int WS=30;
    public static final int OR=36;
    public static final int SBO=19;
    public static final int Misc2=77;
    public static final int Misc3=78;
    public static final int Popup=66;
    public static final int YtoWith=74;
    public static final int T__79=79;
    public static final int End=70;
    public static final int Ycoord=56;
    public static final int Shapetext=61;
    public static final int HIDE=38;
    public static final int RP=16;
    public static final int ORS=12;
    public static final int MyDouble=60;
    public static final int ABSTRACT=49;
    public static final int AND=35;
    public static final int ID=52;
    public static final int Width=57;
    public static final int IF=33;
    public static final int AT=11;
    public static final int THEN=34;
    public static final int IN=44;
    public static final int UNKNOWN=40;
    public static final int COMMA=8;
    public static final int Height=58;
    public static final int ALL=45;
    public static final int PROD=28;
    public static final int Knowledge=68;
    public static final int TILDE=14;
    public static final int PLUS=26;
    public static final int String=4;
    public static final int DOT=6;
    public static final int HeighttoText=75;
    public static final int Start=69;
    public static final int Pagesheet=72;
    public static final int ALLOWEDNAMES=46;
    public static final int Misc=76;
    public static final int INSTANT=42;
    public static final int MINMAX=43;
    public static final int DEFAULT=48;
    public static final int SET=50;
    public static final int MINUS=27;
    public static final int Tokens=93;
    public static final int SEMI=9;
    public static final int REF=51;
    public static final int QID=64;
    public static final int Box=63;
    public static final int CBC=18;
    public static final int Shape=54;
    public static final int Text=59;
    public static final int DIV=29;
    public static final int CBO=17;
    public static final int LE=21;
    public static final int Pagestart=71;

          private LexerErrorHandler eh;
          
         public VisioLexer(ANTLRInputStream input, LexerErrorHandler eh) {
            this(input);
            this.eh=eh;
            gBasicLexer.setLexerErrorHandler(eh);
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
    public Visio_BasicLexer gBasicLexer;
    // delegators

    public VisioLexer() {;} 
    public VisioLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public VisioLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
        gBasicLexer = new Visio_BasicLexer(input, state, this);
    }
    public String getGrammarFileName() { return "D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g"; }

    // $ANTLR start "T__79"
    public final void mT__79() throws RecognitionException {
        try {
            int _type = T__79;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:36:7: ( '</Pages>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:36:9: '</Pages>'
            {
            match("</Pages>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__79"

    // $ANTLR start "T__80"
    public final void mT__80() throws RecognitionException {
        try {
            int _type = T__80;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:37:7: ( '</Page>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:37:9: '</Page>'
            {
            match("</Page>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__80"

    // $ANTLR start "T__81"
    public final void mT__81() throws RecognitionException {
        try {
            int _type = T__81;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:38:7: ( '<Shapes>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:38:9: '<Shapes>'
            {
            match("<Shapes>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__81"

    // $ANTLR start "T__82"
    public final void mT__82() throws RecognitionException {
        try {
            int _type = T__82;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:39:7: ( '</Shapes>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:39:9: '</Shapes>'
            {
            match("</Shapes>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__82"

    // $ANTLR start "T__83"
    public final void mT__83() throws RecognitionException {
        try {
            int _type = T__83;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:40:7: ( '</PinX>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:40:9: '</PinX>'
            {
            match("</PinX>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__83"

    // $ANTLR start "T__84"
    public final void mT__84() throws RecognitionException {
        try {
            int _type = T__84;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:41:7: ( '<PinY>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:41:9: '<PinY>'
            {
            match("<PinY>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__84"

    // $ANTLR start "T__85"
    public final void mT__85() throws RecognitionException {
        try {
            int _type = T__85;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:42:7: ( '</Width>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:42:9: '</Width>'
            {
            match("</Width>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__85"

    // $ANTLR start "T__86"
    public final void mT__86() throws RecognitionException {
        try {
            int _type = T__86;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:43:7: ( '<Height>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:43:9: '<Height>'
            {
            match("<Height>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__86"

    // $ANTLR start "T__87"
    public final void mT__87() throws RecognitionException {
        try {
            int _type = T__87;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:44:7: ( '</Text></Shape>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:44:9: '</Text></Shape>'
            {
            match("</Text></Shape>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__87"

    // $ANTLR start "T__88"
    public final void mT__88() throws RecognitionException {
        try {
            int _type = T__88;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:45:7: ( 'Bildname:' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:45:9: 'Bildname:'
            {
            match("Bildname:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__88"

    // $ANTLR start "T__89"
    public final void mT__89() throws RecognitionException {
        try {
            int _type = T__89;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:46:7: ( 'Größe:' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:46:9: 'Größe:'
            {
            match("Größe:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__89"

    // $ANTLR start "T__90"
    public final void mT__90() throws RecognitionException {
        try {
            int _type = T__90;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:47:7: ( 'x' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:47:9: 'x'
            {
            match('x'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__90"

    // $ANTLR start "T__91"
    public final void mT__91() throws RecognitionException {
        try {
            int _type = T__91;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:48:7: ( 'Frage:' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:48:9: 'Frage:'
            {
            match("Frage:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__91"

    // $ANTLR start "T__92"
    public final void mT__92() throws RecognitionException {
        try {
            int _type = T__92;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:49:7: ( 'Folgefragen:' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:49:9: 'Folgefragen:'
            {
            match("Folgefragen:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__92"

    // $ANTLR start "Misc"
    public final void mMisc() throws RecognitionException {
        try {
            int _type = Misc;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:100:6: ( '<cp IX=\\'' ( options {greedy=false; } : . )* '\\'/>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:100:8: '<cp IX=\\'' ( options {greedy=false; } : . )* '\\'/>'
            {
            match("<cp IX=\'"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:100:20: ( options {greedy=false; } : . )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0=='\'') ) {
                    int LA1_1 = input.LA(2);

                    if ( (LA1_1=='/') ) {
                        int LA1_3 = input.LA(3);

                        if ( (LA1_3=='>') ) {
                            alt1=2;
                        }
                        else if ( ((LA1_3>='\u0000' && LA1_3<='=')||(LA1_3>='?' && LA1_3<='\uFFFE')) ) {
                            alt1=1;
                        }


                    }
                    else if ( ((LA1_1>='\u0000' && LA1_1<='.')||(LA1_1>='0' && LA1_1<='\uFFFE')) ) {
                        alt1=1;
                    }


                }
                else if ( ((LA1_0>='\u0000' && LA1_0<='&')||(LA1_0>='(' && LA1_0<='\uFFFE')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:100:47: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            match("\'/>"); 

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Misc"

    // $ANTLR start "Misc2"
    public final void mMisc2() throws RecognitionException {
        try {
            int _type = Misc2;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:101:7: ( '<tp IX=\\'' ( options {greedy=false; } : . )* '\\'/>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:101:9: '<tp IX=\\'' ( options {greedy=false; } : . )* '\\'/>'
            {
            match("<tp IX=\'"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:101:21: ( options {greedy=false; } : . )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0=='\'') ) {
                    int LA2_1 = input.LA(2);

                    if ( (LA2_1=='/') ) {
                        int LA2_3 = input.LA(3);

                        if ( (LA2_3=='>') ) {
                            alt2=2;
                        }
                        else if ( ((LA2_3>='\u0000' && LA2_3<='=')||(LA2_3>='?' && LA2_3<='\uFFFE')) ) {
                            alt2=1;
                        }


                    }
                    else if ( ((LA2_1>='\u0000' && LA2_1<='.')||(LA2_1>='0' && LA2_1<='\uFFFE')) ) {
                        alt2=1;
                    }


                }
                else if ( ((LA2_0>='\u0000' && LA2_0<='&')||(LA2_0>='(' && LA2_0<='\uFFFE')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:101:48: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            match("\'/>"); 

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Misc2"

    // $ANTLR start "Misc3"
    public final void mMisc3() throws RecognitionException {
        try {
            int _type = Misc3;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:102:6: ( '<pp IX=\\'' ( options {greedy=false; } : . )* '\\'/>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:102:8: '<pp IX=\\'' ( options {greedy=false; } : . )* '\\'/>'
            {
            match("<pp IX=\'"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:102:20: ( options {greedy=false; } : . )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0=='\'') ) {
                    int LA3_1 = input.LA(2);

                    if ( (LA3_1=='/') ) {
                        int LA3_3 = input.LA(3);

                        if ( (LA3_3=='>') ) {
                            alt3=2;
                        }
                        else if ( ((LA3_3>='\u0000' && LA3_3<='=')||(LA3_3>='?' && LA3_3<='\uFFFE')) ) {
                            alt3=1;
                        }


                    }
                    else if ( ((LA3_1>='\u0000' && LA3_1<='.')||(LA3_1>='0' && LA3_1<='\uFFFE')) ) {
                        alt3=1;
                    }


                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='&')||(LA3_0>='(' && LA3_0<='\uFFFE')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:102:47: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match("\'/>"); 

            _channel=HIDDEN;

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Misc3"

    // $ANTLR start "Start"
    public final void mStart() throws RecognitionException {
        try {
            int _type = Start;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:104:7: ( '<?xml version=\\'1.0\\' encoding=\\'utf-8\\' ?>' ( options {greedy=false; } : . )* '<Pages>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:104:9: '<?xml version=\\'1.0\\' encoding=\\'utf-8\\' ?>' ( options {greedy=false; } : . )* '<Pages>'
            {
            match("<?xml version=\'1.0\' encoding=\'utf-8\' ?>"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:104:55: ( options {greedy=false; } : . )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0=='<') ) {
                    int LA4_1 = input.LA(2);

                    if ( (LA4_1=='P') ) {
                        int LA4_3 = input.LA(3);

                        if ( (LA4_3=='a') ) {
                            int LA4_4 = input.LA(4);

                            if ( (LA4_4=='g') ) {
                                int LA4_5 = input.LA(5);

                                if ( (LA4_5=='e') ) {
                                    int LA4_6 = input.LA(6);

                                    if ( (LA4_6=='s') ) {
                                        int LA4_7 = input.LA(7);

                                        if ( (LA4_7=='>') ) {
                                            alt4=2;
                                        }
                                        else if ( ((LA4_7>='\u0000' && LA4_7<='=')||(LA4_7>='?' && LA4_7<='\uFFFE')) ) {
                                            alt4=1;
                                        }


                                    }
                                    else if ( ((LA4_6>='\u0000' && LA4_6<='r')||(LA4_6>='t' && LA4_6<='\uFFFE')) ) {
                                        alt4=1;
                                    }


                                }
                                else if ( ((LA4_5>='\u0000' && LA4_5<='d')||(LA4_5>='f' && LA4_5<='\uFFFE')) ) {
                                    alt4=1;
                                }


                            }
                            else if ( ((LA4_4>='\u0000' && LA4_4<='f')||(LA4_4>='h' && LA4_4<='\uFFFE')) ) {
                                alt4=1;
                            }


                        }
                        else if ( ((LA4_3>='\u0000' && LA4_3<='`')||(LA4_3>='b' && LA4_3<='\uFFFE')) ) {
                            alt4=1;
                        }


                    }
                    else if ( ((LA4_1>='\u0000' && LA4_1<='O')||(LA4_1>='Q' && LA4_1<='\uFFFE')) ) {
                        alt4=1;
                    }


                }
                else if ( ((LA4_0>='\u0000' && LA4_0<=';')||(LA4_0>='=' && LA4_0<='\uFFFE')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:104:82: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            match("<Pages>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Start"

    // $ANTLR start "Pagesheet"
    public final void mPagesheet() throws RecognitionException {
        try {
            int _type = Pagesheet;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:105:10: ( '<PageSheet' ( options {greedy=false; } : . )* '</PageSheet>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:105:12: '<PageSheet' ( options {greedy=false; } : . )* '</PageSheet>'
            {
            match("<PageSheet"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:105:25: ( options {greedy=false; } : . )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:105:52: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match("</PageSheet>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Pagesheet"

    // $ANTLR start "Pagestart"
    public final void mPagestart() throws RecognitionException {
        try {
            int _type = Pagestart;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:106:10: ( '<Page ID' ( options {greedy=false; } : . )* '>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:106:12: '<Page ID' ( options {greedy=false; } : . )* '>'
            {
            match("<Page ID"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:106:23: ( options {greedy=false; } : . )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='>') ) {
                    alt6=2;
                }
                else if ( ((LA6_0>='\u0000' && LA6_0<='=')||(LA6_0>='?' && LA6_0<='\uFFFE')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:106:50: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Pagestart"

    // $ANTLR start "Shapestart"
    public final void mShapestart() throws RecognitionException {
        try {
            int _type = Shapestart;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:108:11: ( '<Shape ID' ( options {greedy=false; } : . )* '<PinX>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:108:13: '<Shape ID' ( options {greedy=false; } : . )* '<PinX>'
            {
            match("<Shape ID"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:108:25: ( options {greedy=false; } : . )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0=='<') ) {
                    int LA7_1 = input.LA(2);

                    if ( (LA7_1=='P') ) {
                        int LA7_3 = input.LA(3);

                        if ( (LA7_3=='i') ) {
                            int LA7_4 = input.LA(4);

                            if ( (LA7_4=='n') ) {
                                int LA7_5 = input.LA(5);

                                if ( (LA7_5=='X') ) {
                                    int LA7_6 = input.LA(6);

                                    if ( (LA7_6=='>') ) {
                                        alt7=2;
                                    }
                                    else if ( ((LA7_6>='\u0000' && LA7_6<='=')||(LA7_6>='?' && LA7_6<='\uFFFE')) ) {
                                        alt7=1;
                                    }


                                }
                                else if ( ((LA7_5>='\u0000' && LA7_5<='W')||(LA7_5>='Y' && LA7_5<='\uFFFE')) ) {
                                    alt7=1;
                                }


                            }
                            else if ( ((LA7_4>='\u0000' && LA7_4<='m')||(LA7_4>='o' && LA7_4<='\uFFFE')) ) {
                                alt7=1;
                            }


                        }
                        else if ( ((LA7_3>='\u0000' && LA7_3<='h')||(LA7_3>='j' && LA7_3<='\uFFFE')) ) {
                            alt7=1;
                        }


                    }
                    else if ( ((LA7_1>='\u0000' && LA7_1<='O')||(LA7_1>='Q' && LA7_1<='\uFFFE')) ) {
                        alt7=1;
                    }


                }
                else if ( ((LA7_0>='\u0000' && LA7_0<=';')||(LA7_0>='=' && LA7_0<='\uFFFE')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:108:52: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);

            match("<PinX>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "Shapestart"

    // $ANTLR start "YtoWith"
    public final void mYtoWith() throws RecognitionException {
        try {
            int _type = YtoWith;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:110:8: ( '</PinY>' ( options {greedy=false; } : . )* '<Width>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:110:10: '</PinY>' ( options {greedy=false; } : . )* '<Width>'
            {
            match("</PinY>"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:110:20: ( options {greedy=false; } : . )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0=='<') ) {
                    int LA8_1 = input.LA(2);

                    if ( (LA8_1=='W') ) {
                        int LA8_3 = input.LA(3);

                        if ( (LA8_3=='i') ) {
                            int LA8_4 = input.LA(4);

                            if ( (LA8_4=='d') ) {
                                int LA8_5 = input.LA(5);

                                if ( (LA8_5=='t') ) {
                                    int LA8_6 = input.LA(6);

                                    if ( (LA8_6=='h') ) {
                                        int LA8_7 = input.LA(7);

                                        if ( (LA8_7=='>') ) {
                                            alt8=2;
                                        }
                                        else if ( ((LA8_7>='\u0000' && LA8_7<='=')||(LA8_7>='?' && LA8_7<='\uFFFE')) ) {
                                            alt8=1;
                                        }


                                    }
                                    else if ( ((LA8_6>='\u0000' && LA8_6<='g')||(LA8_6>='i' && LA8_6<='\uFFFE')) ) {
                                        alt8=1;
                                    }


                                }
                                else if ( ((LA8_5>='\u0000' && LA8_5<='s')||(LA8_5>='u' && LA8_5<='\uFFFE')) ) {
                                    alt8=1;
                                }


                            }
                            else if ( ((LA8_4>='\u0000' && LA8_4<='c')||(LA8_4>='e' && LA8_4<='\uFFFE')) ) {
                                alt8=1;
                            }


                        }
                        else if ( ((LA8_3>='\u0000' && LA8_3<='h')||(LA8_3>='j' && LA8_3<='\uFFFE')) ) {
                            alt8=1;
                        }


                    }
                    else if ( ((LA8_1>='\u0000' && LA8_1<='V')||(LA8_1>='X' && LA8_1<='\uFFFE')) ) {
                        alt8=1;
                    }


                }
                else if ( ((LA8_0>='\u0000' && LA8_0<=';')||(LA8_0>='=' && LA8_0<='\uFFFE')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:110:47: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            match("<Width>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "YtoWith"

    // $ANTLR start "HeighttoText"
    public final void mHeighttoText() throws RecognitionException {
        try {
            int _type = HeighttoText;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:112:13: ( '</Height>' ( options {greedy=false; } : . )* ( '</Shape>' | '<Text>' ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:112:15: '</Height>' ( options {greedy=false; } : . )* ( '</Shape>' | '<Text>' )
            {
            match("</Height>"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:112:27: ( options {greedy=false; } : . )*
            loop9:
            do {
                int alt9=2;
                alt9 = dfa9.predict(input);
                switch (alt9) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:112:54: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:112:58: ( '</Shape>' | '<Text>' )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='<') ) {
                int LA10_1 = input.LA(2);

                if ( (LA10_1=='/') ) {
                    alt10=1;
                }
                else if ( (LA10_1=='T') ) {
                    alt10=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 10, 1, input);

                    throw nvae;
                }
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:112:59: '</Shape>'
                    {
                    match("</Shape>"); 


                    }
                    break;
                case 2 :
                    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:112:70: '<Text>'
                    {
                    match("<Text>"); 


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
    // $ANTLR end "HeighttoText"

    // $ANTLR start "End"
    public final void mEnd() throws RecognitionException {
        try {
            int _type = End;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:114:4: ( '<Windows' ( options {greedy=false; } : . )* '</VisioDocument>' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:114:6: '<Windows' ( options {greedy=false; } : . )* '</VisioDocument>'
            {
            match("<Windows"); 

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:114:17: ( options {greedy=false; } : . )*
            loop11:
            do {
                int alt11=2;
                alt11 = dfa11.predict(input);
                switch (alt11) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:114:44: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);

            match("</VisioDocument>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "End"

    public void mTokens() throws RecognitionException {
        // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:8: ( T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | Misc | Misc2 | Misc3 | Start | Pagesheet | Pagestart | Shapestart | YtoWith | HeighttoText | End | BasicLexer. Tokens )
        int alt12=25;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:10: T__79
                {
                mT__79(); 

                }
                break;
            case 2 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:16: T__80
                {
                mT__80(); 

                }
                break;
            case 3 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:22: T__81
                {
                mT__81(); 

                }
                break;
            case 4 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:28: T__82
                {
                mT__82(); 

                }
                break;
            case 5 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:34: T__83
                {
                mT__83(); 

                }
                break;
            case 6 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:40: T__84
                {
                mT__84(); 

                }
                break;
            case 7 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:46: T__85
                {
                mT__85(); 

                }
                break;
            case 8 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:52: T__86
                {
                mT__86(); 

                }
                break;
            case 9 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:58: T__87
                {
                mT__87(); 

                }
                break;
            case 10 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:64: T__88
                {
                mT__88(); 

                }
                break;
            case 11 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:70: T__89
                {
                mT__89(); 

                }
                break;
            case 12 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:76: T__90
                {
                mT__90(); 

                }
                break;
            case 13 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:82: T__91
                {
                mT__91(); 

                }
                break;
            case 14 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:88: T__92
                {
                mT__92(); 

                }
                break;
            case 15 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:94: Misc
                {
                mMisc(); 

                }
                break;
            case 16 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:99: Misc2
                {
                mMisc2(); 

                }
                break;
            case 17 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:105: Misc3
                {
                mMisc3(); 

                }
                break;
            case 18 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:111: Start
                {
                mStart(); 

                }
                break;
            case 19 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:117: Pagesheet
                {
                mPagesheet(); 

                }
                break;
            case 20 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:127: Pagestart
                {
                mPagestart(); 

                }
                break;
            case 21 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:137: Shapestart
                {
                mShapestart(); 

                }
                break;
            case 22 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:148: YtoWith
                {
                mYtoWith(); 

                }
                break;
            case 23 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:156: HeighttoText
                {
                mHeighttoText(); 

                }
                break;
            case 24 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:169: End
                {
                mEnd(); 

                }
                break;
            case 25 :
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:1:173: BasicLexer. Tokens
                {
                gBasicLexer.mTokens(); 

                }
                break;

        }

    }


    protected DFA5 dfa5 = new DFA5(this);
    protected DFA9 dfa9 = new DFA9(this);
    protected DFA11 dfa11 = new DFA11(this);
    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA5_eotS =
        "\16\uffff";
    static final String DFA5_eofS =
        "\16\uffff";
    static final String DFA5_minS =
        "\2\0\1\uffff\12\0\1\uffff";
    static final String DFA5_maxS =
        "\2\ufffe\1\uffff\12\ufffe\1\uffff";
    static final String DFA5_acceptS =
        "\2\uffff\1\1\12\uffff\1\2";
    static final String DFA5_specialS =
        "\16\uffff}>";
    static final String[] DFA5_transitionS = {
            "\74\2\1\1\uffc2\2",
            "\57\2\1\3\uffcf\2",
            "",
            "\120\2\1\4\uffae\2",
            "\141\2\1\5\uff9d\2",
            "\147\2\1\6\uff97\2",
            "\145\2\1\7\uff99\2",
            "\123\2\1\10\uffab\2",
            "\150\2\1\11\uff96\2",
            "\145\2\1\12\uff99\2",
            "\145\2\1\13\uff99\2",
            "\164\2\1\14\uff8a\2",
            "\76\2\1\15\uffc0\2",
            ""
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "()* loopback of 105:25: ( options {greedy=false; } : . )*";
        }
    }
    static final String DFA9_eotS =
        "\17\uffff";
    static final String DFA9_eofS =
        "\17\uffff";
    static final String DFA9_minS =
        "\2\0\1\uffff\11\0\1\uffff\1\0\1\uffff";
    static final String DFA9_maxS =
        "\2\ufffe\1\uffff\11\ufffe\1\uffff\1\ufffe\1\uffff";
    static final String DFA9_acceptS =
        "\2\uffff\1\1\11\uffff\1\2\1\uffff\1\2";
    static final String DFA9_specialS =
        "\17\uffff}>";
    static final String[] DFA9_transitionS = {
            "\74\2\1\1\uffc2\2",
            "\57\2\1\3\44\2\1\4\uffaa\2",
            "",
            "\123\2\1\5\uffab\2",
            "\145\2\1\6\uff99\2",
            "\150\2\1\7\uff96\2",
            "\170\2\1\10\uff86\2",
            "\141\2\1\11\uff9d\2",
            "\164\2\1\12\uff8a\2",
            "\160\2\1\13\uff8e\2",
            "\76\2\1\14\uffc0\2",
            "\145\2\1\15\uff99\2",
            "",
            "\76\2\1\16\uffc0\2",
            ""
    };

    static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
    static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
    static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
    static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
    static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
    static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
    static final short[][] DFA9_transition;

    static {
        int numStates = DFA9_transitionS.length;
        DFA9_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
        }
    }

    class DFA9 extends DFA {

        public DFA9(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 9;
            this.eot = DFA9_eot;
            this.eof = DFA9_eof;
            this.min = DFA9_min;
            this.max = DFA9_max;
            this.accept = DFA9_accept;
            this.special = DFA9_special;
            this.transition = DFA9_transition;
        }
        public String getDescription() {
            return "()* loopback of 112:27: ( options {greedy=false; } : . )*";
        }
    }
    static final String DFA11_eotS =
        "\22\uffff";
    static final String DFA11_eofS =
        "\22\uffff";
    static final String DFA11_minS =
        "\2\0\1\uffff\16\0\1\uffff";
    static final String DFA11_maxS =
        "\2\ufffe\1\uffff\16\ufffe\1\uffff";
    static final String DFA11_acceptS =
        "\2\uffff\1\1\16\uffff\1\2";
    static final String DFA11_specialS =
        "\22\uffff}>";
    static final String[] DFA11_transitionS = {
            "\74\2\1\1\uffc2\2",
            "\57\2\1\3\uffcf\2",
            "",
            "\126\2\1\4\uffa8\2",
            "\151\2\1\5\uff95\2",
            "\163\2\1\6\uff8b\2",
            "\151\2\1\7\uff95\2",
            "\157\2\1\10\uff8f\2",
            "\104\2\1\11\uffba\2",
            "\157\2\1\12\uff8f\2",
            "\143\2\1\13\uff9b\2",
            "\165\2\1\14\uff89\2",
            "\155\2\1\15\uff91\2",
            "\145\2\1\16\uff99\2",
            "\156\2\1\17\uff90\2",
            "\164\2\1\20\uff8a\2",
            "\76\2\1\21\uffc0\2",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "()* loopback of 114:17: ( options {greedy=false; } : . )*";
        }
    }
    static final String DFA12_eotS =
        "\1\uffff\3\6\1\22\1\6\12\uffff\2\6\1\uffff\2\6\10\uffff\4\6\4\uffff"+
        "\4\6\4\uffff\4\6\6\uffff\1\6\2\uffff\1\6\4\uffff\4\6\1\uffff\3\6"+
        "\1\uffff";
    static final String DFA12_eofS =
        "\110\uffff";
    static final String DFA12_minS =
        "\1\11\1\57\1\151\1\162\1\44\1\157\1\uffff\1\110\1\150\1\141\6\uffff"+
        "\1\154\1\u00f6\1\uffff\1\141\1\154\1\141\4\uffff\1\141\1\uffff\1"+
        "\147\1\144\1\u00df\3\147\1\156\1\160\1\145\1\156\4\145\1\130\1\145"+
        "\1\40\1\141\2\72\1\146\1\76\2\uffff\1\40\2\uffff\1\155\2\uffff\1"+
        "\162\4\uffff\1\145\1\141\1\72\1\147\1\uffff\1\145\1\156\1\72\1\uffff";
    static final String DFA12_maxS =
        "\1\uefff\1\164\1\151\1\162\1\uefff\1\162\1\uffff\1\127\1\150\1"+
        "\151\6\uffff\1\154\1\u00f6\1\uffff\1\141\1\154\1\151\4\uffff\1\141"+
        "\1\uffff\1\147\1\144\1\u00df\3\147\1\156\1\160\1\145\1\156\4\145"+
        "\1\131\1\145\1\123\1\141\2\72\1\146\1\163\2\uffff\1\163\2\uffff"+
        "\1\155\2\uffff\1\162\4\uffff\1\145\1\141\1\72\1\147\1\uffff\1\145"+
        "\1\156\1\72\1\uffff";
    static final String DFA12_acceptS =
        "\6\uffff\1\31\3\uffff\1\10\1\17\1\20\1\21\1\22\1\30\2\uffff\1\14"+
        "\3\uffff\1\4\1\7\1\11\1\27\1\uffff\1\6\26\uffff\1\5\1\26\1\uffff"+
        "\1\23\1\24\1\uffff\1\13\1\15\1\uffff\1\1\1\2\1\3\1\25\4\uffff\1"+
        "\12\3\uffff\1\16";
    static final String DFA12_specialS =
        "\110\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\6\2\uffff\1\6\22\uffff\34\6\1\1\5\6\1\2\3\6\1\5\1\3\24\6"+
            "\1\uffff\1\6\1\uffff\1\6\1\uffff\27\6\1\4\6\6\42\uffff\uef5f"+
            "\6",
            "\1\7\17\uffff\1\16\10\uffff\1\12\7\uffff\1\11\2\uffff\1\10"+
            "\3\uffff\1\17\13\uffff\1\13\14\uffff\1\15\3\uffff\1\14",
            "\1\20",
            "\1\21",
            "\4\6\27\uffff\1\6\1\uffff\32\6\4\uffff\1\6\1\uffff\32\6\46"+
            "\uffff\uef5f\6",
            "\1\24\2\uffff\1\23",
            "",
            "\1\31\7\uffff\1\25\2\uffff\1\26\1\30\2\uffff\1\27",
            "\1\32",
            "\1\34\7\uffff\1\33",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\35",
            "\1\36",
            "",
            "\1\37",
            "\1\40",
            "\1\41\7\uffff\1\42",
            "",
            "",
            "",
            "",
            "\1\43",
            "",
            "\1\44",
            "\1\45",
            "\1\46",
            "\1\47",
            "\1\50",
            "\1\51",
            "\1\52",
            "\1\53",
            "\1\54",
            "\1\55",
            "\1\56",
            "\1\57",
            "\1\60",
            "\1\61",
            "\1\62\1\63",
            "\1\64",
            "\1\66\62\uffff\1\65",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\72",
            "\1\74\64\uffff\1\73",
            "",
            "",
            "\1\76\122\uffff\1\75",
            "",
            "",
            "\1\77",
            "",
            "",
            "\1\100",
            "",
            "",
            "",
            "",
            "\1\101",
            "\1\102",
            "\1\103",
            "\1\104",
            "",
            "\1\105",
            "\1\106",
            "\1\107",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__79 | T__80 | T__81 | T__82 | T__83 | T__84 | T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | Misc | Misc2 | Misc3 | Start | Pagesheet | Pagestart | Shapestart | YtoWith | HeighttoText | End | BasicLexer. Tokens );";
        }
    }
 

}