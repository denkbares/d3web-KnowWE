package de.d3web.we.kdom.xcl;

import java.util.Collection;
import java.util.List;

import de.d3web.kernel.XPSCase;
import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.DiagnosisState;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.xclPattern.PSMethodXCL;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.rendering.KnowWEDomRenderer;
import de.d3web.we.utils.D3webUtils;
import de.d3web.we.wikiConnector.KnowWEUserContext;

public class XCLHeadHighlightingRenderer extends KnowWEDomRenderer {

	private static XCLHeadHighlightingRenderer instance;
	
	@Override
	public void render(Section sec, KnowWEUserContext user, StringBuilder string) {
		String solution = sec.getOriginalText().replace("\"", "").trim();

		XPSCase xpsCase = D3webUtils.getXPSCase(sec, user);
		
		if (xpsCase != null) {
			
			List<Diagnosis> diags = xpsCase.getKnowledgeBase().getDiagnoses();
			Collection <KnowledgeSlice> slices =
				xpsCase.getKnowledgeBase().getAllKnowledgeSlicesFor(PSMethodXCL.class);
			
			// Some String definitions
			String spanStart = "<span style=\"background-color: rgb(";
			String spanStartEnd = ";\">";
			String spanEnd = "</span>";

			for (Diagnosis d : diags) {

				if (d.getText().equals(solution)) {
					DiagnosisState state; 
					XCLModel diagModel = this.findModel(solution, slices);
					
					if (diagModel == null)
						state = DiagnosisState.UNCLEAR;
					else
						state = diagModel.getState(xpsCase);
						
					solution += " ";

					if (state == DiagnosisState.ESTABLISHED) {
						string.append(spanStart + "51, 255, 51)" + spanStartEnd
								+ solution + spanEnd);
					}

					if (state == DiagnosisState.EXCLUDED) {
						string.append(spanStart + "255, 153, 0)" + spanStartEnd
								+ solution + spanEnd);
					}

					if (state == DiagnosisState.SUGGESTED) {
						string.append(spanStart + "251, 199, 11)" + spanStartEnd
								+ solution + spanEnd);
					}

					if (state == DiagnosisState.UNCLEAR) {
						string.append(solution);
					}
				}

			}
		} else {
			string.append(solution);
		}

	}
	
	/**
	 * Finds a Model from a KnowledgeSlice list.
	 * 
	 * @param solution
	 * @return
	 */
	private XCLModel findModel(String solution, Collection<KnowledgeSlice> slices) {
		for (KnowledgeSlice s : slices) {
			if (((XCLModel)s).getSolution().getText().equals(solution))
				return (XCLModel)s;
		}
		return null;
	}

	/**
	 * Singleton.
	 * 
	 * @return
	 */
	public static XCLHeadHighlightingRenderer getInstance() {
		if (instance == null)
			instance = new XCLHeadHighlightingRenderer();
		
		return instance;
	}

}
