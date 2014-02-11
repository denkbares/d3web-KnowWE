/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.include.export;

import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.include.IncludeMarkup;
import de.knowwe.include.InnerWikiReference;

/**
 * Export handler for exporting sections included by the include markup.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 08.02.2014
 */
public class IncludeExporter implements Exporter<IncludeMarkup> {

	@Override
	public Class<IncludeMarkup> getSectionType() {
		return IncludeMarkup.class;
	}

	@Override
	public boolean canExport(Section<IncludeMarkup> section) {
		return true;
	}

	@Override
	public void export(Section<IncludeMarkup> section, DocumentBuilder manager) throws ExportException {
		List<Section<InnerWikiReference>> references =
				Sections.successors(section, InnerWikiReference.class);
		for (Section<InnerWikiReference> reference : references) {
			manager.closeParagraph();
			int delta = getHeadingDelta(reference);
			boolean wasSuppressHeaderNumbering = manager.isSuppressHeaderNumbering();

			// export article title if requested
			String marks = reference.get().getListMarks(reference);
			int listLevel = marks.length();
			if (listLevel > 0) {
				manager.setSuppressHeaderNumbering(marks.endsWith("*"));
				String title = reference.get().getLinkName(reference);
				HeaderExporter.export(title, listLevel, manager);
				delta += listLevel;
			}

			// export included sections
			manager.incHeaderLevel(delta);
			manager.export(reference.get().getIncludedSections(reference));
			manager.incHeaderLevel(-delta);
			manager.setSuppressHeaderNumbering(wasSuppressHeaderNumbering);
		}
	}

	private int getHeadingDelta(Section<InnerWikiReference> reference) {
		int marks = reference.get().getMaxHeaderMarkCount(reference);
		return marks == 0 ? 0 : (marks - 3);
	}
}
