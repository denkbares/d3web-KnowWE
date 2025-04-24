/*
 * Copyright (C) 2025 denkbares GmbH, Germany
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

package de.knowwe.snapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.wiki.providers.SubWikiUtils;

import com.denkbares.strings.Strings;
import de.knowwe.core.Environment;
import de.knowwe.core.action.AbstractAction;
import de.knowwe.core.action.UserActionContext;
import de.knowwe.core.wikiConnector.WikiConnector;
import de.uniwue.d3web.gitConnector.UserData;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.wiki.providers.SubWikiUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static de.knowwe.snapshot.CreateSnapshotAction.createAndStoreWikiContentSnapshot;
import static de.knowwe.snapshot.CreateSnapshotToolProvider.SNAPSHOT;

//import com.denkbares.strings.Strings;
//import com.denkbares.versioning.server.Version;
//import de.knowwe.core.Environment;
//import de.knowwe.core.action.AbstractAction;
//import de.knowwe.core.action.UserActionContext;
//import de.knowwe.core.utils.KnowWEUtils;
//import de.knowwe.versioning.git.serverConnector.KnowWEGitServerConnector;
//import de.knowwe.versioning.git.serverConnectr.KnowWEGitServerConnectorFactory;
//import de.uniwue.d3web.gitConnector.UserData;

/**
 * Abstract super action for versioning actions. It prepares some input parameters
 * that are (potentially) used by versioning actions. Be aware that some of these input fields
 * may be null if not used for certain actions.
 * Provides further helpful util methods.
 */
public abstract class SnapshotAction extends AbstractAction {

	protected void writeJson(UserActionContext context, Object object) throws IOException {
		context.setContentType("application/json");
		ObjectMapper objectMapper = new ObjectMapper();
		// don't include null values in the json to reduce size
		objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		if (supportsGzip(context)) {
			context.setHeader("Content-Encoding", "gzip");
			GZIPOutputStream gzip = new GZIPOutputStream(context.getOutputStream());
			objectMapper.writeValue(gzip, object);
		}
		else {
			Writer writer = context.getWriter();
			objectMapper.writeValue(writer, object);
		}
	}

	private boolean supportsGzip(UserActionContext context) {
		String accepted = context.getRequest().getHeader("Accept-Encoding");
		if (accepted == null) {
			return false;
		}
		String[] encodings = accepted.split(",");
		for (String encoding : encodings) {
			if ("gzip".equalsIgnoreCase(encoding.trim())) {
				return true;
			}
		}
		return false;
	}

}
