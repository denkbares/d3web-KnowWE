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

package de.d3web.we.ci4ke.util;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Date;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class JDOMTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		Element root = new Element("builds");
		root.setAttribute("article", "CarDiagnosis");
		
		for(int i=10; i>0; i--){
			Element build = new Element("build");
			build.setAttribute("Nr",String.valueOf(i));
			build.setAttribute("executed",new Date().toString());
			
			for(int j=1; j<=4; j++){
				Element test = new Element("test");
				test.setAttribute("name","CITest"+j);
				test.setAttribute("result","SUCCESSFUL");
				build.addContent(test);
			}
			
			root.addContent(build);
		}
		
		
		Document doc = new Document(root);
		XMLOutputter out = new XMLOutputter( Format.getPrettyFormat() ); 
		try {
			out.output( doc, System.out );
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//	    Element root = new Element("Fibonacci_Numbers");
//
//	    BigInteger low  = BigInteger.ONE;
//	    BigInteger high = BigInteger.ONE;
//
//	    for (int i = 1; i <= 5; i++) {
//	      Element fibonacci = new Element("fibonacci");
//	      fibonacci.setAttribute("index", String.valueOf(i));
//	      fibonacci.setText(low.toString());
//	      root.addContent(fibonacci);
//
//	      BigInteger temp = high;
//	      high = high.add(low);
//	      low = temp;
//	    }
//
//	    Document doc = new Document(root);
//	    // serialize it onto System.out
//	    try {
//	      XMLOutputter serializer = new XMLOutputter();
//	      serializer.output(doc, System.out);
//	    }
//	    catch (IOException e) {
//	      System.err.println(e);
//	    }
		

	}

}
