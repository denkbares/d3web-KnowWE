// $ANTLR 3.1 D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g 2009-10-09 16:33:56

package de.d3web.KnOfficeParser.decisiontree;
import de.d3web.KnOfficeParser.ParserErrorHandler;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
/**
 * Grammatik f�r Entscheidungsb�ume
 * @author Markus Friedrich
 *
 */
public class DecisionTree extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "INIT", "ABSTRACT", "SET", "REF", "FUZZY", "DIVTEXT", "DIVNORM", "ID", "BLUB", "Tokens", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74"
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
    public DecisionTree_BasicParser gBasicParser;
    // delegators


        public DecisionTree(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DecisionTree(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            gBasicParser = new DecisionTree_BasicParser(input, state, this);         
        }
        

    public String[] getTokenNames() { return DecisionTree.tokenNames; }
    public String getGrammarFileName() { return "D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g"; }


      private int dashcount = 0;
      private DTBuilder builder;
      private ParserErrorHandler eh;
      
      public DecisionTree(CommonTokenStream tokens, DTBuilder builder, ParserErrorHandler eh) {
        this(tokens);
        this.builder=builder;
        this.eh=eh;
        gBasicParser.setEH(eh);
        eh.setTokenNames(tokenNames);
      }
      
      public void setBuilder(DTBuilder builder) {
        this.builder = builder;
      }
      
      public DTBuilder getBuilder() {
        return builder;
      }
      
      private String delQuotes(String s) {
        s=s.substring(1, s.length()-1);
        s=s.replace("\\\"", "\"");
        return s;
      }
      
      private Double parseGerDouble(String s) {
        s=s.replace(',', '.');
        return Double.parseDouble(s);
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
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:59:1: knowledge : ( line | NL )* ( deslimit )? ( description NL )* ( description )? ;
    public final void knowledge() throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:1: ( ( line | NL )* ( deslimit )? ( description NL )* ( description )? )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:3: ( line | NL )* ( deslimit )? ( description NL )* ( description )?
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:3: ( line | NL )*
            loop1:
            do {
                int alt1=3;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>=String && LA1_0<=INT)||LA1_0==MINUS||LA1_0==INCLUDE||LA1_0==ID) ) {
                    alt1=1;
                }
                else if ( (LA1_0==NL) ) {
                    alt1=2;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:4: line
            	    {
            	    pushFollow(FOLLOW_line_in_knowledge54);
            	    line();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:10: NL
            	    {
            	    match(input,NL,FOLLOW_NL_in_knowledge57); if (state.failed) return ;
            	    if ( state.backtracking==0 ) {
            	      builder.newLine();
            	    }

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:35: ( deslimit )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ALLOWEDNAMES) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:35: deslimit
                    {
                    pushFollow(FOLLOW_deslimit_in_knowledge62);
                    deslimit();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:45: ( description NL )*
            loop3:
            do {
                int alt3=2;
                alt3 = dfa3.predict(input);
                switch (alt3) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:46: description NL
            	    {
            	    pushFollow(FOLLOW_description_in_knowledge66);
            	    description();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    match(input,NL,FOLLOW_NL_in_knowledge68); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:63: ( description )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==ORS) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:60:63: description
                    {
                    pushFollow(FOLLOW_description_in_knowledge72);
                    description();

                    state._fsp--;
                    if (state.failed) return ;

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
    // $ANTLR end "knowledge"


    // $ANTLR start "line"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:62:1: line : ( questionclass | include | dashes {...}? ( numeric[i] | answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] ) ) NL ;
    public final void line() throws RecognitionException {
        int dashes1 = 0;


        int i=0;
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:64:1: ( ( questionclass | include | dashes {...}? ( numeric[i] | answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] ) ) NL )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:64:3: ( questionclass | include | dashes {...}? ( numeric[i] | answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] ) ) NL
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:64:3: ( questionclass | include | dashes {...}? ( numeric[i] | answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] ) )
            int alt6=3;
            switch ( input.LA(1) ) {
            case String:
            case INT:
            case ID:
                {
                alt6=1;
                }
                break;
            case INCLUDE:
                {
                alt6=2;
                }
                break;
            case MINUS:
                {
                alt6=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:64:4: questionclass
                    {
                    pushFollow(FOLLOW_questionclass_in_line86);
                    questionclass();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      builder.finishOldQuestionsandConditions(0);
                    }

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:65:2: include
                    {
                    pushFollow(FOLLOW_include_in_line91);
                    include();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:66:2: dashes {...}? ( numeric[i] | answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] )
                    {
                    pushFollow(FOLLOW_dashes_in_line94);
                    dashes1=dashes();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      i=dashes1;
                    }
                    if ( !(((i<=dashcount+1))) ) {
                        if (state.backtracking>0) {state.failed=true; return ;}
                        throw new FailedPredicateException(input, "line", "(i<=dashcount+1)");
                    }
                    if ( state.backtracking==0 ) {
                      builder.finishOldQuestionsandConditions(i);
                    }
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:66:88: ( numeric[i] | answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] )
                    int alt5=5;
                    alt5 = dfa5.predict(input);
                    switch (alt5) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:66:89: numeric[i]
                            {
                            pushFollow(FOLLOW_numeric_in_line101);
                            numeric(i);

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 2 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:66:100: answer[i]
                            {
                            pushFollow(FOLLOW_answer_in_line104);
                            answer(i);

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 3 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:66:110: question[i]
                            {
                            pushFollow(FOLLOW_question_in_line107);
                            question(i);

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 4 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:66:122: diagnosis[i]
                            {
                            pushFollow(FOLLOW_diagnosis_in_line110);
                            diagnosis(i);

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;
                        case 5 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:66:135: manyQCLinks[i]
                            {
                            pushFollow(FOLLOW_manyQCLinks_in_line113);
                            manyQCLinks(i);

                            state._fsp--;
                            if (state.failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }

            match(input,NL,FOLLOW_NL_in_line118); if (state.failed) return ;

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
    // $ANTLR end "line"

    public static class questionclass_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "questionclass"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:68:1: questionclass : name ( dialogannotations )? ;
    public final DecisionTree.questionclass_return questionclass() throws RecognitionException {
        DecisionTree.questionclass_return retval = new DecisionTree.questionclass_return();
        retval.start = input.LT(1);

        DecisionTree_BasicParser.name_return name2 = null;

        DecisionTree.dialogannotations_return dialogannotations3 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:69:1: ( name ( dialogannotations )? )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:69:3: name ( dialogannotations )?
            {
            pushFollow(FOLLOW_name_in_questionclass127);
            name2=name();

            state._fsp--;
            if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:69:8: ( dialogannotations )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==LP) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:69:8: dialogannotations
                    {
                    pushFollow(FOLLOW_dialogannotations_in_questionclass129);
                    dialogannotations3=dialogannotations();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              dashcount = 0;
            }
            if ( state.backtracking==0 ) {
              builder.addQuestionclass((name2!=null?name2.value:null), ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)), (dialogannotations3!=null?dialogannotations3.attribute:null), (dialogannotations3!=null?dialogannotations3.value:null));
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
    // $ANTLR end "questionclass"

    public static class question_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "question"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:71:1: question[int Dashes] : ( REF h= name | a= name ( synonyms )? ( TILDE b= name )? SBO ID SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP )? (c= ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS manualref )? ) ;
    public final DecisionTree.question_return question(int Dashes) throws RecognitionException {
        DecisionTree.question_return retval = new DecisionTree.question_return();
        retval.start = input.LT(1);

        Token c=null;
        Token ID4=null;
        DecisionTree_BasicParser.name_return h = null;

        DecisionTree_BasicParser.name_return a = null;

        DecisionTree_BasicParser.name_return b = null;

        DecisionTree_BasicParser.name_return e = null;

        DecisionTree_BasicParser.d3double_return f = null;

        DecisionTree_BasicParser.d3double_return g = null;

        DecisionTree.manualref_return manualref5 = null;

        List<String> synonyms6 = null;

        String idlink7 = null;

        DecisionTree.dialogannotations_return dialogannotations8 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:72:1: ( ( REF h= name | a= name ( synonyms )? ( TILDE b= name )? SBO ID SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP )? (c= ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS manualref )? ) )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:72:3: ( REF h= name | a= name ( synonyms )? ( TILDE b= name )? SBO ID SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP )? (c= ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS manualref )? )
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:72:3: ( REF h= name | a= name ( synonyms )? ( TILDE b= name )? SBO ID SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP )? (c= ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS manualref )? )
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==REF) ) {
                alt16=1;
            }
            else if ( ((LA16_0>=String && LA16_0<=INT)||LA16_0==ID) ) {
                alt16=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:72:4: REF h= name
                    {
                    match(input,REF,FOLLOW_REF_in_question145); if (state.failed) return retval;
                    pushFollow(FOLLOW_name_in_question149);
                    h=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {
                      builder.addQuestionLink(Dashes, (h!=null?h.value:null), (h!=null?((Token)h.start):null).getLine(), input.toString(retval.start,input.LT(-1)));
                    }

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:3: a= name ( synonyms )? ( TILDE b= name )? SBO ID SBC ( CBO e= name CBC )? ( LP f= d3double g= d3double RP )? (c= ABSTRACT )? ( idlink )? ( dialogannotations )? ( NS manualref )?
                    {
                    pushFollow(FOLLOW_name_in_question158);
                    a=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:10: ( synonyms )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);

                    if ( (LA8_0==CBO) ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:10: synonyms
                            {
                            pushFollow(FOLLOW_synonyms_in_question160);
                            synonyms6=synonyms();

                            state._fsp--;
                            if (state.failed) return retval;

                            }
                            break;

                    }

                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:20: ( TILDE b= name )?
                    int alt9=2;
                    int LA9_0 = input.LA(1);

                    if ( (LA9_0==TILDE) ) {
                        alt9=1;
                    }
                    switch (alt9) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:21: TILDE b= name
                            {
                            match(input,TILDE,FOLLOW_TILDE_in_question164); if (state.failed) return retval;
                            pushFollow(FOLLOW_name_in_question168);
                            b=name();

                            state._fsp--;
                            if (state.failed) return retval;

                            }
                            break;

                    }

                    match(input,SBO,FOLLOW_SBO_in_question172); if (state.failed) return retval;
                    ID4=(Token)match(input,ID,FOLLOW_ID_in_question174); if (state.failed) return retval;
                    match(input,SBC,FOLLOW_SBC_in_question176); if (state.failed) return retval;
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:47: ( CBO e= name CBC )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);

                    if ( (LA10_0==CBO) ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:48: CBO e= name CBC
                            {
                            match(input,CBO,FOLLOW_CBO_in_question179); if (state.failed) return retval;
                            pushFollow(FOLLOW_name_in_question183);
                            e=name();

                            state._fsp--;
                            if (state.failed) return retval;
                            match(input,CBC,FOLLOW_CBC_in_question185); if (state.failed) return retval;

                            }
                            break;

                    }

                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:65: ( LP f= d3double g= d3double RP )?
                    int alt11=2;
                    int LA11_0 = input.LA(1);

                    if ( (LA11_0==LP) ) {
                        int LA11_1 = input.LA(2);

                        if ( (LA11_1==INT||LA11_1==MINUS) ) {
                            alt11=1;
                        }
                    }
                    switch (alt11) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:66: LP f= d3double g= d3double RP
                            {
                            match(input,LP,FOLLOW_LP_in_question190); if (state.failed) return retval;
                            pushFollow(FOLLOW_d3double_in_question194);
                            f=d3double();

                            state._fsp--;
                            if (state.failed) return retval;
                            pushFollow(FOLLOW_d3double_in_question198);
                            g=d3double();

                            state._fsp--;
                            if (state.failed) return retval;
                            match(input,RP,FOLLOW_RP_in_question200); if (state.failed) return retval;

                            }
                            break;

                    }

                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:97: (c= ABSTRACT )?
                    int alt12=2;
                    int LA12_0 = input.LA(1);

                    if ( (LA12_0==ABSTRACT) ) {
                        alt12=1;
                    }
                    switch (alt12) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:97: c= ABSTRACT
                            {
                            c=(Token)match(input,ABSTRACT,FOLLOW_ABSTRACT_in_question206); if (state.failed) return retval;

                            }
                            break;

                    }

                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:108: ( idlink )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);

                    if ( (LA13_0==AT) ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:108: idlink
                            {
                            pushFollow(FOLLOW_idlink_in_question209);
                            idlink7=idlink();

                            state._fsp--;
                            if (state.failed) return retval;

                            }
                            break;

                    }

                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:116: ( dialogannotations )?
                    int alt14=2;
                    int LA14_0 = input.LA(1);

                    if ( (LA14_0==LP) ) {
                        alt14=1;
                    }
                    switch (alt14) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:116: dialogannotations
                            {
                            pushFollow(FOLLOW_dialogannotations_in_question212);
                            dialogannotations8=dialogannotations();

                            state._fsp--;
                            if (state.failed) return retval;

                            }
                            break;

                    }

                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:135: ( NS manualref )?
                    int alt15=2;
                    int LA15_0 = input.LA(1);

                    if ( (LA15_0==NS) ) {
                        alt15=1;
                    }
                    switch (alt15) {
                        case 1 :
                            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:74:136: NS manualref
                            {
                            match(input,NS,FOLLOW_NS_in_question216); if (state.failed) return retval;
                            pushFollow(FOLLOW_manualref_in_question218);
                            manualref5=manualref();

                            state._fsp--;
                            if (state.failed) return retval;

                            }
                            break;

                    }

                    if ( state.backtracking==0 ) {
                      builder.addQuestion(Dashes, (a!=null?a.value:null), (b!=null?b.value:null), (c!=null), (ID4!=null?ID4.getText():null), (manualref5!=null?input.toString(manualref5.start,manualref5.stop):null), (f!=null?f.value:null), (g!=null?g.value:null), (e!=null?e.value:null), synonyms6, (a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)), idlink7, (dialogannotations8!=null?dialogannotations8.attribute:null), (dialogannotations8!=null?dialogannotations8.value:null));
                    }

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              dashcount = Dashes;
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
    // $ANTLR end "question"

    public static class answer_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "answer"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:77:1: answer[int Dashes] : name ( synonyms )? ( idlink )? ( NS manualref )? (a= DEFAULT )? (b= INIT )? ;
    public final DecisionTree.answer_return answer(int Dashes) throws RecognitionException {
        DecisionTree.answer_return retval = new DecisionTree.answer_return();
        retval.start = input.LT(1);

        Token a=null;
        Token b=null;
        DecisionTree_BasicParser.name_return name9 = null;

        DecisionTree.manualref_return manualref10 = null;

        List<String> synonyms11 = null;

        String idlink12 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:1: ( name ( synonyms )? ( idlink )? ( NS manualref )? (a= DEFAULT )? (b= INIT )? )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:3: name ( synonyms )? ( idlink )? ( NS manualref )? (a= DEFAULT )? (b= INIT )?
            {
            pushFollow(FOLLOW_name_in_answer236);
            name9=name();

            state._fsp--;
            if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:8: ( synonyms )?
            int alt17=2;
            int LA17_0 = input.LA(1);

            if ( (LA17_0==CBO) ) {
                alt17=1;
            }
            switch (alt17) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:8: synonyms
                    {
                    pushFollow(FOLLOW_synonyms_in_answer238);
                    synonyms11=synonyms();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:18: ( idlink )?
            int alt18=2;
            int LA18_0 = input.LA(1);

            if ( (LA18_0==AT) ) {
                alt18=1;
            }
            switch (alt18) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:18: idlink
                    {
                    pushFollow(FOLLOW_idlink_in_answer241);
                    idlink12=idlink();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:26: ( NS manualref )?
            int alt19=2;
            int LA19_0 = input.LA(1);

            if ( (LA19_0==NS) ) {
                alt19=1;
            }
            switch (alt19) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:27: NS manualref
                    {
                    match(input,NS,FOLLOW_NS_in_answer245); if (state.failed) return retval;
                    pushFollow(FOLLOW_manualref_in_answer247);
                    manualref10=manualref();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:43: (a= DEFAULT )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( (LA20_0==DEFAULT) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:43: a= DEFAULT
                    {
                    a=(Token)match(input,DEFAULT,FOLLOW_DEFAULT_in_answer253); if (state.failed) return retval;

                    }
                    break;

            }

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:54: (b= INIT )?
            int alt21=2;
            int LA21_0 = input.LA(1);

            if ( (LA21_0==INIT) ) {
                alt21=1;
            }
            switch (alt21) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:78:54: b= INIT
                    {
                    b=(Token)match(input,INIT,FOLLOW_INIT_in_answer259); if (state.failed) return retval;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              dashcount = Dashes; builder.addAnswerOrQuestionLink(Dashes, (name9!=null?name9.value:null), (manualref10!=null?input.toString(manualref10.start,manualref10.stop):null), synonyms11, (a!=null), (b!=null), (name9!=null?((Token)name9.start):null).getLine(), input.toString(retval.start,input.LT(-1)), idlink12);
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
    // $ANTLR end "answer"

    public static class diagnosis_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "diagnosis"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:80:1: diagnosis[int Dashes] : a= name ( NS a= name )* (b= SET )? diagvalue ( link )? ( idlink )? ;
    public final DecisionTree.diagnosis_return diagnosis(int Dashes) throws RecognitionException {
        DecisionTree.diagnosis_return retval = new DecisionTree.diagnosis_return();
        retval.start = input.LT(1);

        Token b=null;
        DecisionTree_BasicParser.name_return a = null;

        String diagvalue13 = null;

        DecisionTree.link_return link14 = null;

        String idlink15 = null;


        List<String> diags = new ArrayList<String>();
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:1: (a= name ( NS a= name )* (b= SET )? diagvalue ( link )? ( idlink )? )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:3: a= name ( NS a= name )* (b= SET )? diagvalue ( link )? ( idlink )?
            {
            pushFollow(FOLLOW_name_in_diagnosis279);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              diags.add((a!=null?a.value:null));
            }
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:33: ( NS a= name )*
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);

                if ( (LA22_0==NS) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:34: NS a= name
            	    {
            	    match(input,NS,FOLLOW_NS_in_diagnosis284); if (state.failed) return retval;
            	    pushFollow(FOLLOW_name_in_diagnosis288);
            	    a=name();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	      diags.add((a!=null?a.value:null));
            	    }

            	    }
            	    break;

            	default :
            	    break loop22;
                }
            } while (true);

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:70: (b= SET )?
            int alt23=2;
            int LA23_0 = input.LA(1);

            if ( (LA23_0==SET) ) {
                alt23=1;
            }
            switch (alt23) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:70: b= SET
                    {
                    b=(Token)match(input,SET,FOLLOW_SET_in_diagnosis296); if (state.failed) return retval;

                    }
                    break;

            }

            pushFollow(FOLLOW_diagvalue_in_diagnosis299);
            diagvalue13=diagvalue();

            state._fsp--;
            if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:86: ( link )?
            int alt24=2;
            int LA24_0 = input.LA(1);

            if ( (LA24_0==SBO) ) {
                alt24=1;
            }
            switch (alt24) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:86: link
                    {
                    pushFollow(FOLLOW_link_in_diagnosis301);
                    link14=link();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:92: ( idlink )?
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==AT) ) {
                alt25=1;
            }
            switch (alt25) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:82:92: idlink
                    {
                    pushFollow(FOLLOW_idlink_in_diagnosis304);
                    idlink15=idlink();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              dashcount = Dashes; builder.addDiagnosis(Dashes, diags, (b!=null), diagvalue13, (link14!=null?link14.s1:null), (link14!=null?link14.s2:null) ,(a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)), idlink15);
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
    // $ANTLR end "diagnosis"

    public static class numeric_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "numeric"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:84:1: numeric[int Dashes] : (op= eq d1= d3double | SBO d1= d3double d2= d3double SBC ) ;
    public final DecisionTree.numeric_return numeric(int Dashes) throws RecognitionException {
        DecisionTree.numeric_return retval = new DecisionTree.numeric_return();
        retval.start = input.LT(1);

        DecisionTree_BasicParser.eq_return op = null;

        DecisionTree_BasicParser.d3double_return d1 = null;

        DecisionTree_BasicParser.d3double_return d2 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:85:1: ( (op= eq d1= d3double | SBO d1= d3double d2= d3double SBC ) )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:85:3: (op= eq d1= d3double | SBO d1= d3double d2= d3double SBC )
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:85:3: (op= eq d1= d3double | SBO d1= d3double d2= d3double SBC )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( ((LA26_0>=LE && LA26_0<=EQ)) ) {
                alt26=1;
            }
            else if ( (LA26_0==SBO) ) {
                alt26=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:85:4: op= eq d1= d3double
                    {
                    pushFollow(FOLLOW_eq_in_numeric320);
                    op=eq();

                    state._fsp--;
                    if (state.failed) return retval;
                    pushFollow(FOLLOW_d3double_in_numeric324);
                    d1=d3double();

                    state._fsp--;
                    if (state.failed) return retval;

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:85:22: SBO d1= d3double d2= d3double SBC
                    {
                    match(input,SBO,FOLLOW_SBO_in_numeric326); if (state.failed) return retval;
                    pushFollow(FOLLOW_d3double_in_numeric330);
                    d1=d3double();

                    state._fsp--;
                    if (state.failed) return retval;
                    pushFollow(FOLLOW_d3double_in_numeric334);
                    d2=d3double();

                    state._fsp--;
                    if (state.failed) return retval;
                    match(input,SBC,FOLLOW_SBC_in_numeric336); if (state.failed) return retval;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              dashcount = Dashes; builder.addNumericAnswer(Dashes, (d1!=null?d1.value:null), (d2!=null?d2.value:null), (op!=null?input.toString(op.start,op.stop):null), ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));
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
    // $ANTLR end "numeric"

    public static class manyQCLinks_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "manyQCLinks"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:87:1: manyQCLinks[int Dashes] : (a= name SEMI )+ a= name ;
    public final DecisionTree.manyQCLinks_return manyQCLinks(int Dashes) throws RecognitionException {
        DecisionTree.manyQCLinks_return retval = new DecisionTree.manyQCLinks_return();
        retval.start = input.LT(1);

        DecisionTree_BasicParser.name_return a = null;


        List<String> qcs = new ArrayList<String>();
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:89:1: ( (a= name SEMI )+ a= name )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:89:3: (a= name SEMI )+ a= name
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:89:3: (a= name SEMI )+
            int cnt27=0;
            loop27:
            do {
                int alt27=2;
                alt27 = dfa27.predict(input);
                switch (alt27) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:89:4: a= name SEMI
            	    {
            	    pushFollow(FOLLOW_name_in_manyQCLinks357);
            	    a=name();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	      qcs.add((a!=null?a.value:null));
            	    }
            	    match(input,SEMI,FOLLOW_SEMI_in_manyQCLinks361); if (state.failed) return retval;

            	    }
            	    break;

            	default :
            	    if ( cnt27 >= 1 ) break loop27;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(27, input);
                        throw eee;
                }
                cnt27++;
            } while (true);

            pushFollow(FOLLOW_name_in_manyQCLinks367);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              qcs.add((a!=null?a.value:null)); dashcount = Dashes; builder.addManyQuestionClassLink(Dashes, qcs, ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));
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
    // $ANTLR end "manyQCLinks"

    public static class include_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "include"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:91:1: include : INCLUDE EQ String G ;
    public final DecisionTree.include_return include() throws RecognitionException {
        DecisionTree.include_return retval = new DecisionTree.include_return();
        retval.start = input.LT(1);

        Token String16=null;

        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:92:1: ( INCLUDE EQ String G )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:92:3: INCLUDE EQ String G
            {
            match(input,INCLUDE,FOLLOW_INCLUDE_in_include377); if (state.failed) return retval;
            match(input,EQ,FOLLOW_EQ_in_include379); if (state.failed) return retval;
            String16=(Token)match(input,String,FOLLOW_String_in_include381); if (state.failed) return retval;
            match(input,G,FOLLOW_G_in_include383); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              builder.addInclude(delQuotes((String16!=null?String16.getText():null)), String16.getLine(), input.toString(retval.start,input.LT(-1)));
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
    // $ANTLR end "include"

    public static class deslimit_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "deslimit"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:94:1: deslimit : ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL ;
    public final DecisionTree.deslimit_return deslimit() throws RecognitionException {
        DecisionTree.deslimit_return retval = new DecisionTree.deslimit_return();
        retval.start = input.LT(1);

        DecisionTree.ids_return a = null;

        DecisionTree.ids_return b = null;


        List<String> allowedNames = new ArrayList<String>();
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:96:1: ( ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:96:3: ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL
            {
            match(input,ALLOWEDNAMES,FOLLOW_ALLOWEDNAMES_in_deslimit398); if (state.failed) return retval;
            match(input,EQ,FOLLOW_EQ_in_deslimit400); if (state.failed) return retval;
            match(input,CBO,FOLLOW_CBO_in_deslimit402); if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:96:23: (a= ids COMMA )*
            loop28:
            do {
                int alt28=2;
                alt28 = dfa28.predict(input);
                switch (alt28) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:96:24: a= ids COMMA
            	    {
            	    pushFollow(FOLLOW_ids_in_deslimit407);
            	    a=ids();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	      allowedNames.add((a!=null?input.toString(a.start,a.stop):null));
            	    }
            	    match(input,COMMA,FOLLOW_COMMA_in_deslimit410); if (state.failed) return retval;

            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);

            pushFollow(FOLLOW_ids_in_deslimit416);
            b=ids();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              allowedNames.add((b!=null?input.toString(b.start,b.stop):null));
            }
            match(input,CBC,FOLLOW_CBC_in_deslimit419); if (state.failed) return retval;
            match(input,NL,FOLLOW_NL_in_deslimit421); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              builder.setallowedNames(allowedNames, ((Token)retval.start).getLine(), input.toString(retval.start,input.LT(-1)));
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
    // $ANTLR end "deslimit"

    public static class ids_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "ids"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:98:1: ids : ( ID )+ ;
    public final DecisionTree.ids_return ids() throws RecognitionException {
        DecisionTree.ids_return retval = new DecisionTree.ids_return();
        retval.start = input.LT(1);

        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:99:1: ( ( ID )+ )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:99:3: ( ID )+
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:99:3: ( ID )+
            int cnt29=0;
            loop29:
            do {
                int alt29=2;
                int LA29_0 = input.LA(1);

                if ( (LA29_0==ID) ) {
                    alt29=1;
                }


                switch (alt29) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:99:3: ID
            	    {
            	    match(input,ID,FOLLOW_ID_in_ids431); if (state.failed) return retval;

            	    }
            	    break;

            	default :
            	    if ( cnt29 >= 1 ) break loop29;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(29, input);
                        throw eee;
                }
                cnt29++;
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
    // $ANTLR end "ids"

    public static class description_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "description"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:101:1: description : ORS AT a= name ORS c= name ORS ( ID DD DD )? b= name ORS destext ORS ;
    public final DecisionTree.description_return description() throws RecognitionException {
        DecisionTree.description_return retval = new DecisionTree.description_return();
        retval.start = input.LT(1);

        Token ID18=null;
        DecisionTree_BasicParser.name_return a = null;

        DecisionTree_BasicParser.name_return c = null;

        DecisionTree_BasicParser.name_return b = null;

        DecisionTree.destext_return destext17 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:102:1: ( ORS AT a= name ORS c= name ORS ( ID DD DD )? b= name ORS destext ORS )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:102:3: ORS AT a= name ORS c= name ORS ( ID DD DD )? b= name ORS destext ORS
            {
            match(input,ORS,FOLLOW_ORS_in_description440); if (state.failed) return retval;
            match(input,AT,FOLLOW_AT_in_description442); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_description446);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,ORS,FOLLOW_ORS_in_description448); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_description452);
            c=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,ORS,FOLLOW_ORS_in_description454); if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:102:32: ( ID DD DD )?
            int alt30=2;
            int LA30_0 = input.LA(1);

            if ( (LA30_0==ID) ) {
                int LA30_1 = input.LA(2);

                if ( (LA30_1==DD) ) {
                    alt30=1;
                }
            }
            switch (alt30) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:102:33: ID DD DD
                    {
                    ID18=(Token)match(input,ID,FOLLOW_ID_in_description457); if (state.failed) return retval;
                    match(input,DD,FOLLOW_DD_in_description459); if (state.failed) return retval;
                    match(input,DD,FOLLOW_DD_in_description461); if (state.failed) return retval;

                    }
                    break;

            }

            pushFollow(FOLLOW_name_in_description467);
            b=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,ORS,FOLLOW_ORS_in_description469); if (state.failed) return retval;
            pushFollow(FOLLOW_destext_in_description471);
            destext17=destext();

            state._fsp--;
            if (state.failed) return retval;
            match(input,ORS,FOLLOW_ORS_in_description473); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              builder.addDescription((a!=null?a.value:null), (c!=null?input.toString(c.start,c.stop):null), (b!=null?b.value:null), (destext17!=null?input.toString(destext17.start,destext17.stop):null), (a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)), (ID18!=null?ID18.getText():null));
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
    // $ANTLR end "description"


    // $ANTLR start "synonyms"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:104:1: synonyms returns [List<String> syn] : ( CBO (a= name SEMI )* b= name CBC ) ;
    public final List<String> synonyms() throws RecognitionException {
        List<String> syn = null;

        DecisionTree_BasicParser.name_return a = null;

        DecisionTree_BasicParser.name_return b = null;


        syn = new ArrayList<String>();
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:106:1: ( ( CBO (a= name SEMI )* b= name CBC ) )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:106:4: ( CBO (a= name SEMI )* b= name CBC )
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:106:4: ( CBO (a= name SEMI )* b= name CBC )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:106:5: CBO (a= name SEMI )* b= name CBC
            {
            match(input,CBO,FOLLOW_CBO_in_synonyms494); if (state.failed) return syn;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:106:8: (a= name SEMI )*
            loop31:
            do {
                int alt31=2;
                alt31 = dfa31.predict(input);
                switch (alt31) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:106:9: a= name SEMI
            	    {
            	    pushFollow(FOLLOW_name_in_synonyms498);
            	    a=name();

            	    state._fsp--;
            	    if (state.failed) return syn;
            	    match(input,SEMI,FOLLOW_SEMI_in_synonyms500); if (state.failed) return syn;
            	    if ( state.backtracking==0 ) {
            	      syn.add((a!=null?input.toString(a.start,a.stop):null));
            	    }

            	    }
            	    break;

            	default :
            	    break loop31;
                }
            } while (true);

            pushFollow(FOLLOW_name_in_synonyms508);
            b=name();

            state._fsp--;
            if (state.failed) return syn;
            if ( state.backtracking==0 ) {
              syn.add((b!=null?input.toString(b.start,b.stop):null));
            }
            match(input,CBC,FOLLOW_CBC_in_synonyms511); if (state.failed) return syn;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return syn;
    }
    // $ANTLR end "synonyms"


    // $ANTLR start "diagvalue"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:108:1: diagvalue returns [String value] : LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP ;
    public final String diagvalue() throws RecognitionException {
        String value = null;

        DecisionTree_BasicParser.d3double_return d3double19 = null;

        DecisionTree_BasicParser.name_return name20 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:1: ( LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:3: LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP
            {
            match(input,LP,FOLLOW_LP_in_diagvalue524); if (state.failed) return value;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:6: ( ( MINUS INT | INT DOT )=> d3double | name | EX )
            int alt32=3;
            int LA32_0 = input.LA(1);

            if ( (LA32_0==MINUS) && (synpred1_DecisionTree())) {
                alt32=1;
            }
            else if ( (LA32_0==INT) ) {
                int LA32_2 = input.LA(2);

                if ( (synpred1_DecisionTree()) ) {
                    alt32=1;
                }
                else if ( (true) ) {
                    alt32=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 32, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA32_0==String||LA32_0==ID) ) {
                alt32=2;
            }
            else if ( (LA32_0==EX) ) {
                alt32=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 32, 0, input);

                throw nvae;
            }
            switch (alt32) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:7: ( MINUS INT | INT DOT )=> d3double
                    {
                    pushFollow(FOLLOW_d3double_in_diagvalue540);
                    d3double19=d3double();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                      value =(d3double19!=null?d3double19.value:null).toString();
                    }

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:78: name
                    {
                    pushFollow(FOLLOW_name_in_diagvalue545);
                    name20=name();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                      value =(name20!=null?name20.value:null);
                    }

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:107: EX
                    {
                    match(input,EX,FOLLOW_EX_in_diagvalue551); if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                      value ="!";
                    }

                    }
                    break;

            }

            match(input,RP,FOLLOW_RP_in_diagvalue557); if (state.failed) return value;

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
    // $ANTLR end "diagvalue"

    public static class destext_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "destext"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:111:1: destext : ( options {greedy=false; } : ~ ORS )* ;
    public final DecisionTree.destext_return destext() throws RecognitionException {
        DecisionTree.destext_return retval = new DecisionTree.destext_return();
        retval.start = input.LT(1);

        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:112:1: ( ( options {greedy=false; } : ~ ORS )* )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:112:3: ( options {greedy=false; } : ~ ORS )*
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:112:3: ( options {greedy=false; } : ~ ORS )*
            loop33:
            do {
                int alt33=2;
                int LA33_0 = input.LA(1);

                if ( ((LA33_0>=String && LA33_0<=AT)||(LA33_0>=NS && LA33_0<=74)) ) {
                    alt33=1;
                }
                else if ( (LA33_0==ORS) ) {
                    alt33=2;
                }


                switch (alt33) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:112:31: ~ ORS
            	    {
            	    if ( (input.LA(1)>=String && input.LA(1)<=AT)||(input.LA(1)>=NS && input.LA(1)<=74) ) {
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
            	    break loop33;
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
    // $ANTLR end "destext"

    public static class link_return extends ParserRuleReturnScope {
        public String s1;
        public String s2;
    };

    // $ANTLR start "link"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:116:1: link returns [String s1, String s2] : SBO SBO a= name SBC ( SBO b= name SBC )? SBC ;
    public final DecisionTree.link_return link() throws RecognitionException {
        DecisionTree.link_return retval = new DecisionTree.link_return();
        retval.start = input.LT(1);

        DecisionTree_BasicParser.name_return a = null;

        DecisionTree_BasicParser.name_return b = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:117:1: ( SBO SBO a= name SBC ( SBO b= name SBC )? SBC )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:117:3: SBO SBO a= name SBC ( SBO b= name SBC )? SBC
            {
            match(input,SBO,FOLLOW_SBO_in_link594); if (state.failed) return retval;
            match(input,SBO,FOLLOW_SBO_in_link596); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_link600);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,SBC,FOLLOW_SBC_in_link602); if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:117:22: ( SBO b= name SBC )?
            int alt34=2;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==SBO) ) {
                alt34=1;
            }
            switch (alt34) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:117:23: SBO b= name SBC
                    {
                    match(input,SBO,FOLLOW_SBO_in_link605); if (state.failed) return retval;
                    pushFollow(FOLLOW_name_in_link609);
                    b=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    match(input,SBC,FOLLOW_SBC_in_link611); if (state.failed) return retval;

                    }
                    break;

            }

            match(input,SBC,FOLLOW_SBC_in_link615); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              retval.s1 =(a!=null?input.toString(a.start,a.stop):null); retval.s2 =(b!=null?input.toString(b.start,b.stop):null);
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
    // $ANTLR end "link"


    // $ANTLR start "type"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:119:1: type : SBO ID SBC ;
    public final void type() throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:120:1: ( SBO ID SBC )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:120:3: SBO ID SBC
            {
            match(input,SBO,FOLLOW_SBO_in_type625); if (state.failed) return ;
            match(input,ID,FOLLOW_ID_in_type627); if (state.failed) return ;
            match(input,SBC,FOLLOW_SBC_in_type629); if (state.failed) return ;

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
    // $ANTLR end "type"


    // $ANTLR start "dashes"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:123:1: dashes returns [int i] : ( MINUS )+ ;
    public final int dashes() throws RecognitionException {
        int i = 0;

        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:124:1: ( ( MINUS )+ )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:124:3: ( MINUS )+
            {
            if ( state.backtracking==0 ) {
              i=0;
            }
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:124:10: ( MINUS )+
            int cnt35=0;
            loop35:
            do {
                int alt35=2;
                int LA35_0 = input.LA(1);

                if ( (LA35_0==MINUS) ) {
                    alt35=1;
                }


                switch (alt35) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:124:11: MINUS
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_dashes645); if (state.failed) return i;
            	    if ( state.backtracking==0 ) {
            	      i++;
            	    }

            	    }
            	    break;

            	default :
            	    if ( cnt35 >= 1 ) break loop35;
            	    if (state.backtracking>0) {state.failed=true; return i;}
                        EarlyExitException eee =
                            new EarlyExitException(35, input);
                        throw eee;
                }
                cnt35++;
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return i;
    }
    // $ANTLR end "dashes"

    public static class manualref_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "manualref"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:126:1: manualref : ( ID | INT )* ;
    public final DecisionTree.manualref_return manualref() throws RecognitionException {
        DecisionTree.manualref_return retval = new DecisionTree.manualref_return();
        retval.start = input.LT(1);

        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:126:10: ( ( ID | INT )* )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:127:1: ( ID | INT )*
            {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:127:1: ( ID | INT )*
            loop36:
            do {
                int alt36=2;
                int LA36_0 = input.LA(1);

                if ( (LA36_0==INT||LA36_0==ID) ) {
                    alt36=1;
                }


                switch (alt36) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:
            	    {
            	    if ( input.LA(1)==INT||input.LA(1)==ID ) {
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
            	    break loop36;
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
    // $ANTLR end "manualref"


    // $ANTLR start "idlink"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:129:1: idlink returns [String s] : AT name ;
    public final String idlink() throws RecognitionException {
        String s = null;

        DecisionTree_BasicParser.name_return name21 = null;


        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:129:26: ( AT name )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:130:1: AT name
            {
            match(input,AT,FOLLOW_AT_in_idlink673); if (state.failed) return s;
            pushFollow(FOLLOW_name_in_idlink675);
            name21=name();

            state._fsp--;
            if (state.failed) return s;
            if ( state.backtracking==0 ) {
              s =(name21!=null?input.toString(name21.start,name21.stop):null);
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return s;
    }
    // $ANTLR end "idlink"

    public static class dialogannotations_return extends ParserRuleReturnScope {
        public List<String> attribute;
        public List<String> value;
    };

    // $ANTLR start "dialogannotations"
    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:132:1: dialogannotations returns [List<String> attribute, List<String> value] : LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String ( SEMI )? RP ;
    public final DecisionTree.dialogannotations_return dialogannotations() throws RecognitionException {
        DecisionTree.dialogannotations_return retval = new DecisionTree.dialogannotations_return();
        retval.start = input.LT(1);

        Token b=null;
        DecisionTree_BasicParser.name_return a = null;


        retval.attribute = new ArrayList<String>(); retval.value = new ArrayList<String>();
        try {
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:134:1: ( LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String ( SEMI )? RP )
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:134:3: LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String ( SEMI )? RP
            {
            match(input,LP,FOLLOW_LP_in_dialogannotations694); if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:134:6: ( AT a= name DD b= String SEMI )*
            loop37:
            do {
                int alt37=2;
                alt37 = dfa37.predict(input);
                switch (alt37) {
            	case 1 :
            	    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:134:7: AT a= name DD b= String SEMI
            	    {
            	    match(input,AT,FOLLOW_AT_in_dialogannotations697); if (state.failed) return retval;
            	    pushFollow(FOLLOW_name_in_dialogannotations701);
            	    a=name();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    match(input,DD,FOLLOW_DD_in_dialogannotations703); if (state.failed) return retval;
            	    b=(Token)match(input,String,FOLLOW_String_in_dialogannotations707); if (state.failed) return retval;
            	    match(input,SEMI,FOLLOW_SEMI_in_dialogannotations709); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	      retval.attribute.add((a!=null?input.toString(a.start,a.stop):null)); retval.value.add(delQuotes((b!=null?b.getText():null)));
            	    }

            	    }
            	    break;

            	default :
            	    break loop37;
                }
            } while (true);

            match(input,AT,FOLLOW_AT_in_dialogannotations715); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_dialogannotations719);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,DD,FOLLOW_DD_in_dialogannotations721); if (state.failed) return retval;
            b=(Token)match(input,String,FOLLOW_String_in_dialogannotations725); if (state.failed) return retval;
            // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:134:117: ( SEMI )?
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==SEMI) ) {
                alt38=1;
            }
            switch (alt38) {
                case 1 :
                    // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:134:117: SEMI
                    {
                    match(input,SEMI,FOLLOW_SEMI_in_dialogannotations727); if (state.failed) return retval;

                    }
                    break;

            }

            match(input,RP,FOLLOW_RP_in_dialogannotations730); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              retval.attribute.add((a!=null?input.toString(a.start,a.stop):null)); retval.value.add(delQuotes((b!=null?b.getText():null)));
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
    // $ANTLR end "dialogannotations"

    // $ANTLR start synpred1_DecisionTree
    public final void synpred1_DecisionTree_fragment() throws RecognitionException {   
        // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:7: ( MINUS INT | INT DOT )
        int alt39=2;
        int LA39_0 = input.LA(1);

        if ( (LA39_0==MINUS) ) {
            alt39=1;
        }
        else if ( (LA39_0==INT) ) {
            alt39=2;
        }
        else {
            if (state.backtracking>0) {state.failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("", 39, 0, input);

            throw nvae;
        }
        switch (alt39) {
            case 1 :
                // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:8: MINUS INT
                {
                match(input,MINUS,FOLLOW_MINUS_in_synpred1_DecisionTree528); if (state.failed) return ;
                match(input,INT,FOLLOW_INT_in_synpred1_DecisionTree530); if (state.failed) return ;

                }
                break;
            case 2 :
                // D:\\eclipse workspaces\\2009-07\\d3web-KnOfficeParser\\Grammars\\DecisionTree.g:109:20: INT DOT
                {
                match(input,INT,FOLLOW_INT_in_synpred1_DecisionTree534); if (state.failed) return ;
                match(input,DOT,FOLLOW_DOT_in_synpred1_DecisionTree536); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred1_DecisionTree

    // Delegated rules
    public DecisionTree_BasicParser.name_return name() throws RecognitionException { return gBasicParser.name(); }
    public DecisionTree_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }
    public DecisionTree_BasicParser.eq_return eq() throws RecognitionException { return gBasicParser.eq(); }
    public DecisionTree_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException { return gBasicParser.nameOrDouble(); }
    public void eqncalc() throws RecognitionException { gBasicParser.eqncalc(); }

    public final boolean synpred1_DecisionTree() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_DecisionTree_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA3 dfa3 = new DFA3(this);
    protected DFA5 dfa5 = new DFA5(this);
    protected DFA27 dfa27 = new DFA27(this);
    protected DFA28 dfa28 = new DFA28(this);
    protected DFA31 dfa31 = new DFA31(this);
    protected DFA37 dfa37 = new DFA37(this);
    static final String DFA3_eotS =
        "\31\uffff";
    static final String DFA3_eofS =
        "\1\2\26\uffff\1\2\1\uffff";
    static final String DFA3_minS =
        "\1\14\1\13\1\uffff\16\4\1\7\5\4\1\40\1\uffff";
    static final String DFA3_maxS =
        "\1\14\1\13\1\uffff\16\71\1\7\1\71\1\112\2\71\1\112\1\40\1\uffff";
    static final String DFA3_acceptS =
        "\2\uffff\1\2\25\uffff\1\1";
    static final String DFA3_specialS =
        "\31\uffff}>";
    static final String[] DFA3_transitionS = {
            "\1\1",
            "\1\3",
            "",
            "\1\4\1\5\63\uffff\1\5",
            "\1\7\1\5\6\uffff\1\6\54\uffff\1\5",
            "\2\10\6\uffff\1\6\54\uffff\1\10",
            "\1\11\1\12\63\uffff\1\12",
            "\1\7\1\5\63\uffff\1\5",
            "\2\10\6\uffff\1\6\54\uffff\1\10",
            "\1\14\1\12\6\uffff\1\13\54\uffff\1\12",
            "\2\15\6\uffff\1\13\54\uffff\1\15",
            "\1\17\1\20\63\uffff\1\16",
            "\1\14\1\12\63\uffff\1\12",
            "\2\15\6\uffff\1\13\54\uffff\1\15",
            "\2\22\1\uffff\1\21\4\uffff\1\23\54\uffff\1\22",
            "\1\24\1\20\6\uffff\1\23\54\uffff\1\20",
            "\2\22\6\uffff\1\23\54\uffff\1\22",
            "\1\25",
            "\2\22\6\uffff\1\23\54\uffff\1\22",
            "\10\26\1\27\76\26",
            "\1\24\1\20\63\uffff\1\20",
            "\1\17\1\20\63\uffff\1\20",
            "\10\26\1\27\76\26",
            "\1\30",
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
            return "()* loopback of 60:45: ( description NL )*";
        }
    }
    static final String DFA5_eotS =
        "\24\uffff";
    static final String DFA5_eofS =
        "\24\uffff";
    static final String DFA5_minS =
        "\1\4\1\uffff\2\4\1\uffff\2\4\1\uffff\1\4\2\uffff\6\4\1\13\2\4";
    static final String DFA5_maxS =
        "\1\71\1\uffff\2\71\1\uffff\2\71\1\uffff\1\71\2\uffff\6\71\1\62"+
        "\2\71";
    static final String DFA5_acceptS =
        "\1\uffff\1\1\2\uffff\1\3\2\uffff\1\2\1\uffff\1\5\1\4\11\uffff";
    static final String DFA5_specialS =
        "\24\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\2\1\3\15\uffff\1\1\1\uffff\5\1\33\uffff\1\4\3\uffff\1\3",
            "",
            "\1\5\1\3\3\uffff\1\11\1\uffff\1\7\1\uffff\1\10\1\4\1\12\1"+
            "\uffff\1\6\1\uffff\1\4\14\uffff\1\7\20\uffff\2\7\1\uffff\1\12"+
            "\4\uffff\1\3",
            "\2\13\3\uffff\1\11\1\uffff\1\7\1\uffff\1\10\1\4\1\12\1\uffff"+
            "\1\6\1\uffff\1\4\14\uffff\1\7\20\uffff\2\7\1\uffff\1\12\4\uffff"+
            "\1\13",
            "",
            "\1\5\1\3\63\uffff\1\3",
            "\1\14\1\15\63\uffff\1\15",
            "",
            "\1\12\1\16\32\uffff\1\7\20\uffff\2\7\6\uffff\1\16",
            "",
            "",
            "\2\13\3\uffff\1\11\1\uffff\1\7\1\uffff\1\10\1\4\1\12\1\uffff"+
            "\1\6\1\uffff\1\4\14\uffff\1\7\20\uffff\2\7\1\uffff\1\12\4\uffff"+
            "\1\13",
            "\1\17\1\15\3\uffff\1\20\10\uffff\1\21\46\uffff\1\15",
            "\2\22\3\uffff\1\20\10\uffff\1\21\46\uffff\1\22",
            "\1\12\1\23\7\uffff\1\12\1\uffff\1\12\20\uffff\1\7\20\uffff"+
            "\2\7\1\uffff\1\12\4\uffff\1\23",
            "\1\17\1\15\63\uffff\1\15",
            "\1\14\1\15\63\uffff\1\15",
            "\1\7\1\uffff\1\7\1\4\4\uffff\1\4\14\uffff\1\7\20\uffff\2\7",
            "\2\22\3\uffff\1\20\10\uffff\1\21\46\uffff\1\22",
            "\1\12\1\23\7\uffff\1\12\1\uffff\1\12\20\uffff\1\7\20\uffff"+
            "\2\7\1\uffff\1\12\4\uffff\1\23"
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
            return "66:88: ( numeric[i] | answer[i] | question[i] | diagnosis[i] | manyQCLinks[i] )";
        }
    }
    static final String DFA27_eotS =
        "\7\uffff";
    static final String DFA27_eofS =
        "\7\uffff";
    static final String DFA27_minS =
        "\3\4\1\uffff\1\4\1\uffff\1\4";
    static final String DFA27_maxS =
        "\3\71\1\uffff\1\71\1\uffff\1\71";
    static final String DFA27_acceptS =
        "\3\uffff\1\2\1\uffff\1\1\1\uffff";
    static final String DFA27_specialS =
        "\7\uffff}>";
    static final String[] DFA27_transitionS = {
            "\1\1\1\2\63\uffff\1\2",
            "\1\4\1\2\3\uffff\1\5\26\uffff\1\3\30\uffff\1\2",
            "\2\6\3\uffff\1\5\26\uffff\1\3\30\uffff\1\6",
            "",
            "\1\4\1\2\63\uffff\1\2",
            "",
            "\2\6\3\uffff\1\5\26\uffff\1\3\30\uffff\1\6"
    };

    static final short[] DFA27_eot = DFA.unpackEncodedString(DFA27_eotS);
    static final short[] DFA27_eof = DFA.unpackEncodedString(DFA27_eofS);
    static final char[] DFA27_min = DFA.unpackEncodedStringToUnsignedChars(DFA27_minS);
    static final char[] DFA27_max = DFA.unpackEncodedStringToUnsignedChars(DFA27_maxS);
    static final short[] DFA27_accept = DFA.unpackEncodedString(DFA27_acceptS);
    static final short[] DFA27_special = DFA.unpackEncodedString(DFA27_specialS);
    static final short[][] DFA27_transition;

    static {
        int numStates = DFA27_transitionS.length;
        DFA27_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA27_transition[i] = DFA.unpackEncodedString(DFA27_transitionS[i]);
        }
    }

    class DFA27 extends DFA {

        public DFA27(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 27;
            this.eot = DFA27_eot;
            this.eof = DFA27_eof;
            this.min = DFA27_min;
            this.max = DFA27_max;
            this.accept = DFA27_accept;
            this.special = DFA27_special;
            this.transition = DFA27_transition;
        }
        public String getDescription() {
            return "()+ loopback of 89:3: (a= name SEMI )+";
        }
    }
    static final String DFA28_eotS =
        "\4\uffff";
    static final String DFA28_eofS =
        "\4\uffff";
    static final String DFA28_minS =
        "\1\71\1\10\2\uffff";
    static final String DFA28_maxS =
        "\2\71\2\uffff";
    static final String DFA28_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA28_specialS =
        "\4\uffff}>";
    static final String[] DFA28_transitionS = {
            "\1\1",
            "\1\2\11\uffff\1\3\46\uffff\1\1",
            "",
            ""
    };

    static final short[] DFA28_eot = DFA.unpackEncodedString(DFA28_eotS);
    static final short[] DFA28_eof = DFA.unpackEncodedString(DFA28_eofS);
    static final char[] DFA28_min = DFA.unpackEncodedStringToUnsignedChars(DFA28_minS);
    static final char[] DFA28_max = DFA.unpackEncodedStringToUnsignedChars(DFA28_maxS);
    static final short[] DFA28_accept = DFA.unpackEncodedString(DFA28_acceptS);
    static final short[] DFA28_special = DFA.unpackEncodedString(DFA28_specialS);
    static final short[][] DFA28_transition;

    static {
        int numStates = DFA28_transitionS.length;
        DFA28_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA28_transition[i] = DFA.unpackEncodedString(DFA28_transitionS[i]);
        }
    }

    class DFA28 extends DFA {

        public DFA28(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 28;
            this.eot = DFA28_eot;
            this.eof = DFA28_eof;
            this.min = DFA28_min;
            this.max = DFA28_max;
            this.accept = DFA28_accept;
            this.special = DFA28_special;
            this.transition = DFA28_transition;
        }
        public String getDescription() {
            return "()* loopback of 96:23: (a= ids COMMA )*";
        }
    }
    static final String DFA31_eotS =
        "\7\uffff";
    static final String DFA31_eofS =
        "\7\uffff";
    static final String DFA31_minS =
        "\4\4\2\uffff\1\4";
    static final String DFA31_maxS =
        "\4\71\2\uffff\1\71";
    static final String DFA31_acceptS =
        "\4\uffff\1\1\1\2\1\uffff";
    static final String DFA31_specialS =
        "\7\uffff}>";
    static final String[] DFA31_transitionS = {
            "\1\1\1\2\63\uffff\1\2",
            "\1\3\1\2\3\uffff\1\4\10\uffff\1\5\46\uffff\1\2",
            "\2\6\3\uffff\1\4\10\uffff\1\5\46\uffff\1\6",
            "\1\3\1\2\63\uffff\1\2",
            "",
            "",
            "\2\6\3\uffff\1\4\10\uffff\1\5\46\uffff\1\6"
    };

    static final short[] DFA31_eot = DFA.unpackEncodedString(DFA31_eotS);
    static final short[] DFA31_eof = DFA.unpackEncodedString(DFA31_eofS);
    static final char[] DFA31_min = DFA.unpackEncodedStringToUnsignedChars(DFA31_minS);
    static final char[] DFA31_max = DFA.unpackEncodedStringToUnsignedChars(DFA31_maxS);
    static final short[] DFA31_accept = DFA.unpackEncodedString(DFA31_acceptS);
    static final short[] DFA31_special = DFA.unpackEncodedString(DFA31_specialS);
    static final short[][] DFA31_transition;

    static {
        int numStates = DFA31_transitionS.length;
        DFA31_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA31_transition[i] = DFA.unpackEncodedString(DFA31_transitionS[i]);
        }
    }

    class DFA31 extends DFA {

        public DFA31(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 31;
            this.eot = DFA31_eot;
            this.eof = DFA31_eof;
            this.min = DFA31_min;
            this.max = DFA31_max;
            this.accept = DFA31_accept;
            this.special = DFA31_special;
            this.transition = DFA31_transition;
        }
        public String getDescription() {
            return "()* loopback of 106:8: (a= name SEMI )*";
        }
    }
    static final String DFA37_eotS =
        "\13\uffff";
    static final String DFA37_eofS =
        "\13\uffff";
    static final String DFA37_minS =
        "\1\13\6\4\1\11\1\13\2\uffff";
    static final String DFA37_maxS =
        "\1\13\3\71\1\4\2\71\2\20\2\uffff";
    static final String DFA37_acceptS =
        "\11\uffff\1\2\1\1";
    static final String DFA37_specialS =
        "\13\uffff}>";
    static final String[] DFA37_transitionS = {
            "\1\1",
            "\1\2\1\3\63\uffff\1\3",
            "\1\5\1\3\1\uffff\1\4\61\uffff\1\3",
            "\2\6\1\uffff\1\4\61\uffff\1\6",
            "\1\7",
            "\1\5\1\3\63\uffff\1\3",
            "\2\6\1\uffff\1\4\61\uffff\1\6",
            "\1\10\6\uffff\1\11",
            "\1\12\4\uffff\1\11",
            "",
            ""
    };

    static final short[] DFA37_eot = DFA.unpackEncodedString(DFA37_eotS);
    static final short[] DFA37_eof = DFA.unpackEncodedString(DFA37_eofS);
    static final char[] DFA37_min = DFA.unpackEncodedStringToUnsignedChars(DFA37_minS);
    static final char[] DFA37_max = DFA.unpackEncodedStringToUnsignedChars(DFA37_maxS);
    static final short[] DFA37_accept = DFA.unpackEncodedString(DFA37_acceptS);
    static final short[] DFA37_special = DFA.unpackEncodedString(DFA37_specialS);
    static final short[][] DFA37_transition;

    static {
        int numStates = DFA37_transitionS.length;
        DFA37_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA37_transition[i] = DFA.unpackEncodedString(DFA37_transitionS[i]);
        }
    }

    class DFA37 extends DFA {

        public DFA37(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 37;
            this.eot = DFA37_eot;
            this.eof = DFA37_eof;
            this.min = DFA37_min;
            this.max = DFA37_max;
            this.accept = DFA37_accept;
            this.special = DFA37_special;
            this.transition = DFA37_transition;
        }
        public String getDescription() {
            return "()* loopback of 134:6: ( AT a= name DD b= String SEMI )*";
        }
    }
 

    public static final BitSet FOLLOW_line_in_knowledge54 = new BitSet(new long[]{0x0201800108001032L});
    public static final BitSet FOLLOW_NL_in_knowledge57 = new BitSet(new long[]{0x0201800108001032L});
    public static final BitSet FOLLOW_deslimit_in_knowledge62 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_description_in_knowledge66 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NL_in_knowledge68 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_description_in_knowledge72 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_questionclass_in_line86 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_include_in_line91 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_dashes_in_line94 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_numeric_in_line101 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_answer_in_line104 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_question_in_line107 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_diagnosis_in_line110 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_manyQCLinks_in_line113 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NL_in_line118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_questionclass127 = new BitSet(new long[]{0x0000000000008002L});
    public static final BitSet FOLLOW_dialogannotations_in_questionclass129 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_REF_in_question145 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_question149 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_question158 = new BitSet(new long[]{0x00000000000A4000L});
    public static final BitSet FOLLOW_synonyms_in_question160 = new BitSet(new long[]{0x0000000000084000L});
    public static final BitSet FOLLOW_TILDE_in_question164 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_question168 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_question172 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ID_in_question174 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_question176 = new BitSet(new long[]{0x000800000002A802L});
    public static final BitSet FOLLOW_CBO_in_question179 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_question183 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CBC_in_question185 = new BitSet(new long[]{0x000800000000A802L});
    public static final BitSet FOLLOW_LP_in_question190 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_question194 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_question198 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_question200 = new BitSet(new long[]{0x000800000000A802L});
    public static final BitSet FOLLOW_ABSTRACT_in_question206 = new BitSet(new long[]{0x000000000000A802L});
    public static final BitSet FOLLOW_idlink_in_question209 = new BitSet(new long[]{0x000000000000A002L});
    public static final BitSet FOLLOW_dialogannotations_in_question212 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_NS_in_question216 = new BitSet(new long[]{0x0200000000000020L});
    public static final BitSet FOLLOW_manualref_in_question218 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_answer236 = new BitSet(new long[]{0x0006000000022802L});
    public static final BitSet FOLLOW_synonyms_in_answer238 = new BitSet(new long[]{0x0006000000002802L});
    public static final BitSet FOLLOW_idlink_in_answer241 = new BitSet(new long[]{0x0006000000002002L});
    public static final BitSet FOLLOW_NS_in_answer245 = new BitSet(new long[]{0x0206000000000020L});
    public static final BitSet FOLLOW_manualref_in_answer247 = new BitSet(new long[]{0x0006000000000002L});
    public static final BitSet FOLLOW_DEFAULT_in_answer253 = new BitSet(new long[]{0x0004000000000002L});
    public static final BitSet FOLLOW_INIT_in_answer259 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_diagnosis279 = new BitSet(new long[]{0x001000000000A000L});
    public static final BitSet FOLLOW_NS_in_diagnosis284 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_diagnosis288 = new BitSet(new long[]{0x001000000000A000L});
    public static final BitSet FOLLOW_SET_in_diagnosis296 = new BitSet(new long[]{0x001000000000A000L});
    public static final BitSet FOLLOW_diagvalue_in_diagnosis299 = new BitSet(new long[]{0x0000000000080802L});
    public static final BitSet FOLLOW_link_in_diagnosis301 = new BitSet(new long[]{0x0000000000000802L});
    public static final BitSet FOLLOW_idlink_in_diagnosis304 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_eq_in_numeric320 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_numeric324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_numeric326 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_numeric330 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_d3double_in_numeric334 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_numeric336 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_manyQCLinks357 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_manyQCLinks361 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_manyQCLinks367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INCLUDE_in_include377 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_include379 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_String_in_include381 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_G_in_include383 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALLOWEDNAMES_in_deslimit398 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_deslimit400 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_deslimit402 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ids_in_deslimit407 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_deslimit410 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ids_in_deslimit416 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CBC_in_deslimit419 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NL_in_deslimit421 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_ids431 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_ORS_in_description440 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AT_in_description442 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_description446 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORS_in_description448 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_description452 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORS_in_description454 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_ID_in_description457 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_description459 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_description461 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_description467 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORS_in_description469 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x00000000000007FFL});
    public static final BitSet FOLLOW_destext_in_description471 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORS_in_description473 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CBO_in_synonyms494 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_synonyms498 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_synonyms500 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_synonyms508 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CBC_in_synonyms511 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_diagvalue524 = new BitSet(new long[]{0x022000000BE80430L});
    public static final BitSet FOLLOW_d3double_in_diagvalue540 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_name_in_diagvalue545 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EX_in_diagvalue551 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_diagvalue557 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_destext577 = new BitSet(new long[]{0xFFFFFFFFFFFFEFF2L,0x00000000000007FFL});
    public static final BitSet FOLLOW_SBO_in_link594 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_link596 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_link600 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_link602 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_SBO_in_link605 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_link609 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_link611 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_link615 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_type625 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ID_in_type627 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_type629 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_dashes645 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_set_in_manualref657 = new BitSet(new long[]{0x0200000000000022L});
    public static final BitSet FOLLOW_AT_in_idlink673 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_idlink675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_dialogannotations694 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AT_in_dialogannotations697 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_dialogannotations701 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_dialogannotations703 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_String_in_dialogannotations707 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_dialogannotations709 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AT_in_dialogannotations715 = new BitSet(new long[]{0x0220000003E80030L});
    public static final BitSet FOLLOW_name_in_dialogannotations719 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_dialogannotations721 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_String_in_dialogannotations725 = new BitSet(new long[]{0x0000000000010200L});
    public static final BitSet FOLLOW_SEMI_in_dialogannotations727 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_dialogannotations730 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred1_DecisionTree528 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_synpred1_DecisionTree530 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_synpred1_DecisionTree534 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_DOT_in_synpred1_DecisionTree536 = new BitSet(new long[]{0x0000000000000002L});

}