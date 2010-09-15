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
package de.d3web.wisec.writers;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.SourceList;
import de.d3web.wisec.model.WISECModel;

public class SourceListOverviewWriter extends WISECWriter {

	public static final String FILENAME = WISECExcelConverter.FILE_PRAEFIX
			+ "All+Sources";

	private static final String[] WRITEABLE_ATTRIBUTES = new String[] {
			"ID", "Name", "Author", "Country", "Inclusion_WISEC"
	};

	public SourceListOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory
				+ FILENAME + ".txt");

		writeBreadcrumb(writer);
		writeHeader(writer);
		StringBuffer buffy = new StringBuffer();
		List<SourceList> sortedLists = new ArrayList<SourceList>(model.sourceLists);
		Collections.sort(sortedLists, new Comparator<SourceList>() {

			@Override
			public int compare(SourceList o1, SourceList o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		for (SourceList list : sortedLists) {
			if (list.getId() != null && !list.getId().isEmpty()) {
				for (String attribute : WRITEABLE_ATTRIBUTES) {
					String value = list.get(attribute);
					if (value != null) {
						value = ConverterUtils.clean(value);
						if (value.equals(list.getName())) {
							value = "[" + value + " | "
									+ ConverterUtils.cleanWikiLinkSpaces(SourceListWriter.getWikiFilename(list.getId()))
									+ "]";
						}
						else if (attribute.equals(WISECExcelConverter.NUMBER_KEY)) {
							value = ConverterUtils.toShoppedString(value);
						}
					}
					else {
						value = "";
					}
					buffy.append("| " + value + " ");
				}
				buffy.append("\n");
			}
		}
		writer.append(buffy.toString());

		writeFooter(writer);
		writer.close();
	}

	private void writeFooter(Writer writer) throws IOException {
		// close the zebra and the sortable table
		writer.append("/%\n/%\n");
	}

	private void writeHeader(Writer writer) throws IOException {
		writer.append("!!! Information Sources %%(font-size:small;font-weight:normal)- sources used/not used for data basis  %% \n\n");
		// open the rebra and the sortable table
		writer.append("%%zebra-table\n%%sortable\n");
		// write all header names
		for (String attribute : WRITEABLE_ATTRIBUTES) {
			writer.append("|| " + attribute);
		}
		writer.append("\n");
	}

	@Override
	protected void writeBreadcrumb(Writer writer) throws IOException {
		super.writeBreadcrumb(writer);
		writer.append(" > Index of Sources \n\n");
	}

}
