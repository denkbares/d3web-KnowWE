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
package de.knowwe.include.export;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 * Special implementation of a document builder for building a cell inside of a
 * table.
 * 
 * @author Volker Belli (denkbares GmbH)
 * @created 09.02.2014
 */
public class ListBuilder extends DefaultBuilder {

	private DocumentBuilder decorate;

	public ListBuilder(DocumentBuilder decorate) {
		super(decorate);
		this.decorate = decorate;
		this.paragraph = decorate.getParagraph();
	}

	@Override
	protected XWPFParagraph createParagraph() {
		// instead of creating a new paragraph we continue
		// to use the one we are decorating, but add new CR.
		paragraph = decorate.getParagraph();
		paragraph.createRun().addCarriageReturn();
		return paragraph;
	}

	@Override
	protected Style getDefaultStyle() {
		return Style.list;
	}
}
