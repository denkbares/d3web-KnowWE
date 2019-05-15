/*
 * Copyright (C) 2010 denkbares GmbH
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
package de.knowwe.d3web;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.rendering.RenderResult;
import de.knowwe.core.kdom.sectionFinder.AllTextFinderTrimmed;
import de.knowwe.core.user.UserContext;
import de.knowwe.kdom.AnonymousType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupRenderer;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

public class PluginConfigType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("plugin-config");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addContentType(new AnonymousType("xml", AllTextFinderTrimmed.getInstance()));
	}

	public PluginConfigType() {
		super(MARKUP);
		setRenderer(new Renderer());
		addCompileScript(new PluginConfigReviseSubtreeHandler());
	}

	private static class Renderer extends DefaultMarkupRenderer {
		public Renderer() {
			super("KnowWEExtension/images/settings_128.png");
			setListAnnotations(true);
			setPreFormattedStyle(false);
		}

		@Override
		public void renderContentsAndAnnotations(Section<?> section, UserContext user, RenderResult result) {
			result.append("%%prettify-nonum\n{{{");
			super.renderContentsAndAnnotations(section, user, result);
			result.append("}}}\n/%\n");
		}
	}
}
