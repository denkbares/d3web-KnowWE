package de.knowwe.ontology.tests;

import de.knowwe.ontology.sparql.SparqlFormatAction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by MB on 21.03.16.
 */
public class SparqlFormatterTest {

	@Test
	public void testSimpleSparql() {
		String testSparql =
				"  SELECT ?Substance ?CAS ?EC ?Name\n" +
						"  \n" +
						"  WHERE {\n" +
						"  \n" +
						"  \t  ?Substance     rdf:type lns:Substance .\n" +
						"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS . ?Substance <lns:hasFirstCAS+Number> ?CAS . ?Substance <lns:hasFirstCAS+Number> ?CAS. }\n" +
						"\t\tOPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
						"    \t\t    OPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
						"   \t OPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
						"    \n" +
						"    \n" +
						" \t\t\t   ?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
						"    \n\n\n\n" +
						" \t   }\n" +
						"      \t  ORDER BY ?Name ?CAS ?EC";

		String expectedSparql = "\nSELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Substance <lns:hasFirstCAS+Number> ?CAS .\n" +
				"\t\t?Substance <lns:hasFirstCAS+Number> ?CAS .\n" +
				"\t\t?Substance <lns:hasFirstCAS+Number> ?CAS .\n" +
				"\t}\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"ORDER BY ?Name ?CAS ?EC";

		SparqlFormatAction sparqlFormatAction = new SparqlFormatAction();
		String formattedSparql = sparqlFormatAction.formatSparql(new StringBuilder(testSparql)).toString();

		assertEquals("Compared Sparqls are not equal", expectedSparql, formattedSparql);
	}


	@Test
	public void testRecursiveCall() {
		String testSparql = "%%Sparql\n" +
				"\n" +
				"SELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE { \n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\tOPTIONAL { \n" +
				"\t\tSELECT ?Session (GROUP_CONCAT(?BwSt2 ; separator = \"\\n\" ) AS ?BwSt)\n" +
				"\t\tWHERE {\n" +
				"\t\t\t?Session lns:hasFact ?Fact2 .\n" +
				"\t\t\t?Fact2 lns:hasTerminologyObject ?BwStTO .\n" +
				"\t\t\t?Fact2 lns:hasValue ?Score2 .\n" +
				"\t\t\t?BwStTO rdfs:subClassOf <lns:Decisions+on+Responsibility+within+DE> .\n" +
				"\t\t\t?BwStTO rdfs:label ?BwSt1 .\n" +
				"\t\t\t\n" +
				"\t\t\tBIND (CONCAT( \"- \" , ?BwSt1) as ?BwSt2 ) .\n" +
				"\t\t\tFILTER (?Score2 > 41) .\n" +
				"\t\t}\n" +
				"\t\tGROUP BY ?Session \n" +
				"\t\tORDER BY ?BwSt\n" +
				"}\n" +
				"\n" +
				"?Substance lns:hasEstablished <lns:OECD+HPV+Chemical> \n" +
				"}\n" +
				"\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%";

		String expectedSparql = "%%Sparql\n" +
				"\n" +
				"SELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\tOPTIONAL {\n" +
				"\t\tSELECT ?Session (GROUP_CONCAT(?BwSt2 ; separator = \"\\n\" ) AS ?BwSt)\n" +
				"\t\tWHERE {\n" +
				"\t\t\t?Session lns:hasFact ?Fact2 .\n" +
				"\t\t\t?Fact2 lns:hasTerminologyObject ?BwStTO .\n" +
				"\t\t\t?Fact2 lns:hasValue ?Score2 .\n" +
				"\t\t\t?BwStTO rdfs:subClassOf <lns:Decisions+on+Responsibility+within+DE> .\n" +
				"\t\t\t?BwStTO rdfs:label ?BwSt1 .\n" +
				"\t\t\t\n" +
				"\t\t\tBIND (CONCAT( \"- \" , ?BwSt1) as ?BwSt2 ) .\n" +
				"\t\t\tFILTER (?Score2 > 41) .\n" +
				"\t\t}\n" +
				"\t\tGROUP BY ?Session\n" +
				"\t\tORDER BY ?BwSt\n" +
				"\t}\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"}\n" +
				"\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%";

		SparqlFormatAction sparqlFormatAction = new SparqlFormatAction();
		String formattedSparql = sparqlFormatAction.formatSparql(new StringBuilder(testSparql)).toString();

		assertEquals("Compared Sparqls are not equal", expectedSparql, formattedSparql);
	}


	@Test
	public void testDeeperRecursiveCall() {
		String testSparql = "%%Sparql\n" +
				"SELECT ?BfR ?CC ?Dossier ?EC ?Name ?Decisions ?Reasons ?1 ?M1 ?2 ?M2 ?3 ?M3 ?4 ?M4 ?5 ?M5 ?6 ?M6 ?7 ?M7 ?8 ?M8 ?9 ?M9 ?Memos\n" +
				"WHERE {\n" +
				"\t?CC rdf:type <lns:Compliance+Check> .\n" +
				"\tOPTIONAL { ?CC <lns:hasDossier+UUID> ?Dossier }\n" +
				"\tOPTIONAL { ?CC <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?CC <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?CC <lns:hasFirstBfR-Nummer> ?BfR }\n" +
				"\t\n" +
				"\t?CC lns:hasSession ?Session .\n" +
				"\t?CC lns:hasEstablished ?DecisionsTO .\n" +
				"\t?DecisionsTO rdfs:subClassOf <lns:Decisions+on+Compliance+Abbaubarkeit> .\n" +
				"\t?DecisionsTO rdfs:label ?Decisions .\n" +
				"\tFilter regex(?Decisions, \"UC Abiotische Abbaubarkeit\" )\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\tSELECT (GROUP_CONCAT(?Reason ; separator = \"\\n\" ) AS ?Reasons)\n" +
				"\t\tWHERE {\n" +
				"\t\t\t?CC lns:hasEstablished ?ReasonsTO .\n" +
				"\t\t\t?ReasonsTO rdfs:subClassOf <lns:Decisions+on+Compliance+Abbaubarkeit> .\n" +
				"\t\t\t?ReasonsTO rdfs:label ?Reason .\n" +
				"\t\t\tFilter regex(?Reason, \"UCABA\" )\n" +
				"\t\t}\n" +
				"\t\tORDER BY ?Reason\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact4 .\n" +
				"\t\t?Fact4 lns:hasTerminologyObject <lns:UCABA1> .\n" +
				"\t\t?Fact4 lns:hasValue ?1 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?CC lns:hasMemo ?MemoId1 .\n" +
				"\t\t?MemoId1 lns:hasFact ?Fact1x .\n" +
				"\t\t?Fact1x lns:hasQuestion <lns:UCABA1> .\n" +
				"\t\t?MemoId1 lns:hasContent ?M1 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact5 .\n" +
				"\t\t?Fact5 lns:hasTerminologyObject <lns:UCABA2> .\n" +
				"\t\t?Fact5 lns:hasValue ?2 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\tSELECT (GROUP_CONCAT(?Memo ; separator = \"\\n\" ) AS ?Memos)\n" +
				"\t\tWHERE {\n" +
				"\t?CC lns:hasMemo ?MemoId .\n" +
				"\tOPTIONAL {\n" +
				"\t\tSELECT (GROUP_CONCAT(?Memo ; separator = \"\\n\" ) AS ?Memos)\n" +
				"\t\tWHERE {\n" +
				"\t\t\t?CC lns:hasMemo ?MemoId .\n" +
				"OPTIONAL {\n" +
				"\tSELECT (GROUP_CONCAT(?Memo ; separator = \"\\n\" ) AS ?Memos)\n" +
				"\tWHERE {\n" +
				"\t\t?CC lns:hasMemo ?MemoId .\n" +
				"\t\t?MemoId lns:hasTitle ?Titel .\n" +
				"\t\tFilter regex(?Titel, \"AbioDeg\" )\n" +
				"\t\t?MemoId lns:hasContent ?Memo .\n" +
				"\t}\n" +
				"\tORDER BY ?Memo\n" +
				"}\n" +
				"?MemoId lns:hasTitle ?Titel .\n" +
				"Filter regex(?Titel, \"AbioDeg\" )\n" +
				"?MemoId lns:hasContent ?Memo .\n" +
				"}\n" +
				"ORDER BY ?Memo\n" +
				"}\n" +
				"?MemoId lns:hasTitle ?Titel .\n" +
				"Filter regex(?Titel, \"AbioDeg\" )\n" +
				"?MemoId lns:hasContent ?Memo .\n" +
				"}\n" +
				"ORDER BY ?Memo\n" +
				"}\n" +
				"}\n" +
				"\n" +
				"GROUP BY ?BfR ?CC ?Dossier ?EC ?Name ?Decisions ?Reasons ?1 ?M1 ?2 ?M2 ?3 ?M3 ?4 ?M4 ?5 ?M5 ?6 ?M6 ?7 ?M7 ?8 ?M8 ?9 ?M9 ?Memos\n" +
				"ORDER BY ?BfR\n" +
				"\n" +
				"@timeout: 60s\n" +
				"%";

		String expectedSparql = "%%Sparql\n" +
				"SELECT ?BfR ?CC ?Dossier ?EC ?Name ?Decisions ?Reasons ?1 ?M1 ?2 ?M2 ?3 ?M3 ?4 ?M4 ?5 ?M5 ?6 ?M6 ?7 ?M7 ?8 ?M8 ?9 ?M9 ?Memos\n" +
				"WHERE {\n" +
				"\t?CC rdf:type <lns:Compliance+Check> .\n" +
				"\tOPTIONAL { ?CC <lns:hasDossier+UUID> ?Dossier }\n" +
				"\tOPTIONAL { ?CC <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?CC <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?CC <lns:hasFirstBfR-Nummer> ?BfR }\n" +
				"\t\n" +
				"\t?CC lns:hasSession ?Session .\n" +
				"\t?CC lns:hasEstablished ?DecisionsTO .\n" +
				"\t?DecisionsTO rdfs:subClassOf <lns:Decisions+on+Compliance+Abbaubarkeit> .\n" +
				"\t?DecisionsTO rdfs:label ?Decisions .\n" +
				"\tFilter regex(?Decisions, \"UC Abiotische Abbaubarkeit\" )\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\tSELECT (GROUP_CONCAT(?Reason ; separator = \"\\n\" ) AS ?Reasons)\n" +
				"\t\tWHERE {\n" +
				"\t\t\t?CC lns:hasEstablished ?ReasonsTO .\n" +
				"\t\t\t?ReasonsTO rdfs:subClassOf <lns:Decisions+on+Compliance+Abbaubarkeit> .\n" +
				"\t\t\t?ReasonsTO rdfs:label ?Reason .\n" +
				"\t\t\tFilter regex(?Reason, \"UCABA\" )\n" +
				"\t\t}\n" +
				"\t\tORDER BY ?Reason\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact4 .\n" +
				"\t\t?Fact4 lns:hasTerminologyObject <lns:UCABA1> .\n" +
				"\t\t?Fact4 lns:hasValue ?1 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?CC lns:hasMemo ?MemoId1 .\n" +
				"\t\t?MemoId1 lns:hasFact ?Fact1x .\n" +
				"\t\t?Fact1x lns:hasQuestion <lns:UCABA1> .\n" +
				"\t\t?MemoId1 lns:hasContent ?M1 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact5 .\n" +
				"\t\t?Fact5 lns:hasTerminologyObject <lns:UCABA2> .\n" +
				"\t\t?Fact5 lns:hasValue ?2 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\tSELECT (GROUP_CONCAT(?Memo ; separator = \"\\n\" ) AS ?Memos)\n" +
				"\t\tWHERE {\n" +
				"\t\t\t?CC lns:hasMemo ?MemoId .\n" +
				"\t\t\tOPTIONAL {\n" +
				"\t\t\t\tSELECT (GROUP_CONCAT(?Memo ; separator = \"\\n\" ) AS ?Memos)\n" +
				"\t\t\t\tWHERE {\n" +
				"\t\t\t\t\t?CC lns:hasMemo ?MemoId .\n" +
				"\t\t\t\t\tOPTIONAL {\n" +
				"\t\t\t\t\t\tSELECT (GROUP_CONCAT(?Memo ; separator = \"\\n\" ) AS ?Memos)\n" +
				"\t\t\t\t\t\tWHERE {\n" +
				"\t\t\t\t\t\t\t?CC lns:hasMemo ?MemoId .\n" +
				"\t\t\t\t\t\t\t?MemoId lns:hasTitle ?Titel .\n" +
				"\t\t\t\t\t\t\tFilter regex(?Titel, \"AbioDeg\" )\n" +
				"\t\t\t\t\t\t\t?MemoId lns:hasContent ?Memo .\n" +
				"\t\t\t\t\t\t}\n" +
				"\t\t\t\t\t\tORDER BY ?Memo\n" +
				"\t\t\t\t\t}\n" +
				"\t\t\t\t\t?MemoId lns:hasTitle ?Titel .\n" +
				"\t\t\t\t\tFilter regex(?Titel, \"AbioDeg\" )\n" +
				"\t\t\t\t\t?MemoId lns:hasContent ?Memo .\n" +
				"\t\t\t\t}\n" +
				"\t\t\t\tORDER BY ?Memo\n" +
				"\t\t\t}\n" +
				"\t\t\t?MemoId lns:hasTitle ?Titel .\n" +
				"\t\t\tFilter regex(?Titel, \"AbioDeg\" )\n" +
				"\t\t\t?MemoId lns:hasContent ?Memo .\n" +
				"\t\t}\n" +
				"\t\tORDER BY ?Memo\n" +
				"\t}\n" +
				"}\n" +
				"\n" +
				"GROUP BY ?BfR ?CC ?Dossier ?EC ?Name ?Decisions ?Reasons ?1 ?M1 ?2 ?M2 ?3 ?M3 ?4 ?M4 ?5 ?M5 ?6 ?M6 ?7 ?M7 ?8 ?M8 ?9 ?M9 ?Memos\n" +
				"ORDER BY ?BfR\n" +
				"\n" +
				"@timeout: 60s\n" +
				"%";

		SparqlFormatAction sparqlFormatAction = new SparqlFormatAction();
		String formattedSparql = sparqlFormatAction.formatSparql(new StringBuilder(testSparql)).toString();

		assertEquals("Compared Sparqls are not equal", expectedSparql, formattedSparql);
	}


	@Test
	public void testSELECTIndentedWrong() {
		String testSparql = "%%Sparql SELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%\n";

		String expectedSparql = "%%Sparql\n" +
				"SELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%\n";

		SparqlFormatAction sparqlFormatAction = new SparqlFormatAction();
		String formattedSparql = sparqlFormatAction.formatSparql(new StringBuilder(testSparql)).toString();

		assertEquals("Compared Sparqls are not equal", expectedSparql, formattedSparql);
	}


	@Test
	public void testSimpleSparql2() {
		String testSparql = "%%Sparql\n" +
				"\n" +
				"  SELECT ?Substance ?CAS ?EC ?Name\n" +
				"  \n" +
				"  WHERE {\n" +
				"  \n" +
				"    ?Substance rdf:type lns:Substance .\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"    \n" +
				"    ?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"    \n" +
				"    }\n" +
				"        ORDER BY ?Name ?CAS ?EC\n" +
				"%";

		String expectedSparql = "%%Sparql\n" +
				"\n" +
				"SELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%";

		SparqlFormatAction sparqlFormatAction = new SparqlFormatAction();
		String formattedSparql = sparqlFormatAction.formatSparql(new StringBuilder(testSparql)).toString();

		assertEquals("Compared Sparqls are not equal", expectedSparql, formattedSparql);
	}


	@Test
	public void testSparqlWithMoreThanThreeAttributesInOptional() {
		String testSparql = "%%Sparql\n" +
				"\n" +
				"SELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL {?Substance <lns:hasFirstCAS+Number> ?CAS .?Substance <lns:hasFirstCAS+Number> ?CAS}\n" +
				"\tOPTIONAL {?Substance <lns:hasFirstEC+Number> ?EC . ?Substance <lns:hasFirstEC+Number> ?EC}\n" +
				"\tOPTIONAL {?Substance <lns:hasFirstSubstance+Name> ?Name . ?Substance <lns:hasFirstEC+Number> ?EC . ?Substance <lns:hasFirstEC+Number> ?EC . \n" +
				"\t?Substance <lns:hasFirstEC+Number> ?EC .\n" +
				"\t}\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%";

		String expectedSparql = "%%Sparql\n" +
				"\n" +
				"SELECT ?Substance ?CAS ?EC ?Name\n" +
				"\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Substance <lns:hasFirstCAS+Number> ?CAS .\n" +
				"\t\t?Substance <lns:hasFirstCAS+Number> ?CAS\n" +
				"\t}\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Substance <lns:hasFirstEC+Number> ?EC .\n" +
				"\t\t?Substance <lns:hasFirstEC+Number> ?EC\n" +
				"\t}\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Substance <lns:hasFirstSubstance+Name> ?Name .\n" +
				"\t\t?Substance <lns:hasFirstEC+Number> ?EC .\n" +
				"\t\t?Substance <lns:hasFirstEC+Number> ?EC .\n" +
				"\t\t\n" +
				"\t\t?Substance <lns:hasFirstEC+Number> ?EC .\n" +
				"\t}\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%";

		SparqlFormatAction sparqlFormatAction = new SparqlFormatAction();
		String formattedSparql = sparqlFormatAction.formatSparql(new StringBuilder(testSparql)).toString();

		assertEquals("Compared Sparqls are not equal", expectedSparql, formattedSparql);
	}

	@Test
	public void testWHEREIndentedWrong() {
		String testSparql = "%%Sparql\n" +
				"\n" +
				"SELECT ?Substance ?CAS ?EC ?Name WHERE { \n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%";

		String expectedSparql = "%%Sparql\n" +
				"\n" +
				"SELECT ?Substance ?CAS ?EC ?Name\n" +
				"WHERE {\n" +
				"\t\n" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%";

		SparqlFormatAction sparqlFormatAction = new SparqlFormatAction();
		String formattedSparql = sparqlFormatAction.formatSparql(new StringBuilder(testSparql)).toString();

		assertEquals("Compared Sparqls are not equal", expectedSparql, formattedSparql);
	}

}
