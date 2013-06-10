package gov.nasa.jpl.docweb.concept;


import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "Content")
public interface Content extends Orderable {

	@Iri(URI.DOCWEB + "source")
	public Source getSource();
	
	@Iri(URI.DOCWEB + "source")
	public void setSource(Source s);
	
	@Iri(URI.DOCWEB + "useProperty")
	public String getUseProperty();
	
	@Iri(URI.DOCWEB + "useProperty")
	public void setUseProperty(String s);
}
