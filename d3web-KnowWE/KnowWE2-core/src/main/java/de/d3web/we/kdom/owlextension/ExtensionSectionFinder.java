package de.d3web.we.kdom.owlextension;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.kdom.owlextension.Extension;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class ExtensionSectionFinder extends SectionFinder {

	private static final String REGEXP_extension = "<extension[\\w\\W]*?</extension>";
	private static final String REGEXP_extensionCONTENT = "<extension[\\w\\W]*?>([\\w\\W]*?)</extension>";
	private static Pattern extensionregex = Pattern.compile(REGEXP_extension);
	private static Pattern extensioncontentregex = Pattern
			.compile(REGEXP_extensionCONTENT);
	private Extension father;

	public ExtensionSectionFinder(KnowWEObjectType type) {
		super(type);
		if (type instanceof Extension) {
			father = (Extension) type;
		}

	}


	@Override
	public List<Section> lookForSections(Section tmpSection, Section father,
			KnowledgeRepresentationManager kbm, KnowWEDomParseReport report,
			IDGenerator idg) {
		ArrayList<Section> sectionlist = new ArrayList<Section>();
		int i = 0;
		String text = tmpSection.getOriginalText();
		Matcher m = extensionregex.matcher(text);
		while (m.find(i)) {
			String s = m.group();
			i += s.length();
			int indexOfKopicStart = text.indexOf(s);
			Section newsection = Section.createSection(this.getType(), father,
					tmpSection, indexOfKopicStart, indexOfKopicStart
							+ s.length(), kbm, report, idg);
			if (newsection != null) {
				sectionlist.add(newsection);
				Matcher cmatcher = extensioncontentregex.matcher(s);
				if (cmatcher.find()) {
					String erg = cmatcher.group(1).trim();
					this.father.addExtensionSource(newsection, erg);
					ExtensionObject eo = new ExtensionObject(newsection, erg);
					this.father.setExtensionObject(newsection, eo);
				}
			}

		}
		return sectionlist;
	}
}
