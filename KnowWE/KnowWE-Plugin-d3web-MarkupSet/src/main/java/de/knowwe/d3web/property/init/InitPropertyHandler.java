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

package de.knowwe.d3web.property.init;

import java.util.Collection;

import de.d3web.core.inference.PSMethodInit;
import de.d3web.core.knowledge.terminology.NamedObject;
import de.d3web.core.knowledge.terminology.Question;
import de.d3web.core.knowledge.terminology.info.BasicProperties;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.we.reviseHandler.D3webSubtreeHandler;
import de.d3web.we.utils.MessageUtils;
import de.knowwe.core.kdom.KnowWEArticle;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.report.KDOMReportMessage;
import de.knowwe.core.report.SimpleMessageError;
import de.knowwe.d3web.property.NamedObjectReference;
import de.knowwe.d3web.property.PropertyContentType;
import de.knowwe.d3web.property.PropertyDeclarationType;
import de.knowwe.d3web.property.PropertyType;

/**
 * Further checks some constraints with init properties to add proper error
 * messages.
 * 
 * @author Albrecht Striffler
 * @created 20.09.2011
 */
public class InitPropertyHandler extends D3webSubtreeHandler<PropertyDeclarationType> {

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<PropertyDeclarationType> s) {
		// get Property
		Section<PropertyType> propertySection = Sections.findSuccessor(s,
				PropertyType.class);
		if (propertySection == null) {
			return MessageUtils.asList();
		}
		Property<?> property = propertySection.get().getProperty(propertySection);
		if (property == null || !property.equals(BasicProperties.INIT)) {
			return MessageUtils.asList();
		}

		// get NamedObject
		Section<NamedObjectReference> namendObjectSection = Sections.findSuccessor(s,
				NamedObjectReference.class);
		if (namendObjectSection == null) {
			return MessageUtils.asList();
		}
		NamedObject object = namendObjectSection.get().getTermObject(article, namendObjectSection);
		if (object == null || !(object instanceof Question)) {
			return MessageUtils.asList();
		}

		// get content
		Section<PropertyContentType> contentSection = Sections.findSuccessor(s,
				PropertyContentType.class);
		if (contentSection == null) {
			return MessageUtils.asList();
		}
		String content = contentSection.get().getPropertyContent(contentSection);
		if (content == null || content.trim().isEmpty()) {
			return MessageUtils.asList();
		}

		try {
			PSMethodInit.getValue((Question) object, content);
		}
		catch (Exception e) {
			return MessageUtils.asList(new SimpleMessageError(e.getMessage()));
		}
		return MessageUtils.asList();
	}

}
