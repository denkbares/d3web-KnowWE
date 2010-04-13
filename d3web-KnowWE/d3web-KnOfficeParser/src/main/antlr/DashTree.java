// $ANTLR 3.1.1 D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g 2010-04-06 16:15:58

package de.d3web.KnOfficeParser.dashtree;
import de.d3web.KnOfficeParser.ParserErrorHandler;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
/**
 * Grammatik fuer Entscheidungsbaeume
 * @author Markus Friedrich
 *
 */
public class DashTree extends Parser {
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
    public DashTree_BasicParser gBasicParser;
    // delegators


        public DashTree(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DashTree(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            gBasicParser = new DashTree_BasicParser(input, state, this);         
        }
        

    public String[] getTokenNames() { return DashTree.tokenNames; }
    public String getGrammarFileName() { return "D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g"; }


      private int dashcount = 0;
      private DashTBuilder builder;
      private ParserErrorHandler eh;
      
      public DashTree(CommonTokenStream tokens, DashTBuilder builder, ParserErrorHandler eh) {
        this(tokens);
        this.builder=builder;
        this.eh=eh;
        gBasicParser.setEH(eh);
        eh.setTokenNames(tokenNames);
      }
      
      public void setBuilder(DashTBuilder builder) {
        this.builder = builder;
      }
      
      public DashTBuilder getBuilder() {
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:79:1: knowledge : ( line | NL )* ( deslimit )? ( description NL )* ( description )? ;
    public final void knowledge() throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:1: ( ( line | NL )* ( deslimit )? ( description NL )* ( description )? )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:3: ( line | NL )* ( deslimit )? ( description NL )* ( description )?
            {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:3: ( line | NL )*
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
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:4: line
            	    {
            	    pushFollow(FOLLOW_line_in_knowledge57);
            	    line();

            	    state._fsp--;
            	    if (state.failed) return ;

            	    }
            	    break;
            	case 2 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:10: NL
            	    {
            	    match(input,NL,FOLLOW_NL_in_knowledge60); if (state.failed) return ;
            	    if ( state.backtracking==0 ) {
            	      builder.newLine();
            	    }

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:35: ( deslimit )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==ALLOWEDNAMES) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:35: deslimit
                    {
                    pushFollow(FOLLOW_deslimit_in_knowledge65);
                    deslimit();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:45: ( description NL )*
            loop3:
            do {
                int alt3=2;
                alt3 = dfa3.predict(input);
                switch (alt3) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:46: description NL
            	    {
            	    pushFollow(FOLLOW_description_in_knowledge69);
            	    description();

            	    state._fsp--;
            	    if (state.failed) return ;
            	    match(input,NL,FOLLOW_NL_in_knowledge71); if (state.failed) return ;

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:63: ( description )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( (LA4_0==ORS) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:80:63: description
                    {
                    pushFollow(FOLLOW_description_in_knowledge75);
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:82:1: line : ( node[0] | include | dashes ( node[i] ) ) NL ;
    public final void line() throws RecognitionException {
        int dashes1 = 0;


        int i=0;
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:84:1: ( ( node[0] | include | dashes ( node[i] ) ) NL )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:84:3: ( node[0] | include | dashes ( node[i] ) ) NL
            {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:84:3: ( node[0] | include | dashes ( node[i] ) )
            int alt5=3;
            switch ( input.LA(1) ) {
            case String:
            case INT:
            case ID:
                {
                alt5=1;
                }
                break;
            case INCLUDE:
                {
                alt5=2;
                }
                break;
            case MINUS:
                {
                alt5=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }

            switch (alt5) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:84:4: node[0]
                    {
                    pushFollow(FOLLOW_node_in_line89);
                    node(0);

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:85:2: include
                    {
                    pushFollow(FOLLOW_include_in_line93);
                    include();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:86:2: dashes ( node[i] )
                    {
                    pushFollow(FOLLOW_dashes_in_line96);
                    dashes1=dashes();

                    state._fsp--;
                    if (state.failed) return ;
                    if ( state.backtracking==0 ) {
                      i=dashes1;
                    }
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:86:23: ( node[i] )
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:86:24: node[i]
                    {
                    pushFollow(FOLLOW_node_in_line100);
                    node(i);

                    state._fsp--;
                    if (state.failed) return ;

                    }


                    }
                    break;

            }

            match(input,NL,FOLLOW_NL_in_line105); if (state.failed) return ;

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


    // $ANTLR start "node"
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:88:1: node[int Dashes] : a= name ( TILDE b= name )? ( SBO order SBC )? ;
    public final void node(int Dashes) throws RecognitionException {
        DashTree_BasicParser.name_return a = null;

        DashTree_BasicParser.name_return b = null;

        int order2 = 0;


        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:1: (a= name ( TILDE b= name )? ( SBO order SBC )? )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:3: a= name ( TILDE b= name )? ( SBO order SBC )?
            {
            pushFollow(FOLLOW_name_in_node118);
            a=name();

            state._fsp--;
            if (state.failed) return ;
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:10: ( TILDE b= name )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==TILDE) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:11: TILDE b= name
                    {
                    match(input,TILDE,FOLLOW_TILDE_in_node121); if (state.failed) return ;
                    pushFollow(FOLLOW_name_in_node125);
                    b=name();

                    state._fsp--;
                    if (state.failed) return ;

                    }
                    break;

            }

            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:26: ( SBO order SBC )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0==SBO) ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:89:27: SBO order SBC
                    {
                    match(input,SBO,FOLLOW_SBO_in_node130); if (state.failed) return ;
                    pushFollow(FOLLOW_order_in_node132);
                    order2=order();

                    state._fsp--;
                    if (state.failed) return ;
                    match(input,SBC,FOLLOW_SBC_in_node134); if (state.failed) return ;

                    }
                    break;

            }

            if ( state.backtracking==0 ) {
              dashcount = Dashes; builder.addNode(Dashes, (a!=null?a.value:null), (a!=null?((Token)a.start):null).getLine(), (b!=null?b.value:null), order2);
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
    // $ANTLR end "node"

    public static class include_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "include"
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:91:1: include : INCLUDE EQ String G ;
    public final DashTree.include_return include() throws RecognitionException {
        DashTree.include_return retval = new DashTree.include_return();
        retval.start = input.LT(1);

        Token String3=null;

        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:92:1: ( INCLUDE EQ String G )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:92:3: INCLUDE EQ String G
            {
            match(input,INCLUDE,FOLLOW_INCLUDE_in_include146); if (state.failed) return retval;
            match(input,EQ,FOLLOW_EQ_in_include148); if (state.failed) return retval;
            String3=(Token)match(input,String,FOLLOW_String_in_include150); if (state.failed) return retval;
            match(input,G,FOLLOW_G_in_include152); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              builder.addInclude(delQuotes((String3!=null?String3.getText():null)), String3.getLine(), input.toString(retval.start,input.LT(-1)));
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:94:1: deslimit : ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL ;
    public final DashTree.deslimit_return deslimit() throws RecognitionException {
        DashTree.deslimit_return retval = new DashTree.deslimit_return();
        retval.start = input.LT(1);

        DashTree.ids_return a = null;

        DashTree.ids_return b = null;


        List<String> allowedNames = new ArrayList<String>();
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:96:1: ( ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:96:3: ALLOWEDNAMES EQ CBO (a= ids COMMA )* b= ids CBC NL
            {
            match(input,ALLOWEDNAMES,FOLLOW_ALLOWEDNAMES_in_deslimit167); if (state.failed) return retval;
            match(input,EQ,FOLLOW_EQ_in_deslimit169); if (state.failed) return retval;
            match(input,CBO,FOLLOW_CBO_in_deslimit171); if (state.failed) return retval;
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:96:23: (a= ids COMMA )*
            loop8:
            do {
                int alt8=2;
                alt8 = dfa8.predict(input);
                switch (alt8) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:96:24: a= ids COMMA
            	    {
            	    pushFollow(FOLLOW_ids_in_deslimit176);
            	    a=ids();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	      allowedNames.add((a!=null?input.toString(a.start,a.stop):null));
            	    }
            	    match(input,COMMA,FOLLOW_COMMA_in_deslimit179); if (state.failed) return retval;

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);

            pushFollow(FOLLOW_ids_in_deslimit185);
            b=ids();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              allowedNames.add((b!=null?input.toString(b.start,b.stop):null));
            }
            match(input,CBC,FOLLOW_CBC_in_deslimit188); if (state.failed) return retval;
            match(input,NL,FOLLOW_NL_in_deslimit190); if (state.failed) return retval;
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


    // $ANTLR start "order"
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:98:1: order returns [int o] : INT ;
    public final int order() throws RecognitionException {
        int o = 0;

        Token INT4=null;

        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:99:1: ( INT )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:99:3: INT
            {
            INT4=(Token)match(input,INT,FOLLOW_INT_in_order204); if (state.failed) return o;
            if ( state.backtracking==0 ) {
              o =Integer.parseInt((INT4!=null?INT4.getText():null));
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return o;
    }
    // $ANTLR end "order"

    public static class ids_return extends ParserRuleReturnScope {
    };

    // $ANTLR start "ids"
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:101:1: ids : ( ID )+ ;
    public final DashTree.ids_return ids() throws RecognitionException {
        DashTree.ids_return retval = new DashTree.ids_return();
        retval.start = input.LT(1);

        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:102:1: ( ( ID )+ )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:102:3: ( ID )+
            {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:102:3: ( ID )+
            int cnt9=0;
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==ID) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:102:3: ID
            	    {
            	    match(input,ID,FOLLOW_ID_in_ids214); if (state.failed) return retval;

            	    }
            	    break;

            	default :
            	    if ( cnt9 >= 1 ) break loop9;
            	    if (state.backtracking>0) {state.failed=true; return retval;}
                        EarlyExitException eee =
                            new EarlyExitException(9, input);
                        throw eee;
                }
                cnt9++;
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:104:1: description : ORS AT a= name ORS c= name ORS b= name ORS destext ORS ;
    public final DashTree.description_return description() throws RecognitionException {
        DashTree.description_return retval = new DashTree.description_return();
        retval.start = input.LT(1);

        DashTree_BasicParser.name_return a = null;

        DashTree_BasicParser.name_return c = null;

        DashTree_BasicParser.name_return b = null;

        DashTree.destext_return destext5 = null;


        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:105:1: ( ORS AT a= name ORS c= name ORS b= name ORS destext ORS )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:105:3: ORS AT a= name ORS c= name ORS b= name ORS destext ORS
            {
            match(input,ORS,FOLLOW_ORS_in_description223); if (state.failed) return retval;
            match(input,AT,FOLLOW_AT_in_description225); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_description229);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,ORS,FOLLOW_ORS_in_description231); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_description235);
            c=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,ORS,FOLLOW_ORS_in_description237); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_description241);
            b=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,ORS,FOLLOW_ORS_in_description243); if (state.failed) return retval;
            pushFollow(FOLLOW_destext_in_description245);
            destext5=destext();

            state._fsp--;
            if (state.failed) return retval;
            match(input,ORS,FOLLOW_ORS_in_description247); if (state.failed) return retval;
            if ( state.backtracking==0 ) {
              builder.addDescription((a!=null?a.value:null), (c!=null?input.toString(c.start,c.stop):null), (b!=null?b.value:null), (destext5!=null?input.toString(destext5.start,destext5.stop):null), (a!=null?((Token)a.start):null).getLine(), input.toString(retval.start,input.LT(-1)));
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


    // $ANTLR start "diagvalue"
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:107:1: diagvalue returns [String value] : LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP ;
    public final String diagvalue() throws RecognitionException {
        String value = null;

        DashTree_BasicParser.d3double_return d3double6 = null;

        DashTree_BasicParser.name_return name7 = null;


        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:1: ( LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:3: LP ( ( MINUS INT | INT DOT )=> d3double | name | EX ) RP
            {
            match(input,LP,FOLLOW_LP_in_diagvalue261); if (state.failed) return value;
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:6: ( ( MINUS INT | INT DOT )=> d3double | name | EX )
            int alt10=3;
            int LA10_0 = input.LA(1);

            if ( (LA10_0==MINUS) && (synpred1_DashTree())) {
                alt10=1;
            }
            else if ( (LA10_0==INT) ) {
                int LA10_2 = input.LA(2);

                if ( (synpred1_DashTree()) ) {
                    alt10=1;
                }
                else if ( (true) ) {
                    alt10=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return value;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 10, 2, input);

                    throw nvae;
                }
            }
            else if ( (LA10_0==String||LA10_0==ID) ) {
                alt10=2;
            }
            else if ( (LA10_0==EX) ) {
                alt10=3;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:7: ( MINUS INT | INT DOT )=> d3double
                    {
                    pushFollow(FOLLOW_d3double_in_diagvalue277);
                    d3double6=d3double();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                      value =(d3double6!=null?d3double6.value:null).toString();
                    }

                    }
                    break;
                case 2 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:78: name
                    {
                    pushFollow(FOLLOW_name_in_diagvalue282);
                    name7=name();

                    state._fsp--;
                    if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                      value =(name7!=null?name7.value:null);
                    }

                    }
                    break;
                case 3 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:107: EX
                    {
                    match(input,EX,FOLLOW_EX_in_diagvalue288); if (state.failed) return value;
                    if ( state.backtracking==0 ) {
                      value ="!";
                    }

                    }
                    break;

            }

            match(input,RP,FOLLOW_RP_in_diagvalue294); if (state.failed) return value;

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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:110:1: destext : ( options {greedy=false; } : ~ ORS )* ;
    public final DashTree.destext_return destext() throws RecognitionException {
        DashTree.destext_return retval = new DashTree.destext_return();
        retval.start = input.LT(1);

        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:111:1: ( ( options {greedy=false; } : ~ ORS )* )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:111:3: ( options {greedy=false; } : ~ ORS )*
            {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:111:3: ( options {greedy=false; } : ~ ORS )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( ((LA11_0>=String && LA11_0<=AT)||(LA11_0>=NS && LA11_0<=74)) ) {
                    alt11=1;
                }
                else if ( (LA11_0==ORS) ) {
                    alt11=2;
                }


                switch (alt11) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:111:31: ~ ORS
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
            	    break loop11;
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:113:1: link returns [String s1, String s2] : SBO SBO a= name SBC ( SBO b= name SBC )? SBC ;
    public final DashTree.link_return link() throws RecognitionException {
        DashTree.link_return retval = new DashTree.link_return();
        retval.start = input.LT(1);

        DashTree_BasicParser.name_return a = null;

        DashTree_BasicParser.name_return b = null;


        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:114:1: ( SBO SBO a= name SBC ( SBO b= name SBC )? SBC )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:114:3: SBO SBO a= name SBC ( SBO b= name SBC )? SBC
            {
            match(input,SBO,FOLLOW_SBO_in_link329); if (state.failed) return retval;
            match(input,SBO,FOLLOW_SBO_in_link331); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_link335);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,SBC,FOLLOW_SBC_in_link337); if (state.failed) return retval;
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:114:22: ( SBO b= name SBC )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==SBO) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:114:23: SBO b= name SBC
                    {
                    match(input,SBO,FOLLOW_SBO_in_link340); if (state.failed) return retval;
                    pushFollow(FOLLOW_name_in_link344);
                    b=name();

                    state._fsp--;
                    if (state.failed) return retval;
                    match(input,SBC,FOLLOW_SBC_in_link346); if (state.failed) return retval;

                    }
                    break;

            }

            match(input,SBC,FOLLOW_SBC_in_link350); if (state.failed) return retval;
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:116:1: type : SBO ID SBC ;
    public final void type() throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:117:1: ( SBO ID SBC )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:117:3: SBO ID SBC
            {
            match(input,SBO,FOLLOW_SBO_in_type360); if (state.failed) return ;
            match(input,ID,FOLLOW_ID_in_type362); if (state.failed) return ;
            match(input,SBC,FOLLOW_SBC_in_type364); if (state.failed) return ;

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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:119:1: dashes returns [int i] : ( MINUS )+ ;
    public final int dashes() throws RecognitionException {
        int i = 0;

        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:120:1: ( ( MINUS )+ )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:120:3: ( MINUS )+
            {
            if ( state.backtracking==0 ) {
              i=0;
            }
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:120:10: ( MINUS )+
            int cnt13=0;
            loop13:
            do {
                int alt13=2;
                int LA13_0 = input.LA(1);

                if ( (LA13_0==MINUS) ) {
                    alt13=1;
                }


                switch (alt13) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:120:11: MINUS
            	    {
            	    match(input,MINUS,FOLLOW_MINUS_in_dashes379); if (state.failed) return i;
            	    if ( state.backtracking==0 ) {
            	      i++;
            	    }

            	    }
            	    break;

            	default :
            	    if ( cnt13 >= 1 ) break loop13;
            	    if (state.backtracking>0) {state.failed=true; return i;}
                        EarlyExitException eee =
                            new EarlyExitException(13, input);
                        throw eee;
                }
                cnt13++;
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


    // $ANTLR start "manualref"
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:122:1: manualref : ( ID | INT )* ;
    public final void manualref() throws RecognitionException {
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:122:10: ( ( ID | INT )* )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:123:1: ( ID | INT )*
            {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:123:1: ( ID | INT )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==INT||LA14_0==ID) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:
            	    {
            	    if ( input.LA(1)==INT||input.LA(1)==ID ) {
            	        input.consume();
            	        state.errorRecovery=false;state.failed=false;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return ;}
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop14;
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
    // $ANTLR end "manualref"


    // $ANTLR start "idlink"
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:125:1: idlink returns [String s] : AT name ;
    public final String idlink() throws RecognitionException {
        String s = null;

        DashTree_BasicParser.name_return name8 = null;


        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:125:26: ( AT name )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:126:1: AT name
            {
            match(input,AT,FOLLOW_AT_in_idlink407); if (state.failed) return s;
            pushFollow(FOLLOW_name_in_idlink409);
            name8=name();

            state._fsp--;
            if (state.failed) return s;
            if ( state.backtracking==0 ) {
              s =(name8!=null?input.toString(name8.start,name8.stop):null);
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:128:1: dialogannotations returns [List<String> attribute, List<String> value] : LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String ( SEMI )? RP ;
    public final DashTree.dialogannotations_return dialogannotations() throws RecognitionException {
        DashTree.dialogannotations_return retval = new DashTree.dialogannotations_return();
        retval.start = input.LT(1);

        Token b=null;
        DashTree_BasicParser.name_return a = null;


        retval.attribute = new ArrayList<String>(); retval.value = new ArrayList<String>();
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:1: ( LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String ( SEMI )? RP )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:3: LP ( AT a= name DD b= String SEMI )* AT a= name DD b= String ( SEMI )? RP
            {
            match(input,LP,FOLLOW_LP_in_dialogannotations428); if (state.failed) return retval;
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:6: ( AT a= name DD b= String SEMI )*
            loop15:
            do {
                int alt15=2;
                alt15 = dfa15.predict(input);
                switch (alt15) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:7: AT a= name DD b= String SEMI
            	    {
            	    match(input,AT,FOLLOW_AT_in_dialogannotations431); if (state.failed) return retval;
            	    pushFollow(FOLLOW_name_in_dialogannotations435);
            	    a=name();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    match(input,DD,FOLLOW_DD_in_dialogannotations437); if (state.failed) return retval;
            	    b=(Token)match(input,String,FOLLOW_String_in_dialogannotations441); if (state.failed) return retval;
            	    match(input,SEMI,FOLLOW_SEMI_in_dialogannotations443); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {
            	      retval.attribute.add((a!=null?input.toString(a.start,a.stop):null)); retval.value.add(delQuotes((b!=null?b.getText():null)));
            	    }

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            match(input,AT,FOLLOW_AT_in_dialogannotations449); if (state.failed) return retval;
            pushFollow(FOLLOW_name_in_dialogannotations453);
            a=name();

            state._fsp--;
            if (state.failed) return retval;
            match(input,DD,FOLLOW_DD_in_dialogannotations455); if (state.failed) return retval;
            b=(Token)match(input,String,FOLLOW_String_in_dialogannotations459); if (state.failed) return retval;
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:117: ( SEMI )?
            int alt16=2;
            int LA16_0 = input.LA(1);

            if ( (LA16_0==SEMI) ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:130:117: SEMI
                    {
                    match(input,SEMI,FOLLOW_SEMI_in_dialogannotations461); if (state.failed) return retval;

                    }
                    break;

            }

            match(input,RP,FOLLOW_RP_in_dialogannotations464); if (state.failed) return retval;
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

    // $ANTLR start synpred1_DashTree
    public final void synpred1_DashTree_fragment() throws RecognitionException {   
        // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:7: ( MINUS INT | INT DOT )
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
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:8: MINUS INT
                {
                match(input,MINUS,FOLLOW_MINUS_in_synpred1_DashTree265); if (state.failed) return ;
                match(input,INT,FOLLOW_INT_in_synpred1_DashTree267); if (state.failed) return ;

                }
                break;
            case 2 :
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\DashTree.g:108:20: INT DOT
                {
                match(input,INT,FOLLOW_INT_in_synpred1_DashTree271); if (state.failed) return ;
                match(input,DOT,FOLLOW_DOT_in_synpred1_DashTree273); if (state.failed) return ;

                }
                break;

        }}
    // $ANTLR end synpred1_DashTree

    // Delegated rules
    public void eqncalc() throws RecognitionException { gBasicParser.eqncalc(); }
    public DashTree_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }
    public void eq() throws RecognitionException { gBasicParser.eq(); }
    public DashTree_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException { return gBasicParser.nameOrDouble(); }
    public DashTree_BasicParser.name_return name() throws RecognitionException { return gBasicParser.name(); }

    public final boolean synpred1_DashTree() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred1_DashTree_fragment(); // can never throw exception
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
    protected DFA8 dfa8 = new DFA8(this);
    protected DFA15 dfa15 = new DFA15(this);
    static final String DFA3_eotS =
        "\26\uffff";
    static final String DFA3_eofS =
        "\1\2\23\uffff\1\2\1\uffff";
    static final String DFA3_minS =
        "\1\14\1\13\1\uffff\21\4\1\40\1\uffff";
    static final String DFA3_maxS =
        "\1\14\1\13\1\uffff\15\71\1\112\2\71\1\112\1\40\1\uffff";
    static final String DFA3_acceptS =
        "\2\uffff\1\2\22\uffff\1\1";
    static final String DFA3_specialS =
        "\26\uffff}>";
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
            "\1\16\1\17\63\uffff\1\17",
            "\1\14\1\12\63\uffff\1\12",
            "\2\15\6\uffff\1\13\54\uffff\1\15",
            "\1\21\1\17\6\uffff\1\20\54\uffff\1\17",
            "\2\22\6\uffff\1\20\54\uffff\1\22",
            "\10\23\1\24\76\23",
            "\1\21\1\17\63\uffff\1\17",
            "\2\22\6\uffff\1\20\54\uffff\1\22",
            "\10\23\1\24\76\23",
            "\1\25",
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
            return "()* loopback of 80:45: ( description NL )*";
        }
    }
    static final String DFA8_eotS =
        "\4\uffff";
    static final String DFA8_eofS =
        "\4\uffff";
    static final String DFA8_minS =
        "\1\71\1\10\2\uffff";
    static final String DFA8_maxS =
        "\2\71\2\uffff";
    static final String DFA8_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA8_specialS =
        "\4\uffff}>";
    static final String[] DFA8_transitionS = {
            "\1\1",
            "\1\2\11\uffff\1\3\46\uffff\1\1",
            "",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "()* loopback of 96:23: (a= ids COMMA )*";
        }
    }
    static final String DFA15_eotS =
        "\13\uffff";
    static final String DFA15_eofS =
        "\13\uffff";
    static final String DFA15_minS =
        "\1\13\6\4\1\11\1\13\2\uffff";
    static final String DFA15_maxS =
        "\1\13\4\71\1\4\1\71\2\20\2\uffff";
    static final String DFA15_acceptS =
        "\11\uffff\1\2\1\1";
    static final String DFA15_specialS =
        "\13\uffff}>";
    static final String[] DFA15_transitionS = {
            "\1\1",
            "\1\2\1\3\63\uffff\1\3",
            "\1\4\1\3\1\uffff\1\5\61\uffff\1\3",
            "\2\6\1\uffff\1\5\61\uffff\1\6",
            "\1\4\1\3\63\uffff\1\3",
            "\1\7",
            "\2\6\1\uffff\1\5\61\uffff\1\6",
            "\1\10\6\uffff\1\11",
            "\1\12\4\uffff\1\11",
            "",
            ""
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
            return "()* loopback of 130:6: ( AT a= name DD b= String SEMI )*";
        }
    }
 

    public static final BitSet FOLLOW_line_in_knowledge57 = new BitSet(new long[]{0x0201800108001032L});
    public static final BitSet FOLLOW_NL_in_knowledge60 = new BitSet(new long[]{0x0201800108001032L});
    public static final BitSet FOLLOW_deslimit_in_knowledge65 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_description_in_knowledge69 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NL_in_knowledge71 = new BitSet(new long[]{0x0000000000001002L});
    public static final BitSet FOLLOW_description_in_knowledge75 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_node_in_line89 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_include_in_line93 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_dashes_in_line96 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_node_in_line100 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NL_in_line105 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_node118 = new BitSet(new long[]{0x0000000000084002L});
    public static final BitSet FOLLOW_TILDE_in_node121 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_node125 = new BitSet(new long[]{0x0000000000080002L});
    public static final BitSet FOLLOW_SBO_in_node130 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_order_in_node132 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_node134 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INCLUDE_in_include146 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_include148 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_String_in_include150 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_G_in_include152 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALLOWEDNAMES_in_deslimit167 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_deslimit169 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_CBO_in_deslimit171 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ids_in_deslimit176 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_COMMA_in_deslimit179 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ids_in_deslimit185 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_CBC_in_deslimit188 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_NL_in_deslimit190 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_order204 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_ids214 = new BitSet(new long[]{0x0200000000000002L});
    public static final BitSet FOLLOW_ORS_in_description223 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AT_in_description225 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_description229 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORS_in_description231 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_description235 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORS_in_description237 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_description241 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORS_in_description243 = new BitSet(new long[]{0xFFFFFFFFFFFFFFF0L,0x00000000000007FFL});
    public static final BitSet FOLLOW_destext_in_description245 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_ORS_in_description247 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_diagvalue261 = new BitSet(new long[]{0x0200000008000430L});
    public static final BitSet FOLLOW_d3double_in_diagvalue277 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_name_in_diagvalue282 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_EX_in_diagvalue288 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_diagvalue294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_destext314 = new BitSet(new long[]{0xFFFFFFFFFFFFEFF2L,0x00000000000007FFL});
    public static final BitSet FOLLOW_SBO_in_link329 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_SBO_in_link331 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_link335 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_link337 = new BitSet(new long[]{0x0000000000180000L});
    public static final BitSet FOLLOW_SBO_in_link340 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_link344 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_link346 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_link350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_type360 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_ID_in_type362 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_type364 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_dashes379 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_set_in_manualref391 = new BitSet(new long[]{0x0200000000000022L});
    public static final BitSet FOLLOW_AT_in_idlink407 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_idlink409 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LP_in_dialogannotations428 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AT_in_dialogannotations431 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_dialogannotations435 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_dialogannotations437 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_String_in_dialogannotations441 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_dialogannotations443 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_AT_in_dialogannotations449 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_dialogannotations453 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_dialogannotations455 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_String_in_dialogannotations459 = new BitSet(new long[]{0x0000000000010200L});
    public static final BitSet FOLLOW_SEMI_in_dialogannotations461 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_RP_in_dialogannotations464 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_synpred1_DashTree265 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_synpred1_DashTree267 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_synpred1_DashTree271 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_DOT_in_synpred1_DashTree273 = new BitSet(new long[]{0x0000000000000002L});

}