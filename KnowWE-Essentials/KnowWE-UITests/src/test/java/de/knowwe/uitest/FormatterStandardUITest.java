package de.knowwe.uitest;

import org.openqa.selenium.WebDriver;

/**
 * Created by ad on 13.10.16.
 */
public class FormatterStandardUITest extends FormatterChromeUITest {

	@Override
	protected WikiTemplate getTemplate() {
		return WikiTemplate.standard;
	}
}
