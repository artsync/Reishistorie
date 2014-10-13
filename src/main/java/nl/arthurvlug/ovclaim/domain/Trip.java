package nl.arthurvlug.ovclaim.domain;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;



public class Trip {
	private Transaction checkInTransaction;
	private Transaction checkOutTransaction;

	public Trip(Transaction checkInTransaction, Transaction checkOutTransaction) {
		this.checkInTransaction = checkInTransaction;
		if(checkOutTransaction != null && checkOutTransaction.isCheckIn()) {
			this.checkOutTransaction = new NoCheckOutTransaction();
		} else {
			this.checkOutTransaction = checkOutTransaction;
		}
	}

	public boolean forgotCheckout() {
		return checkOutTransaction instanceof NoCheckOutTransaction;
	}
	
	@Override
	public String toString() {
		DateTime checkIn = checkInTransaction.getCheckIn();
		DateTime checkOut = checkOutTransaction.getCheckOut();
		String from = checkInTransaction.getFrom();
		String to = checkOutTransaction.getTo();
		String product = checkOutTransaction.getProduct();
		
		return "Transaction(\n" +
			"	Check in:  " + dateTimeToString(checkIn) + " from " + from + "\n" +
			"	Check out: " + dateTimeToString(checkOut) + " to " + to + "\n" +
			"	Amount: [" +
					"In: " + checkInTransaction.getAmount() + ", " +
					"Out: " + (checkOutTransaction.getAmount() == null ? "?" : checkOutTransaction.getAmount()) +
			"]\n" +
			"	Product: " + product + "\n" +
		")";
	}

	private String dateTimeToString(DateTime dateTime) {
		if(dateTime == null) {
			return "?";
		}
		DateTimeFormatter parser1 = DateTimeFormat.forPattern("dd MMMM yyyy HH:mm");
		return parser1.print(dateTime);
	}

	public Double getAmount() {
		return checkInTransaction.getAmount();
	}
}
