/**
 * 
 */
package de.d3web.we.module.semantic.owl.tests;

import org.openrdf.repository.RepositoryConnection;

import de.d3web.we.module.semantic.owl.UpperOntology2;


/**
 * @author kazamatzuri
 *
 */
public class UpperTest {

    /**
     * @param args
     */
    public static void main(String[] args) {
	UpperOntology2 uo=UpperOntology2.getInstance("/home/kazamatzuri/workspaces/knowwe-semantic/KnowWE2/build/opened/KnowWEExtension");
	RepositoryConnection con = uo.getConnection();


    }

}
