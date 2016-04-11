/*
 * Copyright (C) 2013 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.objectinfo;

import de.knowwe.kdom.defaultMarkup.DefaultMarkup;
import de.knowwe.kdom.defaultMarkup.DefaultMarkupType;


/**
 * 
 * @author stefan
 * @created 09.12.2013
 */
public class ObjectInfoType extends DefaultMarkupType {

	/**
	 * @param markup
	 */
	private static final DefaultMarkup MARKUP;

	static {
		MARKUP = new DefaultMarkup("ObjectInfo");
	}

	public ObjectInfoType() {
		super(MARKUP);
		setRenderer(new ObjectInfoRenderer());
	}
}
