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

// $ANTLR 3.1 BasicParser.g 2009-01-10 19:05:51

package de.d3web.KnOfficeParser.visio;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

import de.d3web.KnOfficeParser.ParserErrorHandler;

public class Visio_BasicParser extends Parser {
    public static final int Aidtext=67;
    public static final int LP=15;
    public static final int Textboxtext=65;
    public static final int NOT=37;
    public static final int Page=53;
    public static final int EXCEPT=39;
    public static final int DD=7;
    public static final int EOF=-1;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int T__90=90;
    public static final int EX=10;
    public static final int INCLUDE=47;
    public static final int NL=32;
    public static final int EQ=25;
    public static final int COMMENT=31;
    public static final int Shapestart=73;
    public static final int GE=23;
    public static final int G=24;
    public static final int SBC=20;
    public static final int T__80=80;
    public static final int T__81=81;
    public static final int T__82=82;
    public static final int T__83=83;
    public static final int L=22;
    public static final int NS=13;
    public static final int KNOWN=41;
    public static final int INT=5;
    public static final int T__85=85;
    public static final int T__84=84;
    public static final int T__87=87;
    public static final int T__86=86;
    public static final int T__89=89;
    public static final int T__88=88;
    public static final int Picture=62;
    public static final int Xcoord=55;
    public static final int WS=30;
    public static final int OR=36;
    public static final int SBO=19;
    public static final int Misc2=77;
    public static final int Misc3=78;
    public static final int Popup=66;
    public static final int YtoWith=74;
    public static final int T__79=79;
    public static final int End=70;
    public static final int Ycoord=56;
    public static final int Shapetext=61;
    public static final int HIDE=38;
    public static final int RP=16;
    public static final int ORS=12;
    public static final int MyDouble=60;
    public static final int ABSTRACT=49;
    public static final int AND=35;
    public static final int ID=52;
    public static final int Width=57;
    public static final int IF=33;
    public static final int AT=11;
    public static final int THEN=34;
    public static final int IN=44;
    public static final int UNKNOWN=40;
    public static final int COMMA=8;
    public static final int Height=58;
    public static final int ALL=45;
    public static final int PROD=28;
    public static final int Knowledge=68;
    public static final int TILDE=14;
    public static final int PLUS=26;
    public static final int String=4;
    public static final int DOT=6;
    public static final int HeighttoText=75;
    public static final int Start=69;
    public static final int Pagesheet=72;
    public static final int ALLOWEDNAMES=46;
    public static final int Misc=76;
    public static final int INSTANT=42;
    public static final int MINMAX=43;
    public static final int DEFAULT=48;
    public static final int SET=50;
    public static final int MINUS=27;
    public static final int SEMI=9;
    public static final int REF=51;
    public static final int QID=64;
    public static final int Box=63;
    public static final int CBC=18;
    public static final int Shape=54;
    public static final int Text=59;
    public static final int DIV=29;
    public static final int CBO=17;
    public static final int LE=21;
    public static final int Pagestart=71;

    // delegates
    // delegators
    public VisioParser gVisio;
    public VisioParser gParent;


        public Visio_BasicParser(TokenStream input, VisioParser gVisio) {
            this(input, new RecognizerSharedState(), gVisio);
        }
        public Visio_BasicParser(TokenStream input, RecognizerSharedState state, VisioParser gVisio) {
            super(input, state);
            this.gVisio = gVisio;
             
            gParent = gVisio;
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return VisioParser.tokenNames; }
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


    public static class name_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "name"
    // BasicParser.g:36:1: name returns [String value] : ( ( String )* ( ID | INT ) ( ID | INT | String )* | String );
    public final Visio_BasicParser.name_return name() throws RecognitionException {
        Visio_BasicParser.name_return retval = new Visio_BasicParser.name_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token String1=null;
        Token set2=null;
        Token set3=null;
        Token String4=null;

        Object String1_tree=null;
        Object set2_tree=null;
        Object set3_tree=null;
        Object String4_tree=null;

        try {
            // BasicParser.g:37:1: ( ( String )* ( ID | INT ) ( ID | INT | String )* | String )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==String) ) {
                int LA3_1 = input.LA(2);

                if ( ((LA3_1>=DOT && LA3_1<=DD)||LA3_1==SEMI||LA3_1==SBC||LA3_1==87||LA3_1==89||LA3_1==92) ) {
                    alt3=2;
                }
                else if ( ((LA3_1>=String && LA3_1<=INT)||LA3_1==ID) ) {
                    alt3=1;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 3, 1, input);

                    throw nvae;
                }
            }
            else if ( (LA3_0==INT||LA3_0==ID) ) {
                alt3=1;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // BasicParser.g:37:3: ( String )* ( ID | INT ) ( ID | INT | String )*
                    {
                    root_0 = (Object)adaptor.nil();

                    // BasicParser.g:37:3: ( String )*
                    loop1:
                    do {
                        int alt1=2;
                        int LA1_0 = input.LA(1);

                        if ( (LA1_0==String) ) {
                            alt1=1;
                        }


                        switch (alt1) {
                    	case 1 :
                    	    // BasicParser.g:37:3: String
                    	    {
                    	    String1=(Token)match(input,String,FOLLOW_String_in_name33); 
                    	    String1_tree = (Object)adaptor.create(String1);
                    	    adaptor.addChild(root_0, String1_tree);


                    	    }
                    	    break;

                    	default :
                    	    break loop1;
                        }
                    } while (true);

                    set2=(Token)input.LT(1);
                    if ( input.LA(1)==INT||input.LA(1)==ID ) {
                        input.consume();
                        adaptor.addChild(root_0, (Object)adaptor.create(set2));
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    // BasicParser.g:37:20: ( ID | INT | String )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( ((LA2_0>=String && LA2_0<=INT)||LA2_0==ID) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // BasicParser.g:
                    	    {
                    	    set3=(Token)input.LT(1);
                    	    if ( (input.LA(1)>=String && input.LA(1)<=INT)||input.LA(1)==ID ) {
                    	        input.consume();
                    	        adaptor.addChild(root_0, (Object)adaptor.create(set3));
                    	        state.errorRecovery=false;
                    	    }
                    	    else {
                    	        MismatchedSetException mse = new MismatchedSetException(null,input);
                    	        throw mse;
                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);

                    retval.value =input.toString(retval.start,input.LT(-1));

                    }
                    break;
                case 2 :
                    // BasicParser.g:38:3: String
                    {
                    root_0 = (Object)adaptor.nil();

                    String4=(Token)match(input,String,FOLLOW_String_in_name55); 
                    String4_tree = (Object)adaptor.create(String4);
                    adaptor.addChild(root_0, String4_tree);

                    retval.value =delQuotes((String4!=null?String4.getText():null));

                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "name"

    public static class type_return extends ParserRuleReturnScope {
        public String value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "type"
    // BasicParser.g:40:1: type returns [String value] : SBO name SBC ;
    public final Visio_BasicParser.type_return type() throws RecognitionException {
        Visio_BasicParser.type_return retval = new Visio_BasicParser.type_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token SBO5=null;
        Token SBC7=null;
        Visio_BasicParser.name_return name6 = null;


        Object SBO5_tree=null;
        Object SBC7_tree=null;

        try {
            // BasicParser.g:41:1: ( SBO name SBC )
            // BasicParser.g:41:3: SBO name SBC
            {
            root_0 = (Object)adaptor.nil();

            SBO5=(Token)match(input,SBO,FOLLOW_SBO_in_type69); 
            SBO5_tree = (Object)adaptor.create(SBO5);
            adaptor.addChild(root_0, SBO5_tree);

            pushFollow(FOLLOW_name_in_type71);
            name6=name();

            state._fsp--;

            adaptor.addChild(root_0, name6.getTree());
            SBC7=(Token)match(input,SBC,FOLLOW_SBC_in_type73); 
            SBC7_tree = (Object)adaptor.create(SBC7);
            adaptor.addChild(root_0, SBC7_tree);

            retval.value =(name6!=null?name6.value:null);

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "type"

    public static class eq_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "eq"
    // BasicParser.g:43:1: eq : ( EQ | LE | L | GE | G );
    public final Visio_BasicParser.eq_return eq() throws RecognitionException {
        Visio_BasicParser.eq_return retval = new Visio_BasicParser.eq_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token set8=null;

        Object set8_tree=null;

        try {
            // BasicParser.g:43:5: ( EQ | LE | L | GE | G )
            // BasicParser.g:
            {
            root_0 = (Object)adaptor.nil();

            set8=(Token)input.LT(1);
            if ( (input.LA(1)>=LE && input.LA(1)<=EQ) ) {
                input.consume();
                adaptor.addChild(root_0, (Object)adaptor.create(set8));
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }


            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "eq"

    public static class eqncalc_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "eqncalc"
    // BasicParser.g:44:1: eqncalc : ( eq | PLUS EQ | MINUS EQ );
    public final Visio_BasicParser.eqncalc_return eqncalc() throws RecognitionException {
        Visio_BasicParser.eqncalc_return retval = new Visio_BasicParser.eqncalc_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token PLUS10=null;
        Token EQ11=null;
        Token MINUS12=null;
        Token EQ13=null;
        Visio_BasicParser.eq_return eq9 = null;


        Object PLUS10_tree=null;
        Object EQ11_tree=null;
        Object MINUS12_tree=null;
        Object EQ13_tree=null;

        try {
            // BasicParser.g:44:9: ( eq | PLUS EQ | MINUS EQ )
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
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // BasicParser.g:44:11: eq
                    {
                    root_0 = (Object)adaptor.nil();

                    pushFollow(FOLLOW_eq_in_eqncalc99);
                    eq9=eq();

                    state._fsp--;

                    adaptor.addChild(root_0, eq9.getTree());

                    }
                    break;
                case 2 :
                    // BasicParser.g:44:14: PLUS EQ
                    {
                    root_0 = (Object)adaptor.nil();

                    PLUS10=(Token)match(input,PLUS,FOLLOW_PLUS_in_eqncalc101); 
                    PLUS10_tree = (Object)adaptor.create(PLUS10);
                    adaptor.addChild(root_0, PLUS10_tree);

                    EQ11=(Token)match(input,EQ,FOLLOW_EQ_in_eqncalc103); 
                    EQ11_tree = (Object)adaptor.create(EQ11);
                    adaptor.addChild(root_0, EQ11_tree);


                    }
                    break;
                case 3 :
                    // BasicParser.g:44:22: MINUS EQ
                    {
                    root_0 = (Object)adaptor.nil();

                    MINUS12=(Token)match(input,MINUS,FOLLOW_MINUS_in_eqncalc105); 
                    MINUS12_tree = (Object)adaptor.create(MINUS12);
                    adaptor.addChild(root_0, MINUS12_tree);

                    EQ13=(Token)match(input,EQ,FOLLOW_EQ_in_eqncalc107); 
                    EQ13_tree = (Object)adaptor.create(EQ13);
                    adaptor.addChild(root_0, EQ13_tree);


                    }
                    break;

            }
            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "eqncalc"

    public static class d3double_return extends ParserRuleReturnScope {
        public Double value;
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "d3double"
    // BasicParser.g:46:1: d3double returns [Double value] : ( MINUS )? INT ( ( COMMA | DOT ) INT )? ;
    public final Visio_BasicParser.d3double_return d3double() throws RecognitionException {
        Visio_BasicParser.d3double_return retval = new Visio_BasicParser.d3double_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token MINUS14=null;
        Token INT15=null;
        Token set16=null;
        Token INT17=null;

        Object MINUS14_tree=null;
        Object INT15_tree=null;
        Object set16_tree=null;
        Object INT17_tree=null;

        try {
            // BasicParser.g:47:1: ( ( MINUS )? INT ( ( COMMA | DOT ) INT )? )
            // BasicParser.g:47:3: ( MINUS )? INT ( ( COMMA | DOT ) INT )?
            {
            root_0 = (Object)adaptor.nil();

            // BasicParser.g:47:3: ( MINUS )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==MINUS) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // BasicParser.g:47:3: MINUS
                    {
                    MINUS14=(Token)match(input,MINUS,FOLLOW_MINUS_in_d3double119); 
                    MINUS14_tree = (Object)adaptor.create(MINUS14);
                    adaptor.addChild(root_0, MINUS14_tree);


                    }
                    break;

            }

            INT15=(Token)match(input,INT,FOLLOW_INT_in_d3double122); 
            INT15_tree = (Object)adaptor.create(INT15);
            adaptor.addChild(root_0, INT15_tree);

            // BasicParser.g:47:14: ( ( COMMA | DOT ) INT )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==DOT||LA6_0==COMMA) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // BasicParser.g:47:15: ( COMMA | DOT ) INT
                    {
                    set16=(Token)input.LT(1);
                    if ( input.LA(1)==DOT||input.LA(1)==COMMA ) {
                        input.consume();
                        adaptor.addChild(root_0, (Object)adaptor.create(set16));
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }

                    INT17=(Token)match(input,INT,FOLLOW_INT_in_d3double131); 
                    INT17_tree = (Object)adaptor.create(INT17);
                    adaptor.addChild(root_0, INT17_tree);


                    }
                    break;

            }

            retval.value =parseDouble(input.toString(retval.start,input.LT(-1)));

            }

            retval.stop = input.LT(-1);

            retval.tree = (Object)adaptor.rulePostProcessing(root_0);
            adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
    	retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);

        }
        finally {
        }
        return retval;
    }
    // $ANTLR end "d3double"

    // Delegated rules


 

    public static final BitSet FOLLOW_String_in_name33 = new BitSet(new long[]{0x0010000000000030L});
    public static final BitSet FOLLOW_set_in_name36 = new BitSet(new long[]{0x0010000000000032L});
    public static final BitSet FOLLOW_set_in_name42 = new BitSet(new long[]{0x0010000000000032L});
    public static final BitSet FOLLOW_String_in_name55 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SBO_in_type69 = new BitSet(new long[]{0x0010000000000030L});
    public static final BitSet FOLLOW_name_in_type71 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_SBC_in_type73 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_set_in_eq0 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_eq_in_eqncalc99 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_eqncalc101 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_eqncalc103 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_eqncalc105 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_EQ_in_eqncalc107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_d3double119 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_d3double122 = new BitSet(new long[]{0x0000000000000142L});
    public static final BitSet FOLLOW_set_in_d3double125 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_d3double131 = new BitSet(new long[]{0x0000000000000002L});

}