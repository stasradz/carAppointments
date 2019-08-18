package carAppointments;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Same as parent class, but also has id
 * @author Stas
 *
 */
public class IdAppointment extends timeAppointment {
	public IdAppointment() {
		super();
		this.id = -1;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * Same as parent method, but also sets id
	 * @param rs ResultSet from which to take the data
	 * @return Whether no error occurred
	 */
	public boolean setFromResultSet(ResultSet rs) {
		boolean success = super.setFromResultSet(rs);
		
		if(success) {
			try {
				this.setId(rs.getInt("appointmentID"));
			}
			catch(SQLException e) {
				System.out.println("Error parsing SQL");
				
				success = false;
			}
		}
		
		return success;
	}
	
	int id;
}