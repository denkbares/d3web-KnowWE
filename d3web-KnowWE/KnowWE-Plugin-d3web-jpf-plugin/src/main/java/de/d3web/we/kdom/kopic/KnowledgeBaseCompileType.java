package de.d3web.we.kdom.kopic;

import java.util.Arrays;
import java.util.List;

import de.d3web.we.core.packaging.PackageCompileType;
import de.d3web.we.core.packaging.PackageReference;
import de.d3web.we.kdom.Section;

public class KnowledgeBaseCompileType extends PackageCompileType {

	@Override
	protected void init() {
	}

	@Override
	public List<String> getPackagesToCompile(Section<? extends PackageReference> section) {
		return Arrays.asList(section.getOriginalText().trim());
	}

}
