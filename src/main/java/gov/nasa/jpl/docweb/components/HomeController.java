package gov.nasa.jpl.docweb.components;

import gov.nasa.jpl.docweb.concept.Project;
import gov.nasa.jpl.docweb.services.ProjectService;
import gov.nasa.jpl.docweb.spring.LocalConnectionFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.object.ObjectConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/ui")
public class HomeController {

	@Autowired
	private LocalConnectionFactory<ObjectConnection> connectionFactory;	
	
	@Autowired
	private ProjectService projectService;
	
	@RequestMapping(method=RequestMethod.GET)
	public String home() {
		return "home";
	}
	
	@RequestMapping(value="/test", method=RequestMethod.GET) //this prints out all the logged in user's groups
	public String test(Model model) {
		List<String> auths = new ArrayList<String>();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		for (GrantedAuthority ga: authentication.getAuthorities()) {
			auths.add(ga.getAuthority());
		}
		model.addAttribute("groups", auths);
		return "authtest";
	}
	
	@Transactional(readOnly = true)
	@ModelAttribute
	public void populateProjects(Model model) throws QueryEvaluationException, RepositoryException {
		ObjectConnection oc = connectionFactory.getCurrentConnection();
		model.addAttribute("projects", projectService.getProjects(oc));
	}
}
