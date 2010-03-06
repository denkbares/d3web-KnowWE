package de.d3web.we.refactoring.script;

import de.d3web.we.kdom.KnowWEObjectType;

public class DeleteCommentsT extends DeleteComments  {
	@Override
	public <T extends KnowWEObjectType> String findObjectID(Class<T> clazz) {
		return "DeleteComments";
	}
}
