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

package de.d3web.textParser.visioReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VisioTree {

//	public VisioTree links;//Antwort JA
//	public VisioTree rechts;//Antwort nein
	public ArrayList<VisioTree> childs;
	private String frage;//Frage
	
	//Caption of the path leading to this Tree
	public String pathLabel;
	
	private int depth;
	
	public int shapeType;
	
	public VisioTree(){}
	
	public VisioTree(String str)
	{
		this.frage = str;
		this.childs = new ArrayList<VisioTree>();
		this.depth = 0;
		this.pathLabel="";
		shapeType = -1;
	}
	
	public VisioTree(String str, int depth, String pathLabel, int shapeType)
	{
		this.frage = str;
		this.childs = new ArrayList<VisioTree>();
		this.depth = depth;
		this.pathLabel = pathLabel;
		this.shapeType = shapeType;
	}	
	
//	//kann auch wieder eine Frage sein
//	public void antwortJaEinfuegen(String antwort)
//	{
//		this.links = new VisioTree(antwort, (depth+1));
//	}
//
//	//kann auch wieder eine Frage sein
//	public void antwortNeinEinfuegen(String antwort)
//	{
//		this.rechts = new VisioTree(antwort, (depth+1));
//	}
	
	public void collectDiagnoses(TreeSet<String> ts)
	{
		if(this.childs.size() == 0 && (this.shapeType == ParserConstants._SHAPE_TERMINATOR ))
		{
			ts.add(cleanDiagnosisString(this.frage));
		}
		else
		{
			Iterator it = childs.iterator();
			while(it.hasNext())
			{
				VisioTree child = (VisioTree)it.next();
				child.collectDiagnoses(ts);
			}
		}
	}
	
	private static String cleanDiagnosisString(String dirty)
	{
		int lastOpenedBracket = dirty.lastIndexOf("(");
		return dirty.substring(0,lastOpenedBracket-1);
	}
	
	public void collectQuestionsheets(TreeSet<String> ts)
	{
		if((this.shapeType == ParserConstants._SHAPE_PROCESS ))
		{
			ts.add(this.frage);
		}

		Iterator it = childs.iterator();
		while(it.hasNext())
		{
			VisioTree child = (VisioTree)it.next();
			child.collectQuestionsheets(ts);
		}
	}	

	
	public void printFragebaum(PrintWriter pW)
	{
		for(int i=0; i<((2*depth)-1 < 0 ? 0 : (2*depth)-1); i++)
			pW.print("-");
		if((2*depth)-1 > 0)
			pW.print(" ");
		if(this.shapeType == ParserConstants._SHAPE_PROCESS)
			pW.println(stripSquaredBrackets(frage));
		else
			pW.println(frage);
		
		Iterator it = childs.iterator();
		while(it.hasNext())
		{
			VisioTree child = (VisioTree)it.next();
			if(child.pathLabel != "")
			{
				for(int i=0; i<(2*depth); i++)
					pW.print("-");
				pW.println(" "+child.pathLabel);
			}			
			child.printFragebaum(pW);
		}
	}
	
	private static String stripSquaredBrackets(String dirty)
	{
		Pattern p = Pattern.compile(".*\\[.\\]");
		Matcher m = p.matcher(dirty);
		if(m.matches())
		{
			int lastBracket = dirty.lastIndexOf("[");
			return dirty.substring(0, lastBracket).trim();
		}
		return dirty;
	}
	
	public void printFragebaum(PrintWriter pW, String worksheetname, String sourcefilepath)
	{
		try
		{
			
			pW.println("// imported from Source-File: "+sourcefilepath);
			pW.println("// imported from Worksheet: "+worksheetname);
			printFragebaum(pW);
			pW.println("");

			
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public void printOutputFiles(PrintWriter[] outputWriters, String worksheetname, String sourcefilepath)
	{
		try
		{
			//outputWriters[0]: Fragebogen-Hierachie
			//outputWriters[1]: Diagnosen-Hierachie
			//outputWriters[2]: Fragebaum
			
			//outputWriters[0].println("// imported from Worksheet: "+worksheetname);
			//outputWriters[1].println("// imported from Worksheet: "+worksheetname);
			outputWriters[2].println("// imported from Source-File: "+sourcefilepath);
			outputWriters[2].println("// imported from Worksheet: "+worksheetname);
			
			
			//printDiagnosehierachie(outputWriters[1]);
			printFragebaum(outputWriters[2]);
			
			outputWriters[0].println("");
			//outputWriters[1].println("");
			outputWriters[2].println("");
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
