package de.d3web.we.alignment.aligner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.d3web.kernel.domainModel.Answer;
import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerNum;
import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionChoice;
import de.d3web.kernel.domainModel.qasets.QuestionNum;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.we.alignment.AlignmentUtilRepository;
import de.d3web.we.alignment.LocalAlignment;
import de.d3web.we.alignment.NumericalIdentity;
import de.d3web.we.alignment.method.AlignMethod;
import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.alignment.type.NoAlignType;
import de.d3web.we.alignment.type.NumericalIdentityAlignType;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.terminology.local.LocalTerminologyAccess;
import de.d3web.we.terminology.local.LocalTerminologyHandler;
import de.d3web.we.terminology.local.LocalTerminologyStorage;

public class D3webLocalAligner implements LocalAligner<NamedObject>{

	public List<LocalAlignment> align(LocalTerminologyStorage storage, NamedObject object, String idString) {
		List<LocalAlignment> result = new ArrayList<LocalAlignment>();
		
		Collection<AlignMethod> methods = AlignmentUtilRepository.getInstance().getMethods(String.class);
		
		for (String id : storage.getIDs()) {
			for (LocalTerminologyAccess<NamedObject> termObject : storage.getTerminologies(id)) {
				if(!idString.equals(id)) {
					LocalTerminologyHandler<NamedObject, NamedObject> handler = termObject.getHandler();
					for (NamedObject no : handler) {
						for (AlignMethod method : methods) {
							AbstractAlignType type = method.align(getText(no), getText(object));
							if(!(type instanceof NoAlignType)) {
								LocalAlignment localAlignment = new LocalAlignment(
																		getII(id, no),
																		getII(idString, object),
																		type);
								Object obj1 = object.getProperties().getProperty(Property.FOREIGN);
								Object obj2 = no.getProperties().getProperty(Property.FOREIGN);
								if((obj1 != null && obj1 instanceof Boolean && ((Boolean)obj1).booleanValue())
										||(obj2 != null && obj2 instanceof Boolean && ((Boolean)obj2).booleanValue())){
									localAlignment.setProperty("visible", Boolean.FALSE);
								}
								result.add(localAlignment);
								
								if(object instanceof Question && no instanceof Question) {
									Question q1 = (Question) object;
									Question q2 = (Question) no;
									LocalAlignment localAlignmentMaU = new LocalAlignment(
																				getII(id, q1.getUnknownAlternative()),
																				getII(idString, q2.getUnknownAlternative()),
																				type);
									if((obj1 != null && obj1 instanceof Boolean && ((Boolean)obj1).booleanValue())
											||(obj2 != null && obj2 instanceof Boolean && ((Boolean)obj2).booleanValue())){
										localAlignmentMaU.setProperty("visible", Boolean.FALSE);
									}
									result.add(localAlignmentMaU);
								}
								
								if(object instanceof QuestionNum && no instanceof QuestionNum) {
									result.addAll(alignValues((QuestionNum)object, (QuestionNum)no, idString, id));
								} else if(object instanceof QuestionChoice && no instanceof QuestionChoice) {
									result.addAll(alignValues((QuestionChoice)object, (QuestionChoice)no, idString, id));
								}
							}
						}
					}
				}
			}
		}
		Collections.sort(result);
		return result;
	}

	

	private List<LocalAlignment> alignValues(QuestionNum object, QuestionNum no, String idString, String id) {
		List<LocalAlignment> result = new ArrayList<LocalAlignment>();
		LocalAlignment localAlignment = new LocalAlignment(
						getII(id, no, new NumericalIdentity()),
						getII(idString, object, new NumericalIdentity()),
						NumericalIdentityAlignType.getInstance());
		Object obj1 = object.getProperties().getProperty(Property.FOREIGN);
		Object obj2 = no.getProperties().getProperty(Property.FOREIGN);
		if((obj1 != null && obj1 instanceof Boolean && ((Boolean)obj1).booleanValue())
				||(obj2 != null && obj2 instanceof Boolean && ((Boolean)obj2).booleanValue())){
			localAlignment.setProperty("visible", Boolean.FALSE);
		}
		result.add(localAlignment);
		return result;
	}

	private List<LocalAlignment> alignValues(QuestionChoice first, QuestionChoice second, String firstId, String secondId) {
		List<LocalAlignment> result = new ArrayList<LocalAlignment>();
		
		LocalTerminologyHandler<IDObject, IDObject> answer1Handler = AlignmentUtilRepository.getInstance().getLocalTerminogyHandler(IDObject.class);
		answer1Handler.setTerminology(first);
		LocalTerminologyHandler<IDObject, IDObject> answer2Handler = AlignmentUtilRepository.getInstance().getLocalTerminogyHandler(IDObject.class);
		answer2Handler.setTerminology(second);
		
		Collection<AlignMethod> methods = AlignmentUtilRepository.getInstance().getMethods(String.class);
		
		for (IDObject a1 : answer1Handler) {
			for (IDObject a2 : answer2Handler) {
				for (AlignMethod method : methods) {
					AbstractAlignType type = method.align(getText(a1), getText(a2));
					if(!(type instanceof NoAlignType)) {
						LocalAlignment localAlignment = new LocalAlignment(
														getII(firstId, a1),
														getII(secondId, a2),
														type);
						if(a1 instanceof Answer && a2 instanceof Answer) {
							Object obj1 = ((Answer)a1).getProperties().getProperty(Property.FOREIGN);
							Object obj2 = ((Answer)a2).getProperties().getProperty(Property.FOREIGN);
							if((obj1 != null && obj1 instanceof Boolean && ((Boolean)obj1).booleanValue())
									||(obj2 != null && obj2 instanceof Boolean && ((Boolean)obj2).booleanValue())){
								localAlignment.setProperty("visible", Boolean.FALSE);
							}
						}
						result.add(localAlignment);
					}
				}
			}
		}
		return result;
	}

	private Object getText(IDObject ido) {
		if(ido instanceof NamedObject) {
			return ((NamedObject)ido).getText();
		} else if(ido instanceof AnswerChoice) {
			return ((AnswerChoice)ido).getText();
		} else if(ido instanceof AnswerNum) {
			return ((AnswerNum)ido).getId();
		}
		return "";
	}

	private IdentifiableInstance getII(String idString, IDObject object) {
		if(object instanceof NamedObject) {
			return new IdentifiableInstance(idString, object.getId(), null);
		} else if(object instanceof Answer) {
			Answer answer = (Answer) object;
			return new IdentifiableInstance(idString, answer.getQuestion().getId(), answer.getId());
		}
		return null;
	}
	
	

	private IdentifiableInstance getII(String idString, QuestionNum object, NumericalIdentity ni) {
		return new IdentifiableInstance(idString, object.getId(), ni);
	}
	
}
