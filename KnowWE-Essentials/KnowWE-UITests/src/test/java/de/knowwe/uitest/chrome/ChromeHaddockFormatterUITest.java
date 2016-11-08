package de.knowwe.uitest.chrome;

import de.knowwe.uitest.WikiTemplate;

public class ChromeHaddockFormatterUITest extends ChromeFormatterUITest {
	@Override
	protected WikiTemplate getTemplate() {
		return WikiTemplate.haddock;
	}
}
