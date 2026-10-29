/*
 * Copyright (C) 2026 denkbares GmbH, Germany
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package de.knowwe.core.compile;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests {@link Compilers#trimSharedAffixes}, used for the short compiler names.
 *
 * @author Albrecht Striffler (denkbares GmbH) + Claude
 * @created 29.10.2026
 */
public class CompilersShortNameTest {

	@Test
	public void trimsSharedPrefixAndSuffix() {
		assertEquals(List.of("city", "ebike", "mtb"), shortNames("ssm-city", "ssm-ebike", "ssm-mtb"));
		assertEquals(List.of("city", "ebike"), shortNames("Bike city (KB)", "Bike ebike (KB)"));
	}

	@Test
	public void trimsOnlyWholeParts() {
		// "mate-c" is shared, but only "mate-" is a whole part
		assertEquals(List.of("city", "cbx"), shortNames("mate-city", "mate-cbx"));
		assertEquals(List.of("city", "city-2"), shortNames("ssm-city", "ssm-city-2"));
		// "Bike" is shared, but not separated from the rest
		assertEquals(List.of("BikeCity", "BikeRacer"), shortNames("BikeCity", "BikeRacer"));
	}

	@Test
	public void keepsNamesWithoutSharedParts() {
		assertEquals(List.of("ssm-city"), shortNames("ssm-city"));
		// a name would become blank
		assertEquals(List.of("ssm-ssm", "ssm-a-ssm"), shortNames("ssm-ssm", "ssm-a-ssm"));
		assertEquals(List.of("city", "ebike"), shortNames("city", "ebike"));
	}

	private static List<String> shortNames(String... names) {
		Map<String, String> shortNames = Compilers.trimSharedAffixes(List.of(names));
		return List.of(names).stream().map(shortNames::get).toList();
	}
}
