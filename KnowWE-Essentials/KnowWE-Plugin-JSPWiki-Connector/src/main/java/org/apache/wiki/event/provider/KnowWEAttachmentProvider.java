/*
 * Copyright (C) 2015 denkbares GmbH, Germany
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

package org.apache.wiki.event.provider;

import java.io.IOException;
import java.io.InputStream;

import org.apache.wiki.api.exceptions.ProviderException;
import org.apache.wiki.event.WikiAttachmentEvent;
import org.apache.wiki.event.WikiEventManager;
import org.apache.wiki.providers.BasicAttachmentProvider;

import com.denkbares.events.EventManager;
import de.knowwe.event.ArticleUpdateEvent;

/**
 * AttachmentProvider that justs fixes the BasicAttachmentProvider from JSPWiki, which does not fire proper events when
 * storing and deleting attachments.
 *
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 16.06.15
 */
public class KnowWEAttachmentProvider extends BasicAttachmentProvider {

	public static final String FIRE_DELETE_EVENT = "fireDeleteEvent";

	@Override
	public void putAttachmentData(org.apache.wiki.api.core.Attachment att, InputStream data) throws ProviderException, IOException {
		super.putAttachmentData(att, data);
		WikiEventManager.fireEvent(this, new WikiAttachmentEvent(this, att.getParentName(), att.getFileName(), WikiAttachmentEvent.STORED));
		EventManager.getInstance()
				.fireEvent(new ArticleUpdateEvent(att.getParentName() + "/" + att.getFileName(), att.getAuthor()));
	}

	@Override
	public void deleteAttachment(org.apache.wiki.api.core.Attachment att) throws ProviderException {
		super.deleteAttachment(att);
		EventManager.getInstance()
				.fireEvent(new ArticleUpdateEvent(att.getParentName() + "/" + att.getFileName(), att.getAuthor()));
		if (!"false".equals(att.getAttribute(FIRE_DELETE_EVENT))) {
			WikiEventManager.fireEvent(this, new WikiAttachmentEvent(this, att.getParentName(), att.getFileName(), WikiAttachmentEvent.DELETED));
		}
	}
}
