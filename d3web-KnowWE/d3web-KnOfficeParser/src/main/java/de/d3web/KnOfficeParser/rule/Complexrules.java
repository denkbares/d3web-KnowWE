// $ANTLR 3.1 D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g 2009-09-30 15:26:04

package de.d3web.KnOfficeParser.rule;
import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
/**
 * Grammatik für komplexe Regeln
 * @author Markus Friedrich
 *
 */
public class Complexrules extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "INIT", "ABSTRACT", "SET", "REF", "FUZZY", "DIVTEXT", "DIVNORM", "ID", "BLUB", "Tokens", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86"
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
    public Complexrules_ComplexCondition_BasicParser gBasicParser;
    public Complexrules_ComplexCondition gComplexCondition;
    // delegators


        public Complexrules(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public Complexrules(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            gComplexCondition = new Complexrules_ComplexCondition(input, state, this);         
            gBasicParser = gComplexCondition.gBasicParser;
        }
        

    public String[] getTokenNames() { return Complexrules.tokenNames; }
    public String getGrammarFileName() { return "D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g"; }


      private RuleBuilder builder;
      private ParserErrorHandler eh;
      
      public Complexrules(CommonTokenStream tokens, RuleBuilder builder, ParserErrorHandler eh, ConditionBuilder cb) {
        this(tokens);
        this.builder=builder;
        this.eh=eh;
        gComplexCondition.setEH(eh);
        if (eh!=null) eh.setTokenNames(tokenNames);
        gComplexCondition.setBuilder(cb);
      }
      
      public void setBuilder(RuleBuilder builder) {
        this.builder = builder;
      }
      
      public RuleBuilder getBuilder() {
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
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:48:1: knowledge : ( complexrule )* ;
    public final void knowledge() throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:49:1: ( ( complexrule )* )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:49:3: ( complexrule )*
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:49:3: ( complexrule )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==IF) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:49:3: complexrule
            	    {
            	    pushFollow(FOLLOW_complexrule_in_knowledge49);
            	    complexrule();

            	    state._fsp--;
            	    if (state.failed) return ;

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


    // $ANTLR start "complexrule"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:51:1: complexrule : IF complexcondition (a= EXCEPT complexcondition ( NL )? )? THEN ruleaction[($a!=null)] ;
    public final void complexrule() throws RecognitionException {
        Token a=null;

        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:52:1: ( IF complexcondition (a= EXCEPT complexcondition ( NL )? )? THEN ruleaction[($a!=null)] )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:52:3: IF complexcondition (a= EXCEPT complexcondition ( NL )? )? THEN ruleaction[($a!=null)]
            {
            match(input,IF,FOLLOW_IF_in_complexrule58); if (state.failed) return ;
            pushFollow(FOLLOW_complexcondition_in_complexrule60);
            complexcondition();

            state._fsp--;
            if (state.failed) return ;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:52:23: (a= EXCEPT complexcondition ( NL )? )?
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==EXCEPT) ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:52:24: a= EXCEPT complexcondition ( NL )?
                    {
                    a=(Token)match(input,EXCEPT,FOLLOW_EXCEPT_in_complexrule65); if (state.failed) return ;
                    pushFollow(FOLLOW_complexcondition_in_complexrule67);
                    complexcondition();

                    state._fsp--;
                    if (state.failed) return ;
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:52:50: ( NL )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);

                    if ( (LA2_0==NL) ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:52:50: NL
                            {
                            match(input,NL,FOLLOW_NL_in_complexrule69); if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }

            match(input,THEN,FOLLOW_THEN_in_complexrule74); if (state.failed) return ;
            pushFollow(FOLLOW_ruleaction_in_complexrule76);
            ruleaction((a!=null));

            state._fsp--;
            if (state.failed) return ;

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
    // $ANTLR end "complexrule"


    // $ANTLR start "ruleaction"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:54:1: ruleaction[boolean except] : ( indicationrule[except] | suppressrule[except] | abstractionrule[except] );
    public final void ruleaction(boolean except) throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:55:1: ( indicationrule[except] | suppressrule[except] | abstractionrule[except] )
            int alt4=3;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:55:3: indicationrule[except]
                    {
                    pushFollow(FOLLOW_indicationrule_in_ruleaction86);
                    indicationrule(except);

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:55:28: suppressrule[except]
                    {
                    pushFollow(FOLLOW_suppressrule_in_ruleaction91);
                    suppressrule(except);

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:55:51: abstractionrule[except]
                    {
                    pushFollow(FOLLOW_abstractionrule_in_ruleaction96);
                    abstractionrule(except);

                    state._fsp--;
                    if (state.failed) return ;

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
    // $ANTLR end "ruleaction"

    public static class indicationrule_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "indicationrule"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:57:1: indicationrule[boolean except] : (n= names | a= INSTANT SBO n= names SBC | b= NOT SBO n= names SBC ) ;
    public final Complexrules.indicationrule_return indicationrule(boolean except) throws RecognitionException {
        Complexrules.indicationrule_return retval = new Complexrules.indicationrule_return();
        retval.start = input.LT(1);

        Token a=null;
        Token b=null;
        Complexrules.names_return n = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:58:1: ( (n= names | a= INSTANT SBO n= names SBC | b= NOT SBO n= names SBC ) )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:58:4: (n= names | a= INSTANT SBO n= names SBC | b= NOT SBO n= names SBC )
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:58:4: (n= names | a= INSTANT SBO n= names SBC | b= NOT SBO n= names SBC )
            int alt5=3;
            switch ( input.LA(1) ) {
            case String:
            case INT:
            case ID:
                {
                alt5=1;
                }
                break;
            case INSTANT:
                {
                alt5=2;
                }
                break;
            case NOT:
                {
                alt5=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:58:5: n= names
                    {
                    pushFollow(FOLLOW_names_in_indicationrule111);
                    n=names();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:58:15: a= INSTANT SBO n= names SBC
                    {
                    a=(Token)match(input,INSTANT,FOLLOW_INSTANT_in_indicationrule117); if (state.failed) return retval;
                    match(input,SBO,FOLLOW_SBO_in_indicationrule119); if (state.failed) return retval;
                    pushFollow(FOLLOW_names_in_indicationrule123);
                    n=names();

                    state._fsp--;
                    if (state.failed) return retval;
                    match(input,SBC,FOLLOW_SBC_in_indicationrule125); if (state.failed) return retval;

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:58:43: b= NOT SBO n= names SBC
                    {
                    b=(Token)match(input,NOT,FOLLOW_NOT_in_indicationrule131); if (state.failed) return retval;
                    match(input,SBO,FOLLOW_SBO_in_indicationrule133); if (state.failed) return retval;
                    pushFollow(FOLLOW_names_in_indicationrule137);
                    n=names();

                    state._fsp--;
                    if (state.failed) return retval;
                    match(input,SBC,FOLLOW_SBC_in_indicationrule139); if (state.failed) return retval;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              builder.indicationrule((n!=null?((Token)n.start):null).getLine(), input.toString(retval.start,input.LT(-1)), (n!=null?n.nlist:null), (n!=null?n.tlist:null), except, (a!=null), (b!=null));
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
    // $ANTLR end "indicationrule"

    public static class scoreOrName_return extends ParserRuleReturnScope {
        public String value;
    };

    // $ANTLR start "scoreOrName"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:60:1: scoreOrName returns [String value] : ( name | EX );
    public final Complexrules.scoreOrName_return scoreOrName() throws RecognitionException {
        Complexrules.scoreOrName_return retval = new Complexrules.scoreOrName_return();
        retval.start = input.LT(1);

        Complexrules_ComplexCondition_BasicParser.name_return name1 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:61:1: ( name | EX )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( ((LA6_0>=String && LA6_0<=INT)||LA6_0==ID) ) {
                alt6=1;
            }
            else if ( (LA6_0==EX) ) {
                alt6=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:61:3: name
                    {
                    pushFollow(FOLLOW_name_in_scoreOrName153);
                    name1=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      retval.value =(name1!=null?name1.value:null);
                    }

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:61:32: EX
                    {
                    match(input,EX,FOLLOW_EX_in_scoreOrName159); if (state.failed) return retval;
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
    // $ANTLR end "scoreOrName"

    public static class suppressrule_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "suppressrule"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:63:1: suppressrule[boolean except] : HIDE a= name ( type )? EQ SBO names SBC ;
    public final Complexrules.suppressrule_return suppressrule(boolean except) throws RecognitionException {
        Complexrules.suppressrule_return retval = new Complexrules.suppressrule_return();
        retval.start = input.LT(1);

        Complexrules_ComplexCondition_BasicParser.name_return a = null;

        String type2 = null;

        Complexrules.names_return names3 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:64:1: ( HIDE a= name ( type )? EQ SBO names SBC )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:64:3: HIDE a= name ( type )? EQ SBO names SBC
            {
            match(input,HIDE,FOLLOW_HIDE_in_suppressrule171); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_suppressrule175);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:64:15: ( type )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==SBO) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:64:15: type
                    {
                    pushFollow(FOLLOW_type_in_suppressrule177);
                    type2=type();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            match(input,EQ,FOLLOW_EQ_in_suppressrule180); if (state.failed) return retval;
            match(input,SBO,FOLLOW_SBO_in_suppressrule182); if (state.failed) return retval;
            pushFollow(FOLLOW_names_in_suppressrule184);
            names3=names();

            state._fsp--;
            if (state.failed) return retval;
            match(input,SBC,FOLLOW_SBC_in_suppressrule186); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              builder.suppressrule((a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type2, (names3!=null?names3.nlist:null), except);
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
    // $ANTLR end "suppressrule"

    public static class abstractionrule_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "abstractionrule"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:66:1: abstractionrule[boolean except] : a= name ( type )? eqncalc ( ( formulaOrName ( PLUS | MINUS | DIV | PROD ) )=> formulawithoutP | scoreOrName | formula ) ;
    public final Complexrules.abstractionrule_return abstractionrule(boolean except) throws RecognitionException {
        Complexrules.abstractionrule_return retval = new Complexrules.abstractionrule_return();
        retval.start = input.LT(1);

        Complexrules_ComplexCondition_BasicParser.name_return a = null;

        String type4 = null;

        Complexrules_ComplexCondition_BasicParser.eqncalc_return eqncalc5 = null;

        Complexrules.scoreOrName_return scoreOrName6 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:67:1: (a= name ( type )? eqncalc ( ( formulaOrName ( PLUS | MINUS | DIV | PROD ) )=> formulawithoutP | scoreOrName | formula ) )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:67:3: a= name ( type )? eqncalc ( ( formulaOrName ( PLUS | MINUS | DIV | PROD ) )=> formulawithoutP | scoreOrName | formula )
            {
            pushFollow(FOLLOW_name_in_abstractionrule200);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:67:10: ( type )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==SBO) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:67:10: type
                    {
                    pushFollow(FOLLOW_type_in_abstractionrule202);
                    type4=type();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              builder.questionOrDiagnosis((a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type4);
            }
            pushFollow(FOLLOW_eqncalc_in_abstractionrule207);
            eqncalc5=eqncalc();

            state._fsp--;
            if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:67:105: ( ( formulaOrName ( PLUS | MINUS | DIV | PROD ) )=> formulawithoutP | scoreOrName | formula )
            int alt9=3;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==String) ) {
                int LA9_1 = input.LA(2);

                if ( (synpred1_Complexrules()) ) {
                    alt9=1;
                }
                else if ( (true) ) {
                    alt9=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA9_0==INT) ) {
                int LA9_2 = input.LA(2);

                if ( (synpred1_Complexrules()) ) {
                    alt9=1;
                }
                else if ( (true) ) {
                    alt9=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA9_0==MINUS) && (synpred1_Complexrules())) {
                alt9=1;
            }
            else if ( (LA9_0==ID) ) {
                int LA9_4 = input.LA(2);

                if ( (synpred1_Complexrules()) ) {
                    alt9=1;
                }
                else if ( (true) ) {
                    alt9=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 4, input);

                    throw nvae;
                }
            }
            else if ( (LA9_0==LP) ) {
                int LA9_5 = input.LA(2);

                if ( (synpred1_Complexrules()) ) {
                    alt9=1;
                }
                else if ( (true) ) {
                    alt9=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 5, input);

                    throw nvae;
                }
            }
            else if ( (LA9_0==EX) ) {
                alt9=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:67:106: ( formulaOrName ( PLUS | MINUS | DIV | PROD ) )=> formulawithoutP
                    {
                    pushFollow(FOLLOW_formulawithoutP_in_abstractionrule225);
                    formulawithoutP();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      builder.numValue((a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)), except, (eqncalc5!=null?input.toString(eqncalc5.start,eqncalc5.stop):null));
                    }

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:68:3: scoreOrName
                    {
                    pushFollow(FOLLOW_scoreOrName_in_abstractionrule231);
                    scoreOrName6=scoreOrName();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      builder.choiceOrDiagValue((a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)), (eqncalc5!=null?input.toString(eqncalc5.start,eqncalc5.stop):null), (scoreOrName6!=null?scoreOrName6.value:null), except);
                    }

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:69:4: formula
                    {
                    pushFollow(FOLLOW_formula_in_abstractionrule239);
                    formula();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      builder.numValue((a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)), except, (eqncalc5!=null?input.toString(eqncalc5.start,eqncalc5.stop):null));
                    }

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
    // $ANTLR end "abstractionrule"


    // $ANTLR start "formula"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:72:1: formula : LP formulawithoutP RP ;
    public final void formula() throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:73:1: ( LP formulawithoutP RP )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:73:3: LP formulawithoutP RP
            {
            match(input,LP,FOLLOW_LP_in_formula252); if (state.failed) return ;
            pushFollow(FOLLOW_formulawithoutP_in_formula254);
            formulawithoutP();

            state._fsp--;
            if (state.failed) return ;
            match(input,RP,FOLLOW_RP_in_formula256); if (state.failed) return ;

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
    // $ANTLR end "formula"

    public static class formulawithoutP_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "formulawithoutP"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:75:1: formulawithoutP : ( name | ( MINUS INT | INT DOT )=> d3double | formulaOrName ( PLUS formulaOrName | MINUS formulaOrName | PROD formulaOrName | DIV formulaOrName ) );
    public final Complexrules.formulawithoutP_return formulawithoutP() throws RecognitionException {
        Complexrules.formulawithoutP_return retval = new Complexrules.formulawithoutP_return();
        retval.start = input.LT(1);

        Complexrules_ComplexCondition_BasicParser.name_return name7 = null;

        Complexrules_ComplexCondition_BasicParser.d3double_return d3double8 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:76:1: ( name | ( MINUS INT | INT DOT )=> d3double | formulaOrName ( PLUS formulaOrName | MINUS formulaOrName | PROD formulaOrName | DIV formulaOrName ) )
            int alt11=3;
            alt11 = dfa11.predict(input);
            switch (alt11) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:76:3: name
                    {
                    pushFollow(FOLLOW_name_in_formulawithoutP264);
                    name7=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      builder.formula((name7!=null?((Token)name7.start):null).getLine(), input.toString(retval.start,input.LT(-1)), (name7!=null?name7.value:null));
                    }

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:77:3: ( MINUS INT | INT DOT )=> d3double
                    {
                    pushFollow(FOLLOW_d3double_in_formulawithoutP283);
                    d3double8=d3double();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      builder.formula(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (d3double8!=null?d3double8.value:null).toString());
                    }

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:78:3: formulaOrName ( PLUS formulaOrName | MINUS formulaOrName | PROD formulaOrName | DIV formulaOrName )
                    {
                    pushFollow(FOLLOW_formulaOrName_in_formulawithoutP289);
                    formulaOrName();

                    state._fsp--;
                    if (state.failed) return retval;
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:78:17: ( PLUS formulaOrName | MINUS formulaOrName | PROD formulaOrName | DIV formulaOrName )
                    int alt10=4;
                    switch ( input.LA(1) ) {
                    case PLUS:
                        {
                        alt10=1;
                        }
                        break;
                    case MINUS:
                        {
                        alt10=2;
                        }
                        break;
                    case PROD:
                        {
                        alt10=3;
                        }
                        break;
                    case DIV:
                        {
                        alt10=4;
                        }
                        break;
                    default:
                        if (state.backtracking>0) {state.failed=true; return retval;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 10, 0, input);

                        throw nvae;
                    }

                    switch (alt10) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:78:18: PLUS formulaOrName
                            {
                            match(input,PLUS,FOLLOW_PLUS_in_formulawithoutP292); if (state.failed) return retval;
                            pushFollow(FOLLOW_formulaOrName_in_formulawithoutP294);
                            formulaOrName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                              builder.formulaAdd();
                            }

                            }
                            break;
                        case 2 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:79:2: MINUS formulaOrName
                            {
                            match(input,MINUS,FOLLOW_MINUS_in_formulawithoutP299); if (state.failed) return retval;
                            pushFollow(FOLLOW_formulaOrName_in_formulawithoutP301);
                            formulaOrName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                              builder.formulaSub();
                            }

                            }
                            break;
                        case 3 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:80:3: PROD formulaOrName
                            {
                            match(input,PROD,FOLLOW_PROD_in_formulawithoutP307); if (state.failed) return retval;
                            pushFollow(FOLLOW_formulaOrName_in_formulawithoutP309);
                            formulaOrName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                              builder.formulaMult();
                            }

                            }
                            break;
                        case 4 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:81:3: DIV formulaOrName
                            {
                            match(input,DIV,FOLLOW_DIV_in_formulawithoutP315); if (state.failed) return retval;
                            pushFollow(FOLLOW_formulaOrName_in_formulawithoutP317);
                            formulaOrName();

                            state._fsp--;
                            if (state.failed) return retval;
                            if ( state.backtracking==0 ) {
                              builder.formulaDiv();
                            }

                            }
                            break;

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
    // $ANTLR end "formulawithoutP"

    public static class formulaOrName_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "formulaOrName"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:84:1: formulaOrName : ( formula | ( MINUS INT | INT DOT )=> d3double | name );
    public final Complexrules.formulaOrName_return formulaOrName() throws RecognitionException {
        Complexrules.formulaOrName_return retval = new Complexrules.formulaOrName_return();
        retval.start = input.LT(1);

        Complexrules_ComplexCondition_BasicParser.d3double_return d3double9 = null;

        Complexrules_ComplexCondition_BasicParser.name_return name10 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:85:1: ( formula | ( MINUS INT | INT DOT )=> d3double | name )
            int alt12=3;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==LP) ) {
                alt12=1;
            }
            else if ( (LA12_0==MINUS) && (synpred3_Complexrules())) {
                alt12=2;
            }
            else if ( (LA12_0==INT) ) {
                int LA12_3 = input.LA(2);

                if ( (synpred3_Complexrules()) ) {
                    alt12=2;
                }
                else if ( (true) ) {
                    alt12=3;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 12, 3, input);

                    throw nvae;
                }
            }
            else if ( (LA12_0==String||LA12_0==ID) ) {
                alt12=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }
            switch (alt12) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:85:2: formula
                    {
                    pushFollow(FOLLOW_formula_in_formulaOrName328);
                    formula();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:85:10: ( MINUS INT | INT DOT )=> d3double
                    {
                    pushFollow(FOLLOW_d3double_in_formulaOrName343);
                    d3double9=d3double();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      builder.formula(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (d3double9!=null?d3double9.value:null).toString());
                    }

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:85:115: name
                    {
                    pushFollow(FOLLOW_name_in_formulaOrName347);
                    name10=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      builder.formula((name10!=null?((Token)name10.start):null).getLine(), input.toString(retval.start,input.LT(-1)), (name10!=null?name10.value:null));
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
    // $ANTLR end "formulaOrName"

    public static class names_return extends ParserRuleReturnScope {
        public List<String> nlist;
        public List<String> tlist;
    };

    // $ANTLR start "names"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:87:1: names returns [List<String> nlist, List<String> tlist] : a= name (c= type )? ( SEMI b= name (d= type )? )* ;
    public final Complexrules.names_return names() throws RecognitionException {
        Complexrules.names_return retval = new Complexrules.names_return();
        retval.start = input.LT(1);

        Complexrules_ComplexCondition_BasicParser.name_return a = null;

        String c = null;

        Complexrules_ComplexCondition_BasicParser.name_return b = null;

        String d = null;


        retval.nlist = new ArrayList<String>(); retval.tlist = new ArrayList<String>();
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:89:1: (a= name (c= type )? ( SEMI b= name (d= type )? )* )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:89:2: a= name (c= type )? ( SEMI b= name (d= type )? )*
            {
            pushFollow(FOLLOW_name_in_names366);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:89:10: (c= type )?
            int alt13=2;
            int LA13_0 = input.LA(1);

            if ( (LA13_0==SBO) ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:89:10: c= type
                    {
                    pushFollow(FOLLOW_type_in_names370);
                    c=type();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              retval.nlist.add((a!=null?a.value:null)); retval.tlist.add(c);
            }
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:89:63: ( SEMI b= name (d= type )? )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==SEMI) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:89:64: SEMI b= name (d= type )?
            	    {
            	    match(input,SEMI,FOLLOW_SEMI_in_names376); if (state.failed) return retval;
            	    pushFollow(FOLLOW_name_in_names380);
            	    b=name();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:89:77: (d= type )?
            	    int alt14=2;
            	    int LA14_0 = input.LA(1);

            	    if ( (LA14_0==SBO) ) {
            	        alt14=1;
            	    }
            	    switch (alt14) {
            	        case 1 :
            	            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:89:77: d= type
            	            {
            	            pushFollow(FOLLOW_type_in_names384);
            	            d=type();

            	            state._fsp--;
            	            if (state.failed) return retval;

            	            }
            	            break;

            	    }

            	    if ( state.backtracking==0 ) {
            	      retval.nlist.add((b!=null?b.value:null)); retval.tlist.add(d);
            	    }

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


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
    // $ANTLR end "names"

    public static class intervall_return extends ParserRuleReturnScope {
        public Double a;
        public Double b;
    };

    // $ANTLR start "intervall"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:91:1: intervall returns [Double a, Double b] : SBO d1= d3double d2= d3double SBC ;
    public final Complexrules.intervall_return intervall() throws RecognitionException {
        Complexrules.intervall_return retval = new Complexrules.intervall_return();
        retval.start = input.LT(1);

        Complexrules_ComplexCondition_BasicParser.d3double_return d1 = null;

        Complexrules_ComplexCondition_BasicParser.d3double_return d2 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:92:1: ( SBO d1= d3double d2= d3double SBC )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:92:3: SBO d1= d3double d2= d3double SBC
            {
            match(input,SBO,FOLLOW_SBO_in_intervall400); if (state.failed) return retval;
            pushFollow(FOLLOW_d3double_in_intervall404);
            d1=d3double();

            state._fsp--;
            if (state.failed) return retval;
            pushFollow(FOLLOW_d3double_in_intervall408);
            d2=d3double();

            state._fsp--;
            if (state.failed) return retval;
            match(input,SBC,FOLLOW_SBC_in_intervall410); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              retval.a =(d1!=null?d1.value:null); retval.b =(d2!=null?d2.value:null);
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
    // $ANTLR end "intervall"

    // $ANTLR start synpred1_Complexrules
    public final void synpred1_Complexrules_fragment() throws RecognitionException {   
        // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:67:106: ( formulaOrName ( PLUS | MINUS | DIV | PROD ) )
        // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:67:107: formulaOrName ( PLUS | MINUS | DIV | PROD )
        {
        pushFollow(FOLLOW_formulaOrName_in_synpred1_Complexrules211);
        formulaOrName();

        state._fsp--;
        if (state.failed) return ;
        if ( (input.LA(1)>=PLUS && input.LA(1)<=DIV) ) {
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
    // $ANTLR end synpred1_Complexrules

    // $ANTLR start synpred2_Complexrules
    public final void synpred2_Complexrules_fragment() throws RecognitionException {   
        // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:77:3: ( MINUS INT | INT DOT )
        int alt16=2;
        int LA16_0 = input.LA(1);

        if ( (LA16_0==MINUS) ) {
            alt16=1;
        }
        else if ( (LA16_0==INT) ) {
            alt16=2;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 16, 0, input);

            throw nvae;
        }
        switch (alt16) {
            case 1 :
                // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:77:4: MINUS INT
                {
                match(input,MINUS,FOLLOW_MINUS_in_synpred2_Complexrules271); if (state.failed) return ;
                match(input,INT,FOLLOW_INT_in_synpred2_Complexrules273); if (state.failed) return ;

                }
                break;
            case 2 :
                // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:77:16: INT DOT
                {
                match(input,INT,FOLLOW_INT_in_synpred2_Complexrules277); if (state.failed) return ;
                match(input,DOT,FOLLOW_DOT_in_synpred2_Complexrules279); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred2_Complexrules

    // $ANTLR start synpred3_Complexrules
    public final void synpred3_Complexrules_fragment() throws RecognitionException {   
        // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:85:10: ( MINUS INT | INT DOT )
        int alt17=2;
        int LA17_0 = input.LA(1);

        if ( (LA17_0==MINUS) ) {
            alt17=1;
        }
        else if ( (LA17_0==INT) ) {
            alt17=2;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 17, 0, input);

            throw nvae;
        }
        switch (alt17) {
            case 1 :
                // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:85:11: MINUS INT
                {
                match(input,MINUS,FOLLOW_MINUS_in_synpred3_Complexrules331); if (state.failed) return ;
                match(input,INT,FOLLOW_INT_in_synpred3_Complexrules333); if (state.failed) return ;

                }
                break;
            case 2 :
                // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\Complexrules.g:85:23: INT DOT
                {
                match(input,INT,FOLLOW_INT_in_synpred3_Complexrules337); if (state.failed) return ;
                match(input,DOT,FOLLOW_DOT_in_synpred3_Complexrules339); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred3_Complexrules

    // Delegated rules
    public String type() throws RecognitionException { return gBasicParser.type(); }
    public Complexrules_ComplexCondition_BasicParser.eqncalc_return eqncalc() throws RecognitionException { return gBasicParser.eqncalc(); }
    public Complexrules_ComplexCondition.complexcondition_return complexcondition() throws RecognitionException { return gComplexCondition.complexcondition(); }
    public Complexrules_ComplexCondition.conjunct_return conjunct() throws RecognitionException { return gComplexCondition.conjunct(); }
    public Complexrules_ComplexCondition_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }
    public Complexrules_ComplexCondition_BasicParser.name_return name() throws RecognitionException { return gBasicParser.name(); }
    public Complexrules_ComplexCondition.disjunct_return disjunct() throws RecognitionException { return gComplexCondition.disjunct(); }
    public Complexrules_ComplexCondition.dnf_return dnf() throws RecognitionException { return gComplexCondition.dnf(); }
    public void startruleComplexCondition() throws RecognitionException { gComplexCondition.startruleComplexCondition(); }
    public Complexrules_ComplexCondition_BasicParser.eq_return eq() throws RecognitionException { return gBasicParser.eq(); }
    public Complexrules_ComplexCondition.condition_return condition() throws RecognitionException { return gComplexCondition.condition(); }

    public final boolean synpred1_Complexrules() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_Complexrules_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred3_Complexrules() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred3_Complexrules_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred2_Complexrules() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred2_Complexrules_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA4_eotS =
        "\13\uffff";
    static final String DFA4_eofS =
        "\1\uffff\2\3\5\uffff\1\3\1\uffff\1\3";
    static final String DFA4_minS =
        "\3\4\2\uffff\1\4\1\71\1\uffff\1\4\1\24\1\11";
    static final String DFA4_maxS =
        "\3\71\2\uffff\2\71\1\uffff\1\71\1\24\1\41";
    static final String DFA4_acceptS =
        "\3\uffff\1\1\1\2\2\uffff\1\3\3\uffff";
    static final String DFA4_specialS =
        "\13\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\1\1\2\37\uffff\1\3\1\4\3\uffff\1\3\16\uffff\1\2",
            "\1\5\1\2\3\uffff\1\3\11\uffff\1\6\1\uffff\7\7\5\uffff\1\3"+
            "\27\uffff\1\2",
            "\2\10\3\uffff\1\3\11\uffff\1\6\1\uffff\7\7\5\uffff\1\3\27"+
            "\uffff\1\10",
            "",
            "",
            "\1\5\1\2\63\uffff\1\2",
            "\1\11",
            "",
            "\2\10\3\uffff\1\3\11\uffff\1\6\1\uffff\7\7\5\uffff\1\3\27"+
            "\uffff\1\10",
            "\1\12",
            "\1\3\13\uffff\7\7\5\uffff\1\3"
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
            return "54:1: ruleaction[boolean except] : ( indicationrule[except] | suppressrule[except] | abstractionrule[except] );";
        }
    }
    static final String DFA11_eotS =
        "\20\uffff";
    static final String DFA11_eofS =
        "\1\uffff\1\6\2\uffff\1\6\4\uffff\1\14\1\6\4\uffff\1\14";
    static final String DFA11_minS =
        "\3\4\1\5\1\4\2\uffff\1\4\1\uffff\1\6\1\4\1\5\3\uffff\1\20";
    static final String DFA11_maxS =
        "\3\71\1\5\1\71\2\uffff\1\71\1\uffff\1\41\1\71\1\5\3\uffff\1\41";
    static final String DFA11_acceptS =
        "\5\uffff\1\3\1\1\1\uffff\1\2\3\uffff\3\2\1\uffff";
    static final String DFA11_specialS =
        "\2\uffff\1\0\6\uffff\1\2\5\uffff\1\1}>";
    static final String[] DFA11_transitionS = {
            "\1\1\1\2\11\uffff\1\5\13\uffff\1\3\35\uffff\1\4",
            "\1\7\1\4\12\uffff\1\6\11\uffff\4\5\3\uffff\1\6\27\uffff\1"+
            "\4",
            "\3\5\1\uffff\1\5\21\uffff\4\5\33\uffff\1\5",
            "\1\11",
            "\2\12\12\uffff\1\6\11\uffff\4\5\3\uffff\1\6\27\uffff\1\12",
            "",
            "",
            "\1\7\1\4\63\uffff\1\4",
            "",
            "\1\13\1\uffff\1\13\7\uffff\1\16\11\uffff\4\5\3\uffff\1\15",
            "\2\12\12\uffff\1\6\11\uffff\4\5\3\uffff\1\6\27\uffff\1\12",
            "\1\17",
            "",
            "",
            "",
            "\1\16\11\uffff\4\5\3\uffff\1\15"
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
            return "75:1: formulawithoutP : ( name | ( MINUS INT | INT DOT )=> d3double | formulaOrName ( PLUS formulaOrName | MINUS formulaOrName | PROD formulaOrName | DIV formulaOrName ) );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA11_2 = input.LA(1);

                         
                        int index11_2 = input.index();
                        input.rewind();
                        s = -1;
                        if ( ((LA11_2>=String && LA11_2<=DOT)||LA11_2==COMMA||(LA11_2>=PLUS && LA11_2<=DIV)||LA11_2==ID) ) {s = 5;}

                        else if ( (true) ) {s = 6;}

                        else if ( (synpred2_Complexrules()) ) {s = 8;}

                         
                        input.seek(index11_2);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA11_15 = input.LA(1);

                         
                        int index11_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA11_15==EOF) && (synpred2_Complexrules())) {s = 12;}

                        else if ( (LA11_15==IF) && (synpred2_Complexrules())) {s = 13;}

                        else if ( (LA11_15==RP) && (synpred2_Complexrules())) {s = 14;}

                        else if ( ((LA11_15>=PLUS && LA11_15<=DIV)) ) {s = 5;}

                         
                        input.seek(index11_15);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA11_9 = input.LA(1);

                         
                        int index11_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (LA11_9==DOT||LA11_9==COMMA) ) {s = 11;}

                        else if ( (LA11_9==EOF) && (synpred2_Complexrules())) {s = 12;}

                        else if ( (LA11_9==IF) && (synpred2_Complexrules())) {s = 13;}

                        else if ( (LA11_9==RP) && (synpred2_Complexrules())) {s = 14;}

                        else if ( ((LA11_9>=PLUS && LA11_9<=DIV)) ) {s = 5;}

                         
                        input.seek(index11_9);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 11, _s, input);
            error(nvae);
            throw nvae;
        }
    }
 

    public static final BitSet FOLLOW_complexrule_in_knowledge49 = new BitSet(new long[]{0x0000000200000002L});
    public static final BitSet FOLLOW_IF_in_complexrule58 = new BitSet(new long[]{0x02000B2000008030L});
    public static final BitSet FOLLOW_complexcondition_in_complexrule60 = new BitSet(new long[]{0x0000008400000000L});
    public static final BitSet FOLLOW_EXCEPT_in_complexrule65 = new BitSet(new long[]{0x02000B2000008030L});
    public static final BitSet FOLLOW_complexcondition_in_complexrule67 = new BitSet(new long[]{0x0000000500000000L});
    public static final BitSet FOLLOW_NL_in_complexrule69 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_THEN_in_complexrule74 = new BitSet(new long[]{0x0200076000000030L});
    public static final BitSet FOLLOW_ruleaction_in_complexrule76 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_indicationrule_in_ruleaction86 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_suppressrule_in_ruleaction91 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_abstractionrule_in_ruleaction96 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_names_in_indicationrule111 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INSTANT_in_indicationrule117 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_indicationrule119 = new BitSet(new long[]{0x0200076000000030L});
    public static final BitSet FOLLOW_names_in_indicationrule123 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_indicationrule125 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_indicationrule131 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_indicationrule133 = new BitSet(new long[]{0x0200076000000030L});
    public static final BitSet FOLLOW_names_in_indicationrule137 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_indicationrule139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_scoreOrName153 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EX_in_scoreOrName159 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_HIDE_in_suppressrule171 = new BitSet(new long[]{0x0200076000000030L});
    public static final BitSet FOLLOW_name_in_suppressrule175 = new BitSet(new long[]{0x0000000002080000L});
    public static final BitSet FOLLOW_type_in_suppressrule177 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_suppressrule180 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_suppressrule182 = new BitSet(new long[]{0x0200076000000030L});
    public static final BitSet FOLLOW_names_in_suppressrule184 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_suppressrule186 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_abstractionrule200 = new BitSet(new long[]{0x000000000FE80000L});
    public static final BitSet FOLLOW_type_in_abstractionrule202 = new BitSet(new long[]{0x000000000FE80000L});
    public static final BitSet FOLLOW_eqncalc_in_abstractionrule207 = new BitSet(new long[]{0x0200076008008430L});
    public static final BitSet FOLLOW_formulawithoutP_in_abstractionrule225 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_scoreOrName_in_abstractionrule231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formula_in_abstractionrule239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_formula252 = new BitSet(new long[]{0x0200076008008030L});
    public static final BitSet FOLLOW_formulawithoutP_in_formula254 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_formula256 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_formulawithoutP264 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_d3double_in_formulawithoutP283 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formulaOrName_in_formulawithoutP289 = new BitSet(new long[]{0x000000003C000000L});
    public static final BitSet FOLLOW_PLUS_in_formulawithoutP292 = new BitSet(new long[]{0x0200076008008030L});
    public static final BitSet FOLLOW_formulaOrName_in_formulawithoutP294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_formulawithoutP299 = new BitSet(new long[]{0x0200076008008030L});
    public static final BitSet FOLLOW_formulaOrName_in_formulawithoutP301 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PROD_in_formulawithoutP307 = new BitSet(new long[]{0x0200076008008030L});
    public static final BitSet FOLLOW_formulaOrName_in_formulawithoutP309 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DIV_in_formulawithoutP315 = new BitSet(new long[]{0x0200076008008030L});
    public static final BitSet FOLLOW_formulaOrName_in_formulawithoutP317 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formula_in_formulaOrName328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_d3double_in_formulaOrName343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_formulaOrName347 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_names366 = new BitSet(new long[]{0x0000000000080202L});
    public static final BitSet FOLLOW_type_in_names370 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_SEMI_in_names376 = new BitSet(new long[]{0x0200076008008030L});
    public static final BitSet FOLLOW_name_in_names380 = new BitSet(new long[]{0x0000000000080202L});
    public static final BitSet FOLLOW_type_in_names384 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_SBO_in_intervall400 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_intervall404 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_intervall408 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_intervall410 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_formulaOrName_in_synpred1_Complexrules211 = new BitSet(new long[]{0x000000003C000000L});
    public static final BitSet FOLLOW_set_in_synpred1_Complexrules213 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred2_Complexrules271 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_synpred2_Complexrules273 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_synpred2_Complexrules277 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_DOT_in_synpred2_Complexrules279 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred3_Complexrules331 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_synpred3_Complexrules333 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_synpred3_Complexrules337 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_DOT_in_synpred3_Complexrules339 = new BitSet(new long[]{0x0000000000000002L});

}