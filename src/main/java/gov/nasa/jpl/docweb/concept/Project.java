package gov.nasa.jpl.docweb.concept;

import java.util.Set;

import org.openrdf.annotations.Bind;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "Project")
public interface Project extends NamedElement, VolumeContainer {

}
