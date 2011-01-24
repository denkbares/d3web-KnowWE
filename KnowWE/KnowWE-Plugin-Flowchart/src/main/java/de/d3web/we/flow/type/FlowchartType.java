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

package de.d3web.we.flow.type;

import de.d3web.we.flow.FlowchartRenderer;
import de.d3web.we.flow.FlowchartSubTreeHandler;
import de.d3web.we.flow.type.FlowchartXMLHeadType.FlowchartTermDef;
import de.d3web.we.kdom.InvalidKDOMSchemaModificationOperation;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Priority;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.IncrementalMarker;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.subtreeHandler.IncrementalConstraint;
import de.d3web.we.kdom.xml.AbstractXMLObjectType;
import de.d3web.we.kdom.xml.XMLHead;
import de.d3web.we.utils.KnowWEUtils;

/**
 * @author Reinhard Hatko
 * @created on: 09.10.2009
 */
public class FlowchartType extends AbstractXMLObjectType implements IncrementalMarker, IncrementalConstraint<FlowchartType> {

	protected KnowWEDomRenderer<FlowchartType> renderer = new FlowchartRenderer();

	public FlowchartType() {
		super("flowchart");
		replaceHead(); // can not be done in init, because object
		// construction
		// is not yet finished
	}

	@Override
	protected void init() {
		this.childrenTypes.add(FlowchartContentType.getInstance());
		addSubtreeHandler(Priority.DEFAULT, new FlowchartSubTreeHandler());

	}

	/**
	 * 
	 * @created 08.12.2010
	 */
	public void replaceHead() {
		try {
			this.replaceChildType(new FlowchartXMLHeadType(), XMLHead.class);
		}
		catch (InvalidKDOMSchemaModificationOperation e) {
			e.printStackTrace();
		}
	}

	@Override
	public KnowWEDomRenderer<FlowchartType> getRenderer() {
		return renderer;
	}

	public static String getFlowchartName(Section<FlowchartType> sec) {
		return sec.findSuccessor(FlowchartTermDef.class).getOriginalText();
	}

	@Override
	public boolean violatedConstraints(KnowWEArticle article, Section<FlowchartType> s) {
		return KnowWEUtils.getTerminologyHandler(
				article.getWeb()).areTermDefinitionsModifiedFor(article);
	}

}
