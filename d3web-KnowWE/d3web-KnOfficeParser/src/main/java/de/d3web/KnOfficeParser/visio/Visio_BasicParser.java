// $ANTLR 3.1.1 BasicParser.g 2009-10-14 10:52:29

package de.d3web.KnOfficeParser.visio;
import de.d3web.KnOfficeParser.ParserErrorHandler;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;

public class Visio_BasicParser extends Parser {
    public static final int Aidtext=72;
    public static final int LP=15;
    public static final int FUZZY=54;
    public static final int Textboxtext=70;
    public static final int NOT=37;
    public static final int Page=58;
    public static final int EXCEPT=39;
    public static final int DD=7;
    public static final int EOF=-1;
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
    public static final int Shapestart=78;
    public static final int T__96=96;
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
    public static final int SEMI=9;
    public static final int REF=53;
    public static final int QID=69;
    public static final int Box=68;
    public static final int CBC=18;
    public static final int Shape=59;
    public static final int Text=64;
    public static final int DIVTEXT=55;
    public static final int DIV=29;
    public static final int CBO=17;
    public static final int LE=21;
    public static final int Pagestart=76;

    // delegates
    // delegators
    public VisioParser gVisio;
    public VisioParser gParent;


        public Visio_BasicParser(TokenStream input, VisioParser gVisio) {
            this(input, new RecognizerSharedState(), gVisio);
        }
        public Visio_BasicParser(TokenStream input, RecognizerSharedState state, VisioParser gVisio) {
            super(input, state);
            this.gVisio = gVisio;
             
            gParent = gVisio;
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    @Override
	public String[] getTokenNames() { return VisioParser.tokenNames; }
    @Override
	public String getGrammarFileName() { return "BasicParser.g"; }



      private ParserErrorHandler eh;
      
      public void setEH(ParserErrorHandler eh) {
        this.eh=eh;
      }
      
      private String delQuotes(String s) {
        s=s.substring(1, s.length()-1);
        s=s.replace("\\\"", "\"");
        return s;
      }
      
      private Double parseDouble(String s) {
        if (s==null||s.equals("")) s="0";
        s=s.replace(',', '.');
        Double d=0.0;
        try {
          d = Double.parseDouble(s);
        } catch (NumberFormatException e) {
          
        }
          return d;
      }
      
      @Override
      public void reportError(RecognitionException re) {
        if (eh!=null) {
          eh.parsererror(re);
        } else {
          super.reportError(re);
        }
      }


    public static class name_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        @Override
		public Object getTree() { return tree; }
    };

    // $ANTLR start "name"
    // BasicParser.g:62:1: name returns [String value] : ( ( String )* ( ID | INT ) ( ID | INT | String )* | String );
    public final Visio_BasicParser.name_return name() throws RecognitionException {
        Visio_BasicParser.name_return retval = new Visio_BasicParser.name_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token String1=null;
        Token set2=null;
        Token set3=null;
        Token String4=null;

        Object String1_tree=null;
        Object set2_tree=null;
        Object set3_tree=null;
        Object String4_tree=null;

        try {
            // BasicParser.g:63:1: ( ( String )* ( ID | INT ) ( ID | INT | String )* | String )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==String) ) {
                int LA3_1 = input.LA(2);

                if ( ((LA3_1>=String && LA3_1<=INT)||LA3_1==ID) ) {
                    alt3=1;
                }
                else if ( (LA3_1==EOF||(LA3_1>=DOT && LA3_1<=DD)||LA3_1==SEMI||LA3_1==92||LA3_1==94||LA3_1==97) ) {
                    alt3=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA3_0==INT||LA3_0==ID) ) {
                alt3=1;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // BasicParser.g:63:3: ( String )* ( ID | INT ) ( ID | INT | String )*
                    {
                    root_0 = adaptor.nil();

                    // BasicParser.g:63:3: ( String )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==String) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // BasicParser.g:63:3: String
                    	    {
                    	    String1=(Token)match(input,String,FOLLOW_String_in_name38); if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {
                    	    String1_tree = adaptor.create(String1);
                    	    adaptor.addChild(root_0, String1_tree);
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);

                    set2=input.LT(1);
                    if ( input.LA(1)==INT||input.LA(1)==ID ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set2));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // BasicParser.g:63:20: ( ID | INT | String )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>=String && LA2_0<=INT)||LA2_0==ID) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // BasicParser.g:
                    	    {
                    	    set3=input.LT(1);
                    	    if ( (input.LA(1)>=String && input.LA(1)<=INT)||input.LA(1)==ID ) {
                    	        input.consume();
                    	        if ( state.backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set3));
                    	        state.errorRecovery=false;state.failed=false;
                    	    }
                    	    else {
                    	        if (state.backtracking>0) {state.failed=true; return retval;}
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);

                    if ( state.backtracking==0 ) {
                      retval.value =input.toString(retval.start,input.LT(-1));
                    }

                    }
                    break;
                case 2 :
                    // BasicParser.g:64:3: String
                    {
                    root_0 = adaptor.nil();

                    String4=(Token)match(input,String,FOLLOW_String_in_name60); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    String4_tree = adaptor.create(String4);
                    adaptor.addChild(root_0, String4_tree);
                    }
                    if ( state.backtracking==0 ) {
                      retval.value =delQuotes((String4!=null?String4.getText():null));
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "name"

    public static class type_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        @Override
		public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // BasicParser.g:66:1: type returns [String value] : SBO ID SBC ;
    public final Visio_BasicParser.type_return type() throws RecognitionException {
        Visio_BasicParser.type_return retval = new Visio_BasicParser.type_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SBO5=null;
        Token ID6=null;
        Token SBC7=null;

        Object SBO5_tree=null;
        Object ID6_tree=null;
        Object SBC7_tree=null;

        try {
            // BasicParser.g:67:1: ( SBO ID SBC )
            // BasicParser.g:67:3: SBO ID SBC
            {
            root_0 = adaptor.nil();

            SBO5=(Token)match(input,SBO,FOLLOW_SBO_in_type74); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SBO5_tree = adaptor.create(SBO5);
            adaptor.addChild(root_0, SBO5_tree);
            }
            ID6=(Token)match(input,ID,FOLLOW_ID_in_type76); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            ID6_tree = adaptor.create(ID6);
            adaptor.addChild(root_0, ID6_tree);
            }
            SBC7=(Token)match(input,SBC,FOLLOW_SBC_in_type78); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            SBC7_tree = adaptor.create(SBC7);
            adaptor.addChild(root_0, SBC7_tree);
            }
            if ( state.backtracking==0 ) {
              retval.value =(ID6!=null?ID6.getText():null);
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "type"

    public static class eq_return extends ParserRuleReturnScope {
        Object tree;
        @Override
		public Object getTree() { return tree; }
    };

    // $ANTLR start "eq"
    // BasicParser.g:69:1: eq : ( EQ | LE | L | GE | G );
    public final Visio_BasicParser.eq_return eq() throws RecognitionException {
        Visio_BasicParser.eq_return retval = new Visio_BasicParser.eq_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set8=null;

        Object set8_tree=null;

        try {
            // BasicParser.g:69:5: ( EQ | LE | L | GE | G )
            // BasicParser.g:
            {
            root_0 = adaptor.nil();

            set8=input.LT(1);
            if ( (input.LA(1)>=LE && input.LA(1)<=EQ) ) {
                input.consume();
                if ( state.backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set8));
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "eq"

    public static class eqncalc_return extends ParserRuleReturnScope {
        Object tree;
        @Override
		public Object getTree() { return tree; }
    };

    // $ANTLR start "eqncalc"
    // BasicParser.g:70:1: eqncalc : ( eq | PLUS EQ | MINUS EQ );
    public final Visio_BasicParser.eqncalc_return eqncalc() throws RecognitionException {
        Visio_BasicParser.eqncalc_return retval = new Visio_BasicParser.eqncalc_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS10=null;
        Token EQ11=null;
        Token MINUS12=null;
        Token EQ13=null;
        Visio_BasicParser.eq_return eq9 = null;


        Object PLUS10_tree=null;
        Object EQ11_tree=null;
        Object MINUS12_tree=null;
        Object EQ13_tree=null;

        try {
            // BasicParser.g:70:9: ( eq | PLUS EQ | MINUS EQ )
            int alt4=3;
            switch ( input.LA(1) ) {
            case LE:
            case L:
            case GE:
            case G:
            case EQ:
                {
                alt4=1;
                }
                break;
            case PLUS:
                {
                alt4=2;
                }
                break;
            case MINUS:
                {
                alt4=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // BasicParser.g:70:11: eq
                    {
                    root_0 = adaptor.nil();

                    pushFollow(FOLLOW_eq_in_eqncalc104);
                    eq9=eq();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, eq9.getTree());

                    }
                    break;
                case 2 :
                    // BasicParser.g:70:14: PLUS EQ
                    {
                    root_0 = adaptor.nil();

                    PLUS10=(Token)match(input,PLUS,FOLLOW_PLUS_in_eqncalc106); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    PLUS10_tree = adaptor.create(PLUS10);
                    adaptor.addChild(root_0, PLUS10_tree);
                    }
                    EQ11=(Token)match(input,EQ,FOLLOW_EQ_in_eqncalc108); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQ11_tree = adaptor.create(EQ11);
                    adaptor.addChild(root_0, EQ11_tree);
                    }

                    }
                    break;
                case 3 :
                    // BasicParser.g:70:22: MINUS EQ
                    {
                    root_0 = adaptor.nil();

                    MINUS12=(Token)match(input,MINUS,FOLLOW_MINUS_in_eqncalc110); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINUS12_tree = adaptor.create(MINUS12);
                    adaptor.addChild(root_0, MINUS12_tree);
                    }
                    EQ13=(Token)match(input,EQ,FOLLOW_EQ_in_eqncalc112); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EQ13_tree = adaptor.create(EQ13);
                    adaptor.addChild(root_0, EQ13_tree);
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "eqncalc"

    public static class d3double_return extends ParserRuleReturnScope {
        public Double value;
        Object tree;
        @Override
		public Object getTree() { return tree; }
    };

    // $ANTLR start "d3double"
    // BasicParser.g:72:1: d3double returns [Double value] : ( MINUS )? INT ( ( COMMA | DOT ) INT )? ;
    public final Visio_BasicParser.d3double_return d3double() throws RecognitionException {
        Visio_BasicParser.d3double_return retval = new Visio_BasicParser.d3double_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS14=null;
        Token INT15=null;
        Token set16=null;
        Token INT17=null;

        Object MINUS14_tree=null;
        Object INT15_tree=null;
        Object set16_tree=null;
        Object INT17_tree=null;

        try {
            // BasicParser.g:73:1: ( ( MINUS )? INT ( ( COMMA | DOT ) INT )? )
            // BasicParser.g:73:3: ( MINUS )? INT ( ( COMMA | DOT ) INT )?
            {
            root_0 = adaptor.nil();

            // BasicParser.g:73:3: ( MINUS )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==MINUS) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // BasicParser.g:73:3: MINUS
                    {
                    MINUS14=(Token)match(input,MINUS,FOLLOW_MINUS_in_d3double124); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    MINUS14_tree = adaptor.create(MINUS14);
                    adaptor.addChild(root_0, MINUS14_tree);
                    }

                    }
                    break;

            }

            INT15=(Token)match(input,INT,FOLLOW_INT_in_d3double127); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
            INT15_tree = adaptor.create(INT15);
            adaptor.addChild(root_0, INT15_tree);
            }
            // BasicParser.g:73:14: ( ( COMMA | DOT ) INT )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==DOT||LA6_0==COMMA) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // BasicParser.g:73:15: ( COMMA | DOT ) INT
                    {
                    set16=input.LT(1);
                    if ( input.LA(1)==DOT||input.LA(1)==COMMA ) {
                        input.consume();
                        if ( state.backtracking==0 ) adaptor.addChild(root_0, adaptor.create(set16));
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    INT17=(Token)match(input,INT,FOLLOW_INT_in_d3double136); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    INT17_tree = adaptor.create(INT17);
                    adaptor.addChild(root_0, INT17_tree);
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              retval.value =parseDouble(input.toString(retval.start,input.LT(-1)));
            }

            }

            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "d3double"

    public static class nameOrDouble_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        @Override
		public Object getTree() { return tree; }
    };

    // $ANTLR start "nameOrDouble"
    // BasicParser.g:75:1: nameOrDouble returns [String value] : ( ( MINUS INT | INT DOT | INT COMMA )=> d3double | name | EX );
    public final Visio_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException {
        Visio_BasicParser.nameOrDouble_return retval = new Visio_BasicParser.nameOrDouble_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token EX20=null;
        Visio_BasicParser.d3double_return d3double18 = null;

        Visio_BasicParser.name_return name19 = null;


        Object EX20_tree=null;

        try {
            // BasicParser.g:76:1: ( ( MINUS INT | INT DOT | INT COMMA )=> d3double | name | EX )
            int alt7=3;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==MINUS) && (synpred1_BasicParser())) {
                alt7=1;
            }
            else if ( (LA7_0==INT) ) {
                int LA7_2 = input.LA(2);

                if ( (synpred1_BasicParser()) ) {
                    alt7=1;
                }
                else if ( (true) ) {
                    alt7=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA7_0==String||LA7_0==ID) ) {
                alt7=2;
            }
            else if ( (LA7_0==EX) ) {
                alt7=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;
            }
            switch (alt7) {
                case 1 :
                    // BasicParser.g:76:2: ( MINUS INT | INT DOT | INT COMMA )=> d3double
                    {
                    root_0 = adaptor.nil();

                    pushFollow(FOLLOW_d3double_in_nameOrDouble170);
                    d3double18=d3double();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, d3double18.getTree());
                    if ( state.backtracking==0 ) {
                      retval.value =(d3double18!=null?d3double18.value:null).toString();
                    }

                    }
                    break;
                case 2 :
                    // BasicParser.g:76:85: name
                    {
                    root_0 = adaptor.nil();

                    pushFollow(FOLLOW_name_in_nameOrDouble175);
                    name19=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) adaptor.addChild(root_0, name19.getTree());
                    if ( state.backtracking==0 ) {
                      retval.value =(name19!=null?name19.value:null);
                    }

                    }
                    break;
                case 3 :
                    // BasicParser.g:76:114: EX
                    {
                    root_0 = adaptor.nil();

                    EX20=(Token)match(input,EX,FOLLOW_EX_in_nameOrDouble181); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                    EX20_tree = adaptor.create(EX20);
                    adaptor.addChild(root_0, EX20_tree);
                    }
                    if ( state.backtracking==0 ) {
                      retval.value =input.toString(retval.start,input.LT(-1));
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            if ( state.backtracking==0 ) {

            retval.tree = adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);
            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "nameOrDouble"

    // $ANTLR start synpred1_BasicParser
    public final void synpred1_BasicParser_fragment() throws RecognitionException {   
        // BasicParser.g:76:2: ( MINUS INT | INT DOT | INT COMMA )
        int alt8=3;
        int LA8_0 = input.LA(1);

        if ( (LA8_0==MINUS) ) {
            alt8=1;
        }
        else if ( (LA8_0==INT) ) {
            int LA8_2 = input.LA(2);

            if ( (LA8_2==DOT) ) {
                alt8=2;
            }
            else if ( (LA8_2==COMMA) ) {
                alt8=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 2, input);

                throw nvae;
            }
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 8, 0, input);

            throw nvae;
        }
        switch (alt8) {
            case 1 :
                // BasicParser.g:76:3: MINUS INT
                {
                match(input,MINUS,FOLLOW_MINUS_in_synpred1_BasicParser152); if (state.failed) return ;
                match(input,INT,FOLLOW_INT_in_synpred1_BasicParser154); if (state.failed) return ;

                }
                break;
            case 2 :
                // BasicParser.g:76:15: INT DOT
                {
                match(input,INT,FOLLOW_INT_in_synpred1_BasicParser158); if (state.failed) return ;
                match(input,DOT,FOLLOW_DOT_in_synpred1_BasicParser160); if (state.failed) return ;

                }
                break;
            case 3 :
                // BasicParser.g:76:25: INT COMMA
                {
                match(input,INT,FOLLOW_INT_in_synpred1_BasicParser164); if (state.failed) return ;
                match(input,COMMA,FOLLOW_COMMA_in_synpred1_BasicParser166); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred1_BasicParser

    // Delegated rules

    public final boolean synpred1_BasicParser() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_BasicParser_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


 

    public static final BitSet FOLLOW_String_in_name38 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_set_in_name41 = new BitSet(new long[]{0x0200000000000032L});
    public static final BitSet FOLLOW_set_in_name47 = new BitSet(new long[]{0x0200000000000032L});
    public static final BitSet FOLLOW_String_in_name60 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_type74 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ID_in_type76 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_type78 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_eq0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_eq_in_eqncalc104 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_eqncalc106 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_eqncalc108 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_eqncalc110 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_eqncalc112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_d3double124 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_d3double127 = new BitSet(new long[]{0x0000000000000142L});
    public static final BitSet FOLLOW_set_in_d3double130 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_d3double136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_d3double_in_nameOrDouble170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_nameOrDouble175 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EX_in_nameOrDouble181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred1_BasicParser152 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_synpred1_BasicParser154 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_synpred1_BasicParser158 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_DOT_in_synpred1_BasicParser160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_synpred1_BasicParser164 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_synpred1_BasicParser166 = new BitSet(new long[]{0x0000000000000002L});

}