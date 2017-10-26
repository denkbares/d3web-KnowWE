package de.knowwe.ontology.kdom.table;

import java.util.List;

import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.jspwiki.types.TableCell;
import de.knowwe.kdom.table.TableCellContent;
import de.knowwe.kdom.table.TableLine;
import de.knowwe.kdom.table.TableType;
import de.knowwe.kdom.table.TableUtils;

/**
 * @author Jochen Reutelshoefer (denkbares GmbH)
 * @created 26.10.17.
 */
public class HierarchyTableUtils {

	public static Section<?> findHierarchyLevelAbove(int level, Section<HierarchyTableMarkup.HierarchyLevelType> section) {
		Section<TableLine> line = Sections.ancestor(section, TableLine.class);
		int rowCounter = TableUtils.getRow(line);
		while(rowCounter >= 1) {
			Section<TableCellContent> hierarchyCell = TableUtils.getCell(section, 0, rowCounter);
			Section<HierarchyTableMarkup.HierarchyLevelType> hierarchyLevelSection = Sections.successor(hierarchyCell, HierarchyTableMarkup.HierarchyLevelType.class);
			Integer hierarchyLevel = HierarchyTableMarkup.HierarchyLevelType.getHierarchyLevel(hierarchyLevelSection);
			if(hierarchyLevel != null) {
				if(hierarchyLevel == level) {
					return hierarchyLevelSection;
				}
			}
			rowCounter--;
		}
		return null;
	}
}
