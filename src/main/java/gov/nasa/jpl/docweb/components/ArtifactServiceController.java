package gov.nasa.jpl.docweb.components;

import java.io.File;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;


@Controller
@RequestMapping(value = "/artifact")
public class ArtifactServiceController {
	private final String ARTIFACT_DIR = "artifacts/";
	
	@Autowired
	ServletContext sc;

	private static Logger log = Logger.getLogger(ViewController.class.getName());

	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	public String upload(Model model) {
		return "artifact_upload";
	}

	@RequestMapping(value="/upload", method = RequestMethod.POST)
	public String handleFileUpoad(	HttpServletRequest request,
									@RequestParam CommonsMultipartFile[] fileUpload) throws Exception {
        String path = request.getParameter("path");
        if (fileUpload != null && fileUpload.length > 0) {
            for (CommonsMultipartFile aFile : fileUpload){
                 
    			if (path.indexOf(ARTIFACT_DIR)<0) {
    				path = ARTIFACT_DIR + path;
    			}

                String saveDirectory = sc.getRealPath("/") + path;
                log.info("saving file: " + saveDirectory + aFile.getOriginalFilename());
                if (!aFile.getOriginalFilename().equals("")) {
                    aFile.transferTo(new File(saveDirectory + aFile.getOriginalFilename()));
                }
            }
        }
 
        return "ok";
	}
	
	@RequestMapping(value="/folder", method = RequestMethod.POST)
	public String createNewFolder( HttpServletRequest request) {
		String path = request.getParameter("path");
		if (path != null) {
			if (path.indexOf(ARTIFACT_DIR)<0) {
				path = ARTIFACT_DIR + path;
			}
			String fullpath = sc.getRealPath("/") + File.separator + path;

			File dir = new File(fullpath);
			if (!dir.exists()) {
                log.info("creating directory recursivley: " + fullpath);
				dir.mkdirs();
			}
		}
		
		return "ok";
	}

	@RequestMapping(value = "/browse", method = RequestMethod.GET)
	public String browse() {
		return "artifact_browse";
	}
}
