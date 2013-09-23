package gov.nasa.jpl.docweb.components;

import gov.nasa.jpl.docweb.concept.DocumentView;
import gov.nasa.jpl.docweb.concept.Project;
import gov.nasa.jpl.docweb.concept.URI;
import gov.nasa.jpl.docweb.concept.Volume;
import gov.nasa.jpl.docweb.services.ProjectService;
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
	private ProjectService projectService;
	
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
		model.addAttribute("projects", projectService.getProjects(oc));
		model.addAttribute("projectId", p.getMdid());
		model.addAttribute("projectName", p.getName());
		List<String> volumelist = new ArrayList<String>(); //top level volumes for this project
		Map<String, Map<String, Object>> volumetree = new HashMap<String, Map<String, Object>>();
		for (Volume v: p.getOrderedVolumes()) {
			volumelist.add(v.getMdid());
			projectService.populateVolumeTree(v, volumetree);
		}
		model.addAttribute("volumeTree", volumetree);
		model.addAttribute("topVolumes", volumelist);
		return "project";
	}
	
	
}
