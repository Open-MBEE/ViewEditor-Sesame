package gov.nasa.jpl.docweb.concept;

import java.util.Set;

import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "User")
public interface User {

	@Iri(URI.DOCWEB + "username")
	public String getUserName();
	
	@Iri(URI.DOCWEB + "username")
	public void setUserName(String name);
	
	@Sparql(URI.PREFIX + 
			"SELECT ?comment WHERE { ?comment docweb:author $this . }")
	public Set<Comment> getComments(); 
	
	@Sparql(URI.PREFIX + 
			"SELECT ?view WHERE { ?view docweb:comments/docweb:replies*/docweb:author $this . }")
	public Set<View> getCommentedViews(); 
}
