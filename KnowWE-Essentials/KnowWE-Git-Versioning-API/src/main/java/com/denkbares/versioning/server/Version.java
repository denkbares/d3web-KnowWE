/*
 * Copyright (C) 2025 denkbares GmbH. All rights reserved.
 */

package com.denkbares.versioning.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a version number of something, for instance: 3.0.4
 */
public record Version(int major, int minor, int fix, boolean isSnapshot) {

	private static final String REGEX_VERSION_FULL = "(\\d+).(\\d+).(\\d+)";
	private static final String REGEX_VERSION_SNAPSHOT = "(\\d+).SNAPSHOT";
	private static final Pattern PATTERN = Pattern.compile(REGEX_VERSION_FULL);

	@Override
	public String toString() {
		return Integer.toString(major) + "." + Integer.toString(minor) + "." + Integer.toString(fix);
	}

	public static Version fromString(String versionFull) {
		Matcher matcher = PATTERN.matcher(versionFull);
		if (!matcher.find()) {
			throw new IllegalStateException("Only version string matching the following pattern are allowed:" + REGEX_VERSION_FULL);
		}

		String majorVersionString = matcher.group(1);
		String minorVersionString = matcher.group(2);
		String fixVersion = matcher.group(3);
		return new Version(Integer.parseInt(majorVersionString), Integer.parseInt(minorVersionString), Integer.parseInt(fixVersion), false);
	}

	public static String toBranchName(String moduleName, String version) {
		return Version.fromString(version).toBranchName(moduleName);
	}

	public String toBranchName(String branchName) {
		return branchName + "_" + major();
	}

	public String toTagName() {
		return minor() + "." + fix();
	}
}
