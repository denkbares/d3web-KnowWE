package de.d3web.we.kdom.questionTreeNew;

import java.util.List;

import de.d3web.we.kdom.AbstractKnowWEObjectType;
import de.d3web.we.kdom.InvalidKDOMSchemaModificationOperation;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.dashTree.DashTree;
import de.d3web.we.kdom.dashTree.DashTreeElement;
import de.d3web.we.kdom.dashTree.DashTreeElementContent;
import de.d3web.we.kdom.dashTree.SubTree;

public class QuestionDashTree extends DashTree {

	public QuestionDashTree() {
		super();
		this.setCustomRenderer(null);
		replaceRootType(this);
	}

	private void replaceRootType(AbstractKnowWEObjectType subClassingDashTree) {
		List<KnowWEObjectType> types = subClassingDashTree
				.getAllowedChildrenTypes();
		for (KnowWEObjectType knowWEObjectType : types) {
			if (knowWEObjectType instanceof SubTree) {
				List<KnowWEObjectType> content = knowWEObjectType
						.getAllowedChildrenTypes();
				for (KnowWEObjectType knowWEObjectType2 : content) {
					if (knowWEObjectType2 instanceof DashTreeElement) {
						try {
							((AbstractKnowWEObjectType) knowWEObjectType2)
									.replaceChildType(
											new QuestionDashTreeElementContent(),
											DashTreeElementContent.class);

						} catch (InvalidKDOMSchemaModificationOperation e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}
