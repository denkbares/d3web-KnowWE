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

package de.d3web.we.kdom.table.attributes;

import java.io.StringReader;
import java.util.Collection;

import de.d3web.KnOfficeParser.SingleKBMIDObjectManager;
import de.d3web.KnOfficeParser.txttable.TxtAttributeTableBuilder;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.table.TableContentRenderer;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.terminology.D3webSubtreeHandler;

public class AttributeTableContent extends XMLContent {

	@Override
	protected void init() {
		TxtAttributeTableBuilder builder = new TxtAttributeTableBuilder();
		this.addSubtreeHandler(new AttributeTableContentSubTreeHandler(builder));
		this.childrenTypes.add(new AttributeTableLine(builder));
		this.setCustomRenderer(new TxtAttributeTableContentRenderer());
	}

	private class AttributeTableContentSubTreeHandler extends D3webSubtreeHandler {

		private final TxtAttributeTableBuilder builder;

		public AttributeTableContentSubTreeHandler(
				TxtAttributeTableBuilder builder) {
			this.builder = builder;
		}

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section s) {

			KnowledgeBaseManagement kbm = getKBM(article);

			if (kbm != null) {
				AbstractKnowWEObjectType.storeMessages(article, s.getFather(), this.getClass(),
						builder.addKnowledge(new StringReader(s.getOriginalText()),
								new SingleKBMIDObjectManager(kbm), null));
			}
			else {
				AbstractKnowWEObjectType.storeMessages(article, s.getFather(), this.getClass(),
						builder.addKnowledge(new StringReader(s.getOriginalText()),
								new SingleKBMIDObjectManager(kbm), null));
			}
			return null;
		}
	}

	protected class TxtAttributeTableContentRenderer extends TableContentRenderer {

		@Override
		protected String getHeader() {
			return "<tr><th>IDObject</th><th>MMInfoSubject</th><th>DCElement [Lang] Title</th><th>Data</th></tr>";
		}

	}

}
