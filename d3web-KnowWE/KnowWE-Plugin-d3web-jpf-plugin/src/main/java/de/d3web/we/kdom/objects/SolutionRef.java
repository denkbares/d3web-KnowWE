package de.d3web.we.kdom.objects;

import de.d3web.core.knowledge.terminology.Solution;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.FontColorRenderer;
import de.d3web.we.terminology.TerminologyManager;

public class SolutionRef extends D3webObjectRef<Solution> {

	public SolutionRef() {
		this.setCustomRenderer(FontColorRenderer.getRenderer(FontColorRenderer.COLOR4));
	}

	@Override
	public Solution getObject(Section<? extends ObjectRef<Solution>> s) {
		if (s.get() instanceof SolutionRef) {

			Section<? extends ObjectDef<Solution>> objectDefinition = TerminologyManager.getInstance().getObjectDefinition(
					s);
			Solution sol = null;
			if (objectDefinition != null) {
				sol = objectDefinition.get().getObject(objectDefinition);
			}


			return sol;
		}
		return null;
	}

	@Override
	public boolean objectExisting(Section<? extends ObjectRef<Solution>> s) {
		return getObject(s) != null;
	}

}
