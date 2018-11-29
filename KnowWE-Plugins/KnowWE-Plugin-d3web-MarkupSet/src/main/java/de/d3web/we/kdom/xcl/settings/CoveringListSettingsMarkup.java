/*
 * Copyright (C) 2011 University Wuerzburg, Computer Science VI
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
package de.d3web.we.kdom.xcl.settings;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import com.denkbares.plugin.Plugin;
import com.denkbares.plugin.PluginManager;
import de.d3web.core.inference.PSConfig;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.plugin.Autodetect;
import de.d3web.plugin.PluginConfig;
import de.d3web.plugin.PluginEntry;
import de.d3web.we.knowledgebase.D3webCompiler;
import de.d3web.we.reviseHandler.D3webHandler;
import de.d3web.we.utils.D3webUtils;
import de.d3web.xcl.ScoreAlgorithm;
import de.d3web.xcl.inference.PSMethodXCL;
import de.knowwe.core.compile.Priority;
import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.report.Message;
import de.knowwe.core.report.Messages;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * This MarkUp offers some annotations to configure the XCL problem solver.
 *
 * @author Sebastian Furth (denkbares GmbH)
 * @created Jan 24, 2011
 */
public class CoveringListSettingsMarkup extends DefaultMarkupType {

	public static final String ESTABLISHED_THRESHOLD = "establishedThreshold";
	public static final String SUGGESTED_THRESHOLD = "suggestedThreshold";
	public static final String MIN_SUPPORT = "minSupport";

	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("CoveringListSettings");
		MARKUP.addAnnotation(ESTABLISHED_THRESHOLD, false);
		MARKUP.addAnnotation(SUGGESTED_THRESHOLD, false);
		MARKUP.addAnnotation(MIN_SUPPORT, false);
		PackageManager.addPackageAnnotation(MARKUP);
	}

	public CoveringListSettingsMarkup() {
		super(MARKUP);
		this.addCompileScript(Priority.HIGHEST, new CoveringListSettingsHandler());
	}

	public static class CoveringListSettingsHandler implements D3webHandler<CoveringListSettingsMarkup> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<CoveringListSettingsMarkup> s) {

			// Get KnowledgeBase
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			if (kb == null) {
				return Messages.asList(Messages.error("No knowledgebase available."));
			}

			// Get ScoreAlgorithms
			ScoreAlgorithm algorithm = ((PSMethodXCL) getPSConfig(kb).getPsMethod()).getScoreAlgorithm();
			if (algorithm == null) {
				return Messages.asList(Messages.error("Internal error. No Score-Algorithm present."));
			}

			// Default established threshold, suggested threshold, and min support
			Collection<Message> m = new LinkedList<>();
			getValueFromAnnotation(s, CoveringListSettingsMarkup.ESTABLISHED_THRESHOLD, m).ifPresent(algorithm::setDefaultEstablishedThreshold);
			getValueFromAnnotation(s, CoveringListSettingsMarkup.SUGGESTED_THRESHOLD, m).ifPresent(algorithm::setDefaultSuggestedThreshold);
			getValueFromAnnotation(s, CoveringListSettingsMarkup.MIN_SUPPORT, m).ifPresent(algorithm::setDefaultMinSupport);
			return m;
		}

		private PSConfig getPSConfig(KnowledgeBase kb) {
			// Search for an existing PSConfig, but only with PSMethod XCL
			for (PSConfig psConfig : kb.getPsConfigs()) {
				PSMethod psm = psConfig.getPsMethod();
				if (psm instanceof PSMethodXCL) {
					return psConfig;
				}
			}

			// get PluginConfiguration
			PluginConfig pc = PluginConfig.getPluginConfig(kb);

			// get PluginEntry, if none is found, one will be created
			PluginEntry pluginEntry = pc.getPluginEntry(PSMethodXCL.PLUGIN_ID);
			if (pluginEntry == null) {
				Plugin plugin = PluginManager.getInstance().getPlugin(PSMethodXCL.PLUGIN_ID);
				pluginEntry = new PluginEntry(plugin);
				pc.addEntry(pluginEntry);
			}

			// get autodetect of the psMethod
			Autodetect auto = pluginEntry.getAutodetect();

			// add the newly created configuration
			PSConfig config = new PSConfig(PSConfig.PSState.autodetect, new PSMethodXCL(), auto,
					PSMethodXCL.EXTENSION_ID, PSMethodXCL.PLUGIN_ID, 5);
			kb.addPSConfig(config);
			return config;
		}

		private Optional<Double> getValueFromAnnotation(Section<CoveringListSettingsMarkup> markup, String annotation, Collection<Message> messages) {
			String stringValue = DefaultMarkupType.getAnnotation(markup, annotation);
			if (stringValue != null) {
				try {
					return Optional.of(Double.parseDouble(stringValue));
				}
				catch (NumberFormatException e) {
					messages.add(Messages.invalidNumberError(annotation));
				}
			}
			return Optional.empty();
		}
	}
}
