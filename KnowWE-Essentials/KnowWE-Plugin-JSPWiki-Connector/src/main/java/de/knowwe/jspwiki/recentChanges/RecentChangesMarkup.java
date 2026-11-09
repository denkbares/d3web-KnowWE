/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.jspwiki.recentChanges;

import de.knowwe.core.kdom.Types;
import de.knowwe.kdom.defaultMarkup.ContentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.kdom.renderer.AsynchronousRenderer;
import de.knowwe.kdom.renderer.PaginationRenderer;

public class RecentChangesMarkup extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("RecentChanges");
		MARKUP.setTemplate("%%RecentChanges %\n");
		MARKUP.setDocumentation("""
				Shows the recent changes of the wiki as a table with the columns page, last modified, author and change
				notes.
				<p>The table is paginated and can be sorted and filtered by each column, e.g. to show only the changes of
				one author. The markup has no content and no annotations.</p>
				""");
		MARKUP.addExample("%%RecentChanges %\n");
	}

	public RecentChangesMarkup() {
		super(MARKUP);
		Types.injectRendererToSuccessors(this, ContentType.class, new AsynchronousRenderer(new RecentChangesPaginationRenderer(new RecentChangesRenderer(), PaginationRenderer.SortingMode.multi, true)));
	}
}
