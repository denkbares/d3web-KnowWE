package de.d3web.we.action;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import de.d3web.kernel.domainModel.qasets.Question;
import de.d3web.kernel.domainModel.qasets.QuestionMC;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.basic.Information;
import de.d3web.we.basic.InformationType;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.broker.Broker;
import de.d3web.we.core.knowledgeService.D3webKnowledgeServiceSession;
import de.d3web.we.core.knowledgeService.KnowledgeServiceSession;
import de.d3web.we.d3webModule.D3webUtils;
import de.d3web.we.d3webModule.DPSEnvironmentManager;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.terminology.term.Term;

public class SetSingleFindingAction implements KnowWEAction {

	@SuppressWarnings("deprecation")
	@Override
	public String perform(KnowWEParameterMap parameterMap) {
		String namespace = java.net.URLDecoder.decode(parameterMap
				.get(KnowWEAttributes.SEMANO_NAMESPACE));
		String objectid = parameterMap.get(KnowWEAttributes.SEMANO_OBJECT_ID);
		String termName = URLDecoder.decode(parameterMap
				.get(KnowWEAttributes.SEMANO_TERM_NAME));
		String valueid = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_ID);
		String valuenum = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_NUM);
		String valueids = parameterMap.get(KnowWEAttributes.SEMANO_VALUE_IDS);
		String user = parameterMap.get(KnowWEAttributes.USER);
		String web = parameterMap.get(KnowWEAttributes.WEB);

		
		
		DPSEnvironment env = DPSEnvironmentManager.getInstance()
				.getEnvironments(web);
		Broker broker = env.getBroker(user);

		if (namespace == null || objectid == null) {
			return "null";
		}
		// Object value = null;
		// List<Object> values = new ArrayList<Object>();
		// if(valueid != null) {
		// value = valueid;
		// } else if(valuenum != null) {
		// value = Double.valueOf(valuenum);
		// }
		// if(value != null) {
		// values.add(value);
		// }
		// if(valueids != null) {
		// String[] ids = valueids.split("\\,");
		// for (String string : ids) {
		// values.add(string.trim());
		// }
		// }
		
		
		
		
		Term term = null;
		term = SemanticAnnotationRenderer.getTerm(env, termName);
		
		//workaround TODO refactor
		if(term == null) {
			KnowledgeServiceSession kss = broker.getSession()
			.getServiceSession(namespace);
			if (kss instanceof D3webKnowledgeServiceSession) {
				Question q = ((D3webKnowledgeServiceSession)kss).getBaseManagement().getKnowledgeBase().searchQuestions(objectid);
				if(q != null) {
					termName = q.getText();
					term = SemanticAnnotationRenderer.getTerm(env, termName);
				}
			}
		}

		IdentifiableInstance ii = null;
		if (term != null) {
			ii = SemanticAnnotationRenderer.getII(env, namespace, term);
		}
		if (ii == null) {
			return "Question not found in KB: " + termName;
		}

		List<String> answers = new ArrayList<String>();

		String qid = null;

		KnowledgeServiceSession kss = broker.getSession().getServiceSession(
				namespace);
		
		List<Information> userInfos = broker.getSession().getBlackboard()
				.getOriginalUserInformation();
		for (Information information : userInfos) {
			IdentifiableInstance iio = information
					.getIdentifiableObjectInstance();
			if (!iio.equals(ii))
				continue;
			qid = iio.getObjectId();
			Collection<IdentifiableInstance> iivs = information
					.getIdentifiableValueInstances();
			if (iivs.isEmpty())
				break;
			Iterator<IdentifiableInstance> iter = iivs.iterator();

			while (iter.hasNext()) {
				IdentifiableInstance iiv = iter.next();
				Object val = iiv.getValue();
				if (val instanceof String) {
					answers.add((String) val);
				}
			}
		}

		boolean contains = false;
		boolean mc = (D3webUtils.getQuestion(kss, qid) instanceof QuestionMC);
		for (String a : answers) {
			if (a.equals(valueid)) {
				contains = true;
			}
		}
		List<Object> valuesAfterClick = new ArrayList<Object>();
		if (mc) {
			if (!contains) {
				for (String a : answers) {
					valuesAfterClick.add(a);
				}
				valuesAfterClick.add(valueid.trim());
			} else {
				for (String a : answers) {
					if (!a.equals(valueid))
						valuesAfterClick.add(a);
				}
			}

		} else {
			if (valuenum != null) {
				valuesAfterClick.add(Double.valueOf(valuenum));
			} else {
				valuesAfterClick.add(valueid.trim());
			}
		}

		Information info = new Information(namespace, objectid,
				valuesAfterClick, TerminologyType.symptom,
				InformationType.OriginalUserInformation);
		kss.inform(info);
		broker.update(info);

		return "value set";
	}

}
