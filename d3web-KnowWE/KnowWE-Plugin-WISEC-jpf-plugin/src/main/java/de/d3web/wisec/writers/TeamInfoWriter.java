/*
 * Copyright (C) 2010 University Wuerzburg, Computer Science VI
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.WISECModel;


/**
 * 
 * @author joba
 * @created 06.08.2010 
 */
public class TeamInfoWriter extends WISECWriter {

	private static final String FILENAME_PRAEFIX = WISECExcelConverter.FILE_PRAEFIX + "Team_";

	public TeamInfoWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		for (String teamName : this.model.getAllTeamNames()) {
			String filename = getWikiFileNameFor(teamName);
			Writer writer = ConverterUtils.createWriter(this.outputDirectory + filename + ".txt");

			writeBreadcrumb(writer, teamName);
			writeTeamInfoPage(writer, teamName);

			writer.close();
		}

	}

	private void writeTeamInfoPage(Writer writer, String teamName) throws IOException {
		StringBuffer buffy = new StringBuffer();
		buffy.append("!!! Team: " + teamName + "\n\n");

		buffy.append("!! Substances \n");
		Collection<String> substances = this.model.teamSubstances.get(teamName);
		if (substances == null || substances.isEmpty()) {
			buffy.append("* - ");
		}
		else {
			for (String substance : substances) {
				buffy.append("* " + SubstanceInfoWriter.asWikiMarkup(substance) + "\n");
			}			
		}

		buffy.append("!! Groups \n");
		Collection<String> groups = this.model.teamGroups.get(teamName);
		if (groups == null || groups.isEmpty()) {
			buffy.append("* - ");
		}
		else {
			for (String group : groups) {
				buffy.append("* [" + group + "|" + GroupInfoWriter.getWikiFileNameFor(group)
						+ "]\n");
			}
		}

		writer.write(buffy.toString());
		// TODO: comment fields
	}

	protected void writeBreadcrumb(Writer writer, String teamName) throws IOException {
		super.writeBreadcrumb(writer);
		writer.write(" > [List of Substances|" + AllSubstancesWriter.FILENAME + "] > "
				+ "[Active Substances|" + ActiveSubstancesWriter.FILENAME + "] > Assessment > "
				+ teamName
				+ "\n\n");
	}

	public static String getWikiFileNameFor(String groupName) {
		return FILENAME_PRAEFIX + ConverterUtils.cleanForFilename(groupName);
	}
}
