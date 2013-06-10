package gov.nasa.jpl.docweb.concept;

import java.util.Set;

import org.openrdf.annotations.Bind;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "Comment")
public interface Comment extends ModelElement {

	@Sparql(URI.PREFIX + 
			"SELECT ?view WHERE { ?view docweb:comments/docweb:replies* $this }")
	public Set<View> getCommentedViews();
	
	@Iri(URI.DOCWEB + "replies")
	public Set<Comment> getReplies();
	@Iri(URI.DOCWEB + "replies")
	public void setReplies(Set<Comment> comments);
	
	@Iri(URI.DOCWEB + "deleted")
	public boolean getDeleted();
	@Iri(URI.DOCWEB + "deleted")
	public void setDeleted(boolean deleted);
	
	@Sparql(URI.PREFIX + 
			"INSERT { $this docweb:replies $comment } WHERE {}")
	public void addReply(@Bind("comment") Comment comment);
	
	@Sparql(URI.PREFIX + 
			"DELETE { $this docweb:replies $comment } WHERE {}")
	public void removeReply(@Bind("comment") Comment comment);
	
	@Sparql(URI.PREFIX +
			"SELECT ?parent WHERE { ?parent docweb:replies $this }")
	public Set<Comment> getParentComments(); 
	
	@Sparql(URI.PREFIX +
			"SELECT ?parent WHERE { ?parent docweb:replies+ $this }")
	public Set<Comment> getParentCommentsRecursive();
	
	@Iri(URI.DOCWEB + "commit")
	public boolean getCommit();
	@Iri(URI.DOCWEB + "commit")
	public void setCommit(boolean b);
	
	@Iri(URI.DOCWEB + "author")
	public User getAuthor();
	@Iri(URI.DOCWEB + "author")
	public void setAuthor(User v);
}
