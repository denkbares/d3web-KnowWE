/*
 * Copyright (C) 2012 denkbares GmbH, Germany
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
package connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import de.knowwe.core.wikiConnector.WikiAttachment;

/**
 * Base class that implements the multi-version behavior of
 * {@link WikiAttachment}s for the non-version implementations of attachments of
 * the headless KnowWE implementation.
 * 
 * @author volker_belli
 * @created 18.05.2012
 */
abstract class SingleVersionAttachment implements WikiAttachment {

	private final DummyPageProvider pageProvider;

	public SingleVersionAttachment(DummyPageProvider pageProvider) {
		this.pageProvider = pageProvider;
	}

	public DummyPageProvider getPageProvider() {
		return pageProvider;
	}

	@Override
	public String getPath() {
		return getParentName() + "/" + getFileName();
	}

	@Override
	public Date getDate(int version) throws IllegalArgumentException {
		if (version != 1) throw new IllegalArgumentException();
		return getDate();
	}

	@Override
	public long getSize(int version) {
		if (version != 1) throw new IllegalArgumentException();
		return getSize();
	}

	@Override
	public InputStream getInputStream(int version) throws IOException, IllegalArgumentException {
		if (version != 1) throw new IllegalArgumentException();
		return getInputStream();
	}

	@Override
	public int getVersion() {
		return 1;
	}

	@Override
	public int[] getAvailableVersions() throws IOException {
		return new int[] { 1 };
	}

	@Override
	public void delete(int version) throws IOException, IllegalArgumentException {
		if (version != 1) throw new IllegalArgumentException();
		getPageProvider().deleteAttachment(getPath());
	}
}