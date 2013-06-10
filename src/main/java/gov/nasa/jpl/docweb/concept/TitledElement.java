package gov.nasa.jpl.docweb.concept;


import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "TitledElement")
public interface TitledElement extends PresentationElement {

	@Iri(URI.DOCWEB + "title")
	public String getTitle();
	
	@Iri(URI.DOCWEB + "title")
	public void setTitle(String caption);
}
