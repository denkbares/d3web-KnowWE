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
public class GroupsWriter extends WISECWriter {

	private static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "Groups";

	public GroupsWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENAME + ".txt");
		writeBreadcrumb(writer);

		StringBuffer buffy = new StringBuffer();
		// open the zebra and the sortable table
		buffy.append("%%zebra-table\n%%sortable\n");

		buffy.append("|| Group_name || CAS_no || \n");
		for (String groupName : this.model.groups.keySet()) {
			Group group = this.model.groups.get(groupName);
			Collection<String> cas_nos = group.getSubstances();
			if (cas_nos != null && !cas_nos.isEmpty()) {
				buffy.append("| [" + groupName + "|"
						+ GroupInfoWriter.getWikiFileNameFor(Integer.toString(group.getID()))
						+ "] | ");
				for (String cas : cas_nos) {
					buffy.append(" [ " + cas + " | "
							+ SubstanceInfoWriter.getWikiFileNameFor(cas) + "]\\\\");
				}
				buffy.append("| [add to group|dummy]\n");
			}
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
				+ "[Active Substances|" + ActiveSubstancesWriter.FILENAME + "] > Groups\n\n");
	}
}
