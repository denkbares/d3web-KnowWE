package de.knowwe.ontology.tests;

import org.junit.Test;

import de.knowwe.ontology.turtle.TurtleFormatAction;

import static org.junit.Assert.assertEquals;

/**
 * Created by Adrian Müller on 30.09.16.
 */
public class TurtleFormatterTest {

	@Test
	public void testNothingSpecial() {
		String test = "%%turtle\n" +
				"si:SimpsonsConcept\n" +
				"a\n" +
				"rdfs:Class .\n" +
				"\n" +
				"si:LivingBeing rdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Lebewesen\"@de,\n" +
				"\t\t\"Living being\",\n" +
				"bla,\n" +
				"bla,\n" +
				"bla .\n" +
				"\n" +
				"\n" +
				"si:Human\n" +
				"\trdfs:subClassOf si:LivingBeing ;\n" +
				"\n" +
				"\n" +
				"\n" +
				"\ta rdfs:Class ;\n" +
				"\trdfs:label \"Mensch\"@de, \"Human\"@en .\n" +
				"\n" +
				"si:Animal\n" +
				"\trdfs:subClassOf si:LivingBeing ;\n" +
				"\trdfs:label \"Tier\"@de, \"Animal\"@en .\n" +
				"\n" +
				"si:GenderType\n" +
				"\trdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Geschlecht\"@de, \"Gender\"@en .\n" +
				"\n" +
				"si:male\n" +
				"\trdf:type si:GenderType ;\n" +
				"\trdfs:label \"männlich\"@de, \"male\"@en .\n" +
				"\n" +
				"si:female\n" +
				"\trdf:type si:GenderType ;\n" +
				"\trdfs:label \"weiblich\"@de, \"female\"@en .\n" +
				"\n" +
				"si:Building\n" +
				"\trdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Gebäude\"@de, \"Building\"@en .\n" +
				"\n" +
				"si:Location\n" +
				"\trdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Ort\"@de, \"Location\"@en .\n" +
				"\n" +
				"si:Powerplant\n" +
				"\trdfs:subClassOf si:Building ;\n" +
				"\trdfs:label \"Kraftwerk\"@de, \"Power plant\"@en .\n" +
				"%\n" +
				"\n" +
				"\n" +
				"\n" +
				"\n";

		String expected = "%%turtle\n" +
				"si:SimpsonsConcept\n" +
				"\ta\n" +
				"\t\trdfs:Class .\n" +
				"\n" +
				"si:LivingBeing rdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Lebewesen\"@de,\n" +
				"\t\t\"Living being\",\n" +
				"\t\tbla,\n" +
				"\t\tbla,\n" +
				"\t\tbla .\n" +
				"\n" +
				"si:Human\n" +
				"\trdfs:subClassOf si:LivingBeing ;\n" +
				"\t\n" +
				"\ta rdfs:Class ;\n" +
				"\trdfs:label \"Mensch\"@de, \"Human\"@en .\n" +
				"\n" +
				"si:Animal\n" +
				"\trdfs:subClassOf si:LivingBeing ;\n" +
				"\trdfs:label \"Tier\"@de, \"Animal\"@en .\n" +
				"\n" +
				"si:GenderType\n" +
				"\trdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Geschlecht\"@de, \"Gender\"@en .\n" +
				"\n" +
				"si:male\n" +
				"\trdf:type si:GenderType ;\n" +
				"\trdfs:label \"männlich\"@de, \"male\"@en .\n" +
				"\n" +
				"si:female\n" +
				"\trdf:type si:GenderType ;\n" +
				"\trdfs:label \"weiblich\"@de, \"female\"@en .\n" +
				"\n" +
				"si:Building\n" +
				"\trdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Gebäude\"@de, \"Building\"@en .\n" +
				"\n" +
				"si:Location\n" +
				"\trdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Ort\"@de, \"Location\"@en .\n" +
				"\n" +
				"si:Powerplant\n" +
				"\trdfs:subClassOf si:Building ;\n" +
				"\trdfs:label \"Kraftwerk\"@de, \"Power plant\"@en .\n" +
				"%\n";

		assertEquals("Comparing failed.", expected, format(test));

	}

	@Test
	public void testEmptyLines() {
		String test = "%%turtle\n" +
				"si:SimpsonsConcept\n" +
				"\t?a\n" +
				"\t\trdfs:Class .\n" +
				"\n" +
				"si:LivingBeing rdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Lebewesen\"@de, \"Living being\" .\n" +
				"\n" +
				"si:Human\n" +
				"\trdfs:subClassOf si:LivingBeing ;\n" +
				"\t?a rdfs:Class ;\n" +
				"\t\n" +
				"\t\n" +
				"\t\n" +
				"\trdfs:label \"Mensch\"@de, \"Human\"@en .\n" +
				"\n" +
				"%";

		String expected = "%%turtle\n" +
				"si:SimpsonsConcept\n" +
				"\t?a\n" +
				"\t\trdfs:Class .\n" +
				"\n" +
				"si:LivingBeing rdfs:subClassOf si:SimpsonsConcept ;\n" +
				"\trdfs:label \"Lebewesen\"@de, \"Living being\" .\n" +
				"\n" +
				"si:Human\n" +
				"\trdfs:subClassOf si:LivingBeing ;\n" +
				"\t?a rdfs:Class ;\n" +
				"\t\n" +
				"\trdfs:label \"Mensch\"@de, \"Human\"@en .\n" +
				"\n" +
				"%\n";

		assertEquals("Comparing failed.", expected, format(test));

	}

	@Test
	public void testSomething() {
		String test = "";

		String expected = test;

		assertEquals("Comparing failed.", expected, format(test));

	}

	public String format(String testSparql) {
		TurtleFormatAction.TurtleFormatter formatter = new TurtleFormatAction.TurtleFormatter(testSparql);
		return formatter.format();
	}
}
