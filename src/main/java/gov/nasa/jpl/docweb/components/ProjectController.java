package gov.nasa.jpl.docweb.components;

import gov.nasa.jpl.docweb.concept.DocumentView;
import gov.nasa.jpl.docweb.concept.Project;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.Volume;
import gov.nasa.jpl.docweb.spring.LocalConnectionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/ui/projects")
public class ProjectController {

	@Autowired
	private LocalConnectionFactory<ObjectConnection> connectionFactory;	
	
	@SuppressWarnings("unused")
	private static Logger log = Logger.getLogger(ViewController.class.getName());
	
	@Transactional(readOnly = true)
	@RequestMapping(value = "/{project}", method=RequestMethod.GET)
	public String getProject(@PathVariable String project, Model model) throws RepositoryException, QueryEvaluationException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		oc.getObjects(Project.class);
		Project p = oc.getObject(Project.class, URI.DATA + project);
		HomeController.addProjects(model, oc);
		model.addAttribute("projectId", p.getMdid());
		model.addAttribute("projectName", p.getName());
		List<String> volumelist = new ArrayList<String>(); //top level volumes for this project
		Map<String, Map<String, Object>> volumetree = new HashMap<String, Map<String, Object>>();
		for (Volume v: p.getOrderedVolumes()) {
			volumelist.add(v.getMdid());
			populateVolumeTree(v, volumetree);
		}
		model.addAttribute("volumeTree", volumetree);
		model.addAttribute("topVolumes", volumelist);
		return "project";
	}
	
	/**
	 * for each volume: it has the object:
	 * { "name": name, "mdid": id, "childVolumes": [volumeid, ...], "childDocuments": [docid, ...]}
	 * @param v
	 * @param volumetree a mapping of volume to its volume object: {volumeid: the volume object, ...}
	 */
	private void populateVolumeTree(Volume v, Map<String, Map<String, Object>> volumetree) {
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
