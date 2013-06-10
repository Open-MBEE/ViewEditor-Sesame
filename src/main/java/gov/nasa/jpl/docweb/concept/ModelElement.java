package gov.nasa.jpl.docweb.concept;

import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;


@Iri(URI.DOCWEB + "ModelElement")
public interface ModelElement extends Source {
	
	@Iri(URI.DOCWEB + "documentation")
	public String getDocumentation();
	
	@Iri(URI.DOCWEB + "documentation")
	public void setDocumentation(String documentation);
	
	@Iri(URI.DOCWEB + "mdid")
	public String getMdid();
	
	@Iri(URI.DOCWEB + "mdid")
	public void setMdid(String uuid);
	
	@Iri(URI.DOCWEB + "oldVersion")
	public ModelElement getOldVersion();
	
	@Iri(URI.DOCWEB + "oldVersion")
	public void setOldVersion(ModelElement modelElement);
	
	@Iri(URI.DOCWEB + "committed")
	public boolean getCommitted();
	
	@Iri(URI.DOCWEB + "committed")
	public void setCommitted(boolean b);
	
	@Iri(URI.DOCWEB + "modified")
	public XMLGregorianCalendar getModified();
	
	@Iri(URI.DOCWEB + "modified")
	public void setModified(XMLGregorianCalendar c);
	
	@Sparql(URI.PREFIX + 
			"SELECT ?me WHERE { ?me docweb:oldVersion $this . }")
	public ModelElement getNewerVersion();
	
	@Sparql(URI.PREFIX + 
			"SELECT DISTINCT ?document WHERE " +
			"{ ?document rdf:type docweb:DocumentView .\n" +
			"  ?document docweb:views*/docweb:contains ?nce .\n" +
			"  ?nce docweb:source $this . }")
	public Set<DocumentView> getOwningDocuments();
	
	@Sparql(URI.PREFIX + 
			"SELECT DISTINCT ?view WHERE " + 
			"{ ?view rdf:type docweb:View .\n" +
			"  ?view docweb:views*/docweb:contains ?nce .\n" +
			"  ?nce docweb:source $this . }")
	public Set<View> getOwningViews();
	
	@Sparql(URI.PREFIX +
			"DELETE { $this ?predicate ?object } WHERE { $this ?predicate ?object . }")
	public void delete();
}
