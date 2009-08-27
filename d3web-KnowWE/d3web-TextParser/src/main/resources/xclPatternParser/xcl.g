grammar xcl;
/*
 * run something alone the lines of 
 * java -cp /path/to/lib/antlr-3.1.1.jar org.antlr.Tool -o ../ xcl.g
 * to generate
 *  
 */ 

@header {
package de.d3web.textParser.xclPatternParser;

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
import de.d3web.kernel.domainModel.qasets.QuestionState;
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
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.*;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
}
 
@lexer::header {
package de.d3web.textParser.xclPatternParser;
}
 
@members{
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

}

name
returns [String name]
        : ((a=STRING {name=a.getText();}) ((WS {name+=" ";})? (a=STRING {name+=a.getText();}))*)
        |(a=ANY{name=a.getText();if (name.length()>2){
                    name=name.substring(1,name.length()-1);
                    }
                    })
        ;catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);
        }

annotation
        :'[' (WS? NEWLINE {linenumber++;})? (WS? (ethres|sthres|msup)(WS? ',')? (NEWLINE {linenumber++;})?)* WS? ']';catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);
        }


xclrule[KnowledgeBase kbase] returns [Report repdead]
@init{
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
	 }
:      ({success=true;validknowledge=true;}(NEWLINE {linenumber++;})? WS?(erg=name {
					  
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
					  
}) WS? ('~' WS? scmweight=name WS?)? '{'  ( WS? NEWLINE {linenumber++;})* body ((NEWLINE {linenumber++;})|WS)? '}' WS? ((NEWLINE {linenumber++;})* annotation WS*)? (NEWLINE WS?)*
{
if (validknowledge && success){
							  diagnosis.addKnowledge(PSMethodXCL.class, model, XCLModel.XCLMODEL);
							  report.note(new Message(VerbalizationManager.getInstance().verbalize(model,RenderingFormat.HTML)));
							  }
repdead=report;
})+  EOF
{
return report;
}
;
catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            repdead=report;
            recover(input,re);
        }

safecast[Question question] returns [QuestionChoice qc]:
		{
				if (question instanceof QuestionChoice){
					qc= (QuestionChoice) question;	
	 				}
				else {					 
				     qc = null;
				}
		}
		;


body    :
((WS? relation (','|WS)* (SL_COMMENT{linenumber++;}|NEWLINE{linenumber++;})?)|(SL_COMMENT {linenumber++;}))*
//(WS? relation (WS|(',' WS?))? ((NEWLINE {linenumber++;})|(SL_COMMENT {linenumber++;}))
//|(WS? SL_COMMENT {linenumber++;}))*
//(WS? relation)?
;catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);
        }

relation
@init{

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
	 }
:       questionname=name WS? {if (validknowledge)  {
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
							  
							  } 
					( 
					     condition=solutionpart[question] {if (condition!=null) condlist.add(condition);} ((
						  	WS? OR WS? questionname=name {
															question=kbm.findQuestion(questionname);
											if (question==null){
															   report.error(new QuestionNotInKBError("Question not found: "+questionname+" in line "+linenumber));
															   success=false;
															   }
														 } WS? (
																		condition=solutionpart[question] {if (condition!=null) condlist.add(condition);}
																	)
																	{mode=1;}
						   )+|(
						  	WS? AND WS? questionname=name {
															question=kbm.findQuestion(questionname);
											if (question==null){
															   report.error(new QuestionNotInKBError("Question not found: "+questionname+" in line "+linenumber));
															   success=false;
															   }
														 } WS? (
																		condition=solutionpart[question] {if (condition!=null) condlist.add(condition);}
																	)
																	{mode=4;}
						   )+)?
					 |(
					  	ALL WS? names=list
						{
							for (String current : names) {
									answer=kbm.findAnswerChoice(safecast(question),current);
									if(answer == null && question instanceof QuestionState) {
                        answer = ((QuestionState)question).selectAnswerForString(current);
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
						)
					 |(
					  	IN WS? names=list
						{
							for (String current : names) {
									answer=kbm.findAnswerChoice(safecast(question),current);
									if(answer == null && question instanceof QuestionState) {
                        answer = ((QuestionState)question).selectAnswerForString(current);
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
						)
					)
					{
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
					}
						(WS? 
('[''--'']' {if (validknowledge && success) {model.addContradictingRelation(relation); added=true;}}
						|'[++]' {if (validknowledge && success) {model.addSufficientRelation(relation);added=true;}}
						|'['n=STRING']'{if (validknowledge && success) {relation.setWeight(Double.parseDouble(n.getText()));}}
						|'[!]' {if (validknowledge && success) {model.addNecessaryRelation(relation);added=true;}}
						)
						)?
				{ if (!added && validknowledge && success) model.addRelation(relation);}
				;catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);
        }

list returns [ArrayList<String> namelist]  
@init{namelist=new ArrayList<String>();}
: '{'component=name {namelist.add(component);}(',' WS? component=name{namelist.add(component);})* '}'
{return namelist;}
;catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);
        }

solutionpart[Question q] returns [AbstractCondition condition]
	:			     (
					 	    ('='WS? (answername=name)	{
					 	    							if(q instanceof QuestionNum) {
					 	    								try{
					 	    									Double d = Double.parseDouble(answername);
					 	    									condition = new CondNumEqual((QuestionNum)q,d);
					 	    								} catch (NumberFormatException e) {
					 	    									report.error(new Message("invalid number:"+answername));
					 	    								}
					 	    							}else {
															answer=kbm.findAnswerChoice(safecast(q), answername);
															if(answer == null && q instanceof QuestionState) {
                                     answer = ((QuestionState)q).selectAnswerForString(answername);
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
													})
					 	    |('>=' WS? (answername=name){
					 	    							if(q instanceof QuestionNum) {
					 	    								try{
					 	    									Double d = Double.parseDouble(answername);
					 	    									condition = new CondNumGreaterEqual((QuestionNum)q,d);
					 	    								} catch (NumberFormatException e) {
					 	    									report.error(new Message("invalid number:"+answername));
					 	    								}
					 	    							}else {
														answer=kbm.findAnswerChoice(safecast(q), answername);
														// not yet implementet
														report.error(new Message(">= not yet implemented for ChoiceAnswers"));
														}
													  })
						    |('>' WS? (answername=name){
						    							if(q instanceof QuestionNum) {
					 	    								try{
					 	    									Double d = Double.parseDouble(answername);
					 	    									condition = new CondNumGreater((QuestionNum)q,d);
					 	    								} catch (NumberFormatException e) {
					 	    									report.error(new Message("invalid number:"+answername));
					 	    								}
					 	    							} else {
														answer=kbm.findAnswerChoice(safecast(q), answername);
														// not yet implementet
														report.error(new Message("> not yet implemented for ChoiceAnswers"));
														}
													  })
						    |('<' WS? (answername=name){
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
													  })
						    |('<=' WS? (answername=name){
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
													  })
						   
					     ){return condition;};
					     catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);
        }

statement
        :       name WS? ('='|'>=') WS? name; catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);}


ethres  :       'establishedThreshold' WS? '=' WS? a=STRING{model.setEstablishedThreshold(Double.parseDouble(a.getText()));}; catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);}
sthres  :       'suggestedThreshold' WS? '=' WS? a=STRING{model.setSuggestedThreshold(Double.parseDouble(a.getText()));}; catch [RecognitionException re] {
            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);}
msup    :       'minSupport' WS? '=' WS? a=STRING{model.setMinSupport(Double.parseDouble(a.getText()));}; catch [RecognitionException re] {

            reportError(re);
            report.error(new Message(re.toString()));
            recover(input,re);}

//NUMBER  :       '-'? ('0'..'9')+ ('.' ('0'..'9')+)?;

ALL
        :       'ALL';
IN
        :       'IN';
OR
        :       'OR';
AND:'AND';  

STRING  :       ('a'..'z'|'A'..'Z'|'.'|'-'|'0'..'9'|'+'|':'|'ä'|'ö'|'ü'|'Ä'|'Ö'|'Ü'|'ß'|'_'|'?'|'('|')'|'/'|';'|'!')+;

ANY:'"' (~'"')* '"';


WS  :   (' '|'\t')+
    ;
    
NEWLINE :       (('\r')?'\n')+ ;

SL_COMMENT:    '//'(~('\n'|'\r'))* ('\n'|'\r'('\n'));
    
