/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */
package de.knowwe.dialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import de.d3web.core.knowledge.KnowledgeBase;
import de.d3web.core.manage.KnowledgeBaseUtils;

/**
 * This class represents a language (with optional quality factor) in the
 * Accept-Language HTTP-Header
 * 
 * @author Marc-Oliver Ochlast (denkbares GmbH)
 * @created 14.12.2010
 */
public class AcceptLanguage implements Comparable<AcceptLanguage> {

	/**
	 * The Locale which is represented by this object
	 */
	private final Locale locale;

	/**
	 * The optional quality factor
	 */
	private final float qualityFactor;

	public AcceptLanguage(Locale locale, float qualityFactor) {
		super();
		this.locale = locale;
		this.qualityFactor = qualityFactor;
	}

	public AcceptLanguage(String locale, float qualityFactor) {
		super();
		this.locale = parseLocaleFromHttpHeader(locale);
		this.qualityFactor = qualityFactor;
	}

	public Locale getLocale() {
		return locale;
	}

	public float getQualityFactor() {
		return qualityFactor;
	}

	/**
	 * Sorts descending!
	 */
	@Override
	public int compareTo(AcceptLanguage otherHeader) {
		return Float.compare(otherHeader.getQualityFactor(), this.getQualityFactor());
	}

	private Locale parseLocaleFromHttpHeader(String localeString) {
		if (localeString == null) {
			throw new IllegalArgumentException("localeString was null!");
		}
		//Locales in HTTP-Header are delimited by "-"
		String replaced = localeString.replaceAll("-", "_");
		//TODO: Language-codes in Accept-Language are defined by RFC3066 (two or three letters),
		// whereas Locale(String) takes ISO-639 coded languages (exactly two letters)
		return Utils.parseLocale(replaced);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + Float.floatToIntBits(qualityFactor);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AcceptLanguage other = (AcceptLanguage) obj;
		if (locale == null) {
			if (other.locale != null) {
				return false;
			}
		}
		else if (!locale.equals(other.locale)) {
			return false;
		}
		return Float.floatToIntBits(qualityFactor) == Float.floatToIntBits(other.qualityFactor);
	}

	@Override
	public String toString() {
		return "AcceptLanguage [locale=" + locale + ", qualityFactor=" + qualityFactor + "]";
	}

	/**
	 * This method parses the Accept-Language Header from the given
	 * {@link HttpServletRequest}. The returned list is ordered (descending) by
	 * the quality factors of the languages (highest quality first).
	 * 
	 * For example: <code>en-us,en;q=0.8,de-de;q=0.5,de;q=0.3</code> denotes a
	 * list of four languages:
	 * <ul>
	 * <li>en_US (no quality factor means 1.0)</li>
	 * <li>en (qf=0.8)</li>
	 * <li>de_DE (qf=0.5)</li>
	 * <li>de (qf=0.3)</li>
	 * </ul>
	 * 
	 * @created 14.12.2010
	 */
	public static List<AcceptLanguage> parseLanguagesFromHttpRequest(HttpServletRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("request was null!");
		}
		List<AcceptLanguage> acceptedLanguages = new ArrayList<>();
		String acceptLanguageHeader = request.getHeader("Accept-Language");

		// If the header isn't set
		if (acceptLanguageHeader == null) {
			return acceptedLanguages;
		}

		// accepted languages are delimited by ","
		String[] split_languages = acceptLanguageHeader.split(",");
		for (String acceptLang : split_languages) {
			// locale and quality factor (if any) are split by ";"
			String[] splitLang = acceptLang.split(";");
			if (splitLang.length == 1) {// no quality factor
				acceptedLanguages.add(new AcceptLanguage(splitLang[0], 1.0f));
			}
			else if (splitLang.length == 2) {// quality factor string starts
												// with "q="
				float qualityFactor = Float.parseFloat(splitLang[1].substring(2));
				acceptedLanguages.add(new AcceptLanguage(splitLang[0], qualityFactor));
			}
		}
		Collections.sort(acceptedLanguages);
		return acceptedLanguages;
	}

	/**
	 * Gets the initialization-language for the dialog, which is set by matching
	 * the {@link Locale}s of the {@link KnowledgeBase} with the Accept-Language
	 * of the {@link HttpServletRequest}.
	 * 
	 * @created 15.12.2010
	 * @return The {@link Locale} from the {@link KnowledgeBase} which is
	 *         best-matched by the Accept-Languages
	 */
	public static Locale getInitLocale(HttpServletRequest request, KnowledgeBase kb) {
		if (request == null || kb == null) {
			throw new IllegalArgumentException("request or kb was null!");
		}
		List<AcceptLanguage> acceptedLanguages = parseLanguagesFromHttpRequest(request);
		Set<Locale> kbLocales = KnowledgeBaseUtils.getAvailableLocales(kb);

		// iterate over AcceptLanguages which are ordered by qualityFactor
		// (descending)
		for (AcceptLanguage acceptedLanguage : acceptedLanguages) {
			Locale preferredLocale = acceptedLanguage.getLocale();
			// first check: preferred locale exactly matched in KB-locales?
			if (kbLocales.contains(preferredLocale)) {
				return preferredLocale;
			}
			// second check: preferred locale without country (this is: only
			// language!) matched in KB-locales?
			Locale preferredLanguage = new Locale(preferredLocale.getLanguage());
			if (kbLocales.contains(preferredLanguage)) {
				return preferredLanguage;
			}
			// third check: preferred language matched with locale-variant from
			// KB-Locales (e.g. preferredLocale: en_US; KB-locales contains
			// en_GB ==> match language from preferredLocale (en) with language
			// from KB-locales (en) and return: en_GB
			for (Locale kbLocale : kbLocales) {
				Locale kbLanguage = new Locale(kbLocale.getLanguage());
				if (kbLanguage.equals(preferredLanguage)) {
					return kbLocale;
				}
			}
		}
		// when no Accept-Languages are set: return first kbLocale
		Iterator<Locale> iter = kbLocales.iterator();
		if (iter.hasNext()) {
			return kbLocales.iterator().next();
		}
		// Return default Locale as last alternative:
		return Locale.getDefault();
	}
}
