package de.d3web.we.hermes.kdom;

import de.d3web.we.kdom.DefaultAbstractKnowWEObjectType;
import de.d3web.we.kdom.sectionFinder.AllTextSectionFinder;

public class TimeEventDescriptionType extends DefaultAbstractKnowWEObjectType {

    @Override
    protected void init() {
	sectionFinder = new AllTextSectionFinder();
    }

}
