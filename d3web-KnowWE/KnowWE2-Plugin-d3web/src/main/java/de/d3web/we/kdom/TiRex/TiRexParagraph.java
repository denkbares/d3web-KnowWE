package de.d3web.we.kdom.TiRex;

import java.util.HashMap;
import java.util.Map;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.sectionFinder.LineSectionFinder;
import de.d3web.we.kdom.typeInformation.XCLRelationInfo;

public class TiRexParagraph extends DefaultAbstractKnowWEObjectType{
	
	
	private Map<Section, XCLRelationInfo> relationStore = new HashMap<Section, XCLRelationInfo>(); 
	
	@Override
	protected  void init() {
		this.sectionFinder = new LineSectionFinder(this);
		childrenTypes.add(new TiRexSentence());
		//childrenTypes.add(new TiRexRestWordSequence());
	}
	
	
	public void storeRelation(Section s, XCLRelationInfo info) {
		relationStore.put(s, info);
	}
	
	public XCLRelationInfo getRelationInfo(Section s) {
		return relationStore.get(s);
	}

//	@Override
//	public KnowWEDomRenderer getDefaultRenderer() {
//		return new KnowWEDomRenderer () {
//			public String render(Section sec, String user, String web, String topic) {
//				String title = "no relation found";
//				XCLRelationInfo info  = ((TiRexLine)sec.getObjectType()).getRelationInfo(sec);
//				if(info != null) {
//						title = info.getId();
//				}
//				return divColorTitle(DefaultDelegateRenderer.getInstance().render(sec, user, web, topic), "",title);
//			}
//		};
//	}

	
	

}
