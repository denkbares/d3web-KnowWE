/*
 * Copyright (C) 2012 University Wuerzburg, Computer Science VI
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
package de.knowwe.core.compile.terminology;

/**
 * 
 * @author Albrecht Striffler (denkbares GmbH)
 * @created 07.02.2012
 */
public enum TermRegistrationScope {
	/**
	 * The term will be registered in the {@link TerminologyHandler} of the
	 * currently compiling master article. Terms are only matched within the
	 * packages this master article is compiling.
	 */
	LOCAL,
	/**
	 * The term will be registered in the global {@link TerminologyHandler}.
	 * Terms are not matched with any terms registered in
	 * {@link TerminologyHandler}s of master articles, but with all other terms
	 * registered in the global one.
	 */
	GLOBAL
}
