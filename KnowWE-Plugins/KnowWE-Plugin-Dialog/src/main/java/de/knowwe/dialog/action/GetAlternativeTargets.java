/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog.action;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.session.Session;
import de.d3web.core.utilities.NamedObjectComparator;
import de.d3web.costbenefit.inference.ExpertMode;
import de.d3web.costbenefit.model.Target;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * Shows all QContainers of the KB as Targets
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 09.06.2011
 */
public class GetAlternativeTargets extends AbstractAction {

	@Override
	public void execute(UserActionContext context) throws IOException {
		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
		Session session = SessionProvider.getSession(context, base);
		// ExpertMode xm = ExpertMode.getExpertMode(session);
		Locale locale = Utils.parseLocale(context.getParameter(GetInterview.PARAM_LANGUAGE));
		context.setContentType("text/xml");
		Writer writer = context.getWriter();
		writer.write("<targets>\n");
		List<QContainer> qContainers = new LinkedList<>(
				session.getKnowledgeBase().getManager().getQContainers());
		Collections.sort(qContainers, new NamedObjectComparator());
		for (QContainer qcon : qContainers) {
			// TODO Handle Multitargets
			String value = qcon.getInfoStore().getValue(
					MMInfo.PROMPT, locale);
			if (value == null) value = qcon.getInfoStore().getValue(MMInfo.PROMPT);
			if (value == null) value = qcon.getName();
			String name = Utils.encodeXML(value);
			writer.write("\t<target");
			writer.write(" name='" + qcon.getName() + ": " + name + "'");
			writer.write(" description='" + getBenefit(session, qcon) + "'");
			writer.write("></target>\n");
		}
		writer.write("</targets>\n");
	}

	private String getBenefit(Session session, QContainer qcon) {
		ExpertMode xm = ExpertMode.getExpertMode(session);
		for (Target t : xm.getAlternativeTargets()) {
			if (t.getQContainers().size() == 1 && t.getQContainers().get(0) == qcon) {
				return "Benefit: " + t.getBenefit();
			}
		}
		return "NA";
	}

}
