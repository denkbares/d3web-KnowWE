/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
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
package de.knowwe.d3web.resource;

import java.io.IOException;
import java.io.InputStream;

import de.d3web.core.knowledge.Resource;
import de.d3web.we.wikiConnector.ConnectorAttachment;

/**
 * Resource for storing an attachment of an Wikipage. See also:
 * {@link ImageToResourceType}
 * 
 * @author Johannes Dienst, Volker Belli
 * 
 */
public class WikiAttachmentResource implements Resource {

	private final String path;
	private final ConnectorAttachment attachment;

	public WikiAttachmentResource(String path, ConnectorAttachment attachment) {
		this.attachment = attachment;
		this.path = path;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.attachment.getInputStream();
	}

	@Override
	public String getPathName() {
		return path;
	}

	@Override
	public long getSize() {
		return this.attachment.getSize();
	}

}
