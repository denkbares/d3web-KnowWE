package de.knowwe.core.kdom.sectionFinder;

import de.knowwe.core.kdom.Type;
import de.knowwe.core.kdom.parsing.Section;

import java.util.List;

public class AllTextFinderPlusEmpty implements SectionFinder {

    @Override
    public List<SectionFinderResult> lookForSections(String text, Section<?> father, Type type) {
        return SectionFinderResult.singleItemList(new SectionFinderResult(0, text.length()));
    }
}
