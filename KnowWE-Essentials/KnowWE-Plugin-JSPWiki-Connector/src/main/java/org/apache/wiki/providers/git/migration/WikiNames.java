/*
 * Copyright (C) 2026 denkbares GmbH, Germany
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

package org.apache.wiki.providers.git.migration;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * The file name encoding the wiki uses for page and attachment names, reimplemented so the migration stays free of
 * wiki dependencies and can run without a deployed wiki.
 * <p>
 * It is the wiki's own percent encoding, not the one of {@code java.net}, which differs in the characters it leaves
 * alone. The migration needs it only to tell whether a name on disk is already in the form the git provider will look
 * for, because a name written by an older provider would otherwise be replayed to a path nothing ever reads.
 */
final class WikiNames {

	private static final String HEX_DIGITS = "0123456789ABCDEF";

	private WikiNames() {
	}

	/**
	 * Encodes a wiki name into the file name the git provider reads.
	 */
	static String encode(String name) {
		StringBuilder encoded = new StringBuilder(name.length() * 2);
		for (byte value : name.getBytes(StandardCharsets.UTF_8)) {
			char character = (char) (value & 0xFF);
			if (character == '_' || character == '.' || character == '*' || character == '-' || character == '/'
					|| (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z')
					|| (character >= '0' && character <= '9')) {
				encoded.append(character);
			}
			else if (character == ' ') {
				encoded.append('+');
			}
			else {
				encoded.append('%').append(HEX_DIGITS.charAt((character & 0xF0) >> 4)).append(HEX_DIGITS.charAt(character & 0x0F));
			}
		}
		return encoded.toString();
	}

	/**
	 * Decodes a file name back into the wiki name it stands for.
	 */
	static String decode(String fileName) {
		ByteArrayOutputStream decoded = new ByteArrayOutputStream(fileName.length());
		for (int index = 0; index < fileName.length(); index++) {
			char character = fileName.charAt(index);
			if (character == '+') {
				decoded.write(' ');
			}
			else if (character == '%' && index + 2 < fileName.length()) {
				int high = HEX_DIGITS.indexOf(Character.toUpperCase(fileName.charAt(++index)));
				int low = HEX_DIGITS.indexOf(Character.toUpperCase(fileName.charAt(++index)));
				if (high < 0 || low < 0) {
					return fileName;
				}
				decoded.write((high << 4) + low);
			}
			else {
				decoded.write(character);
			}
		}
		return decoded.toString(StandardCharsets.UTF_8);
	}

	/**
	 * The name the git provider would use for the same file, which differs from the given one where an older provider
	 * encoded it differently.
	 */
	static String normalize(String fileName) {
		return encode(decode(fileName));
	}
}
