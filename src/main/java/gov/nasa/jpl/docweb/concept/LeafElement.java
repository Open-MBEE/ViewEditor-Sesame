package gov.nasa.jpl.docweb.concept;


import java.util.Set;

import org.openrdf.annotations.Bind;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "LeafElement")
public interface LeafElement extends PresentationElement {

	@Iri(URI.DOCWEB + "sources")
	public Set<Source> getSources();
	
	@Iri(URI.DOCWEB + "sources")
	public void setSources(Set<Source> s);
	
	@Iri(URI.DOCWEB + "useProperty")
	public String getUseProperty();
	
	@Iri(URI.DOCWEB + "useProperty")
	public void setUseProperty(String s);
	
	@Sparql(URI.PREFIX + 
			"INSERT { $this docweb:sources $source } WHERE {}")
	public void addSource(@Bind("source") Source source);
	
	@Sparql(URI.PREFIX + 
			"DELETE { $this docweb:sources $source } WHERE { $this docweb:sources $source . }")
	public void removeSource(@Bind("source") Source source);
}
