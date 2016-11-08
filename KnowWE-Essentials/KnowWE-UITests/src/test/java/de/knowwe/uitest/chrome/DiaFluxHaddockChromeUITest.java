/*
 * Copyright (C) 2016 denkbares GmbH, Germany
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

package de.knowwe.uitest.chrome;

import org.junit.Ignore;

import de.knowwe.uitest.WikiTemplate;

/**
 * DiaFluxSystem Test for Chrome with haddock template
 *
 * @author Jonas Müller
 * @created 13.09.16
 */
@Ignore
public class DiaFluxHaddockChromeUITest extends DiaFluxChromeUITest {
	@Override
	protected WikiTemplate getTemplate() {
		return WikiTemplate.haddock;
	}
}
