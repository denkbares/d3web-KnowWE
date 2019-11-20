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
import java.util.Date;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.jetbrains.annotations.Nullable;

import com.denkbares.utils.Log;
import de.knowwe.core.Environment;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;
import de.knowwe.version.taghandler.VersionTagHandler;

/**
 * @author Jonas Müller
 * @created 14.12.16
 */
public class VersionMarkupType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	private static Manifest manifest;

	static {
		MARKUP = new DefaultMarkup("Version");
		MARKUP.setInline(true);
		MARKUP.addAnnotation("type", false, "short", "long");
	}

	public VersionMarkupType() {
		super(MARKUP);
		setRenderer((section, user, result) -> {
			manifest = getApplicationManifest();
			String type = DefaultMarkupType.getAnnotation(section, "type");
			if (manifest != null) {
				result.appendHtml("<div style='font-size: 90%'>");
				Attributes attributes = manifest.getMainAttributes();

				if ("long".equals(type)) {
					result.appendHtml("<span style='font-weight:bold'>Version:</span> ");
				}

				String version = attributes.getValue("Implementation-Version");
				if (version != null) {
					result.appendHtml("<span>" + version + ",</span> ");
				}

				if ("long".equals(type)) {
					result.appendHtml("<br><span style='font-weight:bold'>Date:</span> ");
				}

				String dateString;
				String value = attributes.getValue("Build-Date");
				if (value == null) {
					dateString = VersionTagHandler.getBuildTime();
				}
				else {
					try {
						Date date = Date.from(Instant.parse(value));
						dateString = new SimpleDateFormat("yyyy-MM-dd HH:MM").format(date);
					}
					catch (Exception e) {
						dateString = value;
					}
				}

				result.appendHtml("<span>" + dateString + "</span>");
				if ("long".equals(type)) {
					String buildBranch = attributes.getValue("Build-Branch");
					String buildVersion = attributes.getValue("Build-Version");
					if (buildVersion == null) {
						buildVersion = "";
					}
					else {
						buildVersion = " " + buildVersion;
					}
					if (buildBranch == null || "${scmBranch}".equals(buildBranch)) {
						buildBranch = "Local IDE build";
					}
					result.appendHtml("<br/><span> Build: " + buildBranch + buildVersion + "</span>");
				}
				result.appendHtml("</div>");
			}
		});
	}

	/**
	 * Tries to load the MANIFEST.MF for the application and returns the singleton manifest
	 *
	 * @return the application manifest
	 */
	@Nullable
	public static Manifest getApplicationManifest() {
		if (manifest == null && Environment.isInitialized()) {
			String manifestPath = Environment.getInstance()
					.getWikiConnector()
					.getApplicationRootPath() + "/META-INF/MANIFEST.MF";
			try {
				manifest = new Manifest(new FileInputStream(new File(manifestPath)));
			}
			catch (IOException e) {
				Log.warning("Could not read manifest file, build info will not be displayed.");
			}
		}
		return manifest;
	}
}
