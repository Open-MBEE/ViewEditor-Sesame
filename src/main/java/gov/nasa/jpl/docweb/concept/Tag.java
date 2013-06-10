package gov.nasa.jpl.docweb.concept;

import java.util.Set;

import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "Tag")
public interface Tag extends NamedElement {
	
	@Sparql(URI.PREFIX +
			"SELECT ?view WHERE { ?view docweb:tags $this }")
	public Set<View> getViews();
}
