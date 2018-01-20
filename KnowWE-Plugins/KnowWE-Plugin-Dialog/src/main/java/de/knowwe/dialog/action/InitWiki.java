/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.action;

import java.io.IOException;
import java.util.Set;

import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.dialog.SessionConstants;
import de.knowwe.dialog.Utils;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.knowledge.Resource;
import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.we.utils.D3webUtils;
import de.knowwe.core.Attributes;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;

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
			if (packageManager == null) {
				throw new IOException("Package manager not found");
			}
			Set<Section<? extends PackageCompileType>> compileSections = packageManager.getCompileSections(packageName);
			if (compileSections.isEmpty()) {
				throw new IOException("No knowledge base found for package '" + packageName + "'");
			}
			sectionId = compileSections.iterator().next().getID();
		}

		// using the StartCase command
		StartCase cmd = (StartCase) Utils.getAction(StartCase.class.getSimpleName());
		WikiProvider provider = new WikiProvider(sectionId);
		StartCase.KnowledgeBaseProvider[] providers = new StartCase.KnowledgeBaseProvider[1];
		providers[0] = provider;
		context.getSession().setAttribute(
				SessionConstants.ATTRIBUTE_AVAILABLE_KNOWLEDGE_BASE_PROVIDERS,
				providers);
		cmd.startCase(context, provider);
	}

}
