package gov.nasa.jpl.docweb.concept;

import java.util.List;
import java.util.Set;

import org.openrdf.annotations.Bind;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "VolumeContainer")
public interface VolumeContainer {

	@Iri(URI.DOCWEB + "volumes")
	public Set<Volume> getVolumes();
	
	@Iri(URI.DOCWEB + "volumes")
	public void setVolumes(Set<Volume> volumes);
	
	@Sparql(URI.PREFIX + 
			"INSERT { $this docweb:volumes $volume } WHERE {}")
	public void addVolume(@Bind("volume") Volume volume);
	
	@Sparql(URI.PREFIX + 
			"DELETE { $this docweb:volumes $volume } WHERE {}")
	public void removeVolume(@Bind("volume") Volume volume);
	
	@Sparql(URI.PREFIX +
			"SELECT ?vol WHERE " + 
			"{ $this docweb:volumes ?vol .\n" +
			"  ?vol docweb:name ?name . }\n" +
			" ORDER BY ?name")
	public List<Volume> getOrderedVolumes();
	
	@Sparql(URI.PREFIX + 
			"DELETE { $p docweb:volumes $this } WHERE { $p docweb:volumes $this . }")
	public void clearParentVolumes();
}
