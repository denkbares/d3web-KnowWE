package de.d3web.textParser.visioReader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;

public class VisioXMLReader {

	//das aktuell geparste Visio XML Dokument
	private File inputFile;	
	
	//Masters Auflistung des Visio-Files
	private ArrayList<Master> masterList;
	
	private ArrayList<Connection> connectionList;
	private ArrayList<Shape> shapeList;
	
	//Das root-Element des XML Files
	private Element root;
	
	//Das aktuell zu parsende Worksheet
	private Element page;
	
	//Namespace des XML Files
	private Namespace ns;
	
	//parsed Visio XML Tree in interner Darstellung
	private VisioTree visioXMLTree;
	
	
	
	//Default Constructor
	public VisioXMLReader(){}
	
	public VisioXMLReader(File inputFile){
		try{
			this.inputFile = inputFile;
	        SAXBuilder builder = new SAXBuilder();
	        Document doc = builder.build(inputFile);
	        
	        this.root = doc.getRootElement();
	        this.ns = root.getNamespace();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void parse()
	{
		PrintWriter[] outputWriters = initOutputFiles();
		
		TreeSet<String> diagnosen = new TreeSet<String>();
		TreeSet<String> frageboegen = new TreeSet<String>();
		
		//Masters are Sheet-independent
		setMasterList();
		
		for(int i=0; i<countWorksheets(); i++)
		{
			this.page = (Element)root.getChild("Pages",ns).getChildren().get(i);
			if(worksheetFilledAndValid())
			{
				//System.out.println(page.getAttributeValue("Name"));
				//parse Connections from XML-File to a List of Connection-Objects
				setConnectionList();
				//Check Connections:
				if(checkConnectionList())
				{
					System.err.println("Error detected. Switching to next worksheet...");
					continue;
				}
				//parse Shapes from XML-File to a List of Shape-Objects
		        setShapeList();
				if(checkShapeList())
				{
					System.err.println("Error detected. Switching to next worksheet...");
					continue;
				}		        
		        buildVisioTree();
		        
		        //Namen des aktuellen Worksheets ermitteln
		        String worksheetname = "";
		        try{
		        	worksheetname = page.getAttribute("Name").getValue();
		        }catch(NullPointerException npe){
		        	worksheetname = page.getAttribute("NameU").getValue();
		        }
		        
		        //einen Fragebaum pro Visio Worksheet printen		        
		        visioXMLTree.printFragebaum(outputWriters[2], worksheetname, inputFile.getPath());
		        
		        //sammle Diagnosen
		        visioXMLTree.collectDiagnoses(diagnosen);
		        
		        //sammle Fragebï¿½gen
		        visioXMLTree.collectQuestionsheets(frageboegen);		        
			}
		}

		//Print out Diagnoses!
		Iterator<String> it = diagnosen.iterator();
		while(it.hasNext())
		{
			String diag = it.next();
			outputWriters[1].println(diag);
		}
		
		//Print out QuestionSheets!
		it = frageboegen.iterator();
		while(it.hasNext())
		{
			String fra = it.next();
			outputWriters[0].println(fra);
		}		
		
		closePrintWriters(outputWriters);
	}
	
	private PrintWriter[] initOutputFiles()
	{
		String path = this.inputFile.getPath();
		int index = path.lastIndexOf("\\");
		path = path.substring(0,index+1);
		
		String filename = this.inputFile.getName();
		index = filename.lastIndexOf(".");
		filename = filename.substring(0,index);
		
		PrintWriter[] writers = new PrintWriter[3];
		
		try{
			writers[0] = new PrintWriter(new BufferedWriter(new FileWriter(path+"Fragebogen-Hierarchie - "+filename+".txt", false)));
			writers[1] = new PrintWriter(new BufferedWriter(new FileWriter(path+"Diagnose-Hierarchie - "+filename+".txt", false)));
			writers[2] = new PrintWriter(new BufferedWriter(new FileWriter(path+"Fragebaum - "+filename+".txt", false)));
		}catch(IOException ioe){
			ioe.printStackTrace();
		}
		
		return writers;
	}
	
	private void closePrintWriters(PrintWriter[] writers)
	{
		for(PrintWriter pW:writers)
			pW.close();
	}
	
	private boolean worksheetFilledAndValid()
	{
		try{
			page.getChild("Connects",ns).getChildren("Connect",ns);
			page.getChild("Shapes",ns).getChildren("Shape",ns);
		}catch(NullPointerException npe)
		{
			System.err.println("Worksheet is not valid");
			return false;
		}
		return true;
	}
	
	private void setMasterList()
	{
		List masters = root.getChild("Masters",ns).getChildren("Master",ns);
		this.masterList = new ArrayList<Master>(masters.size()+1);
		
		Iterator itMasters = masters.iterator();
		while(itMasters.hasNext())
		{
			Element master = (Element)itMasters.next();
			
			int id = Integer.parseInt(master.getAttributeValue("ID"));
			String shapeName = master.getAttributeValue("NameU");
			int type = ParserConstants.shapeNameToConstant(shapeName);
			
			//only valid shapes are accepted
			if(type != ParserConstants._SHAPE_UNKNOWN)
				this.masterList.add(new Master(id,type));
		}
	}
	
	private int getMasterShapeTypeByMasterID(int masterId)
	{
		Iterator it = this.masterList.iterator();
		while(it.hasNext())
		{
			Master m = (Master)it.next();
			if(m.id == masterId)
				return m.shapeType;
		}
		return ParserConstants._SHAPE_UNKNOWN;
	}
	
	private int countWorksheets()
	{
		return root.getChild("Pages",ns).getChildren().size();
	}
	
	private void setConnectionList()
	{
		List connections = page.getChild("Connects",ns).getChildren("Connect",ns);
		
		this.connectionList = new ArrayList<Connection>((connections.size()/2)+1);
		
		Iterator<Element> itConnections = connections.iterator();
		while(itConnections.hasNext())
		{
			Element connect = itConnections.next();
			//ist dies der Beginn einer Connection?
			if(connect.getAttributeValue("FromCell").equals("BeginX"))
			{	//ID der Connection
				int connectid = Integer.parseInt(connect.getAttributeValue("FromSheet"));
				//ID des Shapes, bei der diese Connection beginnt
				int fromid = Integer.parseInt(connect.getAttributeValue("ToSheet"));		
				//Add new Connection Element to List
				this.connectionList.add(new Connection(connectid,fromid));
			}
		}
		
		//parse again through the List to determine the connection End-Points
		
		itConnections = connections.iterator();
		while(itConnections.hasNext())
		{
			Element connect = itConnections.next();
			//ist dies das Ende einer Connection?
			if(connect.getAttributeValue("FromCell").equals("EndX"))
			{	//ID der Connection
				int connectid = Integer.parseInt(connect.getAttributeValue("FromSheet"));
				//ID des Shapes, bei der diese Connection beginnt
				int toid = Integer.parseInt(connect.getAttributeValue("ToSheet"));		
				
				Iterator itTemp = this.connectionList.iterator();
				while (itTemp.hasNext())
				{
					Connection con = (Connection)itTemp.next();
					if(con.connectionID==connectid)
						con.toID=toid;
				}
			}
		}	
	}
	
	/**
	 * Checks the Shapes in the current Object on consistency and validity 
	 * @return false if everything is ok or only informative warnings are found,
	 * 			true if the processing has to be stopped!
	 */
	private boolean checkConnectionList()
	{
		Iterator<Connection> itConnections = this.connectionList.iterator();
		while(itConnections.hasNext())
		{
			Connection connect = itConnections.next();
			if(connect.fromID<0 || connect.toID<0)
			{//Error found
				System.err.println("Unconnected Connection detected! Check if all connections are glued to the appropriate shapes!");
				return true;
			}

			
		}
		return false;//if everything went ok!
		
	}
	
	private Connection getConnectionByID(int connid)
	{
		Iterator it = this.connectionList.iterator();
		while(it.hasNext())
		{
			Connection c = (Connection)it.next();
			if(c.connectionID==connid)
				return c;
		}
		return null;
	}
	
	private Shape getShapeByID(int shapeid)
	{
		Iterator it = this.shapeList.iterator();
		while(it.hasNext())
		{
			Shape s = (Shape)it.next();
			if(s.id==shapeid)
				return s;
		}
		return null;
	}
	
	private int getStartShapeID()
	{
		ArrayList<Integer> connectedShapes = new ArrayList<Integer>();
		Iterator itConnects = this.connectionList.iterator();
		while(itConnects.hasNext())
		{
			Connection c = (Connection)itConnects.next();
			connectedShapes.add(c.toID);
		}
				
		Iterator it = this.shapeList.iterator();
		while(it.hasNext())
		{
			Shape s = (Shape)it.next();
			if(!connectedShapes.contains(s.id))
				return s.id;
			
//			if(s.text.toLowerCase().contains("start"))
//				return s.id;
		}
		return -1;
	}	
	
	private void setShapeList()
	{
        List shapes = page.getChild("Shapes",ns).getChildren("Shape",ns);
        
        this.shapeList = new ArrayList<Shape>(shapes.size()+1);
        
        Iterator it = shapes.iterator();
        while(it.hasNext())
        {
        	Element shape = (Element)it.next();
        	String elementType = shape.getAttributeValue("Type");
        	if(!elementType.equals("Shape"))
        		continue;
        	
        	int masterID = Integer.parseInt(shape.getAttributeValue("Master"));
        	//System.out.println(getMasterShapeTypeByMasterID(masterID));
        	if(shape.getAttributeValue("Type").equals("Shape"))
        	{
        		int id = Integer.parseInt(shape.getAttributeValue("ID"));
        		String text = shape.getChildTextNormalize("Text",ns);
        		if(text==null)
        			text="";
        		//handelt es sich bei dem shape um ein Verbinder?
        		int shapeType = -1;
        		if((shapeType = getMasterShapeTypeByMasterID(masterID)) == ParserConstants._SHAPE_CONNECTOR)
	        	{
        			Connection con= getConnectionByID(id);
        			if(con!=null)
        				con.text=text.toLowerCase();
	        	}
        		else
        		{
        			this.shapeList.add(new Shape(id, shapeType, text));        			
        		}
        	}
        }
        //parse Connections again and set connectionIDï¿½s in Shapes 
        it = this.connectionList.iterator();
        while(it.hasNext())
        {
        	Connection c = (Connection)it.next();
        	Shape s = getShapeByID(c.fromID);
        	s.connections.add(c.connectionID);
//        	if(c.text.contains("nein") || c.text.contains("no")){
//        		s.connectedToID_no = c.toID;
//        	}
//        	else
//        	{
//        		s.connectedToID_yes = c.toID;
//        	}
        }
	}
	
	
	//TODO
	/**
	 * Checks the Shapes in the current Object on consistency and validity 
	 */
	private boolean checkShapeList()
	{
		Iterator it = this.shapeList.iterator();
		Pattern fragetyp = Pattern.compile(".*\\[.*\\]");
		Pattern bewertung = Pattern.compile(".*\\([N,P][1-7]\\)");
		Matcher m;
		while(it.hasNext())
		{
			Shape s = (Shape)it.next();

			//if this shape is not a diagnosis:
			if(s.shapeType == ParserConstants._SHAPE_DECISION )
			{	//and no question type is set (Regex don't match)
				m = fragetyp.matcher(s.text);
				if(!m.matches())
				{	//...set default Question Type to One-Choice Question
					s.text = s.text + " [oc]";
				}
			}
			
			if(s.shapeType == ParserConstants._SHAPE_TERMINATOR)
			{
				m = bewertung.matcher(s.text);
				if(!m.matches())
				{	//...set default Question Type to One-Choice Question
					s.text = "\"" + s.text + "\" (!)";
				}		
				else
				{
					int lastOpenedBracket = s.text.lastIndexOf("(");
					String text = s.text.substring(0, lastOpenedBracket).trim();
					String bewert = s.text.substring(lastOpenedBracket).trim();
					s.text = "\"" + text + "\" " + bewert;
				}				
			}
		}
		

			
		
		return false;
	}
	
	private void buildVisioTree()
	{
		int startShapeID = getStartShapeID();
		if(startShapeID == -1)
		{
			System.err.println("No Start-Shape found!");
			this.visioXMLTree = null;
			return;
		}
		else
			this.visioXMLTree = buildTreeKnot(startShapeID,0,"");
	}
	
	/**
	 * Builds up a VisioTree Object Tree recursively, starting with the Shape given by shapeID
	 * @param shapeID the ID of the starting shape
	 * @param depth the current depth of the starting knot (usually 0)
	 * @param pathLabel the Knotï¿½s Content
	 * @return
	 */
	private VisioTree buildTreeKnot(int shapeID, int depth, String pathLabel)
	{
		if(shapeID!=-1)
		{
			Shape s = getShapeByID(shapeID);
			//System.out.println(s.text);
			VisioTree vt = new VisioTree(s.text, depth, pathLabel, s.shapeType);
			
			Iterator it = s.connections.iterator();
			while(it.hasNext())
			{
				Connection c = getConnectionByID((Integer)it.next());
				vt.childs.add(buildTreeKnot(c.toID, (depth+1), c.text));
			}
		
//			vt.childs.add(buildTreeKnot(s, (depth+1)));
//			vt.childs.add(buildTreeKnot(s.connectedToID_no, (depth+1)));
			return vt;
		}
		return null;
	}
	
	public static void main(String[] args)
	{
//		VisioXMLReader p = new VisioXMLReader("D://10. Semester//d3web-TextParser//exampleFiles//visioTrees//Spielbaeume.vdx");
//		p.parse();
//        //System.out.println(p.masterList);
//		System.out.println(p.countWorksheets());
        //System.out.println(p.connectionList);
        //System.out.println(p.shapeList);
		
        
	}
}

//ein Shape
class Shape
{
	//the Shape ID
	public int id;
	//the shapeï¿½s Type
	public int shapeType;
	//The Text on the Shape
	public String text;
	
	public ArrayList<Integer> connections;
	
	Shape(int id, int shapeType, String text)
	{
		this.id=id;
		this.shapeType=shapeType;
		this.text=text;
		
		this.connections = new ArrayList<Integer>();
	}
	
	@Override
	public String toString()
	{
		return "Shape-ID: "+id+"; Shape-Type: "+
			ParserConstants.shapeConstantToName(shapeType)+
			"; Text: "+text;//"; connectedToID_yes: "+connectedToID_yes+"; connectedToID_no: "+connectedToID_no+"\n";
	}
}

//ein Master ist eine Shape-Vorlage
class Master
{
	//the masters ID frm XML-File
	public int id;
	//the masters Type
	public int shapeType; 
	
	public Master(int id, int shapeType)
	{
		this.id=id;
		this.shapeType=shapeType;
	}
	
	@Override
	public String toString()
	{
		return "Shape-ID: "+id+"; Shape-Type: "+ParserConstants.shapeConstantToName(shapeType)+"\n";
	}
}

//ein Connection ist eine gerichtete Verbindung zwischen zwei Shapes
class Connection
{
	//The ID of the Connection-Shape itself
	public int connectionID;
	//Shape-IDs, which are connected
	public int fromID;
	public int toID; 
	//Der Text auf der Connection
	public String text;
	
	Connection(int connectionID, int fromID)
	{
		this.connectionID=connectionID;
		this.fromID=fromID;
		this.toID=-1;
		this.text="";
	}
	
	@Override
	public String toString()
	{
		return "Connect-ID: "+connectionID+"; From-ID: "+fromID+"; To-ID: "+toID+"; Text: "+text+"\n";
	}
}
