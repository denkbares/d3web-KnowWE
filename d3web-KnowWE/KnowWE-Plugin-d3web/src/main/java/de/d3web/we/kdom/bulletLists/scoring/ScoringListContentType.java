package de.d3web.we.kdom.bulletLists.scoring;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.bulletLists.BulletContentType;
import de.d3web.we.kdom.bulletLists.BulletListItemLine;
import de.d3web.we.kdom.rendering.EditSectionRenderer;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.utils.KnowWEObjectTypeSet;
import de.d3web.we.utils.KnowWEObjectTypeUtils;

public class ScoringListContentType extends XMLContent{

	@Override
	protected void init() {
		this.childrenTypes.add(new BulletListItemLine());
		this.setCustomRenderer(new EditSectionRenderer());
		
		KnowWEObjectTypeSet set = new KnowWEObjectTypeSet(); 
		KnowWEObjectTypeUtils.getAllChildrenTypesRecursive(this, set);
		KnowWEObjectType contentType = set.getInstanceOf(BulletContentType.class);
		
		
		if(contentType instanceof AbstractKnowWEObjectType) { // damn, not nice. maybe we need some interface changes one day
			((AbstractKnowWEObjectType)contentType).addReviseSubtreeHandler(new CreateScoresHandler());
			((AbstractKnowWEObjectType)contentType).setCustomRenderer(new ValueRenderer());
		}
		
	}

}
