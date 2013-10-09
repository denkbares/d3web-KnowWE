/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.diaflux.review;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.d3web.testing.AbstractTest;
import de.d3web.testing.Message;
import de.d3web.testing.Message.Type;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.Article;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.knowwe.diaflux.type.DiaFluxType;

/**
 * This test checks, if a flow is present for each review file. As reviews are
 * assigned by name, reviews can be 'lost', when renaming flows. In future
 * versions this should be solved by having events, when flows are renamed.
 * 
 * @author Reinhard Hatko
 * @created 27.05.2013
 */
public class ReviewConsistencyTest extends AbstractTest<Article> {

	@Override
	public Message execute(Article testObject, String[] args, String[]... ignores) throws InterruptedException {
		WikiConnector connector = Environment.getInstance().getWikiConnector();
		Collection<String> missingFlows = new LinkedList<String>();
		try {
			List<WikiAttachment> list = connector.getAttachments(testObject.getTitle());
			for (WikiAttachment attachment : list) {
				String fileName = attachment.getFileName();
				if (fileName.endsWith(LoadReviewAction.REVIEW_EXTENSION)) {
					String flowName = fileName.substring(0, fileName.length()
							- LoadReviewAction.REVIEW_EXTENSION.length());

					if (findFlow(testObject, flowName) == null) {
						missingFlows.add(flowName);

					}

				}
			}
		}
		catch (IOException e) {
			return new Message(Type.ERROR, "Error while checking attachments of '"
					+ testObject.getTitle() + "':\n" + e.getMessage());
		}
		if (missingFlows.isEmpty()) {
			return new Message(Type.SUCCESS);
		}
		else {
			return new Message(Type.FAILURE, "No flowcharts found named: " + missingFlows);
		}

	}

	/**
	 * Tries to find a flow with the given name in the supplied article.
	 * 
	 * @created 27.05.2013
	 * @param testObject
	 * @param flowName
	 * @return
	 */
	private Section<DiaFluxType> findFlow(Article testObject, String flowName) {
		List<Section<DiaFluxType>> flows = Sections.findSuccessorsOfType(
				testObject.getRootSection(), DiaFluxType.class);
		for (Section<DiaFluxType> flow : flows) {
			if (DiaFluxType.getFlowchartName(flow).equals(flowName)) return flow;
		}
		return null;
	}

	@Override
	public Class<Article> getTestObjectClass() {
		return Article.class;
	}

	@Override
	public String getDescription() {
		return "This test checks, if a flow is present for each review file. As reviews are assigned by name, reviews can be lost, when renaming flows.";
	}


}
