// $ANTLR 3.1.1 D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DefaultLexer.g 2009-10-14 10:09:28

package de.d3web.KnOfficeParser;


import org.antlr.runtime.*;

public class DefaultLexer extends Lexer {
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
    public static final int Tokens=59;
    public static final int SEMI=9;
    public static final int REF=53;
    public static final int WS=30;
    public static final int BLUB=58;
    public static final int CBC=18;
    public static final int OR=36;
    public static final int SBO=19;
    public static final int DIVTEXT=55;
    public static final int DIV=29;
    public static final int INIT=50;
    public static final int CBO=17;
    public static final int LE=21;

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


    // delegates
    public DefaultLexer_BasicLexer gBasicLexer;
    // delegators

    public DefaultLexer() {;} 
    public DefaultLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public DefaultLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
        gBasicLexer = new DefaultLexer_BasicLexer(input, state, this);
    }
    @Override
	public String getGrammarFileName() { return "D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DefaultLexer.g"; }

    // $ANTLR start "BLUB"
    public final void mBLUB() throws RecognitionException {
        try {
            int _type = BLUB;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DefaultLexer.g:57:5: ()
            // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DefaultLexer.g:57:6: 
            {
            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BLUB"

    @Override
	public void mTokens() throws RecognitionException {
        // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DefaultLexer.g:1:8: ( BLUB | BasicLexer. Tokens )
        int alt1=2;
        int LA1_0 = input.LA(1);

        if ( ((LA1_0>='\t' && LA1_0<='\n')||LA1_0=='\r'||(LA1_0>=' ' && LA1_0<='[')||LA1_0==']'||LA1_0=='_'||(LA1_0>='a' && LA1_0<='~')||(LA1_0>='\u00A1' && LA1_0<='\uEFFF')) ) {
            alt1=2;
        }
        else {
            alt1=1;}
        switch (alt1) {
            case 1 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DefaultLexer.g:1:10: BLUB
                {
                mBLUB(); 

                }
                break;
            case 2 :
                // D:\\eclipse workspaces\\Uni SVN\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DefaultLexer.g:1:15: BasicLexer. Tokens
                {
                gBasicLexer.mTokens(); 

                }
                break;

        }

    }


 

}