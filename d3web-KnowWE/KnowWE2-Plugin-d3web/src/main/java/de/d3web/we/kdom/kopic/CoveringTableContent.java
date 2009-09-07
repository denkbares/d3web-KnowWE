package de.d3web.we.kdom.kopic;

import de.d3web.we.kdom.table.TableContent;
import de.d3web.we.kdom.xml.XMLContent;

public class CoveringTableContent extends XMLContent{
	

	@Override
	protected void init() {
		childrenTypes.add( new TableContent() );
	}
	
//	not used anymore, TableContent does this now
//	@Override
//	public void reviseSubtree(Section s, TerminologyManager  kbm, String web, KnowWEDomParseReport rep) {
//		List<Section> lines = new ArrayList<Section>();
//		s.findChildrenOfType(TableLine.class, lines);
//		
//		for(int i = 2; i <  lines.size(); i++) {
//			Section lineX = lines.get(i);
//			List<Section> cells = new ArrayList<Section>();
//			lineX.findChildrenOfType(TableCellContent.class, cells);
//			for(int j = 1; j <  cells.size(); j++) {
//				cells.get(j).setRenderer(new SettingsModeRenderer(DefaultDelegateRenderer.getInstance(),new EditCoveringTableCellRenderer()));
//			}
//		}
//		
//	}

}
