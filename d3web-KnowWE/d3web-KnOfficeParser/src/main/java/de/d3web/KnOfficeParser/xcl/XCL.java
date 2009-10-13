// $ANTLR 3.2 Sep 23, 2009 12:02:23 D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g 2009-10-13 18:05:38

package de.d3web.KnOfficeParser.xcl;
import de.d3web.KnOfficeParser.ConditionBuilder;
import de.d3web.KnOfficeParser.ParserErrorHandler;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/**
 * Grammatik fuer XCL Grammatiken
 * @author Markus Friedrich
 *
 */
public class XCL extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "INIT", "ABSTRACT", "SET", "REF", "FUZZY", "DIVTEXT", "DIVNORM", "ID", "BLUB", "Tokens", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87"
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
    public XCL_ComplexCondition_BasicParser gBasicParser;
    public XCL_ComplexCondition gComplexCondition;
    // delegators


        public XCL(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public XCL(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            gComplexCondition = new XCL_ComplexCondition(input, state, this);         
            gBasicParser = gComplexCondition.gBasicParser;
        }
        

    public String[] getTokenNames() { return XCL.tokenNames; }
    public String getGrammarFileName() { return "D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g"; }


      private XCLBuilder builder;
      private ParserErrorHandler eh;
      
      public XCL(CommonTokenStream tokens, XCLBuilder builder, ParserErrorHandler eh, ConditionBuilder cb) {
        this(tokens);
        this.builder=builder;
        this.eh=eh;
        gComplexCondition.setEH(eh);
        eh.setTokenNames(tokenNames);
        gComplexCondition.setBuilder(cb);
      }
      
      public void setBuilder(XCLBuilder builder) {
        this.builder=builder;
      }
      
      public XCLBuilder getBuilder() {
        return builder;
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
    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:48:1: knowledge : ( solutiondescription )* ;
    public final void knowledge() throws RecognitionException {
        try {
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:49:1: ( ( solutiondescription )* )
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:49:3: ( solutiondescription )*
            {
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:49:3: ( solutiondescription )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=String && LA1_0<=INT)||LA1_0==ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:49:3: solutiondescription
            	    {
            	    pushFollow(FOLLOW_solutiondescription_in_knowledge45);
            	    solutiondescription();

            	    state._fsp--;


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


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

    public static class solutiondescription_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "solutiondescription"
    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:51:1: solutiondescription : name CBO ( finding COMMA )+ CBC ( SBO thr ( COMMA thr )* SBC )? ;
    public final XCL.solutiondescription_return solutiondescription() throws RecognitionException {
        XCL.solutiondescription_return retval = new XCL.solutiondescription_return();
        retval.start = input.LT(1);

        XCL_ComplexCondition_BasicParser.name_return name1 = null;


        try {
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:52:1: ( name CBO ( finding COMMA )+ CBC ( SBO thr ( COMMA thr )* SBC )? )
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:52:3: name CBO ( finding COMMA )+ CBC ( SBO thr ( COMMA thr )* SBC )?
            {
            pushFollow(FOLLOW_name_in_solutiondescription54);
            name1=name();

            state._fsp--;

            builder.solution(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name1!=null?name1.value:null));
            match(input,CBO,FOLLOW_CBO_in_solutiondescription58); 
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:52:70: ( finding COMMA )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>=String && LA2_0<=INT)||LA2_0==LP||LA2_0==NOT||(LA2_0>=UNKNOWN && LA2_0<=KNOWN)||LA2_0==MINMAX||LA2_0==ID) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:52:72: finding COMMA
            	    {
            	    pushFollow(FOLLOW_finding_in_solutiondescription62);
            	    finding();

            	    state._fsp--;

            	    match(input,COMMA,FOLLOW_COMMA_in_solutiondescription64); 

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

            match(input,CBC,FOLLOW_CBC_in_solutiondescription68); 
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:52:92: ( SBO thr ( COMMA thr )* SBC )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==SBO) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:52:93: SBO thr ( COMMA thr )* SBC
                    {
                    match(input,SBO,FOLLOW_SBO_in_solutiondescription71); 
                    pushFollow(FOLLOW_thr_in_solutiondescription73);
                    thr();

                    state._fsp--;

                    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:52:101: ( COMMA thr )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==COMMA) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:52:102: COMMA thr
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_solutiondescription76); 
                    	    pushFollow(FOLLOW_thr_in_solutiondescription78);
                    	    thr();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);

                    match(input,SBC,FOLLOW_SBC_in_solutiondescription82); 

                    }
                    break;

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
    // $ANTLR end "solutiondescription"


    // $ANTLR start "finding"
    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:54:1: finding : complexcondition ( weight )? ;
    public final void finding() throws RecognitionException {
        XCL.weight_return weight2 = null;


        try {
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:54:9: ( complexcondition ( weight )? )
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:54:11: complexcondition ( weight )?
            {
            pushFollow(FOLLOW_complexcondition_in_finding92);
            complexcondition();

            state._fsp--;

            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:54:28: ( weight )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==SBO) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:54:28: weight
                    {
                    pushFollow(FOLLOW_weight_in_finding94);
                    weight2=weight();

                    state._fsp--;


                    }
                    break;

            }

            builder.finding((weight2!=null?input.toString(weight2.start,weight2.stop):null));

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
    // $ANTLR end "finding"

    public static class weight_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "weight"
    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:56:1: weight : SBO ( PLUS PLUS | MINUS MINUS | EX | INT ) SBC ;
    public final XCL.weight_return weight() throws RecognitionException {
        XCL.weight_return retval = new XCL.weight_return();
        retval.start = input.LT(1);

        try {
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:57:1: ( SBO ( PLUS PLUS | MINUS MINUS | EX | INT ) SBC )
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:57:3: SBO ( PLUS PLUS | MINUS MINUS | EX | INT ) SBC
            {
            match(input,SBO,FOLLOW_SBO_in_weight105); 
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:57:7: ( PLUS PLUS | MINUS MINUS | EX | INT )
            int alt6=4;
            switch ( input.LA(1) ) {
            case PLUS:
                {
                alt6=1;
                }
                break;
            case MINUS:
                {
                alt6=2;
                }
                break;
            case EX:
                {
                alt6=3;
                }
                break;
            case INT:
                {
                alt6=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:57:8: PLUS PLUS
                    {
                    match(input,PLUS,FOLLOW_PLUS_in_weight108); 
                    match(input,PLUS,FOLLOW_PLUS_in_weight110); 

                    }
                    break;
                case 2 :
                    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:57:18: MINUS MINUS
                    {
                    match(input,MINUS,FOLLOW_MINUS_in_weight112); 
                    match(input,MINUS,FOLLOW_MINUS_in_weight114); 

                    }
                    break;
                case 3 :
                    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:57:30: EX
                    {
                    match(input,EX,FOLLOW_EX_in_weight116); 

                    }
                    break;
                case 4 :
                    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:57:33: INT
                    {
                    match(input,INT,FOLLOW_INT_in_weight118); 

                    }
                    break;

            }

            match(input,SBC,FOLLOW_SBC_in_weight121); 

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
    // $ANTLR end "weight"

    public static class thr_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "thr"
    // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:59:1: thr : name EQ d3double ;
    public final XCL.thr_return thr() throws RecognitionException {
        XCL.thr_return retval = new XCL.thr_return();
        retval.start = input.LT(1);

        XCL_ComplexCondition_BasicParser.name_return name3 = null;

        XCL_ComplexCondition_BasicParser.d3double_return d3double4 = null;


        try {
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:59:4: ( name EQ d3double )
            // D:\\Workspaces\\KnowWE\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:59:6: name EQ d3double
            {
            pushFollow(FOLLOW_name_in_thr128);
            name3=name();

            state._fsp--;

            match(input,EQ,FOLLOW_EQ_in_thr130); 
            pushFollow(FOLLOW_d3double_in_thr132);
            d3double4=d3double();

            state._fsp--;

            builder.threshold(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name3!=null?name3.value:null), (d3double4!=null?d3double4.value:null));

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
    // $ANTLR end "thr"

    // Delegated rules
    public void startruleComplexCondition() throws RecognitionException { gComplexCondition.startruleComplexCondition(); }
    public void eqncalc() throws RecognitionException { gBasicParser.eqncalc(); }
    public XCL_ComplexCondition.condition_return condition() throws RecognitionException { return gComplexCondition.condition(); }
    public XCL_ComplexCondition.disjunct_return disjunct() throws RecognitionException { return gComplexCondition.disjunct(); }
    public XCL_ComplexCondition_BasicParser.eq_return eq() throws RecognitionException { return gBasicParser.eq(); }
    public XCL_ComplexCondition.complexcondition_return complexcondition() throws RecognitionException { return gComplexCondition.complexcondition(); }
    public XCL_ComplexCondition.conjunct_return conjunct() throws RecognitionException { return gComplexCondition.conjunct(); }
    public String type() throws RecognitionException { return gBasicParser.type(); }
    public XCL_ComplexCondition_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException { return gBasicParser.nameOrDouble(); }
    public XCL_ComplexCondition.dnf_return dnf() throws RecognitionException { return gComplexCondition.dnf(); }
    public XCL_ComplexCondition.intervall_return intervall() throws RecognitionException { return gComplexCondition.intervall(); }
    public XCL_ComplexCondition_BasicParser.name_return name() throws RecognitionException { return gBasicParser.name(); }
    public XCL_ComplexCondition_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }


 

    public static final BitSet FOLLOW_solutiondescription_in_knowledge45 = new BitSet(new long[]{0x0200000000000032L});
    public static final BitSet FOLLOW_name_in_solutiondescription54 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_solutiondescription58 = new BitSet(new long[]{0x02005B2003E88030L});
    public static final BitSet FOLLOW_finding_in_solutiondescription62 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_solutiondescription64 = new BitSet(new long[]{0x02005B2003EC8030L});
    public static final BitSet FOLLOW_CBC_in_solutiondescription68 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_SBO_in_solutiondescription71 = new BitSet(new long[]{0x0200000002000030L});
    public static final BitSet FOLLOW_thr_in_solutiondescription73 = new BitSet(new long[]{0x0000000000100100L});
    public static final BitSet FOLLOW_COMMA_in_solutiondescription76 = new BitSet(new long[]{0x0200000002000030L});
    public static final BitSet FOLLOW_thr_in_solutiondescription78 = new BitSet(new long[]{0x0000000000100100L});
    public static final BitSet FOLLOW_SBC_in_solutiondescription82 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexcondition_in_finding92 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_weight_in_finding94 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_weight105 = new BitSet(new long[]{0x000000000C000420L});
    public static final BitSet FOLLOW_PLUS_in_weight108 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_PLUS_in_weight110 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_MINUS_in_weight112 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_MINUS_in_weight114 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_EX_in_weight116 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_INT_in_weight118 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_weight121 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_thr128 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_thr130 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_thr132 = new BitSet(new long[]{0x0000000000000002L});

}