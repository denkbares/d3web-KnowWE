/*
 * Copyright (C) 2017 denkbares GmbH. All rights reserved.
 */

package de.knowwe.dialog.repository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.denkbares.utils.Log;

public class ArchiveUtils {

	private static final byte[] Hexhars = {
			'0', '1', '2', '3', '4', '5',
			'6', '7', '8', '9', 'a', 'b',
			'c', 'd', 'e', 'f'
	};

	private static String encode(byte[] b) {
		StringBuilder s = new StringBuilder(2 * b.length);
		for (int i = 0; i < b.length; i++) {
			int v = b[i] & 0xff;

			s.append((char) Hexhars[v >> 4]);
			s.append((char) Hexhars[v & 0xf]);
		}
		return s.toString();
	}

	public static String checksum(ZipFile file) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			for (Enumeration<? extends ZipEntry> entries = file.entries(); entries.hasMoreElements();) {
				ZipEntry entry = entries.nextElement();
				long crc = entry.getCrc();
				digest.update(entry.getName().getBytes());
				digest.update(String.valueOf(crc).getBytes());
			}

			return encode(digest.digest());
		}
		catch (NoSuchAlgorithmException e) {
			Log.severe(
					"no MD5 algorithm found, cannot provide checksum for incremental updates.", e);
			return null;
		}
	}
}
