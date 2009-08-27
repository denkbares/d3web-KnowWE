// $ANTLR 3.1.1 /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g 2008-10-28 15:11:08

package de.d3web.textParser.xclPatternParser;

import de.d3web.report.Message;
import de.d3web.report.Report;
import de.d3web.textParser.decisionTable.MessageGenerator;
import java.util.Collection;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.EarlyExitException;
import org.antlr.runtime.MismatchedSetException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.Parser;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import de.d3web.textParser.Utils.*;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionSolution;
import de.d3web.kernel.domainModel.ruleCondition.*;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.kernel.domainModel.qasets.QuestionNum;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class xclParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "STRING", "WS", "ANY", "NEWLINE", "SL_COMMENT", "OR", "AND", "ALL", "IN", "'['", "','", "']'", "'~'", "'{'", "'}'", "'--'", "'[++]'", "'[!]'", "'='", "'>='", "'>'", "'<'", "'<='", "'establishedThreshold'", "'suggestedThreshold'", "'minSupport'"
    };
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int AND=10;
    public static final int EOF=-1;
    public static final int T__19=19;
    public static final int ANY=6;
    public static final int WS=5;
    public static final int T__16=16;
    public static final int IN=12;
    public static final int T__15=15;
    public static final int NEWLINE=7;
    public static final int T__18=18;
    public static final int T__17=17;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int ALL=11;
    public static final int OR=9;
    public static final int SL_COMMENT=8;
    public static final int STRING=4;

    // delegates
    // delegators


        public xclParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public xclParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return xclParser.tokenNames; }
    public String getGrammarFileName() { return "/HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g"; }


    private XCLModel model;
    private Diagnosis  diagnosis;
    private Question question;
    private ArrayList<AbstractCondition> condlist;
    private Answer answer;
    private KnowledgeBaseManagement	kbm;
    private XCLRelation relation;
    private boolean success;
    private boolean validknowledge;
    private Report report;
    private int linenumber;

    public String getErrorMessage(RecognitionException e, String[] tokenNames){
    String msg=super.getErrorMessage(e,tokenNames);
    report.error(new Message(msg));
    return msg;
    }




    // $ANTLR start "name"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:65:1: name returns [String name] : ( ( (a= STRING ) ( ( WS )? (a= STRING ) )* ) | (a= ANY ) );
    public final String name() throws RecognitionException {
        String name = null;

        Token a=null;

        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:9: ( ( (a= STRING ) ( ( WS )? (a= STRING ) )* ) | (a= ANY ) )
            int alt3=2;
            int LA3_0 = input.LA(1);

            if ( (LA3_0==STRING) ) {
                alt3=1;
            }
            else if ( (LA3_0==ANY) ) {
                alt3=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }
            switch (alt3) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:11: ( (a= STRING ) ( ( WS )? (a= STRING ) )* )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:11: ( (a= STRING ) ( ( WS )? (a= STRING ) )* )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:12: (a= STRING ) ( ( WS )? (a= STRING ) )*
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:12: (a= STRING )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:13: a= STRING
                    {
                    a=(Token)match(input,STRING,FOLLOW_STRING_in_name50); 
                    name=a.getText();

                    }

                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:43: ( ( WS )? (a= STRING ) )*
                    loop2:
                    do {
                        int alt2=2;
                        int LA2_0 = input.LA(1);

                        if ( (LA2_0==WS) ) {
                            int LA2_1 = input.LA(2);

                            if ( (LA2_1==STRING) ) {
                                alt2=1;
                            }


                        }
                        else if ( (LA2_0==STRING) ) {
                            alt2=1;
                        }


                        switch (alt2) {
                    	case 1 :
                    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:44: ( WS )? (a= STRING )
                    	    {
                    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:44: ( WS )?
                    	    int alt1=2;
                    	    int LA1_0 = input.LA(1);

                    	    if ( (LA1_0==WS) ) {
                    	        alt1=1;
                    	    }
                    	    switch (alt1) {
                    	        case 1 :
                    	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:45: WS
                    	            {
                    	            match(input,WS,FOLLOW_WS_in_name57); 
                    	            name+=" ";

                    	            }
                    	            break;

                    	    }

                    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:63: (a= STRING )
                    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:67:64: a= STRING
                    	    {
                    	    a=(Token)match(input,STRING,FOLLOW_STRING_in_name66); 
                    	    name+=a.getText();

                    	    }


                    	    }
                    	    break;

                    	default :
                    	    break loop2;
                        }
                    } while (true);


                    }


                    }
                    break;
                case 2 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:68:10: (a= ANY )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:68:10: (a= ANY )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:68:11: a= ANY
                    {
                    a=(Token)match(input,ANY,FOLLOW_ANY_in_name86); 
                    name=a.getText();if (name.length()>2){
                                        name=name.substring(1,name.length()-1);
                                        }
                                        

                    }


                    }
                    break;

            }
        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
                    
        }
        finally {
        }
        return name;
    }
    // $ANTLR end "name"


    // $ANTLR start "annotation"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:78:1: annotation : '[' ( ( WS )? NEWLINE )? ( ( WS )? ( ethres | sthres | msup ) ( ( WS )? ',' )? ( NEWLINE )? )* ( WS )? ']' ;
    public final void annotation() throws RecognitionException {
        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:9: ( '[' ( ( WS )? NEWLINE )? ( ( WS )? ( ethres | sthres | msup ) ( ( WS )? ',' )? ( NEWLINE )? )* ( WS )? ']' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:10: '[' ( ( WS )? NEWLINE )? ( ( WS )? ( ethres | sthres | msup ) ( ( WS )? ',' )? ( NEWLINE )? )* ( WS )? ']'
            {
            match(input,13,FOLLOW_13_in_annotation117); 
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:14: ( ( WS )? NEWLINE )?
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==WS) ) {
                int LA5_1 = input.LA(2);

                if ( (LA5_1==NEWLINE) ) {
                    alt5=1;
                }
            }
            else if ( (LA5_0==NEWLINE) ) {
                alt5=1;
            }
            switch (alt5) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:15: ( WS )? NEWLINE
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:15: ( WS )?
                    int alt4=2;
                    int LA4_0 = input.LA(1);

                    if ( (LA4_0==WS) ) {
                        alt4=1;
                    }
                    switch (alt4) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:15: WS
                            {
                            match(input,WS,FOLLOW_WS_in_annotation120); 

                            }
                            break;

                    }

                    match(input,NEWLINE,FOLLOW_NEWLINE_in_annotation123); 
                    linenumber++;

                    }
                    break;

            }

            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:45: ( ( WS )? ( ethres | sthres | msup ) ( ( WS )? ',' )? ( NEWLINE )? )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==WS) ) {
                    int LA11_1 = input.LA(2);

                    if ( ((LA11_1>=27 && LA11_1<=29)) ) {
                        alt11=1;
                    }


                }
                else if ( ((LA11_0>=27 && LA11_0<=29)) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:46: ( WS )? ( ethres | sthres | msup ) ( ( WS )? ',' )? ( NEWLINE )?
            	    {
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:46: ( WS )?
            	    int alt6=2;
            	    int LA6_0 = input.LA(1);

            	    if ( (LA6_0==WS) ) {
            	        alt6=1;
            	    }
            	    switch (alt6) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:46: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_annotation130); 

            	            }
            	            break;

            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:50: ( ethres | sthres | msup )
            	    int alt7=3;
            	    switch ( input.LA(1) ) {
            	    case 27:
            	        {
            	        alt7=1;
            	        }
            	        break;
            	    case 28:
            	        {
            	        alt7=2;
            	        }
            	        break;
            	    case 29:
            	        {
            	        alt7=3;
            	        }
            	        break;
            	    default:
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 7, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt7) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:51: ethres
            	            {
            	            pushFollow(FOLLOW_ethres_in_annotation134);
            	            ethres();

            	            state._fsp--;


            	            }
            	            break;
            	        case 2 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:58: sthres
            	            {
            	            pushFollow(FOLLOW_sthres_in_annotation136);
            	            sthres();

            	            state._fsp--;


            	            }
            	            break;
            	        case 3 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:65: msup
            	            {
            	            pushFollow(FOLLOW_msup_in_annotation138);
            	            msup();

            	            state._fsp--;


            	            }
            	            break;

            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:70: ( ( WS )? ',' )?
            	    int alt9=2;
            	    int LA9_0 = input.LA(1);

            	    if ( (LA9_0==WS) ) {
            	        int LA9_1 = input.LA(2);

            	        if ( (LA9_1==14) ) {
            	            alt9=1;
            	        }
            	    }
            	    else if ( (LA9_0==14) ) {
            	        alt9=1;
            	    }
            	    switch (alt9) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:71: ( WS )? ','
            	            {
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:71: ( WS )?
            	            int alt8=2;
            	            int LA8_0 = input.LA(1);

            	            if ( (LA8_0==WS) ) {
            	                alt8=1;
            	            }
            	            switch (alt8) {
            	                case 1 :
            	                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:71: WS
            	                    {
            	                    match(input,WS,FOLLOW_WS_in_annotation141); 

            	                    }
            	                    break;

            	            }

            	            match(input,14,FOLLOW_14_in_annotation144); 

            	            }
            	            break;

            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:81: ( NEWLINE )?
            	    int alt10=2;
            	    int LA10_0 = input.LA(1);

            	    if ( (LA10_0==NEWLINE) ) {
            	        alt10=1;
            	    }
            	    switch (alt10) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:82: NEWLINE
            	            {
            	            match(input,NEWLINE,FOLLOW_NEWLINE_in_annotation149); 
            	            linenumber++;

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);

            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:110: ( WS )?
            int alt12=2;
            int LA12_0 = input.LA(1);

            if ( (LA12_0==WS) ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:79:110: WS
                    {
                    match(input,WS,FOLLOW_WS_in_annotation157); 

                    }
                    break;

            }

            match(input,15,FOLLOW_15_in_annotation160); 

            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
                    
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "annotation"


    // $ANTLR start "xclrule"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:86:1: xclrule[KnowledgeBase kbase] returns [Report repdead] : ( ( NEWLINE )? ( WS )? (erg= name ) ( WS )? ( '~' ( WS )? scmweight= name ( WS )? )? '{' ( ( WS )? NEWLINE )* body ( ( NEWLINE ) | WS )? '}' ( WS )? ( ( NEWLINE )* annotation ( WS )* )? ( NEWLINE ( WS )? )* )+ EOF ;
    public final Report xclrule(KnowledgeBase kbase) throws RecognitionException {
        Report repdead = null;

        String erg = null;

        String scmweight = null;



        		linenumber=0;
        		
        		report = new Report();
        				
        		 success=true;
        		 validknowledge=true;
        		 if (kbase==null){
        						 validknowledge=false;
        						 report.error(new Message("no valid Knowledgebase"));
        	 } else {			
        	 	kbm=KnowledgeBaseManagement.createInstance(kbase);
        		}	
        	 
        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:1: ( ( ( NEWLINE )? ( WS )? (erg= name ) ( WS )? ( '~' ( WS )? scmweight= name ( WS )? )? '{' ( ( WS )? NEWLINE )* body ( ( NEWLINE ) | WS )? '}' ( WS )? ( ( NEWLINE )* annotation ( WS )* )? ( NEWLINE ( WS )? )* )+ EOF )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:8: ( ( NEWLINE )? ( WS )? (erg= name ) ( WS )? ( '~' ( WS )? scmweight= name ( WS )? )? '{' ( ( WS )? NEWLINE )* body ( ( NEWLINE ) | WS )? '}' ( WS )? ( ( NEWLINE )* annotation ( WS )* )? ( NEWLINE ( WS )? )* )+ EOF
            {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:8: ( ( NEWLINE )? ( WS )? (erg= name ) ( WS )? ( '~' ( WS )? scmweight= name ( WS )? )? '{' ( ( WS )? NEWLINE )* body ( ( NEWLINE ) | WS )? '}' ( WS )? ( ( NEWLINE )* annotation ( WS )* )? ( NEWLINE ( WS )? )* )+
            int cnt28=0;
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( ((LA28_0>=STRING && LA28_0<=NEWLINE)) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:9: ( NEWLINE )? ( WS )? (erg= name ) ( WS )? ( '~' ( WS )? scmweight= name ( WS )? )? '{' ( ( WS )? NEWLINE )* body ( ( NEWLINE ) | WS )? '}' ( WS )? ( ( NEWLINE )* annotation ( WS )* )? ( NEWLINE ( WS )? )*
            	    {
            	    success=true;validknowledge=true;
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:44: ( NEWLINE )?
            	    int alt13=2;
            	    int LA13_0 = input.LA(1);

            	    if ( (LA13_0==NEWLINE) ) {
            	        alt13=1;
            	    }
            	    switch (alt13) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:45: NEWLINE
            	            {
            	            match(input,NEWLINE,FOLLOW_NEWLINE_in_xclrule191); 
            	            linenumber++;

            	            }
            	            break;

            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:71: ( WS )?
            	    int alt14=2;
            	    int LA14_0 = input.LA(1);

            	    if ( (LA14_0==WS) ) {
            	        alt14=1;
            	    }
            	    switch (alt14) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:71: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_xclrule197); 

            	            }
            	            break;

            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:74: (erg= name )
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:101:75: erg= name
            	    {
            	    pushFollow(FOLLOW_name_in_xclrule202);
            	    erg=name();

            	    state._fsp--;


            	    					  
            	    					  if (validknowledge && erg != null) diagnosis=kbm.findDiagnosis(erg);
            	    					  if (diagnosis==null){
            	    										  validknowledge=false;
            	    								          ConceptNotInKBError err = new ConceptNotInKBError("Solution not found: "+erg+" in line "+linenumber);
            	    								          err.setObjectName(erg);
            	                							  err.setKey(MessageGenerator.KEY_INVALID_DIAGNOSIS);
            	                							  report.error(err);
            	    				  			}
            	    					  if (validknowledge){
            	    					  	boolean modelExists = false;
            	                						  Collection<KnowledgeSlice> models = kbm.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);
            	                						  for (KnowledgeSlice knowledgeSlice : models) {
            	    										if(knowledgeSlice instanceof XCLModel && ((XCLModel)knowledgeSlice).getSolution().equals(diagnosis)) {
            	    											model = ((XCLModel)knowledgeSlice);
            	    											modelExists = true;
            	    											break;
            	    										}
            	                						  }
            	                				if(!modelExists) {
            	                					model=new XCLModel(diagnosis);
            	                				}
            	    					  } 
            	    					  


            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:4: ( WS )?
            	    int alt15=2;
            	    int LA15_0 = input.LA(1);

            	    if ( (LA15_0==WS) ) {
            	        alt15=1;
            	    }
            	    switch (alt15) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:4: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_xclrule207); 

            	            }
            	            break;

            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:8: ( '~' ( WS )? scmweight= name ( WS )? )?
            	    int alt18=2;
            	    int LA18_0 = input.LA(1);

            	    if ( (LA18_0==16) ) {
            	        alt18=1;
            	    }
            	    switch (alt18) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:9: '~' ( WS )? scmweight= name ( WS )?
            	            {
            	            match(input,16,FOLLOW_16_in_xclrule211); 
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:13: ( WS )?
            	            int alt16=2;
            	            int LA16_0 = input.LA(1);

            	            if ( (LA16_0==WS) ) {
            	                alt16=1;
            	            }
            	            switch (alt16) {
            	                case 1 :
            	                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:13: WS
            	                    {
            	                    match(input,WS,FOLLOW_WS_in_xclrule213); 

            	                    }
            	                    break;

            	            }

            	            pushFollow(FOLLOW_name_in_xclrule218);
            	            scmweight=name();

            	            state._fsp--;

            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:32: ( WS )?
            	            int alt17=2;
            	            int LA17_0 = input.LA(1);

            	            if ( (LA17_0==WS) ) {
            	                alt17=1;
            	            }
            	            switch (alt17) {
            	                case 1 :
            	                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:32: WS
            	                    {
            	                    match(input,WS,FOLLOW_WS_in_xclrule220); 

            	                    }
            	                    break;

            	            }


            	            }
            	            break;

            	    }

            	    match(input,17,FOLLOW_17_in_xclrule225); 
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:43: ( ( WS )? NEWLINE )*
            	    loop20:
            	    do {
            	        int alt20=2;
            	        int LA20_0 = input.LA(1);

            	        if ( (LA20_0==WS) ) {
            	            int LA20_1 = input.LA(2);

            	            if ( (LA20_1==NEWLINE) ) {
            	                alt20=1;
            	            }


            	        }
            	        else if ( (LA20_0==NEWLINE) ) {
            	            alt20=1;
            	        }


            	        switch (alt20) {
            	    	case 1 :
            	    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:45: ( WS )? NEWLINE
            	    	    {
            	    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:45: ( WS )?
            	    	    int alt19=2;
            	    	    int LA19_0 = input.LA(1);

            	    	    if ( (LA19_0==WS) ) {
            	    	        alt19=1;
            	    	    }
            	    	    switch (alt19) {
            	    	        case 1 :
            	    	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:45: WS
            	    	            {
            	    	            match(input,WS,FOLLOW_WS_in_xclrule230); 

            	    	            }
            	    	            break;

            	    	    }

            	    	    match(input,NEWLINE,FOLLOW_NEWLINE_in_xclrule233); 
            	    	    linenumber++;

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop20;
            	        }
            	    } while (true);

            	    pushFollow(FOLLOW_body_in_xclrule239);
            	    body();

            	    state._fsp--;

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:80: ( ( NEWLINE ) | WS )?
            	    int alt21=3;
            	    int LA21_0 = input.LA(1);

            	    if ( (LA21_0==NEWLINE) ) {
            	        alt21=1;
            	    }
            	    else if ( (LA21_0==WS) ) {
            	        alt21=2;
            	    }
            	    switch (alt21) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:81: ( NEWLINE )
            	            {
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:81: ( NEWLINE )
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:82: NEWLINE
            	            {
            	            match(input,NEWLINE,FOLLOW_NEWLINE_in_xclrule243); 
            	            linenumber++;

            	            }


            	            }
            	            break;
            	        case 2 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:107: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_xclrule248); 

            	            }
            	            break;

            	    }

            	    match(input,18,FOLLOW_18_in_xclrule252); 
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:116: ( WS )?
            	    int alt22=2;
            	    int LA22_0 = input.LA(1);

            	    if ( (LA22_0==WS) ) {
            	        alt22=1;
            	    }
            	    switch (alt22) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:116: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_xclrule254); 

            	            }
            	            break;

            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:120: ( ( NEWLINE )* annotation ( WS )* )?
            	    int alt25=2;
            	    alt25 = dfa25.predict(input);
            	    switch (alt25) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:121: ( NEWLINE )* annotation ( WS )*
            	            {
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:121: ( NEWLINE )*
            	            loop23:
            	            do {
            	                int alt23=2;
            	                int LA23_0 = input.LA(1);

            	                if ( (LA23_0==NEWLINE) ) {
            	                    alt23=1;
            	                }


            	                switch (alt23) {
            	            	case 1 :
            	            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:122: NEWLINE
            	            	    {
            	            	    match(input,NEWLINE,FOLLOW_NEWLINE_in_xclrule259); 
            	            	    linenumber++;

            	            	    }
            	            	    break;

            	            	default :
            	            	    break loop23;
            	                }
            	            } while (true);

            	            pushFollow(FOLLOW_annotation_in_xclrule265);
            	            annotation();

            	            state._fsp--;

            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:159: ( WS )*
            	            loop24:
            	            do {
            	                int alt24=2;
            	                int LA24_0 = input.LA(1);

            	                if ( (LA24_0==WS) ) {
            	                    alt24=1;
            	                }


            	                switch (alt24) {
            	            	case 1 :
            	            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:159: WS
            	            	    {
            	            	    match(input,WS,FOLLOW_WS_in_xclrule267); 

            	            	    }
            	            	    break;

            	            	default :
            	            	    break loop24;
            	                }
            	            } while (true);


            	            }
            	            break;

            	    }

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:165: ( NEWLINE ( WS )? )*
            	    loop27:
            	    do {
            	        int alt27=2;
            	        int LA27_0 = input.LA(1);

            	        if ( (LA27_0==NEWLINE) ) {
            	            alt27=1;
            	        }


            	        switch (alt27) {
            	    	case 1 :
            	    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:166: NEWLINE ( WS )?
            	    	    {
            	    	    match(input,NEWLINE,FOLLOW_NEWLINE_in_xclrule273); 
            	    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:174: ( WS )?
            	    	    int alt26=2;
            	    	    int LA26_0 = input.LA(1);

            	    	    if ( (LA26_0==WS) ) {
            	    	        alt26=1;
            	    	    }
            	    	    switch (alt26) {
            	    	        case 1 :
            	    	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:126:174: WS
            	    	            {
            	    	            match(input,WS,FOLLOW_WS_in_xclrule275); 

            	    	            }
            	    	            break;

            	    	    }


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop27;
            	        }
            	    } while (true);


            	    if (validknowledge && success){
            	    							  diagnosis.addKnowledge(PSMethodXCL.class, model, XCLModel.XCLMODEL);
            	    							  report.note(new Message(VerbalizationManager.getInstance().verbalize(model,RenderingFormat.HTML)));
            	    							  }
            	    repdead=report;


            	    }
            	    break;

            	default :
            	    if ( cnt28 >= 1 ) break loop28;
                        EarlyExitException eee =
                            new EarlyExitException(28, input);
                        throw eee;
                }
                cnt28++;
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_xclrule285); 

            return report;


            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        repdead=report;
                        recover(input,re);
                    
        }
        finally {
        }
        return repdead;
    }
    // $ANTLR end "xclrule"


    // $ANTLR start "safecast"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:145:1: safecast[Question question] returns [QuestionChoice qc] : ;
    public final QuestionChoice safecast(Question question) throws RecognitionException {
        QuestionChoice qc = null;

        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:145:56: ()
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:146:3: 
            {

            				if (question instanceof QuestionChoice){
            					qc= (QuestionChoice) question;	
            	 				}
            				else {					 
            				     qc = null;
            				}
            		

            }

        }
        finally {
        }
        return qc;
    }
    // $ANTLR end "safecast"


    // $ANTLR start "body"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:157:1: body : ( ( ( WS )? relation ( ',' | WS )* ( SL_COMMENT | NEWLINE )? ) | ( SL_COMMENT ) )* ;
    public final void body() throws RecognitionException {
        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:157:9: ( ( ( ( WS )? relation ( ',' | WS )* ( SL_COMMENT | NEWLINE )? ) | ( SL_COMMENT ) )* )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:1: ( ( ( WS )? relation ( ',' | WS )* ( SL_COMMENT | NEWLINE )? ) | ( SL_COMMENT ) )*
            {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:1: ( ( ( WS )? relation ( ',' | WS )* ( SL_COMMENT | NEWLINE )? ) | ( SL_COMMENT ) )*
            loop32:
            do {
                int alt32=3;
                switch ( input.LA(1) ) {
                case WS:
                    {
                    int LA32_2 = input.LA(2);

                    if ( (LA32_2==STRING||LA32_2==ANY) ) {
                        alt32=1;
                    }


                    }
                    break;
                case STRING:
                case ANY:
                    {
                    alt32=1;
                    }
                    break;
                case SL_COMMENT:
                    {
                    alt32=2;
                    }
                    break;

                }

                switch (alt32) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:2: ( ( WS )? relation ( ',' | WS )* ( SL_COMMENT | NEWLINE )? )
            	    {
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:2: ( ( WS )? relation ( ',' | WS )* ( SL_COMMENT | NEWLINE )? )
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:3: ( WS )? relation ( ',' | WS )* ( SL_COMMENT | NEWLINE )?
            	    {
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:3: ( WS )?
            	    int alt29=2;
            	    int LA29_0 = input.LA(1);

            	    if ( (LA29_0==WS) ) {
            	        alt29=1;
            	    }
            	    switch (alt29) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:3: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_body325); 

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_relation_in_body328);
            	    relation();

            	    state._fsp--;

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:16: ( ',' | WS )*
            	    loop30:
            	    do {
            	        int alt30=2;
            	        int LA30_0 = input.LA(1);

            	        if ( (LA30_0==WS||LA30_0==14) ) {
            	            alt30=1;
            	        }


            	        switch (alt30) {
            	    	case 1 :
            	    	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:
            	    	    {
            	    	    if ( input.LA(1)==WS||input.LA(1)==14 ) {
            	    	        input.consume();
            	    	        state.errorRecovery=false;
            	    	    }
            	    	    else {
            	    	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	    	        throw mse;
            	    	    }


            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop30;
            	        }
            	    } while (true);

            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:26: ( SL_COMMENT | NEWLINE )?
            	    int alt31=3;
            	    int LA31_0 = input.LA(1);

            	    if ( (LA31_0==SL_COMMENT) ) {
            	        alt31=1;
            	    }
            	    else if ( (LA31_0==NEWLINE) ) {
            	        alt31=2;
            	    }
            	    switch (alt31) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:27: SL_COMMENT
            	            {
            	            match(input,SL_COMMENT,FOLLOW_SL_COMMENT_in_body338); 
            	            linenumber++;

            	            }
            	            break;
            	        case 2 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:53: NEWLINE
            	            {
            	            match(input,NEWLINE,FOLLOW_NEWLINE_in_body341); 
            	            linenumber++;

            	            }
            	            break;

            	    }


            	    }


            	    }
            	    break;
            	case 2 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:79: ( SL_COMMENT )
            	    {
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:79: ( SL_COMMENT )
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:158:80: SL_COMMENT
            	    {
            	    match(input,SL_COMMENT,FOLLOW_SL_COMMENT_in_body348); 
            	    linenumber++;

            	    }


            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
                    
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "body"


    // $ANTLR start "relation"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:168:1: relation : questionname= name ( WS )? (condition= solutionpart[question] ( ( ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ | ( ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ )? | ( ALL ( WS )? names= list ) | ( IN ( WS )? names= list ) ) ( ( WS )? ( '[' '--' ']' | '[++]' | '[' n= STRING ']' | '[!]' ) )? ;
    public final void relation() throws RecognitionException {
        Token n=null;
        String questionname = null;

        AbstractCondition condition = null;

        ArrayList<String> names = null;




        		 condlist=new ArrayList<AbstractCondition>();
        		 //AbstractCondition condition;
        		 /*
        		   0  singlerelation
        		   1 or-list
        		   2 ALL 
        		   3 IN 
        		   4 and-list
        		   
        			remark: 1 is semantically equivalent to 3, but for syntax's completeness it is handled as well		  
        		 */		 
        		 int mode=0; 		
        		 boolean added=false;
        	 
        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:185:1: (questionname= name ( WS )? (condition= solutionpart[question] ( ( ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ | ( ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ )? | ( ALL ( WS )? names= list ) | ( IN ( WS )? names= list ) ) ( ( WS )? ( '[' '--' ']' | '[++]' | '[' n= STRING ']' | '[!]' ) )? )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:185:9: questionname= name ( WS )? (condition= solutionpart[question] ( ( ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ | ( ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ )? | ( ALL ( WS )? names= list ) | ( IN ( WS )? names= list ) ) ( ( WS )? ( '[' '--' ']' | '[++]' | '[' n= STRING ']' | '[!]' ) )?
            {
            pushFollow(FOLLOW_name_in_relation382);
            questionname=name();

            state._fsp--;

            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:185:27: ( WS )?
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==WS) ) {
                alt33=1;
            }
            switch (alt33) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:185:27: WS
                    {
                    match(input,WS,FOLLOW_WS_in_relation384); 

                    }
                    break;

            }

            if (validknowledge)  {
            									if (questionname==null){
            										validknowledge=false;
            										success=false;
            										report.error(new Message("null is not a question"));
            									} else{
            							   				question=kbm.findQuestion(questionname);
            											if (question==null){
            															   report.error(new QuestionNotInKBError("Question not found: "+questionname+" in line "+linenumber));
            															   success=false;
            															   }
            									}
            							   }
            							  
            							  
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:200:6: (condition= solutionpart[question] ( ( ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ | ( ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ )? | ( ALL ( WS )? names= list ) | ( IN ( WS )? names= list ) )
            int alt45=3;
            switch ( input.LA(1) ) {
            case 22:
            case 23:
            case 24:
            case 25:
            case 26:
                {
                alt45=1;
                }
                break;
            case ALL:
                {
                alt45=2;
                }
                break;
            case IN:
                {
                alt45=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 45, 0, input);

                throw nvae;
            }

            switch (alt45) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:201:11: condition= solutionpart[question] ( ( ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ | ( ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ )?
                    {
                    pushFollow(FOLLOW_solutionpart_in_relation410);
                    condition=solutionpart(question);

                    state._fsp--;

                    if (condition!=null) condlist.add(condition);
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:201:92: ( ( ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ | ( ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+ )?
                    int alt42=3;
                    switch ( input.LA(1) ) {
                        case WS:
                            {
                            int LA42_1 = input.LA(2);

                            if ( (LA42_1==OR) ) {
                                alt42=1;
                            }
                            else if ( (LA42_1==AND) ) {
                                alt42=2;
                            }
                            }
                            break;
                        case OR:
                            {
                            alt42=1;
                            }
                            break;
                        case AND:
                            {
                            alt42=2;
                            }
                            break;
                    }

                    switch (alt42) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:201:93: ( ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+
                            {
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:201:93: ( ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+
                            int cnt37=0;
                            loop37:
                            do {
                                int alt37=2;
                                int LA37_0 = input.LA(1);

                                if ( (LA37_0==WS) ) {
                                    int LA37_1 = input.LA(2);

                                    if ( (LA37_1==OR) ) {
                                        alt37=1;
                                    }


                                }
                                else if ( (LA37_0==OR) ) {
                                    alt37=1;
                                }


                                switch (alt37) {
                            	case 1 :
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:202:10: ( WS )? OR ( WS )? questionname= name ( WS )? (condition= solutionpart[question] )
                            	    {
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:202:10: ( WS )?
                            	    int alt34=2;
                            	    int LA34_0 = input.LA(1);

                            	    if ( (LA34_0==WS) ) {
                            	        alt34=1;
                            	    }
                            	    switch (alt34) {
                            	        case 1 :
                            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:202:10: WS
                            	            {
                            	            match(input,WS,FOLLOW_WS_in_relation427); 

                            	            }
                            	            break;

                            	    }

                            	    match(input,OR,FOLLOW_OR_in_relation430); 
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:202:17: ( WS )?
                            	    int alt35=2;
                            	    int LA35_0 = input.LA(1);

                            	    if ( (LA35_0==WS) ) {
                            	        alt35=1;
                            	    }
                            	    switch (alt35) {
                            	        case 1 :
                            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:202:17: WS
                            	            {
                            	            match(input,WS,FOLLOW_WS_in_relation432); 

                            	            }
                            	            break;

                            	    }

                            	    pushFollow(FOLLOW_name_in_relation437);
                            	    questionname=name();

                            	    state._fsp--;


                            	    															question=kbm.findQuestion(questionname);
                            	    											if (question==null){
                            	    															   report.error(new QuestionNotInKBError("Question not found: "+questionname+" in line "+linenumber));
                            	    															   success=false;
                            	    															   }
                            	    														 
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:208:18: ( WS )?
                            	    int alt36=2;
                            	    int LA36_0 = input.LA(1);

                            	    if ( (LA36_0==WS) ) {
                            	        alt36=1;
                            	    }
                            	    switch (alt36) {
                            	        case 1 :
                            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:208:18: WS
                            	            {
                            	            match(input,WS,FOLLOW_WS_in_relation441); 

                            	            }
                            	            break;

                            	    }

                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:208:22: (condition= solutionpart[question] )
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:209:19: condition= solutionpart[question]
                            	    {
                            	    pushFollow(FOLLOW_solutionpart_in_relation466);
                            	    condition=solutionpart(question);

                            	    state._fsp--;

                            	    if (condition!=null) condlist.add(condition);

                            	    }

                            	    mode=1;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt37 >= 1 ) break loop37;
                                        EarlyExitException eee =
                                            new EarlyExitException(37, input);
                                        throw eee;
                                }
                                cnt37++;
                            } while (true);


                            }
                            break;
                        case 2 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:212:13: ( ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+
                            {
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:212:13: ( ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] ) )+
                            int cnt41=0;
                            loop41:
                            do {
                                int alt41=2;
                                int LA41_0 = input.LA(1);

                                if ( (LA41_0==WS) ) {
                                    int LA41_1 = input.LA(2);

                                    if ( (LA41_1==AND) ) {
                                        alt41=1;
                                    }


                                }
                                else if ( (LA41_0==AND) ) {
                                    alt41=1;
                                }


                                switch (alt41) {
                            	case 1 :
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:213:10: ( WS )? AND ( WS )? questionname= name ( WS )? (condition= solutionpart[question] )
                            	    {
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:213:10: ( WS )?
                            	    int alt38=2;
                            	    int LA38_0 = input.LA(1);

                            	    if ( (LA38_0==WS) ) {
                            	        alt38=1;
                            	    }
                            	    switch (alt38) {
                            	        case 1 :
                            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:213:10: WS
                            	            {
                            	            match(input,WS,FOLLOW_WS_in_relation532); 

                            	            }
                            	            break;

                            	    }

                            	    match(input,AND,FOLLOW_AND_in_relation535); 
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:213:18: ( WS )?
                            	    int alt39=2;
                            	    int LA39_0 = input.LA(1);

                            	    if ( (LA39_0==WS) ) {
                            	        alt39=1;
                            	    }
                            	    switch (alt39) {
                            	        case 1 :
                            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:213:18: WS
                            	            {
                            	            match(input,WS,FOLLOW_WS_in_relation537); 

                            	            }
                            	            break;

                            	    }

                            	    pushFollow(FOLLOW_name_in_relation542);
                            	    questionname=name();

                            	    state._fsp--;


                            	    															question=kbm.findQuestion(questionname);
                            	    											if (question==null){
                            	    															   report.error(new QuestionNotInKBError("Question not found: "+questionname+" in line "+linenumber));
                            	    															   success=false;
                            	    															   }
                            	    														 
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:219:18: ( WS )?
                            	    int alt40=2;
                            	    int LA40_0 = input.LA(1);

                            	    if ( (LA40_0==WS) ) {
                            	        alt40=1;
                            	    }
                            	    switch (alt40) {
                            	        case 1 :
                            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:219:18: WS
                            	            {
                            	            match(input,WS,FOLLOW_WS_in_relation546); 

                            	            }
                            	            break;

                            	    }

                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:219:22: (condition= solutionpart[question] )
                            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:220:19: condition= solutionpart[question]
                            	    {
                            	    pushFollow(FOLLOW_solutionpart_in_relation571);
                            	    condition=solutionpart(question);

                            	    state._fsp--;

                            	    if (condition!=null) condlist.add(condition);

                            	    }

                            	    mode=4;

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt41 >= 1 ) break loop41;
                                        EarlyExitException eee =
                                            new EarlyExitException(41, input);
                                        throw eee;
                                }
                                cnt41++;
                            } while (true);


                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:224:8: ( ALL ( WS )? names= list )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:224:8: ( ALL ( WS )? names= list )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:225:9: ALL ( WS )? names= list
                    {
                    match(input,ALL,FOLLOW_ALL_in_relation645); 
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:225:13: ( WS )?
                    int alt43=2;
                    int LA43_0 = input.LA(1);

                    if ( (LA43_0==WS) ) {
                        alt43=1;
                    }
                    switch (alt43) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:225:13: WS
                            {
                            match(input,WS,FOLLOW_WS_in_relation647); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_list_in_relation652);
                    names=list();

                    state._fsp--;


                    							for (String current : names) {
                    									answer=kbm.findAnswerChoice(safecast(question),current);
                    									if(answer == null && question instanceof QuestionSolution) {
                    				                        answer = ((QuestionSolution)question).selectAnswerForString(current);
                    									}
                    									if (answer==null && question!=null){
                                        										 AnswerNotInKBError err=new AnswerNotInKBError("Answer not found: "+current+" in line "+linenumber);
                    															 err.setObjectName(current);
                                        										 report.error(err);
                                        										 success=false;
                    																		 } 
                    																		 else {
                    									condition=new CondEqual(question,answer);
                    									condlist.add(condition);									
                    									}
                    							}
                    							mode=2;
                    						
                    						

                    }


                    }
                    break;
                case 3 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:244:8: ( IN ( WS )? names= list )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:244:8: ( IN ( WS )? names= list )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:245:9: IN ( WS )? names= list
                    {
                    match(input,IN,FOLLOW_IN_in_relation687); 
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:245:12: ( WS )?
                    int alt44=2;
                    int LA44_0 = input.LA(1);

                    if ( (LA44_0==WS) ) {
                        alt44=1;
                    }
                    switch (alt44) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:245:12: WS
                            {
                            match(input,WS,FOLLOW_WS_in_relation689); 

                            }
                            break;

                    }

                    pushFollow(FOLLOW_list_in_relation694);
                    names=list();

                    state._fsp--;


                    							for (String current : names) {
                    									answer=kbm.findAnswerChoice(safecast(question),current);
                    									if(answer == null && question instanceof QuestionSolution) {
                    				                        answer = ((QuestionSolution)question).selectAnswerForString(current);
                    									}
                    									if (answer==null && question!=null){
                                        										 AnswerNotInKBError err=new AnswerNotInKBError("Answer not found: "+current+" in line "+linenumber);
                    															 err.setObjectName(current);
                                        										 report.error(err);
                                        										 success=false;
                    																		 } 
                    																		 else {
                    									condition=new CondEqual(question,answer);
                    									condlist.add(condition);
                    																
                    									}
                    							}							
                    							mode=3;
                    						

                    }


                    }
                    break;

            }


            						if (condlist.size()>0){
            					switch (mode){
            								case 0:  //singlerelation
            								    
            								 	relation=XCLRelation.createXCLRelation(condlist.get(0));
            									break;
            								case 1: //or-list
            								    relation=XCLRelation.createXCLRelation(new CondOr(condlist));
            								   break;
            								case 4:
            								case 2: // ALL
            								    relation=XCLRelation.createXCLRelation(new CondAnd(condlist));
            								   break;
            								case 3: // IN
            								    relation=XCLRelation.createXCLRelation(new CondOr(condlist));
            								   break;
            								 }
            								 }
            					
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:285:7: ( ( WS )? ( '[' '--' ']' | '[++]' | '[' n= STRING ']' | '[!]' ) )?
            int alt48=2;
            int LA48_0 = input.LA(1);

            if ( (LA48_0==WS) ) {
                int LA48_1 = input.LA(2);

                if ( (LA48_1==13||(LA48_1>=20 && LA48_1<=21)) ) {
                    alt48=1;
                }
            }
            else if ( (LA48_0==13||(LA48_0>=20 && LA48_0<=21)) ) {
                alt48=1;
            }
            switch (alt48) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:285:8: ( WS )? ( '[' '--' ']' | '[++]' | '[' n= STRING ']' | '[!]' )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:285:8: ( WS )?
                    int alt46=2;
                    int LA46_0 = input.LA(1);

                    if ( (LA46_0==WS) ) {
                        alt46=1;
                    }
                    switch (alt46) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:285:8: WS
                            {
                            match(input,WS,FOLLOW_WS_in_relation733); 

                            }
                            break;

                    }

                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:286:1: ( '[' '--' ']' | '[++]' | '[' n= STRING ']' | '[!]' )
                    int alt47=4;
                    switch ( input.LA(1) ) {
                    case 13:
                        {
                        int LA47_1 = input.LA(2);

                        if ( (LA47_1==19) ) {
                            alt47=1;
                        }
                        else if ( (LA47_1==STRING) ) {
                            alt47=3;
                        }
                        else {
                            NoViableAltException nvae =
                                new NoViableAltException("", 47, 1, input);

                            throw nvae;
                        }
                        }
                        break;
                    case 20:
                        {
                        alt47=2;
                        }
                        break;
                    case 21:
                        {
                        alt47=4;
                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 47, 0, input);

                        throw nvae;
                    }

                    switch (alt47) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:286:2: '[' '--' ']'
                            {
                            match(input,13,FOLLOW_13_in_relation738); 
                            match(input,19,FOLLOW_19_in_relation739); 
                            match(input,15,FOLLOW_15_in_relation740); 
                            if (validknowledge && success) {model.addContradictingRelation(relation); added=true;}

                            }
                            break;
                        case 2 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:287:8: '[++]'
                            {
                            match(input,20,FOLLOW_20_in_relation751); 
                            if (validknowledge && success) {model.addSufficientRelation(relation);added=true;}

                            }
                            break;
                        case 3 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:288:8: '[' n= STRING ']'
                            {
                            match(input,13,FOLLOW_13_in_relation762); 
                            n=(Token)match(input,STRING,FOLLOW_STRING_in_relation765); 
                            match(input,15,FOLLOW_15_in_relation766); 
                            if (validknowledge && success) {relation.setWeight(Double.parseDouble(n.getText()));}

                            }
                            break;
                        case 4 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:289:8: '[!]'
                            {
                            match(input,21,FOLLOW_21_in_relation776); 
                            if (validknowledge && success) {model.addNecessaryRelation(relation);added=true;}

                            }
                            break;

                    }


                    }
                    break;

            }

             if (!added && validknowledge && success) model.addRelation(relation);

            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
                    
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "relation"


    // $ANTLR start "list"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:299:1: list returns [ArrayList<String> namelist] : '{' component= name ( ',' ( WS )? component= name )* '}' ;
    public final ArrayList<String> list() throws RecognitionException {
        ArrayList<String> namelist = null;

        String component = null;


        namelist=new ArrayList<String>();
        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:301:1: ( '{' component= name ( ',' ( WS )? component= name )* '}' )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:301:3: '{' component= name ( ',' ( WS )? component= name )* '}'
            {
            match(input,17,FOLLOW_17_in_list829); 
            pushFollow(FOLLOW_name_in_list832);
            component=name();

            state._fsp--;

            namelist.add(component);
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:301:47: ( ',' ( WS )? component= name )*
            loop50:
            do {
                int alt50=2;
                int LA50_0 = input.LA(1);

                if ( (LA50_0==14) ) {
                    alt50=1;
                }


                switch (alt50) {
            	case 1 :
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:301:48: ',' ( WS )? component= name
            	    {
            	    match(input,14,FOLLOW_14_in_list836); 
            	    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:301:52: ( WS )?
            	    int alt49=2;
            	    int LA49_0 = input.LA(1);

            	    if ( (LA49_0==WS) ) {
            	        alt49=1;
            	    }
            	    switch (alt49) {
            	        case 1 :
            	            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:301:52: WS
            	            {
            	            match(input,WS,FOLLOW_WS_in_list838); 

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_name_in_list843);
            	    component=name();

            	    state._fsp--;

            	    namelist.add(component);

            	    }
            	    break;

            	default :
            	    break loop50;
                }
            } while (true);

            match(input,18,FOLLOW_18_in_list848); 
            return namelist;

            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
                    
        }
        finally {
        }
        return namelist;
    }
    // $ANTLR end "list"


    // $ANTLR start "solutionpart"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:309:1: solutionpart[Question q] returns [AbstractCondition condition] : ( ( '=' ( WS )? (answername= name ) ) | ( '>=' ( WS )? (answername= name ) ) | ( '>' ( WS )? (answername= name ) ) | ( '<' ( WS )? (answername= name ) ) | ( '<=' ( WS )? (answername= name ) ) ) ;
    public final AbstractCondition solutionpart(Question q) throws RecognitionException {
        AbstractCondition condition = null;

        String answername = null;


        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:310:2: ( ( ( '=' ( WS )? (answername= name ) ) | ( '>=' ( WS )? (answername= name ) ) | ( '>' ( WS )? (answername= name ) ) | ( '<' ( WS )? (answername= name ) ) | ( '<=' ( WS )? (answername= name ) ) ) )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:310:11: ( ( '=' ( WS )? (answername= name ) ) | ( '>=' ( WS )? (answername= name ) ) | ( '>' ( WS )? (answername= name ) ) | ( '<' ( WS )? (answername= name ) ) | ( '<=' ( WS )? (answername= name ) ) )
            {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:310:11: ( ( '=' ( WS )? (answername= name ) ) | ( '>=' ( WS )? (answername= name ) ) | ( '>' ( WS )? (answername= name ) ) | ( '<' ( WS )? (answername= name ) ) | ( '<=' ( WS )? (answername= name ) ) )
            int alt56=5;
            switch ( input.LA(1) ) {
            case 22:
                {
                alt56=1;
                }
                break;
            case 23:
                {
                alt56=2;
                }
                break;
            case 24:
                {
                alt56=3;
                }
                break;
            case 25:
                {
                alt56=4;
                }
                break;
            case 26:
                {
                alt56=5;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 56, 0, input);

                throw nvae;
            }

            switch (alt56) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:311:12: ( '=' ( WS )? (answername= name ) )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:311:12: ( '=' ( WS )? (answername= name ) )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:311:13: '=' ( WS )? (answername= name )
                    {
                    match(input,22,FOLLOW_22_in_solutionpart891); 
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:311:16: ( WS )?
                    int alt51=2;
                    int LA51_0 = input.LA(1);

                    if ( (LA51_0==WS) ) {
                        alt51=1;
                    }
                    switch (alt51) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:311:16: WS
                            {
                            match(input,WS,FOLLOW_WS_in_solutionpart892); 

                            }
                            break;

                    }

                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:311:20: (answername= name )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:311:21: answername= name
                    {
                    pushFollow(FOLLOW_name_in_solutionpart898);
                    answername=name();

                    state._fsp--;


                    }


                    					 	    							if(q instanceof QuestionNum) {
                    					 	    								try{
                    					 	    									Double d = Double.parseDouble(answername);
                    					 	    									condition = new CondNumEqual((QuestionNum)q,d);
                    					 	    								} catch (NumberFormatException e) {
                    					 	    									report.error(new Message("invalid number:"+answername));
                    					 	    								}
                    					 	    							}else {
                    															answer=kbm.findAnswerChoice(safecast(q), answername);
                    															if(answer == null && question instanceof QuestionSolution) {
                    										                        answer = ((QuestionSolution)question).selectAnswerForString(answername);
                    										                    }
                    															if (answer==null && question!=null){
                                        	                    										 AnswerNotInKBError err=new AnswerNotInKBError("Answer not found: "+answername+" in line "+linenumber);
                                        																 err.setObjectName(answername);
                                        	                    										 report.error(err);
                                        	                    										 success=false;
                    																		 } else {
                    															condition=new CondEqual(q,answer);
                    														}																																									
                    													}
                    													

                    }


                    }
                    break;
                case 2 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:331:13: ( '>=' ( WS )? (answername= name ) )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:331:13: ( '>=' ( WS )? (answername= name ) )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:331:14: '>=' ( WS )? (answername= name )
                    {
                    match(input,23,FOLLOW_23_in_solutionpart917); 
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:331:19: ( WS )?
                    int alt52=2;
                    int LA52_0 = input.LA(1);

                    if ( (LA52_0==WS) ) {
                        alt52=1;
                    }
                    switch (alt52) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:331:19: WS
                            {
                            match(input,WS,FOLLOW_WS_in_solutionpart919); 

                            }
                            break;

                    }

                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:331:23: (answername= name )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:331:24: answername= name
                    {
                    pushFollow(FOLLOW_name_in_solutionpart925);
                    answername=name();

                    state._fsp--;


                    }


                    					 	    							if(q instanceof QuestionNum) {
                    					 	    								try{
                    					 	    									Double d = Double.parseDouble(answername);
                    					 	    									condition = new CondNumGreaterEqual((QuestionNum)q,d);
                    					 	    								} catch (NumberFormatException e) {
                    					 	    									report.error(new Message("invalid number:"+answername));
                    					 	    								}
                    					 	    							}else {
                    														answer=kbm.findAnswerChoice(safecast(q), answername);
                    														if(answer == null && question instanceof QuestionSolution) {
                    									                        answer = ((QuestionSolution)question).selectAnswerForString(answername);
                    														}
                    														// not yet implementet
                    														report.error(new Message(">= not yet implemented for ChoiceAnswers"));
                    														}
                    													  

                    }


                    }
                    break;
                case 3 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:345:12: ( '>' ( WS )? (answername= name ) )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:345:12: ( '>' ( WS )? (answername= name ) )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:345:13: '>' ( WS )? (answername= name )
                    {
                    match(input,24,FOLLOW_24_in_solutionpart942); 
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:345:17: ( WS )?
                    int alt53=2;
                    int LA53_0 = input.LA(1);

                    if ( (LA53_0==WS) ) {
                        alt53=1;
                    }
                    switch (alt53) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:345:17: WS
                            {
                            match(input,WS,FOLLOW_WS_in_solutionpart944); 

                            }
                            break;

                    }

                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:345:21: (answername= name )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:345:22: answername= name
                    {
                    pushFollow(FOLLOW_name_in_solutionpart950);
                    answername=name();

                    state._fsp--;


                    }


                    						    							if(q instanceof QuestionNum) {
                    					 	    								try{
                    					 	    									Double d = Double.parseDouble(answername);
                    					 	    									condition = new CondNumGreater((QuestionNum)q,d);
                    					 	    								} catch (NumberFormatException e) {
                    					 	    									report.error(new Message("invalid number:"+answername));
                    					 	    								}
                    					 	    							} else {
                    														answer=kbm.findAnswerChoice(safecast(q), answername);
                    														if(answer == null && question instanceof QuestionSolution) {
                    									                        answer = ((QuestionSolution)question).selectAnswerForString(answername);
                    														}
                    														// not yet implementet
                    														report.error(new Message("> not yet implemented for ChoiceAnswers"));
                    														}
                    													  

                    }


                    }
                    break;
                case 4 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:359:12: ( '<' ( WS )? (answername= name ) )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:359:12: ( '<' ( WS )? (answername= name ) )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:359:13: '<' ( WS )? (answername= name )
                    {
                    match(input,25,FOLLOW_25_in_solutionpart967); 
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:359:17: ( WS )?
                    int alt54=2;
                    int LA54_0 = input.LA(1);

                    if ( (LA54_0==WS) ) {
                        alt54=1;
                    }
                    switch (alt54) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:359:17: WS
                            {
                            match(input,WS,FOLLOW_WS_in_solutionpart969); 

                            }
                            break;

                    }

                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:359:21: (answername= name )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:359:22: answername= name
                    {
                    pushFollow(FOLLOW_name_in_solutionpart975);
                    answername=name();

                    state._fsp--;


                    }


                    						    							if(q instanceof QuestionNum) {
                    					 	    								try{
                    					 	    									Double d = Double.parseDouble(answername);
                    					 	    									condition = new CondNumLess((QuestionNum)q,d);
                    					 	    								} catch (NumberFormatException e) {
                    					 	    									report.error(new Message("invalid number:"+answername));
                    					 	    								}
                    					 	    							}else {
                    														answer=kbm.findAnswerChoice(safecast(q), answername);
                    														// not yet implementet
                    														report.error(new Message("< not yet implemented for ChoiceAnswers"));
                    														}
                    													  

                    }


                    }
                    break;
                case 5 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:373:12: ( '<=' ( WS )? (answername= name ) )
                    {
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:373:12: ( '<=' ( WS )? (answername= name ) )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:373:13: '<=' ( WS )? (answername= name )
                    {
                    match(input,26,FOLLOW_26_in_solutionpart992); 
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:373:18: ( WS )?
                    int alt55=2;
                    int LA55_0 = input.LA(1);

                    if ( (LA55_0==WS) ) {
                        alt55=1;
                    }
                    switch (alt55) {
                        case 1 :
                            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:373:18: WS
                            {
                            match(input,WS,FOLLOW_WS_in_solutionpart994); 

                            }
                            break;

                    }

                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:373:22: (answername= name )
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:373:23: answername= name
                    {
                    pushFollow(FOLLOW_name_in_solutionpart1000);
                    answername=name();

                    state._fsp--;


                    }


                    						    							if(q instanceof QuestionNum) {
                    					 	    								try{
                    					 	    									Double d = Double.parseDouble(answername);
                    					 	    									condition = new CondNumLessEqual((QuestionNum)q,d);
                    					 	    								} catch (NumberFormatException e) {
                    					 	    									report.error(new Message("invalid number:"+answername));
                    					 	    								}
                    					 	    							}else {
                    														answer=kbm.findAnswerChoice(safecast(q), answername);
                    														// not yet implementet
                    														report.error(new Message("<= not yet implemented for ChoiceAnswers"));
                    														}
                    													  

                    }


                    }
                    break;

            }

            return condition;

            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
                    
        }
        finally {
        }
        return condition;
    }
    // $ANTLR end "solutionpart"


    // $ANTLR start "statement"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:395:1: statement : name ( WS )? ( '=' | '>=' ) ( WS )? name ;
    public final void statement() throws RecognitionException {
        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:396:9: ( name ( WS )? ( '=' | '>=' ) ( WS )? name )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:396:17: name ( WS )? ( '=' | '>=' ) ( WS )? name
            {
            pushFollow(FOLLOW_name_in_statement1064);
            name();

            state._fsp--;

            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:396:22: ( WS )?
            int alt57=2;
            int LA57_0 = input.LA(1);

            if ( (LA57_0==WS) ) {
                alt57=1;
            }
            switch (alt57) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:396:22: WS
                    {
                    match(input,WS,FOLLOW_WS_in_statement1066); 

                    }
                    break;

            }

            if ( (input.LA(1)>=22 && input.LA(1)<=23) ) {
                input.consume();
                state.errorRecovery=false;
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                throw mse;
            }

            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:396:37: ( WS )?
            int alt58=2;
            int LA58_0 = input.LA(1);

            if ( (LA58_0==WS) ) {
                alt58=1;
            }
            switch (alt58) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:396:37: WS
                    {
                    match(input,WS,FOLLOW_WS_in_statement1075); 

                    }
                    break;

            }

            pushFollow(FOLLOW_name_in_statement1078);
            name();

            state._fsp--;


            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "statement"


    // $ANTLR start "ethres"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:402:1: ethres : 'establishedThreshold' ( WS )? '=' ( WS )? a= STRING ;
    public final void ethres() throws RecognitionException {
        Token a=null;

        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:402:9: ( 'establishedThreshold' ( WS )? '=' ( WS )? a= STRING )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:402:17: 'establishedThreshold' ( WS )? '=' ( WS )? a= STRING
            {
            match(input,27,FOLLOW_27_in_ethres1100); 
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:402:40: ( WS )?
            int alt59=2;
            int LA59_0 = input.LA(1);

            if ( (LA59_0==WS) ) {
                alt59=1;
            }
            switch (alt59) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:402:40: WS
                    {
                    match(input,WS,FOLLOW_WS_in_ethres1102); 

                    }
                    break;

            }

            match(input,22,FOLLOW_22_in_ethres1105); 
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:402:48: ( WS )?
            int alt60=2;
            int LA60_0 = input.LA(1);

            if ( (LA60_0==WS) ) {
                alt60=1;
            }
            switch (alt60) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:402:48: WS
                    {
                    match(input,WS,FOLLOW_WS_in_ethres1107); 

                    }
                    break;

            }

            a=(Token)match(input,STRING,FOLLOW_STRING_in_ethres1112); 
            model.setEstablishedThreshold(Double.parseDouble(a.getText()));

            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "ethres"


    // $ANTLR start "sthres"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:406:1: sthres : 'suggestedThreshold' ( WS )? '=' ( WS )? a= STRING ;
    public final void sthres() throws RecognitionException {
        Token a=null;

        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:406:9: ( 'suggestedThreshold' ( WS )? '=' ( WS )? a= STRING )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:406:17: 'suggestedThreshold' ( WS )? '=' ( WS )? a= STRING
            {
            match(input,28,FOLLOW_28_in_sthres1133); 
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:406:38: ( WS )?
            int alt61=2;
            int LA61_0 = input.LA(1);

            if ( (LA61_0==WS) ) {
                alt61=1;
            }
            switch (alt61) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:406:38: WS
                    {
                    match(input,WS,FOLLOW_WS_in_sthres1135); 

                    }
                    break;

            }

            match(input,22,FOLLOW_22_in_sthres1138); 
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:406:46: ( WS )?
            int alt62=2;
            int LA62_0 = input.LA(1);

            if ( (LA62_0==WS) ) {
                alt62=1;
            }
            switch (alt62) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:406:46: WS
                    {
                    match(input,WS,FOLLOW_WS_in_sthres1140); 

                    }
                    break;

            }

            a=(Token)match(input,STRING,FOLLOW_STRING_in_sthres1145); 
            model.setSuggestedThreshold(Double.parseDouble(a.getText()));

            }

        }
        catch (RecognitionException re) {

                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "sthres"


    // $ANTLR start "msup"
    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:410:1: msup : 'minSupport' ( WS )? '=' ( WS )? a= STRING ;
    public final void msup() throws RecognitionException {
        Token a=null;

        try {
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:410:9: ( 'minSupport' ( WS )? '=' ( WS )? a= STRING )
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:410:17: 'minSupport' ( WS )? '=' ( WS )? a= STRING
            {
            match(input,29,FOLLOW_29_in_msup1168); 
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:410:30: ( WS )?
            int alt63=2;
            int LA63_0 = input.LA(1);

            if ( (LA63_0==WS) ) {
                alt63=1;
            }
            switch (alt63) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:410:30: WS
                    {
                    match(input,WS,FOLLOW_WS_in_msup1170); 

                    }
                    break;

            }

            match(input,22,FOLLOW_22_in_msup1173); 
            // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:410:38: ( WS )?
            int alt64=2;
            int LA64_0 = input.LA(1);

            if ( (LA64_0==WS) ) {
                alt64=1;
            }
            switch (alt64) {
                case 1 :
                    // /HOME/s179455/workspaces/knowwe-semantic/d3web-TextParser/resources/xclPatternParser/xcl.g:410:38: WS
                    {
                    match(input,WS,FOLLOW_WS_in_msup1175); 

                    }
                    break;

            }

            a=(Token)match(input,STRING,FOLLOW_STRING_in_msup1180); 
            model.setMinSupport(Double.parseDouble(a.getText()));

            }

        }
        catch (RecognitionException re) {


                        reportError(re);
                        report.error(new Message(re.toString()));
                        recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "msup"

    // Delegated rules


    protected DFA25 dfa25 = new DFA25(this);
    static final String DFA25_eotS =
        "\4\uffff";
    static final String DFA25_eofS =
        "\2\3\2\uffff";
    static final String DFA25_minS =
        "\2\4\2\uffff";
    static final String DFA25_maxS =
        "\2\15\2\uffff";
    static final String DFA25_acceptS =
        "\2\uffff\1\1\1\2";
    static final String DFA25_specialS =
        "\4\uffff}>";
    static final String[] DFA25_transitionS = {
            "\3\3\1\1\5\uffff\1\2",
            "\3\3\1\1\5\uffff\1\2",
            "",
            ""
    };

    static final short[] DFA25_eot = DFA.unpackEncodedString(DFA25_eotS);
    static final short[] DFA25_eof = DFA.unpackEncodedString(DFA25_eofS);
    static final char[] DFA25_min = DFA.unpackEncodedStringToUnsignedChars(DFA25_minS);
    static final char[] DFA25_max = DFA.unpackEncodedStringToUnsignedChars(DFA25_maxS);
    static final short[] DFA25_accept = DFA.unpackEncodedString(DFA25_acceptS);
    static final short[] DFA25_special = DFA.unpackEncodedString(DFA25_specialS);
    static final short[][] DFA25_transition;

    static {
        int numStates = DFA25_transitionS.length;
        DFA25_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA25_transition[i] = DFA.unpackEncodedString(DFA25_transitionS[i]);
        }
    }

    class DFA25 extends DFA {

        public DFA25(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 25;
            this.eot = DFA25_eot;
            this.eof = DFA25_eof;
            this.min = DFA25_min;
            this.max = DFA25_max;
            this.accept = DFA25_accept;
            this.special = DFA25_special;
            this.transition = DFA25_transition;
        }
        public String getDescription() {
            return "126:120: ( ( NEWLINE )* annotation ( WS )* )?";
        }
    }
 

    public static final BitSet FOLLOW_STRING_in_name50 = new BitSet(new long[]{0x0000000000000032L});
    public static final BitSet FOLLOW_WS_in_name57 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_STRING_in_name66 = new BitSet(new long[]{0x0000000000000032L});
    public static final BitSet FOLLOW_ANY_in_name86 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_annotation117 = new BitSet(new long[]{0x00000000380080A0L});
    public static final BitSet FOLLOW_WS_in_annotation120 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_annotation123 = new BitSet(new long[]{0x0000000038008020L});
    public static final BitSet FOLLOW_WS_in_annotation130 = new BitSet(new long[]{0x0000000038000020L});
    public static final BitSet FOLLOW_ethres_in_annotation134 = new BitSet(new long[]{0x000000003800C0A0L});
    public static final BitSet FOLLOW_sthres_in_annotation136 = new BitSet(new long[]{0x000000003800C0A0L});
    public static final BitSet FOLLOW_msup_in_annotation138 = new BitSet(new long[]{0x000000003800C0A0L});
    public static final BitSet FOLLOW_WS_in_annotation141 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_14_in_annotation144 = new BitSet(new long[]{0x00000000380080A0L});
    public static final BitSet FOLLOW_NEWLINE_in_annotation149 = new BitSet(new long[]{0x0000000038008020L});
    public static final BitSet FOLLOW_WS_in_annotation157 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_annotation160 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NEWLINE_in_xclrule191 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_xclrule197 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_xclrule202 = new BitSet(new long[]{0x0000000000030020L});
    public static final BitSet FOLLOW_WS_in_xclrule207 = new BitSet(new long[]{0x0000000000030000L});
    public static final BitSet FOLLOW_16_in_xclrule211 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_xclrule213 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_xclrule218 = new BitSet(new long[]{0x0000000000020020L});
    public static final BitSet FOLLOW_WS_in_xclrule220 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_17_in_xclrule225 = new BitSet(new long[]{0x00000000000401F0L});
    public static final BitSet FOLLOW_WS_in_xclrule230 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_NEWLINE_in_xclrule233 = new BitSet(new long[]{0x00000000000401F0L});
    public static final BitSet FOLLOW_body_in_xclrule239 = new BitSet(new long[]{0x00000000000400A0L});
    public static final BitSet FOLLOW_NEWLINE_in_xclrule243 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_WS_in_xclrule248 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_18_in_xclrule252 = new BitSet(new long[]{0x00000000000020F0L});
    public static final BitSet FOLLOW_WS_in_xclrule254 = new BitSet(new long[]{0x00000000000020F0L});
    public static final BitSet FOLLOW_NEWLINE_in_xclrule259 = new BitSet(new long[]{0x0000000000002080L});
    public static final BitSet FOLLOW_annotation_in_xclrule265 = new BitSet(new long[]{0x00000000000000F0L});
    public static final BitSet FOLLOW_WS_in_xclrule267 = new BitSet(new long[]{0x00000000000000F0L});
    public static final BitSet FOLLOW_NEWLINE_in_xclrule273 = new BitSet(new long[]{0x00000000000000F0L});
    public static final BitSet FOLLOW_WS_in_xclrule275 = new BitSet(new long[]{0x00000000000000F0L});
    public static final BitSet FOLLOW_EOF_in_xclrule285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WS_in_body325 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_relation_in_body328 = new BitSet(new long[]{0x00000000000041F2L});
    public static final BitSet FOLLOW_set_in_body330 = new BitSet(new long[]{0x00000000000041F2L});
    public static final BitSet FOLLOW_SL_COMMENT_in_body338 = new BitSet(new long[]{0x0000000000000172L});
    public static final BitSet FOLLOW_NEWLINE_in_body341 = new BitSet(new long[]{0x0000000000000172L});
    public static final BitSet FOLLOW_SL_COMMENT_in_body348 = new BitSet(new long[]{0x0000000000000172L});
    public static final BitSet FOLLOW_name_in_relation382 = new BitSet(new long[]{0x0000000007C01820L});
    public static final BitSet FOLLOW_WS_in_relation384 = new BitSet(new long[]{0x0000000007C01800L});
    public static final BitSet FOLLOW_solutionpart_in_relation410 = new BitSet(new long[]{0x0000000000302622L});
    public static final BitSet FOLLOW_WS_in_relation427 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_OR_in_relation430 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_relation432 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_relation437 = new BitSet(new long[]{0x0000000007C00020L});
    public static final BitSet FOLLOW_WS_in_relation441 = new BitSet(new long[]{0x0000000007C00000L});
    public static final BitSet FOLLOW_solutionpart_in_relation466 = new BitSet(new long[]{0x0000000000302222L});
    public static final BitSet FOLLOW_WS_in_relation532 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_AND_in_relation535 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_relation537 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_relation542 = new BitSet(new long[]{0x0000000007C00020L});
    public static final BitSet FOLLOW_WS_in_relation546 = new BitSet(new long[]{0x0000000007C00000L});
    public static final BitSet FOLLOW_solutionpart_in_relation571 = new BitSet(new long[]{0x0000000000302422L});
    public static final BitSet FOLLOW_ALL_in_relation645 = new BitSet(new long[]{0x0000000000020020L});
    public static final BitSet FOLLOW_WS_in_relation647 = new BitSet(new long[]{0x0000000000020020L});
    public static final BitSet FOLLOW_list_in_relation652 = new BitSet(new long[]{0x0000000000302022L});
    public static final BitSet FOLLOW_IN_in_relation687 = new BitSet(new long[]{0x0000000000020020L});
    public static final BitSet FOLLOW_WS_in_relation689 = new BitSet(new long[]{0x0000000000020020L});
    public static final BitSet FOLLOW_list_in_relation694 = new BitSet(new long[]{0x0000000000302022L});
    public static final BitSet FOLLOW_WS_in_relation733 = new BitSet(new long[]{0x0000000000302000L});
    public static final BitSet FOLLOW_13_in_relation738 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_19_in_relation739 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_relation740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_relation751 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_13_in_relation762 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_STRING_in_relation765 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_15_in_relation766 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_relation776 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_list829 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_list832 = new BitSet(new long[]{0x0000000000044000L});
    public static final BitSet FOLLOW_14_in_list836 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_list838 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_list843 = new BitSet(new long[]{0x0000000000044000L});
    public static final BitSet FOLLOW_18_in_list848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_22_in_solutionpart891 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_solutionpart892 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_solutionpart898 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_solutionpart917 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_solutionpart919 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_solutionpart925 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_solutionpart942 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_solutionpart944 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_solutionpart950 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_solutionpart967 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_solutionpart969 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_solutionpart975 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_solutionpart992 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_solutionpart994 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_solutionpart1000 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_name_in_statement1064 = new BitSet(new long[]{0x0000000000C00020L});
    public static final BitSet FOLLOW_WS_in_statement1066 = new BitSet(new long[]{0x0000000000C00000L});
    public static final BitSet FOLLOW_set_in_statement1069 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_WS_in_statement1075 = new BitSet(new long[]{0x0000000000000070L});
    public static final BitSet FOLLOW_name_in_statement1078 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_ethres1100 = new BitSet(new long[]{0x0000000000400020L});
    public static final BitSet FOLLOW_WS_in_ethres1102 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_ethres1105 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_WS_in_ethres1107 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_STRING_in_ethres1112 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_sthres1133 = new BitSet(new long[]{0x0000000000400020L});
    public static final BitSet FOLLOW_WS_in_sthres1135 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_sthres1138 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_WS_in_sthres1140 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_STRING_in_sthres1145 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_msup1168 = new BitSet(new long[]{0x0000000000400020L});
    public static final BitSet FOLLOW_WS_in_msup1170 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_22_in_msup1173 = new BitSet(new long[]{0x0000000000000030L});
    public static final BitSet FOLLOW_WS_in_msup1175 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_STRING_in_msup1180 = new BitSet(new long[]{0x0000000000000002L});

}