package de.d3web.we.kdom.kopic.renderer;

import java.util.ArrayList;
import java.util.List;

import de.d3web.report.Message;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.SettingsModeRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.kdom.table.TableCellContent;

public class KopicTableSectionRenderer extends KopicSectionRenderer {

	@Override
	protected String wrappContent(String string) {
		return string;
	}

	@Override
	protected void insertErrorRenderer(List<Section> lines, Message m,
			String user) {
		int line = m.getLineNo();
		int col = m.getColumnNo();
		if (line - 1 >= 0 && line - 1 < lines.size()) {
			Section lineSec = lines.get(line-1);
			List<Section> cells = new ArrayList<Section>();
			lineSec.findChildrenOfType(TableCellContent.class, cells);
			Section cell = cells.get(col - 1);
			if (!cell.hasQuickEditModeSet(user)) {
				cell.setRenderer(ErrorRendererTable.getInstance());
			}else {
				cell.setRenderer(new SettingsModeRenderer(SpecialDelegateRenderer.getInstance(),new EditCoveringTableCellRenderer()));
			}
		}
	}

}
