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

package de.knowwe.jspwiki.readOnly;

import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Simple markup that displays the content of the markup in case the read only mode is active.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 28.09.15
 */
public class ReadOnlyMessageMarkup extends DefaultMarkupType {

	public ReadOnlyMessageMarkup() {
		super(new DefaultMarkup("ReadOnlyMessage"));
		setRenderer((section, user, result) -> {
			boolean readOnly = ReadOnlyManager.isReadOnly();
			result.appendHtml("<div class='readOnlyMessage'" + (readOnly ? " style='display: block'" : "") + ">");
			result.append(DefaultMarkupType.getContent(section));
			result.appendHtml("</div>");
		});
	}

}
