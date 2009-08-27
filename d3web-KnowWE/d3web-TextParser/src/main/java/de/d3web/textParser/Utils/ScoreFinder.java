/*
 * Created on 22.05.2005
 */
package de.d3web.textParser.Utils;

import java.util.Iterator;
import java.util.List;

import de.d3web.kernel.domainModel.Score;

/**
 * @author Andreas Klar
 */
public class ScoreFinder {
	
	/**
	 * Returns the Score which is represented by the symbol given by scoreSymbol
	 * @param scoreSymbol the symbol whose Score object should be returned
	 * @return the score object which refers to the given scoreSymbol or null
	 * if there exists no score for the given symbol 
	 */
	public static Score getScore(String scoreSymbol) {
		List allScores = Score.getAllScores();
		for (Iterator it = allScores.iterator(); it.hasNext(); ) {
			Score theScore = (Score)it.next();
			if (theScore.getSymbol().equalsIgnoreCase(scoreSymbol))
				return theScore;
		}
		return null;
	}

}
