package gov.nasa.jpl.docweb.concept;

import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "NamedElement")
public interface NamedElement extends ModelElement {
	
	@Iri(URI.DOCWEB + "name")
	public String getName();
	
	@Iri(URI.DOCWEB + "name")
	public void setName(String name);
	
}
