package gov.nasa.jpl.docweb.concept;


import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "Text")
public interface Text extends Source {
	
	@Iri(URI.DOCWEB + "text")
	public String getText();
	
	@Iri(URI.DOCWEB + "text")
	public void setText(String text);

}
