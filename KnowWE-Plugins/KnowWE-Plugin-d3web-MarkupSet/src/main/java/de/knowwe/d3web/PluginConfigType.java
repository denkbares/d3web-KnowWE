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
import de.knowwe.util.Icon;

public class PluginConfigType extends DefaultMarkupType {

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("plugin-config");
		PackageManager.addPackageAnnotation(MARKUP);
		MARKUP.addContentType(() -> new AnonymousType("xml", AllTextFinderTrimmed.getInstance()));

		MARKUP.setDocumentation("""
				Configures a problem solver (PSMethod) of the knowledge base with an XML snippet.
				<p>The content is one or more <code>&lt;psEntry&gt;</code> elements as they appear in the
				<code>&lt;psmethods&gt;</code> section of a persisted knowledge base. Each entry names the problem solver
				by <code>extensionID</code> and <code>pluginID</code> and sets its <code>priority</code> and
				<code>state</code> (<code>active</code>, <code>deactivated</code> or <code>autodetect</code>); the child
				elements and further attributes are specific to the problem solver. An entry replaces any previous
				configuration of the same problem solver.</p>
				<p>Only edit this with good knowledge of the configured problem solver.</p>
				""");
		MARKUP.addExample("""
				%%plugin-config
				<!-- configure the scoring of the set-covering problem solver -->
				<psEntry extensionID="PSMethodXCL" pluginID="d3web-XCL" priority="5.0" state="active">
				  <scoreAlgorithm name="SprintGroupScoreAlgorithm">
				    <defaultEstablishedThreshold value="0.9999"/>
				    <defaultSuggestedThreshold value="0.3"/>
				    <minSupport value="0.01"/>
				  </scoreAlgorithm>
				</psEntry>
				%
				""");
	}

	public PluginConfigType() {
		super(MARKUP);
		setRenderer(new Renderer());
		addCompileScript(new PluginConfigReviseSubtreeHandler());
	}

	private static class Renderer extends DefaultMarkupRenderer {
		public Renderer() {
			super(Icon.SETTINGS);
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
