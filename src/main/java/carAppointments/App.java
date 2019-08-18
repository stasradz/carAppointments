package carAppointments;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The API class
 * @author Stas
 *
 */
@Path("/appointments")
public class App {
	
	/**
	 * Constructor, initializes the database
	 */
	public App() {
		database = new carServiceDatabase("carsnielsen123.database.windows.net",
										  "CarService",
										  "carserviceadmin",
										  "carServicePassword1");
	}
	
	/**
	 * API get Function, will return an appointment by id if exists
	 * @param id The id of the appointment
	 * @return The representation of the appointment, will be transfered to json
	 */
	@GET
	@Path("/get/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public IdAppointment getAppointmentByID(@PathParam("id") String id) {
		IdAppointment app = new IdAppointment();
		
		int n_id = 0;
		try {
			n_id = Integer.parseInt(id);
			
			// Get the appointment from database
			ResultSet rs = database.getAppointmentByID(n_id);
			boolean success = app.setFromResultSet(rs);
			
			return success ? app : new IdAppointment();
			
		}
		catch(Exception e) {
			System.out.println("Bad input");
			
			return new IdAppointment();
		}
	}
	
	/**
	 * Gets the json representing appointment, and posts it into the database.
	 * Respond with the newly created id
	 * 
	 * @param app The application to be inserted
	 * @return Response, indicating the result
	 */
	@POST
	@Path("/post")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postAppointment(timeAppointment app) {
		// Insert the appointment
		int new_id = database.insertAppointment(app.getAppointmentDate(), app.getTimeStart(),
												app.getPrice(), app.getCustomerName(),
												app.getCustomerLName());
		
		
        String result = new_id != -1 ? "Record entered, new id: "+ new_id : 
        							   "Record has a problem";
        
        return Response.status(new_id != -1 ? 200 : 400).entity(result).build();
    }
	
	/**
	 * Gets the json representing appointment without time, and posts it into the database.
	 * Respond with the newly created id
	 * 
	 * @param app The application to be inserted
	 * @return Response, indicating the result
	 */
	@POST
	@Path("/schedule")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postTimeLessAppoointment(Appointment app) {
		// Insert the appointment without the time, will be selected randomly
		int new_id = database.scheduleRandomAppointment(app.getAppointmentDate(), app.getPrice(),
														app.getCustomerName(), app.getCustomerLName());
		
		String result = new_id != -1 ? "Record entered, new id: "+ new_id : 
			   						   "Record has a problem";

		return Response.status(new_id != -1 ? 200 : 400).entity(result).build();
	}
	
	/**
	 * Removes the selected record by id, responds accordingly
	 * @param id The id to remove
	 * @return The response representing the success of the operation
	 */
	@DELETE
	@Path("/remove/{id}")
	public Response removeAppointment(@PathParam("id") String id) {
		int n_id = 0;
		boolean success = true;
		try {
			n_id = Integer.parseInt(id);
			
			// Remove the appointment
			success = database.deleteByID(n_id);
		}
		catch(Exception e) {
			System.out.println("Bad input");
			
			success = false;
		}
		
		String result = success ? "Record deleted: " : 
			   					  "Record had a problem";
		
		return Response.status(success ? 200 : 400).entity(result).build();
	}
	
	/**
	 * Updates the status of the appointment to finished, Responds accordingly
	 * @param id The id to update its status
	 * @return The response representing the success of the operation
	 */
	@PUT
	@Path("/update/{id}")
	public Response updateStatusOfAppointment(@PathParam("id") String id) {
		int n_id = 0;
		boolean success = true;
		try {
			n_id = Integer.parseInt(id);
			
			// Update the status of the appointment to true
			success = database.updateStatus(n_id);
		}
		catch(Exception e) {
			System.out.println("Bad input");
			
			success = false;
		}
		
		String result = success ? "Record status updated " : 
			   					  "Record had a problem";
		
		return Response.status(success ? 200 : 400).entity(result).build();
	}
	
	/**
	 * Gets all the appointments corresponding to the date range, those will
	 * be transfered to json
	 * @param startYear Start year of range
	 * @param startMonth Start month of range
	 * @param startDay Start date of range
	 * @param endYear End Year of range
	 * @param endMonth End month of range
	 * @param endDay End date of range
	 * @return List with all the corresponding appointments
	 */
	@GET
	@Path("/daterange/{startYear}/{startMonth}/{startDay}/{endYear}/{endMonth}/{endDay}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<IdAppointment> getByDateRange(@PathParam("startYear") int startYear,
			                                  @PathParam("startMonth") int startMonth,
			                                  @PathParam("startDay") int startDay,
			                                  @PathParam("endYear") int endYear,
			                                  @PathParam("endMonth") int endMonth,
			                                  @PathParam("endDay") int endDay) {
		
		// Set the dates by the integer values
		Calendar cal = Calendar.getInstance();
		cal.set( Calendar.YEAR, startYear );
		cal.set( Calendar.MONTH, startMonth -1 );
		cal.set( Calendar.DATE, startDay );
		cal.set( Calendar.HOUR, 0 );
		cal.set( Calendar.MINUTE, 0 );
		cal.set( Calendar.SECOND, 0 );
		
		Date start = new Date(cal.getTimeInMillis());
		
		cal.set( Calendar.YEAR, endYear );
		cal.set( Calendar.MONTH, endMonth -1 );
		cal.set( Calendar.DATE, endDay );
		cal.set( Calendar.HOUR, 0 );
		cal.set( Calendar.MINUTE, 0 );
		cal.set( Calendar.SECOND, 0 );
		
		Date end = new Date(cal.getTimeInMillis());
		
		// Return all the appointments as a range
		ResultSet rs = database.getAppointmentsByDateRange(start, end);
		
		List<IdAppointment> ret_list = new LinkedList<IdAppointment>();
		
		if(rs != null) {
			boolean finished = false;
			while(!finished) {
				IdAppointment app = new IdAppointment();
				
				// Set the data to classes, later will be transfered to JSON
				finished = !app.setFromResultSet(rs);
			
				if(!finished)
					ret_list.add(app);
			}
		}
		
		return ret_list;
	}

	
	/**
	 * The SQL database wrapper
	 */
	private carServiceDatabase database;
}
