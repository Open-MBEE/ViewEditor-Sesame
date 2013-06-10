package gov.nasa.jpl.docweb.concept;

import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "Table")
public interface Table extends TitledElement, CaptionedElement, LeafElement, NonContainerElement {

	@Iri(URI.DOCWEB + "header")
	public String getHeader();
	
	@Iri(URI.DOCWEB + "header")
	public void setHeader(String text);
	
	@Iri(URI.DOCWEB + "body")
	public String getBody();
	
	@Iri(URI.DOCWEB + "body")
	public void setBody(String text);
	
	@Iri(URI.DOCWEB + "style")
	public void setStyle(String style);
	
	@Iri(URI.DOCWEB + "style")
	public String getStyle();
}
