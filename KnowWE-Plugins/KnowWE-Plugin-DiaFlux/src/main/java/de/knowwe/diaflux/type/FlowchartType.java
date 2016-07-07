/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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

package de.knowwe.diaflux.type;

import java.util.Map;

import de.d3web.core.session.SessionFactory;
import de.d3web.strings.Identifier;
import de.d3web.strings.Strings;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.MessageRenderer;
import de.knowwe.diaflux.DiaFluxTrace;
import de.knowwe.diaflux.DiaFluxValueTrace;
import de.knowwe.diaflux.FlowchartRenderer;
import de.knowwe.diaflux.FlowchartSubTreeHandler;
import de.knowwe.diaflux.type.FlowchartXMLHeadType.FlowchartTermDef;
import de.knowwe.kdom.xml.AbstractXMLType;
import de.knowwe.kdom.xml.XMLHead;

/**
 * @author Reinhard Hatko
 * @created on: 09.10.2009
 */
public class FlowchartType extends AbstractXMLType {

	public FlowchartType() {
		super("flowchart");
		this.addChildType(FlowchartContentType.getInstance());
		addCompileScript(Priority.LOW, new FlowchartSubTreeHandler());
		addCompileScript(Priority.LOWER, new NoAutostartFlowWarningScript());
		replaceHead();
		setRenderer(new FlowchartRenderer());
		// enable tracing
		SessionFactory.addPropagationListener(DiaFluxTrace.LISTENER);
		SessionFactory.addPropagationListener(DiaFluxValueTrace.LISTENER);
	}

	@Override
	public MessageRenderer getMessageRenderer(Message.Type type) {
		return null;
	}

	public void replaceHead() {
		this.replaceChildType(XMLHead.class, new FlowchartXMLHeadType());
	}

	public static String getFlowchartName(Section<FlowchartType> sec) {
		Section<FlowchartTermDef> definition = Sections.successor(sec, FlowchartTermDef.class);
		if (definition == null) return "Unnamed Flowchart";
		return Strings.decodeHtml(definition.getText());
	}

	public static Identifier getFlowchartTermIdentifier(Section<FlowchartType> sec) {
		Section<FlowchartTermDef> definition = Sections.successor(sec, FlowchartTermDef.class);
		if (definition == null) return null;
		return definition.get().getTermIdentifier(definition);
	}

	public static boolean isAutoStart(Section<FlowchartType> sec) {
		Map<String, String> attributeMap = AbstractXMLType.getAttributes(sec);
		return Boolean.parseBoolean(attributeMap.get("autostart"));
	}

	public static String getIcon(Section<FlowchartType> sec) {
		return AbstractXMLType.getAttributes(sec).get("icon");
	}

}
