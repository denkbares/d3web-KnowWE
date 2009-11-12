/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.KnowledgeSlice;
import de.d3web.kernel.psMethods.MethodKind;
import de.d3web.kernel.psMethods.heuristic.PSMethodHeuristic;
import de.d3web.we.core.DPSEnvironment;
import de.d3web.we.core.KnowWEAttributes;
import de.d3web.we.core.KnowWEParameterMap;
import de.d3web.we.core.knowledgeService.D3webKnowledgeService;
import de.d3web.we.core.knowledgeService.KnowledgeService;
import de.d3web.we.d3webModule.D3webModule;
import de.d3web.we.d3webModule.ProblemSolverType;

public class ExplanationRenderer2 extends AbstractKnowWEAction {


	public String perform(KnowWEParameterMap parameterMap) {
		
		String toExplain = parameterMap.get(KnowWEAttributes.EXPLAIN);
//		String kbid = parameterMap.get(KnowWEAttributes.NAMESPACE);
		String typeString = parameterMap.get(KnowWEAttributes.PROBLEM_SOLVER_TYPE);
	
		ProblemSolverType type = null;
		if(typeString == null) {
			List<ProblemSolverType> types = getProblemSolverTypes(parameterMap, toExplain);
			if(types != null && !types.isEmpty()) {
				type = types.get(0);
			}
		} else {
			type = ProblemSolverType.getType(typeString);
			
		}
		if(type == null) type = ProblemSolverType.heuristic;
		try { 
		if(type.equals(ProblemSolverType.heuristic)) {
			String rendered = renderHeuristic(parameterMap,toExplain);
			return rendered;
		} 
		else if(type.equals(ProblemSolverType.setcovering)){
			String rendered = renderSCM(parameterMap,toExplain);
			return rendered;
		} 
		else if(type.equals(ProblemSolverType.casebased)){
			return "cbs explanation not ready";
//			model.setAttribute("kbid", kbid, model.getWebApp());
//			model.setAttribute("comparisontype", "simple", model.getWebApp());
//			model.setAttribute("cmode", "4", model.getWebApp());
//			model.setAttribute("diagId", toExplain, model.getWebApp());
//			List cases = new ArrayList(((D3webKnowledgeService)KnowWEUtils.getEnvironment(model).getService(kbid)).getBase().getCaseRepository("train"));
//			model.setAttribute("cases", cases, model.getWebApp());
//			model.getWebApp().getAction("comparecase").perform(model);
//			model.getWebApp().getRenderer("comparecase").render(model);
			//model.removeAttribute("cases", model.getWebApp());
		}
		} catch (Exception e){
			return e.getMessage();
		}
		return "error in ExplanationRenderer2";
	}

	
	
	public String renderHeuristic(KnowWEParameterMap map , String toExplain) throws Exception {
		
		StringBuffer sb = new StringBuffer();
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"de\">");
		sb.append("<head>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
		sb.append("<meta http-equiv=\"REFRESH\" content=\"0; url=faces/Controller;jsessionid=" + map.getSession().getId() + "?knowweexplanation=true&toexplain=" + toExplain + "&id="+map.getSession().getId()+"\">");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
//		try {
//			java.io.PrintWriter out = getHtmlPrintWriter(model);
//			out.print(sb.toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	
	public String renderSCM(KnowWEParameterMap parameterMap, String toExplain) throws Exception {
		StringBuffer sb = new StringBuffer();
		sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"de\">");
		sb.append("<head>");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />");
		sb.append("<meta http-equiv=\"REFRESH\" content=\"0; url=faces/Controller;jsessionid=" + parameterMap.getSession().getId() + "?knowwescm=true&toexplain=" + toExplain + "&id="+parameterMap.getSession().getId()+"\">");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("</body>");
		sb.append("</html>");
		return sb.toString();
//		try {
//			java.io.PrintWriter out = getHtmlPrintWriter(model);
//			out.print(sb.toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	private List<ProblemSolverType> getProblemSolverTypes(Map<String,String> map, String id) {
		List<ProblemSolverType> result = new ArrayList<ProblemSolverType>();
		String namespace = map.get(KnowWEAttributes.NAMESPACE);
		DPSEnvironment dpse = D3webModule.getDPSE(map);
		
	
		KnowledgeService ks = dpse.getService(namespace);
		if(ks instanceof D3webKnowledgeService) {
			D3webKnowledgeService d3 = (D3webKnowledgeService) ks;
			Diagnosis diag = d3.getBase().searchDiagnosis(id);
			if(diag != null) {
				List<? extends KnowledgeSlice> heu = diag.getKnowledge(PSMethodHeuristic.class, MethodKind.BACKWARD);
				if(heu != null && !heu.isEmpty()) {
					result.add(ProblemSolverType.heuristic);
				}
				if(d3.getBase().getCaseRepository("train") != null && !d3.getBase().getCaseRepository("train").isEmpty()) {
					result.add(ProblemSolverType.casebased);
				}
			}
		}
		return result;
	}

}
