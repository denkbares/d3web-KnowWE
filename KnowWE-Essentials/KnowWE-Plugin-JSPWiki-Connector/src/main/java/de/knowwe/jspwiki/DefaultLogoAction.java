/*
 * Copyright (C) 2024 denkbares GmbH. All rights reserved.
 */

package de.knowwe.jspwiki;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.denkbares.utils.Streams;
import de.knowwe.core.action.AbstractAction;

import de.knowwe.core.action.UserActionContext;

/**
 * Default action for retrieving the correct KnowWE logo path for wiki
 * If individual logo is used, override getter methods. Logos should be placed in resource folder.
 * Extension in plugin.xml needs to be set to priority 4 for inheriting classes.
 * Refer to KnowSECLogoAction as implementation example
 *
 * @author Antonia Heyder (denkbares GmbH)
 * @created 03.06.24
 */
public class DefaultLogoAction extends AbstractAction {

	private static final String LOGO_PATH_DARK_MODE = "/webapp/KnowWEExtension/images/knowwe-logo_dark-mode.svg";
	private static final String LOGO_PATH_LIGHT_MODE = "/webapp/KnowWEExtension/images/knowwe-logo_light-mode.svg";

	@Override
	public void execute(UserActionContext context) throws IOException {
		String logoPath = getLogoPath(context);
		setLogoContentType(context);
		setCharsetEncoding(context);
		try (InputStream logoStream = getClass().getResourceAsStream(logoPath)) {
			try (OutputStream logoStreamOut = context.getResponse().getOutputStream()) {
				if (logoStream != null) {
					Streams.stream(logoStream, logoStreamOut);
				}
			}
		}
		setLogoContentType(context);
		setCharsetEncoding(context);
	}

	/**
	 * Retrieves the path of the logo file based on the specified display mode (Light-mode per default).
	 *
	 * @param context the user action context containing request parameters
	 * @return the path of the logo file
	 */
	private String getLogoPath(UserActionContext context) {
		String logoText = getDefaultLogoPath();
		String displayMode = context.getParameter("displayMode");
		if ("dark-mode".equals(displayMode.replace("'", ""))) {
			String darkModeLogoPath = getDarkModeLogoPath();
			if (darkModeLogoPath != null) {
				logoText = darkModeLogoPath;
			}
		}
		return logoText;
	}

	/**
	 * Gets the path of the light-mode logo file.
	 *
	 * @return the path of the default light-mode logo file
	 */
	public String getDefaultLogoPath() {
		return LOGO_PATH_LIGHT_MODE;
	}

	/**
	 * Gets the path of the default dark-mode logo file.
	 * If no dark-mode specific file exists, just call default method.
	 *
	 * @return the path of the default dark-mode logo file
	 */
	public String getDarkModeLogoPath() {
		return LOGO_PATH_DARK_MODE;
	}

	/**
	 * Sets the content type of the logo image response to "image/svg+xml".
	 * To be overridden in case of different type (e.g. image/png)
	 *
	 * @param context the user action context for setting response content type
	 */
	public void setLogoContentType(UserActionContext context) {
		context.getResponse().setContentType("image/svg+xml");
	}

	/**
	 * Sets the charset encoding for the response. Needs to be set null when png is used!
	 *
	 * @param context the user action context for setting charset encoding
	 */
	public void setCharsetEncoding(UserActionContext context) {
	}
}

