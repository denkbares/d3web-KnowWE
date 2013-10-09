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
package de.knowwe.diaflux.review;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.core.utils.KnowWEUtils;
import de.knowwe.core.wikiConnector.WikiAttachment;
import de.knowwe.diaflux.FlowchartUtils;
import de.knowwe.diaflux.review.Review.Priority;
import de.knowwe.diaflux.type.FlowchartType;


/**
 * Render a list of all DiaFlux reviews.
 * 
 * @author Reinhard Hatko
 * @created 06.06.2013
 */
public class ReviewListRender implements Renderer {

	public static final String ICON_PATH = "KnowWEExtension/images/";
	public static final String[] ICONS = {
			"priority_low.png", "priority_important.png", "priority_critical.png" };

	@Override
	public void render(Section<?> section, UserContext user, RenderResult string) {

		Collection<Review> reviews;
		try {
			Collection<WikiAttachment> attachments = Environment.getInstance().getWikiConnector().getAttachments();
			Collection<WikiAttachment> reviewAtts = new LinkedList<WikiAttachment>();
			for (WikiAttachment att : attachments) {
				if (att.getFileName().endsWith(LoadReviewAction.REVIEW_EXTENSION)) reviewAtts.add(att);

			}

			if (reviewAtts.isEmpty()) {
				string.append("No reviews found.");
				return;
			}

			reviews = loadReviews(reviewAtts);

		}
		catch (IOException e) {
			e.printStackTrace();
			string.append("Error retrieving attachments.");
			return;
		}


		createTable(reviews, string);

	}

	private void createTable(Collection<Review> reviews, RenderResult string) {
		string.append("%%zebra-table\n");
		string.append("%%table-filter\n");
		string.append("%%sortable\n");
		appendRow(string, "||", " ", "Flowchart", "Description", "Priority", "State");

		for (Review review : reviews) {
			appendContentRow(review, string);
			
		}

	}

	private void appendContentRow(Review review, RenderResult string) {

		String desc = review.getDescription().replaceAll("\n", " ");
		String link = getLink(review, string);
		String icon = getIcon(review, string);

		appendRow(string, "|", icon, link, KnowWEUtils.maskJSPWikiMarkup(desc),
				review.getPriority().toString(), review.isResolved() ? "Resolved" : "Open");
	}

	private String getIcon(Review review, RenderResult string) {
		Priority priority = review.getPriority();

		return RenderResult.mask("<img title='" + priority.toString() + "' src='" + ICON_PATH
				+ ICONS[priority.ordinal()] + "'>", string);
	}

	private String getLink(Review review, RenderResult string) {
		String flowName = review.getFlowName();
		Section<FlowchartType> flowSec = FlowchartUtils.findFlowchartSection("default_web",
				flowName);
		if (flowSec == null) {
			return flowName;
		}
		else {

			String urlLink = KnowWEUtils.getURLLink(flowSec.getTitle());
			String anchor = KnowWEUtils.getAnchor(flowSec);
			return RenderResult.mask("<a href='" + urlLink
					+ "&highlight=review&item=" + review.getNumber() + "#"
					+ anchor + "' >" + flowName + "</a>", string);
		}

	}

	private void appendRow(RenderResult string, String separator, String... values) {
		for (String value : values) {
			string.append(separator);
			if (value.isEmpty()) {
				string.append(" ");
			}
			else {
				string.append(value);
			}
		}
		string.append("\n");
		
		
	}

	private Collection<Review> loadReviews(Collection<WikiAttachment> reviewAtts) throws IOException {
		Collection<Review> result = new LinkedList<Review>();

		for (WikiAttachment wikiAttachment : reviewAtts) {
			result.addAll(Review.read(wikiAttachment.getInputStream()));

		}
		return result;
	}

}
