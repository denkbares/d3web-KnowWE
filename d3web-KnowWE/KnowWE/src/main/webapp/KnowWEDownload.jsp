<%@page import="de.d3web.we.core.*"%>
<%@ page import="java.util.*,java.io.*"%>
<%!
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
%>
<%
	
	
	//String value = 
	String filename = findParam(pageContext,"filename");;
	String nodeID = findParam(pageContext,"nodeID");
	String topic = findParam(pageContext, KnowWEAttributes.TOPIC);
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
		de.d3web.we.knowRep.KnowledgeRepresentationHandler handler = KnowWEEnvironment.getInstance()
			.getKnowledgeRepresentationManager(web).getHandler("d3web");
		if (handler != null) {
			java.net.URL home = handler.saveKnowledge(topic);
			in = home.openStream();
		}
	}
	
	

	//OPen an input stream to the file and post the file contents thru the 
	//servlet output stream to the client m/c
	

	ServletOutputStream outs = response.getOutputStream();
	
	int bit = 256;

	try {
		while ((bit) >= 0) {
			bit = in.read();
			outs.write(bit);
		}
		//System.out.println("" +bit);


    } catch (IOException ioe) {
    	ioe.printStackTrace(System.out);
    }
    //		System.out.println( "\n" + i + " byt
    //     es sent.");
    //		System.out.println( "\n" + f.length(
    //     ) + " bytes sent.");
	outs.flush();
	outs.close();
	in.close();	

            %>
