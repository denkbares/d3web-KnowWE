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

package de.knowwe.core.kdom.parsing;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.sectionFinder.SectionFinderResult;

@FunctionalInterface
public interface SectionizerModule {

	/**
	 * Created a new section of the specified type, within the specified parent section, for the secified text (full
	 * text of the parent section) and the specified sub-range. The method returns null if this sectionizer module is
	 * not capable (or not willing) to create a section for the specified range, text, and/or type.
	 *
	 * @param parent     parent section to create the new section as a child
	 * @param parentText the plain text of the parent section
	 * @param childType  the type if the child section to be created
	 * @param range      the text range in the parent text to create the child section for
	 */
	@Nullable
	Section<?> createSection(Section<?> parent, String parentText, Type childType, SectionFinderResult range);

	/**
	 * Utility class to manage the registered / plugged sectionizer modules
	 */
	class Registry {

		private static final List<SectionizerModule> sectionizerModules = new ArrayList<>();

		public static void register(SectionizerModule sectionizerModule) {
			sectionizerModules.add(sectionizerModule);
		}

		/**
		 * Created a new section of the specified type, within the specified parent section, for the secified text (full
		 * text of the parent section) and the specified sub-range.
		 *
		 * @param parent     parent section to create the new section as a child
		 * @param parentText the plain text of the parent section
		 * @param childType  the type if the child section to be created
		 * @param range      the text range in the parent text to create the child section for
		 */
		public static void createSection(Section<?> parent, String parentText, Type childType, SectionFinderResult range) {
			// try eventually plugged sectionizer modules to create the section
			String sectionText = parentText.substring(range.getStart(), range.getEnd());
			for (SectionizerModule sModule : sectionizerModules) {
				Section<?> child = sModule.createSection(parent, sectionText, childType, range);
				if (child != null) return;
			}

			// if no valid section craeted by a plugged sectionizer module, use the default module,
			// that always creates a valid section
			DefaultSectionizerModule.getInstance().createSection(parent, sectionText, childType, range);
		}
	}
}
