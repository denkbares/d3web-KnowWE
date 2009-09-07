package de.d3web.we.action;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.we.core.KnowWEArticleManager;
import de.d3web.we.core.KnowWEEnvironment;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.javaEnv.KnowWEAttributes;
import de.d3web.we.javaEnv.KnowWEParameterMap;
import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.TiRex.TiRex;
import de.d3web.we.kdom.TiRex.TiRexParagraph;
import de.d3web.we.kdom.contexts.ContextManager;
import de.d3web.we.kdom.contexts.SolutionContext;
import de.d3web.we.kdom.typeInformation.XCLRelationInfo;

public class TirexToXCLRenderer implements KnowWEAction {

	@Override
	public String perform(KnowWEParameterMap parameterMap) {

		// Get all TiRexSection of the current Article
		LinkedList <Section> tiRexSectionList = this.getAllTiRexSections(parameterMap);

		String xcl = "";
		// extract
		for (Section ts : tiRexSectionList) {

			//xcl = xcl + ts.getOriginalText() + "\n";
			xcl = this.analyseTiRexSections(ts, parameterMap);

			// Delete the TiRexSection from the Article
			KnowWEArticleManager manager = KnowWEEnvironment.getInstance().
												getArticleManager(parameterMap.getWeb());
			manager.replaceKDOMNode(
					parameterMap, parameterMap.get(KnowWEAttributes.TOPIC_FOR_XCL),
					ts.getId(), xcl);
		}

		return "Please restart this Page to make changes visible";
	}

	/**
	 * Returns all TiRexLines in an Article.
	 * 
	 * @param map
	 * @return
	 */
	private LinkedList <Section> getAllTiRexSections(KnowWEParameterMap map) {

		// Get Sections of the current Topic.
		KnowWEArticle art = KnowWEEnvironment.getInstance().
								getArticle(map.getWeb(),
										map.get(KnowWEAttributes.TOPIC_FOR_XCL));
		List <Section> secs = art.getSection().getChildren();

		// Get all TiRex Sections.
		ListIterator <Section> secsIt = secs.listIterator();
		LinkedList <Section> tiRexSectionList = new LinkedList <Section>();

		while (secsIt.hasNext()) {
			Section s = secsIt.next();

			if (s.getObjectType() instanceof TiRex) {
				tiRexSectionList.add(s);
			}
			
		}

		return tiRexSectionList;
	}

	/**
	 * Returns a KnowledgeBase a given Section lies in.
	 * 
	 * @param map
	 * @param section
	 */
	private KnowledgeBase getKnowledgeBaseForSection(KnowWEParameterMap map, Section section) {

		List <Section> lines = section.getChildren();

		for (Section s : lines) {
			if (s.getObjectType() instanceof TiRexParagraph) {
				if (((TiRexParagraph) s.getObjectType()).getRelationInfo(s) != null) {
					XCLRelationInfo info = ((TiRexParagraph) s.getObjectType()).
															getRelationInfo(s);
					return ((D3webKnowledgeService) D3webModule.getInstance().
							getKnowledgeService(map.getWeb(),
									info.getKbid())).getBase();
				}
			}
		}
		return null;
	}

	/**
	 * Returns a List of Question/Answer arrays.
	 * 
	 * @param section
	 * @param map
	 */
	private String analyseTiRexSections(Section section, KnowWEParameterMap map) {
		StringBuffer buffi = new StringBuffer("<XCL>\n");

		// Get Head/Body/Tail and read out the Information
		List <Section> secList = section.getChildren();
		
		// First get the name of the solution
		SolutionContext solutioncontext=((SolutionContext)ContextManager.getInstance().getContext(section, SolutionContext.CID));
		
		String solName = solutioncontext.getSolution();

		// Get KnowledgeBase	
		KnowledgeBase base = this.getKnowledgeBaseForSection(map, secList.get(1));

		// Check for the right XCLModel
		Collection<KnowledgeSlice> slices = base.getAllKnowledgeSlices();

		for (KnowledgeSlice knowledgeSlice : slices) {
			if (knowledgeSlice instanceof XCLModel) {
				XCLModel model = (XCLModel) knowledgeSlice;
				if (model.getSolution().getText().equals(solName)) {

					// Open the Model Presentation
					buffi.append(solName + " {\n");

					// get all Relations from TiRexBody
					List <Section> lines = secList.get(1).getChildren();

					// find Relations in the Model and append the comment
					for (Section se : lines) {
						if (((TiRexParagraph) se.getObjectType()).getRelationInfo(se) != null) {

							// verbalize the relations and append to buffi
							ExplainsRelationVerbalizer verb = new ExplainsRelationVerbalizer();
							XCLRelation rel = model.
												findRelation(((TiRexParagraph) se.getObjectType()).
														getRelationInfo(se).getId());
							verb.verbalizeExplainsRelation(rel, buffi);
							buffi.append("//" + se.getOriginalText().substring(2));

						} else {
							if (se.getOriginalText().length() > 2) {
								buffi.append("//" + se.getOriginalText().substring(1));
							}
						}
					}

					// end the Model Presentation
					buffi.append("}");
				}
			}
		}

		buffi.append("\n</XCL>");

		// Append the content of the TiRexLines as comments
		return buffi.toString();
	}

}
