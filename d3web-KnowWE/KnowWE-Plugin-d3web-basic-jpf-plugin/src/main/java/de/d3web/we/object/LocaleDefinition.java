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
package de.d3web.we.object;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.NewObjectCreated;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;

/**
 * 
 * @author Markus Friedrich (denkbares GmbH)
 * @created 12.11.2010
 */
public class LocaleDefinition extends TermDefinition<Locale> {

	public LocaleDefinition() {
		super(Locale.class);
		this.addSubtreeHandler(new LocaleSubTreeHandler());
	}

	@Override
	public String getTermName(Section<? extends KnowWETerm<Locale>> s) {
		return s.getOriginalText().trim();
	}

	private class LocaleSubTreeHandler extends SubtreeHandler<LocaleDefinition> {

		@Override
		public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<LocaleDefinition> s) {
			Locale locale;
			// trimm first .
			String text = s.getOriginalText().substring(1);
			if (text.contains(".")) {
				String[] split = text.split("[.]");
				locale = new Locale(split[0], split[1]);
			}
			else {
				locale = new Locale(text);
			}
			s.get().storeTermObject(article, s, locale);
			return Arrays.asList((KDOMReportMessage) new NewObjectCreated("Locale: \""
					+ locale.toString() + "\" created."));
		}

	}

}
