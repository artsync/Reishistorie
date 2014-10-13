package nl.arthurvlug.ovclaim.domain;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

import org.joda.time.DateTime;

@EqualsAndHashCode
@AllArgsConstructor
@ToString
@Value
@Getter
public class Transaction {
	private DateTime checkIn;
	private DateTime checkOut;
	private String from;
	private String to;
	private Double amount;
	private String product;
	
	public boolean isCheckIn() {
		if(checkIn.getMillis() == nl.arthurvlug.ovclaim.database.Database.NO_TIMESTAMP.getTime()) {
			return false;
		}
		return true;
	}
}
