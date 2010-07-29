package de.d3web.we.kdom.objects;

import de.d3web.we.kdom.KnowWEObjectType;

/**
 * This is a marker interface. If you are implementing a KnowWEObjectType with a
 * D3webSubtreeHandler that works with KnowWETerms anywhere in the Subtree, but
 * the KnowWEObjectType that the D3webSubtreeHandler is registered to is neither
 * an TermDefinition nor an TermReference, than you have to extend this class to
 * make the D3webSubtreeHandler able to distinguish. D3webSubtreeHandlers that
 * are not registered to a KnowWEObjectType implementing KnowWETermMarker,
 * respectively TermDefinition or TermReference, will automatically create and
 * destroy every time new TermDefinitions are registered or unregistered in the
 * TerminologyManager.
 * 
 * @author Albrecht Striffler
 * @created 19.07.2010
 */
public interface KnowWETermMarker extends KnowWEObjectType {

}
