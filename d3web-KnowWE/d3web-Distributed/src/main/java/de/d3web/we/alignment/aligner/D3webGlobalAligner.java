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

package de.d3web.we.alignment.aligner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import de.d3web.kernel.domainModel.Diagnosis;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.supportknowledge.Property;
import de.d3web.we.alignment.AlignmentUtilRepository;
import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.alignment.NumericalIdentity;
import de.d3web.we.alignment.SolutionIdentity;
import de.d3web.we.alignment.method.AlignMethod;
import de.d3web.we.alignment.type.AbstractAlignType;
import de.d3web.we.alignment.type.NoAlignType;
import de.d3web.we.alignment.type.NumericalIdentityAlignType;
import de.d3web.we.alignment.type.SolutionIdentityAlignType;
import de.d3web.we.basic.IdentifiableInstance;
import de.d3web.we.terminology.term.Term;
import de.d3web.we.terminology.term.TermInfoType;

/**
 * 
 * @deprecated
 *
 */
public class D3webGlobalAligner implements GlobalAligner<NamedObject> {

	public List<GlobalAlignment> align(Term term, NamedObject object, String idString) {
		List<GlobalAlignment> result = new ArrayList<GlobalAlignment>();
//		Boolean privat = (Boolean) object.getProperties().getProperty(Property.PRIVATE);
//		if(privat != null && privat) return result;
		Collection<AlignMethod> methods = AlignmentUtilRepository.getInstance().getMethods(String.class);
		for (AlignMethod method : methods) {
			AbstractAlignType type = method.align(term.getInfo(TermInfoType.TERM_NAME), object.getText());
			if(!(type instanceof NoAlignType)) {
				GlobalAlignment globalAlignment = new GlobalAlignment(term, new IdentifiableInstance(idString, object.getId(), null), type);
				Object obj = object.getProperties().getProperty(Property.FOREIGN);
				if(obj != null && obj instanceof Boolean && ((Boolean)obj).booleanValue()){
					globalAlignment.setProperty("visible", Boolean.FALSE);
				}
				result.add(globalAlignment);
				// "values":
				if(object instanceof Diagnosis) {
					GlobalAlignment globalAlignment1 = new GlobalAlignment(term, new IdentifiableInstance(idString, object.getId(), new SolutionIdentity()), SolutionIdentityAlignType.getInstance());
					GlobalAlignment globalAlignment2 = new GlobalAlignment(term, new IdentifiableInstance(idString, object.getId(), new NumericalIdentity()), NumericalIdentityAlignType.getInstance());
					result.add(globalAlignment1);
					result.add(globalAlignment2);
				}
			}
		}
		Collections.sort(result);
		return result;
	}

}
