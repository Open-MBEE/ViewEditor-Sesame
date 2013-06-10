package gov.nasa.jpl.docweb.concept;

import java.util.List;
import java.util.Set;

import org.openrdf.annotations.Bind;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "Volume")
public interface Volume extends NamedElement, VolumeContainer {

	@Iri(URI.DOCWEB + "documents")
	public Set<DocumentView> getDocuments();
	
	@Iri(URI.DOCWEB + "documents")
	public void setDocuments(Set<DocumentView> documents);
	
	@Sparql(URI.PREFIX + 
			"INSERT { $this docweb:documents $document } WHERE {}")
	public void addDocument(@Bind("document") DocumentView document);
	
	@Sparql(URI.PREFIX + 
			"DELETE { $this docweb:documentss $document } WHERE {}")
	public void removeDocument(@Bind("document") DocumentView document);
	
	@Sparql(URI.PREFIX +
			"SELECT ?doc WHERE " + 
			"{ $this docweb:documents ?doc .\n" +
			"  ?doc docweb:name ?name . }\n" +
			" ORDER BY ?name")
	public List<DocumentView> getOrderedDocuments();
}
