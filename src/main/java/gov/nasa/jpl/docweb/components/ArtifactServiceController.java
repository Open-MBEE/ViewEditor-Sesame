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

/**
 * Artifact service that handles file uploads
 * @author cinyoung
 * 
 * Testing git change
 *
 */
@Controller
@RequestMapping(value = "/artifact")
public class ArtifactServiceController {
	private final String ARTIFACT_DIR = "artifacts/";
	
	@Autowired
	ServletContext sc;

	private static Logger log = Logger.getLogger(ArtifactServiceController.class.getName());

	/**
	 * Passes request to artifact_upload.jsp that has the upload GUI
	 * @return
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	public String upload() {
		return "artifact_upload";
	}

	/**
	 * Handles a multipart file upload request
	 * @param request
	 * @param fileUpload
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value="/upload", method = RequestMethod.POST)
	public String handleFileUpoad(	HttpServletRequest request,
									@RequestParam CommonsMultipartFile[] fileUpload) throws Exception {
        String path = request.getParameter("path");
        if (path != null && isValidPath(path)) {
	        if (fileUpload != null && fileUpload.length > 0) {
	            for (CommonsMultipartFile aFile : fileUpload){
	                 
	    			if (path.indexOf(ARTIFACT_DIR)<0) {
	    				path = ARTIFACT_DIR + path;
	    			}
	
	                String saveDirectory = sc.getRealPath("/") + path;
	                // check that the directory exists
	                File saveDirFile = new File(saveDirectory);
	                if (!saveDirFile.exists()) {
	                	log.info("creating save directory: " + saveDirectory);
	                	saveDirFile.mkdirs();
	                }
	                
	                log.info("saving file: " + saveDirectory + aFile.getOriginalFilename());
	                if (!aFile.getOriginalFilename().equals("")) {
	                    aFile.transferTo(new File(saveDirectory + aFile.getOriginalFilename()));
	                }
	            }
	        }
        }
 
        return "ok";
	}
	
	/** Handles creating a new folder
	 * 
	 */
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

	/**
	 * Returns the browse capabilities
	 */
	@RequestMapping(value = "/browse", method = RequestMethod.GET)
	public String browse() {
		return "artifact_browse";
	}
	
	/**
	 * Check that path is valid
	 * @param path
	 * @return
	 */
	public boolean isValidPath(String path) {
		return true;
//		File file = new File(sc.getRealPath("/") + path);
//		if (file.getAbsolutePath().indexOf(ARTIFACT_DIR) >= 0) {
//			return true;
//		} else {
//			return false;
//		}
	}
}
