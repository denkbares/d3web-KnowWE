/* Created on 25. Januar 2005, 20:02 */
package de.d3web.textParser.decisionTable;

import de.d3web.report.Report;

/**
 * @author  Andreas Klar
 */
public interface ValueChecker {
    
    /**
     * Checks the validity ofall values in the table.
     * All questions, answers and diagnoses as well as scores will be proofed 
     * @param table table to be proofed
     * @return List of all found errors. If no errors are found the list will be empty
     */
    public abstract Report checkValues(DecisionTable table);
    
}
