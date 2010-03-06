package de.d3web.we.refactoring.script;

import de.d3web.we.kdom.KnowWEArticle;
import de.d3web.we.kdom.KnowWEObjectType;

public class RenameArticleT extends Rename  {
	@Override
	public Class<? extends KnowWEObjectType> findRenamingType() {
		return KnowWEArticle.class;
	}

	@Override
	public <T extends KnowWEObjectType> String findObjectID(Class<T> clazz) {
		return "RenameA";
	}
	
	@Override
	public String findNewName() {
		return "RenameTest";
	}
}
