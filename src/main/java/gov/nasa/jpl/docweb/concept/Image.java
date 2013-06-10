package gov.nasa.jpl.docweb.concept;

import java.util.Set;

import org.openrdf.annotations.Iri;

@Iri(URI.DOCWEB + "Image")
public interface Image extends CaptionedElement, TitledElement, NonContainerElement, LeafElement {

}
