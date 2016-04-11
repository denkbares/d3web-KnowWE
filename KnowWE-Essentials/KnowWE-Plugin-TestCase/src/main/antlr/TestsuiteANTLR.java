// $ANTLR 3.1.1 D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g 2010-08-18 21:19:38

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

/**
 * Grammatik für Testsuites
 * @author Sebastian Furth
 *
 */
public class TestsuiteANTLR extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "INIT", "ABSTRACT", "SET", "REF", "FUZZY", "DIVTEXT", "DIVNORM", "ID", "HEURISTIC", "XCL", "Tokens", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75"
    };
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
    public String getGrammarFileName() { return "D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g"; }


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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:93:1: knowledge : ( sqtestcase )* ;
    public final void knowledge() throws RecognitionException {
        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:94:1: ( ( sqtestcase )* )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:94:3: ( sqtestcase )*
            {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:94:3: ( sqtestcase )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=String && LA1_0<=INT)||LA1_0==MINUS||LA1_0==ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:94:3: sqtestcase
            	    {
            	    pushFollow(FOLLOW_sqtestcase_in_knowledge54);
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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:96:1: sqtestcase : n= name CBO ( ratedtestcase[i] )+ CBC ;
    public final void sqtestcase() throws RecognitionException {
        TestsuiteANTLR.name_return n = null;


        int i = 0;
        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:98:1: (n= name CBO ( ratedtestcase[i] )+ CBC )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:98:3: n= name CBO ( ratedtestcase[i] )+ CBC
            {
            pushFollow(FOLLOW_name_in_sqtestcase72);
            n=name();

            state._fsp--;

            builder.addSequentialTestCase((n!=null?n.value:null));
            match(input,CBO,FOLLOW_CBO_in_sqtestcase76); 
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:98:57: ( ratedtestcase[i] )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=String && LA2_0<=INT)||LA2_0==MINUS||LA2_0==ID) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:98:58: ratedtestcase[i]
            	    {
            	    i++;
            	    pushFollow(FOLLOW_ratedtestcase_in_sqtestcase81);
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

            match(input,CBC,FOLLOW_CBC_in_sqtestcase86); 
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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:100:1: ratedtestcase[int i] : findings DD ( solutions )? SEMI ;
    public final TestsuiteANTLR.ratedtestcase_return ratedtestcase(int i) throws RecognitionException {
        TestsuiteANTLR.ratedtestcase_return retval = new TestsuiteANTLR.ratedtestcase_return();
        retval.start = input.LT(1);

        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:101:1: ( findings DD ( solutions )? SEMI )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:101:3: findings DD ( solutions )? SEMI
            {
            builder.addRatedTestCase(i, ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));
            pushFollow(FOLLOW_findings_in_ratedtestcase99);
            findings();

            state._fsp--;

            match(input,DD,FOLLOW_DD_in_ratedtestcase101); 
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:101:71: ( solutions )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( ((LA3_0>=String && LA3_0<=INT)||LA3_0==MINUS||LA3_0==ID) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:101:71: solutions
                    {
                    pushFollow(FOLLOW_solutions_in_ratedtestcase103);
                    solutions();

                    state._fsp--;


                    }
                    break;

            }

            match(input,SEMI,FOLLOW_SEMI_in_ratedtestcase106); 
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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:103:1: findings : (q= name EQ a= name COMMA )* (q= name EQ a= name ) ;
    public final TestsuiteANTLR.findings_return findings() throws RecognitionException {
        TestsuiteANTLR.findings_return retval = new TestsuiteANTLR.findings_return();
        retval.start = input.LT(1);

        TestsuiteANTLR.name_return q = null;

        TestsuiteANTLR.name_return a = null;


        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:104:1: ( (q= name EQ a= name COMMA )* (q= name EQ a= name ) )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:104:3: (q= name EQ a= name COMMA )* (q= name EQ a= name )
            {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:104:3: (q= name EQ a= name COMMA )*
            loop4:
            do {
                int alt4=2;
                alt4 = dfa4.predict(input);
                switch (alt4) {
            	case 1 :
            	    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:104:4: q= name EQ a= name COMMA
            	    {
            	    pushFollow(FOLLOW_name_in_findings119);
            	    q=name();

            	    state._fsp--;

            	    match(input,EQ,FOLLOW_EQ_in_findings121); 
            	    pushFollow(FOLLOW_name_in_findings125);
            	    a=name();

            	    state._fsp--;

            	    match(input,COMMA,FOLLOW_COMMA_in_findings127); 
            	    builder.addFinding((q!=null?q.value:null), (a!=null?a.value:null), ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);

            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:105:3: (q= name EQ a= name )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:105:4: q= name EQ a= name
            {
            pushFollow(FOLLOW_name_in_findings139);
            q=name();

            state._fsp--;

            match(input,EQ,FOLLOW_EQ_in_findings141); 
            pushFollow(FOLLOW_name_in_findings145);
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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:107:1: solutions : ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )* ( heuristic_solution | xcl_solution | normalsolution ) ;
    public final void solutions() throws RecognitionException {
        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:1: ( ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )* ( heuristic_solution | xcl_solution | normalsolution ) )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:3: ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )* ( heuristic_solution | xcl_solution | normalsolution )
            {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:3: ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )*
            loop6:
            do {
                int alt6=2;
                alt6 = dfa6.predict(input);
                switch (alt6) {
            	case 1 :
            	    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:4: ( heuristic_solution | xcl_solution | normalsolution ) COMMA
            	    {
            	    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:4: ( heuristic_solution | xcl_solution | normalsolution )
            	    int alt5=3;
            	    alt5 = dfa5.predict(input);
            	    switch (alt5) {
            	        case 1 :
            	            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:5: heuristic_solution
            	            {
            	            pushFollow(FOLLOW_heuristic_solution_in_solutions158);
            	            heuristic_solution();

            	            state._fsp--;


            	            }
            	            break;
            	        case 2 :
            	            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:24: xcl_solution
            	            {
            	            pushFollow(FOLLOW_xcl_solution_in_solutions160);
            	            xcl_solution();

            	            state._fsp--;


            	            }
            	            break;
            	        case 3 :
            	            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:37: normalsolution
            	            {
            	            pushFollow(FOLLOW_normalsolution_in_solutions162);
            	            normalsolution();

            	            state._fsp--;


            	            }
            	            break;

            	    }

            	    match(input,COMMA,FOLLOW_COMMA_in_solutions165); 

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:61: ( heuristic_solution | xcl_solution | normalsolution )
            int alt7=3;
            alt7 = dfa7.predict(input);
            switch (alt7) {
                case 1 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:62: heuristic_solution
                    {
                    pushFollow(FOLLOW_heuristic_solution_in_solutions170);
                    heuristic_solution();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:81: xcl_solution
                    {
                    pushFollow(FOLLOW_xcl_solution_in_solutions172);
                    xcl_solution();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:108:94: normalsolution
                    {
                    pushFollow(FOLLOW_normalsolution_in_solutions174);
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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:110:1: heuristic_solution : (n= name ( LP HEURISTIC DD r= name RP ) ) ;
    public final TestsuiteANTLR.heuristic_solution_return heuristic_solution() throws RecognitionException {
        TestsuiteANTLR.heuristic_solution_return retval = new TestsuiteANTLR.heuristic_solution_return();
        retval.start = input.LT(1);

        TestsuiteANTLR.name_return n = null;

        TestsuiteANTLR.name_return r = null;


        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:111:1: ( (n= name ( LP HEURISTIC DD r= name RP ) ) )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:111:3: (n= name ( LP HEURISTIC DD r= name RP ) )
            {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:111:3: (n= name ( LP HEURISTIC DD r= name RP ) )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:111:4: n= name ( LP HEURISTIC DD r= name RP )
            {
            pushFollow(FOLLOW_name_in_heuristic_solution186);
            n=name();

            state._fsp--;

            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:111:11: ( LP HEURISTIC DD r= name RP )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:111:12: LP HEURISTIC DD r= name RP
            {
            match(input,LP,FOLLOW_LP_in_heuristic_solution189); 
            match(input,HEURISTIC,FOLLOW_HEURISTIC_in_heuristic_solution191); 
            match(input,DD,FOLLOW_DD_in_heuristic_solution193); 
            pushFollow(FOLLOW_name_in_heuristic_solution197);
            r=name();

            state._fsp--;

            match(input,RP,FOLLOW_RP_in_heuristic_solution199); 

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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:113:1: xcl_solution : (n= name ( LP XCL DD r= name RP ) ) ;
    public final TestsuiteANTLR.xcl_solution_return xcl_solution() throws RecognitionException {
        TestsuiteANTLR.xcl_solution_return retval = new TestsuiteANTLR.xcl_solution_return();
        retval.start = input.LT(1);

        TestsuiteANTLR.name_return n = null;

        TestsuiteANTLR.name_return r = null;


        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:114:1: ( (n= name ( LP XCL DD r= name RP ) ) )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:114:3: (n= name ( LP XCL DD r= name RP ) )
            {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:114:3: (n= name ( LP XCL DD r= name RP ) )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:114:4: n= name ( LP XCL DD r= name RP )
            {
            pushFollow(FOLLOW_name_in_xcl_solution215);
            n=name();

            state._fsp--;

            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:114:11: ( LP XCL DD r= name RP )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:114:12: LP XCL DD r= name RP
            {
            match(input,LP,FOLLOW_LP_in_xcl_solution218); 
            match(input,XCL,FOLLOW_XCL_in_xcl_solution220); 
            match(input,DD,FOLLOW_DD_in_xcl_solution222); 
            pushFollow(FOLLOW_name_in_xcl_solution226);
            r=name();

            state._fsp--;

            match(input,RP,FOLLOW_RP_in_xcl_solution228); 

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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:116:1: normalsolution : (n= name ( LP r= name RP ) ) ;
    public final TestsuiteANTLR.normalsolution_return normalsolution() throws RecognitionException {
        TestsuiteANTLR.normalsolution_return retval = new TestsuiteANTLR.normalsolution_return();
        retval.start = input.LT(1);

        TestsuiteANTLR.name_return n = null;

        TestsuiteANTLR.name_return r = null;


        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:117:1: ( (n= name ( LP r= name RP ) ) )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:117:3: (n= name ( LP r= name RP ) )
            {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:117:3: (n= name ( LP r= name RP ) )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:117:4: n= name ( LP r= name RP )
            {
            pushFollow(FOLLOW_name_in_normalsolution243);
            n=name();

            state._fsp--;

            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:117:11: ( LP r= name RP )
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:117:12: LP r= name RP
            {
            match(input,LP,FOLLOW_LP_in_normalsolution246); 
            pushFollow(FOLLOW_name_in_normalsolution250);
            r=name();

            state._fsp--;

            match(input,RP,FOLLOW_RP_in_normalsolution252); 

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
    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:119:1: name returns [String value] : ( ( ID | d3double )+ | String );
    public final TestsuiteANTLR.name_return name() throws RecognitionException {
        TestsuiteANTLR.name_return retval = new TestsuiteANTLR.name_return();
        retval.start = input.LT(1);

        Token String1=null;

        try {
            // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:120:1: ( ( ID | d3double )+ | String )
            int alt9=2;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==INT||LA9_0==MINUS||LA9_0==ID) ) {
                alt9=1;
            }
            else if ( (LA9_0==String) ) {
                alt9=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:120:3: ( ID | d3double )+
                    {
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:120:3: ( ID | d3double )+
                    int cnt8=0;
                    loop8:
                    do {
                        int alt8=3;
                        int LA8_0 = input.LA(1);

                        if ( (LA8_0==ID) ) {
                            alt8=1;
                        }
                        else if ( (LA8_0==INT||LA8_0==MINUS) ) {
                            alt8=2;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:120:4: ID
                    	    {
                    	    match(input,ID,FOLLOW_ID_in_name269); 

                    	    }
                    	    break;
                    	case 2 :
                    	    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:120:7: d3double
                    	    {
                    	    pushFollow(FOLLOW_d3double_in_name271);
                    	    d3double();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    if ( cnt8 >= 1 ) break loop8;
                                EarlyExitException eee =
                                    new EarlyExitException(8, input);
                                throw eee;
                        }
                        cnt8++;
                    } while (true);

                    retval.value =input.toString(retval.start,input.LT(-1));

                    }
                    break;
                case 2 :
                    // D:\\Projects\\denkbares\\workspace\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\TestsuiteANTLR.g:121:3: String
                    {
                    String1=(Token)match(input,String,FOLLOW_String_in_name279); 
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
    public String type() throws RecognitionException { return gBasicParser.type(); }
    public TestsuiteANTLR_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }
    public TestsuiteANTLR_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException { return gBasicParser.nameOrDouble(); }
    public void eqncalc() throws RecognitionException { gBasicParser.eqncalc(); }
    public void eq() throws RecognitionException { gBasicParser.eq(); }


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA6 dfa6 = new DFA6(this);
    protected DFA5 dfa5 = new DFA5(this);
    protected DFA7 dfa7 = new DFA7(this);
    static final String DFA4_eotS =
        "\31\uffff";
    static final String DFA4_eofS =
        "\31\uffff";
    static final String DFA4_minS =
        "\1\4\3\5\1\31\1\4\4\5\1\7\1\5\2\uffff\1\4\6\5\1\4\3\5";
    static final String DFA4_maxS =
        "\2\71\1\5\1\71\1\31\1\71\1\5\1\71\1\5\1\71\1\10\1\71\2\uffff\1"+
        "\71\1\5\3\71\1\5\2\71\1\5\2\71";
    static final String DFA4_acceptS =
        "\14\uffff\1\1\1\2\13\uffff";
    static final String DFA4_specialS =
        "\31\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\4\1\3\25\uffff\1\2\35\uffff\1\1",
            "\1\3\23\uffff\1\5\1\uffff\1\2\35\uffff\1\1",
            "\1\3",
            "\1\3\1\6\1\uffff\1\6\20\uffff\1\5\1\uffff\1\2\35\uffff\1\1",
            "\1\5",
            "\1\12\1\11\25\uffff\1\10\35\uffff\1\7",
            "\1\13",
            "\1\11\1\uffff\1\15\1\14\22\uffff\1\10\35\uffff\1\7",
            "\1\11",
            "\1\11\1\17\1\15\1\16\22\uffff\1\10\35\uffff\1\7",
            "\1\15\1\14",
            "\1\3\23\uffff\1\5\1\uffff\1\2\35\uffff\1\1",
            "",
            "",
            "\1\14\1\20\25\uffff\1\14\35\uffff\1\14",
            "\1\21",
            "\1\24\1\14\1\15\1\14\20\uffff\1\14\1\uffff\1\23\35\uffff\1"+
            "\22",
            "\1\11\1\uffff\1\15\1\14\22\uffff\1\10\35\uffff\1\7",
            "\1\24\1\uffff\1\15\1\14\20\uffff\1\14\1\uffff\1\23\35\uffff"+
            "\1\22",
            "\1\24",
            "\1\24\1\26\1\15\1\25\20\uffff\1\14\1\uffff\1\23\35\uffff\1"+
            "\22",
            "\1\14\1\27\25\uffff\1\14\35\uffff\1\14",
            "\1\30",
            "\1\24\1\14\1\15\1\14\20\uffff\1\14\1\uffff\1\23\35\uffff\1"+
            "\22",
            "\1\24\1\uffff\1\15\1\14\20\uffff\1\14\1\uffff\1\23\35\uffff"+
            "\1\22"
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
            return "()* loopback of 104:3: (q= name EQ a= name COMMA )*";
        }
    }
    static final String DFA6_eotS =
        "\43\uffff";
    static final String DFA6_eofS =
        "\43\uffff";
    static final String DFA6_minS =
        "\1\4\3\5\1\17\1\4\1\5\2\7\3\5\1\20\1\5\2\4\1\10\4\5\1\20\3\5\1"+
        "\20\2\uffff\1\5\1\10\1\5\1\10\3\5";
    static final String DFA6_maxS =
        "\2\71\1\5\1\71\1\17\1\73\1\5\2\7\1\71\1\5\1\71\1\20\3\71\1\11\1"+
        "\5\1\71\1\5\1\71\1\20\1\71\1\5\1\71\1\20\2\uffff\1\71\1\11\1\5\1"+
        "\11\1\5\2\71";
    static final String DFA6_acceptS =
        "\32\uffff\1\2\1\1\7\uffff";
    static final String DFA6_specialS =
        "\43\uffff}>";
    static final String[] DFA6_transitionS = {
            "\1\4\1\3\25\uffff\1\2\35\uffff\1\1",
            "\1\3\11\uffff\1\5\13\uffff\1\2\35\uffff\1\1",
            "\1\3",
            "\1\3\1\6\1\uffff\1\6\6\uffff\1\5\13\uffff\1\2\35\uffff\1\1",
            "\1\5",
            "\1\14\1\13\25\uffff\1\12\35\uffff\1\11\1\10\1\7",
            "\1\15",
            "\1\16",
            "\1\17",
            "\1\13\12\uffff\1\20\12\uffff\1\12\35\uffff\1\11",
            "\1\13",
            "\1\13\1\21\1\uffff\1\21\7\uffff\1\20\12\uffff\1\12\35\uffff"+
            "\1\11",
            "\1\20",
            "\1\3\11\uffff\1\5\13\uffff\1\2\35\uffff\1\1",
            "\1\25\1\24\25\uffff\1\23\35\uffff\1\22",
            "\1\31\1\30\25\uffff\1\27\35\uffff\1\26",
            "\1\33\1\32",
            "\1\34",
            "\1\24\12\uffff\1\35\12\uffff\1\23\35\uffff\1\22",
            "\1\24",
            "\1\24\1\36\1\uffff\1\36\7\uffff\1\35\12\uffff\1\23\35\uffff"+
            "\1\22",
            "\1\35",
            "\1\30\12\uffff\1\37\12\uffff\1\27\35\uffff\1\26",
            "\1\30",
            "\1\30\1\40\1\uffff\1\40\7\uffff\1\37\12\uffff\1\27\35\uffff"+
            "\1\26",
            "\1\37",
            "",
            "",
            "\1\13\12\uffff\1\20\12\uffff\1\12\35\uffff\1\11",
            "\1\33\1\32",
            "\1\41",
            "\1\33\1\32",
            "\1\42",
            "\1\24\12\uffff\1\35\12\uffff\1\23\35\uffff\1\22",
            "\1\30\12\uffff\1\37\12\uffff\1\27\35\uffff\1\26"
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
            return "()* loopback of 108:3: ( ( heuristic_solution | xcl_solution | normalsolution ) COMMA )*";
        }
    }
    static final String DFA5_eotS =
        "\13\uffff";
    static final String DFA5_eofS =
        "\13\uffff";
    static final String DFA5_minS =
        "\1\4\3\5\1\17\1\4\1\5\3\uffff\1\5";
    static final String DFA5_maxS =
        "\2\71\1\5\1\71\1\17\1\73\1\5\3\uffff\1\71";
    static final String DFA5_acceptS =
        "\7\uffff\1\2\1\1\1\3\1\uffff";
    static final String DFA5_specialS =
        "\13\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\4\1\3\25\uffff\1\2\35\uffff\1\1",
            "\1\3\11\uffff\1\5\13\uffff\1\2\35\uffff\1\1",
            "\1\3",
            "\1\3\1\6\1\uffff\1\6\6\uffff\1\5\13\uffff\1\2\35\uffff\1\1",
            "\1\5",
            "\2\11\25\uffff\1\11\35\uffff\1\11\1\10\1\7",
            "\1\12",
            "",
            "",
            "",
            "\1\3\11\uffff\1\5\13\uffff\1\2\35\uffff\1\1"
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
            return "108:4: ( heuristic_solution | xcl_solution | normalsolution )";
        }
    }
    static final String DFA7_eotS =
        "\13\uffff";
    static final String DFA7_eofS =
        "\13\uffff";
    static final String DFA7_minS =
        "\1\4\3\5\1\17\1\4\1\5\3\uffff\1\5";
    static final String DFA7_maxS =
        "\2\71\1\5\1\71\1\17\1\73\1\5\3\uffff\1\71";
    static final String DFA7_acceptS =
        "\7\uffff\1\1\1\2\1\3\1\uffff";
    static final String DFA7_specialS =
        "\13\uffff}>";
    static final String[] DFA7_transitionS = {
            "\1\4\1\3\25\uffff\1\2\35\uffff\1\1",
            "\1\3\11\uffff\1\5\13\uffff\1\2\35\uffff\1\1",
            "\1\3",
            "\1\3\1\6\1\uffff\1\6\6\uffff\1\5\13\uffff\1\2\35\uffff\1\1",
            "\1\5",
            "\2\11\25\uffff\1\11\35\uffff\1\11\1\7\1\10",
            "\1\12",
            "",
            "",
            "",
            "\1\3\11\uffff\1\5\13\uffff\1\2\35\uffff\1\1"
    };

    static final short[] DFA7_eot = DFA.unpackEncodedString(DFA7_eotS);
    static final short[] DFA7_eof = DFA.unpackEncodedString(DFA7_eofS);
    static final char[] DFA7_min = DFA.unpackEncodedStringToUnsignedChars(DFA7_minS);
    static final char[] DFA7_max = DFA.unpackEncodedStringToUnsignedChars(DFA7_maxS);
    static final short[] DFA7_accept = DFA.unpackEncodedString(DFA7_acceptS);
    static final short[] DFA7_special = DFA.unpackEncodedString(DFA7_specialS);
    static final short[][] DFA7_transition;

    static {
        int numStates = DFA7_transitionS.length;
        DFA7_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA7_transition[i] = DFA.unpackEncodedString(DFA7_transitionS[i]);
        }
    }

    class DFA7 extends DFA {

        public DFA7(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 7;
            this.eot = DFA7_eot;
            this.eof = DFA7_eof;
            this.min = DFA7_min;
            this.max = DFA7_max;
            this.accept = DFA7_accept;
            this.special = DFA7_special;
            this.transition = DFA7_transition;
        }
        public String getDescription() {
            return "108:61: ( heuristic_solution | xcl_solution | normalsolution )";
        }
    }
 

    public static final BitSet FOLLOW_sqtestcase_in_knowledge54 = new BitSet(new long[]{0x0200000008000032L});
    public static final BitSet FOLLOW_name_in_sqtestcase72 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_sqtestcase76 = new BitSet(new long[]{0x020000000A000030L});
    public static final BitSet FOLLOW_ratedtestcase_in_sqtestcase81 = new BitSet(new long[]{0x020000000A040030L});
    public static final BitSet FOLLOW_CBC_in_sqtestcase86 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_findings_in_ratedtestcase99 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_ratedtestcase101 = new BitSet(new long[]{0x0200000008008230L});
    public static final BitSet FOLLOW_solutions_in_ratedtestcase103 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_ratedtestcase106 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_findings119 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_findings121 = new BitSet(new long[]{0x0200000008000130L});
    public static final BitSet FOLLOW_name_in_findings125 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_findings127 = new BitSet(new long[]{0x020000000A000030L});
    public static final BitSet FOLLOW_name_in_findings139 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_findings141 = new BitSet(new long[]{0x0200000008000030L});
    public static final BitSet FOLLOW_name_in_findings145 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_heuristic_solution_in_solutions158 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_xcl_solution_in_solutions160 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_normalsolution_in_solutions162 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_solutions165 = new BitSet(new long[]{0x0200000008008030L});
    public static final BitSet FOLLOW_heuristic_solution_in_solutions170 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_xcl_solution_in_solutions172 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_normalsolution_in_solutions174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_heuristic_solution186 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_heuristic_solution189 = new BitSet(new long[]{0x0400000000000000L});
    public static final BitSet FOLLOW_HEURISTIC_in_heuristic_solution191 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_heuristic_solution193 = new BitSet(new long[]{0x0200000008010030L});
    public static final BitSet FOLLOW_name_in_heuristic_solution197 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_heuristic_solution199 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_xcl_solution215 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_xcl_solution218 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_XCL_in_xcl_solution220 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_xcl_solution222 = new BitSet(new long[]{0x0200000008010030L});
    public static final BitSet FOLLOW_name_in_xcl_solution226 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_xcl_solution228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_normalsolution243 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_normalsolution246 = new BitSet(new long[]{0x0200000008010030L});
    public static final BitSet FOLLOW_name_in_normalsolution250 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_normalsolution252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_name269 = new BitSet(new long[]{0x0200000008000022L});
    public static final BitSet FOLLOW_d3double_in_name271 = new BitSet(new long[]{0x0200000008000022L});
    public static final BitSet FOLLOW_String_in_name279 = new BitSet(new long[]{0x0000000000000002L});

}