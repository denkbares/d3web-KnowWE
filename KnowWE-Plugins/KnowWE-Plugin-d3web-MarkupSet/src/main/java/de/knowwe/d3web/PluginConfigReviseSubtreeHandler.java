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
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import com.denkbares.progress.DummyProgressListener;
import de.d3web.core.io.PersistenceManager;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.plugin.io.PluginConfigPersistenceHandler;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;

public class PluginConfigReviseSubtreeHandler implements D3webHandler<PluginConfigType> {

	@Override
	public Collection<Message> create(D3webCompiler compiler, Section<PluginConfigType> s) {
		KnowledgeBase kb = getKnowledgeBase(compiler);
		if (kb == null) {
			return Messages.asList(Messages.error("No knowledgebase available."));
		}

		// parse the xml file as ps-methods configuration snipplet
		try {
			String xmlText = "<settings><plugins /><psmethods>" + s.getText() + "</psmethods></settings>";
			new PluginConfigPersistenceHandler().read(PersistenceManager.getInstance(), kb,
					new ByteArrayInputStream(xmlText.getBytes(StandardCharsets.UTF_8)),
					new DummyProgressListener());
		}
		catch (IOException e) {
			return Messages.asList(Messages.error(e.getMessage()));
		}

		return null;
	}
}
