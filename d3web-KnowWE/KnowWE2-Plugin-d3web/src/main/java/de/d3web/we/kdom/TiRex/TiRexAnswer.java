package de.d3web.we.kdom.TiRex;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.kdom.rendering.SpecialDelegateRenderer;
import de.d3web.we.module.semantic.owl.IntermediateOwlObject;
import de.d3web.we.module.semantic.owl.UpperOntology2;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class TiRexAnswer extends DefaultAbstractKnowWEObjectType {

	Map<Section, TiRexAnswerInfo> answerStore = new HashMap<Section, TiRexAnswerInfo>();

	@Override
	public KnowWEDomRenderer getDefaultRenderer() {
		return new KnowWEDomRenderer() {
			@Override
			public String render(Section sec, KnowWEUserContext user, String web,
					String topic) {

				TiRexAnswerInfo info = ((TiRexAnswer) sec.getObjectType())
						.getAnswerInfo(sec);
				String questionid = info.answerID;
				String qText = info.answerText;
				Double rating = info.rating;
				String strat = info.strategy;
				String kbid = info.kbid;

				String title = "Answer: " + qText + " id: " + questionid
						+ " rating:" + rating + " strat: " + strat + " kbid:"
						+ kbid;
				

//				return "<i>"+DefaultDelegateRenderer.getInstance().render(sec, user, web, topic)+"</i>";
				
				return spanColorTitle(SpecialDelegateRenderer.getInstance()
						.render(sec, user, web, topic), "lightgray", title);
			}
		};
	}

	@Override
	public Collection<Section> getAllSectionsOfType() {
		return answerStore.keySet();
	}

	public TiRexAnswerInfo getAnswerInfo(Section s) {
		return answerStore.get(s);
	}

	@Override
	public List<? extends KnowWEObjectType> getAllowedChildrenTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SectionFinder getSectioner() {
		return new TiRexAnswerSectioner(this);
	}

	

	

	@Override
	public IntermediateOwlObject getOwl(Section section) {
		IntermediateOwlObject io = new IntermediateOwlObject();
		UpperOntology2 uo = UpperOntology2.getInstance();
		TiRexAnswerInfo info = getAnswerInfo(section);
		String answer = info.getAnswerText();
		URI answeruri = uo.createlocalURI(answer);
		io.addLiteral(answeruri);
		return io;
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

}
