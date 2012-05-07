/*
 * Copyright (C) 2012 denkbares GmbH
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
package tests;

import java.util.Arrays;

import junit.framework.TestCase;
import de.knowwe.core.compile.terminology.TermIdentifier;

/**
 * Test for the critical conversion from {@link TermIdentifier} to its external
 * form and back.
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 25.04.2012
 */
public class TermIdentifierTest extends TestCase {

	public void testFromExternalForm() {
		checkPath("");
		checkPath("\"");
		checkPath("\"", "\"");
		checkPath("\\");
		checkPath("termIdentifier");
		checkPath("termIdentifier\\");
		checkPath("\\termIdentifier");
		checkPath("\\termIdentifier\\");
		checkPath("termIdentifier\\\"");
		checkPath("\"\\termIdentifier");
		checkPath("\"\\termIdentifier\\\"");
		checkPath("termI\"dentifier");
		checkPath("termI\\dentifier");
		checkPath("termI\"den\\tifier");
		checkPath("termI\\\"den\\tifier");
		checkPath("te\\\\rmI\"den\\\\ti\\fier");
		checkPath("\"termIdenti\"fier\"");
		checkPath("termIdentifier", "\"termIdenti\"fier\"");
		checkPath(" ", "termIdentifier", "\"termIdenti\"fier\"");
		checkPath("", "termIdentifier", "\"termIdenti\"fier\"");
		checkPath("asd\\\\lök", "termIdentifier", "\"termIdenti\"fier\"");
		checkPath("asd\\\\lök", "termIdentifier", "\"termIdenti\"fier\"", "test");
		checkPath("asd\\\\lök", "termIdentifier", "\"termIdenti\"fier\"", " ");
		checkPath("asd\\\\lök", "termIdentifier", "\"termIdenti\"fier\"", "");
	}

	private void checkPath(String... pathElements) {
		TermIdentifier termIdentifier = new TermIdentifier(pathElements);
		String externalForm = termIdentifier.toString();
		TermIdentifier fromExternalForm = TermIdentifier.fromExternalForm(externalForm);

		String listOutput = Arrays.asList(termIdentifier.getPathElements()).toString();
		String listOutPutFromExternalForm = Arrays.asList(fromExternalForm.getPathElements()).toString();

		boolean equals = listOutput.equals(listOutPutFromExternalForm);
		System.out.println("equals: " + equals + " " + listOutput + " ==> " + externalForm
				+ " ==> " + listOutPutFromExternalForm);
		assertTrue(
				"Conversion from TermIdentifier to external form and back to TermIdentifier failed:\n"
						+ listOutput + " ==> " + externalForm
						+ " ==> " + listOutPutFromExternalForm,
				equals);
	}

}
