// $ANTLR 3.1.1 BasicParser.g 2010-08-18 21:19:38

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
import de.d3web.we.testsuite.TestsuiteBuilder;
import de.d3web.KnOfficeParser.ParserErrorHandler;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class TestsuiteANTLR_BasicParser extends Parser {
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
    public static final int MINUS=27;
    public static final int DIVNORM=56;
    public static final int Tokens=60;
    public static final int SEMI=9;
    public static final int XCL=59;
    public static final int REF=53;
    public static final int WS=30;
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
    public TestsuiteANTLR gTestsuiteANTLR;
    public TestsuiteANTLR gParent;


        public TestsuiteANTLR_BasicParser(TokenStream input, TestsuiteANTLR gTestsuiteANTLR) {
            this(input, new RecognizerSharedState(), gTestsuiteANTLR);
        }
        public TestsuiteANTLR_BasicParser(TokenStream input, RecognizerSharedState state, TestsuiteANTLR gTestsuiteANTLR) {
            super(input, state);
            this.gTestsuiteANTLR = gTestsuiteANTLR;
             
            gParent = gTestsuiteANTLR;
        }
        

    public String[] getTokenNames() { return TestsuiteANTLR.tokenNames; }
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



    // $ANTLR start "type"
    // BasicParser.g:66:1: type returns [String value] : SBO ID SBC ;
    public final String type() throws RecognitionException {
        String value = null;

        Token ID1=null;

        try {
            // BasicParser.g:67:1: ( SBO ID SBC )
            // BasicParser.g:67:3: SBO ID SBC
            {
            match(input,SBO,FOLLOW_SBO_in_type74); if (state.failed) return value;
            ID1=(Token)match(input,ID,FOLLOW_ID_in_type76); if (state.failed) return value;
            match(input,SBC,FOLLOW_SBC_in_type78); if (state.failed) return value;
            if ( state.backtracking==0 ) {
              value =(ID1!=null?ID1.getText():null);
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


    // $ANTLR start "eq"
    // BasicParser.g:69:1: eq : ( EQ | LE | L | GE | G );
    public final void eq() throws RecognitionException {
        try {
            // BasicParser.g:69:5: ( EQ | LE | L | GE | G )
            // BasicParser.g:
            {
            if ( (input.LA(1)>=LE && input.LA(1)<=EQ) ) {
                input.consume();
                state.errorRecovery=false;state.failed=false;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return ;}
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


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
    public final TestsuiteANTLR_BasicParser.d3double_return d3double() throws RecognitionException {
        TestsuiteANTLR_BasicParser.d3double_return retval = new TestsuiteANTLR_BasicParser.d3double_return();
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

            if ( (LA6_0==COMMA) ) {
                int LA6_1 = input.LA(2);

                if ( (LA6_1==INT) ) {
                    alt6=1;
                }
            }
            else if ( (LA6_0==DOT) ) {
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
    public final TestsuiteANTLR_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException {
        TestsuiteANTLR_BasicParser.nameOrDouble_return retval = new TestsuiteANTLR_BasicParser.nameOrDouble_return();
        retval.start = input.LT(1);

        TestsuiteANTLR_BasicParser.d3double_return d3double2 = null;

        TestsuiteANTLR.name_return name3 = null;


        try {
            // BasicParser.g:76:1: ( ( MINUS INT | INT DOT | INT COMMA )=> d3double | name | EX )
            int alt7=3;
            switch ( input.LA(1) ) {
            case MINUS:
                {
                int LA7_1 = input.LA(2);

                if ( (LA7_1==INT) ) {
                    int LA7_2 = input.LA(3);

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
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;
                }
                }
                break;
            case INT:
                {
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
                break;
            case String:
            case ID:
                {
                alt7=2;
                }
                break;
            case EX:
                {
                alt7=3;
                }
                break;
            default:
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
                    d3double2=d3double();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      retval.value =(d3double2!=null?d3double2.value:null).toString();
                    }

                    }
                    break;
                case 2 :
                    // BasicParser.g:76:85: name
                    {
                    pushFollow(FOLLOW_name_in_nameOrDouble175);
                    name3=gTestsuiteANTLR.name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      retval.value =(name3!=null?name3.value:null);
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