/*
 * Copyright (C) 2010 denkbares GmbH
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.knowwe.d3web;

import java.io.IOException;

import org.apache.tools.ant.filters.StringInputStream;

import de.d3web.core.io.progress.DummyProgressListener;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.plugin.io.PluginConfigPersistenceHandler;
import de.d3web.report.Message;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.ReviseSubTreeHandler;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.defaultMarkup.DefaultMarkupType;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.SimpleMessageError;

public class PluginConfigReviseSubtreeHandler implements ReviseSubTreeHandler {

	@SuppressWarnings("unchecked")
	@Override
	public KDOMReportMessage reviseSubtree(KnowWEArticle article, Section s) {
		String xmlText = "<settings><plugins /><psmethods>"+s.getOriginalText()+"</psmethods></settings>";
		KnowledgeBase kb = D3webModule.getKnowledgeRepresentationHandler(article.getWeb()).getKBM(article, s).getKnowledgeBase();
		try {
			new PluginConfigPersistenceHandler().read(kb, new StringInputStream(xmlText), new DummyProgressListener());
		}
		catch (IOException e1) {
			DefaultMarkupType.addErrorMessage(s, new Message(Message.ERROR, e1.getMessage(), null, -1, null));
			return null;
		}
		
		return null;
	}

}
