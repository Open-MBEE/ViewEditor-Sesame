package gov.nasa.jpl.docweb.resources;

import gov.nasa.jpl.docweb.concept.DocumentView;
import gov.nasa.jpl.docweb.concept.Project;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.Volume;
import gov.nasa.jpl.docweb.services.ProjectService;
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
	
	@Autowired
	private ProjectService projectService;
	
	private static Logger log = Logger.getLogger(ProjectResource.class.getName());
	
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
	public @ResponseBody String postProject(@PathVariable("projectid") String pid, @RequestBody String body) throws RepositoryException, ParseException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("posting project " + pid + ": \n" + body);
		JSONObject posted = (JSONObject)(new JSONParser()).parse(body);
		Project proj = projectService.getOrCreateProject(oc, pid, (String)posted.get("name"));
		JSONObject volumes = (JSONObject)posted.get("volumes");
		Map<String, Volume> volumemap = new HashMap<String, Volume>();
		for (String vid: (Set<String>)volumes.keySet()) {
			Volume v = projectService.getOrCreateVolume(oc, vid, (String)volumes.get(vid));
			volumemap.put(vid, v);
		}
		JSONArray documents = (JSONArray)posted.get("documents");
		Map<String, DocumentView> documentmap = new HashMap<String, DocumentView>();
		for (Object did: documents) {
			DocumentView dv = projectService.getOrCreateDocument(oc, (String)did);
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
			//Set<DocumentView> childDs = new HashSet<DocumentView>();
			for (Object did: (JSONArray)v2d.get(vid)) {
				DocumentView cd = documentmap.get((String)did);
				cd.clearVolumes();
				fromV.addDocument(cd);
				//childDs.add(cd);
			}
			//fromV.setDocuments(childDs);
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
	
	@Transactional
	@RequestMapping(value="/document/{docid}", method=RequestMethod.POST)
	public @ResponseBody String postDocument(@PathVariable("docid") String did, @RequestBody String body) throws RepositoryException, ParseException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("posting document volume " + did + ": \n" + body);
		DocumentView dv = projectService.getOrCreateDocument(oc, (String)did);
		Volume v = projectService.getVolume(oc, body);
		if (v == null)
			return "NotFound";
		dv.clearVolumes();
		v.addDocument(dv);
		return "ok";
	}
	
	@Transactional
	@RequestMapping(value="/document/{docid}/delete", method=RequestMethod.POST)
	public @ResponseBody String deleteDocument(@PathVariable("docid") String did, @RequestBody String body) throws RepositoryException, ParseException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("deleting document " + did );
		DocumentView dv = projectService.getDocument(oc, did);
		if (dv == null)
			return "NotFound";
		dv.clearVolumes();
		return "ok";
	}
	
	@Transactional
	@RequestMapping(value="/{projectid}/delete", method=RequestMethod.POST)
	public @ResponseBody String deleteProject(@PathVariable("projectid") String pid) throws RepositoryException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		log.info("delete project " + pid);
		Project proj = projectService.getProject(oc, pid);
		if (proj == null)
			return "NotFound";
		proj.delete();
		return "ok";
	}
}
