/*
 * Copyright (C) 2011 denkbares GmbH
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
package de.d3web.we.ci4ke.daemon;

import de.d3web.we.ci4ke.dashboard.type.CIDashboardType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * The CI-Daemon can be connected to a dashboard and renders the colored bubble
 * according to the current dashboard build state. It can be inserted at a
 * highly visible location (e.g., left menu) to constantly display the quality
 * state of a dashboard.
 * 
 * @author Albrecht Striffler
 * @created 14.01.2011
 */
public class CIDaemonType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	public static final String DASHBOARD_ARTICLE = "dashboardArticle";

	static {
		MARKUP = new DefaultMarkup("CIDaemon");
		configureMarkup(MARKUP);
	}

	public static void configureMarkup(DefaultMarkup markup) {
		markup.addAnnotation(DASHBOARD_ARTICLE, false);
		markup.setAnnotationDeprecated(DASHBOARD_ARTICLE);
		markup.addAnnotation(CIDashboardType.NAME_KEY, true);
	}

	public CIDaemonType() {
		super(MARKUP);
		this.setRenderer(new CIDaemonRenderer());
	}

}
