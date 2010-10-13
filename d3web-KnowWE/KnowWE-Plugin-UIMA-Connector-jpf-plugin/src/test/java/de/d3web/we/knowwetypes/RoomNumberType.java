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

package de.d3web.we.knowwetypes;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.knowweobjecttypes.FeatureImplementation;
import de.d3web.we.sectionfinder.FeatureSectionFinder;
import de.d3web.we.sectionfinder.TypeSectionFinder;

public class RoomNumberType extends DefaultAbstractKnowWEObjectType {

	@Override
	protected void init() {
		FeatureImplementation fI = new FeatureImplementation();
		fI.setSectionFinder(
				new FeatureSectionFinder(
						"org.apache.uima.tutorial.RoomNumber:building", false));
		this.childrenTypes.add(fI);
		this.sectionFinder = new TypeSectionFinder(
				"org.apache.uima.tutorial.RoomNumber",false);
		this.childrenTypes.add(new BuildingType());
	}
}
