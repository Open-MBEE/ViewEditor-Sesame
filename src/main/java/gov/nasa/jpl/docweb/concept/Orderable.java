package gov.nasa.jpl.docweb.concept;

import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "Orderable")
public interface Orderable {

	@Iri(URI.DOCWEB + "index")
	public Integer getIndex();
	
	@Iri(URI.DOCWEB + "index")
	public void setIndex(Integer index);
}
