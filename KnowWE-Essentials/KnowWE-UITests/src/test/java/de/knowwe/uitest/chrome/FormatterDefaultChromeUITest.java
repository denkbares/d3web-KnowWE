package de.knowwe.uitest.chrome;

import de.knowwe.uitest.WikiTemplate;

public class FormatterDefaultChromeUITest extends FormatterChromeUITest {

	@Override
	protected WikiTemplate getTemplate() {
		return WikiTemplate.standard;
	}
}
