/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog.action;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.knowwe.dialog.KeepAlive;
import de.knowwe.dialog.SessionConstants;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.QContainer;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.session.Session;
import de.d3web.core.utilities.NamedObjectComparator;
import de.d3web.costbenefit.inference.AbortException;
import de.d3web.costbenefit.inference.ExpertMode;
import de.d3web.we.basic.SessionProvider;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;

/**
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 09.06.2011
 */
public class SelectAlternativeTarget extends AbstractAction {

	public static final String PARAM_INDEX = "index";

	@Override
	public void execute(UserActionContext context) throws IOException {
		int index = Integer.parseInt(context.getParameter(PARAM_INDEX));
		KnowledgeBase base = (KnowledgeBase) context.getSession().getAttribute(
				SessionConstants.ATTRIBUTE_KNOWLEDGE_BASE);
		Session session = SessionProvider.getSession(context, base);
		List<QContainer> qContainers = new LinkedList<>(
				session.getKnowledgeBase().getManager().getQContainers());
		Collections.sort(qContainers, new NamedObjectComparator());
		QContainer qcon = qContainers.get(index);
		ExpertMode xm = ExpertMode.getExpertMode(session);
		KeepAlive heartbeat = new KeepAlive(context.getOutputStream());
		heartbeat.start();
		try {
			xm.selectTarget(qcon);
		}
		catch (AbortException e) {
			e.printStackTrace();
		}
		finally {
			heartbeat.terminate();
		}

		String language = context.getParameter(StartCase.PARAM_LANGUAGE);
		context.sendRedirect("Resource/ui.zip/html/index.html?" +
				StartCase.PARAM_LANGUAGE + "=" + language);

	}

	public static final class EnglishPromptComparator implements Comparator<NamedObject> {

		@Override
		public int compare(NamedObject r1, NamedObject r2) {
			String name1 = getName(r1);
			String name2 = getName(r2);
			return name1.compareTo(name2);
		}

		private String getName(NamedObject object) {
			String name = object.getInfoStore().getValue(MMInfo.PROMPT, Locale.ENGLISH);
			if (name == null) {
				name = object.getInfoStore().getValue(MMInfo.PROMPT);
				if (name == null) {
					name = object.getName();
				}
			}
			return name;
		}
	}

}
