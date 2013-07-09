//package gov.nasa.jpl.docweb.components;
//
//import static org.junit.Assert.*;
//
//import java.lang.reflect.Method;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.mock.web.MockHttpServletRequest;
//import org.springframework.mock.web.MockHttpServletResponse;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
////import org.springframework.test.web.server.MockMvc;
////import org.springframework.test.web.server.setup.MockMvcBuilders;
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={
////		"file:src/main/webapp/WEB-INF/web.xml",	
////		"file:src/main/webapp/WEB-INF/application-servlet.xml",
//		"file:src/test/java/test-spring-context.xml"})
//public class ArtifactServiceControllerTest {
//
//	private MockHttpServletRequest			request;
//	private MockHttpServletResponse			response;
//	private ArtifactServiceController			controller;
//
//	@Before
//	public void setUp() throws Exception {
//		controller	= new ArtifactServiceController();
//		request		= new MockHttpServletRequest();
//		response	= new MockHttpServletResponse();
////		MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new ArtifactServiceController()).build();
//	}
//
//	@After
//	public void tearDown() throws Exception {
//	}
//
//	@Test
//	public void testUpload() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testHandleFileUpoad() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testCreateNewFolder() {
//		fail("Not yet implemented");
//	}
//
//	@Test
//	public void testBrowse() throws Exception {
//		request.setMethod("GET");
//		String view = controller.browse();
//		assert(view.equals("artifact_browse"));
//	}
//
//	@Test
//	public void testIsValidPath() throws Exception {
////		Class params[] = new Class[1];
////		params[0] = String.class;
////		Method method = controller.getClass().getDeclaredMethod("isValidPath", params);
////		method.setAccessible(true);
////		String args[] = new String[1];
////		args[0] = "../test";
////		method.invoke(controller, args);
//		controller.isValidPath("../test");
//	}
//}
