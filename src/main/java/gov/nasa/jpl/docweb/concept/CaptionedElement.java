package gov.nasa.jpl.docweb.concept;


import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "CaptionedElement")
public interface CaptionedElement extends PresentationElement {

	@Iri(URI.DOCWEB + "caption")
	public String getCaption();
	
	@Iri(URI.DOCWEB + "caption")
	public void setCaption(String caption);
}
