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
import java.util.Collection;

import de.d3web.core.io.progress.DummyProgressListener;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.plugin.io.PluginConfigPersistenceHandler;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

public class PluginConfigReviseSubtreeHandler extends D3webSubtreeHandler<PluginConfigType> {

	@Override
	public Collection<Message> create(KnowWEArticle article, Section<PluginConfigType> s) {
		String xmlText = "<settings><plugins /><psmethods>" + s.getText()
				+ "</psmethods></settings>";
		KnowledgeBase kb = getKB(article);
		if (kb == null) {
			return Messages.asList(Messages.error(
					"No knowledgebase available."));
		}
		try {
			new PluginConfigPersistenceHandler().read(kb,
					new ByteArrayInputStream(xmlText.getBytes()),
					new DummyProgressListener());
		}
		catch (IOException e1) {
			return Messages.asList(Messages.error(e1.getMessage()));
		}

		return null;
	}

}
