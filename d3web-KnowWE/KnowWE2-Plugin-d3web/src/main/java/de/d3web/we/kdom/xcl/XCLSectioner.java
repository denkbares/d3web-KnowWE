package de.d3web.we.kdom.xcl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.d3web.we.kdom.IDGenerator;
import de.d3web.we.kdom.KnowWEDomParseReport;
import de.d3web.we.kdom.KnowWEObjectType;
import de.d3web.we.kdom.Section;
import de.d3web.we.kdom.SectionFinder;
import de.d3web.we.knowRep.KnowledgeRepresentationManager;

public class XCLSectioner extends SectionFinder{
	

	public XCLSectioner(KnowWEObjectType type) {
		super(type);
	}

	public static final String XCL_OPEN_TAG = "\\<XCL";
	public static final String XCL_CLOSE_TAG = "\\</XCL";

	@Override
	public List<Section> lookForSections(Section tmp, Section father, KnowledgeRepresentationManager mgn, KnowWEDomParseReport rep, IDGenerator idg) {
		String text = tmp.getOriginalText();
		ArrayList<Section> result = new ArrayList<Section>();
		
		//Pattern p = Pattern.compile(XCL_OPEN_TAG+"(\\w|\\W)*"+XCL_CLOSE_TAG);
		Pattern p = Pattern.compile("<XCL[\\w\\W]*?</XCL>");
		
		Matcher m = p.matcher(text);

		while (m.find()) {
			result.add (Section.createSection(this.getType(), father, tmp, m.start(), m.end(), mgn, rep, idg));
		}
		/*
		Pattern p0 = Pattern.compile(XCL_OPEN_TAG);
		Pattern p1 = Pattern.compile(XCL_CLOSE_TAG);
		
		Matcher m0 = p0.matcher(text);
		Matcher m1 = p1.matcher(text);

		LinkedList <int[]> pairs = new LinkedList<int[]>();
		
		while (m0.find()) {
			int[] p = new int[2];
			p[0] = m0.start();
			pairs.add(p);
		}
		
		while (m1.find() && (!pairs.isEmpty())) {
			int[] p = pairs.pollFirst();
			p[1] = m1.start();
			pairs.add(p);			
		}
		for (int[] a : pairs) {
			result.add (Section.createSection(this.getType(), father, tmp, a[0], a[1], mgn, rep, idg));
		}*/

		return result;
	}

}
