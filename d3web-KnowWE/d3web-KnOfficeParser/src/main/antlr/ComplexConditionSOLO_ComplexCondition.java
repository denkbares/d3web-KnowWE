// $ANTLR 3.1.1 ComplexCondition.g 2010-04-06 16:15:57

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
    // ComplexCondition.g:55:1: startruleComplexCondition : ;
    public final void startruleComplexCondition() throws RecognitionException {
        try {
            // ComplexCondition.g:55:26: ()
            // ComplexCondition.g:55:28: 
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
    // ComplexCondition.g:57:1: complexcondition : ( dnf | MINMAX LP a= INT b= INT RP CBO complexcondition ( SEMI complexcondition )* CBC );
    public final ComplexConditionSOLO_ComplexCondition.complexcondition_return complexcondition() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.complexcondition_return retval = new ComplexConditionSOLO_ComplexCondition.complexcondition_return();
        retval.start = input.LT(1);

        Token a=null;
        Token b=null;

        try {
            // ComplexCondition.g:58:1: ( dnf | MINMAX LP a= INT b= INT RP CBO complexcondition ( SEMI complexcondition )* CBC )
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
                    // ComplexCondition.g:59:1: dnf
                    {
                    pushFollow(FOLLOW_dnf_in_complexcondition45);
                    dnf();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ComplexCondition.g:60:3: MINMAX LP a= INT b= INT RP CBO complexcondition ( SEMI complexcondition )* CBC
                    {
                    int i=1;
                    match(input,MINMAX,FOLLOW_MINMAX_in_complexcondition51); 
                    match(input,LP,FOLLOW_LP_in_complexcondition53); 
                    a=(Token)match(input,INT,FOLLOW_INT_in_complexcondition57); 
                    b=(Token)match(input,INT,FOLLOW_INT_in_complexcondition61); 
                    match(input,RP,FOLLOW_RP_in_complexcondition63); 
                    match(input,CBO,FOLLOW_CBO_in_complexcondition65); 
                    pushFollow(FOLLOW_complexcondition_in_complexcondition67);
                    complexcondition();

                    state._fsp--;

                    // ComplexCondition.g:60:60: ( SEMI complexcondition )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==SEMI) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // ComplexCondition.g:60:61: SEMI complexcondition
                    	    {
                    	    match(input,SEMI,FOLLOW_SEMI_in_complexcondition70); 
                    	    pushFollow(FOLLOW_complexcondition_in_complexcondition72);
                    	    complexcondition();

                    	    state._fsp--;

                    	    i++;

                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);

                    match(input,CBC,FOLLOW_CBC_in_complexcondition78); 
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
    // ComplexCondition.g:62:1: dnf : disjunct ( OR disjunct )* ;
    public final ComplexConditionSOLO_ComplexCondition.dnf_return dnf() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.dnf_return retval = new ComplexConditionSOLO_ComplexCondition.dnf_return();
        retval.start = input.LT(1);

        try {
            // ComplexCondition.g:63:1: ( disjunct ( OR disjunct )* )
            // ComplexCondition.g:63:3: disjunct ( OR disjunct )*
            {
            pushFollow(FOLLOW_disjunct_in_dnf88);
            disjunct();

            state._fsp--;

            // ComplexCondition.g:63:12: ( OR disjunct )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==OR) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // ComplexCondition.g:63:13: OR disjunct
            	    {
            	    match(input,OR,FOLLOW_OR_in_dnf91); 
            	    pushFollow(FOLLOW_disjunct_in_dnf93);
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
    // ComplexCondition.g:65:1: disjunct : conjunct ( AND conjunct )* ;
    public final ComplexConditionSOLO_ComplexCondition.disjunct_return disjunct() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.disjunct_return retval = new ComplexConditionSOLO_ComplexCondition.disjunct_return();
        retval.start = input.LT(1);

        try {
            // ComplexCondition.g:66:1: ( conjunct ( AND conjunct )* )
            // ComplexCondition.g:66:3: conjunct ( AND conjunct )*
            {
            pushFollow(FOLLOW_conjunct_in_disjunct105);
            conjunct();

            state._fsp--;

            // ComplexCondition.g:66:12: ( AND conjunct )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==AND) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ComplexCondition.g:66:13: AND conjunct
            	    {
            	    match(input,AND,FOLLOW_AND_in_disjunct108); 
            	    pushFollow(FOLLOW_conjunct_in_disjunct110);
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
    // ComplexCondition.g:68:1: conjunct : ( condition | LP complexcondition RP | NOT conjunct );
    public final ComplexConditionSOLO_ComplexCondition.conjunct_return conjunct() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.conjunct_return retval = new ComplexConditionSOLO_ComplexCondition.conjunct_return();
        retval.start = input.LT(1);

        try {
            // ComplexCondition.g:69:1: ( condition | LP complexcondition RP | NOT conjunct )
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
                    // ComplexCondition.g:69:3: condition
                    {
                    pushFollow(FOLLOW_condition_in_conjunct123);
                    condition();

                    state._fsp--;


                    }
                    break;
                case 2 :
                    // ComplexCondition.g:70:3: LP complexcondition RP
                    {
                    match(input,LP,FOLLOW_LP_in_conjunct127); 
                    pushFollow(FOLLOW_complexcondition_in_conjunct129);
                    complexcondition();

                    state._fsp--;

                    match(input,RP,FOLLOW_RP_in_conjunct131); 
                    builder.complexcondition(input.toString(retval.start,input.LT(-1)));

                    }
                    break;
                case 3 :
                    // ComplexCondition.g:71:3: NOT conjunct
                    {
                    match(input,NOT,FOLLOW_NOT_in_conjunct137); 
                    pushFollow(FOLLOW_conjunct_in_conjunct139);
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
    // ComplexCondition.g:73:1: condition : (a= name ( type )? ( eq nod= nameOrDouble ( ORS nod2= nameOrDouble )* | (in= IN )? intervall ) | ( KNOWN | c= UNKNOWN ) SBO a= name ( type )? SBC | a= name ( type )? IN CBO b= name ( COMMA d= name )* CBC | a= name ( type )? ALL CBO b= name ( COMMA d= name )* CBC );
    public final ComplexConditionSOLO_ComplexCondition.condition_return condition() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.condition_return retval = new ComplexConditionSOLO_ComplexCondition.condition_return();
        retval.start = input.LT(1);

        Token in=null;
        Token c=null;
        ComplexConditionSOLO_ComplexCondition_BasicParser.name_return a = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.nameOrDouble_return nod = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.nameOrDouble_return nod2 = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.name_return b = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.name_return d = null;

        String type1 = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.eq_return eq2 = null;

        ComplexConditionSOLO_ComplexCondition.intervall_return intervall3 = null;

        String type4 = null;

        String type5 = null;

        String type6 = null;


        try {
            // ComplexCondition.g:74:1: (a= name ( type )? ( eq nod= nameOrDouble ( ORS nod2= nameOrDouble )* | (in= IN )? intervall ) | ( KNOWN | c= UNKNOWN ) SBO a= name ( type )? SBC | a= name ( type )? IN CBO b= name ( COMMA d= name )* CBC | a= name ( type )? ALL CBO b= name ( COMMA d= name )* CBC )
            int alt16=4;
            alt16 = dfa16.predict(input);
            switch (alt16) {
                case 1 :
                    // ComplexCondition.g:74:3: a= name ( type )? ( eq nod= nameOrDouble ( ORS nod2= nameOrDouble )* | (in= IN )? intervall )
                    {
                    pushFollow(FOLLOW_name_in_condition151);
                    a=gComplexConditionSOLO.name();

                    state._fsp--;

                    // ComplexCondition.g:74:10: ( type )?
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
                            // ComplexCondition.g:74:10: type
                            {
                            pushFollow(FOLLOW_type_in_condition153);
                            type1=gComplexConditionSOLO.type();

                            state._fsp--;


                            }
                            break;

                    }

                    // ComplexCondition.g:74:16: ( eq nod= nameOrDouble ( ORS nod2= nameOrDouble )* | (in= IN )? intervall )
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( ((LA9_0>=LE && LA9_0<=EQ)) ) {
                        alt9=1;
                    }
                    else if ( (LA9_0==SBO||LA9_0==IN) ) {
                        alt9=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 9, 0, input);

                        throw nvae;
                    }
                    switch (alt9) {
                        case 1 :
                            // ComplexCondition.g:74:17: eq nod= nameOrDouble ( ORS nod2= nameOrDouble )*
                            {
                            pushFollow(FOLLOW_eq_in_condition157);
                            eq2=gComplexConditionSOLO.eq();

                            state._fsp--;

                            pushFollow(FOLLOW_nameOrDouble_in_condition161);
                            nod=gComplexConditionSOLO.nameOrDouble();

                            state._fsp--;

                            builder.condition( ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type1, (eq2!=null?input.toString(eq2.start,eq2.stop):null), (nod!=null?nod.value:null));
                            // ComplexCondition.g:74:129: ( ORS nod2= nameOrDouble )*
                            loop7:
                            do {
                                int alt7=2;
                                int LA7_0 = input.LA(1);

                                if ( (LA7_0==ORS) ) {
                                    alt7=1;
                                }


                                switch (alt7) {
                            	case 1 :
                            	    // ComplexCondition.g:74:130: ORS nod2= nameOrDouble
                            	    {
                            	    match(input,ORS,FOLLOW_ORS_in_condition166); 
                            	    pushFollow(FOLLOW_nameOrDouble_in_condition170);
                            	    nod2=gComplexConditionSOLO.nameOrDouble();

                            	    state._fsp--;

                            	    builder.condition( ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type1, (eq2!=null?input.toString(eq2.start,eq2.stop):null), (nod2!=null?nod2.value:null)); builder.orcond(input.toString(retval.start,input.LT(-1)));

                            	    }
                            	    break;

                            	default :
                            	    break loop7;
                                }
                            } while (true);


                            }
                            break;
                        case 2 :
                            // ComplexCondition.g:75:3: (in= IN )? intervall
                            {
                            // ComplexCondition.g:75:5: (in= IN )?
                            int alt8=2;
                            int LA8_0 = input.LA(1);

                            if ( (LA8_0==IN) ) {
                                alt8=1;
                            }
                            switch (alt8) {
                                case 1 :
                                    // ComplexCondition.g:75:5: in= IN
                                    {
                                    in=(Token)match(input,IN,FOLLOW_IN_in_condition180); 

                                    }
                                    break;

                            }

                            pushFollow(FOLLOW_intervall_in_condition183);
                            intervall3=intervall();

                            state._fsp--;

                            builder.condition(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type1, (intervall3!=null?intervall3.a:null), (intervall3!=null?intervall3.b:null), (in!=null));

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // ComplexCondition.g:76:3: ( KNOWN | c= UNKNOWN ) SBO a= name ( type )? SBC
                    {
                    // ComplexCondition.g:76:3: ( KNOWN | c= UNKNOWN )
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==KNOWN) ) {
                        alt10=1;
                    }
                    else if ( (LA10_0==UNKNOWN) ) {
                        alt10=2;
                    }
                    else {
                        NoViableAltException nvae =
                            new NoViableAltException("", 10, 0, input);

                        throw nvae;
                    }
                    switch (alt10) {
                        case 1 :
                            // ComplexCondition.g:76:4: KNOWN
                            {
                            match(input,KNOWN,FOLLOW_KNOWN_in_condition193); 

                            }
                            break;
                        case 2 :
                            // ComplexCondition.g:76:10: c= UNKNOWN
                            {
                            c=(Token)match(input,UNKNOWN,FOLLOW_UNKNOWN_in_condition197); 

                            }
                            break;

                    }

                    match(input,SBO,FOLLOW_SBO_in_condition200); 
                    pushFollow(FOLLOW_name_in_condition204);
                    a=gComplexConditionSOLO.name();

                    state._fsp--;

                    // ComplexCondition.g:76:32: ( type )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==SBO) ) {
                        alt11=1;
                    }
                    switch (alt11) {
                        case 1 :
                            // ComplexCondition.g:76:32: type
                            {
                            pushFollow(FOLLOW_type_in_condition206);
                            type4=gComplexConditionSOLO.type();

                            state._fsp--;


                            }
                            break;

                    }

                    match(input,SBC,FOLLOW_SBC_in_condition209); 
                    builder.knowncondition(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type4, c!=null);

                    }
                    break;
                case 3 :
                    // ComplexCondition.g:77:3: a= name ( type )? IN CBO b= name ( COMMA d= name )* CBC
                    {
                    List<String> answers= new ArrayList();
                    pushFollow(FOLLOW_name_in_condition219);
                    a=gComplexConditionSOLO.name();

                    state._fsp--;

                    // ComplexCondition.g:77:51: ( type )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==SBO) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // ComplexCondition.g:77:51: type
                            {
                            pushFollow(FOLLOW_type_in_condition221);
                            type5=gComplexConditionSOLO.type();

                            state._fsp--;


                            }
                            break;

                    }

                    match(input,IN,FOLLOW_IN_in_condition224); 
                    match(input,CBO,FOLLOW_CBO_in_condition226); 
                    pushFollow(FOLLOW_name_in_condition230);
                    b=gComplexConditionSOLO.name();

                    state._fsp--;

                    answers.add((b!=null?b.value:null));
                    // ComplexCondition.g:77:96: ( COMMA d= name )*
                    loop13:
                    do {
                        int alt13=2;
                        int LA13_0 = input.LA(1);

                        if ( (LA13_0==COMMA) ) {
                            alt13=1;
                        }


                        switch (alt13) {
                    	case 1 :
                    	    // ComplexCondition.g:77:97: COMMA d= name
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_condition235); 
                    	    pushFollow(FOLLOW_name_in_condition239);
                    	    d=gComplexConditionSOLO.name();

                    	    state._fsp--;

                    	    answers.add((d!=null?d.value:null));

                    	    }
                    	    break;

                    	default :
                    	    break loop13;
                        }
                    } while (true);

                    match(input,CBC,FOLLOW_CBC_in_condition245); 
                    builder.in(((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (a!=null?a.value:null), type5, answers);

                    }
                    break;
                case 4 :
                    // ComplexCondition.g:78:3: a= name ( type )? ALL CBO b= name ( COMMA d= name )* CBC
                    {
                    List<String> answers= new ArrayList();
                    pushFollow(FOLLOW_name_in_condition255);
                    a=gComplexConditionSOLO.name();

                    state._fsp--;

                    // ComplexCondition.g:78:51: ( type )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==SBO) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // ComplexCondition.g:78:51: type
                            {
                            pushFollow(FOLLOW_type_in_condition257);
                            type6=gComplexConditionSOLO.type();

                            state._fsp--;


                            }
                            break;

                    }

                    match(input,ALL,FOLLOW_ALL_in_condition260); 
                    match(input,CBO,FOLLOW_CBO_in_condition262); 
                    pushFollow(FOLLOW_name_in_condition266);
                    b=gComplexConditionSOLO.name();

                    state._fsp--;

                    answers.add((b!=null?b.value:null));
                    // ComplexCondition.g:78:97: ( COMMA d= name )*
                    loop15:
                    do {
                        int alt15=2;
                        int LA15_0 = input.LA(1);

                        if ( (LA15_0==COMMA) ) {
                            alt15=1;
                        }


                        switch (alt15) {
                    	case 1 :
                    	    // ComplexCondition.g:78:98: COMMA d= name
                    	    {
                    	    match(input,COMMA,FOLLOW_COMMA_in_condition271); 
                    	    pushFollow(FOLLOW_name_in_condition275);
                    	    d=gComplexConditionSOLO.name();

                    	    state._fsp--;

                    	    answers.add((d!=null?d.value:null));

                    	    }
                    	    break;

                    	default :
                    	    break loop15;
                        }
                    } while (true);

                    match(input,CBC,FOLLOW_CBC_in_condition281); 
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
    // ComplexCondition.g:82:1: intervall returns [Double a, Double b] : SBO d1= d3double d2= d3double SBC ;
    public final ComplexConditionSOLO_ComplexCondition.intervall_return intervall() throws RecognitionException {
        ComplexConditionSOLO_ComplexCondition.intervall_return retval = new ComplexConditionSOLO_ComplexCondition.intervall_return();
        retval.start = input.LT(1);

        ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return d1 = null;

        ComplexConditionSOLO_ComplexCondition_BasicParser.d3double_return d2 = null;


        try {
            // ComplexCondition.g:83:1: ( SBO d1= d3double d2= d3double SBC )
            // ComplexCondition.g:83:3: SBO d1= d3double d2= d3double SBC
            {
            match(input,SBO,FOLLOW_SBO_in_intervall296); 
            pushFollow(FOLLOW_d3double_in_intervall300);
            d1=gComplexConditionSOLO.d3double();

            state._fsp--;

            pushFollow(FOLLOW_d3double_in_intervall304);
            d2=gComplexConditionSOLO.d3double();

            state._fsp--;

            match(input,SBC,FOLLOW_SBC_in_intervall306); 
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


    protected DFA16 dfa16 = new DFA16(this);
    static final String DFA16_eotS =
        "\15\uffff";
    static final String DFA16_eofS =
        "\15\uffff";
    static final String DFA16_minS =
        "\3\4\1\uffff\1\4\1\5\1\uffff\1\21\1\uffff\1\4\1\24\1\uffff\1\23";
    static final String DFA16_maxS =
        "\3\71\1\uffff\2\71\1\uffff\1\23\1\uffff\1\71\1\24\1\uffff\1\56";
    static final String DFA16_acceptS =
        "\3\uffff\1\2\2\uffff\1\4\1\uffff\1\1\2\uffff\1\3\1\uffff";
    static final String DFA16_specialS =
        "\15\uffff}>";
    static final String[] DFA16_transitionS = {
            "\1\1\1\2\42\uffff\2\3\17\uffff\1\2",
            "\1\4\1\2\15\uffff\1\5\1\uffff\5\10\22\uffff\1\7\1\uffff\1"+
            "\6\12\uffff\1\2",
            "\2\11\15\uffff\1\5\1\uffff\5\10\22\uffff\1\7\1\uffff\1\6\12"+
            "\uffff\1\11",
            "",
            "\1\4\1\2\63\uffff\1\2",
            "\1\10\25\uffff\1\10\35\uffff\1\12",
            "",
            "\1\13\1\uffff\1\10",
            "",
            "\2\11\15\uffff\1\5\1\uffff\5\10\22\uffff\1\7\1\uffff\1\6\12"+
            "\uffff\1\11",
            "\1\14",
            "",
            "\1\10\1\uffff\5\10\22\uffff\1\7\1\uffff\1\6"
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "73:1: condition : (a= name ( type )? ( eq nod= nameOrDouble ( ORS nod2= nameOrDouble )* | (in= IN )? intervall ) | ( KNOWN | c= UNKNOWN ) SBO a= name ( type )? SBC | a= name ( type )? IN CBO b= name ( COMMA d= name )* CBC | a= name ( type )? ALL CBO b= name ( COMMA d= name )* CBC );";
        }
    }
 

    public static final BitSet FOLLOW_dnf_in_complexcondition45 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINMAX_in_complexcondition51 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_LP_in_complexcondition53 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_complexcondition57 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_complexcondition61 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_complexcondition63 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_complexcondition65 = new BitSet(new long[]{0x02000B2000008030L});
    public static final BitSet FOLLOW_complexcondition_in_complexcondition67 = new BitSet(new long[]{0x0000000000040200L});
    public static final BitSet FOLLOW_SEMI_in_complexcondition70 = new BitSet(new long[]{0x02000B2000008030L});
    public static final BitSet FOLLOW_complexcondition_in_complexcondition72 = new BitSet(new long[]{0x0000000000040200L});
    public static final BitSet FOLLOW_CBC_in_complexcondition78 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_disjunct_in_dnf88 = new BitSet(new long[]{0x0000001000000002L});
    public static final BitSet FOLLOW_OR_in_dnf91 = new BitSet(new long[]{0x0200032000008030L});
    public static final BitSet FOLLOW_disjunct_in_dnf93 = new BitSet(new long[]{0x0000001000000002L});
    public static final BitSet FOLLOW_conjunct_in_disjunct105 = new BitSet(new long[]{0x0000000800000002L});
    public static final BitSet FOLLOW_AND_in_disjunct108 = new BitSet(new long[]{0x0200032000008030L});
    public static final BitSet FOLLOW_conjunct_in_disjunct110 = new BitSet(new long[]{0x0000000800000002L});
    public static final BitSet FOLLOW_condition_in_conjunct123 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_conjunct127 = new BitSet(new long[]{0x02000B2000008030L});
    public static final BitSet FOLLOW_complexcondition_in_conjunct129 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_conjunct131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_conjunct137 = new BitSet(new long[]{0x0200032000008030L});
    public static final BitSet FOLLOW_conjunct_in_conjunct139 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_condition151 = new BitSet(new long[]{0x0000100003E80000L});
    public static final BitSet FOLLOW_type_in_condition153 = new BitSet(new long[]{0x0000100003E80000L});
    public static final BitSet FOLLOW_eq_in_condition157 = new BitSet(new long[]{0x0200000008000430L});
    public static final BitSet FOLLOW_nameOrDouble_in_condition161 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_ORS_in_condition166 = new BitSet(new long[]{0x0200000008000430L});
    public static final BitSet FOLLOW_nameOrDouble_in_condition170 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_IN_in_condition180 = new BitSet(new long[]{0x0000100003E80000L});
    public static final BitSet FOLLOW_intervall_in_condition183 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_KNOWN_in_condition193 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_UNKNOWN_in_condition197 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_condition200 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_condition204 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_type_in_condition206 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_condition209 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_condition219 = new BitSet(new long[]{0x0000100000080000L});
    public static final BitSet FOLLOW_type_in_condition221 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_IN_in_condition224 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_condition226 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_condition230 = new BitSet(new long[]{0x0000000000040100L});
    public static final BitSet FOLLOW_COMMA_in_condition235 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_condition239 = new BitSet(new long[]{0x0000000000040100L});
    public static final BitSet FOLLOW_CBC_in_condition245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_condition255 = new BitSet(new long[]{0x0000400000080000L});
    public static final BitSet FOLLOW_type_in_condition257 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_ALL_in_condition260 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_condition262 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_condition266 = new BitSet(new long[]{0x0000000000040100L});
    public static final BitSet FOLLOW_COMMA_in_condition271 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_condition275 = new BitSet(new long[]{0x0000000000040100L});
    public static final BitSet FOLLOW_CBC_in_condition281 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_intervall296 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_intervall300 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_intervall304 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_intervall306 = new BitSet(new long[]{0x0000000000000002L});

}