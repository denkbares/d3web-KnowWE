package de.d3web.KnOfficeParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.d3web.KnOfficeParser.util.ConditionGenerator;
import de.d3web.KnOfficeParser.util.D3webQuestionFactory;
import de.d3web.report.Message;
import de.d3web.KnOfficeParser.util.MessageKnOfficeGenerator;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.domainModel.qasets.QuestionYN;
import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.domainModel.ruleCondition.CondAnd;
import de.d3web.kernel.domainModel.ruleCondition.CondDState;
import de.d3web.kernel.domainModel.ruleCondition.CondEqual;
import de.d3web.kernel.domainModel.ruleCondition.CondKnown;
import de.d3web.kernel.domainModel.ruleCondition.CondMofN;
import de.d3web.kernel.domainModel.ruleCondition.CondNot;
import de.d3web.kernel.domainModel.ruleCondition.CondOr;
import de.d3web.kernel.domainModel.ruleCondition.CondUnknown;
import de.d3web.kernel.domainModel.ruleCondition.TerminalCondition;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
/**
 * Klasse um Conditionen in d3web zu erstellen
 * @author Markus Friedrich
 *
 */
public class D3webConditionBuilder implements ConditionBuilder{
	private String file;
	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	private Stack<AbstractCondition> condstack = new Stack<AbstractCondition>();
	private List<Message> errors = new ArrayList<Message>();
	private boolean lazy=false;
	private IDObjectManagement idom;
	
	private Map<String, Question> questions = new HashMap<String, Question>();
	private boolean useQuestionmap = true;
	
	
	public boolean isUseQuestionmap() {
		return useQuestionmap;
	}

	public void setUseQuestionmap(boolean useQuestionmap) {
		this.useQuestionmap = useQuestionmap;
	}

	public boolean isLazy() {
		return lazy;
	}

	public void setLazy(boolean lazy) {
		this.lazy = lazy;
	}

	public D3webConditionBuilder(String file, List<Message> errors, IDObjectManagement idom) {
		this.file=file;
		this.errors=errors;
		this.idom=idom;
	}
	
	public IDObjectManagement getIdom() {
		return idom;
	}

	public void setIdom(IDObjectManagement idom) {
		this.idom = idom;
	}

	public AbstractCondition pop() {
		if (!condstack.isEmpty()) {
			return condstack.pop();
		} else {
			return null;
		}
	}
	
	@Override
	public void condition(int line, String linetext, String qname, String type, String op, String value) {
		Diagnosis diag = idom.findDiagnosis(qname);
		if (diag!=null) {
			if (value.equalsIgnoreCase("established")||value.equalsIgnoreCase("etabliert")) {
				condstack.push(new CondDState(diag, DiagnosisState.ESTABLISHED, PSMethodHeuristic.class));
			} else  if (value.equalsIgnoreCase("excluded")||value.equalsIgnoreCase("ausgeschlossen")){
				condstack.push(new CondDState(diag, DiagnosisState.EXCLUDED, PSMethodHeuristic.class));
			} else  if (value.equalsIgnoreCase("suggested")||value.equalsIgnoreCase("verdächtigt")){
				condstack.push(new CondDState(diag, DiagnosisState.SUGGESTED, PSMethodHeuristic.class));
			} 
//			else if (value.equalsIgnoreCase("unclear")||value.equalsIgnoreCase("unklar")){
//				condstack.push(new CondDState(diag, DiagnosisState.UNCLEAR, null));
//			}
			else {
				condstack.push(null);
				errors.add(MessageKnOfficeGenerator.createWrongDiagState(file, line, linetext, value));
			}
			return;
		}
		Question question = null;
		if (useQuestionmap) question = this.questions.get(qname); 
		if(question == null) {
			question = idom.findQuestion(qname);
			if(question != null) {
				this.questions.put(qname, question);
			}
		}
		if (question == null) {
			if (lazy) {
				if (type!=null) {
					question=D3webQuestionFactory.createQuestion(qname, type, idom);
					if (question==null) {
						errors.add(MessageKnOfficeGenerator.createTypeRecognitionError(file, line, linetext, qname, type));
						condstack.push(null);
						return;
					}
				} else if (op.equals("=")) {
					question = idom.createQuestionOC(qname, idom.getKnowledgeBase().getRootQASet(), new AnswerChoice[0]);
				} else {
					question = idom.createQuestionNum(qname, idom.getKnowledgeBase().getRootQASet());
				}
			} else {
				errors.add(MessageKnOfficeGenerator.createQuestionNotFoundException(file, line, linetext, qname));
				condstack.push(null);
				return;
			}
		}
		if (!D3webQuestionFactory.checkType(question, type)) {
			errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file, line, linetext, qname, type));
		}
		TerminalCondition c;
		if (question instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) question;
			double d;
			try {
				d = Double.parseDouble(value);
			} catch (NumberFormatException e) {
				errors.add(MessageKnOfficeGenerator.createNaNException(file, line, linetext, value));
				condstack.push(null);
				return;
			}
			c = ConditionGenerator.condNum(qnum, op, d, errors, line, value, file);
		} else if (question instanceof QuestionChoice) {
			AnswerChoice answer = null;
			QuestionChoice qc = (QuestionChoice) question;
			if (qc instanceof QuestionYN) {
				QuestionYN qyn = (QuestionYN) qc;
				if ((value.equals("ja"))||(value.equals("yes"))) {
					answer = qyn.yes;
				} else if ((value.equals("nein"))||(value.equals("no"))) {
					answer = qyn.no;
				} else {
					errors.add(MessageKnOfficeGenerator.createWrongYNAnswer(file, line, linetext, qyn.getText()));
				}
			} else  {
				answer = idom.findAnswerChoice(qc, value);
			}
			if (answer==null) {
				if (lazy) {
					answer=(AnswerChoice) idom.addChoiceAnswer(qc, value);
					c = new CondEqual(qc, answer);
				} else {
					errors.add(MessageKnOfficeGenerator.createAnswerNotFoundException(file, line, linetext, value, qc.getText()));
					c = null;
				}
			} else {
				c = new CondEqual(qc, answer);
			}
		} else {
			errors.add(MessageKnOfficeGenerator.createNoAnswerAllowedException(file, line, linetext));
			c=null;
		}
		condstack.push(c);
	}
	
	@Override
	public void condition(int line, String linetext, String qname, String type, double left, double right, boolean in) {
		Question question = null;
		if (useQuestionmap) question = this.questions.get(qname);
		if(question == null) {
			question = idom.findQuestion(qname);
			if(question != null) {
				this.questions.put(qname, question);
			}
		}
		if (question == null) {
			if (lazy) {
				if (type!=null) {
					question=D3webQuestionFactory.createQuestion(qname, type, idom);
					if (question==null) {
						errors.add(MessageKnOfficeGenerator.createTypeRecognitionError(file, line, linetext, qname, type));
						condstack.push(null);
						return;
					}
				} else {
					question = idom.createQuestionNum(qname, idom.getKnowledgeBase().getRootQASet());
				}
			} else {
				errors.add(MessageKnOfficeGenerator.createQuestionNotFoundException(file, line, linetext, qname));
				condstack.push(null);
				return;
			}
		}
		if (!D3webQuestionFactory.checkType(question, type)) {
			errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file, line, linetext, qname, type));
		}
		TerminalCondition c;
		if (question instanceof QuestionNum) {
			QuestionNum qnum = (QuestionNum) question;
			c=ConditionGenerator.condNum(qnum, left, right, errors, line, linetext, file);
		} else {
			errors.add(MessageKnOfficeGenerator.createIntervallQuestionError(file, line, linetext));
			c= null;
		}
		condstack.push(c);
	}
	
	@Override
	public void knowncondition(int line, String linetext, String name, String type, boolean unknown) {
		Question q = idom.findQuestion(name);
		if (q==null) {
			if (lazy) {
				if (type!=null) {
					q=D3webQuestionFactory.createQuestion(name, type, idom);
					if (q==null) {
						errors.add(MessageKnOfficeGenerator.createTypeRecognitionError(file, line, linetext, name, type));
						condstack.push(null);
						return;
					}
				} else {
					q = idom.createQuestionOC(name, idom.getKnowledgeBase().getRootQASet(), new AnswerChoice[0]);
				}
			} else {
				errors.add(MessageKnOfficeGenerator.createQuestionNotFoundException(file, line, linetext, name));
			}
		}
		if (!D3webQuestionFactory.checkType(q, type)) {
			errors.add(MessageKnOfficeGenerator.createTypeMismatchWarning(file, line, linetext, name, type));
		}
		TerminalCondition c;
		if (unknown) {
			c=new CondUnknown(q);
		} else {
			c=new CondKnown(q);
		}
		condstack.add(c);
	}
	
	@Override
	public void notcond(String text) {
		if (!condstack.isEmpty()) {
			AbstractCondition cond = condstack.pop();
			if (cond!=null) {
				condstack.push(new CondNot(cond));
			} else {
				condstack.push(null);
			}
		}
	}
	
	@Override
	public void andcond(String text) {
		if (condstack.size()>1) {
			List<AbstractCondition> clist = new ArrayList<AbstractCondition>();
			AbstractCondition cond = condstack.pop();
			AbstractCondition cond2 = condstack.pop();
			if (cond!=null&&cond2!=null) {
				clist.add(cond);
				clist.add(cond2);
				condstack.push(new CondAnd(clist));
			} else {
				condstack.push(null);
			}
		}
	}
	
	@Override
	public void orcond(String text) {
		if (condstack.size()>1) {
			List<AbstractCondition> clist = new ArrayList<AbstractCondition>();
			AbstractCondition cond = condstack.pop();
			AbstractCondition cond2 = condstack.pop();
			if (cond!=null&&cond2!=null) {
				clist.add(cond);
				clist.add(cond2);
				condstack.push(new CondOr(clist));
			} else {
				condstack.push(null);
			}
		}
	}
	
	
	@Override
	public void minmax(int line, String linetext, int min, int max, int anzahlcond) {
		List<AbstractCondition> condlist = new ArrayList<AbstractCondition>();
		boolean failure = false;
		for (int i=0; i<anzahlcond; i++) {
			AbstractCondition cond = condstack.pop();
			condlist.add(cond);
			if (cond==null) {
				failure = true;
			}
		}
		if (!condlist.isEmpty()) {
			if (failure) {
				condstack.push(null);
			} else {
				condstack.push(new CondMofN(condlist, min, max));
			}
		} else {
			errors.add(MessageKnOfficeGenerator.createNoValidCondsException(file, line, linetext));
			condstack.push(null);
		}
	}
	
	@Override
	public void in(int line, String linetext, String question, String type, List<String> answers) {
		List<AbstractCondition> conds = condList(line, linetext, question, type,
				answers);
		if (conds.contains(null)) {
			condstack.push(null);
		} else {
			condstack.push(new CondOr(conds));
		}
	}
	
	@Override
	public void all(int line, String linetext, String question, String type, List<String> answers) {
		List<AbstractCondition> conds = condList(line, linetext, question, type,
				answers);
		if (conds.contains(null)) {
			condstack.push(null);
		} else {
			condstack.push(new CondAnd(conds));
		}
	}

	private List<AbstractCondition> condList(int line, String linetext,
			String question, String type, List<String> answers) {
		List<AbstractCondition> conds = new ArrayList<AbstractCondition>();
		for (String s: answers) {
			condition(line, linetext, question, type, "=", s);
			conds.add(condstack.pop());
		}
		return conds;
	}

	@Override
	public void complexcondition(String text) {
		// wird für d3web nicht benötigt
	}
}
