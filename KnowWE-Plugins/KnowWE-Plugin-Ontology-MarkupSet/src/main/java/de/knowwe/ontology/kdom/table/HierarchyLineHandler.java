package de.knowwe.ontology.kdom.table;

import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.ontology.compile.provider.NodeProvider;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 26.10.17.
 */
public class HierarchyLineHandler extends LineHandler {

	@Override
	protected Section<NodeProvider> findSubject(Section<TableLine> section) {
		List<Section<TableCellContent>> cells = Sections.successors(section, TableCellContent.class);
		if(cells.size() > 1) {
			return Sections.successor(cells.get(1), NodeProvider.class);
		}
		return null;
	}
}
