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

import de.d3web.core.inference.PSConfig;
import de.d3web.core.inference.PSMethod;
import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.plugin.Autodetect;
import de.d3web.plugin.Plugin;
import de.d3web.plugin.PluginConfig;
import de.d3web.plugin.PluginEntry;
import de.d3web.plugin.PluginManager;
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

	private static DefaultMarkup m = null;

	static {
		m = new DefaultMarkup("CoveringListSettings");
		m.addAnnotation(ESTABLISHED_THRESHOLD, false);
		m.addAnnotation(SUGGESTED_THRESHOLD, false);
		m.addAnnotation(MIN_SUPPORT, false);
		PackageManager.addPackageAnnotation(m);
	}



	public CoveringListSettingsMarkup() {
		super(m);
		this.addCompileScript(Priority.HIGHEST, new CoveringListSettingsHandler());
	}

	public class CoveringListSettingsHandler implements D3webHandler<CoveringListSettingsMarkup> {

		@Override
		public Collection<Message> create(D3webCompiler compiler, Section<CoveringListSettingsMarkup> s) {

			// Get KnowledgeBase
			KnowledgeBase kb = D3webUtils.getKnowledgeBase(compiler);
			if (kb == null) {
				return Messages.asList(Messages.error(
						"No knowledgebase available."));
			}

			// Get PSConfig
			PSConfig config = getPSConfig(kb);
			PSMethodXCL psMethod;
			if (config.getPsMethod() instanceof PSMethodXCL) {
				psMethod = (PSMethodXCL) config.getPsMethod();
			}
			else {
				return Messages.asList(Messages.error(
						"Internal error. Wrong PSMethod in PSConfig."));
			}

			// Get ScoreAlgorithms
			ScoreAlgorithm algorithm = psMethod.getScoreAlgorithm();
			if (algorithm == null) {
				return Messages.asList(Messages.error(
						"Internal error. No Score-Algorithm present."));
			}

			Collection<Message> m = new LinkedList<>();

			// Default established threshold
			Double establishedThreshold = getValueFromAnnotation(s,
					CoveringListSettingsMarkup.ESTABLISHED_THRESHOLD, m);
			if (!establishedThreshold.equals(Double.NaN)) {
				algorithm.setDefaultEstablishedThreshold(establishedThreshold);
			}

			// Default suggested threshold
			Double suggestedThreshold = getValueFromAnnotation(s,
					CoveringListSettingsMarkup.SUGGESTED_THRESHOLD, m);
			if (!suggestedThreshold.equals(Double.NaN)) {
				algorithm.setDefaultSuggestedThreshold(suggestedThreshold);
			}

			// Minimum support
			Double minSupport = getValueFromAnnotation(s, CoveringListSettingsMarkup.MIN_SUPPORT, m);
			if (!minSupport.equals(Double.NaN)) {
				algorithm.setDefaultMinSupport(minSupport);
			}

			return m;
		}

		private PSConfig getPSConfig(KnowledgeBase kb) {
			// Search for an existing PSConfig
			for (PSConfig psConfig : kb.getPsConfigs()) {
				PSMethod psm = psConfig.getPsMethod();
				if (psm == null) {
					continue;
				}
				if (psm.getClass().equals(PSMethodXCL.class)) {
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

		private double getValueFromAnnotation(Section<CoveringListSettingsMarkup> markup, String annotation, Collection<Message> messages) {
			double value = Double.NaN;
			String stringValue = DefaultMarkupType.getAnnotation(markup, annotation);
			if (stringValue != null) {
				try {
					value = Double.parseDouble(stringValue);
				}
				catch (NumberFormatException e) {
					messages.add(Messages.invalidNumberError(annotation));
				}
			}
			return value;
		}

	}
}
