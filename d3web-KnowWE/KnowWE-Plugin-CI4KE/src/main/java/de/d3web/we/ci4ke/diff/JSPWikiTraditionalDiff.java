package de.d3web.we.ci4ke.diff;

import java.text.ChoiceFormat;
import java.text.Format;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Logger;

import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.Chunk;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;
import org.apache.commons.jrcs.diff.RevisionVisitor;
import org.apache.commons.jrcs.diff.myers.MyersDiff;

import de.d3web.we.ci4ke.util.CIUtilities;

public class JSPWikiTraditionalDiff implements DiffInterface {
   
//	private static Logger logger = Logger.getLogger(JSPWikiTraditionalDiff.class.getName());

    private static final String CSS_DIFF_ADDED = "<tr><td class=\"diffadd\">";
    private static final String CSS_DIFF_REMOVED = "<tr><td class=\"diffrem\">";
    private static final String CSS_DIFF_UNCHANGED = "<tr><td class=\"diff\">";
    private static final String CSS_DIFF_CLOSE = "</td></tr>" + Diff.NL;


    /**
     *  Constructs the provider.
     */
    public JSPWikiTraditionalDiff()
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
    public String makeDiffHtml( String p1, String p2 )
    {
        String diffResult = "";

        try
        {
            String[] first  = Diff.stringToArray(CIUtilities.replaceEntities(p1));
            String[] second = Diff.stringToArray(CIUtilities.replaceEntities(p2));
            Revision rev = Diff.diff(first, second, new MyersDiff());

            if( rev == null || rev.size() == 0 )
            {
                // No difference

                return "";
            }

            StringBuffer ret = new StringBuffer(rev.size() * 20); // Guessing how big it will become...

//            ret.append("<table class=\"diff\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
            rev.accept( new RevisionPrint(ret) );
//            ret.append("</table>\n");

            return ret.toString();
        }
        catch( DifferentiationFailedException e )
        {
            diffResult = "makeDiff failed with DifferentiationFailedException";
            Logger.getLogger(this.getClass().getName()).warning(diffResult);
        }

        return diffResult;
    }


    private static final class RevisionPrint
        implements RevisionVisitor
    {
        private StringBuffer m_result = null;
//        private WikiContext  m_context;
//        private ResourceBundle m_rb;
        
        private RevisionPrint(/*WikiContext ctx,*/StringBuffer sb)
        {
            m_result = sb;
//            m_context = ctx;
//            m_rb = ctx.getBundle( InternationalizationManager.CORE_BUNDLE );
        }

        public void visit(Revision rev)
        {
            // GNDN (Goes nowhere, does nothing)
        }

        public void visit(AddDelta delta)
        {
        	Chunk changed = delta.getOriginal();
        	//opening a div
        	m_result.append("<div class='ci-changes-panel'><h4>");
        	print(changed, "At line {0} added {1}" );
        	m_result.append("</h4>\n");
        	//opening the table
        	m_result.append("<table class=\"diff\" border=\"0\" cellspacing=\"0\" "+
        			"cellpadding=\"0\" style=\"display: none;\">\n");
        	System.out.println(changed);
        	changed.toString(m_result, CSS_DIFF_ADDED, CSS_DIFF_CLOSE);
            
            m_result.append("</table>\n</div>\n");  
        }

        public void visit(ChangeDelta delta)
        {
        	Chunk changed = delta.getOriginal();
        	
        	//opening a div
        	m_result.append("<div class='ci-changes-panel'><h4>");
        	print(changed, "At line {0} changed {1}" );
        	m_result.append("</h4>\n");
        	//opening the table
        	m_result.append("<table class=\"diff\" border=\"0\" cellspacing=\"0\" "+
        			"cellpadding=\"0\" style=\"display: none;\">\n");        	
        	
            changed.toString(m_result, CSS_DIFF_REMOVED, CSS_DIFF_CLOSE);
            delta.getRevised().toString(m_result, CSS_DIFF_ADDED, CSS_DIFF_CLOSE);
            
            m_result.append("</table>\n</div>\n");
        }

        public void visit(DeleteDelta delta)
        {
        	Chunk changed = delta.getOriginal();
        	//opening a div
        	m_result.append("<div class='ci-changes-panel'><h4>");
        	print(changed, "At line {0} removed {1}" );
        	m_result.append("</h4>\n");
        	//opening the table
        	m_result.append("<table class=\"diff\" border=\"0\" cellspacing=\"0\" "+
        			"cellpadding=\"0\" style=\"display: none;\">\n");
            changed.toString(m_result, CSS_DIFF_REMOVED, CSS_DIFF_CLOSE);
            
            m_result.append("</table>\n</div>\n");            
        }

        private void print(Chunk changed, String type)
        {
        	//Starting the first line
//            m_result.append(CSS_DIFF_UNCHANGED);
            
            String[] choiceString = 
            {
               "one line",
               "{2} lines"
            };
            double[] choiceLimits = { 1, 2 };
            
            MessageFormat fmt = new MessageFormat("");
            
            fmt.setLocale( Locale.GERMAN );
            ChoiceFormat cfmt = new ChoiceFormat( choiceLimits, choiceString );
            fmt.applyPattern( type );
            Format[] formats = { NumberFormat.getInstance(), cfmt, NumberFormat.getInstance() };
            fmt.setFormats( formats );
            
            Object[] params = { changed.first() + 1, 
                                changed.size(),
                                changed.size() };
            m_result.append( fmt.format(params) );
            
            
//            m_result.append(CSS_DIFF_CLOSE);
        }
    }

}
