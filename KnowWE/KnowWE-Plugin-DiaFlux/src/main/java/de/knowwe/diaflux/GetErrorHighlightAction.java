/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.diaflux;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.diaflux.type.EdgeType;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeType;
import de.knowwe.kdom.xml.AbstractXMLType;

/**
 * Highlights nodes and edges containing errors.
 * 
 * @author Reinhard Hatko
 * @created 14.01.2013
 */
public class GetErrorHighlightAction extends AbstractHighlightAction {

	private static final String PREFIX = "error";
	private static final String ERROR_CLASS = PREFIX + "Error";

	@Override
	public String getPrefix() {
		return PREFIX;
	}

	@Override
	public void insertHighlighting(Section<FlowchartType> flowchart, Highlight highlight, UserActionContext context) throws IOException {
		List<Section<NodeType>> nodes = Sections.findSuccessorsOfType(flowchart, NodeType.class);

		for (Section<NodeType> section : nodes) {
			if (section.hasErrorInSubtree()) {
				String id = AbstractXMLType.getAttributes(section).get("fcid");
				highlight.addNode(id, Highlight.CSS_CLASS, ERROR_CLASS);

				Map<de.knowwe.core.compile.Compiler, Collection<Message>> messages = Messages.getMessagesMapFromSubtree(
						section, Message.Type.ERROR);

				StringBuilder bob = new StringBuilder();
				// Show "general" errors without article first
				Collection<Message> generalMessages = messages.get(null);
				if (generalMessages != null) {
					bob.append(generalMessages);
					bob.append("\r\n");
				}

				// then article-specific ones
				for (de.knowwe.core.compile.Compiler compiler : messages.keySet()) {
					if (compiler == null) continue;
					bob.append("[");
					bob.append(compiler);
					bob.append("]: ");
					bob.append(messages.get(compiler));
					bob.append("\r\n");

				}

				highlight.addNode(id, Highlight.TOOL_TIP, bob.toString());

			}

		}
		List<Section<EdgeType>> edges = Sections.findSuccessorsOfType(flowchart, EdgeType.class);

		for (Section<EdgeType> section : edges) {
			if (section.hasErrorInSubtree()) {
				String id = AbstractXMLType.getAttributes(section).get("fcid");
				highlight.addEdge(id, Highlight.CSS_CLASS, ERROR_CLASS);

				Map<de.knowwe.core.compile.Compiler, Collection<Message>> messages = Messages.getMessagesMapFromSubtree(
						section, Message.Type.ERROR);

				StringBuilder bob = new StringBuilder();
				// Show "general" errors without article first
				Collection<Message> generalMessages = messages.get(null);
				bob.append(generalMessages);
				bob.append("\r\n");

				// then article-specific ones
				for (de.knowwe.core.compile.Compiler compiler : messages.keySet()) {
					if (compiler == null) continue;
					bob.append("[");
					bob.append(compiler);
					bob.append("]: ");
					bob.append(messages.get(compiler));
					bob.append("\r\n");

				}

				highlight.addEdge(id, Highlight.TOOL_TIP, bob.toString());

			}

		}

	}

}
