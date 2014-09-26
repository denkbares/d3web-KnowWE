/*
 * Copyright (C) 2014 denkbares GmbH
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
package de.knowwe.ontology.ci.provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.d3web.testing.TestObjectContainer;
import de.d3web.testing.TestObjectProvider;
import de.knowwe.core.Environment;
import de.knowwe.core.compile.Compilers;
import de.knowwe.ontology.compile.OntologyCompiler;


/**
 * 
 * @author Jochen Reutelshöfer
 * @created 10.01.2014
 */
public class OntologyCompilerTestObjectProvider implements TestObjectProvider {

	@Override
	public <T> List<TestObjectContainer<T>> getTestObjects(Class<T> clazz, String name) {
		List<TestObjectContainer<T>> containers = new ArrayList<TestObjectContainer<T>>();
		if (!clazz.equals(OntologyCompiler.class)) return containers;
		Collection<OntologyCompiler> compilers = Compilers.getCompilers(
				Environment.getInstance().getArticleManager(Environment.DEFAULT_WEB),
				OntologyCompiler.class);
		for (OntologyCompiler ontologyCompiler : compilers) {
			String compilerIdentifier = ontologyCompiler.toString();
			if (compilerIdentifier.matches(name)) {
				containers.add(new TestObjectContainer<T>(compilerIdentifier,
						clazz.cast(ontologyCompiler)));
			}
		}
		return containers;
	}

}
