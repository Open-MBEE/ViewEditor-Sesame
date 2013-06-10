package gov.nasa.jpl.docweb.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value="/rest/images")
public class ImageResource {

	@Autowired
	ServletContext sc;
	
	private static Logger log = Logger.getLogger(ImageResource.class.getName());
	private static String IMAGE_PATH = "images/docgen";
	
	
	/**
	 * Simple method for checking if an image id already exists (the image file is
	 * actually stored in the images directory (redirection doesn't seem to work too
	 * well).
	 * @param	imageid		The image id
	 * @param	cs			The checksum value (as a string)
	 * @param	extension	The filename extension type to look up
	 * 
	 * @return		Response OK if image found with specified cs and extension, NOT_FOUND otherwise
	 */
	@RequestMapping(value="/{imageid}", method=RequestMethod.GET)
	public void getImage(
			@PathVariable("imageid") 	String 				imageid,
			@RequestParam("cs")			String 				cs,
			@RequestParam("extension")	String				extension,
										HttpServletResponse response)
	{
		boolean exists = true;

		// Check the image directory first
		File directory = new File(sc.getRealPath(IMAGE_PATH));
		if (!directory.exists()) {
			exists = false;
		}
		
		// if directory exists, check that file exists
		if (exists) {
			File imageFile = new File(directory, imageid + "_cs" + cs + extension);
			if (!imageFile.exists()) {
				exists = false;
			}
		}
		
		if (exists) {
			response.setStatus(HttpURLConnection.HTTP_OK);
		} else {
			response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
		}
	}
	
	
	/**
	 * Simple method for uploading a file into the images directory
	 * @param	imageid		The image id
	 * @param	cs			The checksum value (as a string)
	 * @param	extension	The filename extension type to look up
	 * 
	 * @return		Response OK if upload succeeded, INTERNAL_ERROR otherwise
	 */
	@RequestMapping(value="/{imageid}", method=RequestMethod.POST)
	public void uploadImage(
			@PathVariable("imageid") 	String 				imageid,
			@RequestParam("cs")			String 				cs,
			@RequestParam("extension")	String				extension,
										InputStream			byteStream,
										HttpServletResponse response)
	{
		try {
			File directory = new File(sc.getRealPath(IMAGE_PATH));
			if (!directory.exists()) {
				directory.mkdirs();
			}

			// write out the file
			File imageFile = new File(directory, imageid + "_cs" + cs + extension);
			log.log(Level.INFO, "uploading file to: " + imageFile.getAbsolutePath());
			
			OutputStream out = new FileOutputStream(imageFile);
			int read = 0;
			byte[] bytes = new byte[1024];
			
			while ((read = byteStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
			
			// copy the file (need JRE 1.7 for nio) (links to work in Apache for some reason)
			File latest = new File(directory, imageid + "_latest" + extension);
			if (latest.exists()) {
				latest.delete();
			}
			String command;
			command = "cp " + imageFile.getAbsolutePath() + " " + latest.getAbsolutePath();
			Runtime.getRuntime().exec(command);
			
		} catch (IOException e) {
			e.printStackTrace();
			response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
			return;
		}
		
		response.setStatus(HttpURLConnection.HTTP_OK);
	}
}
