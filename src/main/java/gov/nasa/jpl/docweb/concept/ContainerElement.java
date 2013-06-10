package gov.nasa.jpl.docweb.concept;


import java.util.List;
import java.util.Set;

import org.openrdf.annotations.Bind;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "ContainerElement")
public interface ContainerElement extends PresentationElement, Orderable {

	@Iri(URI.DOCWEB + "contains")
	public Set<NonContainerElement> getContains();
	
	@Iri(URI.DOCWEB + "contains")
	public void setContains(Set<NonContainerElement> s);
	
	@Sparql(URI.PREFIX + 
			"INSERT { $this docweb:contains $child } WHERE {}")
	public void addContainedElement(@Bind("child") NonContainerElement child);
	
	@Sparql(URI.PREFIX + 
			"DELETE { $this docweb:contains $child } WHERE {}")
	public void removeContainedElement(@Bind("child") NonContainerElement child);
	
	@Sparql(URI.PREFIX +
			"SELECT ?e WHERE " + 
			"{ $this docweb:contains ?e .\n" +
			"  ?e docweb:index ?index . }\n" +
			" ORDER BY ?index")
	public List<NonContainerElement> getOrderedContainedElements();
}
