<%@page import="de.d3web.we.core.*"%><%@ page import="com.ecyrd.jspwiki.*"%>
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
	String data = KnowWEFacade.getInstance().getNodeData("default_web", topic, nodeID);
	//set the content type(can be excel/word/powerpoint etc..)
	response.setContentType ("application/txt");
	//set the header and also the Name by which user will be prompted to save
	response.setHeader ("Content-Disposition", "attachment;filename=\""+filename+"\"");
	
	//get the file name
	//OPen an input stream to the file and post the file contents thru the 
	//servlet output stream to the client m/c
	if(data != null) {
		StringBuffer StringBuffer1 = new StringBuffer(data);
		ByteArrayInputStream Bis1 = new ByteArrayInputStream(StringBuffer1.toString().getBytes("UTF-8"));
	
		InputStream in = Bis1;
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
	}
            %>
