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

package com.ecyrd.jspwiki.diff;

import java.io.IOException;
import java.text.ChoiceFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.logging.Level;

import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.Chunk;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;
import org.apache.commons.jrcs.diff.RevisionVisitor;
import org.apache.commons.jrcs.diff.myers.MyersDiff;
import org.apache.log4j.Logger;


import com.ecyrd.jspwiki.NoRequiredPropertyException;
import com.ecyrd.jspwiki.TextUtil;
import com.ecyrd.jspwiki.WikiContext;
import com.ecyrd.jspwiki.WikiEngine;
import com.ecyrd.jspwiki.i18n.InternationalizationManager;

import de.d3web.we.flow.diff.FlowchartEdge;
import de.d3web.we.flow.diff.FlowchartNode;
import de.d3web.we.flow.diff.FlowchartNodeType;
import de.d3web.we.logging.Logging;




public class FlowchartDiffProvider implements DiffProvider
{
    private static final Logger log = Logger.getLogger(FlowchartDiffProvider.class);

    private static final String CSS_DIFF_ADDED = "<tr><td class=\"diffadd\">";
    private static final String CSS_DIFF_REMOVED = "<tr><td class=\"diffrem\">";
    private static final String CSS_DIFF_UNCHANGED = "<tr><td class=\"diff\">";
    private static final String CSS_DIFF_CLOSE = "</td></tr>" + Diff.NL;


    /**
     *  Constructs the provider.
     */
    public FlowchartDiffProvider()
    {
    }


    /**
     * {@inheritDoc}
     * @see com.ecyrd.jspwiki.WikiProvider#getProviderInfo()
     */
    public String getProviderInfo()
    {
        return "FlowchartDiffProvider";
    }

    /**
     * {@inheritDoc}
     * @see com.ecyrd.jspwiki.WikiProvider#initialize(com.ecyrd.jspwiki.WikiEngine, java.util.Properties)
     */
    public void initialize(WikiEngine engine, Properties properties)
        throws NoRequiredPropertyException, IOException
    {
    }

    /**
     * Makes a diff using the BMSI utility package. We use our own diff printer,
     * which makes things easier.
     * 
     * @param ctx The WikiContext in which the diff should be made.
     * @param p1 The first string
     * @param p2 The second string.
     * 
     * @return Full HTML diff.
     */
    public String makeDiffHtml( WikiContext ctx, String p1, String p2 )
    {        
        String diffResult = "";

        try
        {
            String[] first  = Diff.stringToArray(TextUtil.replaceEntities(p1));
            String[] second = Diff.stringToArray(TextUtil.replaceEntities(p2));
            Revision rev = Diff.diff(first, second, new MyersDiff());


            if( rev == null || rev.size() == 0 )
            {
                // No difference

                return "";
            }

            StringBuffer ret = new StringBuffer(rev.size() * 20); // Guessing how big it will become...

            ret.append("<table class=\"diff\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
            rev.accept( new RevisionPrint(ctx,ret) );
            ret.append("</table>\n");

            
            // if the changes are in a flowchart section
            if (ret.toString().contains("&lt;DIV class=&quot;Flowchart&quot; style=&quot;")) {
            
                String v1 = p1.substring(p1.indexOf("<flowchart id="), p1.indexOf("</preview></flowchart>")) + "</preview></flowchart>";
                String v2 = p2.substring(p2.indexOf("<flowchart id=" ), p2.indexOf("</preview></flowchart>")) + "</preview></flowchart>";

                List<FlowchartNode> f1 = getNodes(v1);
                List<FlowchartNode> f2 = getNodes(v2);
         
                List<FlowchartNode> addedNodes = addedNodes(f1, f2);
                List<FlowchartNode> removedNodes = removedNodes(f1, f2);
                List<FlowchartNode> changedNodes = changedNodes(f1, f2);
              
                List<FlowchartEdge> e1 = getEdges(v1);
                List<FlowchartEdge> e2 = getEdges(v2);
              
                List<FlowchartEdge> addedEdges = addedEdges(e1, e2);
                List<FlowchartEdge> removedEdges = removedEdges(e1, e2);
                List<FlowchartEdge> changedEdges = changedEdges(e1, e2);
 
                String s = "";
                
                if (!addedNodes.isEmpty()) {
                    s += "added Nodes:<br />" + addedNodes + "<br />";
                }
                
                if (!removedNodes.isEmpty()){
                    s += "removed Nodes:<br />" + removedNodes + "<br />";
                }
                
                if (!changedNodes.isEmpty()) {
                    s += "changed Nodes:<br />" + changedNodes + "<br />";
                }
                
                s += "<br />";
                
                if (!addedEdges.isEmpty()) {
                    s += "added Edges:<br />" + addedEdges + "<br />";
                }
                
                if (!removedEdges.isEmpty()) {
                    s += "removed Edges:<br />" + removedEdges + "<br />";
                }
                
                if (!changedEdges.isEmpty()) {
                    s += "changed Edges:<br />" + changedEdges + "<br />";
                }

                
                v1 = setIdToNodes( v1, getNodes( v1 ) );
                v2 = setIdToNodes( v2, getNodes( v2 ) );

                v2 = colorNodes( v2, changedNodes, FlowchartChangeType.changed );
                v2 = colorNodes(v2, addedNodes, FlowchartChangeType.added);
                v1 = colorNodes (v1, removedNodes, FlowchartChangeType.removed);  
                
                return s + "<br \\>" + "<br \\>" + createPreview( v1 ) + "<br \\>" + "<br \\>" + "<br \\>" + "<br \\>"
                       + createPreview( v2 );
            }
            return ret.toString();
        }
        catch( DifferentiationFailedException e )
        {
            diffResult = "makeDiff failed with DifferentiationFailedException";
            log.error(diffResult, e);
        }

        return diffResult;
    }
    
    private String createPreview(String version) {
        String xml = version;
        int startPos = xml.lastIndexOf("<preview mimetype=\"text/html\">");
        int endPos = xml.lastIndexOf("</preview>");
        if (startPos >= 0 && endPos >= 0) {
            return 
                "<div style='zoom: 50%; cursor: pointer;'>" +
                "<link rel='stylesheet' type='text/css' href='cc/kbinfo/dropdownlist.css'></link>" +
                "<link rel='stylesheet' type='text/css' href='cc/kbinfo/objectselect.css'></link>" +
                "<link rel='stylesheet' type='text/css' href='cc/kbinfo/objecttree.css'></link>" +
                "<link rel='stylesheet' type='text/css' href='cc/flow/flowchart.css'></link>" +
                "<link rel='stylesheet' type='text/css' href='cc/flow/floweditor.css'></link>" +
                "<link rel='stylesheet' type='text/css' href='cc/flow/guard.css'></link>" +
                "<link rel='stylesheet' type='text/css' href='cc/flow/node.css'></link>" +
                "<link rel='stylesheet' type='text/css' href='cc/flow/nodeeditor.css'></link>" +
                "<link rel='stylesheet' type='text/css' href='cc/flow/rule.css'></link>" +
                "<style type='text/css'>div, span, a { cursor: pointer !important; }</style>" + 
                xml.substring(startPos+43, endPos-8) + 
                "</div>";
        }
        return "you shouldn't read this :(";

    }
    
    /**
     * gets all nodes from the xml
     */
    private List<FlowchartNode> getNodes(String version) {
        ArrayList<FlowchartNode> nodes = new ArrayList<FlowchartNode>();
        version = version.substring( version.indexOf("<!-- nodes of the flowchart -->") + 31, 
                                     version.indexOf("<!-- rules of the flowchart -->"));
        
        // to get the tags, e.g. <node>, <position>, also the closing ones like </node>
        // as line
        String[] lines = version.split("<");


        for (String line : lines) {
            // if our "line" is a node, create a new temp node
            if (line.startsWith("node")) {
                String id = line.substring(line.indexOf("#"),line.indexOf(">") -1);
                nodes.add(new FlowchartNode(id));
             
            //if the "line" is a position
            } else if (line.startsWith("position")) {
                String pos = line.substring( 15, line.indexOf(">"));
                int left = Integer.valueOf(pos.substring(0, pos.indexOf( "\"" )));
                
                pos = pos.substring( pos.indexOf("top=\"") + 5, pos.length() -1);
                int top = Integer.valueOf( pos );
                
                // set the position of the last node
                nodes.get(nodes.size() - 1).setLeft(left);
                nodes.get(nodes.size() - 1).setTop(top);
                
            // if the "line" is an action
            } else if (line.startsWith("action" )) {
                String action = line.substring(25);
                // set the text of the last node and the nodeType
                nodes.get(nodes.size() - 1).setNodeType(FlowchartNodeType.action);
                nodes.get(nodes.size() - 1).setText(action);
                
            // if the "line" is a start    
            } else if (line.startsWith( "start" )) {
                String start = line.substring( 5 ).replace( ">", "" );
                // set the text of the last node and the nodeType
                nodes.get(nodes.size() - 1).setNodeType(FlowchartNodeType.start);
                nodes.get(nodes.size() - 1).setText(start);
               
            // if the line is an exit
            } else if (line.startsWith( "exit" )) {
                String exit = line.substring( 4 ).replace( ">", "" );;
                // set the text of the last node and the nodeType
                nodes.get(nodes.size() - 1).setNodeType(FlowchartNodeType.exit);
                nodes.get(nodes.size() - 1).setText(exit);
            }
        }

        return nodes;
    }
    
    
    /**
     * returns all nodes which were added between version v1 and version v2
     */
    private List<FlowchartNode> addedNodes(List<FlowchartNode> v1, List<FlowchartNode> v2) {
        ArrayList<FlowchartNode> addedNodes = new ArrayList<FlowchartNode>();
        
        boolean added = true;
        
        for (FlowchartNode f2 : v2) {
            added = true;
            for (FlowchartNode f1 : v1) {
                if (f2.getID().equals(f1.getID()) && 
                    f2.getNodeType().equals( f1.getNodeType())) {
                    added = false;
                }
            }
            if (added) {
                addedNodes.add(f2);
            }
        }
        return addedNodes;
    }
    
    
    /**
     * returns all nodes which were removed between version v1 and version v2
     */
    private List<FlowchartNode> removedNodes(List<FlowchartNode> v1, List<FlowchartNode> v2) {
        ArrayList<FlowchartNode> removedNodes = new ArrayList<FlowchartNode>();
        
        boolean removed = false;
        
        for (FlowchartNode f1 : v1) {
            removed = true;
            for (FlowchartNode f2 : v2) {
                if (f1.getID().equals( f2.getID()) && 
                    f1.getNodeType().equals(f2.getNodeType())) {
                    removed = false;
                }
            }
            if (removed) {
                removedNodes.add(f1);
            }
        }
        return removedNodes;
    }
    
    
    /**
     * returns all nodes which were changed between version v1 and version v2
     */
    private List<FlowchartNode> changedNodes(List<FlowchartNode> v1, List<FlowchartNode> v2) {
        ArrayList<FlowchartNode> changedNodes = new ArrayList<FlowchartNode>();
        
        // to sort out the added or removed Nodes
        v1.removeAll(removedNodes(v1, v2));
        v2.removeAll(addedNodes(v1, v2));
        
        boolean unchanged;
        
        for (FlowchartNode n2 : v2) {
            unchanged = false;
            for (FlowchartNode n1 : v1) {
                if (n2.equals( n1 )) {
                    unchanged = true;
                }
            }
            if (!unchanged) {
                changedNodes.add(n2);
            }
        }
        
        return changedNodes;
    }
    
    /**
     * gets all the edges from the xml
     */
    private List<FlowchartEdge> getEdges(String version) {
        ArrayList<FlowchartEdge> edges = new ArrayList<FlowchartEdge>();
        version = version.substring( version.indexOf( "<!-- rules of the flowchart -->" ) + 31, 
                                     version.indexOf( "<preview " ) );
        String[] lines = version.split("<");
        
        for (String line : lines) {
            if (line.startsWith("edge")) {
                String id = line.substring(9, line.indexOf(">") - 1);
                edges.add(new FlowchartEdge(id));
            } else if (line.startsWith("source")) {
                String source = line.substring(7);
                edges.get(edges.size() -1).setSource(source);
            } else if (line.startsWith("target")) {
                String target = line.substring(7);
                edges.get(edges.size() -1).setTarget(target);
            } else if (line.startsWith("guard")) {
                String guard = line.substring(line.indexOf(">") +1);
                edges.get(edges.size() -1).setGuard(guard);
            }
        }    
        return edges;
    }
    
    /**
     * returns all edges which were added between version v1 and version v2
     */
    private List<FlowchartEdge> addedEdges(List<FlowchartEdge> v1, List<FlowchartEdge> v2) {
        ArrayList<FlowchartEdge> addedEdges = new ArrayList<FlowchartEdge>();
        
        boolean added = true;
        
        for (FlowchartEdge f2 : v2) {
            added = true;
            for (FlowchartEdge f1 : v1) {
                if (f2.getID().equals(f1.getID())) {
                    added = false;
                }
            }
            if (added) {
                addedEdges.add(f2);
            }
        }
        return addedEdges;
    }
    
    
    /**
     * returns all edges which were removed between version v1 and version v2
     */
    private List<FlowchartEdge> removedEdges(List<FlowchartEdge> v1, List<FlowchartEdge> v2) {
        ArrayList<FlowchartEdge> removedEdges = new ArrayList<FlowchartEdge>();
        
        boolean removed = false;
        
        for (FlowchartEdge f1 : v1) {
            removed = true;
            for (FlowchartEdge f2 : v2) {
                if (f1.getID().equals( f2.getID())) {
                    removed = false;
                }
            }
            if (removed) {
                removedEdges.add(f1);
            }
        }
        return removedEdges;
    }
    
    
    /**
     * returns all edges which were changed between version v1 and version v2
     */
    private List<FlowchartEdge> changedEdges(List<FlowchartEdge> v1, List<FlowchartEdge> v2) {
        ArrayList<FlowchartEdge> changedEdges = new ArrayList<FlowchartEdge>();
        
        // to sort out the added or removed Nodes
        v1.removeAll(removedEdges(v1, v2));
        v2.removeAll(addedEdges(v1, v2));
        
        
        for (FlowchartEdge n2 : v2) {
            for (FlowchartEdge n1 : v1) {
                if( n2.getID().equals( n1.getID() )
                    && (!(n2.getSource().equals( n1.getSource()) ) || !(n2.getTarget().equals( n1.getTarget()) )) )
                {
                    changedEdges.add(n2);
                    Logging.getInstance().log( Level.INFO, "n1: " + n1 + "  n2: " + n2 );
                
                }
            }
        }
        
        return changedEdges;
    }
    
    
    /**
     * doe the coloring
     */
    private String colorNodes(String version, List<FlowchartNode> alteredNodes, FlowchartChangeType change) {
    	
    	// set the additional class of the yet to be colored nodes
    	String alteration = "";
    	if (change.equals(FlowchartChangeType.added)) {
    		alteration = " added";
    	} else if (change.equals(FlowchartChangeType.removed)) {
    		alteration = " removed";	
    	} else if (change.equals(FlowchartChangeType.changed)) {
    		alteration = " changed";
    	} else {
    		return version;
    	}
    	
    	
        String temp = version;
        
        // get the lower part of the flowchart
        version = version.substring(version.indexOf("<DIV class=\"Flowchart\""));
        version = version.substring(version.indexOf(">") + 1);
      
        // get all the nodes
        String[] nodes = version.split("<DIV class=\"Node\" id=\"");

        
        for (int i = 1; i < nodes.length; i++) {  

            String id = nodes[i].substring( 0, nodes[i].indexOf("\""));

            
            for (FlowchartNode n : alteredNodes) {

            	// check for each node if it changed
                String nID = n.getID();
                if (id.equals( nID )) {
  	
                	// if yes, add the additional class
                	String inputHelper1 = temp.substring(0, temp.indexOf(nodes[i]) - 6);
                	String inputHelper2 = temp.substring(temp.indexOf(nodes[i]));
                	temp = inputHelper1 + alteration + "\" id=\"" + inputHelper2;
                }
            }
        }
        return temp;
    }
    
        
    
    private String setIdToNodes(String version, List<FlowchartNode> flowchartNodes) {
        String temp = version;
        
        // get the lower part of the flowchart
        version = version.substring(version.indexOf("<DIV class=\"Flowchart\""));
        version = version.substring(version.indexOf(">") + 1);
        
        // get all the nodes
        String[] nodes = version.split("<DIV class=\"Node\"");
       
        // make temporary nodes out of the lines, which are easier to compare
        // to the xml nodes and add their respective ids
        for (int i = 1; i < nodes.length; i++) {  
            nodes[i] = nodes[i].substring(0, nodes[i].indexOf( ">" ));
            String line = nodes[i];
                      
            nodes[i] = nodes[i].substring(14);
            int left = Integer.valueOf(nodes[i].substring(0, nodes[i].indexOf("px")));
            
            nodes[i] = nodes[i].substring( nodes[i].indexOf("px") + 8);
            int top = Integer.valueOf(nodes[i].substring(0, nodes[i].indexOf("px")));
            
            FlowchartNode tempNode = new FlowchartNode(left, top);
          
            for (FlowchartNode n : flowchartNodes) {
                if (tempNode.equalsPositionOnly(n)) {
                    String inputHelper1 = temp.substring(0, temp.indexOf(line));
                    String inputHelper2 = temp.substring(temp.indexOf(line));
                    temp = inputHelper1 + " id=\"" + n.getID() + "\"" + inputHelper2;
                }
            }
        }
        return temp;
    }

    private static final class RevisionPrint
        implements RevisionVisitor
    {
        private StringBuffer m_result = null;
        private WikiContext  m_context;
        private ResourceBundle m_rb;
        
        private RevisionPrint(WikiContext ctx,StringBuffer sb)
        {
            m_result = sb;
            m_context = ctx;
            m_rb = ctx.getBundle( InternationalizationManager.CORE_BUNDLE );
        }

        public void visit(Revision rev)
        {
            // GNDN (Goes nowhere, does nothing)
        }

        public void visit(AddDelta delta)
        {
            Chunk changed = delta.getRevised();
            print(changed, m_rb.getString( "diff.traditional.added" ) );
            changed.toString(m_result, CSS_DIFF_ADDED, CSS_DIFF_CLOSE);
        }

        public void visit(ChangeDelta delta)
        {
            Chunk changed = delta.getOriginal();
            print(changed, m_rb.getString( "diff.traditional.changed") );
            changed.toString(m_result, CSS_DIFF_REMOVED, CSS_DIFF_CLOSE);
            delta.getRevised().toString(m_result, CSS_DIFF_ADDED, CSS_DIFF_CLOSE);
        }

        public void visit(DeleteDelta delta)
        {
            Chunk changed = delta.getOriginal();
            print(changed, m_rb.getString( "diff.traditional.removed") );
            changed.toString(m_result, CSS_DIFF_REMOVED, CSS_DIFF_CLOSE);
        }

        private void print(Chunk changed, String type)
        {
            m_result.append(CSS_DIFF_UNCHANGED);
            
            String[] choiceString = 
            {
               m_rb.getString("diff.traditional.oneline"),
               m_rb.getString("diff.traditional.lines")
            };
            double[] choiceLimits = { 1, 2 };
            
            MessageFormat fmt = new MessageFormat("");
            fmt.setLocale( WikiContext.getLocale(m_context) );
            ChoiceFormat cfmt = new ChoiceFormat( choiceLimits, choiceString );
            fmt.applyPattern( type );
            Format[] formats = { NumberFormat.getInstance(), cfmt, NumberFormat.getInstance() };
            fmt.setFormats( formats );
            
            Object[] params = { changed.first() + 1, 
                                changed.size(),
                                changed.size() };
            m_result.append( fmt.format(params) );
            m_result.append(CSS_DIFF_CLOSE);
        }
    }
    
    public enum FlowchartChangeType {
    	added, removed, changed
    }
}
