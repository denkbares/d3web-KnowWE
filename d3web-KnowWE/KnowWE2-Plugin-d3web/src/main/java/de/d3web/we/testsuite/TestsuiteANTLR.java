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

// $ANTLR 3.1.2 D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g 2009-06-22 22:40:10

package de.d3web.we.testsuite;
import de.d3web.we.testsuite.TestsuiteBuilder;
import de.d3web.KnOfficeParser.ParserErrorHandler;


import org.antlr.runtime.*;

/**
 * Grammatik für Testsuites
 * @author Sebastian Furth
 *
 */
public class TestsuiteANTLR extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "ABSTRACT", "SET", "REF", "FUZZY", "DIVTEXT", "DIVNORM", "ID", "HEURISTIC", "XCL", "Tokens", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73"
    };
    public static final int HIDE=38;
    public static final int RP=16;
    public static final int ORS=12;
    public static final int LP=15;
    public static final int FUZZY=53;
    public static final int ABSTRACT=50;
    public static final int NOT=37;
    public static final int EXCEPT=39;
    public static final int AND=35;
    public static final int ID=56;
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
    public static final int HEURISTIC=57;
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
    public static final int SET=51;
    public static final int INT=5;
    public static final int MINUS=27;
    public static final int DIVNORM=55;
    public static final int Tokens=59;
    public static final int SEMI=9;
    public static final int XCL=58;
    public static final int REF=52;
    public static final int WS=30;
    public static final int OR=36;
    public static final int CBC=18;
    public static final int SBO=19;
    public static final int DIVTEXT=54;
    public static final int DIV=29;
    public static final int CBO=17;
    public static final int LE=21;

    // delegates
    public TestsuiteANTLR_BasicParser gBasicParser;
    // delegators


        public TestsuiteANTLR(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public TestsuiteANTLR(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            gBasicParser = new TestsuiteANTLR_BasicParser(input, state, this);         
        }
        

    public String[] getTokenNames() { return TestsuiteANTLR.tokenNames; }
    public String getGrammarFileName() { return "D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g"; }


      private TestsuiteBuilder builder;
      private ParserErrorHandler eh;
      
      public TestsuiteANTLR(CommonTokenStream tokens, TestsuiteBuilder builder, ParserErrorHandler eh) {
        this(tokens);
        this.builder=builder;
        this.eh=eh;
      }
      
      public void setBuilder(TestsuiteBuilder builder) {
        this.builder=builder;
      }
      
      public TestsuiteBuilder getBuilder() {
        return builder;
      }
      
      private String delQuotes(String s) {
        s=s.substring(1, s.length()-1);
        s=s.replace("\\\"", "\"");
        return s;
      }
      
      @Override
      public void reportError(RecognitionException re) {
        if (eh!=null) {
          eh.parsererror(re);
        } else {
          super.reportError(re);
        }
      }




    // $ANTLR start "knowledge"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:53:1: knowledge : ( sqtestcase )* ;
    public final void knowledge() throws RecognitionException {
        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:54:1: ( ( sqtestcase )* )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:54:3: ( sqtestcase )*
            {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:54:3: ( sqtestcase )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=String && LA1_0<=INT)||LA1_0==ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:54:3: sqtestcase
            	    {
            	    pushFollow(FOLLOW_sqtestcase_in_knowledge51);
            	    sqtestcase();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            builder.createTestSuite();

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
    // $ANTLR end "knowledge"


    // $ANTLR start "sqtestcase"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:56:1: sqtestcase : n= name CBO ( ratedtestcase[i] )+ CBC ;
    public final void sqtestcase() throws RecognitionException {
        TestsuiteANTLR.name_return n = null;


        int i = 0;
        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:58:1: (n= name CBO ( ratedtestcase[i] )+ CBC )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:58:3: n= name CBO ( ratedtestcase[i] )+ CBC
            {
            pushFollow(FOLLOW_name_in_sqtestcase69);
            n=name();

            state._fsp--;

            builder.addSequentialTestCase((n!=null?n.value:null));
            match(input,CBO,FOLLOW_CBO_in_sqtestcase73); 
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:58:57: ( ratedtestcase[i] )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=String && LA2_0<=INT)||LA2_0==ID) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:58:58: ratedtestcase[i]
            	    {
            	    i++;
            	    pushFollow(FOLLOW_ratedtestcase_in_sqtestcase78);
            	    ratedtestcase(i);

            	    state._fsp--;


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

            match(input,CBC,FOLLOW_CBC_in_sqtestcase83); 
            builder.finishCurrentSequentialTestCase();

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
    // $ANTLR end "sqtestcase"

    public static class ratedtestcase_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "ratedtestcase"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:60:1: ratedtestcase[int i] : findings DD solutions SEMI ;
    public final TestsuiteANTLR.ratedtestcase_return ratedtestcase(int i) throws RecognitionException {
        TestsuiteANTLR.ratedtestcase_return retval = new TestsuiteANTLR.ratedtestcase_return();
        retval.start = input.LT(1);

        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:61:1: ( findings DD solutions SEMI )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:61:3: findings DD solutions SEMI
            {
            builder.addRatedTestCase(i, ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));
            pushFollow(FOLLOW_findings_in_ratedtestcase96);
            findings();

            state._fsp--;

            match(input,DD,FOLLOW_DD_in_ratedtestcase98); 
            pushFollow(FOLLOW_solutions_in_ratedtestcase100);
            solutions();

            state._fsp--;

            match(input,SEMI,FOLLOW_SEMI_in_ratedtestcase102); 
            builder.finishCurrentRatedTestCase();

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
    // $ANTLR end "ratedtestcase"

    public static class findings_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "findings"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:63:1: findings : (q= name EQ a= name COMMA )* (q= name EQ a= name ) ;
    public final TestsuiteANTLR.findings_return findings() throws RecognitionException {
        TestsuiteANTLR.findings_return retval = new TestsuiteANTLR.findings_return();
        retval.start = input.LT(1);

        TestsuiteANTLR.name_return q = null;

        TestsuiteANTLR.name_return a = null;


        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:64:1: ( (q= name EQ a= name COMMA )* (q= name EQ a= name ) )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:64:3: (q= name EQ a= name COMMA )* (q= name EQ a= name )
            {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:64:3: (q= name EQ a= name COMMA )*
            loop3:
            do {
                int alt3=2;
                alt3 = dfa3.predict(input);
                switch (alt3) {
            	case 1 :
            	    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:64:4: q= name EQ a= name COMMA
            	    {
            	    pushFollow(FOLLOW_name_in_findings115);
            	    q=name();

            	    state._fsp--;

            	    match(input,EQ,FOLLOW_EQ_in_findings117); 
            	    pushFollow(FOLLOW_name_in_findings121);
            	    a=name();

            	    state._fsp--;

            	    match(input,COMMA,FOLLOW_COMMA_in_findings123); 
            	    builder.addFinding((q!=null?q.value:null), (a!=null?a.value:null), ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:65:3: (q= name EQ a= name )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:65:4: q= name EQ a= name
            {
            pushFollow(FOLLOW_name_in_findings135);
            q=name();

            state._fsp--;

            match(input,EQ,FOLLOW_EQ_in_findings137); 
            pushFollow(FOLLOW_name_in_findings141);
            a=name();

            state._fsp--;

            builder.addFinding((q!=null?q.value:null), (a!=null?a.value:null), ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));

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
    // $ANTLR end "findings"


    // $ANTLR start "solutions"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:67:1: solutions : ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )* ( heuristic_solution | xcl_solution | normalsolution ) ;
    public final void solutions() throws RecognitionException {
        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:1: ( ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )* ( heuristic_solution | xcl_solution | normalsolution ) )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:3: ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )* ( heuristic_solution | xcl_solution | normalsolution )
            {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:3: ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )*
            loop5:
            do {
                int alt5=2;
                alt5 = dfa5.predict(input);
                switch (alt5) {
            	case 1 :
            	    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:4: ( heuristic_solution | xcl_solution | normalsolution ) COMMA
            	    {
            	    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:4: ( heuristic_solution | xcl_solution | normalsolution )
            	    int alt4=3;
            	    alt4 = dfa4.predict(input);
            	    switch (alt4) {
            	        case 1 :
            	            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:5: heuristic_solution
            	            {
            	            pushFollow(FOLLOW_heuristic_solution_in_solutions154);
            	            heuristic_solution();

            	            state._fsp--;


            	            }
            	            break;
            	        case 2 :
            	            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:24: xcl_solution
            	            {
            	            pushFollow(FOLLOW_xcl_solution_in_solutions156);
            	            xcl_solution();

            	            state._fsp--;


            	            }
            	            break;
            	        case 3 :
            	            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:37: normalsolution
            	            {
            	            pushFollow(FOLLOW_normalsolution_in_solutions158);
            	            normalsolution();

            	            state._fsp--;


            	            }
            	            break;

            	    }

            	    match(input,COMMA,FOLLOW_COMMA_in_solutions161); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:61: ( heuristic_solution | xcl_solution | normalsolution )
            int alt6=3;
            alt6 = dfa6.predict(input);
            switch (alt6) {
                case 1 :
                    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:62: heuristic_solution
                    {
                    pushFollow(FOLLOW_heuristic_solution_in_solutions166);
                    heuristic_solution();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:81: xcl_solution
                    {
                    pushFollow(FOLLOW_xcl_solution_in_solutions168);
                    xcl_solution();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:68:94: normalsolution
                    {
                    pushFollow(FOLLOW_normalsolution_in_solutions170);
                    normalsolution();

                    state._fsp--;


                    }
                    break;

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
    // $ANTLR end "solutions"

    public static class heuristic_solution_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "heuristic_solution"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:70:1: heuristic_solution : (n= name ( LP HEURISTIC DD r= name RP ) ) ;
    public final TestsuiteANTLR.heuristic_solution_return heuristic_solution() throws RecognitionException {
        TestsuiteANTLR.heuristic_solution_return retval = new TestsuiteANTLR.heuristic_solution_return();
        retval.start = input.LT(1);

        TestsuiteANTLR.name_return n = null;

        TestsuiteANTLR.name_return r = null;


        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:71:1: ( (n= name ( LP HEURISTIC DD r= name RP ) ) )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:71:3: (n= name ( LP HEURISTIC DD r= name RP ) )
            {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:71:3: (n= name ( LP HEURISTIC DD r= name RP ) )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:71:4: n= name ( LP HEURISTIC DD r= name RP )
            {
            pushFollow(FOLLOW_name_in_heuristic_solution182);
            n=name();

            state._fsp--;

            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:71:11: ( LP HEURISTIC DD r= name RP )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:71:12: LP HEURISTIC DD r= name RP
            {
            match(input,LP,FOLLOW_LP_in_heuristic_solution185); 
            match(input,HEURISTIC,FOLLOW_HEURISTIC_in_heuristic_solution187); 
            match(input,DD,FOLLOW_DD_in_heuristic_solution189); 
            pushFollow(FOLLOW_name_in_heuristic_solution193);
            r=name();

            state._fsp--;

            match(input,RP,FOLLOW_RP_in_heuristic_solution195); 

            }

            builder.addHeuristicSolution((n!=null?n.value:null), (r!=null?r.value:null), ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));

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
    // $ANTLR end "heuristic_solution"

    public static class xcl_solution_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "xcl_solution"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:73:1: xcl_solution : (n= name ( LP XCL DD r= name RP ) ) ;
    public final TestsuiteANTLR.xcl_solution_return xcl_solution() throws RecognitionException {
        TestsuiteANTLR.xcl_solution_return retval = new TestsuiteANTLR.xcl_solution_return();
        retval.start = input.LT(1);

        TestsuiteANTLR.name_return n = null;

        TestsuiteANTLR.name_return r = null;


        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:74:1: ( (n= name ( LP XCL DD r= name RP ) ) )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:74:3: (n= name ( LP XCL DD r= name RP ) )
            {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:74:3: (n= name ( LP XCL DD r= name RP ) )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:74:4: n= name ( LP XCL DD r= name RP )
            {
            pushFollow(FOLLOW_name_in_xcl_solution211);
            n=name();

            state._fsp--;

            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:74:11: ( LP XCL DD r= name RP )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:74:12: LP XCL DD r= name RP
            {
            match(input,LP,FOLLOW_LP_in_xcl_solution214); 
            match(input,XCL,FOLLOW_XCL_in_xcl_solution216); 
            match(input,DD,FOLLOW_DD_in_xcl_solution218); 
            pushFollow(FOLLOW_name_in_xcl_solution222);
            r=name();

            state._fsp--;

            match(input,RP,FOLLOW_RP_in_xcl_solution224); 

            }

            builder.addXCLSolution((n!=null?n.value:null), (r!=null?r.value:null), ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));

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
    // $ANTLR end "xcl_solution"

    public static class normalsolution_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "normalsolution"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:76:1: normalsolution : (n= name ( LP r= name RP ) ) ;
    public final TestsuiteANTLR.normalsolution_return normalsolution() throws RecognitionException {
        TestsuiteANTLR.normalsolution_return retval = new TestsuiteANTLR.normalsolution_return();
        retval.start = input.LT(1);

        TestsuiteANTLR.name_return n = null;

        TestsuiteANTLR.name_return r = null;


        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:77:1: ( (n= name ( LP r= name RP ) ) )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:77:3: (n= name ( LP r= name RP ) )
            {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:77:3: (n= name ( LP r= name RP ) )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:77:4: n= name ( LP r= name RP )
            {
            pushFollow(FOLLOW_name_in_normalsolution239);
            n=name();

            state._fsp--;

            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:77:11: ( LP r= name RP )
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:77:12: LP r= name RP
            {
            match(input,LP,FOLLOW_LP_in_normalsolution242); 
            pushFollow(FOLLOW_name_in_normalsolution246);
            r=name();

            state._fsp--;

            match(input,RP,FOLLOW_RP_in_normalsolution248); 

            }

            builder.addSolution((n!=null?n.value:null), (r!=null?r.value:null), ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));

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
    // $ANTLR end "normalsolution"

    public static class name_return extends ParserRuleReturnScope {
        public String value;
    };

    // $ANTLR start "name"
    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:79:1: name returns [String value] : ( ( ID | INT )+ | String );
    public final TestsuiteANTLR.name_return name() throws RecognitionException {
        TestsuiteANTLR.name_return retval = new TestsuiteANTLR.name_return();
        retval.start = input.LT(1);

        Token String1=null;

        try {
            // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:80:1: ( ( ID | INT )+ | String )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==INT||LA8_0==ID) ) {
                alt8=1;
            }
            else if ( (LA8_0==String) ) {
                alt8=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:80:3: ( ID | INT )+
                    {
                    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:80:3: ( ID | INT )+
                    int cnt7=0;
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);

                        if ( (LA7_0==INT||LA7_0==ID) ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:
                    	    {
                    	    if ( input.LA(1)==INT||input.LA(1)==ID ) {
                    	        input.consume();
                    	        state.errorRecovery=false;
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt7 >= 1 ) break loop7;
                                EarlyExitException eee =
                                    new EarlyExitException(7, input);
                                throw eee;
                        }
                        cnt7++;
                    } while (true);

                    retval.value =input.toString(retval.start,input.LT(-1));

                    }
                    break;
                case 2 :
                    // D:\\Eigene Projekte\\ANTLR\\Grammars\\TestsuiteANTLR.g:81:3: String
                    {
                    String1=(Token)match(input,String,FOLLOW_String_in_name275); 
                    retval.value =delQuotes((String1!=null?String1.getText():null));

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

    // Delegated rules
    public TestsuiteANTLR_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }
    public void eqncalc() throws RecognitionException { gBasicParser.eqncalc(); }
    public void eq() throws RecognitionException { gBasicParser.eq(); }
    public String type() throws RecognitionException { return gBasicParser.type(); }


    protected DFA3 dfa3 = new DFA3(this);
    protected DFA5 dfa5 = new DFA5(this);
    protected DFA4 dfa4 = new DFA4(this);
    protected DFA6 dfa6 = new DFA6(this);
    static final String DFA3_eotS =
        "\10\uffff";
    static final String DFA3_eofS =
        "\10\uffff";
    static final String DFA3_minS =
        "\1\4\1\5\1\31\1\4\1\5\1\7\2\uffff";
    static final String DFA3_maxS =
        "\2\70\1\31\2\70\1\10\2\uffff";
    static final String DFA3_acceptS =
        "\6\uffff\1\2\1\1";
    static final String DFA3_specialS =
        "\10\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\2\1\1\62\uffff\1\1",
            "\1\1\23\uffff\1\3\36\uffff\1\1",
            "\1\3",
            "\1\5\1\4\62\uffff\1\4",
            "\1\4\1\uffff\1\6\1\7\57\uffff\1\4",
            "\1\6\1\7",
            "",
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
            return "()* loopback of 64:3: (q= name EQ a= name COMMA )*";
        }
    }
    static final String DFA5_eotS =
        "\23\uffff";
    static final String DFA5_eofS =
        "\23\uffff";
    static final String DFA5_minS =
        "\1\4\1\5\1\17\1\4\2\7\1\5\1\20\2\4\1\10\1\5\1\20\1\5\1\20\2\uffff"+
        "\2\10";
    static final String DFA5_maxS =
        "\2\70\1\17\1\72\2\7\1\70\1\20\2\70\1\11\1\70\1\20\1\70\1\20\2\uffff"+
        "\2\11";
    static final String DFA5_acceptS =
        "\17\uffff\1\1\1\2\2\uffff";
    static final String DFA5_specialS =
        "\23\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\2\1\1\62\uffff\1\1",
            "\1\1\11\uffff\1\3\50\uffff\1\1",
            "\1\3",
            "\1\7\1\6\62\uffff\1\6\1\5\1\4",
            "\1\10",
            "\1\11",
            "\1\6\12\uffff\1\12\47\uffff\1\6",
            "\1\12",
            "\1\14\1\13\62\uffff\1\13",
            "\1\16\1\15\62\uffff\1\15",
            "\1\17\1\20",
            "\1\13\12\uffff\1\21\47\uffff\1\13",
            "\1\21",
            "\1\15\12\uffff\1\22\47\uffff\1\15",
            "\1\22",
            "",
            "",
            "\1\17\1\20",
            "\1\17\1\20"
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
            return "()* loopback of 68:3: ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )*";
        }
    }
    static final String DFA4_eotS =
        "\7\uffff";
    static final String DFA4_eofS =
        "\7\uffff";
    static final String DFA4_minS =
        "\1\4\1\5\1\17\1\4\3\uffff";
    static final String DFA4_maxS =
        "\2\70\1\17\1\72\3\uffff";
    static final String DFA4_acceptS =
        "\4\uffff\1\2\1\1\1\3";
    static final String DFA4_specialS =
        "\7\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\2\1\1\62\uffff\1\1",
            "\1\1\11\uffff\1\3\50\uffff\1\1",
            "\1\3",
            "\2\6\62\uffff\1\6\1\5\1\4",
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "68:4: ( heuristic_solution | xcl_solution | normalsolution )";
        }
    }
    static final String DFA6_eotS =
        "\7\uffff";
    static final String DFA6_eofS =
        "\7\uffff";
    static final String DFA6_minS =
        "\1\4\1\5\1\17\1\4\3\uffff";
    static final String DFA6_maxS =
        "\2\70\1\17\1\72\3\uffff";
    static final String DFA6_acceptS =
        "\4\uffff\1\2\1\1\1\3";
    static final String DFA6_specialS =
        "\7\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\2\1\1\62\uffff\1\1",
            "\1\1\11\uffff\1\3\50\uffff\1\1",
            "\1\3",
            "\2\6\62\uffff\1\6\1\5\1\4",
            "",
            "",
            ""
    };

    static final short[] DFA6_eot = DFA.unpackEncodedString(DFA6_eotS);
    static final short[] DFA6_eof = DFA.unpackEncodedString(DFA6_eofS);
    static final char[] DFA6_min = DFA.unpackEncodedStringToUnsignedChars(DFA6_minS);
    static final char[] DFA6_max = DFA.unpackEncodedStringToUnsignedChars(DFA6_maxS);
    static final short[] DFA6_accept = DFA.unpackEncodedString(DFA6_acceptS);
    static final short[] DFA6_special = DFA.unpackEncodedString(DFA6_specialS);
    static final short[][] DFA6_transition;

    static {
        int numStates = DFA6_transitionS.length;
        DFA6_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA6_transition[i] = DFA.unpackEncodedString(DFA6_transitionS[i]);
        }
    }

    class DFA6 extends DFA {

        public DFA6(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 6;
            this.eot = DFA6_eot;
            this.eof = DFA6_eof;
            this.min = DFA6_min;
            this.max = DFA6_max;
            this.accept = DFA6_accept;
            this.special = DFA6_special;
            this.transition = DFA6_transition;
        }
        public String getDescription() {
            return "68:61: ( heuristic_solution | xcl_solution | normalsolution )";
        }
    }
 

    public static final BitSet FOLLOW_sqtestcase_in_knowledge51 = new BitSet(new long[]{0x0100000000000032L});
    public static final BitSet FOLLOW_name_in_sqtestcase69 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_sqtestcase73 = new BitSet(new long[]{0x0100000002000030L});
    public static final BitSet FOLLOW_ratedtestcase_in_sqtestcase78 = new BitSet(new long[]{0x0100000002040030L});
    public static final BitSet FOLLOW_CBC_in_sqtestcase83 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_findings_in_ratedtestcase96 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_ratedtestcase98 = new BitSet(new long[]{0x0100000000008030L});
    public static final BitSet FOLLOW_solutions_in_ratedtestcase100 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_ratedtestcase102 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_findings115 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_findings117 = new BitSet(new long[]{0x0100000000000130L});
    public static final BitSet FOLLOW_name_in_findings121 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_findings123 = new BitSet(new long[]{0x0100000002000030L});
    public static final BitSet FOLLOW_name_in_findings135 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_findings137 = new BitSet(new long[]{0x0100000000000030L});
    public static final BitSet FOLLOW_name_in_findings141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_heuristic_solution_in_solutions154 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_xcl_solution_in_solutions156 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_normalsolution_in_solutions158 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_solutions161 = new BitSet(new long[]{0x0100000000008030L});
    public static final BitSet FOLLOW_heuristic_solution_in_solutions166 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xcl_solution_in_solutions168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_normalsolution_in_solutions170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_heuristic_solution182 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_heuristic_solution185 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_HEURISTIC_in_heuristic_solution187 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_heuristic_solution189 = new BitSet(new long[]{0x0100000000010030L});
    public static final BitSet FOLLOW_name_in_heuristic_solution193 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_heuristic_solution195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_xcl_solution211 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_xcl_solution214 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_XCL_in_xcl_solution216 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_xcl_solution218 = new BitSet(new long[]{0x0100000000010030L});
    public static final BitSet FOLLOW_name_in_xcl_solution222 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_xcl_solution224 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_normalsolution239 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_normalsolution242 = new BitSet(new long[]{0x0100000000010030L});
    public static final BitSet FOLLOW_name_in_normalsolution246 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_normalsolution248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_name264 = new BitSet(new long[]{0x0100000000000022L});
    public static final BitSet FOLLOW_String_in_name275 = new BitSet(new long[]{0x0000000000000002L});

}