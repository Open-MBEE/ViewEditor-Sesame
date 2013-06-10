package gov.nasa.jpl.docweb.concept;

public class URI {
	public static final String DOCWEB = "http://docgen.jpl.nasa.gov/ontologies/docweb#";
	public static final String PREFIX = "PREFIX docweb:<http://docgen.jpl.nasa.gov/ontologies/docweb#>\n" +
										"PREFIX rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
										"PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n";
	
	public static final String DATA = "http://docgen.jpl.nasa.gov/data/";
	public static final String USER = "http://docgen.jpl.nasa.gov/user/";
	
	public static final String ELEMENT_QUERY_BY_UUID = PREFIX + "SELECT ?me WHERE " + 
																"{ ?me docweb:uuid ?uuid . " +
																"  FILTER NOT EXISTS { ?nil docweb:oldVersion ?me . } }";
	
}
