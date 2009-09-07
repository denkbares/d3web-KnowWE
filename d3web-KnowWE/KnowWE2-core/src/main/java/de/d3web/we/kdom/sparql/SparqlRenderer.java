/**
 * 
 */
package de.d3web.we.kdom.sparql;

import java.util.Map;

import org.openrdf.query.TupleQueryResult;

/**
 * @author kazamatzuri
 *
 */
public interface SparqlRenderer {

    public String render(TupleQueryResult result,Map<String, String> params);
}
