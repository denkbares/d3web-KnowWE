/*
 * Copyright (C) 2009 Chair of Artificial Intelligence and Applied Informatics
 *                    Computer Science VI, University of Wuerzburg
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
