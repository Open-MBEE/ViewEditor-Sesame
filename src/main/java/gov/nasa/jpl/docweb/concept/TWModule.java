package gov.nasa.jpl.docweb.concept;

import java.util.Set;

import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "TWModule")
public interface TWModule extends NamedElement {

	@Iri(URI.DOCWEB + "mounts")
	public Set<TWModule> getMounts();
	
	@Iri(URI.DOCWEB + "mounts")
	public void setMounts(Set<TWModule> mounts);
}
