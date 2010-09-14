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

import de.d3web.wisec.converter.WISECExcelConverter;
import de.d3web.wisec.model.WISECModel;

public class RatingOverviewWriter extends WISECWriter {

	private final String FILENAME = WISECExcelConverter.FILE_PRAEFIX + "Overview+Ratings";

	public RatingOverviewWriter(WISECModel model, String outputDirectory) {
		super(model, outputDirectory);
	}

	@Override
	public void write() throws IOException {
		Writer writer = ConverterUtils.createWriter(this.outputDirectory + FILENAME + ".txt");

		writer.write("!!! Rankings\n\n");
		for (String rating : model.generatedRatings()) {
			writer.write("* [" + rating + " | "
					+ ConverterUtils.cleanWikiLinkSpaces(model.wikiFileNameForRating(rating))
					+ "]\n");
		}

		writer.close();
	}

}
