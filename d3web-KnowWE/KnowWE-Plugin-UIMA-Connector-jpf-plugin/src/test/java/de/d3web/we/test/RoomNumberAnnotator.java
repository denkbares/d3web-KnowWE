package de.d3web.we.test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.tutorial.Building;
import org.apache.uima.tutorial.RoomNumber;

public class RoomNumberAnnotator extends JCasAnnotator_ImplBase {

	  // create regular expression pattern for Yorktown room number
	  private Pattern mYorktownPattern = 
	        Pattern.compile("\\b[0-4]\\d-[0-2]\\d\\d\\b");

	  // create regular expression pattern for Hawthorne room number
	  private Pattern mHawthornePattern = 
	        Pattern.compile("\\b[G1-4][NS]-[A-Z]\\d\\d\\b");

	  public void process(JCas aJCas) {
	    
	    // The JCas object is the data object inside UIMA where all the 
	    // information is stored. It contains all annotations created by 
	    // previous annotators, and the document text to be analyzed.
	    
	    // get document text from JCas
	    String docText = aJCas.getDocumentText();
	    
	    // search for Yorktown room numbers
	    Matcher matcher = mYorktownPattern.matcher(docText);
	    int pos = 0;
	    while (matcher.find(pos)) {
	      // match found - create the match as annotation in 
	      // the JCas with some additional meta information
	      RoomNumber annotation = new RoomNumber(aJCas);
	      annotation.setBegin(matcher.start());
	      annotation.setEnd(matcher.end());
	      Building building = new Building(aJCas);
	      building.setBegin(matcher.start());
	      building.setEnd(matcher.end());
	      annotation.setBuilding(building);
	      annotation.addToIndexes();
	      pos = matcher.end();
	    }
	  
	    // search for Hawthorne room numbers
	    matcher = mHawthornePattern.matcher(docText);
	    pos = 0;
	    while (matcher.find(pos)) {
	      // match found - create the match as annotation in 
	      // the JCas with some additional meta information
	      RoomNumber annotation = new RoomNumber(aJCas);
	      annotation.setBegin(matcher.start());
	      annotation.setEnd(matcher.end());
	      Building building = new Building(aJCas);
	      building.setBegin(matcher.start());
	      building.setEnd(matcher.end());
	      annotation.setBuilding(building);
	      annotation.addToIndexes();
	      pos = matcher.end();
	    }
	  }


}
