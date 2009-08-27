// $ANTLR 3.1 ComplexCondition.g 2009-03-14 18:59:14

package de.d3web.KnOfficeParser.complexcondition;
import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class ComplexConditionSOLO_ComplexCondition extends Parser {
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
    public static final int Tokens=58;
    public static final int SEMI=9;
    public static final int REF=52;
    public static final int WS=30;
    public static final int BLUB=57;
    public static final int OR=36;
    public static final int CBC=18;
    public static final int SBO=19;
    public static final int DIVTEXT=54;
    public static final int DIV=29;
    public static final int CBO=17;
    public static final int LE=21;

    // delegates
    public ComplexConditionSOLO_ComplexCondition_BasicParser gBasicParser;
    // delegators
    public ComplexConditionSOLO gComplexConditionSOLO;
    public ComplexConditionSOLO gParent;


        public ComplexConditionSOLO_ComplexCondition(TokenStream input, ComplexConditionSOLO gComplexConditionSOLO) {
            this(input, new RecognizerSharedState(), gComplexConditionSOLO);
        }
        public ComplexConditionSOLO_ComplexCondition(TokenStream input, RecognizerSharedState state, ComplexConditionSOLO gComplexConditionSOLO) {
            super(input, state);
            this.gComplexConditionSOLO = gComplexConditionSOLO;
            gBasicParser = new ComplexConditionSOLO_ComplexCondition_BasicParser(input, state, gComplexConditionSOLO, this);         
            gParent = gComplexConditionSOLO;
        }
        

    public String[] getTokenNames() { return ComplexConditionSOLO.tokenNames; }
    public String getGrammarFileName() { return "ComplexCondition.g"; }


      private ConditionBuilder builder;
      private ParserErrorHandler eh;
      
      public void setEH(ParserErrorHandler eh) {
        this.eh=eh;
        gBasicParser.setEH(eh);
      }
      
      public void setBuilder(ConditionBuilder builder) {
        this.builder = builder;
      }
      
      public ConditionBuilder getBuilder() {
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



    // $ANTLR start "startruleComplexCondition"
    // ComplexCondition.g:35:1: startruleComplexCondition : ;
    public final void startruleComplexCondition() throws RecognitionException {
        try {
            // ComplexCondition.g:35:26: ()
            // ComplexCondition.g:35:28: 
            {
            }

        }
        finally {
        }
        return ;
    }
    // $ANTLR end "startruleComplexCondition"

    public static class complexcondition_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "complexcondition"
    // ComplexCondition.g:37:1: complexcondition : ( dnf | MINMAX LP a= INT b= INT RP CBO complexcondition ( SEMI complexcondition )* CBC );
    public final ComplexConditionSOLO_ComplexCondition.complexcondition_return complexcondition() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.complexcondition_return retval = new ComplexConditionSOLO_ComplexCondition.complexcondition_return();
        retval.start = input.LT(1);

        Token a=null;
        Token b=null;

        try {
            // ComplexCondition.g:38:1: ( dnf | MINMAX LP a= INT b= INT RP CBO complexcondition ( SEMI complexcondition )* CBC )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( ((LA2_0>=String && LA2_0<=INT)||LA2_0==LP||LA2_0==NOT||(LA2_0>=UNKNOWN && LA2_0<=KNOWN)||LA2_0==ID) ) {
                alt2=1;
            }
            else if ( (LA2_0==MINMAX) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // ComplexCondition.g:39:1: dnf
                    {
                    pushFollow(FOLLOW_dnf_in_complexcondition42);
                    dnf();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ComplexCondition.g:40:3: MINMAX LP a= INT b= INT RP CBO complexcondition ( SEMI complexcondition )* CBC
                    {
                    int i=1;
                    match(input,MINMAX,FOLLOW_MINMAX_in_complexcondition48); 
                    match(input,LP,FOLLOW_LP_in_complexcondition50); 
                    a=(Token)match(input,INT,FOLLOW_INT_in_complexcondition54); 
                    b=(Token)match(input,INT,FOLLOW_INT_in_complexcondition58); 
                    match(input,RP,FOLLOW_RP_in_complexcondition60); 
                    match(input,CBO,FOLLOW_CBO_in_complexcondition62); 
                    pushFollow(FOLLOW_complexcondition_in_complexcondition64);
                    complexcondition();

                    state._fsp--;

                    // ComplexCondition.g:40:60: ( SEMI complexcondition )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==SEMI) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // ComplexCondition.g:40:61: SEMI complexcondition
                    	    {
                    	    match(input,SEMI,FOLLOW_SEMI_in_complexcondition67); 
                    	    pushFollow(FOLLOW_complexcondition_in_complexcondition69);
                    	    complexcondition();

                    	    state._fsp--;

                    	    i++;

                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);

                    match(input,CBC,FOLLOW_CBC_in_complexcondition75); 
                    builder.minmax(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), Integer.parseInt((a!=null?a.getText():null)), Integer.parseInt((b!=null?b.getText():null)), i);

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
    // $ANTLR end "complexcondition"

    public static class dnf_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "dnf"
    // ComplexCondition.g:42:1: dnf : disjunct ( OR disjunct )* ;
    public final ComplexConditionSOLO_ComplexCondition.dnf_return dnf() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.dnf_return retval = new ComplexConditionSOLO_ComplexCondition.dnf_return();
        retval.start = input.LT(1);

        try {
            // ComplexCondition.g:43:1: ( disjunct ( OR disjunct )* )
            // ComplexCondition.g:43:3: disjunct ( OR disjunct )*
            {
            pushFollow(FOLLOW_disjunct_in_dnf85);
            disjunct();

            state._fsp--;

            // ComplexCondition.g:43:12: ( OR disjunct )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==OR) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ComplexCondition.g:43:13: OR disjunct
            	    {
            	    match(input,OR,FOLLOW_OR_in_dnf88); 
            	    pushFollow(FOLLOW_disjunct_in_dnf90);
            	    disjunct();

            	    state._fsp--;

            	    builder.orcond(input.toString(retval.start,input.LT(-1)));

            	    }
            	    break;

            	default :
            	    break loop3;
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
    // $ANTLR end "dnf"

    public static class disjunct_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "disjunct"
    // ComplexCondition.g:45:1: disjunct : conjunct ( AND conjunct )* ;
    public final ComplexConditionSOLO_ComplexCondition.disjunct_return disjunct() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.disjunct_return retval = new ComplexConditionSOLO_ComplexCondition.disjunct_return();
        retval.start = input.LT(1);

        try {
            // ComplexCondition.g:46:1: ( conjunct ( AND conjunct )* )
            // ComplexCondition.g:46:3: conjunct ( AND conjunct )*
            {
            pushFollow(FOLLOW_conjunct_in_disjunct102);
            conjunct();

            state._fsp--;

            // ComplexCondition.g:46:12: ( AND conjunct )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==AND) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ComplexCondition.g:46:13: AND conjunct
            	    {
            	    match(input,AND,FOLLOW_AND_in_disjunct105); 
            	    pushFollow(FOLLOW_conjunct_in_disjunct107);
            	    conjunct();

            	    state._fsp--;

            	    builder.andcond(input.toString(retval.start,input.LT(-1)));

            	    }
            	    break;

            	default :
            	    break loop4;
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
    // $ANTLR end "disjunct"

    public static class conjunct_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "conjunct"
    // ComplexCondition.g:48:1: conjunct : ( condition | LP complexcondition RP | NOT conjunct );
    public final ComplexConditionSOLO_ComplexCondition.conjunct_return conjunct() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.conjunct_return retval = new ComplexConditionSOLO_ComplexCondition.conjunct_return();
        retval.start = input.LT(1);

        try {
            // ComplexCondition.g:49:1: ( condition | LP complexcondition RP | NOT conjunct )
            int alt5=3;
            switch ( input.LA(1) ) {
            case String:
            case INT:
            case UNKNOWN:
            case KNOWN:
            case ID:
                {
                alt5=1;
                }
                break;
            case LP:
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
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // ComplexCondition.g:49:3: condition
                    {
                    pushFollow(FOLLOW_condition_in_conjunct120);
                    condition();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ComplexCondition.g:50:3: LP complexcondition RP
                    {
                    match(input,LP,FOLLOW_LP_in_conjunct124); 
                    pushFollow(FOLLOW_complexcondition_in_conjunct126);
                    complexcondition();

                    state._fsp--;

                    match(input,RP,FOLLOW_RP_in_conjunct128); 
                    builder.complexcondition(input.toString(retval.start,input.LT(-1)));

                    }
                    break;
                case 3 :
                    // ComplexCondition.g:51:3: NOT conjunct
                    {
                    match(input,NOT,FOLLOW_NOT_in_conjunct134); 
                    pushFollow(FOLLOW_conjunct_in_conjunct136);
                    conjunct();

                    state._fsp--;

                    builder.notcond(input.toString(retval.start,input.LT(-1)));

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
    // $ANTLR end "conjunct"

    public static class condition_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "condition"
    // ComplexCondition.g:53:1: condition : (a= name ( type )? ( eq b= name | (in= IN )? intervall ) | ( KNOWN | c= UNKNOWN ) SBO a= name ( type )? SBC | a= name ( type )? IN CBO b= name ( COMMA d= name )* CBC | a= name ( type )? ALL CBO b= name ( COMMA d= name )* CBC );
    public final ComplexConditionSOLO_ComplexCondition.condition_return condition() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.condition_return retval = new ComplexConditionSOLO_ComplexCondition.condition_return();
        retval.start = input.LT(1);

        Token in=null;
        Token c=null;
        ComplexConditionSOLO_ComplexCondition_BasicParser.name_return a = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.name_return b = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.name_return d = null;

        String type1 = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.eq_return eq2 = null;

        ComplexConditionSOLO_ComplexCondition.intervall_return intervall3 = null;

        String type4 = null;

        String type5 = null;

        String type6 = null;


        try {
            // ComplexCondition.g:54:1: (a= name ( type )? ( eq b= name | (in= IN )? intervall ) | ( KNOWN | c= UNKNOWN ) SBO a= name ( type )? SBC | a= name ( type )? IN CBO b= name ( COMMA d= name )* CBC | a= name ( type )? ALL CBO b= name ( COMMA d= name )* CBC )
            int alt15=4;
            alt15 = dfa15.predict(input);
            switch (alt15) {
                case 1 :
                    // ComplexCondition.g:54:3: a= name ( type )? ( eq b= name | (in= IN )? intervall )
                    {
                    pushFollow(FOLLOW_name_in_condition148);
                    a=gComplexConditionSOLO.name();

                    state._fsp--;

                    // ComplexCondition.g:54:10: ( type )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0==SBO) ) {
                        int LA6_1 = input.LA(2);

                        if ( (LA6_1==ID) ) {
                            alt6=1;
                        }
                    }
                    switch (alt6) {
                        case 1 :
                            // ComplexCondition.g:54:10: type
                            {
                            pushFollow(FOLLOW_type_in_condition150);
                            type1=gComplexConditionSOLO.type();

                            state._fsp--;


                            }
                            break;

                    }

                    // ComplexCondition.g:54:16: ( eq b= name | (in= IN )? intervall )
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( ((LA8_0>=LE && LA8_0<=EQ)) ) {
                        alt8=1;
                    }
                    else if ( (LA8_0==SBO||LA8_0==IN) ) {
                        alt8=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 8, 0, input);

                        throw nvae;
                    }
                    switch (alt8) {
                        case 1 :
                            // ComplexCondition.g:54:17: eq b= name
                            {
                            pushFollow(FOLLOW_eq_in_condition154);
                            eq2=gComplexConditionSOLO.eq();

                            state._fsp--;

                            pushFollow(FOLLOW_name_in_condition158);
                            b=gComplexConditionSOLO.name();

                            state._fsp--;

                            builder.condition( ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type1, (eq2!=null?input.toString(eq2.start,eq2.stop):null), (b!=null?b.value:null));

                            }
                            break;
                        case 2 :
                            // ComplexCondition.g:55:3: (in= IN )? intervall
                            {
                            // ComplexCondition.g:55:5: (in= IN )?
                            int alt7=2;
                            int LA7_0 = input.LA(1);

                            if ( (LA7_0==IN) ) {
                                alt7=1;
                            }
                            switch (alt7) {
                                case 1 :
                                    // ComplexCondition.g:55:5: in= IN
                                    {
                                    in=(Token)match(input,IN,FOLLOW_IN_in_condition166); 

                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_intervall_in_condition169);
                            intervall3=intervall();

                            state._fsp--;

                            builder.condition(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type1, (intervall3!=null?intervall3.a:null), (intervall3!=null?intervall3.b:null), (in!=null));

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // ComplexCondition.g:56:3: ( KNOWN | c= UNKNOWN ) SBO a= name ( type )? SBC
                    {
                    // ComplexCondition.g:56:3: ( KNOWN | c= UNKNOWN )
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==KNOWN) ) {
                        alt9=1;
                    }
                    else if ( (LA9_0==UNKNOWN) ) {
                        alt9=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 9, 0, input);

                        throw nvae;
                    }
                    switch (alt9) {
                        case 1 :
                            // ComplexCondition.g:56:4: KNOWN
                            {
                            match(input,KNOWN,FOLLOW_KNOWN_in_condition179); 

                            }
                            break;
                        case 2 :
                            // ComplexCondition.g:56:10: c= UNKNOWN
                            {
                            c=(Token)match(input,UNKNOWN,FOLLOW_UNKNOWN_in_condition183); 

                            }
                            break;

                    }

                    match(input,SBO,FOLLOW_SBO_in_condition186); 
                    pushFollow(FOLLOW_name_in_condition190);
                    a=gComplexConditionSOLO.name();

                    state._fsp--;

                    // ComplexCondition.g:56:32: ( type )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==SBO) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // ComplexCondition.g:56:32: type
                            {
                            pushFollow(FOLLOW_type_in_condition192);
                            type4=gComplexConditionSOLO.type();

                            state._fsp--;


                            }
                            break;

                    }

                    match(input,SBC,FOLLOW_SBC_in_condition195); 
                    builder.knowncondition(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type4, c!=null);

                    }
                    break;
                case 3 :
                    // ComplexCondition.g:57:3: a= name ( type )? IN CBO b= name ( COMMA d= name )* CBC
                    {
                    List<String> answers= new ArrayList();
                    pushFollow(FOLLOW_name_in_condition205);
                    a=gComplexConditionSOLO.name();

                    state._fsp--;

                    // ComplexCondition.g:57:51: ( type )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==SBO) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // ComplexCondition.g:57:51: type
                            {
                            pushFollow(FOLLOW_type_in_condition207);
                            type5=gComplexConditionSOLO.type();

                            state._fsp--;


                            }
                            break;

                    }

                    match(input,IN,FOLLOW_IN_in_condition210); 
                    match(input,CBO,FOLLOW_CBO_in_condition212); 
                    pushFollow(FOLLOW_name_in_condition216);
                    b=gComplexConditionSOLO.name();

                    state._fsp--;

                    answers.add((b!=null?b.value:null));
                    // ComplexCondition.g:57:96: ( COMMA d= name )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);

                        if ( (LA12_0==COMMA) ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // ComplexCondition.g:57:97: COMMA d= name
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_condition221); 
                    	    pushFollow(FOLLOW_name_in_condition225);
                    	    d=gComplexConditionSOLO.name();

                    	    state._fsp--;

                    	    answers.add((d!=null?d.value:null));

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);

                    match(input,CBC,FOLLOW_CBC_in_condition231); 
                    builder.in(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type5, answers);

                    }
                    break;
                case 4 :
                    // ComplexCondition.g:58:3: a= name ( type )? ALL CBO b= name ( COMMA d= name )* CBC
                    {
                    List<String> answers= new ArrayList();
                    pushFollow(FOLLOW_name_in_condition241);
                    a=gComplexConditionSOLO.name();

                    state._fsp--;

                    // ComplexCondition.g:58:51: ( type )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==SBO) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // ComplexCondition.g:58:51: type
                            {
                            pushFollow(FOLLOW_type_in_condition243);
                            type6=gComplexConditionSOLO.type();

                            state._fsp--;


                            }
                            break;

                    }

                    match(input,ALL,FOLLOW_ALL_in_condition246); 
                    match(input,CBO,FOLLOW_CBO_in_condition248); 
                    pushFollow(FOLLOW_name_in_condition252);
                    b=gComplexConditionSOLO.name();

                    state._fsp--;

                    answers.add((b!=null?b.value:null));
                    // ComplexCondition.g:58:97: ( COMMA d= name )*
                    loop14:
                    do {
                        int alt14=2;
                        int LA14_0 = input.LA(1);

                        if ( (LA14_0==COMMA) ) {
                            alt14=1;
                        }


                        switch (alt14) {
                    	case 1 :
                    	    // ComplexCondition.g:58:98: COMMA d= name
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_condition257); 
                    	    pushFollow(FOLLOW_name_in_condition261);
                    	    d=gComplexConditionSOLO.name();

                    	    state._fsp--;

                    	    answers.add((d!=null?d.value:null));

                    	    }
                    	    break;

                    	default :
                    	    break loop14;
                        }
                    } while (true);

                    match(input,CBC,FOLLOW_CBC_in_condition267); 
                    builder.all(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type6, answers);

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
    // $ANTLR end "condition"

    public static class intervall_return extends ParserRuleReturnScope {
        public Double a;
        public Double b;
    };

    // $ANTLR start "intervall"
    // ComplexCondition.g:60:1: intervall returns [Double a, Double b] : SBO d1= d3double d2= d3double SBC ;
    public final ComplexConditionSOLO_ComplexCondition.intervall_return intervall() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.intervall_return retval = new ComplexConditionSOLO_ComplexCondition.intervall_return();
        retval.start = input.LT(1);

        ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return d1 = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return d2 = null;


        try {
            // ComplexCondition.g:61:1: ( SBO d1= d3double d2= d3double SBC )
            // ComplexCondition.g:61:3: SBO d1= d3double d2= d3double SBC
            {
            match(input,SBO,FOLLOW_SBO_in_intervall280); 
            pushFollow(FOLLOW_d3double_in_intervall284);
            d1=gComplexConditionSOLO.d3double();

            state._fsp--;

            pushFollow(FOLLOW_d3double_in_intervall288);
            d2=gComplexConditionSOLO.d3double();

            state._fsp--;

            match(input,SBC,FOLLOW_SBC_in_intervall290); 
            retval.a =(d1!=null?d1.value:null); retval.b =(d2!=null?d2.value:null);

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

    // Delegated rules


    protected DFA15 dfa15 = new DFA15(this);
    static final String DFA15_eotS =
        "\15\uffff";
    static final String DFA15_eofS =
        "\15\uffff";
    static final String DFA15_minS =
        "\3\4\1\uffff\1\5\1\21\1\4\2\uffff\1\4\1\24\1\uffff\1\23";
    static final String DFA15_maxS =
        "\3\70\1\uffff\1\70\1\23\1\70\2\uffff\1\70\1\24\1\uffff\1\56";
    static final String DFA15_acceptS =
        "\3\uffff\1\2\3\uffff\1\1\1\4\2\uffff\1\3\1\uffff";
    static final String DFA15_specialS =
        "\15\uffff}>";
    static final String[] DFA15_transitionS = {
            "\1\1\1\2\42\uffff\2\3\16\uffff\1\2",
            "\1\6\1\2\15\uffff\1\4\1\uffff\5\7\22\uffff\1\5\1\uffff\1\10"+
            "\11\uffff\1\2",
            "\2\11\15\uffff\1\4\1\uffff\5\7\22\uffff\1\5\1\uffff\1\10\11"+
            "\uffff\1\11",
            "",
            "\1\7\25\uffff\1\7\34\uffff\1\12",
            "\1\13\1\uffff\1\7",
            "\1\6\1\2\62\uffff\1\2",
            "",
            "",
            "\2\11\15\uffff\1\4\1\uffff\5\7\22\uffff\1\5\1\uffff\1\10\11"+
            "\uffff\1\11",
            "\1\14",
            "",
            "\1\7\1\uffff\5\7\22\uffff\1\5\1\uffff\1\10"
    };

    static final short[] DFA15_eot = DFA.unpackEncodedString(DFA15_eotS);
    static final short[] DFA15_eof = DFA.unpackEncodedString(DFA15_eofS);
    static final char[] DFA15_min = DFA.unpackEncodedStringToUnsignedChars(DFA15_minS);
    static final char[] DFA15_max = DFA.unpackEncodedStringToUnsignedChars(DFA15_maxS);
    static final short[] DFA15_accept = DFA.unpackEncodedString(DFA15_acceptS);
    static final short[] DFA15_special = DFA.unpackEncodedString(DFA15_specialS);
    static final short[][] DFA15_transition;

    static {
        int numStates = DFA15_transitionS.length;
        DFA15_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA15_transition[i] = DFA.unpackEncodedString(DFA15_transitionS[i]);
        }
    }

    class DFA15 extends DFA {

        public DFA15(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 15;
            this.eot = DFA15_eot;
            this.eof = DFA15_eof;
            this.min = DFA15_min;
            this.max = DFA15_max;
            this.accept = DFA15_accept;
            this.special = DFA15_special;
            this.transition = DFA15_transition;
        }
        public String getDescription() {
            return "53:1: condition : (a= name ( type )? ( eq b= name | (in= IN )? intervall ) | ( KNOWN | c= UNKNOWN ) SBO a= name ( type )? SBC | a= name ( type )? IN CBO b= name ( COMMA d= name )* CBC | a= name ( type )? ALL CBO b= name ( COMMA d= name )* CBC );";
        }
    }
 

    public static final BitSet FOLLOW_dnf_in_complexcondition42 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINMAX_in_complexcondition48 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_complexcondition50 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_complexcondition54 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_complexcondition58 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_complexcondition60 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_complexcondition62 = new BitSet(new long[]{0x01000B2000008030L});
    public static final BitSet FOLLOW_complexcondition_in_complexcondition64 = new BitSet(new long[]{0x0000000000040200L});
    public static final BitSet FOLLOW_SEMI_in_complexcondition67 = new BitSet(new long[]{0x01000B2000008030L});
    public static final BitSet FOLLOW_complexcondition_in_complexcondition69 = new BitSet(new long[]{0x0000000000040200L});
    public static final BitSet FOLLOW_CBC_in_complexcondition75 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_disjunct_in_dnf85 = new BitSet(new long[]{0x0000001000000002L});
    public static final BitSet FOLLOW_OR_in_dnf88 = new BitSet(new long[]{0x0100032000008030L});
    public static final BitSet FOLLOW_disjunct_in_dnf90 = new BitSet(new long[]{0x0000001000000002L});
    public static final BitSet FOLLOW_conjunct_in_disjunct102 = new BitSet(new long[]{0x0000000800000002L});
    public static final BitSet FOLLOW_AND_in_disjunct105 = new BitSet(new long[]{0x0100032000008030L});
    public static final BitSet FOLLOW_conjunct_in_disjunct107 = new BitSet(new long[]{0x0000000800000002L});
    public static final BitSet FOLLOW_condition_in_conjunct120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_conjunct124 = new BitSet(new long[]{0x01000B2000008030L});
    public static final BitSet FOLLOW_complexcondition_in_conjunct126 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_conjunct128 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_conjunct134 = new BitSet(new long[]{0x0100032000008030L});
    public static final BitSet FOLLOW_conjunct_in_conjunct136 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_condition148 = new BitSet(new long[]{0x0000100003E80000L});
    public static final BitSet FOLLOW_type_in_condition150 = new BitSet(new long[]{0x0000100003E80000L});
    public static final BitSet FOLLOW_eq_in_condition154 = new BitSet(new long[]{0x0100030000000030L});
    public static final BitSet FOLLOW_name_in_condition158 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_condition166 = new BitSet(new long[]{0x0000100003E80000L});
    public static final BitSet FOLLOW_intervall_in_condition169 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KNOWN_in_condition179 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_UNKNOWN_in_condition183 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_condition186 = new BitSet(new long[]{0x0100030000000030L});
    public static final BitSet FOLLOW_name_in_condition190 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_type_in_condition192 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_condition195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_condition205 = new BitSet(new long[]{0x0000100000080000L});
    public static final BitSet FOLLOW_type_in_condition207 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_IN_in_condition210 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_condition212 = new BitSet(new long[]{0x0100030000000030L});
    public static final BitSet FOLLOW_name_in_condition216 = new BitSet(new long[]{0x0000000000040100L});
    public static final BitSet FOLLOW_COMMA_in_condition221 = new BitSet(new long[]{0x0100030000000030L});
    public static final BitSet FOLLOW_name_in_condition225 = new BitSet(new long[]{0x0000000000040100L});
    public static final BitSet FOLLOW_CBC_in_condition231 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_condition241 = new BitSet(new long[]{0x0000400000080000L});
    public static final BitSet FOLLOW_type_in_condition243 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_ALL_in_condition246 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_condition248 = new BitSet(new long[]{0x0100030000000030L});
    public static final BitSet FOLLOW_name_in_condition252 = new BitSet(new long[]{0x0000000000040100L});
    public static final BitSet FOLLOW_COMMA_in_condition257 = new BitSet(new long[]{0x0100030000000030L});
    public static final BitSet FOLLOW_name_in_condition261 = new BitSet(new long[]{0x0000000000040100L});
    public static final BitSet FOLLOW_CBC_in_condition267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_intervall280 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_intervall284 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_intervall288 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_intervall290 = new BitSet(new long[]{0x0000000000000002L});

}