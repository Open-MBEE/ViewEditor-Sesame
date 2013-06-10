package gov.nasa.jpl.docweb.concept;

import java.util.Set;

import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "DocumentView")
public interface DocumentView extends View {

	@Sparql(URI.PREFIX + 
			"SELECT ?project WHERE " +
			"{ ?project docweb:volumes+/docweb:documents $this . " +
			"  ?project rdf:type docweb:Project . }")
	public Set<Project> getProject(); //is it possible to have more than one parent? Possibly (the docgen profile allows it, but it may not be what ppl want....
	
	@Sparql(URI.PREFIX + 
			"DELETE { $p docweb:documents $this } WHERE { $p docweb:documents $this . }")
	public void clearVolumes();
}
