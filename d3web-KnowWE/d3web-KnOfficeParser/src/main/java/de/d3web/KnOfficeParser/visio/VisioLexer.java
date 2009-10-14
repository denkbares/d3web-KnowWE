// $ANTLR 3.1.1 D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g 2009-10-14 10:52:30

package de.d3web.KnOfficeParser.visio;
import de.d3web.KnOfficeParser.LexerErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class VisioLexer extends Lexer {
    public static final int Aidtext=72;
    public static final int FUZZY=54;
    public static final int LP=15;
    public static final int Textboxtext=70;
    public static final int NOT=37;
    public static final int Page=58;
    public static final int EXCEPT=39;
    public static final int EOF=-1;
    public static final int DD=7;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int T__90=90;
    public static final int EX=10;
    public static final int INCLUDE=48;
    public static final int NL=32;
    public static final int EQ=25;
    public static final int COMMENT=31;
    public static final int T__97=97;
    public static final int T__96=96;
    public static final int Shapestart=78;
    public static final int T__95=95;
    public static final int GE=23;
    public static final int G=24;
    public static final int SBC=20;
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
    public static final int Picture=67;
    public static final int Xcoord=60;
    public static final int WS=30;
    public static final int OR=36;
    public static final int SBO=19;
    public static final int Misc2=82;
    public static final int Misc3=83;
    public static final int Popup=71;
    public static final int YtoWith=79;
    public static final int INIT=50;
    public static final int End=75;
    public static final int Ycoord=61;
    public static final int Shapetext=66;
    public static final int HIDE=38;
    public static final int RP=16;
    public static final int ORS=12;
    public static final int MyDouble=65;
    public static final int ABSTRACT=51;
    public static final int AND=35;
    public static final int ID=57;
    public static final int Width=62;
    public static final int IF=33;
    public static final int AT=11;
    public static final int THEN=34;
    public static final int IN=44;
    public static final int UNKNOWN=40;
    public static final int COMMA=8;
    public static final int Height=63;
    public static final int ALL=46;
    public static final int PROD=28;
    public static final int Knowledge=73;
    public static final int TILDE=14;
    public static final int PLUS=26;
    public static final int String=4;
    public static final int DOT=6;
    public static final int HeighttoText=80;
    public static final int Start=74;
    public static final int Pagesheet=77;
    public static final int Misc=81;
    public static final int ALLOWEDNAMES=47;
    public static final int INSTANT=42;
    public static final int MINMAX=43;
    public static final int DEFAULT=49;
    public static final int INTER=45;
    public static final int SET=52;
    public static final int MINUS=27;
    public static final int DIVNORM=56;
    public static final int Tokens=98;
    public static final int SEMI=9;
    public static final int REF=53;
    public static final int QID=69;
    public static final int Box=68;
    public static final int CBC=18;
    public static final int Text=64;
    public static final int Shape=59;
    public static final int DIVTEXT=55;
    public static final int DIV=29;
    public static final int CBO=17;
    public static final int LE=21;
    public static final int Pagestart=76;

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
    public String getGrammarFileName() { return "D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g"; }

    // $ANTLR start "T__84"
    public final void mT__84() throws RecognitionException {
        try {
            int _type = T__84;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:36:7: ( '</Pages>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:36:9: '</Pages>'
            {
            match("</Pages>"); 


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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:37:7: ( '</Page>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:37:9: '</Page>'
            {
            match("</Page>"); 


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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:38:7: ( '<Shapes>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:38:9: '<Shapes>'
            {
            match("<Shapes>"); 


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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:39:7: ( '</Shapes>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:39:9: '</Shapes>'
            {
            match("</Shapes>"); 


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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:40:7: ( '</PinX>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:40:9: '</PinX>'
            {
            match("</PinX>"); 


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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:41:7: ( '<PinY>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:41:9: '<PinY>'
            {
            match("<PinY>"); 


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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:42:7: ( '</Width>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:42:9: '</Width>'
            {
            match("</Width>"); 


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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:43:7: ( '<Height>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:43:9: '<Height>'
            {
            match("<Height>"); 


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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:44:7: ( '</Text></Shape>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:44:9: '</Text></Shape>'
            {
            match("</Text></Shape>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__92"

    // $ANTLR start "T__93"
    public final void mT__93() throws RecognitionException {
        try {
            int _type = T__93;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:45:7: ( 'Bildname:' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:45:9: 'Bildname:'
            {
            match("Bildname:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__93"

    // $ANTLR start "T__94"
    public final void mT__94() throws RecognitionException {
        try {
            int _type = T__94;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:46:7: ( 'Groesse:' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:46:9: 'Groesse:'
            {
            match("Groesse:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__94"

    // $ANTLR start "T__95"
    public final void mT__95() throws RecognitionException {
        try {
            int _type = T__95;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:47:7: ( 'x' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:47:9: 'x'
            {
            match('x'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__95"

    // $ANTLR start "T__96"
    public final void mT__96() throws RecognitionException {
        try {
            int _type = T__96;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:48:7: ( 'Frage:' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:48:9: 'Frage:'
            {
            match("Frage:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__96"

    // $ANTLR start "T__97"
    public final void mT__97() throws RecognitionException {
        try {
            int _type = T__97;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:49:7: ( 'Folgefragen:' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:49:9: 'Folgefragen:'
            {
            match("Folgefragen:"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__97"

    // $ANTLR start "Misc"
    public final void mMisc() throws RecognitionException {
        try {
            int _type = Misc;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:121:6: ( '<cp IX=\\'' ( options {greedy=false; } : . )* '\\'/>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:121:8: '<cp IX=\\'' ( options {greedy=false; } : . )* '\\'/>'
            {
            match("<cp IX=\'"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:121:20: ( options {greedy=false; } : . )*
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
                        else if ( ((LA1_3>='\u0000' && LA1_3<='=')||(LA1_3>='?' && LA1_3<='\uFFFF')) ) {
                            alt1=1;
                        }


                    }
                    else if ( ((LA1_1>='\u0000' && LA1_1<='.')||(LA1_1>='0' && LA1_1<='\uFFFF')) ) {
                        alt1=1;
                    }


                }
                else if ( ((LA1_0>='\u0000' && LA1_0<='&')||(LA1_0>='(' && LA1_0<='\uFFFF')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:121:47: .
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:122:7: ( '<tp IX=\\'' ( options {greedy=false; } : . )* '\\'/>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:122:9: '<tp IX=\\'' ( options {greedy=false; } : . )* '\\'/>'
            {
            match("<tp IX=\'"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:122:21: ( options {greedy=false; } : . )*
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
                        else if ( ((LA2_3>='\u0000' && LA2_3<='=')||(LA2_3>='?' && LA2_3<='\uFFFF')) ) {
                            alt2=1;
                        }


                    }
                    else if ( ((LA2_1>='\u0000' && LA2_1<='.')||(LA2_1>='0' && LA2_1<='\uFFFF')) ) {
                        alt2=1;
                    }


                }
                else if ( ((LA2_0>='\u0000' && LA2_0<='&')||(LA2_0>='(' && LA2_0<='\uFFFF')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:122:48: .
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:123:6: ( '<pp IX=\\'' ( options {greedy=false; } : . )* '\\'/>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:123:8: '<pp IX=\\'' ( options {greedy=false; } : . )* '\\'/>'
            {
            match("<pp IX=\'"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:123:20: ( options {greedy=false; } : . )*
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
                        else if ( ((LA3_3>='\u0000' && LA3_3<='=')||(LA3_3>='?' && LA3_3<='\uFFFF')) ) {
                            alt3=1;
                        }


                    }
                    else if ( ((LA3_1>='\u0000' && LA3_1<='.')||(LA3_1>='0' && LA3_1<='\uFFFF')) ) {
                        alt3=1;
                    }


                }
                else if ( ((LA3_0>='\u0000' && LA3_0<='&')||(LA3_0>='(' && LA3_0<='\uFFFF')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:123:47: .
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:125:7: ( '<?xml version=\\'1.0\\' encoding=\\'utf-8\\' ?>' ( options {greedy=false; } : . )* '<Pages>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:125:9: '<?xml version=\\'1.0\\' encoding=\\'utf-8\\' ?>' ( options {greedy=false; } : . )* '<Pages>'
            {
            match("<?xml version=\'1.0\' encoding=\'utf-8\' ?>"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:125:55: ( options {greedy=false; } : . )*
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
                                        else if ( ((LA4_7>='\u0000' && LA4_7<='=')||(LA4_7>='?' && LA4_7<='\uFFFF')) ) {
                                            alt4=1;
                                        }


                                    }
                                    else if ( ((LA4_6>='\u0000' && LA4_6<='r')||(LA4_6>='t' && LA4_6<='\uFFFF')) ) {
                                        alt4=1;
                                    }


                                }
                                else if ( ((LA4_5>='\u0000' && LA4_5<='d')||(LA4_5>='f' && LA4_5<='\uFFFF')) ) {
                                    alt4=1;
                                }


                            }
                            else if ( ((LA4_4>='\u0000' && LA4_4<='f')||(LA4_4>='h' && LA4_4<='\uFFFF')) ) {
                                alt4=1;
                            }


                        }
                        else if ( ((LA4_3>='\u0000' && LA4_3<='`')||(LA4_3>='b' && LA4_3<='\uFFFF')) ) {
                            alt4=1;
                        }


                    }
                    else if ( ((LA4_1>='\u0000' && LA4_1<='O')||(LA4_1>='Q' && LA4_1<='\uFFFF')) ) {
                        alt4=1;
                    }


                }
                else if ( ((LA4_0>='\u0000' && LA4_0<=';')||(LA4_0>='=' && LA4_0<='\uFFFF')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:125:82: .
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:126:10: ( '<PageSheet' ( options {greedy=false; } : . )* '</PageSheet>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:126:12: '<PageSheet' ( options {greedy=false; } : . )* '</PageSheet>'
            {
            match("<PageSheet"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:126:25: ( options {greedy=false; } : . )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:126:52: .
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:127:10: ( '<Page ID' ( options {greedy=false; } : . )* '>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:127:12: '<Page ID' ( options {greedy=false; } : . )* '>'
            {
            match("<Page ID"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:127:23: ( options {greedy=false; } : . )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='>') ) {
                    alt6=2;
                }
                else if ( ((LA6_0>='\u0000' && LA6_0<='=')||(LA6_0>='?' && LA6_0<='\uFFFF')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:127:50: .
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:129:11: ( '<Shape ID' ( options {greedy=false; } : . )* '<PinX>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:129:13: '<Shape ID' ( options {greedy=false; } : . )* '<PinX>'
            {
            match("<Shape ID"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:129:25: ( options {greedy=false; } : . )*
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
                                    else if ( ((LA7_6>='\u0000' && LA7_6<='=')||(LA7_6>='?' && LA7_6<='\uFFFF')) ) {
                                        alt7=1;
                                    }


                                }
                                else if ( ((LA7_5>='\u0000' && LA7_5<='W')||(LA7_5>='Y' && LA7_5<='\uFFFF')) ) {
                                    alt7=1;
                                }


                            }
                            else if ( ((LA7_4>='\u0000' && LA7_4<='m')||(LA7_4>='o' && LA7_4<='\uFFFF')) ) {
                                alt7=1;
                            }


                        }
                        else if ( ((LA7_3>='\u0000' && LA7_3<='h')||(LA7_3>='j' && LA7_3<='\uFFFF')) ) {
                            alt7=1;
                        }


                    }
                    else if ( ((LA7_1>='\u0000' && LA7_1<='O')||(LA7_1>='Q' && LA7_1<='\uFFFF')) ) {
                        alt7=1;
                    }


                }
                else if ( ((LA7_0>='\u0000' && LA7_0<=';')||(LA7_0>='=' && LA7_0<='\uFFFF')) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:129:52: .
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:131:8: ( '</PinY>' ( options {greedy=false; } : . )* '<Width>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:131:10: '</PinY>' ( options {greedy=false; } : . )* '<Width>'
            {
            match("</PinY>"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:131:20: ( options {greedy=false; } : . )*
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
                                        else if ( ((LA8_7>='\u0000' && LA8_7<='=')||(LA8_7>='?' && LA8_7<='\uFFFF')) ) {
                                            alt8=1;
                                        }


                                    }
                                    else if ( ((LA8_6>='\u0000' && LA8_6<='g')||(LA8_6>='i' && LA8_6<='\uFFFF')) ) {
                                        alt8=1;
                                    }


                                }
                                else if ( ((LA8_5>='\u0000' && LA8_5<='s')||(LA8_5>='u' && LA8_5<='\uFFFF')) ) {
                                    alt8=1;
                                }


                            }
                            else if ( ((LA8_4>='\u0000' && LA8_4<='c')||(LA8_4>='e' && LA8_4<='\uFFFF')) ) {
                                alt8=1;
                            }


                        }
                        else if ( ((LA8_3>='\u0000' && LA8_3<='h')||(LA8_3>='j' && LA8_3<='\uFFFF')) ) {
                            alt8=1;
                        }


                    }
                    else if ( ((LA8_1>='\u0000' && LA8_1<='V')||(LA8_1>='X' && LA8_1<='\uFFFF')) ) {
                        alt8=1;
                    }


                }
                else if ( ((LA8_0>='\u0000' && LA8_0<=';')||(LA8_0>='=' && LA8_0<='\uFFFF')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:131:47: .
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:133:13: ( '</Height>' ( options {greedy=false; } : . )* ( '</Shape>' | '<Text>' ) )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:133:15: '</Height>' ( options {greedy=false; } : . )* ( '</Shape>' | '<Text>' )
            {
            match("</Height>"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:133:27: ( options {greedy=false; } : . )*
            loop9:
            do {
                int alt9=2;
                alt9 = dfa9.predict(input);
                switch (alt9) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:133:54: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:133:58: ( '</Shape>' | '<Text>' )
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
                    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:133:59: '</Shape>'
                    {
                    match("</Shape>"); 


                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:133:70: '<Text>'
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
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:135:4: ( '<Windows' ( options {greedy=false; } : . )* '</VisioDocument>' )
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:135:6: '<Windows' ( options {greedy=false; } : . )* '</VisioDocument>'
            {
            match("<Windows"); 

            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:135:17: ( options {greedy=false; } : . )*
            loop11:
            do {
                int alt11=2;
                alt11 = dfa11.predict(input);
                switch (alt11) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:135:44: .
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
        // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:8: ( T__84 | T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | T__93 | T__94 | T__95 | T__96 | T__97 | Misc | Misc2 | Misc3 | Start | Pagesheet | Pagestart | Shapestart | YtoWith | HeighttoText | End | BasicLexer. Tokens )
        int alt12=25;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:10: T__84
                {
                mT__84(); 

                }
                break;
            case 2 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:16: T__85
                {
                mT__85(); 

                }
                break;
            case 3 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:22: T__86
                {
                mT__86(); 

                }
                break;
            case 4 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:28: T__87
                {
                mT__87(); 

                }
                break;
            case 5 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:34: T__88
                {
                mT__88(); 

                }
                break;
            case 6 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:40: T__89
                {
                mT__89(); 

                }
                break;
            case 7 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:46: T__90
                {
                mT__90(); 

                }
                break;
            case 8 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:52: T__91
                {
                mT__91(); 

                }
                break;
            case 9 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:58: T__92
                {
                mT__92(); 

                }
                break;
            case 10 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:64: T__93
                {
                mT__93(); 

                }
                break;
            case 11 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:70: T__94
                {
                mT__94(); 

                }
                break;
            case 12 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:76: T__95
                {
                mT__95(); 

                }
                break;
            case 13 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:82: T__96
                {
                mT__96(); 

                }
                break;
            case 14 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:88: T__97
                {
                mT__97(); 

                }
                break;
            case 15 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:94: Misc
                {
                mMisc(); 

                }
                break;
            case 16 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:99: Misc2
                {
                mMisc2(); 

                }
                break;
            case 17 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:105: Misc3
                {
                mMisc3(); 

                }
                break;
            case 18 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:111: Start
                {
                mStart(); 

                }
                break;
            case 19 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:117: Pagesheet
                {
                mPagesheet(); 

                }
                break;
            case 20 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:127: Pagestart
                {
                mPagestart(); 

                }
                break;
            case 21 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:137: Shapestart
                {
                mShapestart(); 

                }
                break;
            case 22 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:148: YtoWith
                {
                mYtoWith(); 

                }
                break;
            case 23 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:156: HeighttoText
                {
                mHeighttoText(); 

                }
                break;
            case 24 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:169: End
                {
                mEnd(); 

                }
                break;
            case 25 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:1:173: BasicLexer. Tokens
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
        "\2\uffff\1\uffff\12\uffff\1\uffff";
    static final String DFA5_acceptS =
        "\2\uffff\1\1\12\uffff\1\2";
    static final String DFA5_specialS =
        "\1\12\1\4\1\uffff\1\5\1\2\1\3\1\0\1\1\1\11\1\13\1\7\1\10\1\6\1"+
        "\uffff}>";
    static final String[] DFA5_transitionS = {
            "\74\2\1\1\uffc3\2",
            "\57\2\1\3\uffd0\2",
            "",
            "\120\2\1\4\uffaf\2",
            "\141\2\1\5\uff9e\2",
            "\147\2\1\6\uff98\2",
            "\145\2\1\7\uff9a\2",
            "\123\2\1\10\uffac\2",
            "\150\2\1\11\uff97\2",
            "\145\2\1\12\uff9a\2",
            "\145\2\1\13\uff9a\2",
            "\164\2\1\14\uff8b\2",
            "\76\2\1\15\uffc1\2",
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
            return "()* loopback of 126:25: ( options {greedy=false; } : . )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA5_6 = input.LA(1);

                        s = -1;
                        if ( (LA5_6=='e') ) {s = 7;}

                        else if ( ((LA5_6>='\u0000' && LA5_6<='d')||(LA5_6>='f' && LA5_6<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA5_7 = input.LA(1);

                        s = -1;
                        if ( (LA5_7=='S') ) {s = 8;}

                        else if ( ((LA5_7>='\u0000' && LA5_7<='R')||(LA5_7>='T' && LA5_7<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA5_4 = input.LA(1);

                        s = -1;
                        if ( (LA5_4=='a') ) {s = 5;}

                        else if ( ((LA5_4>='\u0000' && LA5_4<='`')||(LA5_4>='b' && LA5_4<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA5_5 = input.LA(1);

                        s = -1;
                        if ( (LA5_5=='g') ) {s = 6;}

                        else if ( ((LA5_5>='\u0000' && LA5_5<='f')||(LA5_5>='h' && LA5_5<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA5_1 = input.LA(1);

                        s = -1;
                        if ( (LA5_1=='/') ) {s = 3;}

                        else if ( ((LA5_1>='\u0000' && LA5_1<='.')||(LA5_1>='0' && LA5_1<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA5_3 = input.LA(1);

                        s = -1;
                        if ( (LA5_3=='P') ) {s = 4;}

                        else if ( ((LA5_3>='\u0000' && LA5_3<='O')||(LA5_3>='Q' && LA5_3<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA5_12 = input.LA(1);

                        s = -1;
                        if ( (LA5_12=='>') ) {s = 13;}

                        else if ( ((LA5_12>='\u0000' && LA5_12<='=')||(LA5_12>='?' && LA5_12<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA5_10 = input.LA(1);

                        s = -1;
                        if ( (LA5_10=='e') ) {s = 11;}

                        else if ( ((LA5_10>='\u0000' && LA5_10<='d')||(LA5_10>='f' && LA5_10<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA5_11 = input.LA(1);

                        s = -1;
                        if ( (LA5_11=='t') ) {s = 12;}

                        else if ( ((LA5_11>='\u0000' && LA5_11<='s')||(LA5_11>='u' && LA5_11<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA5_8 = input.LA(1);

                        s = -1;
                        if ( (LA5_8=='h') ) {s = 9;}

                        else if ( ((LA5_8>='\u0000' && LA5_8<='g')||(LA5_8>='i' && LA5_8<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA5_0 = input.LA(1);

                        s = -1;
                        if ( (LA5_0=='<') ) {s = 1;}

                        else if ( ((LA5_0>='\u0000' && LA5_0<=';')||(LA5_0>='=' && LA5_0<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA5_9 = input.LA(1);

                        s = -1;
                        if ( (LA5_9=='e') ) {s = 10;}

                        else if ( ((LA5_9>='\u0000' && LA5_9<='d')||(LA5_9>='f' && LA5_9<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 5, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA9_eotS =
        "\17\uffff";
    static final String DFA9_eofS =
        "\17\uffff";
    static final String DFA9_minS =
        "\2\0\1\uffff\11\0\1\uffff\1\0\1\uffff";
    static final String DFA9_maxS =
        "\2\uffff\1\uffff\11\uffff\1\uffff\1\uffff\1\uffff";
    static final String DFA9_acceptS =
        "\2\uffff\1\1\11\uffff\1\2\1\uffff\1\2";
    static final String DFA9_specialS =
        "\1\4\1\13\1\uffff\1\0\1\7\1\1\1\12\1\5\1\11\1\6\1\10\1\2\1\uffff"+
        "\1\3\1\uffff}>";
    static final String[] DFA9_transitionS = {
            "\74\2\1\1\uffc3\2",
            "\57\2\1\3\44\2\1\4\uffab\2",
            "",
            "\123\2\1\5\uffac\2",
            "\145\2\1\6\uff9a\2",
            "\150\2\1\7\uff97\2",
            "\170\2\1\10\uff87\2",
            "\141\2\1\11\uff9e\2",
            "\164\2\1\12\uff8b\2",
            "\160\2\1\13\uff8f\2",
            "\76\2\1\14\uffc1\2",
            "\145\2\1\15\uff9a\2",
            "",
            "\76\2\1\16\uffc1\2",
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
            return "()* loopback of 133:27: ( options {greedy=false; } : . )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA9_3 = input.LA(1);

                        s = -1;
                        if ( (LA9_3=='S') ) {s = 5;}

                        else if ( ((LA9_3>='\u0000' && LA9_3<='R')||(LA9_3>='T' && LA9_3<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA9_5 = input.LA(1);

                        s = -1;
                        if ( (LA9_5=='h') ) {s = 7;}

                        else if ( ((LA9_5>='\u0000' && LA9_5<='g')||(LA9_5>='i' && LA9_5<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA9_11 = input.LA(1);

                        s = -1;
                        if ( (LA9_11=='e') ) {s = 13;}

                        else if ( ((LA9_11>='\u0000' && LA9_11<='d')||(LA9_11>='f' && LA9_11<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA9_13 = input.LA(1);

                        s = -1;
                        if ( (LA9_13=='>') ) {s = 14;}

                        else if ( ((LA9_13>='\u0000' && LA9_13<='=')||(LA9_13>='?' && LA9_13<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA9_0 = input.LA(1);

                        s = -1;
                        if ( (LA9_0=='<') ) {s = 1;}

                        else if ( ((LA9_0>='\u0000' && LA9_0<=';')||(LA9_0>='=' && LA9_0<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA9_7 = input.LA(1);

                        s = -1;
                        if ( (LA9_7=='a') ) {s = 9;}

                        else if ( ((LA9_7>='\u0000' && LA9_7<='`')||(LA9_7>='b' && LA9_7<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA9_9 = input.LA(1);

                        s = -1;
                        if ( (LA9_9=='p') ) {s = 11;}

                        else if ( ((LA9_9>='\u0000' && LA9_9<='o')||(LA9_9>='q' && LA9_9<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA9_4 = input.LA(1);

                        s = -1;
                        if ( (LA9_4=='e') ) {s = 6;}

                        else if ( ((LA9_4>='\u0000' && LA9_4<='d')||(LA9_4>='f' && LA9_4<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA9_10 = input.LA(1);

                        s = -1;
                        if ( (LA9_10=='>') ) {s = 12;}

                        else if ( ((LA9_10>='\u0000' && LA9_10<='=')||(LA9_10>='?' && LA9_10<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA9_8 = input.LA(1);

                        s = -1;
                        if ( (LA9_8=='t') ) {s = 10;}

                        else if ( ((LA9_8>='\u0000' && LA9_8<='s')||(LA9_8>='u' && LA9_8<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA9_6 = input.LA(1);

                        s = -1;
                        if ( (LA9_6=='x') ) {s = 8;}

                        else if ( ((LA9_6>='\u0000' && LA9_6<='w')||(LA9_6>='y' && LA9_6<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA9_1 = input.LA(1);

                        s = -1;
                        if ( (LA9_1=='/') ) {s = 3;}

                        else if ( (LA9_1=='T') ) {s = 4;}

                        else if ( ((LA9_1>='\u0000' && LA9_1<='.')||(LA9_1>='0' && LA9_1<='S')||(LA9_1>='U' && LA9_1<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 9, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA11_eotS =
        "\22\uffff";
    static final String DFA11_eofS =
        "\22\uffff";
    static final String DFA11_minS =
        "\2\0\1\uffff\16\0\1\uffff";
    static final String DFA11_maxS =
        "\2\uffff\1\uffff\16\uffff\1\uffff";
    static final String DFA11_acceptS =
        "\2\uffff\1\1\16\uffff\1\2";
    static final String DFA11_specialS =
        "\1\17\1\14\1\uffff\1\13\1\16\1\15\1\3\1\4\1\5\1\6\1\7\1\10\1\11"+
        "\1\12\1\0\1\1\1\2\1\uffff}>";
    static final String[] DFA11_transitionS = {
            "\74\2\1\1\uffc3\2",
            "\57\2\1\3\uffd0\2",
            "",
            "\126\2\1\4\uffa9\2",
            "\151\2\1\5\uff96\2",
            "\163\2\1\6\uff8c\2",
            "\151\2\1\7\uff96\2",
            "\157\2\1\10\uff90\2",
            "\104\2\1\11\uffbb\2",
            "\157\2\1\12\uff90\2",
            "\143\2\1\13\uff9c\2",
            "\165\2\1\14\uff8a\2",
            "\155\2\1\15\uff92\2",
            "\145\2\1\16\uff9a\2",
            "\156\2\1\17\uff91\2",
            "\164\2\1\20\uff8b\2",
            "\76\2\1\21\uffc1\2",
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
            return "()* loopback of 135:17: ( options {greedy=false; } : . )*";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            IntStream input = _input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA11_14 = input.LA(1);

                        s = -1;
                        if ( (LA11_14=='n') ) {s = 15;}

                        else if ( ((LA11_14>='\u0000' && LA11_14<='m')||(LA11_14>='o' && LA11_14<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA11_15 = input.LA(1);

                        s = -1;
                        if ( (LA11_15=='t') ) {s = 16;}

                        else if ( ((LA11_15>='\u0000' && LA11_15<='s')||(LA11_15>='u' && LA11_15<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA11_16 = input.LA(1);

                        s = -1;
                        if ( (LA11_16=='>') ) {s = 17;}

                        else if ( ((LA11_16>='\u0000' && LA11_16<='=')||(LA11_16>='?' && LA11_16<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA11_6 = input.LA(1);

                        s = -1;
                        if ( (LA11_6=='i') ) {s = 7;}

                        else if ( ((LA11_6>='\u0000' && LA11_6<='h')||(LA11_6>='j' && LA11_6<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA11_7 = input.LA(1);

                        s = -1;
                        if ( (LA11_7=='o') ) {s = 8;}

                        else if ( ((LA11_7>='\u0000' && LA11_7<='n')||(LA11_7>='p' && LA11_7<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA11_8 = input.LA(1);

                        s = -1;
                        if ( (LA11_8=='D') ) {s = 9;}

                        else if ( ((LA11_8>='\u0000' && LA11_8<='C')||(LA11_8>='E' && LA11_8<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA11_9 = input.LA(1);

                        s = -1;
                        if ( (LA11_9=='o') ) {s = 10;}

                        else if ( ((LA11_9>='\u0000' && LA11_9<='n')||(LA11_9>='p' && LA11_9<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA11_10 = input.LA(1);

                        s = -1;
                        if ( (LA11_10=='c') ) {s = 11;}

                        else if ( ((LA11_10>='\u0000' && LA11_10<='b')||(LA11_10>='d' && LA11_10<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA11_11 = input.LA(1);

                        s = -1;
                        if ( (LA11_11=='u') ) {s = 12;}

                        else if ( ((LA11_11>='\u0000' && LA11_11<='t')||(LA11_11>='v' && LA11_11<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA11_12 = input.LA(1);

                        s = -1;
                        if ( (LA11_12=='m') ) {s = 13;}

                        else if ( ((LA11_12>='\u0000' && LA11_12<='l')||(LA11_12>='n' && LA11_12<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA11_13 = input.LA(1);

                        s = -1;
                        if ( (LA11_13=='e') ) {s = 14;}

                        else if ( ((LA11_13>='\u0000' && LA11_13<='d')||(LA11_13>='f' && LA11_13<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA11_3 = input.LA(1);

                        s = -1;
                        if ( (LA11_3=='V') ) {s = 4;}

                        else if ( ((LA11_3>='\u0000' && LA11_3<='U')||(LA11_3>='W' && LA11_3<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA11_1 = input.LA(1);

                        s = -1;
                        if ( (LA11_1=='/') ) {s = 3;}

                        else if ( ((LA11_1>='\u0000' && LA11_1<='.')||(LA11_1>='0' && LA11_1<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA11_5 = input.LA(1);

                        s = -1;
                        if ( (LA11_5=='s') ) {s = 6;}

                        else if ( ((LA11_5>='\u0000' && LA11_5<='r')||(LA11_5>='t' && LA11_5<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA11_4 = input.LA(1);

                        s = -1;
                        if ( (LA11_4=='i') ) {s = 5;}

                        else if ( ((LA11_4>='\u0000' && LA11_4<='h')||(LA11_4>='j' && LA11_4<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA11_0 = input.LA(1);

                        s = -1;
                        if ( (LA11_0=='<') ) {s = 1;}

                        else if ( ((LA11_0>='\u0000' && LA11_0<=';')||(LA11_0>='=' && LA11_0<='\uFFFF')) ) {s = 2;}

                        if ( s>=0 ) return s;
                        break;
            }
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 11, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA12_eotS =
        "\1\uffff\3\6\1\22\1\6\12\uffff\2\6\1\uffff\2\6\10\uffff\4\6\4\uffff"+
        "\4\6\4\uffff\4\6\6\uffff\2\6\1\uffff\1\6\4\uffff\4\6\1\uffff\1\6"+
        "\1\uffff\3\6\1\uffff";
    static final String DFA12_eofS =
        "\112\uffff";
    static final String DFA12_minS =
        "\1\11\1\57\1\151\1\162\1\44\1\157\1\uffff\1\110\1\150\1\141\6\uffff"+
        "\1\154\1\157\1\uffff\1\141\1\154\1\141\4\uffff\1\141\1\uffff\1\147"+
        "\1\144\1\145\3\147\1\156\1\160\1\145\1\156\1\163\3\145\1\130\1\145"+
        "\1\40\1\141\1\163\1\72\1\146\1\76\2\uffff\1\40\2\uffff\1\155\1\145"+
        "\1\uffff\1\162\4\uffff\1\145\1\72\1\141\1\72\1\uffff\1\147\1\uffff"+
        "\1\145\1\156\1\72\1\uffff";
    static final String DFA12_maxS =
        "\1\uefff\1\164\1\151\1\162\1\uefff\1\162\1\uffff\1\127\1\150\1"+
        "\151\6\uffff\1\154\1\157\1\uffff\1\141\1\154\1\151\4\uffff\1\141"+
        "\1\uffff\1\147\1\144\1\145\3\147\1\156\1\160\1\145\1\156\1\163\3"+
        "\145\1\131\1\145\1\123\1\141\1\163\1\72\1\146\1\163\2\uffff\1\163"+
        "\2\uffff\1\155\1\145\1\uffff\1\162\4\uffff\1\145\1\72\1\141\1\72"+
        "\1\uffff\1\147\1\uffff\1\145\1\156\1\72\1\uffff";
    static final String DFA12_acceptS =
        "\6\uffff\1\31\3\uffff\1\10\1\17\1\20\1\21\1\22\1\30\2\uffff\1\14"+
        "\3\uffff\1\4\1\7\1\11\1\27\1\uffff\1\6\26\uffff\1\5\1\26\1\uffff"+
        "\1\23\1\24\2\uffff\1\15\1\uffff\1\1\1\2\1\3\1\25\4\uffff\1\13\1"+
        "\uffff\1\12\3\uffff\1\16";
    static final String DFA12_specialS =
        "\112\uffff}>";
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
            "\1\100",
            "",
            "\1\101",
            "",
            "",
            "",
            "",
            "\1\102",
            "\1\103",
            "\1\104",
            "\1\105",
            "",
            "\1\106",
            "",
            "\1\107",
            "\1\110",
            "\1\111",
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
            return "1:1: Tokens : ( T__84 | T__85 | T__86 | T__87 | T__88 | T__89 | T__90 | T__91 | T__92 | T__93 | T__94 | T__95 | T__96 | T__97 | Misc | Misc2 | Misc3 | Start | Pagesheet | Pagestart | Shapestart | YtoWith | HeighttoText | End | BasicLexer. Tokens );";
        }
    }
 

}