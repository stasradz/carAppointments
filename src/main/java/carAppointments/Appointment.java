package carAppointments;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/**
 * Package class for the appointment, this class will be transfered to json.
 * The class has only getters and setters and setter from ResultSet.
 * @author Stas
 *
 */
public class Appointment {
	
	public Appointment() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 1);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		
		this.appointmentDate = new Date(cal.getTimeInMillis());
		
		this.price = 0;
		
		this.customerName = "";
		this.customerLName = "";
		
	}
	
	public Date getAppointmentDate() {
		return appointmentDate;
	}
	public void setAppointmentDate(Date appointmentDate) {
		this.appointmentDate = appointmentDate;
	}
	public int getPrice() {
		return price;
	}
	public void setPrice(int price) {
		this.price = price;
	}
	public String getCustomerName() {
		return customerName;
	}
	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}
	public String getCustomerLName() {
		return customerLName;
	}
	public void setCustomerLName(String customerLName) {
		this.customerLName = customerLName;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Set the  fields of class from ResultSet, it should contain only
	 * @param rs ResultSet from which to take the data
	 * @return Whether no error occurred
	 */
	public boolean setFromResultSet(ResultSet rs) {
		try {
			if(!rs.next())
				return false;
		
			this.setAppointmentDate(rs.getDate("appointmentDate"));
			this.setCustomerName(rs.getString("customerName"));
			this.setCustomerLName(rs.getString("customerLName"));
			this.setPrice(rs.getInt("Price"));
			this.setStatus(rs.getBoolean("status") ? "Finished" : "Unfinished");
			
			return true;
		}
		catch(SQLException e) {
			System.out.println("Error parsing SQL");
			
			return false;
		}
	}
	
	
	Date appointmentDate;
	int price;
	String customerName;
	String customerLName;
	String status;
}
