/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.we.flow.type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.objects.KnowWETerm;
import de.d3web.we.kdom.objects.TermDefinition;
import de.d3web.we.kdom.report.KDOMReportMessage;
import de.d3web.we.kdom.report.message.ObjectAlreadyDefinedError;
import de.d3web.we.kdom.subtreeHandler.SubtreeHandler;
import de.d3web.we.terminology.TerminologyHandler;
import de.d3web.we.utils.KnowWEUtils;

class FlowchartTermDefinitionRegistrationHandler extends SubtreeHandler<TermDefinition<String>> {

	@Override
	public Collection<KDOMReportMessage> create(KnowWEArticle article, Section<TermDefinition<String>> s) {

		TerminologyHandler tHandler = KnowWEUtils.getTerminologyHandler(article.getWeb());

		Section<? extends TermDefinition> before = tHandler.getTermDefiningSection(
				article, s.get().getTermName(s), KnowWETerm.LOCAL);

		tHandler.registerTermDefinition(article, s);

		Section<? extends TermDefinition> after = tHandler.getTermDefiningSection(
				article, s.get().getTermName(s), KnowWETerm.LOCAL);

		if (before == after) {
			return Arrays.asList((KDOMReportMessage) new ObjectAlreadyDefinedError(
					s.get().getName()
							+ ": " + s.get().getTermName(s), before));
		}

		s.get().storeTermObject(article, s, s.get().getTermName(s));

		return new ArrayList<KDOMReportMessage>(0);
	}

	@Override
	public void destroy(KnowWEArticle article, Section<TermDefinition<String>> s) {
		KnowWEUtils.getTerminologyHandler(article.getWeb()).unregisterTermDefinition(
				article, s);
	}

}