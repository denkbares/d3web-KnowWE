/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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

package de.knowwe.version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.commons.lang.ObjectUtils;

import com.denkbares.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.rendering.Renderer;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.version.taghandler.VersionTagHandler;

/**
 * @author Jonas Müller
 * @created 14.12.16
 */
public class VersionMarkupType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Version");
		MARKUP.addAnnotation("type", false, "short", "long");
	}

	public VersionMarkupType() {
		super(MARKUP);
		setRenderer((section, user, result) -> {
			String type = DefaultMarkupType.getAnnotation(section, "type");
			String manifestPath = Environment.getInstance()
					.getWikiConnector()
					.getApplicationRootPath() + "/META-INF/MANIFEST.MF";
			try {
				Manifest manifest = new Manifest(new FileInputStream(new File(manifestPath)));
				Attributes attributes = manifest.getMainAttributes();

				if ("long".equals(type)) {
					result.appendHtml("<span style='font-weight:bold'>Version:</span> ");
				}

				result.appendHtml("<span>" + attributes.getValue("Implementation-Version") + ",</span> ");

				if ("long".equals(type)) {
					result.appendHtml("<br><span style='font-weight:bold'>Date:</span> ");
				}

				String dateString;
				try {
					Date date = Date.from(Instant.parse(attributes.getValue("Build-Date")));
					dateString = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(date);
				} catch (NullPointerException npe) {
					dateString = VersionTagHandler.getBuildTime();
				}

				result.appendHtml("<span>" + dateString + "</span>");

			} catch (IOException e) {
				Log.severe("Could not read manifest file", e);
			}
		});
	}

}
