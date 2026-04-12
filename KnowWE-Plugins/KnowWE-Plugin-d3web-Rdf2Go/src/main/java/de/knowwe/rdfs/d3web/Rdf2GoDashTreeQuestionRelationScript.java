/*
 * Copyright (C) 2014 denkbares GmbH, Germany
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

package de.knowwe.rdfs.d3web;

import java.util.List;

import org.eclipse.rdf4j.model.IRI;

import de.d3web.we.kdom.questionTree.QuestionTreeQuestionRelationScript;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.kdom.dashtree.DashTreeElement;
import de.knowwe.rdf2go.Rdf2GoCore;

/**
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 12.03.2014
 */
public class Rdf2GoDashTreeQuestionRelationScript extends Rdf2GoDashTreeTermRelationScript {


	@Override
	protected List<Section<DashTreeElement>> getChildrenDashtreeElements(Section<?> termDefiningSection) {
		return QuestionTreeQuestionRelationScript.getQuestionChildrenDashtreeElements(termDefiningSection);
	}

	@Override
	protected IRI getRootIRI(Rdf2GoCore core) {
		return core.createLocalIRI("Q000");
	}

}
