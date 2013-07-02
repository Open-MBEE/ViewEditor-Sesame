package gov.nasa.jpl.docweb.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nasa.jpl.docweb.concept.DocumentView;
import gov.nasa.jpl.docweb.concept.Project;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.Volume;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

@Service
public class ProjectService {

	public DocumentView getOrCreateDocument(ObjectConnection oc, String did) throws RepositoryException, QueryEvaluationException {
		DocumentView dv = null;
		try {
			dv = oc.getObject(DocumentView.class, URI.DATA + did);
		} catch (ClassCastException e) {
			dv = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + did, DocumentView.class), DocumentView.class);
			dv.setMdid(did);
			dv.setName("Unexported Document");
			dv.setCommitted(true);
		} 
		return dv;
	}
	
	public Volume getOrCreateVolume(ObjectConnection oc, String vid, String vname) throws RepositoryException, QueryEvaluationException {
		Volume v = null;
		try {
			v = oc.getObject(Volume.class, URI.DATA + vid);
		} catch (ClassCastException e) {
			v = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + vid, Volume.class), Volume.class);
			v.setMdid(vid);
		} 
		v.setName(vname);
		return v;
	}
	
	public Volume getVolume(ObjectConnection oc, String vid) throws RepositoryException, QueryEvaluationException {
		Volume v = null;
		try {
			v = oc.getObject(Volume.class, URI.DATA + vid);
			return v;
		} catch (ClassCastException e) {
			return null;
		} 
	}
	
	public Project getProject(ObjectConnection oc, String vid) throws RepositoryException, QueryEvaluationException {
		Project v = null;
		try {
			v = oc.getObject(Project.class, URI.DATA + vid);
			return v;
		} catch (ClassCastException e) {
			return null;
		} 
	}
	
	public Project getOrCreateProject(ObjectConnection oc, String pid, String pname) throws RepositoryException, QueryEvaluationException {
		Project v = null;
		try {
			v = oc.getObject(Project.class, URI.DATA + pid);
		} catch (ClassCastException e) {
			v = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + pid, Project.class), Project.class);
			v.setMdid(pid);
		} 
		v.setName(pname);
		return v;
	}
	
	public List<Map<String, String>> getProjects(ObjectConnection oc) throws QueryEvaluationException, RepositoryException {
		List<Map<String, String>> projs = new ArrayList<Map<String, String>>();
		List<Project> projects = oc.getObjects(Project.class).asList();
		for (Project p: projects) {
			Map<String, String> proj = new HashMap<String, String>();
			proj.put("name", p.getName());
			proj.put("mdid", p.getMdid());
			projs.add(proj);
		}
		return projs;
	}
	
	/**
	 * for each volume: it has the object:
	 * { "name": name, "mdid": id, "childVolumes": [volumeid, ...], "childDocuments": [docid, ...]}
	 * @param v
	 * @param volumetree a mapping of volume to its volume object: {volumeid: the volume object, ...}
	 */
	public void populateVolumeTree(Volume v, Map<String, Map<String, Object>> volumetree) {
		Map<String, Object> volumeinfo = new HashMap<String, Object>();
		volumeinfo.put("name", v.getName());
		volumeinfo.put("mdid", v.getMdid());
		List<String> cvs = new ArrayList<String>();
		for (Volume cv: v.getOrderedVolumes()) {
			populateVolumeTree(cv, volumetree);
			cvs.add(cv.getMdid());
		}
		volumeinfo.put("childVolumes", cvs);
		List<Map<String, String>> cds = new ArrayList<Map<String, String>>();
		for (DocumentView dv: v.getOrderedDocuments()) {
			Map<String, String> docinfo = new HashMap<String, String>();
			docinfo.put("name", dv.getName());
			docinfo.put("mdid", dv.getMdid());
			cds.add(docinfo);
		}
		volumeinfo.put("childDocuments", cds);
		volumetree.put(v.getMdid(), volumeinfo);
	}
	
}
