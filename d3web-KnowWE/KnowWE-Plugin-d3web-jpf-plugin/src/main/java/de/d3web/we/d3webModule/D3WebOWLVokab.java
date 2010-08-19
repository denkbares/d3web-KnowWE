/*
 * Copyright (C) 2010 Chair of Artificial Intelligence and Applied Informatics
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

package de.d3web.we.d3webModule;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public class D3WebOWLVokab {

	private static String basens = "http://ki.informatik.uni-wuerzburg.de/d3web/we/knowwe.owl#";
	public static Object FINDING;
	public static URI HASFINDING;
	public static URI CONJUNCTION;
	public static URI DISJUNCTION;
	public static URI HASTITLE;
	public static URI ANNOTATION;
	public static URI LITERAL;
	public static URI HASINPUT;
	public static URI HASCOMPARATOR;
	public static URI HASVALUE;
	public static URI COMPLEXFINDING;
	public static URI HASDISJUNCTS;
	public static URI HASCONJUNCTS;
	public static URI HASWEIGHT;
	public static URI EXPLAINS;
	public static URI ISRATEDBY;

	static {
		ValueFactory factory = ValueFactoryImpl.getInstance();
		FINDING = factory.createURI(basens, "Finding");
		HASFINDING = factory.createURI(basens, "hasFinding");
		CONJUNCTION = factory.createURI(basens, "Conjunction");
		DISJUNCTION = factory.createURI(basens, "Disjunction");
		HASTITLE = factory.createURI(basens, "hasTitle");
		ANNOTATION = factory.createURI(basens, "Annotation");
		LITERAL = factory.createURI(basens, "Literal");
		HASINPUT = factory.createURI(basens, "hasInput");
		HASCOMPARATOR = factory.createURI(basens, "hasComparator");
		HASVALUE = factory.createURI(basens, "hasValue");
		COMPLEXFINDING = factory.createURI(basens, "ComplexFinding");
		HASDISJUNCTS = factory.createURI(basens, "hasDisjuncts");
		HASCONJUNCTS = factory.createURI(basens, "hasConjuncts");
		HASWEIGHT = factory.createURI(basens, "hasWeight");
		EXPLAINS = factory.createURI(basens, "Explains");
		ISRATEDBY = factory.createURI(basens, "isRatedBy");
	}
}
