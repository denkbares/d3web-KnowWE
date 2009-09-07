<%@ page import="com.ecyrd.jspwiki.*"%>
<%@ page import="de.d3web.we.javaEnv.*"%>
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
%><%
    //set the content type(can be excel/word/powerpoint etc..)
	response.setContentType ("application/txt");
	//set the header and also the Name by which user will be prompted to save
	response.setHeader ("Content-Disposition", "attachment;filename=\"ontology.owl\"");	
		ServletOutputStream outs = response.getOutputStream();		
		KnowWEFacade.getInstance().writeOwl(outs);
%>
