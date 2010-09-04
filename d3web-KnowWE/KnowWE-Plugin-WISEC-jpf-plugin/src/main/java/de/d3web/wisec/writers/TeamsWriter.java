/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
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
package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.Group;
import de.d3web.wisec.model.WISECModel;

/**
 * 
 * @author joba
 * @created 06.08.2010
 */
public class TeamsWriter extends WISECWriter {

	private static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "Teams";

	public TeamsWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENAME + ".txt");
		writeBreadcrumb(writer);

		StringBuffer buffy = new StringBuffer();
		buffy.append("!!! Teams\n\n");

		// open the zebra and the sortable table
		buffy.append("%%zebra-table\n%%sortable\n");

		buffy.append("|| Team_name || CAS_no || Groups || \n");
		for (String teamName : model.getAllTeamNames()) {
			buffy.append("| [" + teamName + "|" + TeamInfoWriter.getWikiFileNameFor(teamName)
					+ "] ");

			// the substances
			Collection<String> cas_nos = this.model.teamSubstances.get(teamName);
			buffy.append(" | ");
			if (cas_nos != null && !cas_nos.isEmpty()) {
				for (String cas : cas_nos) {
					buffy.append(" [ " + cas + " | "
							+ SubstanceInfoWriter.getWikiFileNameFor(cas) + "]\\\\");
				}
			}

			// the groups
			Collection<String> groups = this.model.teamGroups.get(teamName);
			buffy.append(" | ");
			if (groups != null && !groups.isEmpty()) {
				for (String groupName : groups) {
					Group group = this.model.groups.get(groupName);
					buffy.append(" [ " + groupName
							+ " | "
							+ GroupInfoWriter.getWikiFileNameFor(Integer.toString(group.getID()))
							+ "]\\\\");
				}
			}

			// the dynamics
			buffy.append("| [add to team|dummy]\n");

		}
		// close the zebra and the sortable table
		buffy.append("/%\n/%\n");

		writer.write(buffy.toString());
		writer.close();

	}

	@Override
	protected void writeBreadcrumb(Writer writer) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [List of Substances|" + AllSubstancesOverviewWriter.FILENAME + "] > "
				+ "[Active Substances | " + ActiveSubstancesWriter.FILENAME
				+ "] > Assessment > Teams\n\n");
	}
}
