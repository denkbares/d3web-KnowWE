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

package utilities;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedList;

public class Utilities {

    public static String readBytes(Reader r) {
        int zeichen = 0;
        LinkedList<Integer> ints = new LinkedList<Integer>();
        while (true) {

            try {
                zeichen = r.read();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                break;
            } catch (OutOfMemoryError e1) {
                break;
            }
            
            // Ende des Stream erreicht
            if (zeichen == -1) {                
                break;
            }
            
            ints.add(zeichen);
        }

        StringBuilder buffi = new StringBuilder(5000000);
        for (Integer i : ints) {
            
            if ((i.intValue() == 128) || (i.intValue() == 228)
                    || (i.intValue() == 252) || (i.intValue() == 246)
                    || (i.intValue() == 214) || (i.intValue() == 196)
                    || (i.intValue() == 220) || (i.intValue() == 223)) {
                if (i.intValue() == 128) {
                    buffi.append('');
                }
                if (i.intValue() == 228) {
                    buffi.append('ä');
                }
                if (i.intValue() == 252) {
                    buffi.append('ü');
                }
                if (i.intValue() == 246) {
                    buffi.append('ö');
                }
                if (i.intValue() == 214) {
                    buffi.append('ü');
                }
                if (i.intValue() == 196) {
                    buffi.append('Ö');
                }
                if (i.intValue() == 220) {
                    buffi.append('Ü');
                }
                if (i.intValue() == 223) {
                    buffi.append('ß');
                }
            } else {
                buffi.append(((char) i.intValue()));
            }
        }
        return buffi.toString();
    }
    
    public static String ReaderToString(Reader r) {
        return readBytes(r).replace('@', '%');
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
