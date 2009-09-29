package de.d3web.we.kdom.tagging;

import org.openrdf.model.URI;

import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.renderer.NothingRenderer;
import de.d3web.we.kdom.xml.XMLContent;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology;

public class TagsContent extends XMLContent {

	@Override
	protected void init() {		
		this.setCustomRenderer(NothingRenderer.getInstance());
	}

	@Override
	public IntermediateOwlObject getOwl(Section s) {
		String text = s.getOriginalText();
		IntermediateOwlObject io = new IntermediateOwlObject();
		for (String cur : text.split(" |,")) {
			UpperOntology uo = UpperOntology.getInstance();
			URI suri = uo.getHelper().createlocalURI(s.getTitle());
			URI puri = uo.getHelper().createlocalURI("hasTag");
			URI ouri = uo.getHelper().createlocalURI(cur);
			io.merge(uo.getHelper().createProperty(suri, puri, ouri, s));
		}
		return io;
	}

}
