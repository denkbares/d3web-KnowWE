/*
 * Copyright (C) 2014 University Wuerzburg, Computer Science VI
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
package de.knowwe.testcases;

import de.d3web.testcase.model.TestCase;
import com.denkbares.utils.Triple;
import de.knowwe.core.compile.packaging.PackageCompileType;
import de.knowwe.core.kdom.parsing.Section;

/**
 * Triple providing a {@link TestCaseProvider}, the Section of the provider and
 * the section of type {@link PackageCompileType} providing the knowledge base
 * for the {@link TestCase}
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 19.01.2014
 */
public class ProviderTriple extends Triple<TestCaseProvider, Section<?>, Section<? extends PackageCompileType>> {

	public ProviderTriple(TestCaseProvider provider, Section<?> providerSection, Section<? extends PackageCompileType> kbSection) {
		super(provider, providerSection, kbSection);
	}

	public TestCaseProvider getProvider() {
		return getA();
	}

	public Section<?> getProviderSection() {
		return getB();
	}

	public Section<? extends PackageCompileType> getKbSection() {
		return getC();
	}

}
