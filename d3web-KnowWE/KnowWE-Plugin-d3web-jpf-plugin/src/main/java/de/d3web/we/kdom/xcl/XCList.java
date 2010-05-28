/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package de.d3web.we.kdom.xcl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.RegexSectionFinder;
import de.d3web.we.utils.Patterns;

public class XCList extends DefaultAbstractKnowWEObjectType {
	
	@Override
	public void init() {
		this.sectionFinder = new RegexSectionFinder(Patterns.XCLIST, Pattern.MULTILINE);
		childrenTypes.add(new XCLHead());
		childrenTypes.add(new XCLTail());
		childrenTypes.add(new XCLBody());
	}
	
	
	
	public static void main(String[] args) {
		
		String text = readTxtFile("C:\\Users\\Public\\Documents\\Test.txt");
		Pattern pattern = Pattern.compile(Patterns.XCLIST, Pattern.MULTILINE);
		Matcher m = pattern.matcher(text);	
		
		while (m.find()) {
			System.out.println(m.group());
		}
		
	}
	
	
	public static String readTxtFile(String fileName) {
		StringBuffer inContent = new StringBuffer();
		try {
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(new FileInputStream(fileName), "UTF8"));
			int char1 = bufferedReader.read();
			while (char1 != -1) {
				inContent.append((char) char1);
				char1 = bufferedReader.read();
			}
			bufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inContent.toString();
	}
}
