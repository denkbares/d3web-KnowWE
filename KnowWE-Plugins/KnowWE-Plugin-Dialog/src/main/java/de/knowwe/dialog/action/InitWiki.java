/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.IOException;
import java.util.Set;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;

public class InitWiki extends AbstractAction {

	public static final String PARAM_USER = StartCase.PARAM_USER;
	public static final String PARAM_LANGUAGE = StartCase.PARAM_LANGUAGE;

	protected static class WikiProvider implements StartCase.KnowledgeBaseProvider {

		private final String sectionId;

		public WikiProvider(String sectionId) {
			this.sectionId = sectionId;
		}

		@Override
		public KnowledgeBase getKnowledgeBase() throws IOException {
			Section<?> section = Sections.get(sectionId);
			if (section == null) {
				throw new IOException(
						"no section with id '" + sectionId + "' found");
			}
			KnowledgeBase base = D3webUtils.getKnowledgeBase(section);

			if (base == null) {
				throw new IOException(
						"the specified wiki article does not contain any knowledge base");
			}
			return base;
		}

		@Override
		public String getName() throws IOException {
			return getKnowledgeBase().getName();
		}

		@Override
		public String getDescription() throws IOException {
			return getKnowledgeBase().getInfoStore().getValue(MMInfo.DESCRIPTION);
		}

		@Override
		public Resource getFavIcon() throws IOException {
			return getKnowledgeBase().getResource("favicon.png");
		}

		public String getSectionId() {
			return this.sectionId;
		}
	}

	@Override
	public void execute(UserActionContext context) throws IOException {
		// remember user name
		String sectionId = context.getParameter(Attributes.SECTION_ID);
		if (sectionId == null) {
			String packageName = context.getParameter(Attributes.PACKAGE);
			PackageManager packageManager = KnowWEUtils.getPackageManager(context.getArticleManager());
			Set<Section<? extends PackageCompileType>> compileSections = packageManager.getCompileSections(packageName);
			if (compileSections.isEmpty()) {
				throw new IOException("No knowledge base found for package '" + packageName + "'");
			}
			sectionId = compileSections.iterator().next().getID();
		}

		// using the StartCase command
		context.getSession().setAttribute(SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS,
				new StartCase.KnowledgeBaseProvider[] { new WikiProvider(sectionId) });
		StartCase cmd = (StartCase) Utils.getAction(StartCase.class.getSimpleName());
		if (cmd != null) cmd.startCase(context, new WikiProvider(sectionId), null);
	}
}
