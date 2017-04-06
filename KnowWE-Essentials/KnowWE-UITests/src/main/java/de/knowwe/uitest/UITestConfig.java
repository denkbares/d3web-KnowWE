/*
 * Copyright (C) 2017 denkbares GmbH, Germany
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

package de.knowwe.uitest;

import org.openqa.selenium.Platform;

/**
 * Edit class text
 *
 * @author Jonas Müller
 * @created 17.02.17
 */
public class UITestConfig {

	private final UITestUtils.Browser browser;
	private final Platform os;

	public UITestConfig(UITestUtils.Browser browser, Platform os, WikiTemplate template) {
		this.browser = browser;
		this.os = os;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		UITestConfig that = (UITestConfig) o;

		if (browser != null ? browser != that.browser : that.browser != null) return false;
		return os == that.os;
	}

	@Override
	public int hashCode() {
		int result = browser != null ? browser.hashCode() : 0;
		result = 31 * result + (os != null ? os.hashCode() : 0);
		return result;
	}
}
