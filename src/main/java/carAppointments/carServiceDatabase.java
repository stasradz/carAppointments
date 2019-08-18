package carAppointments;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Statement;
import java.sql.Time;
import java.util.Random;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DriverManager;

/**
 * This class will be connecting to the Azure SQL database provided by the constructor params,
 * It has all the API, that deals with the datastore.
 * @author Stas
 *
 */
class carServiceDatabase {
	
	/**
	 * Constructor
	 * @param hostName The name of the server
	 * @param dbName The name of the database
	 * @param user Admin username
	 * @param password Admin password
	 */
	public carServiceDatabase(String hostName, String dbName, String user, String password) {
		this.hostName = hostName;
		this.dbName = dbName;
		this.user = user;
		this.password = password;
		
		lastID = -1;
	}
	
	/**
	 * Insert a new appointment by the id
	 * @param appointmentDate Date of the appointment
	 * @param timeStart Start time of the appointment
	 * @param price Price
	 * @param customerName Customer First Name
	 * @param customerLName Customer Last Name
	 * @return The inserted id, -1 if error happened
	 */
	public int insertAppointment(Date appointmentDate, Time timeStart, int price,
								 String customerName, String customerLName) {
		
		// Insert the appointment to database
		boolean success = insertToDatabase(appointmentDate, timeStart, price,
										   customerName, customerLName);
		
		if(success)
		{
			bumpLastID();
			return lastID;
		}
		else
			return -1;
	}
	
	/**
	 * Update the status of the appointment to be finished
	 * @param appointmentID The ID of the appointment to be updated
	 * @return Whether the update found the appointment ID in the database
	 */
	public boolean updateStatus(int appointmentID) {
		String query = "UPDATE Appointments \n" + 
					   "SET status = 'True' \n" + 
					   "WHERE appointmentID = ?;";
		
		// Execute the query to update the status of the appointment to Finished
		return executeSingleUpdateQuery(query, appointmentID);
	}
	
	/**
	 * Delete the entry from the database, that has the following id
	 * @param appointmentID The id to be deleted
	 * @return Whether it was deleted
	 */
	public boolean deleteByID(int appointmentID) {
		String query = "DELETE FROM Appointments WHERE appointmentID=?";
	
		// Delete by ID
		return executeSingleUpdateQuery(query, appointmentID);
	}
	
	public ResultSet getAppointmentsByDateRange(Date start, Date end) {
		String query =   "SELECT * FROM Appointments \n"
		               + "WHERE appointmentDate >= ? AND appointmentDate <= ? \n"
		               + "ORDER BY Price;";
		
		// Select all the appointments by daterange and oerder by price
		return executeSelectQuery(query, -1, start, end);
	}
	
	/**
	 * Get the ResultSet with the Appointment corresponding to the id.
	 * @param appointmentID The id of the appointment
	 * @return The ResultSet containing the appointment. NULL if doesn't exist
	 */
	public ResultSet getAppointmentByID(int appointmentID) {
		String query = "SELECT * FROM Appointments WHERE appointmentID = ?";
	
		// Select by ID
		return executeSelectQuery(query, appointmentID, null, null);
	}
	
	/**
	 * Schedule An appointment at a random time.
	 * @param appointmentDate The Date of the appointment
	 * @param price The price
	 * @param customerName Customer Name
	 * @param customerLName Customer Family Name
	 * @return The id of the created appointment
	 */
	public int scheduleRandomAppointment(Date appointmentDate, int price,
										 String customerName, String customerLName) {
		
		//Generate random time
		final Random random = new Random();
		final int millisInDay = 24*60*60*1000;
		Time time = new Time((long)random.nextInt(millisInDay));
		
		// Insert it into the database
		return insertAppointment(appointmentDate, time, price, customerName, customerLName);
	}
	
	/**
	 * Execute Select query with optional parameters, it is assumed that the number of ?'s
	 * in the query equals the provided real parameters
	 * @param query The query string with ?'s
	 * @param appointmentID Appointment id to be included in the query, won't be included if -1
	 * @param start Optional start Date, can be null
	 * @param end Optional End data, can be null
	 * @return A Result Set with the query results
	 */
	private ResultSet executeSelectQuery(String query, int appointmentID, Date start, Date end) {
		boolean has_conn = getConnection();
		
		if(has_conn)
		{
			int idx = 1;
			
			try {
				PreparedStatement stmt = connection.prepareStatement(query);
				
				// If the arguments are valid, set them into the query and bump the idx
				if(appointmentID != -1)
				{
					stmt.setInt (idx, appointmentID);
					++idx;
				}
				
				if(start != null)
				{
					stmt.setDate(idx, start);
					++idx;
				}
				
				if(end != null)
				{
					stmt.setDate(idx, end);
				}
				
				// Execute the query
        		ResultSet resultSet = stmt.executeQuery();
        		
        		resultSet.setFetchSize(30);
        		
        		return resultSet;
			}
			catch(SQLException e) {
				System.out.println("Error while running Select Query");
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * Execute an update or delete query, that requires only one id parameter
	 * @param query The query string
	 * @param appointmentID The iD of the appointment
	 * @return Whether the operation did any change
	 */
	private boolean executeSingleUpdateQuery(String query, int appointmentID) {
		boolean has_conn = getConnection();
	
		if(has_conn)
		{
			try {
				// Create a prepaed statement and set an ID to it.
				PreparedStatement stmt = connection.prepareStatement(query);
				stmt.setInt   (1, appointmentID);
				
				// Execute the update query
				int num_aff = stmt.executeUpdate();
				
				return num_aff > 0;
			}
			catch(SQLException e) {
				System.out.println("Error while updating");
				
				return false;
			}
		}
		
		return false;
	}
	
	/**
	 * Increment the last id by 1, this is to be used after insertion 
	 * @return The incremented last ID
	 */
	private int bumpLastID()
	{
		// Update the ID if needed
		if(lastID == -1)
			getLastID();
		else
			++lastID;
		
		return lastID;
	}
	
	/**
	 * Get the last appointment ID in the database, this value is cached. If cache is unavailable
	 * get it from the database.
	 * @return the last ID
	 */
	private int getLastID() {
		// If last ID is not set, retrieve it from database
		if(lastID == -1) {
			String query = "SELECT MAX(appointmentID) FROM Appointments";
			
			ResultSet rs = executeSelectQuery(query);
			
			if(rs != null) {
				try {
					if(!rs.next())
						lastID = 0;
					else
						lastID = rs.getInt(1);
				}
				catch(SQLException e) {
					System.out.println("Error retrieving the last ID");
					lastID = -1;
				}
			}
			else
				lastID = -1;
		}
		
		// Return the last id
		return lastID;
	}
	
	/**
	 * Finalizer, will close the database connection
	 */
	@Override
	public void finalize() {
		// Close the resource, if present
	    closeConnection();
	}
	
	/**
	 * Insert a new Appointment  to the database
	 * @param appointmentDate The date of the appointment
	 * @param timeStart Beginning time for the appointment
	 * @param price The Price
	 * @param customerName The First Name of the customer
	 * @param customerLName The  second name of the customer
	 * @return Whether insert succeeded
	 */
	private boolean insertToDatabase(Date appointmentDate, Time timeStart, int price,
									 String customerName, String customerLName)
	{
		// Insert an entire appointment data
		String query = "INSERT INTO Appointments(appointmentDate, timeStart, Price, " +
					   "customerName, customerLName, status) VALUES(?, ?, ?, ?, ?, ?)";
		
		boolean has_conn = getConnection();
		
		if(has_conn)
		{
			try {
				// Prepare the statement and set the arguments
				PreparedStatement stmt = connection.prepareStatement(query);
				stmt.setDate  (1, appointmentDate);
				stmt.setTime  (2, timeStart);
				stmt.setInt   (3, price);
				stmt.setString(4, customerName);
				stmt.setString(5, customerLName);
				stmt.setBoolean(6,  false);
				
				// Execute
				stmt.executeUpdate();
				
				return true;
			}
			catch(SQLException e) {
				System.out.println("An error occured while inserting");
				
				return false;
			}
		}
		
		return false;
	}
	/**
	 * Executes an SQL Insert query provided, if other query type is passed,
	 * will do nothing and return null
	 * @param SQLquery The SQL query to execute
	 * @return The result of the select query
	 */
	private ResultSet executeSelectQuery(String SQLquery) {
		boolean has_conn = getConnection();
		
		if(has_conn) {
			
			try {
				// Create a simple statement and execute
				Statement statement = connection.createStatement();
        		ResultSet resultSet = statement.executeQuery(SQLquery);
        		
        		return resultSet;
			}
			catch(SQLException e) {
				System.out.println("SQL Select Query error");
				
				return null;
			}
		}
		
		return null;
	}
	
	/**
	 * Try to close the SQL connection
	 * @param connection the connection to close
	 */
	private void closeConnection() {
		if(connection == null)
			return;
		
		try {
			if(!connection.isClosed())
				connection.close();
		}
		catch(SQLException e) {
			System.out.println("Could not close connection");
		}
		finally {
			connection = null;
		}
	}

	/**
	 * This function will create a connection object to the database.
	 * This object should be closed after use.
	 * @return Whether the connection was established successfully
	 */
	private boolean getConnection() {
		closeConnection();
		
		// Connet to the azure database
        String url = String.format("jdbc:sqlserver://%s:1433;database=%s;user=%s;password=%s;encrypt=true;"
                                   + "hostNameInCertificate=*.database.windows.net;loginTimeout=30;",
                                   hostName, dbName, user, password);
        
        try {
        	connection = DriverManager.getConnection(url);
        	return true;
        }
        catch(SQLException e) {
        	System.out.println("There was a problem with SQL database connection");
        	return false;
        }
	}
	
	
	
	private String hostName;
	private String dbName;
	private String user;
	private String password;
	
	private Connection connection;
	
	private int lastID;
}
