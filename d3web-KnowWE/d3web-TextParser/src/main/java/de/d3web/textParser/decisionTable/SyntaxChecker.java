/* Created on 25. Januar 2005, 19:37 */
package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;

/**
 * @author  Andreas Klar
 */
public interface SyntaxChecker {
    
    /**
     * Checks the syntax of a table, which means the existence of all necessary values and
     * the absence of values in cells which are not supposed to contain a value.
     * @param table table to be proofed
     * @return List of all found errors. If no errors are found the list will be empty
     */
    public abstract Report checkSyntax(DecisionTable table);
    
}
