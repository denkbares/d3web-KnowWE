/* Created on 25. Januar 2005, 21:13 */
package de.d3web.textParser.decisionTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import de.d3web.kernel.domainModel.KnowledgeBaseManagement;
import de.d3web.kernel.domainModel.NamedObject;
import de.d3web.report.Message;
import de.d3web.report.Report;

/**
 * Implementierung des Interface ValueChecker zur ï¿½berprï¿½fung
 * der Werte einer Attribut-Tabelle
 * @author Andreas Klar
 */
public class AttributeTableValueChecker extends DecisionTableValueChecker {
    
    private AttributeConfigReader attrReader;
    
    /**
     * Erstellt eine neue Instanz von Value-Checker fï¿½r Attribut-Tabellen,
     * welche das ï¿½bergebene KnowledgeBaseManagement und den AttributeConfigReader
     * zur Werte-ï¿½berprï¿½fung benutzt.
     * @param reader AttributConfigReader, der zur ï¿½berprï¿½fung der Werte benutzt werden soll
     * @param kbm KnowledgeBaseManagement, das zur ï¿½berprï¿½fung der Werte benutzt werden soll
     */
    public AttributeTableValueChecker(AttributeConfigReader reader,
    								KnowledgeBaseManagement kbm) {
        super(null, kbm);
        this.attrReader = reader;
    }
    
    /* (non-Javadoc)
     * @see de.d3web.textParser.decisionTable.ValueChecker#checkValues(de.d3web.textParser.decisionTable.DecisionTable)
     */
    @Override
	public Report checkValues(DecisionTable table) {
    	Report report = new Report();
        
        // Objekte ï¿½berprï¿½fen
        report.addAll(checkObjects(table));
        report.addAll(checkAnswers(table, 1, table.rows()));
        report.addAll(checkAttributes(table));
        report.addAll(checkAttributeValues(table));

        return report;
    }
    
    
    /**
     * ï¿½berprï¿½ft, ob in der 1. Spalte gï¿½ltige Frageklassen, Fragen bzw. Diagnosen stehen
     * @param table Zu ï¿½berprï¿½fende Tabelle
     * @return Liste mit allen Fehlermeldungen oder leere Liste, falls die Prï¿½fung
     * erfolgreich war
     */
    private Report checkObjects(DecisionTable table) {
    	Report report = new Report();
        
    	for (int i=1; i<table.rows(); i++) {
    		String objectName = table.get(i,0);
            if (!objectName.equals("")) {
            	if (kbm.findQContainer(objectName)==null &&
            		kbm.findQuestion(objectName)==null &&
					kbm.findDiagnosis(objectName)==null)
					report.error(MessageGenerator.invalidNotQASetOrDiagnosis(i,1,objectName));
            }
        }
    	
    	return report;
    }
    
    /**
     * ï¿½berprï¿½ft die Attribute auf Gï¿½ltigkeit
     * @param table Zu ï¿½berprï¿½fende Tabelle
     * @return Liste mit allen Fehlermeldungen oder leere Liste, falls die Prï¿½fung
     * erfolgreich war
     */
    private Report checkAttributes(DecisionTable table) {
    	Report report = new Report();
    	
    	for (int j=1; j<table.columns(); j++) {
    		if (!table.isEmptyCell(0,j)) {
    			String attributeText = table.get(0,j);
    			if (attributeText.startsWith(AttributeConfigReader.SHARED_LOCAL_WEIGHT)) {
    				if (attributeText.indexOf("(")==-1 || attributeText.indexOf(")")==-1)
    					report.error(MessageGenerator.missingDiagnosisForSharedLocalWeight(
    							0,j,AttributeConfigReader.SHARED_LOCAL_WEIGHT,attributeText));
    				else {
    					String diaName = attributeText.substring(attributeText.indexOf("(")+1,
																attributeText.indexOf(")"));
    					if (kbm.findDiagnosis(diaName)==null)
    						report.error(MessageGenerator.invalidDiagnosisForSharedLocalWeight(
    								0,j,AttributeConfigReader.SHARED_LOCAL_WEIGHT,diaName));
    				}
    				attributeText = AttributeConfigReader.SHARED_LOCAL_WEIGHT;
    			}
    			if (!attrReader.isProperty(attributeText))
    				report.error(MessageGenerator.invalidAttribute(0,j,attributeText));
    		}
    	}
    	// TODO: Bei Abnormalitï¿½ten und Lokalen Gewichten: sicherstellen,
    	// dass keine Werte direkt bei den Fragen stehen
    	return report;
    }
    
    /**
     * ï¿½berprï¿½ft die Werte, welche gesetzt werden sollen
     * @param table Zu ï¿½berprï¿½fende Tabelle
     * @return Liste mit allen Fehlermeldungen oder leere Liste, falls die Prï¿½fung
     * erfolgreich war
     */
    private Report checkAttributeValues(DecisionTable table) {
    	Report report = new Report();

    	for (int i=1; i<table.rows(); i++) {
    		for (int j=1; j<table.columns(); j++) {
    			if (!table.isEmptyCell(i,j)) {
    				String objectName = table.getQuestionText(i);
    				String attributeText = table.get(0,j);
    				if (attributeText.equals("") || !attrReader.isProperty(attributeText))
    					continue;  // continue if there's no valid attribute
    				
    				// check if special attribute values are correct
    				report.addAll(checkAttributeValueSpecialCases(table, i, j));
    				// ensure that attribute is allowed for given object
    				report.addAll(checkAttributeAllowedForObject(objectName, attributeText, i, j));
    				// ensure that value is valid for given attribute
    				if (!attrReader.isAllowedAttributeValue(attributeText, table.get(i,j)))
    					report.error(MessageGenerator.invalidAttributeValue(i,j,
    							attributeText,table.get(i,j)));
    			}
    		}
    	}
    	return report;
    }
    
    private Report checkAttributeValueSpecialCases(DecisionTable table, int row, int column) {
    	Report report = new Report();
    	String attributeText = table.get(0,column);
		if (attributeText.equals(AttributeConfigReader.LINK)) {
			if (table.get(row,column).indexOf("|")==-1)
				report.warning(MessageGenerator.missingLinkDescriptor(row,column));
		}
		else if (attributeText.equals(AttributeConfigReader.MEDIA)) {
			if (table.get(row,column).indexOf("|")==-1)
				report.warning(MessageGenerator.missingImageDescriptor(row,column));
		}
		else if (attributeText.equals(AttributeConfigReader.SHARED_ABNORMALITY)) {
			if (table.get(row,1).equals("")) {
				if (table.get(row,0).equals("")) 
					report.error(MessageGenerator.missingAnswer(row,column));
				else
					report.error(MessageGenerator.abnormalityValueNotAllowedForQuestion(row,column));
			}			
		}
    	else if (attributeText.startsWith(AttributeConfigReader.SHARED_LOCAL_WEIGHT)) {
			if (table.get(row,1).equals("")) {
				if (table.get(row,0).equals("")) 
					report.error(MessageGenerator.missingAnswer(row,column));
				else
					report.error(MessageGenerator.localWeightValueNotAllowedForQuestion(row,column));
			}			
    	}
    	else if (attributeText.startsWith(AttributeConfigReader.NUM2CHOICE)) {
			if (table.get(row,1).equals("")) {
				if (table.get(row,0).equals("")) 
					report.error(MessageGenerator.missingAnswer(row,column));
				else
					report.error(MessageGenerator.num2ChoiceNotAllowedForQuestion(row, column));
			}
			else{
				String attributeValue = table.get(row,column);
				ArrayList<Double> allValues = new ArrayList<Double>();
				Scanner scan = new Scanner(attributeValue).useDelimiter(";");
				while (scan.hasNext()) {
					Double value = Double.parseDouble(scan.next().replaceAll(",", "."));
					allValues.add(value);
				}
				Double[] allValuesInArray = new Double[allValues.size()];
				Iterator iter = allValues.iterator();
				int i = 0;
				while (iter.hasNext()) {
					allValuesInArray[i] = (Double) iter.next();
					i++;
				}
				if(!this.checkOrder(allValuesInArray)){
					report.error(new Message(
					"Num2Choice : schema array is neither ascending nor descending"));
				
				}
			}
    	}
		else if (table.get(row,0).equals("") &! attrReader.isMultiple(attributeText)) {
			report.error(MessageGenerator.multiplicityNotAllowed(row,column,attributeText));
		}
		
		return report;
    }
 
    /**
     * Checks if the attribute represented by <CODE>attributeText<CODE> is allowed
     * for the NamedObject represented by <CODE>objectName</CODE>
     * @param objectName
     * @param attributeText
     * @param row row number of the attribute value
     * @param column column number of the attribute value
     * @return
     */
    private Report checkAttributeAllowedForObject(String objectName, String attributeText, int row, int column) {
    	Report report = new Report();
    	
    	NamedObject object = kbm.findQContainer(objectName);
		if (object==null) object = kbm.findQuestion(objectName);
		if (object==null) object = kbm.findDiagnosis(objectName);
		if (object==null) return report;  // continue if there's no valid object
		String objectClass = object.getClass().getSimpleName();
	
		if (!attrReader.isAllowedObject(attributeText, object))
			report.error(MessageGenerator.AttributeNotAllowedForObject(row,column,
					attributeText,objectClass));
		
		return report;
    }
    
    //Begin change 
	private boolean checkOrder(Double[] toCheck) {
		if (toCheck.length > 1) {
			// descending
			if (toCheck[0] > toCheck[1]) {
				Double oldValue = toCheck[0];
				for (Double value : toCheck) {
					if (value > oldValue) {
						return false;
					}
					oldValue = value;
				}
			}
			// ascending
			else {
				Double oldValue = toCheck[0];
				for (Double value : toCheck) {
					if (value < oldValue) {
						return false;
					}
					oldValue = value;
				}
			}
		}
		return true;

	}
	// End Change
}
