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

// $ANTLR 3.1 D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g 2009-01-10 19:06:19

package de.d3web.KnOfficeParser.visio;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.BaseRecognizer;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.DFA;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.RewriteRuleSubtreeStream;
import org.antlr.runtime.tree.RewriteRuleTokenStream;
import org.antlr.runtime.tree.TreeAdaptor;

import de.d3web.KnOfficeParser.ParserErrorHandler;

/**
 * Grammatik zum Einlesen von Visiofiles und überführen selbiger in die Standard ANTLR-Baumrepräsenation
 * @author Markus Friedrich
 *
 */
public class VisioParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "ABSTRACT", "SET", "REF", "ID", "Page", "Shape", "Xcoord", "Ycoord", "Width", "Height", "Text", "MyDouble", "Shapetext", "Picture", "Box", "QID", "Textboxtext", "Popup", "Aidtext", "Knowledge", "Start", "End", "Pagestart", "Pagesheet", "Shapestart", "YtoWith", "HeighttoText", "Misc", "Misc2", "Misc3", "'</Pages>'", "'</Page>'", "'<Shapes>'", "'</Shapes>'", "'</PinX>'", "'<PinY>'", "'</Width>'", "'<Height>'", "'</Text></Shape>'", "'Bildname:'", "'Größe:'", "'x'", "'Frage:'", "'Folgefragen:'"
    };
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
    public Visio_BasicParser gBasicParser;
    // delegators


        public VisioParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public VisioParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            gBasicParser = new Visio_BasicParser(input, state, this);         
        }
        
    protected TreeAdaptor adaptor = new CommonTreeAdaptor();

    public void setTreeAdaptor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
        gBasicParser.setTreeAdaptor(this.adaptor);
    }
    public TreeAdaptor getTreeAdaptor() {
        return adaptor;
    }

    public String[] getTokenNames() { return VisioParser.tokenNames; }
    public String getGrammarFileName() { return "D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g"; }


      private ParserErrorHandler eh;
      
      public VisioParser(CommonTokenStream tokens, ParserErrorHandler eh) {
        this(tokens);
        this.eh=eh;
        gBasicParser.setEH(eh);
        if (eh!=null) eh.setTokenNames(tokenNames);
      }
      
      @Override
      public void reportError(RecognitionException re) {
        if (eh!=null) {
          eh.parsererror(re);
        } else {
          super.reportError(re);
        }
      }


    public static class knowledge_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "knowledge"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:82:1: knowledge : Start ( page )* '</Pages>' End -> ^( Knowledge ( page )* ) ;
    public final VisioParser.knowledge_return knowledge() throws RecognitionException {
        VisioParser.knowledge_return retval = new VisioParser.knowledge_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token Start1=null;
        Token string_literal3=null;
        Token End4=null;
        VisioParser.page_return page2 = null;


        Object Start1_tree=null;
        Object string_literal3_tree=null;
        Object End4_tree=null;
        RewriteRuleTokenStream stream_79=new RewriteRuleTokenStream(adaptor,"token 79");
        RewriteRuleTokenStream stream_End=new RewriteRuleTokenStream(adaptor,"token End");
        RewriteRuleTokenStream stream_Start=new RewriteRuleTokenStream(adaptor,"token Start");
        RewriteRuleSubtreeStream stream_page=new RewriteRuleSubtreeStream(adaptor,"rule page");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:82:11: ( Start ( page )* '</Pages>' End -> ^( Knowledge ( page )* ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:82:13: Start ( page )* '</Pages>' End
            {
            Start1=(Token)match(input,Start,FOLLOW_Start_in_knowledge150);  
            stream_Start.add(Start1);

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:82:20: ( page )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==Pagestart) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:82:20: page
            	    {
            	    pushFollow(FOLLOW_page_in_knowledge153);
            	    page2=page();

            	    state._fsp--;

            	    stream_page.add(page2.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            string_literal3=(Token)match(input,79,FOLLOW_79_in_knowledge156);  
            stream_79.add(string_literal3);

            End4=(Token)match(input,End,FOLLOW_End_in_knowledge158);  
            stream_End.add(End4);



            // AST REWRITE
            // elements: page
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 82:41: -> ^( Knowledge ( page )* )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:82:44: ^( Knowledge ( page )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Knowledge, "Knowledge"), root_1);

                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:82:56: ( page )*
                while ( stream_page.hasNext() ) {
                    adaptor.addChild(root_1, stream_page.nextTree());

                }
                stream_page.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "knowledge"

    public static class page_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "page"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:83:1: page : Pagestart Pagesheet shapes '</Page>' -> ^( Page shapes ) ;
    public final VisioParser.page_return page() throws RecognitionException {
        VisioParser.page_return retval = new VisioParser.page_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token Pagestart5=null;
        Token Pagesheet6=null;
        Token string_literal8=null;
        VisioParser.shapes_return shapes7 = null;


        Object Pagestart5_tree=null;
        Object Pagesheet6_tree=null;
        Object string_literal8_tree=null;
        RewriteRuleTokenStream stream_Pagesheet=new RewriteRuleTokenStream(adaptor,"token Pagesheet");
        RewriteRuleTokenStream stream_80=new RewriteRuleTokenStream(adaptor,"token 80");
        RewriteRuleTokenStream stream_Pagestart=new RewriteRuleTokenStream(adaptor,"token Pagestart");
        RewriteRuleSubtreeStream stream_shapes=new RewriteRuleSubtreeStream(adaptor,"rule shapes");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:83:5: ( Pagestart Pagesheet shapes '</Page>' -> ^( Page shapes ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:83:7: Pagestart Pagesheet shapes '</Page>'
            {
            Pagestart5=(Token)match(input,Pagestart,FOLLOW_Pagestart_in_page173);  
            stream_Pagestart.add(Pagestart5);

            Pagesheet6=(Token)match(input,Pagesheet,FOLLOW_Pagesheet_in_page175);  
            stream_Pagesheet.add(Pagesheet6);

            pushFollow(FOLLOW_shapes_in_page177);
            shapes7=shapes();

            state._fsp--;

            stream_shapes.add(shapes7.getTree());
            string_literal8=(Token)match(input,80,FOLLOW_80_in_page179);  
            stream_80.add(string_literal8);



            // AST REWRITE
            // elements: shapes
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 83:44: -> ^( Page shapes )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:83:47: ^( Page shapes )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Page, "Page"), root_1);

                adaptor.addChild(root_1, stream_shapes.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "page"

    public static class shapes_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shapes"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:1: shapes : '<Shapes>' (pic+= picture | shape | tex+= textbox )* '</Shapes>' {...}? -> ( textbox )* ( picture )* ( shape )* ;
    public final VisioParser.shapes_return shapes() throws RecognitionException {
        VisioParser.shapes_return retval = new VisioParser.shapes_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal9=null;
        Token string_literal11=null;
        List list_pic=null;
        List list_tex=null;
        VisioParser.shape_return shape10 = null;

        VisioParser.picture_return pic = null;
        VisioParser.textbox_return tex = null;
        Object string_literal9_tree=null;
        Object string_literal11_tree=null;
        RewriteRuleTokenStream stream_82=new RewriteRuleTokenStream(adaptor,"token 82");
        RewriteRuleTokenStream stream_81=new RewriteRuleTokenStream(adaptor,"token 81");
        RewriteRuleSubtreeStream stream_picture=new RewriteRuleSubtreeStream(adaptor,"rule picture");
        RewriteRuleSubtreeStream stream_textbox=new RewriteRuleSubtreeStream(adaptor,"rule textbox");
        RewriteRuleSubtreeStream stream_shape=new RewriteRuleSubtreeStream(adaptor,"rule shape");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:7: ( '<Shapes>' (pic+= picture | shape | tex+= textbox )* '</Shapes>' {...}? -> ( textbox )* ( picture )* ( shape )* )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:9: '<Shapes>' (pic+= picture | shape | tex+= textbox )* '</Shapes>' {...}?
            {
            string_literal9=(Token)match(input,81,FOLLOW_81_in_shapes193);  
            stream_81.add(string_literal9);

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:20: (pic+= picture | shape | tex+= textbox )*
            loop2:
            do {
                int alt2=4;
                alt2 = dfa2.predict(input);
                switch (alt2) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:21: pic+= picture
            	    {
            	    pushFollow(FOLLOW_picture_in_shapes198);
            	    pic=picture();

            	    state._fsp--;

            	    stream_picture.add(pic.getTree());
            	    if (list_pic==null) list_pic=new ArrayList();
            	    list_pic.add(pic.getTree());


            	    }
            	    break;
            	case 2 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:34: shape
            	    {
            	    pushFollow(FOLLOW_shape_in_shapes200);
            	    shape10=shape();

            	    state._fsp--;

            	    stream_shape.add(shape10.getTree());

            	    }
            	    break;
            	case 3 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:40: tex+= textbox
            	    {
            	    pushFollow(FOLLOW_textbox_in_shapes204);
            	    tex=textbox();

            	    state._fsp--;

            	    stream_textbox.add(tex.getTree());
            	    if (list_tex==null) list_tex=new ArrayList();
            	    list_tex.add(tex.getTree());


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            string_literal11=(Token)match(input,82,FOLLOW_82_in_shapes208);  
            stream_82.add(string_literal11);

            if ( !(((list_tex.size()==1)&&(list_pic.size()==1))) ) {
                throw new FailedPredicateException(input, "shapes", "($tex.size()==1)&&($pic.size()==1)");
            }


            // AST REWRITE
            // elements: textbox, shape, picture
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 84:105: -> ( textbox )* ( picture )* ( shape )*
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:108: ( textbox )*
                while ( stream_textbox.hasNext() ) {
                    adaptor.addChild(root_0, stream_textbox.nextTree());

                }
                stream_textbox.reset();
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:117: ( picture )*
                while ( stream_picture.hasNext() ) {
                    adaptor.addChild(root_0, stream_picture.nextTree());

                }
                stream_picture.reset();
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:84:126: ( shape )*
                while ( stream_shape.hasNext() ) {
                    adaptor.addChild(root_0, stream_shape.nextTree());

                }
                stream_shape.reset();

            }

            retval.tree = root_0;
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
    // $ANTLR end "shapes"

    public static class shape_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shape"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:85:1: shape : x y width height shapetext -> ^( Shape x y width height shapetext ) ;
    public final VisioParser.shape_return shape() throws RecognitionException {
        VisioParser.shape_return retval = new VisioParser.shape_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        VisioParser.x_return x12 = null;

        VisioParser.y_return y13 = null;

        VisioParser.width_return width14 = null;

        VisioParser.height_return height15 = null;

        VisioParser.shapetext_return shapetext16 = null;


        RewriteRuleSubtreeStream stream_height=new RewriteRuleSubtreeStream(adaptor,"rule height");
        RewriteRuleSubtreeStream stream_shapetext=new RewriteRuleSubtreeStream(adaptor,"rule shapetext");
        RewriteRuleSubtreeStream stream_width=new RewriteRuleSubtreeStream(adaptor,"rule width");
        RewriteRuleSubtreeStream stream_y=new RewriteRuleSubtreeStream(adaptor,"rule y");
        RewriteRuleSubtreeStream stream_x=new RewriteRuleSubtreeStream(adaptor,"rule x");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:85:7: ( x y width height shapetext -> ^( Shape x y width height shapetext ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:85:9: x y width height shapetext
            {
            pushFollow(FOLLOW_x_in_shape228);
            x12=x();

            state._fsp--;

            stream_x.add(x12.getTree());
            pushFollow(FOLLOW_y_in_shape230);
            y13=y();

            state._fsp--;

            stream_y.add(y13.getTree());
            pushFollow(FOLLOW_width_in_shape232);
            width14=width();

            state._fsp--;

            stream_width.add(width14.getTree());
            pushFollow(FOLLOW_height_in_shape234);
            height15=height();

            state._fsp--;

            stream_height.add(height15.getTree());
            pushFollow(FOLLOW_shapetext_in_shape236);
            shapetext16=shapetext();

            state._fsp--;

            stream_shapetext.add(shapetext16.getTree());


            // AST REWRITE
            // elements: width, y, x, shapetext, height
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 85:36: -> ^( Shape x y width height shapetext )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:85:39: ^( Shape x y width height shapetext )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Shape, "Shape"), root_1);

                adaptor.addChild(root_1, stream_x.nextTree());
                adaptor.addChild(root_1, stream_y.nextTree());
                adaptor.addChild(root_1, stream_width.nextTree());
                adaptor.addChild(root_1, stream_height.nextTree());
                adaptor.addChild(root_1, stream_shapetext.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "shape"

    public static class picture_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "picture"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:86:1: picture : x y width height -> ^( Picture x y width height ) ;
    public final VisioParser.picture_return picture() throws RecognitionException {
        VisioParser.picture_return retval = new VisioParser.picture_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        VisioParser.x_return x17 = null;

        VisioParser.y_return y18 = null;

        VisioParser.width_return width19 = null;

        VisioParser.height_return height20 = null;


        RewriteRuleSubtreeStream stream_height=new RewriteRuleSubtreeStream(adaptor,"rule height");
        RewriteRuleSubtreeStream stream_width=new RewriteRuleSubtreeStream(adaptor,"rule width");
        RewriteRuleSubtreeStream stream_y=new RewriteRuleSubtreeStream(adaptor,"rule y");
        RewriteRuleSubtreeStream stream_x=new RewriteRuleSubtreeStream(adaptor,"rule x");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:86:9: ( x y width height -> ^( Picture x y width height ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:86:11: x y width height
            {
            pushFollow(FOLLOW_x_in_picture259);
            x17=x();

            state._fsp--;

            stream_x.add(x17.getTree());
            pushFollow(FOLLOW_y_in_picture261);
            y18=y();

            state._fsp--;

            stream_y.add(y18.getTree());
            pushFollow(FOLLOW_width_in_picture263);
            width19=width();

            state._fsp--;

            stream_width.add(width19.getTree());
            pushFollow(FOLLOW_height_in_picture265);
            height20=height();

            state._fsp--;

            stream_height.add(height20.getTree());


            // AST REWRITE
            // elements: width, x, y, height
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 86:28: -> ^( Picture x y width height )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:86:31: ^( Picture x y width height )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Picture, "Picture"), root_1);

                adaptor.addChild(root_1, stream_x.nextTree());
                adaptor.addChild(root_1, stream_y.nextTree());
                adaptor.addChild(root_1, stream_width.nextTree());
                adaptor.addChild(root_1, stream_height.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "picture"

    public static class textbox_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "textbox"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:87:1: textbox : x y width height textboxtext -> ^( Box x y width height textboxtext ) ;
    public final VisioParser.textbox_return textbox() throws RecognitionException {
        VisioParser.textbox_return retval = new VisioParser.textbox_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        VisioParser.x_return x21 = null;

        VisioParser.y_return y22 = null;

        VisioParser.width_return width23 = null;

        VisioParser.height_return height24 = null;

        VisioParser.textboxtext_return textboxtext25 = null;


        RewriteRuleSubtreeStream stream_height=new RewriteRuleSubtreeStream(adaptor,"rule height");
        RewriteRuleSubtreeStream stream_width=new RewriteRuleSubtreeStream(adaptor,"rule width");
        RewriteRuleSubtreeStream stream_textboxtext=new RewriteRuleSubtreeStream(adaptor,"rule textboxtext");
        RewriteRuleSubtreeStream stream_y=new RewriteRuleSubtreeStream(adaptor,"rule y");
        RewriteRuleSubtreeStream stream_x=new RewriteRuleSubtreeStream(adaptor,"rule x");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:87:9: ( x y width height textboxtext -> ^( Box x y width height textboxtext ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:87:11: x y width height textboxtext
            {
            pushFollow(FOLLOW_x_in_textbox286);
            x21=x();

            state._fsp--;

            stream_x.add(x21.getTree());
            pushFollow(FOLLOW_y_in_textbox288);
            y22=y();

            state._fsp--;

            stream_y.add(y22.getTree());
            pushFollow(FOLLOW_width_in_textbox290);
            width23=width();

            state._fsp--;

            stream_width.add(width23.getTree());
            pushFollow(FOLLOW_height_in_textbox292);
            height24=height();

            state._fsp--;

            stream_height.add(height24.getTree());
            pushFollow(FOLLOW_textboxtext_in_textbox294);
            textboxtext25=textboxtext();

            state._fsp--;

            stream_textboxtext.add(textboxtext25.getTree());


            // AST REWRITE
            // elements: width, x, textboxtext, height, y
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 87:40: -> ^( Box x y width height textboxtext )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:87:43: ^( Box x y width height textboxtext )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Box, "Box"), root_1);

                adaptor.addChild(root_1, stream_x.nextTree());
                adaptor.addChild(root_1, stream_y.nextTree());
                adaptor.addChild(root_1, stream_width.nextTree());
                adaptor.addChild(root_1, stream_height.nextTree());
                adaptor.addChild(root_1, stream_textboxtext.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "textbox"

    public static class x_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "x"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:88:1: x : Shapestart mydouble '</PinX>' -> ^( Xcoord mydouble ) ;
    public final VisioParser.x_return x() throws RecognitionException {
        VisioParser.x_return retval = new VisioParser.x_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token Shapestart26=null;
        Token string_literal28=null;
        VisioParser.mydouble_return mydouble27 = null;


        Object Shapestart26_tree=null;
        Object string_literal28_tree=null;
        RewriteRuleTokenStream stream_83=new RewriteRuleTokenStream(adaptor,"token 83");
        RewriteRuleTokenStream stream_Shapestart=new RewriteRuleTokenStream(adaptor,"token Shapestart");
        RewriteRuleSubtreeStream stream_mydouble=new RewriteRuleSubtreeStream(adaptor,"rule mydouble");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:88:3: ( Shapestart mydouble '</PinX>' -> ^( Xcoord mydouble ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:88:5: Shapestart mydouble '</PinX>'
            {
            Shapestart26=(Token)match(input,Shapestart,FOLLOW_Shapestart_in_x317);  
            stream_Shapestart.add(Shapestart26);

            pushFollow(FOLLOW_mydouble_in_x319);
            mydouble27=mydouble();

            state._fsp--;

            stream_mydouble.add(mydouble27.getTree());
            string_literal28=(Token)match(input,83,FOLLOW_83_in_x321);  
            stream_83.add(string_literal28);



            // AST REWRITE
            // elements: mydouble
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 88:35: -> ^( Xcoord mydouble )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:88:38: ^( Xcoord mydouble )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Xcoord, "Xcoord"), root_1);

                adaptor.addChild(root_1, stream_mydouble.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "x"

    public static class y_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "y"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:89:1: y : '<PinY>' mydouble -> ^( Ycoord mydouble ) ;
    public final VisioParser.y_return y() throws RecognitionException {
        VisioParser.y_return retval = new VisioParser.y_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal29=null;
        VisioParser.mydouble_return mydouble30 = null;


        Object string_literal29_tree=null;
        RewriteRuleTokenStream stream_84=new RewriteRuleTokenStream(adaptor,"token 84");
        RewriteRuleSubtreeStream stream_mydouble=new RewriteRuleSubtreeStream(adaptor,"rule mydouble");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:89:3: ( '<PinY>' mydouble -> ^( Ycoord mydouble ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:89:5: '<PinY>' mydouble
            {
            string_literal29=(Token)match(input,84,FOLLOW_84_in_y336);  
            stream_84.add(string_literal29);

            pushFollow(FOLLOW_mydouble_in_y338);
            mydouble30=mydouble();

            state._fsp--;

            stream_mydouble.add(mydouble30.getTree());


            // AST REWRITE
            // elements: mydouble
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 89:23: -> ^( Ycoord mydouble )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:89:26: ^( Ycoord mydouble )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Ycoord, "Ycoord"), root_1);

                adaptor.addChild(root_1, stream_mydouble.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "y"

    public static class width_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "width"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:90:1: width : YtoWith c= mydouble '</Width>' -> ^( Width mydouble ) ;
    public final VisioParser.width_return width() throws RecognitionException {
        VisioParser.width_return retval = new VisioParser.width_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token YtoWith31=null;
        Token string_literal32=null;
        VisioParser.mydouble_return c = null;


        Object YtoWith31_tree=null;
        Object string_literal32_tree=null;
        RewriteRuleTokenStream stream_YtoWith=new RewriteRuleTokenStream(adaptor,"token YtoWith");
        RewriteRuleTokenStream stream_85=new RewriteRuleTokenStream(adaptor,"token 85");
        RewriteRuleSubtreeStream stream_mydouble=new RewriteRuleSubtreeStream(adaptor,"rule mydouble");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:90:6: ( YtoWith c= mydouble '</Width>' -> ^( Width mydouble ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:90:8: YtoWith c= mydouble '</Width>'
            {
            YtoWith31=(Token)match(input,YtoWith,FOLLOW_YtoWith_in_width352);  
            stream_YtoWith.add(YtoWith31);

            pushFollow(FOLLOW_mydouble_in_width356);
            c=mydouble();

            state._fsp--;

            stream_mydouble.add(c.getTree());
            string_literal32=(Token)match(input,85,FOLLOW_85_in_width358);  
            stream_85.add(string_literal32);



            // AST REWRITE
            // elements: mydouble
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 90:38: -> ^( Width mydouble )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:90:41: ^( Width mydouble )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Width, "Width"), root_1);

                adaptor.addChild(root_1, stream_mydouble.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "width"

    public static class height_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "height"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:91:1: height : '<Height>' d= mydouble HeighttoText -> ^( Height mydouble ) ;
    public final VisioParser.height_return height() throws RecognitionException {
        VisioParser.height_return retval = new VisioParser.height_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal33=null;
        Token HeighttoText34=null;
        VisioParser.mydouble_return d = null;


        Object string_literal33_tree=null;
        Object HeighttoText34_tree=null;
        RewriteRuleTokenStream stream_HeighttoText=new RewriteRuleTokenStream(adaptor,"token HeighttoText");
        RewriteRuleTokenStream stream_86=new RewriteRuleTokenStream(adaptor,"token 86");
        RewriteRuleSubtreeStream stream_mydouble=new RewriteRuleSubtreeStream(adaptor,"rule mydouble");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:91:7: ( '<Height>' d= mydouble HeighttoText -> ^( Height mydouble ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:91:9: '<Height>' d= mydouble HeighttoText
            {
            string_literal33=(Token)match(input,86,FOLLOW_86_in_height372);  
            stream_86.add(string_literal33);

            pushFollow(FOLLOW_mydouble_in_height376);
            d=mydouble();

            state._fsp--;

            stream_mydouble.add(d.getTree());
            HeighttoText34=(Token)match(input,HeighttoText,FOLLOW_HeighttoText_in_height378);  
            stream_HeighttoText.add(HeighttoText34);



            // AST REWRITE
            // elements: mydouble
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 91:44: -> ^( Height mydouble )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:91:47: ^( Height mydouble )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Height, "Height"), root_1);

                adaptor.addChild(root_1, stream_mydouble.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "height"

    public static class shapetext_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "shapetext"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:92:1: shapetext : text '</Text></Shape>' -> ^( Shapetext text ) ;
    public final VisioParser.shapetext_return shapetext() throws RecognitionException {
        VisioParser.shapetext_return retval = new VisioParser.shapetext_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal36=null;
        VisioParser.text_return text35 = null;


        Object string_literal36_tree=null;
        RewriteRuleTokenStream stream_87=new RewriteRuleTokenStream(adaptor,"token 87");
        RewriteRuleSubtreeStream stream_text=new RewriteRuleSubtreeStream(adaptor,"rule text");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:92:10: ( text '</Text></Shape>' -> ^( Shapetext text ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:92:12: text '</Text></Shape>'
            {
            pushFollow(FOLLOW_text_in_shapetext392);
            text35=text();

            state._fsp--;

            stream_text.add(text35.getTree());
            string_literal36=(Token)match(input,87,FOLLOW_87_in_shapetext394);  
            stream_87.add(string_literal36);



            // AST REWRITE
            // elements: text
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 92:35: -> ^( Shapetext text )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:92:38: ^( Shapetext text )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Shapetext, "Shapetext"), root_1);

                adaptor.addChild(root_1, stream_text.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "shapetext"

    public static class textboxtext_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "textboxtext"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:93:1: textboxtext : 'Bildname:' file 'Größe:' a= INT 'x' b= INT 'Frage:' questionid 'Folgefragen:' ( popup )* '</Text></Shape>' -> ^( Textboxtext file INT INT questionid ( popup )* ) ;
    public final VisioParser.textboxtext_return textboxtext() throws RecognitionException {
        VisioParser.textboxtext_return retval = new VisioParser.textboxtext_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token a=null;
        Token b=null;
        Token string_literal37=null;
        Token string_literal39=null;
        Token char_literal40=null;
        Token string_literal41=null;
        Token string_literal43=null;
        Token string_literal45=null;
        VisioParser.file_return file38 = null;

        VisioParser.questionid_return questionid42 = null;

        VisioParser.popup_return popup44 = null;


        Object a_tree=null;
        Object b_tree=null;
        Object string_literal37_tree=null;
        Object string_literal39_tree=null;
        Object char_literal40_tree=null;
        Object string_literal41_tree=null;
        Object string_literal43_tree=null;
        Object string_literal45_tree=null;
        RewriteRuleTokenStream stream_INT=new RewriteRuleTokenStream(adaptor,"token INT");
        RewriteRuleTokenStream stream_92=new RewriteRuleTokenStream(adaptor,"token 92");
        RewriteRuleTokenStream stream_91=new RewriteRuleTokenStream(adaptor,"token 91");
        RewriteRuleTokenStream stream_90=new RewriteRuleTokenStream(adaptor,"token 90");
        RewriteRuleTokenStream stream_87=new RewriteRuleTokenStream(adaptor,"token 87");
        RewriteRuleTokenStream stream_88=new RewriteRuleTokenStream(adaptor,"token 88");
        RewriteRuleTokenStream stream_89=new RewriteRuleTokenStream(adaptor,"token 89");
        RewriteRuleSubtreeStream stream_file=new RewriteRuleSubtreeStream(adaptor,"rule file");
        RewriteRuleSubtreeStream stream_popup=new RewriteRuleSubtreeStream(adaptor,"rule popup");
        RewriteRuleSubtreeStream stream_questionid=new RewriteRuleSubtreeStream(adaptor,"rule questionid");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:93:12: ( 'Bildname:' file 'Größe:' a= INT 'x' b= INT 'Frage:' questionid 'Folgefragen:' ( popup )* '</Text></Shape>' -> ^( Textboxtext file INT INT questionid ( popup )* ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:93:14: 'Bildname:' file 'Größe:' a= INT 'x' b= INT 'Frage:' questionid 'Folgefragen:' ( popup )* '</Text></Shape>'
            {
            string_literal37=(Token)match(input,88,FOLLOW_88_in_textboxtext408);  
            stream_88.add(string_literal37);

            pushFollow(FOLLOW_file_in_textboxtext410);
            file38=file();

            state._fsp--;

            stream_file.add(file38.getTree());
            string_literal39=(Token)match(input,89,FOLLOW_89_in_textboxtext412);  
            stream_89.add(string_literal39);

            a=(Token)match(input,INT,FOLLOW_INT_in_textboxtext416);  
            stream_INT.add(a);

            char_literal40=(Token)match(input,90,FOLLOW_90_in_textboxtext418);  
            stream_90.add(char_literal40);

            b=(Token)match(input,INT,FOLLOW_INT_in_textboxtext422);  
            stream_INT.add(b);

            string_literal41=(Token)match(input,91,FOLLOW_91_in_textboxtext424);  
            stream_91.add(string_literal41);

            pushFollow(FOLLOW_questionid_in_textboxtext426);
            questionid42=questionid();

            state._fsp--;

            stream_questionid.add(questionid42.getTree());
            string_literal43=(Token)match(input,92,FOLLOW_92_in_textboxtext428);  
            stream_92.add(string_literal43);

            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:93:91: ( popup )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>=String && LA3_0<=INT)||LA3_0==ID) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:93:91: popup
            	    {
            	    pushFollow(FOLLOW_popup_in_textboxtext430);
            	    popup44=popup();

            	    state._fsp--;

            	    stream_popup.add(popup44.getTree());

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            string_literal45=(Token)match(input,87,FOLLOW_87_in_textboxtext433);  
            stream_87.add(string_literal45);



            // AST REWRITE
            // elements: questionid, INT, popup, INT, file
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 93:116: -> ^( Textboxtext file INT INT questionid ( popup )* )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:93:119: ^( Textboxtext file INT INT questionid ( popup )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Textboxtext, "Textboxtext"), root_1);

                adaptor.addChild(root_1, stream_file.nextTree());
                adaptor.addChild(root_1, stream_INT.nextNode());
                adaptor.addChild(root_1, stream_INT.nextNode());
                adaptor.addChild(root_1, stream_questionid.nextTree());
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:93:157: ( popup )*
                while ( stream_popup.hasNext() ) {
                    adaptor.addChild(root_1, stream_popup.nextTree());

                }
                stream_popup.reset();

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "textboxtext"

    public static class questionid_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "questionid"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:94:1: questionid : name -> ^( QID name ) ;
    public final VisioParser.questionid_return questionid() throws RecognitionException {
        VisioParser.questionid_return retval = new VisioParser.questionid_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Visio_BasicParser.name_return name46 = null;


        RewriteRuleSubtreeStream stream_name=new RewriteRuleSubtreeStream(adaptor,"rule name");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:94:12: ( name -> ^( QID name ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:94:14: name
            {
            pushFollow(FOLLOW_name_in_questionid457);
            name46=name();

            state._fsp--;

            stream_name.add(name46.getTree());


            // AST REWRITE
            // elements: name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 94:19: -> ^( QID name )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:94:22: ^( QID name )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(QID, "QID"), root_1);

                adaptor.addChild(root_1, stream_name.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "questionid"

    public static class popup_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "popup"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:95:1: popup : ( text ':' text ';' ) -> ^( Popup text text ) ;
    public final VisioParser.popup_return popup() throws RecognitionException {
        VisioParser.popup_return retval = new VisioParser.popup_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token char_literal48=null;
        Token char_literal50=null;
        VisioParser.text_return text47 = null;

        VisioParser.text_return text49 = null;


        Object char_literal48_tree=null;
        Object char_literal50_tree=null;
        RewriteRuleTokenStream stream_DD=new RewriteRuleTokenStream(adaptor,"token DD");
        RewriteRuleTokenStream stream_SEMI=new RewriteRuleTokenStream(adaptor,"token SEMI");
        RewriteRuleSubtreeStream stream_text=new RewriteRuleSubtreeStream(adaptor,"rule text");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:95:7: ( ( text ':' text ';' ) -> ^( Popup text text ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:95:9: ( text ':' text ';' )
            {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:95:9: ( text ':' text ';' )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:95:10: text ':' text ';'
            {
            pushFollow(FOLLOW_text_in_popup473);
            text47=text();

            state._fsp--;

            stream_text.add(text47.getTree());
            char_literal48=(Token)match(input,DD,FOLLOW_DD_in_popup475);  
            stream_DD.add(char_literal48);

            pushFollow(FOLLOW_text_in_popup477);
            text49=text();

            state._fsp--;

            stream_text.add(text49.getTree());
            char_literal50=(Token)match(input,SEMI,FOLLOW_SEMI_in_popup479);  
            stream_SEMI.add(char_literal50);


            }



            // AST REWRITE
            // elements: text, text
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 95:29: -> ^( Popup text text )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:95:32: ^( Popup text text )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Popup, "Popup"), root_1);

                adaptor.addChild(root_1, stream_text.nextTree());
                adaptor.addChild(root_1, stream_text.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "popup"

    public static class text_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "text"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:96:1: text : name -> ^( Text name ) ;
    public final VisioParser.text_return text() throws RecognitionException {
        VisioParser.text_return retval = new VisioParser.text_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Visio_BasicParser.name_return name51 = null;


        RewriteRuleSubtreeStream stream_name=new RewriteRuleSubtreeStream(adaptor,"rule name");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:96:6: ( name -> ^( Text name ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:96:8: name
            {
            pushFollow(FOLLOW_name_in_text497);
            name51=name();

            state._fsp--;

            stream_name.add(name51.getTree());


            // AST REWRITE
            // elements: name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 96:13: -> ^( Text name )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:96:16: ^( Text name )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Text, "Text"), root_1);

                adaptor.addChild(root_1, stream_name.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "text"

    public static class file_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "file"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:97:1: file : name DOT name -> ^( Text name DOT name ) ;
    public final VisioParser.file_return file() throws RecognitionException {
        VisioParser.file_return retval = new VisioParser.file_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token DOT53=null;
        Visio_BasicParser.name_return name52 = null;

        Visio_BasicParser.name_return name54 = null;


        Object DOT53_tree=null;
        RewriteRuleTokenStream stream_DOT=new RewriteRuleTokenStream(adaptor,"token DOT");
        RewriteRuleSubtreeStream stream_name=new RewriteRuleSubtreeStream(adaptor,"rule name");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:97:6: ( name DOT name -> ^( Text name DOT name ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:97:8: name DOT name
            {
            pushFollow(FOLLOW_name_in_file512);
            name52=name();

            state._fsp--;

            stream_name.add(name52.getTree());
            DOT53=(Token)match(input,DOT,FOLLOW_DOT_in_file514);  
            stream_DOT.add(DOT53);

            pushFollow(FOLLOW_name_in_file516);
            name54=name();

            state._fsp--;

            stream_name.add(name54.getTree());


            // AST REWRITE
            // elements: name, DOT, name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 97:22: -> ^( Text name DOT name )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:97:25: ^( Text name DOT name )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Text, "Text"), root_1);

                adaptor.addChild(root_1, stream_name.nextTree());
                adaptor.addChild(root_1, stream_DOT.nextNode());
                adaptor.addChild(root_1, stream_name.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "file"

    public static class mydouble_return extends ParserRuleReturnScope {
        Object tree;
        public Object getTree() { return tree; }
    };

    // $ANTLR start "mydouble"
    // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:98:1: mydouble : d3double -> ^( MyDouble d3double ) ;
    public final VisioParser.mydouble_return mydouble() throws RecognitionException {
        VisioParser.mydouble_return retval = new VisioParser.mydouble_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Visio_BasicParser.d3double_return d3double55 = null;


        RewriteRuleSubtreeStream stream_d3double=new RewriteRuleSubtreeStream(adaptor,"rule d3double");
        try {
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:98:10: ( d3double -> ^( MyDouble d3double ) )
            // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:98:12: d3double
            {
            pushFollow(FOLLOW_d3double_in_mydouble535);
            d3double55=d3double();

            state._fsp--;

            stream_d3double.add(d3double55.getTree());


            // AST REWRITE
            // elements: d3double
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 98:21: -> ^( MyDouble d3double )
            {
                // D:\\eclipse Workspace\\d3web-KnOfficeParser\\Grammars\\Visio.g:98:24: ^( MyDouble d3double )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(MyDouble, "MyDouble"), root_1);

                adaptor.addChild(root_1, stream_d3double.nextTree());

                adaptor.addChild(root_0, root_1);
                }

            }

            retval.tree = root_0;
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
    // $ANTLR end "mydouble"

    // Delegated rules
    public Visio_BasicParser.name_return name() throws RecognitionException { return gBasicParser.name(); }
    public Visio_BasicParser.eq_return eq() throws RecognitionException { return gBasicParser.eq(); }
    public Visio_BasicParser.eqncalc_return eqncalc() throws RecognitionException { return gBasicParser.eqncalc(); }
    public Visio_BasicParser.type_return type() throws RecognitionException { return gBasicParser.type(); }
    public Visio_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }


    protected DFA2 dfa2 = new DFA2(this);
    static final String DFA2_eotS =
        "\34\uffff";
    static final String DFA2_eofS =
        "\34\uffff";
    static final String DFA2_minS =
        "\1\111\1\uffff\2\5\1\6\1\5\1\124\1\123\2\5\1\6\2\5\1\112\1\5\1"+
        "\6\1\5\1\126\1\125\2\5\1\6\1\5\1\4\1\113\3\uffff";
    static final String DFA2_maxS =
        "\1\122\1\uffff\1\33\1\5\1\123\1\5\1\124\1\123\1\33\1\5\1\112\1"+
        "\5\1\33\1\112\1\5\1\125\1\5\1\126\1\125\1\33\1\5\1\113\1\5\1\130"+
        "\1\113\3\uffff";
    static final String DFA2_acceptS =
        "\1\uffff\1\4\27\uffff\1\3\1\1\1\2";
    static final String DFA2_specialS =
        "\34\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\2\10\uffff\1\1",
            "",
            "\1\4\25\uffff\1\3",
            "\1\4",
            "\1\5\1\uffff\1\5\112\uffff\1\6",
            "\1\7",
            "\1\10",
            "\1\6",
            "\1\12\25\uffff\1\11",
            "\1\12",
            "\1\13\1\uffff\1\13\101\uffff\1\14",
            "\1\15",
            "\1\17\25\uffff\1\16",
            "\1\14",
            "\1\17",
            "\1\20\1\uffff\1\20\114\uffff\1\21",
            "\1\22",
            "\1\23",
            "\1\21",
            "\1\25\25\uffff\1\24",
            "\1\25",
            "\1\26\1\uffff\1\26\102\uffff\1\27",
            "\1\30",
            "\2\33\56\uffff\1\33\24\uffff\1\32\10\uffff\1\32\5\uffff\1"+
            "\31",
            "\1\27",
            "",
            "",
            ""
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "()* loopback of 84:20: (pic+= picture | shape | tex+= textbox )*";
        }
    }
 

    public static final BitSet FOLLOW_Start_in_knowledge150 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008080L});
    public static final BitSet FOLLOW_page_in_knowledge153 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008080L});
    public static final BitSet FOLLOW_79_in_knowledge156 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_End_in_knowledge158 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Pagestart_in_page173 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_Pagesheet_in_page175 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_shapes_in_page177 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_80_in_page179 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_81_in_shapes193 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040200L});
    public static final BitSet FOLLOW_picture_in_shapes198 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040200L});
    public static final BitSet FOLLOW_shape_in_shapes200 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040200L});
    public static final BitSet FOLLOW_textbox_in_shapes204 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040200L});
    public static final BitSet FOLLOW_82_in_shapes208 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_x_in_shape228 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_y_in_shape230 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_width_in_shape232 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_height_in_shape234 = new BitSet(new long[]{0x0010000000000030L,0x0000000000800000L});
    public static final BitSet FOLLOW_shapetext_in_shape236 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_x_in_picture259 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_y_in_picture261 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_width_in_picture263 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_height_in_picture265 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_x_in_textbox286 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_y_in_textbox288 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_width_in_textbox290 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_height_in_textbox292 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_textboxtext_in_textbox294 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Shapestart_in_x317 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_mydouble_in_x319 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_83_in_x321 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_84_in_y336 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_mydouble_in_y338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_YtoWith_in_width352 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_mydouble_in_width356 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_85_in_width358 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_height372 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_mydouble_in_height376 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_HeighttoText_in_height378 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_text_in_shapetext392 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_shapetext394 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_88_in_textboxtext408 = new BitSet(new long[]{0x0010000000000030L,0x0000000000800000L});
    public static final BitSet FOLLOW_file_in_textboxtext410 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_89_in_textboxtext412 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_textboxtext416 = new BitSet(new long[]{0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_90_in_textboxtext418 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_textboxtext422 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_91_in_textboxtext424 = new BitSet(new long[]{0x0010000000000030L,0x0000000000800000L});
    public static final BitSet FOLLOW_questionid_in_textboxtext426 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
    public static final BitSet FOLLOW_92_in_textboxtext428 = new BitSet(new long[]{0x0010000000000030L,0x0000000000800000L});
    public static final BitSet FOLLOW_popup_in_textboxtext430 = new BitSet(new long[]{0x0010000000000030L,0x0000000000800000L});
    public static final BitSet FOLLOW_87_in_textboxtext433 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_questionid457 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_text_in_popup473 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_popup475 = new BitSet(new long[]{0x0010000000000030L,0x0000000000800000L});
    public static final BitSet FOLLOW_text_in_popup477 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_popup479 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_text497 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_file512 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_DOT_in_file514 = new BitSet(new long[]{0x0010000000000030L,0x0000000000800000L});
    public static final BitSet FOLLOW_name_in_file516 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_d3double_in_mydouble535 = new BitSet(new long[]{0x0000000000000002L});

}