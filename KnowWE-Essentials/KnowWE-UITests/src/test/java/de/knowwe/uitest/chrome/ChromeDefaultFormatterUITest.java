package de.knowwe.uitest.chrome;

import de.knowwe.uitest.WikiTemplate;

public class ChromeDefaultFormatterUITest extends ChromeFormatterUITest {

	@Override
	protected WikiTemplate getTemplate() {
		return WikiTemplate.standard;
	}
}
