/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 * Computer Science VI, University of Wuerzburg
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package de.d3web.we.terminology.term;

import java.util.List;

import de.d3web.utilities.ISetMap;
import de.d3web.we.alignment.GlobalAlignment;
import de.d3web.we.basic.TerminologyType;
import de.d3web.we.terminology.global.GlobalTerminology;
import de.d3web.we.terminology.local.LocalTerminologyAccess;

public interface TermFactory<T, E> {

	public List<GlobalAlignment> getAlignableTerms(E obj, String idString, GlobalTerminology gt);

	public Term getTerm(E object, TerminologyType type, GlobalTerminology gt);

	public ISetMap<E, Term> addTerminology(LocalTerminologyAccess<E> localTerminology, String idString, GlobalTerminology globalTerminology);
}
