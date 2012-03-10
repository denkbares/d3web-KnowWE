<%@page import="de.knowwe.core.*"%><%@ page import="java.util.*,java.io.*"%><%!
String findParam( PageContext ctx, String key )
    {
        ServletRequest req = ctx.getRequest();

        String val = req.getParameter( key );

        if( val == null )
        {
            val = (String)ctx.findAttribute( key );
        }

        return val;
    }
%><%
	/// DO NOT ALTER THE NON-JAVA-CONTENT OF THIS FILE!!! ///
	// Every character outside of the java-scope gets appended to the generated file!!!
	
	//String value = 
	String filename = findParam(pageContext,"filename");;
	String nodeID = findParam(pageContext,"nodeID");
	String topic = findParam(pageContext, Attributes.TOPIC);
	String web = findParam(pageContext, "web");
	
	//set the header and also the Name by which user will be prompted to save
	response.setHeader ("Content-Disposition", "attachment;filename=\""+filename+"\"");
	InputStream in = null;
	
	if (filename.endsWith(".txt")) {
		//set the content type(can be excel/word/powerpoint etc..)
		response.setContentType ("application/txt");
		String data = KnowWEFacade.getInstance().getNodeData(web, topic, nodeID);
		if(data != null) {
			StringBuffer StringBuffer1 = new StringBuffer(data);
			in = new ByteArrayInputStream(StringBuffer1.toString().getBytes("UTF-8"));
		}
	} else if (filename.endsWith(".jar")) {
		//set the content type
		response.setContentType ("application/jar");
		de.knowwe.knowRep.KnowledgeRepresentationHandler handler = Environment.getInstance()
			.getKnowledgeRepresentationManager(web).getHandler("d3web");
		if (handler != null) {
			java.net.URL home = handler.saveKnowledge(topic);
			in = home.openStream();
		}
	}
	
	

	//OPen an input stream to the file and post the file contents thru the 
	//servlet output stream to the client m/c
	

	ServletOutputStream outs = response.getOutputStream();
	
	int bit;
	
	
	try {
		while ((bit = in.read()) >= 0) {
			outs.write(bit);
		}


    } catch (IOException ioe) {
    	ioe.printStackTrace(System.out);
    	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.valueOf(ioe));
    } finally {
		in.close();	
    }
    
    
	outs.flush();
	outs.close();

%>