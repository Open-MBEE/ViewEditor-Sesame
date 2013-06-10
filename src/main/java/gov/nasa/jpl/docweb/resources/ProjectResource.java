package gov.nasa.jpl.docweb.resources;

import gov.nasa.jpl.docweb.concept.DocumentView;
import gov.nasa.jpl.docweb.concept.Project;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.Volume;
import gov.nasa.jpl.docweb.spring.LocalConnectionFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.datatype.DatatypeConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value="/rest/projects")
public class ProjectResource {

	@Autowired
	private LocalConnectionFactory<ObjectConnection> connectionFactory;	
	
	private static Logger log = Logger.getLogger(ViewResource.class.getName());
	
	/**
	 * <p>accepts body:</p>
	 * <p>{
	 *		"name" : projectname,
	 *		"volumes": {mdid: volumename},
	 *		"volume2volumes": {volumeid: [volumeid, ...], ...},
	 *		"documents": [actual documentview id, ...],
	 *		"volume2documents": {volumeid: [documentid], ...},
	 *		"projectVolumes": [volumeid,...]
	 *	}</p>
	 * @param viewid
	 * @param body
	 * @return
	 * @throws RepositoryException
	 * @throws ParseException
	 * @throws QueryEvaluationException
	 * @throws DatatypeConfigurationException
	 */
	@Transactional
	@SuppressWarnings("unchecked")
	@RequestMapping(value="/{projectid}", method=RequestMethod.POST)
	public @ResponseBody String postProject(@PathVariable("projectid") String pid, @RequestBody String body) throws RepositoryException, ParseException, QueryEvaluationException, DatatypeConfigurationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("posting project " + pid + ": \n" + body);
		JSONObject posted = (JSONObject)(new JSONParser()).parse(body);
		Project proj = getOrCreateProject(oc, pid, (String)posted.get("name"));
		JSONObject volumes = (JSONObject)posted.get("volumes");
		Map<String, Volume> volumemap = new HashMap<String, Volume>();
		for (String vid: (Set<String>)volumes.keySet()) {
			Volume v = getOrCreateVolume(oc, vid, (String)volumes.get(vid));
			volumemap.put(vid, v);
		}
		JSONArray documents = (JSONArray)posted.get("documents");
		Map<String, DocumentView> documentmap = new HashMap<String, DocumentView>();
		for (Object did: documents) {
			DocumentView dv = getOrCreateDocument(oc, (String)did);
			documentmap.put((String)did, dv);
		}
		JSONObject v2v = (JSONObject)posted.get("volume2volumes");
		for (String vid: (Set<String>)v2v.keySet()) {
			Volume fromV = volumemap.get(vid);
			Set<Volume> childVs = new HashSet<Volume>();
			for (Object cid: (JSONArray)v2v.get(vid)) {
				Volume cv = volumemap.get((String)cid);
				cv.clearParentVolumes();
				childVs.add(cv);
			}
			fromV.setVolumes(childVs);
		}
		JSONObject v2d = (JSONObject)posted.get("volume2documents");
		for (String vid: (Set<String>)v2d.keySet()) {
			Volume fromV = volumemap.get(vid);
			Set<DocumentView> childDs = new HashSet<DocumentView>();
			for (Object did: (JSONArray)v2d.get(vid)) {
				DocumentView cd = documentmap.get((String)did);
				cd.clearVolumes();
				childDs.add(cd);
			}
			fromV.setDocuments(childDs);
		}
		Set<Volume> projectVolumes = new HashSet<Volume>();
		for (Object vid: (JSONArray)posted.get("projectVolumes")) {
			Volume pv = volumemap.get((String)vid);
			pv.clearParentVolumes();
			projectVolumes.add(pv);
		}
		proj.setVolumes(projectVolumes);
		return "ok";//Response.status(200).build();
	}
	
	private DocumentView getOrCreateDocument(ObjectConnection oc, String did) throws RepositoryException, QueryEvaluationException {
		DocumentView dv = null;
		try {
			dv = oc.getObject(DocumentView.class, URI.DATA + did);
		} catch (ClassCastException e) {
			dv = oc.addDesignation(oc.getObjectFactory().createObject(URI.DATA + did, DocumentView.class), DocumentView.class);
			dv.setMdid(did);
			dv.setName("Unexported Document");
		} 
		return dv;
	}
	
	private Volume getOrCreateVolume(ObjectConnection oc, String vid, String vname) throws RepositoryException, QueryEvaluationException {
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
	
	private Project getOrCreateProject(ObjectConnection oc, String pid, String pname) throws RepositoryException, QueryEvaluationException {
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
	
}
