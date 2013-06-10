package gov.nasa.jpl.docweb.concept;

import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "Property")
public interface Property extends NamedElement {

	@Iri(URI.DOCWEB + "defaultValue")
	public String getDefaultValue();
	
	@Iri(URI.DOCWEB + "defaultValue")
	public void setDefaultValue(String defaultValue);
}
