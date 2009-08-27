package de.d3web.we.alignment;


import de.d3web.kernel.domainModel.IDObject;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.kernel.domainModel.answers.AnswerChoice;
import de.d3web.kernel.domainModel.answers.AnswerNum;

public class D3webAlignUtils {

	public static String getText(IDObject ido) {
		if(ido instanceof NamedObject) {
			return ((NamedObject)ido).getText();
		} else if(ido instanceof AnswerChoice) {
			return ((AnswerChoice)ido).getText();
		} else if(ido instanceof AnswerNum) {
			return ((AnswerNum)ido).getId();
		} else {
			return ido.getId();
		}
	}
	
}
