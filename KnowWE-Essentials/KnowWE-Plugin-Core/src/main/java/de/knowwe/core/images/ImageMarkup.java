/*
 * Copyright (C) 2023 denkbares GmbH, Germany
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

package de.knowwe.core.images;

import de.knowwe.core.compile.packaging.PackageManager;
import de.knowwe.core.kdom.basicType.AttachmentType;
import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;

/**
 * Markup for images elements.
 * It defines specific annotations relevant for images, such as source, width, height, alternative text, style, ID,
 * class, and caption.
 *
 * @author Antonia Heyder (denkbares GmbH)
 * @created 08.11.2023
 */

public class ImageMarkup extends DefaultMarkupType {

	private static final String ANNOTATION_SRC = "src";
	private static final String ANNOTATION_WIDTH = "width";
	private static final String ANNOTATION_HEIGHT = "height";
	private static final String ANNOTATION_ALT = "alt";
	static final String ANNOTATION_STYLE = "style";
	private static final String ANNOTATION_ID = "id";
	private static final String ANNOTATION_CLASS = "class";
	static final String ANNOTATION_CAPTION = "caption";
	static final String ANNOTATION_ALIGN = "align";
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("Image");
		MARKUP.addAnnotation(ANNOTATION_SRC, true);
		MARKUP.addAnnotationContentType(ANNOTATION_SRC, new AttachmentType());
		MARKUP.addAnnotation(ANNOTATION_WIDTH, false);
		MARKUP.addAnnotation(ANNOTATION_HEIGHT, false);
		MARKUP.addAnnotation(ANNOTATION_ALT, false);
		MARKUP.addAnnotation(ANNOTATION_STYLE, false);
		MARKUP.addAnnotation(ANNOTATION_ID, false);
		MARKUP.addAnnotation(ANNOTATION_CLASS, false);
		MARKUP.addAnnotation(ANNOTATION_CAPTION, false);
		MARKUP.addAnnotation(ANNOTATION_ALIGN, false);
		PackageManager.addPackageAnnotation(MARKUP);
	}

	public ImageMarkup() {
		super(MARKUP);
		this.setRenderer(new ImageRenderer());
	}
}
