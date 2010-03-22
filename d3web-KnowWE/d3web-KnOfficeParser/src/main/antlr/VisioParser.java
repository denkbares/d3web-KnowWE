// $ANTLR 3.1.1 D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g 2010-03-09 12:01:45

package de.d3web.KnOfficeParser.visio;
import de.d3web.KnOfficeParser.ParserErrorHandler;
import de.d3web.KnOfficeParser.ConditionBuilder;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


import org.antlr.runtime.tree.*;

/** 
 * Grammatik zum Einlesen von Visiofiles und ueberfuehren selbiger 
 * in die Standard ANTLR-Baumrepraesenation
 * @author Markus Friedrich
 *
 */
public class VisioParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "String", "INT", "DOT", "DD", "COMMA", "SEMI", "EX", "AT", "ORS", "NS", "TILDE", "LP", "RP", "CBO", "CBC", "SBO", "SBC", "LE", "L", "GE", "G", "EQ", "PLUS", "MINUS", "PROD", "DIV", "WS", "COMMENT", "NL", "IF", "THEN", "AND", "OR", "NOT", "HIDE", "EXCEPT", "UNKNOWN", "KNOWN", "INSTANT", "MINMAX", "IN", "INTER", "ALL", "ALLOWEDNAMES", "INCLUDE", "DEFAULT", "INIT", "ABSTRACT", "SET", "REF", "FUZZY", "DIVTEXT", "DIVNORM", "ID", "Page", "Shape", "Xcoord", "Ycoord", "Width", "Height", "Text", "MyDouble", "Shapetext", "Picture", "Box", "QID", "Textboxtext", "Popup", "Aidtext", "Knowledge", "Start", "End", "Pagestart", "Pagesheet", "Shapestart", "YtoWith", "HeighttoText", "Misc", "Misc2", "Misc3", "'</Pages>'", "'</Page>'", "'<Shapes>'", "'</Shapes>'", "'</PinX>'", "'<PinY>'", "'</Width>'", "'<Height>'", "'</Text></Shape>'", "'Bildname:'", "'Groesse:'", "'x'", "'Frage:'", "'Folgefragen:'"
    };
    public static final int Aidtext=72;
    public static final int LP=15;
    public static final int FUZZY=54;
    public static final int Textboxtext=70;
    public static final int NOT=37;
    public static final int Page=58;
    public static final int EXCEPT=39;
    public static final int DD=7;
    public static final int EOF=-1;
    public static final int T__93=93;
    public static final int T__94=94;
    public static final int T__91=91;
    public static final int T__92=92;
    public static final int T__90=90;
    public static final int EX=10;
    public static final int INCLUDE=48;
    public static final int NL=32;
    public static final int EQ=25;
    public static final int COMMENT=31;
    public static final int T__97=97;
    public static final int Shapestart=78;
    public static final int T__96=96;
    public static final int T__95=95;
    public static final int GE=23;
    public static final int G=24;
    public static final int SBC=20;
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
    public static final int Picture=67;
    public static final int Xcoord=60;
    public static final int WS=30;
    public static final int OR=36;
    public static final int SBO=19;
    public static final int Misc2=82;
    public static final int Misc3=83;
    public static final int Popup=71;
    public static final int YtoWith=79;
    public static final int INIT=50;
    public static final int End=75;
    public static final int Ycoord=61;
    public static final int Shapetext=66;
    public static final int HIDE=38;
    public static final int RP=16;
    public static final int ORS=12;
    public static final int MyDouble=65;
    public static final int ABSTRACT=51;
    public static final int AND=35;
    public static final int ID=57;
    public static final int Width=62;
    public static final int IF=33;
    public static final int AT=11;
    public static final int THEN=34;
    public static final int IN=44;
    public static final int UNKNOWN=40;
    public static final int COMMA=8;
    public static final int Height=63;
    public static final int ALL=46;
    public static final int PROD=28;
    public static final int Knowledge=73;
    public static final int TILDE=14;
    public static final int PLUS=26;
    public static final int String=4;
    public static final int DOT=6;
    public static final int HeighttoText=80;
    public static final int Start=74;
    public static final int Pagesheet=77;
    public static final int Misc=81;
    public static final int ALLOWEDNAMES=47;
    public static final int INSTANT=42;
    public static final int MINMAX=43;
    public static final int DEFAULT=49;
    public static final int INTER=45;
    public static final int SET=52;
    public static final int MINUS=27;
    public static final int DIVNORM=56;
    public static final int SEMI=9;
    public static final int REF=53;
    public static final int QID=69;
    public static final int Box=68;
    public static final int CBC=18;
    public static final int Shape=59;
    public static final int Text=64;
    public static final int DIVTEXT=55;
    public static final int DIV=29;
    public static final int CBO=17;
    public static final int LE=21;
    public static final int Pagestart=76;

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
    public String getGrammarFileName() { return "D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g"; }


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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:103:1: knowledge : Start ( page )* '</Pages>' End -> ^( Knowledge ( page )* ) ;
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
        RewriteRuleTokenStream stream_End=new RewriteRuleTokenStream(adaptor,"token End");
        RewriteRuleTokenStream stream_Start=new RewriteRuleTokenStream(adaptor,"token Start");
        RewriteRuleTokenStream stream_84=new RewriteRuleTokenStream(adaptor,"token 84");
        RewriteRuleSubtreeStream stream_page=new RewriteRuleSubtreeStream(adaptor,"rule page");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:103:11: ( Start ( page )* '</Pages>' End -> ^( Knowledge ( page )* ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:103:13: Start ( page )* '</Pages>' End
            {
            Start1=(Token)match(input,Start,FOLLOW_Start_in_knowledge153);  
            stream_Start.add(Start1);

            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:103:20: ( page )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==Pagestart) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:103:20: page
            	    {
            	    pushFollow(FOLLOW_page_in_knowledge156);
            	    page2=page();

            	    state._fsp--;

            	    stream_page.add(page2.getTree());

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            string_literal3=(Token)match(input,84,FOLLOW_84_in_knowledge159);  
            stream_84.add(string_literal3);

            End4=(Token)match(input,End,FOLLOW_End_in_knowledge161);  
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
            // 103:41: -> ^( Knowledge ( page )* )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:103:44: ^( Knowledge ( page )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Knowledge, "Knowledge"), root_1);

                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:103:56: ( page )*
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:104:1: page : Pagestart Pagesheet shapes '</Page>' -> ^( Page shapes ) ;
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
        RewriteRuleTokenStream stream_Pagestart=new RewriteRuleTokenStream(adaptor,"token Pagestart");
        RewriteRuleTokenStream stream_85=new RewriteRuleTokenStream(adaptor,"token 85");
        RewriteRuleSubtreeStream stream_shapes=new RewriteRuleSubtreeStream(adaptor,"rule shapes");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:104:5: ( Pagestart Pagesheet shapes '</Page>' -> ^( Page shapes ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:104:7: Pagestart Pagesheet shapes '</Page>'
            {
            Pagestart5=(Token)match(input,Pagestart,FOLLOW_Pagestart_in_page176);  
            stream_Pagestart.add(Pagestart5);

            Pagesheet6=(Token)match(input,Pagesheet,FOLLOW_Pagesheet_in_page178);  
            stream_Pagesheet.add(Pagesheet6);

            pushFollow(FOLLOW_shapes_in_page180);
            shapes7=shapes();

            state._fsp--;

            stream_shapes.add(shapes7.getTree());
            string_literal8=(Token)match(input,85,FOLLOW_85_in_page182);  
            stream_85.add(string_literal8);



            // AST REWRITE
            // elements: shapes
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 104:44: -> ^( Page shapes )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:104:47: ^( Page shapes )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:1: shapes : '<Shapes>' (pic+= picture | shape | tex+= textbox )* '</Shapes>' {...}? -> ( textbox )* ( picture )* ( shape )* ;
    public final VisioParser.shapes_return shapes() throws RecognitionException {
        VisioParser.shapes_return retval = new VisioParser.shapes_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal9=null;
        Token string_literal11=null;
        List list_pic=null;
        List list_tex=null;
        VisioParser.shape_return shape10 = null;

        RuleReturnScope pic = null;
        RuleReturnScope tex = null;
        Object string_literal9_tree=null;
        Object string_literal11_tree=null;
        RewriteRuleTokenStream stream_86=new RewriteRuleTokenStream(adaptor,"token 86");
        RewriteRuleTokenStream stream_87=new RewriteRuleTokenStream(adaptor,"token 87");
        RewriteRuleSubtreeStream stream_picture=new RewriteRuleSubtreeStream(adaptor,"rule picture");
        RewriteRuleSubtreeStream stream_textbox=new RewriteRuleSubtreeStream(adaptor,"rule textbox");
        RewriteRuleSubtreeStream stream_shape=new RewriteRuleSubtreeStream(adaptor,"rule shape");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:7: ( '<Shapes>' (pic+= picture | shape | tex+= textbox )* '</Shapes>' {...}? -> ( textbox )* ( picture )* ( shape )* )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:9: '<Shapes>' (pic+= picture | shape | tex+= textbox )* '</Shapes>' {...}?
            {
            string_literal9=(Token)match(input,86,FOLLOW_86_in_shapes196);  
            stream_86.add(string_literal9);

            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:20: (pic+= picture | shape | tex+= textbox )*
            loop2:
            do {
                int alt2=4;
                alt2 = dfa2.predict(input);
                switch (alt2) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:21: pic+= picture
            	    {
            	    pushFollow(FOLLOW_picture_in_shapes201);
            	    pic=picture();

            	    state._fsp--;

            	    stream_picture.add(pic.getTree());
            	    if (list_pic==null) list_pic=new ArrayList();
            	    list_pic.add(pic.getTree());


            	    }
            	    break;
            	case 2 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:34: shape
            	    {
            	    pushFollow(FOLLOW_shape_in_shapes203);
            	    shape10=shape();

            	    state._fsp--;

            	    stream_shape.add(shape10.getTree());

            	    }
            	    break;
            	case 3 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:40: tex+= textbox
            	    {
            	    pushFollow(FOLLOW_textbox_in_shapes207);
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

            string_literal11=(Token)match(input,87,FOLLOW_87_in_shapes211);  
            stream_87.add(string_literal11);

            if ( !(((list_tex.size()==1)&&(list_pic.size()==1))) ) {
                throw new FailedPredicateException(input, "shapes", "($tex.size()==1)&&($pic.size()==1)");
            }


            // AST REWRITE
            // elements: shape, picture, textbox
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 105:105: -> ( textbox )* ( picture )* ( shape )*
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:108: ( textbox )*
                while ( stream_textbox.hasNext() ) {
                    adaptor.addChild(root_0, stream_textbox.nextTree());

                }
                stream_textbox.reset();
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:117: ( picture )*
                while ( stream_picture.hasNext() ) {
                    adaptor.addChild(root_0, stream_picture.nextTree());

                }
                stream_picture.reset();
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:105:126: ( shape )*
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:106:1: shape : x y width height shapetext -> ^( Shape x y width height shapetext ) ;
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
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:106:7: ( x y width height shapetext -> ^( Shape x y width height shapetext ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:106:9: x y width height shapetext
            {
            pushFollow(FOLLOW_x_in_shape231);
            x12=x();

            state._fsp--;

            stream_x.add(x12.getTree());
            pushFollow(FOLLOW_y_in_shape233);
            y13=y();

            state._fsp--;

            stream_y.add(y13.getTree());
            pushFollow(FOLLOW_width_in_shape235);
            width14=width();

            state._fsp--;

            stream_width.add(width14.getTree());
            pushFollow(FOLLOW_height_in_shape237);
            height15=height();

            state._fsp--;

            stream_height.add(height15.getTree());
            pushFollow(FOLLOW_shapetext_in_shape239);
            shapetext16=shapetext();

            state._fsp--;

            stream_shapetext.add(shapetext16.getTree());


            // AST REWRITE
            // elements: width, y, height, shapetext, x
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 106:36: -> ^( Shape x y width height shapetext )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:106:39: ^( Shape x y width height shapetext )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:107:1: picture : x y width height -> ^( Picture x y width height ) ;
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
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:107:9: ( x y width height -> ^( Picture x y width height ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:107:11: x y width height
            {
            pushFollow(FOLLOW_x_in_picture262);
            x17=x();

            state._fsp--;

            stream_x.add(x17.getTree());
            pushFollow(FOLLOW_y_in_picture264);
            y18=y();

            state._fsp--;

            stream_y.add(y18.getTree());
            pushFollow(FOLLOW_width_in_picture266);
            width19=width();

            state._fsp--;

            stream_width.add(width19.getTree());
            pushFollow(FOLLOW_height_in_picture268);
            height20=height();

            state._fsp--;

            stream_height.add(height20.getTree());


            // AST REWRITE
            // elements: height, y, width, x
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 107:28: -> ^( Picture x y width height )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:107:31: ^( Picture x y width height )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:108:1: textbox : x y width height textboxtext -> ^( Box x y width height textboxtext ) ;
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
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:108:9: ( x y width height textboxtext -> ^( Box x y width height textboxtext ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:108:11: x y width height textboxtext
            {
            pushFollow(FOLLOW_x_in_textbox289);
            x21=x();

            state._fsp--;

            stream_x.add(x21.getTree());
            pushFollow(FOLLOW_y_in_textbox291);
            y22=y();

            state._fsp--;

            stream_y.add(y22.getTree());
            pushFollow(FOLLOW_width_in_textbox293);
            width23=width();

            state._fsp--;

            stream_width.add(width23.getTree());
            pushFollow(FOLLOW_height_in_textbox295);
            height24=height();

            state._fsp--;

            stream_height.add(height24.getTree());
            pushFollow(FOLLOW_textboxtext_in_textbox297);
            textboxtext25=textboxtext();

            state._fsp--;

            stream_textboxtext.add(textboxtext25.getTree());


            // AST REWRITE
            // elements: height, textboxtext, x, width, y
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 108:40: -> ^( Box x y width height textboxtext )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:108:43: ^( Box x y width height textboxtext )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:109:1: x : Shapestart mydouble '</PinX>' -> ^( Xcoord mydouble ) ;
    public final VisioParser.x_return x() throws RecognitionException {
        VisioParser.x_return retval = new VisioParser.x_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token Shapestart26=null;
        Token string_literal28=null;
        VisioParser.mydouble_return mydouble27 = null;


        Object Shapestart26_tree=null;
        Object string_literal28_tree=null;
        RewriteRuleTokenStream stream_Shapestart=new RewriteRuleTokenStream(adaptor,"token Shapestart");
        RewriteRuleTokenStream stream_88=new RewriteRuleTokenStream(adaptor,"token 88");
        RewriteRuleSubtreeStream stream_mydouble=new RewriteRuleSubtreeStream(adaptor,"rule mydouble");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:109:3: ( Shapestart mydouble '</PinX>' -> ^( Xcoord mydouble ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:109:5: Shapestart mydouble '</PinX>'
            {
            Shapestart26=(Token)match(input,Shapestart,FOLLOW_Shapestart_in_x320);  
            stream_Shapestart.add(Shapestart26);

            pushFollow(FOLLOW_mydouble_in_x322);
            mydouble27=mydouble();

            state._fsp--;

            stream_mydouble.add(mydouble27.getTree());
            string_literal28=(Token)match(input,88,FOLLOW_88_in_x324);  
            stream_88.add(string_literal28);



            // AST REWRITE
            // elements: mydouble
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 109:35: -> ^( Xcoord mydouble )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:109:38: ^( Xcoord mydouble )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:110:1: y : '<PinY>' mydouble -> ^( Ycoord mydouble ) ;
    public final VisioParser.y_return y() throws RecognitionException {
        VisioParser.y_return retval = new VisioParser.y_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal29=null;
        VisioParser.mydouble_return mydouble30 = null;


        Object string_literal29_tree=null;
        RewriteRuleTokenStream stream_89=new RewriteRuleTokenStream(adaptor,"token 89");
        RewriteRuleSubtreeStream stream_mydouble=new RewriteRuleSubtreeStream(adaptor,"rule mydouble");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:110:3: ( '<PinY>' mydouble -> ^( Ycoord mydouble ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:110:5: '<PinY>' mydouble
            {
            string_literal29=(Token)match(input,89,FOLLOW_89_in_y339);  
            stream_89.add(string_literal29);

            pushFollow(FOLLOW_mydouble_in_y341);
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
            // 110:23: -> ^( Ycoord mydouble )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:110:26: ^( Ycoord mydouble )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:111:1: width : YtoWith c= mydouble '</Width>' -> ^( Width mydouble ) ;
    public final VisioParser.width_return width() throws RecognitionException {
        VisioParser.width_return retval = new VisioParser.width_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token YtoWith31=null;
        Token string_literal32=null;
        VisioParser.mydouble_return c = null;


        Object YtoWith31_tree=null;
        Object string_literal32_tree=null;
        RewriteRuleTokenStream stream_90=new RewriteRuleTokenStream(adaptor,"token 90");
        RewriteRuleTokenStream stream_YtoWith=new RewriteRuleTokenStream(adaptor,"token YtoWith");
        RewriteRuleSubtreeStream stream_mydouble=new RewriteRuleSubtreeStream(adaptor,"rule mydouble");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:111:6: ( YtoWith c= mydouble '</Width>' -> ^( Width mydouble ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:111:8: YtoWith c= mydouble '</Width>'
            {
            YtoWith31=(Token)match(input,YtoWith,FOLLOW_YtoWith_in_width355);  
            stream_YtoWith.add(YtoWith31);

            pushFollow(FOLLOW_mydouble_in_width359);
            c=mydouble();

            state._fsp--;

            stream_mydouble.add(c.getTree());
            string_literal32=(Token)match(input,90,FOLLOW_90_in_width361);  
            stream_90.add(string_literal32);



            // AST REWRITE
            // elements: mydouble
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 111:38: -> ^( Width mydouble )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:111:41: ^( Width mydouble )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:112:1: height : '<Height>' d= mydouble HeighttoText -> ^( Height mydouble ) ;
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
        RewriteRuleTokenStream stream_91=new RewriteRuleTokenStream(adaptor,"token 91");
        RewriteRuleSubtreeStream stream_mydouble=new RewriteRuleSubtreeStream(adaptor,"rule mydouble");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:112:7: ( '<Height>' d= mydouble HeighttoText -> ^( Height mydouble ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:112:9: '<Height>' d= mydouble HeighttoText
            {
            string_literal33=(Token)match(input,91,FOLLOW_91_in_height375);  
            stream_91.add(string_literal33);

            pushFollow(FOLLOW_mydouble_in_height379);
            d=mydouble();

            state._fsp--;

            stream_mydouble.add(d.getTree());
            HeighttoText34=(Token)match(input,HeighttoText,FOLLOW_HeighttoText_in_height381);  
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
            // 112:44: -> ^( Height mydouble )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:112:47: ^( Height mydouble )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:113:1: shapetext : text '</Text></Shape>' -> ^( Shapetext text ) ;
    public final VisioParser.shapetext_return shapetext() throws RecognitionException {
        VisioParser.shapetext_return retval = new VisioParser.shapetext_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Token string_literal36=null;
        VisioParser.text_return text35 = null;


        Object string_literal36_tree=null;
        RewriteRuleTokenStream stream_92=new RewriteRuleTokenStream(adaptor,"token 92");
        RewriteRuleSubtreeStream stream_text=new RewriteRuleSubtreeStream(adaptor,"rule text");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:113:10: ( text '</Text></Shape>' -> ^( Shapetext text ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:113:12: text '</Text></Shape>'
            {
            pushFollow(FOLLOW_text_in_shapetext395);
            text35=text();

            state._fsp--;

            stream_text.add(text35.getTree());
            string_literal36=(Token)match(input,92,FOLLOW_92_in_shapetext397);  
            stream_92.add(string_literal36);



            // AST REWRITE
            // elements: text
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 113:35: -> ^( Shapetext text )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:113:38: ^( Shapetext text )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:114:1: textboxtext : 'Bildname:' file 'Groesse:' a= INT 'x' b= INT 'Frage:' questionid 'Folgefragen:' ( popup )* '</Text></Shape>' -> ^( Textboxtext file INT INT questionid ( popup )* ) ;
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
        RewriteRuleTokenStream stream_97=new RewriteRuleTokenStream(adaptor,"token 97");
        RewriteRuleTokenStream stream_96=new RewriteRuleTokenStream(adaptor,"token 96");
        RewriteRuleTokenStream stream_95=new RewriteRuleTokenStream(adaptor,"token 95");
        RewriteRuleTokenStream stream_INT=new RewriteRuleTokenStream(adaptor,"token INT");
        RewriteRuleTokenStream stream_94=new RewriteRuleTokenStream(adaptor,"token 94");
        RewriteRuleTokenStream stream_93=new RewriteRuleTokenStream(adaptor,"token 93");
        RewriteRuleTokenStream stream_92=new RewriteRuleTokenStream(adaptor,"token 92");
        RewriteRuleSubtreeStream stream_file=new RewriteRuleSubtreeStream(adaptor,"rule file");
        RewriteRuleSubtreeStream stream_popup=new RewriteRuleSubtreeStream(adaptor,"rule popup");
        RewriteRuleSubtreeStream stream_questionid=new RewriteRuleSubtreeStream(adaptor,"rule questionid");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:114:12: ( 'Bildname:' file 'Groesse:' a= INT 'x' b= INT 'Frage:' questionid 'Folgefragen:' ( popup )* '</Text></Shape>' -> ^( Textboxtext file INT INT questionid ( popup )* ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:114:14: 'Bildname:' file 'Groesse:' a= INT 'x' b= INT 'Frage:' questionid 'Folgefragen:' ( popup )* '</Text></Shape>'
            {
            string_literal37=(Token)match(input,93,FOLLOW_93_in_textboxtext411);  
            stream_93.add(string_literal37);

            pushFollow(FOLLOW_file_in_textboxtext413);
            file38=file();

            state._fsp--;

            stream_file.add(file38.getTree());
            string_literal39=(Token)match(input,94,FOLLOW_94_in_textboxtext415);  
            stream_94.add(string_literal39);

            a=(Token)match(input,INT,FOLLOW_INT_in_textboxtext419);  
            stream_INT.add(a);

            char_literal40=(Token)match(input,95,FOLLOW_95_in_textboxtext421);  
            stream_95.add(char_literal40);

            b=(Token)match(input,INT,FOLLOW_INT_in_textboxtext425);  
            stream_INT.add(b);

            string_literal41=(Token)match(input,96,FOLLOW_96_in_textboxtext427);  
            stream_96.add(string_literal41);

            pushFollow(FOLLOW_questionid_in_textboxtext429);
            questionid42=questionid();

            state._fsp--;

            stream_questionid.add(questionid42.getTree());
            string_literal43=(Token)match(input,97,FOLLOW_97_in_textboxtext431);  
            stream_97.add(string_literal43);

            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:114:93: ( popup )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>=String && LA3_0<=INT)||LA3_0==ID) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:114:93: popup
            	    {
            	    pushFollow(FOLLOW_popup_in_textboxtext433);
            	    popup44=popup();

            	    state._fsp--;

            	    stream_popup.add(popup44.getTree());

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            string_literal45=(Token)match(input,92,FOLLOW_92_in_textboxtext436);  
            stream_92.add(string_literal45);



            // AST REWRITE
            // elements: file, popup, questionid, INT, INT
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 114:118: -> ^( Textboxtext file INT INT questionid ( popup )* )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:114:121: ^( Textboxtext file INT INT questionid ( popup )* )
                {
                Object root_1 = (Object)adaptor.nil();
                root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(Textboxtext, "Textboxtext"), root_1);

                adaptor.addChild(root_1, stream_file.nextTree());
                adaptor.addChild(root_1, stream_INT.nextNode());
                adaptor.addChild(root_1, stream_INT.nextNode());
                adaptor.addChild(root_1, stream_questionid.nextTree());
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:114:159: ( popup )*
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:115:1: questionid : name -> ^( QID name ) ;
    public final VisioParser.questionid_return questionid() throws RecognitionException {
        VisioParser.questionid_return retval = new VisioParser.questionid_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Visio_BasicParser.name_return name46 = null;


        RewriteRuleSubtreeStream stream_name=new RewriteRuleSubtreeStream(adaptor,"rule name");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:115:12: ( name -> ^( QID name ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:115:14: name
            {
            pushFollow(FOLLOW_name_in_questionid460);
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
            // 115:19: -> ^( QID name )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:115:22: ^( QID name )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:116:1: popup : ( text ':' text ';' ) -> ^( Popup text text ) ;
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
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:116:7: ( ( text ':' text ';' ) -> ^( Popup text text ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:116:9: ( text ':' text ';' )
            {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:116:9: ( text ':' text ';' )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:116:10: text ':' text ';'
            {
            pushFollow(FOLLOW_text_in_popup476);
            text47=text();

            state._fsp--;

            stream_text.add(text47.getTree());
            char_literal48=(Token)match(input,DD,FOLLOW_DD_in_popup478);  
            stream_DD.add(char_literal48);

            pushFollow(FOLLOW_text_in_popup480);
            text49=text();

            state._fsp--;

            stream_text.add(text49.getTree());
            char_literal50=(Token)match(input,SEMI,FOLLOW_SEMI_in_popup482);  
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
            // 116:29: -> ^( Popup text text )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:116:32: ^( Popup text text )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:117:1: text : name -> ^( Text name ) ;
    public final VisioParser.text_return text() throws RecognitionException {
        VisioParser.text_return retval = new VisioParser.text_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Visio_BasicParser.name_return name51 = null;


        RewriteRuleSubtreeStream stream_name=new RewriteRuleSubtreeStream(adaptor,"rule name");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:117:6: ( name -> ^( Text name ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:117:8: name
            {
            pushFollow(FOLLOW_name_in_text500);
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
            // 117:13: -> ^( Text name )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:117:16: ^( Text name )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:118:1: file : name DOT name -> ^( Text name DOT name ) ;
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
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:118:6: ( name DOT name -> ^( Text name DOT name ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:118:8: name DOT name
            {
            pushFollow(FOLLOW_name_in_file515);
            name52=name();

            state._fsp--;

            stream_name.add(name52.getTree());
            DOT53=(Token)match(input,DOT,FOLLOW_DOT_in_file517);  
            stream_DOT.add(DOT53);

            pushFollow(FOLLOW_name_in_file519);
            name54=name();

            state._fsp--;

            stream_name.add(name54.getTree());


            // AST REWRITE
            // elements: DOT, name, name
            // token labels: 
            // rule labels: retval
            // token list labels: 
            // rule list labels: 
            retval.tree = root_0;
            RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"token retval",retval!=null?retval.tree:null);

            root_0 = (Object)adaptor.nil();
            // 118:22: -> ^( Text name DOT name )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:118:25: ^( Text name DOT name )
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
    // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:119:1: mydouble : d3double -> ^( MyDouble d3double ) ;
    public final VisioParser.mydouble_return mydouble() throws RecognitionException {
        VisioParser.mydouble_return retval = new VisioParser.mydouble_return();
        retval.start = input.LT(1);

        Object root_0 = null;

        Visio_BasicParser.d3double_return d3double55 = null;


        RewriteRuleSubtreeStream stream_d3double=new RewriteRuleSubtreeStream(adaptor,"rule d3double");
        try {
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:119:10: ( d3double -> ^( MyDouble d3double ) )
            // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:119:12: d3double
            {
            pushFollow(FOLLOW_d3double_in_mydouble538);
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
            // 119:21: -> ^( MyDouble d3double )
            {
                // D:\\eclipse workspaces\\Uni SVN 64bit\\d3web-KnowWE\\d3web-KnOfficeParser\\src\\main\\antlr\\Visio.g:119:24: ^( MyDouble d3double )
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
    public Visio_BasicParser.eq_return eq() throws RecognitionException { return gBasicParser.eq(); }
    public Visio_BasicParser.d3double_return d3double() throws RecognitionException { return gBasicParser.d3double(); }
    public Visio_BasicParser.eqncalc_return eqncalc() throws RecognitionException { return gBasicParser.eqncalc(); }
    public Visio_BasicParser.nameOrDouble_return nameOrDouble() throws RecognitionException { return gBasicParser.nameOrDouble(); }
    public Visio_BasicParser.type_return type() throws RecognitionException { return gBasicParser.type(); }
    public Visio_BasicParser.name_return name() throws RecognitionException { return gBasicParser.name(); }


    protected DFA2 dfa2 = new DFA2(this);
    static final String DFA2_eotS =
        "\34\uffff";
    static final String DFA2_eofS =
        "\34\uffff";
    static final String DFA2_minS =
        "\1\116\1\uffff\2\5\1\6\1\5\1\131\1\130\2\5\1\6\2\5\1\117\1\5\1"+
        "\6\1\5\1\133\1\132\2\5\1\6\1\5\1\4\1\120\3\uffff";
    static final String DFA2_maxS =
        "\1\127\1\uffff\1\33\1\5\1\130\1\5\1\131\1\130\1\33\1\5\1\117\1"+
        "\5\1\33\1\117\1\5\1\132\1\5\1\133\1\132\1\33\1\5\1\120\1\5\1\135"+
        "\1\120\3\uffff";
    static final String DFA2_acceptS =
        "\1\uffff\1\4\27\uffff\1\3\1\1\1\2";
    static final String DFA2_specialS =
        "\34\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\2\10\uffff\1\1",
            "",
            "\1\4\25\uffff\1\3",
            "\1\4",
            "\1\5\1\uffff\1\5\117\uffff\1\6",
            "\1\7",
            "\1\10",
            "\1\6",
            "\1\12\25\uffff\1\11",
            "\1\12",
            "\1\13\1\uffff\1\13\106\uffff\1\14",
            "\1\15",
            "\1\17\25\uffff\1\16",
            "\1\14",
            "\1\17",
            "\1\20\1\uffff\1\20\121\uffff\1\21",
            "\1\22",
            "\1\23",
            "\1\21",
            "\1\25\25\uffff\1\24",
            "\1\25",
            "\1\26\1\uffff\1\26\107\uffff\1\27",
            "\1\30",
            "\2\33\63\uffff\1\33\24\uffff\1\32\10\uffff\1\32\5\uffff\1"+
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
            return "()* loopback of 105:20: (pic+= picture | shape | tex+= textbox )*";
        }
    }
 

    public static final BitSet FOLLOW_Start_in_knowledge153 = new BitSet(new long[]{0x0000000000000000L,0x0000000000101000L});
    public static final BitSet FOLLOW_page_in_knowledge156 = new BitSet(new long[]{0x0000000000000000L,0x0000000000101000L});
    public static final BitSet FOLLOW_84_in_knowledge159 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_End_in_knowledge161 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Pagestart_in_page176 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_Pagesheet_in_page178 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_shapes_in_page180 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_85_in_page182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_86_in_shapes196 = new BitSet(new long[]{0x0000000000000000L,0x0000000000804000L});
    public static final BitSet FOLLOW_picture_in_shapes201 = new BitSet(new long[]{0x0000000000000000L,0x0000000000804000L});
    public static final BitSet FOLLOW_shape_in_shapes203 = new BitSet(new long[]{0x0000000000000000L,0x0000000000804000L});
    public static final BitSet FOLLOW_textbox_in_shapes207 = new BitSet(new long[]{0x0000000000000000L,0x0000000000804000L});
    public static final BitSet FOLLOW_87_in_shapes211 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_x_in_shape231 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_y_in_shape233 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_width_in_shape235 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_height_in_shape237 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_shapetext_in_shape239 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_x_in_picture262 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_y_in_picture264 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_width_in_picture266 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_height_in_picture268 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_x_in_textbox289 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_y_in_textbox291 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_width_in_textbox293 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_height_in_textbox295 = new BitSet(new long[]{0x0000000000000000L,0x0000000020000000L});
    public static final BitSet FOLLOW_textboxtext_in_textbox297 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Shapestart_in_x320 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_mydouble_in_x322 = new BitSet(new long[]{0x0000000000000000L,0x0000000001000000L});
    public static final BitSet FOLLOW_88_in_x324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_89_in_y339 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_mydouble_in_y341 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_YtoWith_in_width355 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_mydouble_in_width359 = new BitSet(new long[]{0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_90_in_width361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_91_in_height375 = new BitSet(new long[]{0x0000000008000020L});
    public static final BitSet FOLLOW_mydouble_in_height379 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_HeighttoText_in_height381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_text_in_shapetext395 = new BitSet(new long[]{0x0000000000000000L,0x0000000010000000L});
    public static final BitSet FOLLOW_92_in_shapetext397 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_93_in_textboxtext411 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_file_in_textboxtext413 = new BitSet(new long[]{0x0000000000000000L,0x0000000040000000L});
    public static final BitSet FOLLOW_94_in_textboxtext415 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_textboxtext419 = new BitSet(new long[]{0x0000000000000000L,0x0000000080000000L});
    public static final BitSet FOLLOW_95_in_textboxtext421 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_INT_in_textboxtext425 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
    public static final BitSet FOLLOW_96_in_textboxtext427 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_questionid_in_textboxtext429 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_97_in_textboxtext431 = new BitSet(new long[]{0x0200000000000030L,0x0000000010000000L});
    public static final BitSet FOLLOW_popup_in_textboxtext433 = new BitSet(new long[]{0x0200000000000030L,0x0000000010000000L});
    public static final BitSet FOLLOW_92_in_textboxtext436 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_questionid460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_text_in_popup476 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_DD_in_popup478 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_text_in_popup480 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_SEMI_in_popup482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_text500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_file515 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_DOT_in_file517 = new BitSet(new long[]{0x0200000000000030L});
    public static final BitSet FOLLOW_name_in_file519 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_d3double_in_mydouble538 = new BitSet(new long[]{0x0000000000000002L});

}