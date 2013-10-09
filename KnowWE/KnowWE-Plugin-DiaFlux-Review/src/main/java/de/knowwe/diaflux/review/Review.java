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
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.d3web.core.io.utilities.XMLUtil;

/**
 * Data class to store the most important attributes of a review.
 * 
 * @author Reinhard Hatko
 * @created 06.06.2013
 */
public class Review {

	public enum Priority {
		low, important, critical
	}

	private final int number;
	private final String flowName;
	private final String description;
	private final Priority priority;
	private final boolean resolved;

	private final String article;

	/**
	 * @param number
	 * @param flowName
	 * @param description
	 * @param priority
	 * @param resolved
	 * @param article
	 */
	public Review(int number, String flowName, String description, Priority priority, boolean resolved, String article) {
		this.number = number;
		this.flowName = flowName;
		this.description = description;
		this.priority = priority;
		this.resolved = resolved;
		this.article = article;
	}

	public int getNumber() {
		return number;
	}

	public String getFlowName() {
		return flowName;
	}

	public String getDescription() {
		return description;
	}

	
	public Priority getPriority() {
		return priority;
	}

	public boolean isResolved() {
		return resolved;
	}

	public String getArticle() {
		return article;
	}

	public static Collection<Review> read(InputStream stream) throws IOException {
		Document document = XMLUtil.streamToDocument(stream);
		Collection<Review> result = new LinkedList<Review>();

		String flowName = document.getElementsByTagName("review").item(0).getAttributes().getNamedItem(
				"flowName").getNodeValue();
		NodeList items = document.getElementsByTagName("item");

		for (int i = 0; i < items.getLength(); i++) {
			Element item = (Element) items.item(i);
			int id = Integer.parseInt(item.getAttribute("id"));
			NodeList comments = item.getElementsByTagName("comment");
			String text = "";
			boolean resolved = false;
			Priority priority = Priority.values()[Integer.parseInt(item.getAttribute("priority"))];
			if (comments.getLength() > 0) {
				Element comment = (Element) comments.item(0);
				text = comment.getTextContent();
				Element lastComment = (Element) comments.item(comments.getLength() - 1);
				resolved = Integer.parseInt(lastComment.getAttribute("resolvedPageRev")) != -1;
			}

			result.add(new Review(id, flowName, text, priority, resolved, ""));

		}

		return result;
	}



}
