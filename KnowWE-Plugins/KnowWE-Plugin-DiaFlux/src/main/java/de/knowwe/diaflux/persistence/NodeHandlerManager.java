/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.knowwe.diaflux.persistence;

import java.util.ArrayList;
import java.util.List;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.diaFlux.flow.Node;
import com.denkbares.plugin.Extension;
import com.denkbares.plugin.PluginManager;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.diaflux.type.FlowchartType;
import de.knowwe.diaflux.type.NodeType;

/**
 * 
 * @author Reinhard Hatko
 * @created 16.11.2010
 */
public class NodeHandlerManager implements NodeHandler {

	public static String NODEHANDLER_EXTENSIONPOINT = "NodeHandler";
	public static final String EXTENDED_PLUGIN_ID = "KnowWE-Plugin-DiaFlux";

	private static final List<NodeHandler> HANDLERS;
	private static final NodeHandlerManager INSTANCE;

	static {

		INSTANCE = new NodeHandlerManager();

		HANDLERS = new ArrayList<>();

		HANDLERS.add(new ActionNodeHandler());
		HANDLERS.add(new StartNodeHandler());
		HANDLERS.add(new ExitNodeHandler());
		HANDLERS.add(new CommentNodeHandler());
		HANDLERS.add(new ComposedNodeHandler());
		HANDLERS.add(new SnapshotNodeHandler());
		HANDLERS.add(new DecisionNodeHandler());

		Extension[] extensions = PluginManager.getInstance().getExtensions(EXTENDED_PLUGIN_ID,
				NODEHANDLER_EXTENSIONPOINT);

		for (Extension e : extensions) {
			HANDLERS.add((NodeHandler) e.getSingleton());
		}

	}

	private NodeHandlerManager() {
	}

	public static NodeHandlerManager getInstance() {
		return INSTANCE;
	}

	@Override
	public boolean canCreateNode(D3webCompiler compiler, KnowledgeBase kb, Section<NodeType> nodeSection) {
		for (NodeHandler handler : HANDLERS) {
			if (handler.canCreateNode(compiler, kb, nodeSection)) return true;
		}

		return false;
	}

	@Override
	public Node createNode(D3webCompiler compiler, KnowledgeBase kb, Section<NodeType> nodeSection,
			Section<FlowchartType> flowSection, String id) {
		NodeHandler nodeHandler = findNodeHandler(compiler, kb, nodeSection);

		if (nodeHandler == null) return null;

		return nodeHandler.createNode(compiler, kb, nodeSection, flowSection, id);
	}

	public NodeHandler findNodeHandler(D3webCompiler compiler,
			KnowledgeBase kb, Section<NodeType> nodeSection) {

		for (NodeHandler handler : HANDLERS) {
			if (handler.canCreateNode(compiler, kb, nodeSection)) return handler;
		}

		return null;
	}

	@Override
	public Type get() {
		return null;
	}

}
