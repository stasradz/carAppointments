package carAppointments;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.Calendar;

/**
 * Same as parent class, but also has time
 * @author Stas
 *
 */
public class timeAppointment extends Appointment {
	
	public timeAppointment() {
		super();
		
		Calendar cal = Calendar.getInstance();
		this.timeStart = Time.valueOf(cal.get(Calendar.HOUR_OF_DAY) + ":" + 
                					  cal.get(Calendar.MINUTE) + ":" + 
                					  cal.get(Calendar.SECOND) );
	}
	
	public Time getTimeStart() {
		return timeStart;
	}
	public void setTimeStart(Time timeStart) {
		this.timeStart = timeStart;
	}
	
	/**
	 * Same as parent method, but also sets time
	 * @param rs ResultSet from which to take the data
	 * @return Whether no error occurred
	 */
	public boolean setFromResultSet(ResultSet rs) {
		boolean success = super.setFromResultSet(rs);
		
		if(success) {
			try {
				this.setTimeStart(rs.getTime("timeStart"));
			}
			catch(SQLException e) {
				System.out.println("Error parsing SQL");
				
				success = false;
			}
		}
		
		return success;
	}
	
	Time timeStart;
}