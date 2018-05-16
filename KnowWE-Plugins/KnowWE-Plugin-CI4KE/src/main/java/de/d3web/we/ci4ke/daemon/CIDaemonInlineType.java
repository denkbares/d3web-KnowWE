/*
 * Copyright (C) 2018 denkbares GmbH. All rights reserved.
 */

package de.d3web.we.ci4ke.daemon;

import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * An inline version of the CI daemon.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.05.18
 */
public class CIDaemonInlineType extends DefaultMarkupType {

	public static final String DASHBOARD_ARTICLE = "dashboardArticle";
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("CIDaemonInline");
		MARKUP.setInline(true);
		CIDaemonType.configureMarkup(MARKUP);
	}

	public CIDaemonInlineType() {
		super(MARKUP);
		this.setRenderer(new CIDaemonRenderer());
	}
}
