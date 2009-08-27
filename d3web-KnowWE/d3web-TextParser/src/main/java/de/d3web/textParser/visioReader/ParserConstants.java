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
