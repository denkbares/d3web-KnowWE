/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package de.knowwe.d3web.property;

import java.util.Locale;
import java.util.Optional;

import de.d3web.core.knowledge.terminology.info.MMInfo;
import de.d3web.core.knowledge.terminology.info.Property;
import de.knowwe.core.compile.IncrementalCompiler;
import de.knowwe.core.kdom.basicType.LocaleType;
import de.knowwe.core.kdom.objects.IncrementalTerm;
import de.knowwe.core.kdom.parsing.Section;
import de.knowwe.core.kdom.parsing.Sections;
import de.knowwe.kdom.defaultMarkup.AnnotationContentType;
import de.knowwe.kdom.defaultMarkup.AnnotationType;

import static de.knowwe.core.kdom.parsing.Sections.$;

/**
 * @author Philipp Sehne (denkbares GmbH)
 * @created 11.08.26
 */
public class DescriptionObjectReference extends PropertyObjectReference {

	DescriptionObjectReference () {

	}

	@Override
	protected Property<?> getProperty(Section<PropertyObjectReference> reference) {
		Optional<Section<AnnotationType>> place = $(reference).ancestor(DescriptionMarkup.class)
				.successor(AnnotationType.class)
				.stream()
				.filter(s -> s.get().getName().equals(DescriptionMarkup.ANNOTATION_PLACE))
				.findFirst();
		if(place.isPresent()) {
			Section<AnnotationContentType> content = $(place.get()).successor(AnnotationContentType.class).getFirst();
			if (content != null && "above".equals(content.getText().trim())) {
				return MMInfo.DESCRIPTION_ABOVE;
			}
		}
		return MMInfo.DESCRIPTION;
	}

	@Override
	protected Locale getLocale(Section<PropertyObjectReference> reference) {
		return $(reference).ancestor(DescriptionMarkup.class)
				.successor(LocaleType.class)
				.map(l -> l.get().getLocale(l)).findFirst().orElse(Locale.ROOT);
	}

	@Override
	protected String getPropertyValue(Section<PropertyObjectReference> reference) {
		Section<DescriptionMarkup.DescriptionTextType> textSection = $(reference).ancestor(DescriptionMarkup.class)
				.successor(DescriptionMarkup.DescriptionTextType.class)
				.getFirst();
		if (textSection == null) return "";
		return textSection.get().getDescriptionText(textSection);
	}

	@Override
	public Sections<?> getDependingSections(IncrementalCompiler compiler, Section<IncrementalTerm> section, Class<?>... scriptFilter) {
		return $(section).ancestor(DescriptionMarkup.class);
	}
}
