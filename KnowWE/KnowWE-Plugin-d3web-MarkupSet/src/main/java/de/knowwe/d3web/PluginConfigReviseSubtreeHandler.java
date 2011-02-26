/*
 * Copyright (C) 2010 denkbares GmbH
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package de.knowwe.d3web;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.core.io.progress.DummyProgressListener;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseUtils;
import de.d3web.plugin.io.PluginConfigPersistenceHandler;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

public class PluginConfigReviseSubtreeHandler extends SubtreeHandler<PluginConfigType> {

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<PluginConfigType> s) {
		String xmlText = "<settings><plugins /><psmethods>" + s.getOriginalText()
				+ "</psmethods></settings>";
		KnowledgeBaseUtils kbm = D3webModule.getKnowledgeRepresentationHandler(
				article.getWeb()).getKBM(article.getTitle());
		if (kbm == null) {
			return Arrays.asList((KDOMReportMessage) new SimpleMessageError(
					"No knowledgebase available."));
		}
		KnowledgeBase kb = kbm.getKnowledgeBase();
		try {
			new PluginConfigPersistenceHandler().read(kb,
					new ByteArrayInputStream(xmlText.getBytes()),
					new DummyProgressListener());
		}
		catch (IOException e1) {
			return Arrays.asList((KDOMReportMessage) new SimpleMessageError(e1.getMessage()));
		}

		return null;
	}

}
