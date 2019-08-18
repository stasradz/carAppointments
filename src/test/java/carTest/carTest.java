package carTest;

import org.junit.jupiter.api.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.sql.Time;
import java.util.Calendar;

import carAppointments.Appointment;
import carAppointments.timeAppointment;

public class carTest {
	
	/**
	 * Test inserting a new appointment
	 */
	@Test
	public void testInsert() {
		
		Calendar cal = Calendar.getInstance();
		
		timeAppointment ta = new timeAppointment();
		ta.setAppointmentDate(new Date(cal.getTime().getTime()));
		ta.setCustomerName("Test");
		ta.setCustomerLName("Testers");
		ta.setPrice(500);
		ta.setTimeStart(new Time(cal.getTime().getTime()));
		
		WebResource webResource = createPostWebResource(INSERT_URI);

        ClientResponse response = webResource.accept("application/json")
                .type("application/json").post(ClientResponse.class, ta);
        
        assertTrue(response.getStatus() == 200);

	}
	
	/**
	 * Test inserting an appointment with random time
	 */
	@Test
	public void testSchedule() {
		
		Calendar cal = Calendar.getInstance();
		
		Appointment app = new Appointment();
		app.setAppointmentDate(new Date(cal.getTime().getTime()));
		app.setCustomerName("Test");
		app.setCustomerLName("Testers");
		app.setPrice(500);
		
		WebResource webResource = createPostWebResource(SCHEDULE_URI);

        ClientResponse response = webResource.accept("application/json")
                .type("application/json").post(ClientResponse.class, app);
        
        assertTrue(response.getStatus() == 200);

	}
	
	/**
	 * Test getting a certain appointment
	 */
	@Test
	public void testGet() {
		
		WebResource webResource = createUpdateGetDeleteWebResource(GET_URI + "19");

		ClientResponse response = webResource.accept("application/json")
                .type("application/json").get(ClientResponse.class);
        
        assertTrue(response.getStatus() == 200 || response.getStatus() == 400);

	}
	
	/**
	 * Test updating a status of the appointment
	 */
	@Test
	public void testPut() {
		
		WebResource webResource = createUpdateGetDeleteWebResource(UPDATE_URI + "19");

		ClientResponse response = webResource.accept("application/json")
                .type("application/json").put(ClientResponse.class);
        
        assertTrue(response.getStatus() == 200 || response.getStatus() == 400);

	}
	
	/**
	 * Test deleting an appointment
	 */
	@Test
	public void testDelete() {
		
		WebResource webResource = createUpdateGetDeleteWebResource(DELETE_URI + "19");

		ClientResponse response = webResource.accept("application/json")
                .type("application/json").delete(ClientResponse.class);
        
        assertTrue(response.getStatus() == 200 || response.getStatus() == 400);

	}
	
	/**
	 * Test getting a list of appointments by a date range
	 */
	@Test
	public void testDateRange() {
		
		WebResource webResource = createUpdateGetDeleteWebResource(DATERANGE_URI + "2019/8/29/2019/8/29");

		ClientResponse response = webResource.accept("application/json")
                .type("application/json").get(ClientResponse.class);
        
        assertTrue(response.getStatus() == 200 || response.getStatus() == 400);

	}
	
	/**
	 * Create a web resource for post
	 * @param uri The URI
	 * @return The web resource
	 */
	private WebResource createPostWebResource(final String uri) {
		ClientConfig clientConfig = new DefaultClientConfig();
		 
        clientConfig.getFeatures().put(
                JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);

        Client client = Client.create(clientConfig);

        WebResource webResource = client.resource(uri);
        
        return webResource;
	}
	
	/**
	 * Create a web resource for get, delete, update
	 * @param uri The URI
	 * @return The web resource
	 */
	private WebResource createUpdateGetDeleteWebResource(final String uri) {
		ClientConfig clientConfig = new DefaultClientConfig();
		
		Client client = Client.create(clientConfig);

	    WebResource webResource = client.resource(uri);
	    
	    return webResource;
	}
	
	private static final String INSERT_URI
	= "http://localhost:9090/carAppointments/rest/appointments/post";
	
	private static final String SCHEDULE_URI
	= "http://localhost:9090/carAppointments/rest/appointments/schedule";
	
	private static final String GET_URI
	= "http://localhost:9090/carAppointments/rest/appointments/get/";
	
	private static final String DELETE_URI
	= "http://localhost:9090/carAppointments/rest/appointments/remove/";
	
	private static final String DATERANGE_URI
	= "http://localhost:9090/carAppointments/rest/appointments/daterange/";
	
	private static final String UPDATE_URI
	= "http://localhost:9090/carAppointments/rest/appointments/update/";
}
