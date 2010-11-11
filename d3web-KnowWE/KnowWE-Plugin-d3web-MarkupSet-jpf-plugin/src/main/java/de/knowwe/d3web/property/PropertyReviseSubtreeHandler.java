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
package de.knowwe.d3web.property;

import java.util.Collection;
import java.util.Locale;

import de.d3web.core.knowledge.terminology.IDObject;
import de.d3web.core.knowledge.terminology.info.Property;
import de.d3web.core.manage.KnowledgeBaseManagement;
import de.d3web.we.basic.D3webModule;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * Parses a line defining a property
 * 
 * Syntax of the line: IDOBjectName.Property(.Language(.Country)?)? = content
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 10.11.2010
 */
public class PropertyReviseSubtreeHandler extends SubtreeHandler<PropertyType> {

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<PropertyType> s) {
		KnowledgeBaseManagement kbm = D3webModule.getKnowledgeRepresentationHandler(
				article.getWeb()).getKBM(article.getTitle());
		String[] split = s.getOriginalText().split("=");
		if (split.length == 0) return null;
		String[] split2 = split[0].split("[.]");
		if (split2.length == 0) return null;
		IDObject object = kbm.findIDObjectByName(split2[0].trim());
		if (object == null) return null;
		Property<Object> property = Property.getUntypedProperty(split2[1].trim());
		String content = split[1].trim();
		if (split2.length > 2) {
			Locale locale;
			String language = split2[2].trim();
			if (split2.length == 3) {
				locale = new Locale(language);
			}
			else {
				locale = new Locale(language, split2[3].trim());
			}
			object.getInfoStore().addValue(property, locale, content);
		}
		else {
			object.getInfoStore().addValue(property, content);
		}
		return null;
	}
}
