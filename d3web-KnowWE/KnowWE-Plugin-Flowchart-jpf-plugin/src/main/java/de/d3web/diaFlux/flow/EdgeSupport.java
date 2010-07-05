/**
 * 
 */
package de.d3web.diaFlux.flow;

import de.d3web.core.inference.condition.NoAnswerException;
import de.d3web.core.inference.condition.UnknownAnswerException;
import de.d3web.core.session.Session;

/**
 * 
 * @author Reinhard Hatko
 *
 * Created: 20.12.2009
 */
public class EdgeSupport implements ISupport {

	private final IEdge edge;
	
	
	public EdgeSupport(IEdge edge) {
		if (edge == null)
			throw new IllegalArgumentException("edge must not be null.");
		this.edge = edge;
	}



	public boolean isValid(Session theCase) {
		
		try {
			return edge.getCondition().eval(theCase);
			
		} catch (NoAnswerException e) {
			return false;
		} catch (UnknownAnswerException e) {
			return false;
		}
		
	}
	
	/**
	 * @return the edge
	 */
	public IEdge getEdge() {
		return edge;
	}
	
	
	@Override
	public String toString() {
		return "EdgeSupport:" + edge;
	}

}
