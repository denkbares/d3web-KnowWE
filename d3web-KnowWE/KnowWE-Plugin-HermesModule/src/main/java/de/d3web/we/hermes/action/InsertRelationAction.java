package de.d3web.we.hermes.action;

import de.d3web.we.action.DeprecatedAbstractKnowWEAction;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.hermes.kdom.TimeEventDescriptionType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class InsertRelationAction extends DeprecatedAbstractKnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		KnowWEArticleManager articleManager = KnowWEEnvironment.getInstance().getArticleManager(
				KnowWEEnvironment.DEFAULT_WEB);
		String topic = parameterMap.getTopic();
		KnowWEArticle art = articleManager.getArticle(
				topic);
		Section event = art.getSection().findChild(parameterMap.get("kdomid"));

		if (event != null) {
			Section description = KnowWEObjectTypeUtils.getAncestorOfType(
					event, TimeEventDescriptionType.class);
			if (description != null) {
				String property = parameterMap.get("property");
				String object = parameterMap.get("object");
				if (property != null && object != null) {

					StringBuffer insertion = new StringBuffer();
					insertion.append("[");
					insertion.append(property);
					insertion.append("::");
					insertion.append(object);
					insertion.append("]");
					
					if(!description.getOriginalText().contains(insertion.toString())) {
						articleManager.replaceKDOMNode(parameterMap, topic, description.getId(), description.getOriginalText()+" - "+insertion.toString());
					}
					return "done";
				}
			}
		}

		return "false";
	}

}
