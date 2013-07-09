package gov.nasa.jpl.docweb.concept;

import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.annotations.Bind;
import org.openrdf.annotations.Iri;
import org.openrdf.annotations.Sparql;

@Iri(URI.DOCWEB + "View")
public interface View extends NamedElement, ContainerElement {
	
	@Iri(URI.DOCWEB + "views")
	public Set<View> getViews();
	@Iri(URI.DOCWEB + "views")
	public void setViews(Set<View> view);
	
	@Iri(URI.DOCWEB + "comments")
	public Set<Comment> getComments();
	@Iri(URI.DOCWEB + "comments")
	public void setComments(Set<Comment> comment);
	
	@Iri(URI.DOCWEB + "tags")
	public Set<Tag> getTags();
	@Iri(URI.DOCWEB + "tags")
	public void setTags(Set<Tag> tags);
	
	@Iri(URI.DOCWEB + "first")
	public View getFirst();
	@Iri(URI.DOCWEB + "first")
	public void setFirst(View v);
	
	@Iri(URI.DOCWEB + "next")
	public View getNext();
	@Iri(URI.DOCWEB + "next")
	public void setNext(View v);
	
	@Iri(URI.DOCWEB + "lastModifiedBy")
	public User getLastModifiedBy();
	@Iri(URI.DOCWEB + "lastModifiedBy")
	public void setLastModifiedBy(User v);
	
	@Iri(URI.DOCWEB + "lastModified")
	public XMLGregorianCalendar getLastModified();
	@Iri(URI.DOCWEB + "lastModified")
	public void setLastModified(XMLGregorianCalendar v);
	
	@Iri(URI.DOCWEB + "noSection")
	public Boolean getNoSection();
	@Iri(URI.DOCWEB + "noSection")
	public void setNoSection(Boolean v);
	
	
	@Sparql(URI.PREFIX + 
			"SELECT ?parent WHERE " +
			"{ ?parent docweb:views+ $this . " +
			"  ?parent rdf:type docweb:DocumentView . }")
	public Set<DocumentView> getParentDocument(); //is it possible to have more than one parent? Possibly (the docgen profile allows it, but it may not be what ppl want....
	
	
	@Sparql(URI.PREFIX + 
			"SELECT ?parent WHERE { ?parent docweb:views $this . }")
	public Set<View> getParentViews(); //is it possible to have more than one parent? Possibly (the docgen profile allows it, but it may not be what ppl want....
	
	@Sparql(URI.PREFIX + 
			"DELETE { $p docweb:views $this } WHERE { $p docweb:views $this . }")
	public void clearParents();
	
	@Sparql(URI.PREFIX + 
			"INSERT { $this docweb:views $child } WHERE {}")
	public void addChildView(@Bind("child") View child);
	
	@Sparql(URI.PREFIX + 
			"DELETE { $this docweb:views $child } WHERE { $this docweb:views $child . }")
	public void removeChildView(@Bind("child") View child);
	
	@Sparql(URI.PREFIX + 
			"INSERT { $this docweb:comments $comment } WHERE {}")
	public void addComment(@Bind("comment") Comment comment);
	
	@Sparql(URI.PREFIX + 
			"DELETE { $this docweb:comments $comment } WHERE { $this docweb:comments $comment . }")
	public void removeComment(@Bind("comment") Comment comment);
	
	@Sparql(URI.PREFIX + 
			"INSERT { $this docweb:tags $tag } WHERE {}")
	public void addTag(@Bind("tag") Tag tag);
	
	@Sparql(URI.PREFIX + 
			"DELETE { $this docweb:tags $tag } WHERE { $this docweb:tags $tag . }")
	public void removeTag(@Bind("tag") Tag tag);
	
	@Sparql(URI.PREFIX + 
			"SELECT ?view WHERE " +
			"{ ?pview docweb:views $this .\n" +
			"  ?pview docweb:views ?view .\n" +
			"  $this docweb:index ?index .\n" + 
			"  BIND (?index+1 AS ?nindex) .\n" + 
			"  ?view docweb:index ?nindex .}")
	public View getNextView();
	
	@Sparql(URI.PREFIX + 
			"SELECT ?view WHERE " +
			"{ ?pview docweb:views $this .\n" +
			"  ?pview docweb:views ?view .\n" +
			"  $this docweb:index ?index .\n" + 
			"  BIND (?index-1 AS ?pindex) .\n" + 
			"  ?view docweb:index ?pindex .}")
	public View getPreviousView();
	
	@Sparql(URI.PREFIX +
			"SELECT ?view WHERE " + 
			"{ $this docweb:views ?view .\n" +
			"  ?view docweb:index 1 . }")
	public View getFirstChildView();

	@Sparql(URI.PREFIX +
			"SELECT ?view WHERE " + 
			"{ $this docweb:views ?view .\n" +
			"  ?view docweb:index ?index . }\n" +
			" ORDER BY ?index")
	public List<View> getOrderedChildrenViews();
	
	@Sparql(URI.PREFIX + 
			"SELECT ?view WHERE " +
			"{ $this docweb:views+ ?view . }")
	public Set<View> getChildrenViewsRecursive();
	
	@Sparql(URI.PREFIX +
			"SELECT ?comment WHERE " + 
			"{ $this docweb:comments ?comment .\n" +
			"  ?comment docweb:deleted false .\n" +
			"  ?comment docweb:modified ?mod . }\n" +
			" ORDER BY ?mod")
	public List<Comment> getOrderedComments();
	
	@Sparql(URI.PREFIX + 
			"SELECT ?e WHERE " + 
			"{ $this docweb:contains/docweb:sources ?e .\n" +
			"  ?e rdf:type/rdfs:subClassOf* docweb:ModelElement .\n" +
			"  ?e docweb:committed false . }")
	public Set<ModelElement> getUncommittedModelElements();
	
	@Sparql(URI.PREFIX + 
			"SELECT ?e WHERE " + 
			"{ $this docweb:views*/docweb:contains/docweb:sources ?e .\n" +
			"  ?e rdf:type/rdfs:subClassOf* docweb:ModelElement .\n" +
			"  ?e docweb:committed false . }")
	public Set<ModelElement> getUncommittedModelElementsRecursive();
	
	@Sparql(URI.PREFIX + 
			"SELECT ?e WHERE " + 
			"{ $this docweb:contains/docweb:sources ?e .\n" +
			"  ?e rdf:type/rdfs:subClassOf* docweb:ModelElement . }")
	public Set<ModelElement> getModelElements();
	
	@Sparql(URI.PREFIX + 
			"SELECT ?e WHERE " + 
			"{ $this docweb:views*/docweb:contains/docweb:sources ?e .\n" +
			"  ?e rdf:type/rdfs:subClassOf* docweb:ModelElement . }")
	public Set<ModelElement> getModelElementsRecursive();
	
	@Sparql(URI.PREFIX + 
			"SELECT ?e WHERE " + 
			"{ $this docweb:comments/docweb:replies* ?e .\n" +
			"  ?e docweb:committed false . }")
	public Set<Comment> getUncommittedComments();
}
