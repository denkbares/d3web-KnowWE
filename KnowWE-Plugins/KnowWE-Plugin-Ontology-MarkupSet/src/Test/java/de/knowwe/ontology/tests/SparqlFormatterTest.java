package de.knowwe.ontology.tests;

import org.junit.Test;

import de.knowwe.ontology.sparql.SparqlFormatAction;

import static org.junit.Assert.assertEquals;

/**
 * Created by Maximilian Brell on 21.03.16.
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
				"ORDER BY ?Name ?CAS ?EC\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
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
				"%\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
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
				"%\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
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

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
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
				"%\n";

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
				"%\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
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
				"%\n";

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
				"\t\t?Substance <lns:hasFirstEC+Number> ?EC .\n" +
				"\t}\n" +
				"\tOPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"\t\n" +
				"\t?Substance lns:hasEstablished <lns:OECD+HPV+Chemical>\n" +
				"\t\n" +
				"}\n" +
				"\n" +
				"ORDER BY ?Name ?CAS ?EC\n" +
				"%\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
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
				"SELECT ?Substance ?CAS ?EC ?Name WHERE {\n" +
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
				"%\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
	}

	@Test
	public void testEncapsulatedSparql() {
		String testSparql = "%%Sparql\n" +
				"  SELECT ?BfR ?CC ?Dossier ?EC ?Name ?Decisions ?Reasons ?1 ?M1 ?2 ?M2 ?3 ?M3 ?4 ?M4 ?5 ?M5 ?6 ?M6 ?7 ?M7 ?8 ?M8 ?9 ?M9 ?10 ?M10 ?11 ?M11 ?Memos WHERE {\n" +
				"    ?CC rdf:type <lns:Compliance+Check> .\n" +
				"    OPTIONAL { ?CC <lns:hasDossier+UUID> ?Dossier }\n" +
				"    OPTIONAL { ?CC <lns:hasFirstEC+Number> ?EC }\n" +
				"    OPTIONAL { ?CC <lns:hasFirstSubstance+Name> ?Name }\n" +
				"    OPTIONAL { ?CC <lns:hasFirstBfR-Nummer> ?BfR }\n" +
				"    \n" +
				"      ?CC lns:hasSession ?Session .\n" +
				"      ?CC lns:hasEstablished ?DecisionsTO .\n" +
				"      ?DecisionsTO rdfs:subClassOf <lns:Decisions+on+Compliance+Abbaubarkeit> .\n" +
				"      ?DecisionsTO rdfs:label ?Decisions .\n" +
				"      Filter regex(?Decisions, \"UC Biotische Abbaubarkeit\")\n" +
				"      \n" +
				"      OPTIONAL {\n" +
				"      SELECT (GROUP_CONCAT(?Reason ; separator = \"\\n\") AS ?Reasons) WHERE {        \n" +
				"        ?CC lns:hasEstablished ?ReasonsTO .\n" +
				"        ?ReasonsTO rdfs:subClassOf <lns:Decisions+on+Compliance+Abbaubarkeit> .\n" +
				"        ?ReasonsTO rdfs:label ?Reason .\n" +
				"        Filter regex(?Reason, \"UCBA\")\n" +
				"        }\n" +
				"        ORDER BY ?Reason\n" +
				"      }\n" +
				"            \n" +
				"      OPTIONAL {\n" +
				"         ?Session lns:hasFact ?Fact4 .\n" +
				"         ?Fact4 lns:hasTerminologyObject <lns:UCBA1> .\n" +
				"         ?Fact4 lns:hasValue ?1 .\n" +
				"        }\n" +
				"\n" +
				"   \n" +
				"        \n" +
				"      OPTIONAL {\n" +
				"         ?Session lns:hasFact ?Fact7 .\n" +
				"         ?Fact7 lns:hasTerminologyObject <lns:UCBA4> .\n" +
				"         ?Fact7 lns:hasValue ?4 .\n" +
				"        }\n" +
				"\n" +
				"      OPTIONAL {\n" +
				"    \t?CC lns:hasMemo ?MemoId5 .\n" +
				"    \t?MemoId5 lns:hasFact ?Fact5x .\n" +
				"    \t?Fact5x lns:hasQuestion <lns:UCBA5> .\n" +
				"        ?MemoId5 lns:hasContent ?M5 .\n" +
				"       }\n" +
				"        \n" +
				"      OPTIONAL {\n" +
				"         ?Session lns:hasFact ?Fact9 .\n" +
				"         ?Fact9 lns:hasTerminologyObject <lns:UCBA6> .\n" +
				"         ?Fact9 lns:hasValue ?6 .\n" +
				"        }\n" +
				"\n" +
				"      OPTIONAL {\n" +
				"    \t?CC lns:hasMemo ?MemoId6 .\n" +
				"    \t?MemoId6 lns:hasFact ?Fact6x .\n" +
				"    \t?Fact6x lns:hasQuestion <lns:UCBA6> .\n" +
				"        ?MemoId6 lns:hasContent ?M6 .\n" +
				"       }\n" +
				"        \n" +
				"      OPTIONAL {\n" +
				"         ?Session lns:hasFact ?Fact10 .\n" +
				"         ?Fact10 lns:hasTerminologyObject <lns:UCBA7> .\n" +
				"         ?Fact10 lns:hasValue ?7 .\n" +
				"        }\n" +
				"\n" +
				"\n" +
				"                \n" +
				"      OPTIONAL {\n" +
				"         ?Session lns:hasFact ?Fact12 .\n" +
				"         ?Fact12 lns:hasTerminologyObject <lns:UCBA9> .\n" +
				"         ?Fact12 lns:hasValue ?9 .\n" +
				"        }\n" +
				"\n" +
				"\n" +
				"\n" +
				"          \n" +
				"      OPTIONAL {\n" +
				"         SELECT (GROUP_CONCAT(?Memo ; separator = \"\\n\") AS ?Memos) WHERE {\n" +
				"         ?CC lns:hasMemo ?MemoId .\n" +
				"         ?MemoId lns:hasTitle ?Titel .\n" +
				"         Filter regex(?Titel, \"BioDeg\")\n" +
				"         ?MemoId lns:hasContent ?Memo .\n" +
				"         }\n" +
				"         ORDER BY ?Memo\n" +
				"        }         \n" +
				"      }    \n" +
				"\n" +
				"GROUP BY ?BfR ?CC ?Dossier ?EC ?Name ?Decisions ?Reasons ?1 ?M1 ?2 ?M2 ?3 ?M3 ?4 ?M4 ?5 ?M5 ?6 ?M6 ?7 ?M7 ?8 ?M8 ?9 ?M9 ?10 ?M10 ?11 ?M11 ?Memos\n" +
				"ORDER BY ?BfR\n" +
				"  \n" +
				"@timeout: 120s\n" +
				"%\n";

		String expectedSparql = "%%Sparql\n" +
				"SELECT ?BfR ?CC ?Dossier ?EC ?Name ?Decisions ?Reasons ?1 ?M1 ?2 ?M2 ?3 ?M3 ?4 ?M4 ?5 ?M5 ?6 ?M6 ?7 ?M7 ?8 ?M8 ?9 ?M9 ?10 ?M10 ?11 ?M11 ?Memos WHERE {\n" +
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
				"\tFilter regex(?Decisions, \"UC Biotische Abbaubarkeit\")\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\tSELECT (GROUP_CONCAT(?Reason ; separator = \"\\n\") AS ?Reasons) WHERE {\n" +
				"\t\t\t?CC lns:hasEstablished ?ReasonsTO .\n" +
				"\t\t\t?ReasonsTO rdfs:subClassOf <lns:Decisions+on+Compliance+Abbaubarkeit> .\n" +
				"\t\t\t?ReasonsTO rdfs:label ?Reason .\n" +
				"\t\t\tFilter regex(?Reason, \"UCBA\")\n" +
				"\t\t}\n" +
				"\t\tORDER BY ?Reason\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact4 .\n" +
				"\t\t?Fact4 lns:hasTerminologyObject <lns:UCBA1> .\n" +
				"\t\t?Fact4 lns:hasValue ?1 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact7 .\n" +
				"\t\t?Fact7 lns:hasTerminologyObject <lns:UCBA4> .\n" +
				"\t\t?Fact7 lns:hasValue ?4 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?CC lns:hasMemo ?MemoId5 .\n" +
				"\t\t?MemoId5 lns:hasFact ?Fact5x .\n" +
				"\t\t?Fact5x lns:hasQuestion <lns:UCBA5> .\n" +
				"\t\t?MemoId5 lns:hasContent ?M5 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact9 .\n" +
				"\t\t?Fact9 lns:hasTerminologyObject <lns:UCBA6> .\n" +
				"\t\t?Fact9 lns:hasValue ?6 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?CC lns:hasMemo ?MemoId6 .\n" +
				"\t\t?MemoId6 lns:hasFact ?Fact6x .\n" +
				"\t\t?Fact6x lns:hasQuestion <lns:UCBA6> .\n" +
				"\t\t?MemoId6 lns:hasContent ?M6 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact10 .\n" +
				"\t\t?Fact10 lns:hasTerminologyObject <lns:UCBA7> .\n" +
				"\t\t?Fact10 lns:hasValue ?7 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Session lns:hasFact ?Fact12 .\n" +
				"\t\t?Fact12 lns:hasTerminologyObject <lns:UCBA9> .\n" +
				"\t\t?Fact12 lns:hasValue ?9 .\n" +
				"\t}\n" +
				"\t\n" +
				"\tOPTIONAL {\n" +
				"\t\tSELECT (GROUP_CONCAT(?Memo ; separator = \"\\n\") AS ?Memos) WHERE {\n" +
				"\t\t\t?CC lns:hasMemo ?MemoId .\n" +
				"\t\t\t?MemoId lns:hasTitle ?Titel .\n" +
				"\t\t\tFilter regex(?Titel, \"BioDeg\")\n" +
				"\t\t\t?MemoId lns:hasContent ?Memo .\n" +
				"\t\t}\n" +
				"\t\tORDER BY ?Memo\n" +
				"\t}\n" +
				"}\n" +
				"\n" +
				"GROUP BY ?BfR ?CC ?Dossier ?EC ?Name ?Decisions ?Reasons ?1 ?M1 ?2 ?M2 ?3 ?M3 ?4 ?M4 ?5 ?M5 ?6 ?M6 ?7 ?M7 ?8 ?M8 ?9 ?M9 ?10 ?M10 ?11 ?M11 ?Memos\n" +
				"ORDER BY ?BfR\n" +
				"\n" +
				"@timeout: 120s\n" +
				"%\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
	}

	@Test
	public void testIndention() {
		String testSparql = "SELECT { asdasd WHERE }";

		String expectedSparql = "SELECT {\tasdasd WHERE\n" +
				"}\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
	}

	//	@Test
	public void testQuotesInLiteral() {
		String testSparql = "  SELECT ?Substance ?CAS ?EC ?Name\n" +
				"  \n" +
				"  WHERE {\n" +
				"  \n" +
				"    ?Substance rdf:type lns:Substance .\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"    \n" +
				"    ?Substance lns:hasEstablished ?URI .\n" +
				"    ?URI rdfs:label \"Hallo Max, \\\\\\\"wie\\\" gehts\" .\n" +
				"    }\n" +
				"    ORDER BY ?Name ?CAS ?EC";

		String expectedSparql = "  SELECT ?Substance ?CAS ?EC ?Name\n" +
				"  \n" +
				"  WHERE {\n" +
				"  \n" +
				"    ?Substance rdf:type lns:Substance .\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstCAS+Number> ?CAS }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstEC+Number> ?EC }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstSubstance+Name> ?Name }\n" +
				"    OPTIONAL { ?Substance <lns:hasFirstGroup+Name> ?Name }\n" +
				"    \n" +
				"    ?Substance lns:hasEstablished ?URI .\n" +
				"    ?URI rdfs:label \"Hallo Max, \\\\\\\"wie\\\" gehts\" .\n" +
				"    }\n" +
				"    ORDER BY ?Name ?CAS ?EC\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
	}

	@Test
	public void testIndentationAfterLineBreak() {
		String testSparql = "SELECT ?name\n" +
				"?x ?w\n" +
				"WHERE {\n" +
				"\t{ ?x " +
				"\t\t\t  rdf:type   \n" +
				"si:Human ;\n" +
				"\t\trdfs:label ?name .} UNION {\n" +
				"\t\t?x rdf:type\n" +
				"\tsi:Animal    ;\n" +
				"\t\trdfs:label\t\n" +
				"\t?name .\n" +
				"\t}}";
		String expectedSparql = "SELECT ?name\n" +
				"\t?x ?w\n" +
				"WHERE {\n" +
				"\t{\t?x rdf:type\n" +
				"\t\t\tsi:Human ;\n" +
				"\t\t\trdfs:label ?name .\n" +
				"\t} UNION {\n" +
				"\t\t?x rdf:type\n" +
				"\t\t\tsi:Animal ;\n" +
				"\t\t\trdfs:label\n" +
				"\t\t\t\t?name .\n" +
				"\t}\n" +
				"}\n";

		assertEquals("Compared Sparqls are not equal", expectedSparql, format(testSparql));
	}

	@Test
	public void testMore() {
		String test = "%%SparqlSELECT ?Substance\n" +
				"\t\t  \t?Name\n" +
				"\t     ?CAS\n" +
				"\t\t\t\t?EC\n" +
				"\t\t\t\n\tWHERE {SELECT ?Substance\n" +
				"\t\t  \t?Name\n" +
				"\t?CAS\n" +
				"?EC\n" +
				"\t\t\t\n\tWHERE {\n?Substance rdf:type lns:Substance.OPTIONAL{?Substance\n" +
				"<lns:hasFirstSubstance+Name> ?Name ;\n" +
				"<lns:hasCAS+Number> ?CAS\n" +
				"\t}OPTIONAL { OPTIONAL { ?Substance <lns:hasCAS+Number> ?CAS}\n" +
				"?Substance <lns:hasCAS+Number> ?CAS}OPTIONAL {\n" +
				"?Substance <lns:hasEC+Number> ?EC.}}" +
				"\t?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL{?Substance\n" +
				"<lns:hasFirstSubstance+Name> ?Name ;\n" +
				"<lns:hasCAS+Number> ?CAS\n" +
				"\t}OPTIONAL { OPTIONAL { ?Substance <lns:hasCAS+Number> ?CAS}\n" +
				"?Substance <lns:hasCAS+Number> ?CAS}OPTIONAL {\n" +
				"?Substance <lns:hasEC+Number> ?EC\n" +
				".}}UNION{\n" +
				"\t\t?Substance\n" +
				"rdf:type\n" +
				"\tsi:Animal ;\n" +
				"\t\trdfs:label\n" +
				"\t\t\t\t\t\t?name .\n" +
				"}}}" +
				"\n" +
				"\n" +
				"\n" +
				"\tFILTER (!REGEX(STR(?Substance), \"=(S|G)\\\\d{7}$\")).{}{\n" +
				"ORDER BY ?Name\n" +
				"\n" +
				"@navigation: true\n" +
				"%\n" +
				"\n" +
				"\n" +
				"\n";
		String expected = "%%Sparql\n" +
				"SELECT ?Substance\n" +
				"\t?Name\n" +
				"\t?CAS\n" +
				"\t?EC\n" +
				"\n" +
				"WHERE {\tSELECT ?Substance\n" +
				"\t\t?Name\n" +
				"\t\t?CAS\n" +
				"\t\t?EC\n" +
				"\t\n" +
				"\tWHERE {\n" +
				"\t\t?Substance rdf:type lns:Substance .\n" +
				"\t\tOPTIONAL {\n" +
				"\t\t\t?Substance\n" +
				"\t\t\t\t<lns:hasFirstSubstance+Name> ?Name ;\n" +
				"\t\t\t\t<lns:hasCAS+Number> ?CAS\n" +
				"\t\t}\n" +
				"\t\tOPTIONAL {\n" +
				"\t\t\tOPTIONAL { ?Substance <lns:hasCAS+Number> ?CAS }\n" +
				"\t\t\t?Substance <lns:hasCAS+Number> ?CAS\n" +
				"\t\t}\n" +
				"\t\tOPTIONAL { ?Substance <lns:hasEC+Number> ?EC . }\n" +
				"\t} ?Substance rdf:type lns:Substance .\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Substance\n" +
				"\t\t\t<lns:hasFirstSubstance+Name> ?Name ;\n" +
				"\t\t\t<lns:hasCAS+Number> ?CAS\n" +
				"\t}\n" +
				"\tOPTIONAL {\n" +
				"\t\tOPTIONAL { ?Substance <lns:hasCAS+Number> ?CAS }\n" +
				"\t\t?Substance <lns:hasCAS+Number> ?CAS\n" +
				"\t}\n" +
				"\tOPTIONAL {\n" +
				"\t\t?Substance <lns:hasEC+Number> ?EC\n" +
				"\t\t.\n" +
				"\t}\n" +
				"} UNION {\n" +
				"\t?Substance\n" +
				"\t\trdf:type\n" +
				"\t\t\tsi:Animal ;\n" +
				"\t\trdfs:label\n" +
				"\t\t\t?name .\n" +
				"}\n" +
				"}\n" +
				"}\n" +
				"\n" +
				"FILTER (!REGEX(STR(?Substance), \"=(S|G)\\\\d{7}$\")) .\n" +
				"{\n" +
				"}\n" +
				"{\n" +
				"\tORDER BY ?Name\n" +
				"\t\n" +
				"\t@navigation: true\n" +
				"\t%\n";
		assertEquals("Compared Sparqls are not equal", expected, format(test));
	}

	public String format(String testSparql) {
		SparqlFormatAction.SparqlFormatter formatter = new SparqlFormatAction.SparqlFormatter(testSparql);
		return formatter.format();
	}
}
