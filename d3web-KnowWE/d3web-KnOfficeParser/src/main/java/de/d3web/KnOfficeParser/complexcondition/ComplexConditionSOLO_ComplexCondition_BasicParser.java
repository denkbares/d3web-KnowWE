// $ANTLR 3.1.1 BasicParser.g 2010-02-22 09:40:01

package de.d3web.KnOfficeParser.complexcondition;
import de.d3web.KnOfficeParser.ParserErrorHandler;
import org.antlr.runtime.*;
public class ComplexConditionSOLO_ComplexCondition_BasicParser extends Parser {
    public static final int HIDE=38;
    public static final int RP=16;
    public static final int ORS=12;
    public static final int LP=15;
    public static final int FUZZY=54;
    public static final int ABSTRACT=51;
    public static final int NOT=37;
    public static final int EXCEPT=39;
    public static final int AND=35;
    public static final int ID=57;
    public static final int DD=7;
    public static final int EOF=-1;
    public static final int IF=33;
    public static final int AT=11;
    public static final int THEN=34;
    public static final int IN=44;
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
    public static final int MINUS=27;
    public static final int DIVNORM=56;
    public static final int Tokens=59;
    public static final int SEMI=9;
    public static final int REF=53;
    public static final int WS=30;
    public static final int BLUB=58;
    public static final int OR=36;
    public static final int CBC=18;
    public static final int SBO=19;
    public static final int DIVTEXT=55;
    public static final int DIV=29;
    public static final int INIT=50;
    public static final int CBO=17;
    public static final int LE=21;

    // delegates
    // delegators
    public ComplexConditionSOLO gComplexConditionSOLO;
    public ComplexConditionSOLO_ComplexCondition gComplexCondition;
    public ComplexConditionSOLO_ComplexCondition gParent;


        public ComplexConditionSOLO_ComplexCondition_BasicParser(TokenStream input, ComplexConditionSOLO gComplexConditionSOLO, ComplexConditionSOLO_ComplexCondition gComplexCondition) {
            this(input, new RecognizerSharedState(), gComplexConditionSOLO, gComplexCondition);
        }
        public ComplexConditionSOLO_ComplexCondition_BasicParser(TokenStream input, RecognizerSharedState state, ComplexConditionSOLO gComplexConditionSOLO, ComplexConditionSOLO_ComplexCondition gComplexCondition) {
            super(input, state);
            this.gComplexConditionSOLO = gComplexConditionSOLO;
            this.gComplexCondition = gComplexCondition;
             
            gParent = gComplexCondition;
        }
        

    @Override
	public String[] getTokenNames() { return ComplexConditionSOLO.tokenNames; }
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
    };

    // $ANTLR start "name"
    // BasicParser.g:62:1: name returns [String value] : ( ( String )* ( ID | INT ) ( ID | INT | String )* | String );
    public final ComplexConditionSOLO_ComplexCondition_BasicParser.name_return name() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition_BasicParser.name_return retval = new ComplexConditionSOLO_ComplexCondition_BasicParser.name_return();
        retval.start = input.LT(1);

        Token String1=null;

        try {
            // BasicParser.g:63:1: ( ( String )* ( ID | INT ) ( ID | INT | String )* | String )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==String) ) {
                int LA3_1 = input.LA(2);

                if ( (LA3_1==EOF||(LA3_1>=COMMA && LA3_1<=SEMI)||LA3_1==ORS||LA3_1==RP||(LA3_1>=CBC && LA3_1<=EQ)||(LA3_1>=AND && LA3_1<=OR)||LA3_1==IN||LA3_1==ALL) ) {
                    alt3=2;
                }
                else if ( ((LA3_1>=String && LA3_1<=INT)||LA3_1==ID) ) {
                    alt3=1;
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
                    	    match(input,String,FOLLOW_String_in_name38); if (state.failed) return retval;

                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);

                    if ( input.LA(1)==INT||input.LA(1)==ID ) {
                        input.consume();
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
                    	    if ( (input.LA(1)>=String && input.LA(1)<=INT)||input.LA(1)==ID ) {
                    	        input.consume();
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
                    String1=(Token)match(input,String,FOLLOW_String_in_name60); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      retval.value =delQuotes((String1!=null?String1.getText():null));
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "name"


    // $ANTLR start "type"
    // BasicParser.g:66:1: type returns [String value] : SBO ID SBC ;
    public final String type() throws RecognitionException {
        String value = null;

        Token ID2=null;

        try {
            // BasicParser.g:67:1: ( SBO ID SBC )
            // BasicParser.g:67:3: SBO ID SBC
            {
            match(input,SBO,FOLLOW_SBO_in_type74); if (state.failed) return value;
            ID2=(Token)match(input,ID,FOLLOW_ID_in_type76); if (state.failed) return value;
            match(input,SBC,FOLLOW_SBC_in_type78); if (state.failed) return value;
            if ( state.backtracking==0 ) {
              value =(ID2!=null?ID2.getText():null);
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "type"

    public static class eq_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "eq"
    // BasicParser.g:69:1: eq : ( EQ | LE | L | GE | G );
    public final ComplexConditionSOLO_ComplexCondition_BasicParser.eq_return eq() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition_BasicParser.eq_return retval = new ComplexConditionSOLO_ComplexCondition_BasicParser.eq_return();
        retval.start = input.LT(1);

        try {
            // BasicParser.g:69:5: ( EQ | LE | L | GE | G )
            // BasicParser.g:
            {
            if ( (input.LA(1)>=LE && input.LA(1)<=EQ) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "eq"


    // $ANTLR start "eqncalc"
    // BasicParser.g:70:1: eqncalc : ( eq | PLUS EQ | MINUS EQ );
    public final void eqncalc() throws RecognitionException {
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
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // BasicParser.g:70:11: eq
                    {
                    pushFollow(FOLLOW_eq_in_eqncalc104);
                    eq();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // BasicParser.g:70:14: PLUS EQ
                    {
                    match(input,PLUS,FOLLOW_PLUS_in_eqncalc106); if (state.failed) return ;
                    match(input,EQ,FOLLOW_EQ_in_eqncalc108); if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // BasicParser.g:70:22: MINUS EQ
                    {
                    match(input,MINUS,FOLLOW_MINUS_in_eqncalc110); if (state.failed) return ;
                    match(input,EQ,FOLLOW_EQ_in_eqncalc112); if (state.failed) return ;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "eqncalc"

    public static class d3double_return extends ParserRuleReturnScope {
        public Double value;
    };

    // $ANTLR start "d3double"
    // BasicParser.g:72:1: d3double returns [Double value] : ( MINUS )? INT ( ( COMMA | DOT ) INT )? ;
    public final ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return d3double() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return retval = new ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return();
        retval.start = input.LT(1);

        try {
            // BasicParser.g:73:1: ( ( MINUS )? INT ( ( COMMA | DOT ) INT )? )
            // BasicParser.g:73:3: ( MINUS )? INT ( ( COMMA | DOT ) INT )?
            {
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
                    match(input,MINUS,FOLLOW_MINUS_in_d3double124); if (state.failed) return retval;

                    }
                    break;

            }

            match(input,INT,FOLLOW_INT_in_d3double127); if (state.failed) return retval;
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
                    if ( input.LA(1)==DOT||input.LA(1)==COMMA ) {
                        input.consume();
                        state.errorRecovery=false;state.failed=false;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    match(input,INT,FOLLOW_INT_in_d3double136); if (state.failed) return retval;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              retval.value =parseDouble(input.toString(retval.start,input.LT(-1)));
            }

            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "d3double"

    public static class nameOrDouble_return extends ParserRuleReturnScope {
        public String value;
    };

    // $ANTLR start "nameOrDouble"
    // BasicParser.g:75:1: nameOrDouble returns [String value] : ( ( MINUS INT | INT DOT | INT COMMA )=> d3double | name | EX );
    public final ComplexConditionSOLO_ComplexCondition_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition_BasicParser.nameOrDouble_return retval = new ComplexConditionSOLO_ComplexCondition_BasicParser.nameOrDouble_return();
        retval.start = input.LT(1);

        ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return d3double3 = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.name_return name4 = null;


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
                    pushFollow(FOLLOW_d3double_in_nameOrDouble170);
                    d3double3=d3double();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      retval.value =(d3double3!=null?d3double3.value:null).toString();
                    }

                    }
                    break;
                case 2 :
                    // BasicParser.g:76:85: name
                    {
                    pushFollow(FOLLOW_name_in_nameOrDouble175);
                    name4=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      retval.value =(name4!=null?name4.value:null);
                    }

                    }
                    break;
                case 3 :
                    // BasicParser.g:76:114: EX
                    {
                    match(input,EX,FOLLOW_EX_in_nameOrDouble181); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      retval.value =input.toString(retval.start,input.LT(-1));
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
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