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

public class ParserConstants {

	public static final int _SHAPE_PROCESS = 0;
	public static final int _SHAPE_DECISION = 1;
	public static final int _SHAPE_TERMINATOR = 2;
	public static final int _SHAPE_CONNECTOR = 3;
	
	public static final int _SHAPE_UNKNOWN = -1;
	
	public static int shapeNameToConstant(String shapeType)
	{
		if(shapeType.equals("Process") || shapeType.equals("Prozess"))
			return _SHAPE_PROCESS;
		else if(shapeType.equals("Decision") || shapeType.equals("Entscheidung"))
			return _SHAPE_DECISION;
		else if(shapeType.equals("Terminator") || shapeType.equals("Ende"))
			return _SHAPE_TERMINATOR;		
		else if(shapeType.equals("Dynamic connector") || shapeType.equals("Dynamischer Verbinder"))
			return _SHAPE_CONNECTOR;
		else
			return _SHAPE_UNKNOWN;		
	}
	
	public static String shapeConstantToName(int shapeType)
	{
		switch(shapeType)
		{
		case _SHAPE_PROCESS:
			return "_SHAPE_PROCESS";
		case _SHAPE_DECISION:
			return "_SHAPE_DECISION";
		case _SHAPE_TERMINATOR:
			return "_SHAPE_TERMINATOR";
		case _SHAPE_CONNECTOR:
			return "_SHAPE_CONNECTOR";
		default:
			return "_SHAPE_UNKNOWN";
		}
	}
	
}
