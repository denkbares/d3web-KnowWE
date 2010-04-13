// $ANTLR 3.1.1 D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g 2010-04-06 16:16:26

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
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "INIT", "ABSTRACT", "SET", "REF", "FUZZY", "DIVTEXT", "DIVNORM", "ID", "BLUB", "Tokens", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88"
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
    public String getGrammarFileName() { return "D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g"; }


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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:68:1: knowledge : ( solutiondescription )* ;
    public final void knowledge() throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:69:1: ( ( solutiondescription )* )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:69:3: ( solutiondescription )*
            {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:69:3: ( solutiondescription )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=String && LA1_0<=INT)||LA1_0==ID) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:69:3: solutiondescription
            	    {
            	    pushFollow(FOLLOW_solutiondescription_in_knowledge48);
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:71:1: solutiondescription : name CBO ( finding COMMA )+ CBC ( SBO thr ( COMMA thr )* SBC )? ;
    public final XCL.solutiondescription_return solutiondescription() throws RecognitionException {
        XCL.solutiondescription_return retval = new XCL.solutiondescription_return();
        retval.start = input.LT(1);

        XCL_ComplexCondition_BasicParser.name_return name1 = null;


        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:72:1: ( name CBO ( finding COMMA )+ CBC ( SBO thr ( COMMA thr )* SBC )? )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:72:3: name CBO ( finding COMMA )+ CBC ( SBO thr ( COMMA thr )* SBC )?
            {
            pushFollow(FOLLOW_name_in_solutiondescription57);
            name1=name();

            state._fsp--;

            builder.solution(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (name1!=null?name1.value:null));
            match(input,CBO,FOLLOW_CBO_in_solutiondescription61); 
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:72:70: ( finding COMMA )+
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
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:72:72: finding COMMA
            	    {
            	    pushFollow(FOLLOW_finding_in_solutiondescription65);
            	    finding();

            	    state._fsp--;

            	    match(input,COMMA,FOLLOW_COMMA_in_solutiondescription67); 

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

            match(input,CBC,FOLLOW_CBC_in_solutiondescription71); 
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:72:92: ( SBO thr ( COMMA thr )* SBC )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==SBO) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:72:93: SBO thr ( COMMA thr )* SBC
                    {
                    match(input,SBO,FOLLOW_SBO_in_solutiondescription74); 
                    pushFollow(FOLLOW_thr_in_solutiondescription76);
                    thr();

                    state._fsp--;

                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:72:101: ( COMMA thr )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==COMMA) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:72:102: COMMA thr
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_solutiondescription79); 
                    	    pushFollow(FOLLOW_thr_in_solutiondescription81);
                    	    thr();

                    	    state._fsp--;


                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);

                    match(input,SBC,FOLLOW_SBC_in_solutiondescription85); 

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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:74:1: finding : complexcondition ( weight )? ;
    public final void finding() throws RecognitionException {
        XCL.weight_return weight2 = null;


        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:74:9: ( complexcondition ( weight )? )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:74:11: complexcondition ( weight )?
            {
            pushFollow(FOLLOW_complexcondition_in_finding95);
            complexcondition();

            state._fsp--;

            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:74:28: ( weight )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==SBO) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:74:28: weight
                    {
                    pushFollow(FOLLOW_weight_in_finding97);
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:76:1: weight : SBO ( PLUS PLUS | MINUS MINUS | EX | INT ) SBC ;
    public final XCL.weight_return weight() throws RecognitionException {
        XCL.weight_return retval = new XCL.weight_return();
        retval.start = input.LT(1);

        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:77:1: ( SBO ( PLUS PLUS | MINUS MINUS | EX | INT ) SBC )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:77:3: SBO ( PLUS PLUS | MINUS MINUS | EX | INT ) SBC
            {
            match(input,SBO,FOLLOW_SBO_in_weight108); 
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:77:7: ( PLUS PLUS | MINUS MINUS | EX | INT )
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
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:77:8: PLUS PLUS
                    {
                    match(input,PLUS,FOLLOW_PLUS_in_weight111); 
                    match(input,PLUS,FOLLOW_PLUS_in_weight113); 

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:77:18: MINUS MINUS
                    {
                    match(input,MINUS,FOLLOW_MINUS_in_weight115); 
                    match(input,MINUS,FOLLOW_MINUS_in_weight117); 

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:77:30: EX
                    {
                    match(input,EX,FOLLOW_EX_in_weight119); 

                    }
                    break;
                case 4 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:77:33: INT
                    {
                    match(input,INT,FOLLOW_INT_in_weight121); 

                    }
                    break;

            }

            match(input,SBC,FOLLOW_SBC_in_weight124); 

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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:79:1: thr : name EQ d3double ;
    public final XCL.thr_return thr() throws RecognitionException {
        XCL.thr_return retval = new XCL.thr_return();
        retval.start = input.LT(1);

        XCL_ComplexCondition_BasicParser.name_return name3 = null;

        XCL_ComplexCondition_BasicParser.d3double_return d3double4 = null;


        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:79:4: ( name EQ d3double )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\XCL.g:79:6: name EQ d3double
            {
            pushFollow(FOLLOW_name_in_thr131);
            name3=name();

            state._fsp--;

            match(input,EQ,FOLLOW_EQ_in_thr133); 
            pushFollow(FOLLOW_d3double_in_thr135);
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
    public XCL_ComplexCondition.conjunct_return conjunct() throws RecognitionException { return gComplexCondition.conjunct(); }
    public void eqncalc() throws RecognitionException { gBasicParser.eqncalc(); }
    public XCL_ComplexCondition.disjunct_return disjunct() throws RecognitionException { return gComplexCondition.disjunct(); }
    public XCL_ComplexCondition_BasicParser.eq_return eq() throws RecognitionException { return gBasicParser.eq(); }
    public XCL_ComplexCondition_BasicParser.name_return name() throws RecognitionException { return gBasicParser.name(); }
    public XCL_ComplexCondition.dnf_return dnf() throws RecognitionException { return gComplexCondition.dnf(); }
    public XCL_ComplexCondition.complexcondition_return complexcondition() throws RecognitionException { return gComplexCondition.complexcondition(); }
    public XCL_ComplexCondition.intervall_return intervall() throws RecognitionException { return gComplexCondition.intervall(); }
    public String type() throws RecognitionException { return gBasicParser.type(); }
    public XCL_ComplexCondition.condition_return condition() throws RecognitionException { return gComplexCondition.condition(); }
    public XCL_ComplexCondition_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException { return gBasicParser.nameOrDouble(); }
    public void startruleComplexCondition() throws RecognitionException { gComplexCondition.startruleComplexCondition(); }
    public XCL_ComplexCondition_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }


 

    public static final BitSet FOLLOW_solutiondescription_in_knowledge48 = new BitSet(new long[]{0x0200000000000032L});
    public static final BitSet FOLLOW_name_in_solutiondescription57 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_solutiondescription61 = new BitSet(new long[]{0x02005B2003E88030L});
    public static final BitSet FOLLOW_finding_in_solutiondescription65 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_solutiondescription67 = new BitSet(new long[]{0x02005B2003EC8030L});
    public static final BitSet FOLLOW_CBC_in_solutiondescription71 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_SBO_in_solutiondescription74 = new BitSet(new long[]{0x0200000002000030L});
    public static final BitSet FOLLOW_thr_in_solutiondescription76 = new BitSet(new long[]{0x0000000000100100L});
    public static final BitSet FOLLOW_COMMA_in_solutiondescription79 = new BitSet(new long[]{0x0200000002000030L});
    public static final BitSet FOLLOW_thr_in_solutiondescription81 = new BitSet(new long[]{0x0000000000100100L});
    public static final BitSet FOLLOW_SBC_in_solutiondescription85 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_complexcondition_in_finding95 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_weight_in_finding97 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_weight108 = new BitSet(new long[]{0x000000000C000420L});
    public static final BitSet FOLLOW_PLUS_in_weight111 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_PLUS_in_weight113 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_MINUS_in_weight115 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_MINUS_in_weight117 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_EX_in_weight119 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_INT_in_weight121 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_weight124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_thr131 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_thr133 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_thr135 = new BitSet(new long[]{0x0000000000000002L});

}