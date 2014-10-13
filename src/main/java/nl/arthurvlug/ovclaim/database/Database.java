package nl.arthurvlug.ovclaim.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nl.arthurvlug.ovclaim.domain.Transaction;
import nl.arthurvlug.ovclaim.utils.Config;

import org.joda.time.DateTime;

public class Database {
	public static final Timestamp NO_TIMESTAMP = Timestamp.valueOf("1970-01-01 00:00:00");
	private String dbUrl = "jdbc:mysql://localhost/restitutie";
	private String dbClass = "com.mysql.jdbc.Driver";
	private String username = "prive";
	private String password = "apiemug";
	
	public Database() {
		try {
			Class.forName(dbClass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	
	public void writeTransactions(ArrayList<Transaction> newTransactions, Config config) {
		try {
			Connection connection = DriverManager.getConnection(dbUrl, username, password);
			for(Transaction transaction : newTransactions) {
				PreparedStatement statement = connection.prepareStatement(""
						+ " INSERT IGNORE INTO Transaction(`userId`, `checkIn`, `checkOut`, `from`, `to`, `amount`, `product`)"
						+ " VALUES(?, ?, ?, ?, ?, ?, ?)");
				statement.setString(1, config.getUsername());
				if(transaction.getCheckIn() == null) {
					statement.setTimestamp(2, Database.NO_TIMESTAMP);
				} else {
					statement.setTimestamp(2, new Timestamp(transaction.getCheckIn().toDate().getTime()));
				}
				if(transaction.getCheckOut() == null) {
					statement.setTimestamp(3, Database.NO_TIMESTAMP);
				} else {
					statement.setTimestamp(3, new Timestamp(transaction.getCheckOut().toDate().getTime()));
				}
				statement.setString(4, transaction.getFrom());
				statement.setString(5, transaction.getTo());
				statement.setDouble(6, transaction.getAmount());
				statement.setString(7, transaction.getProduct());
				
				statement.execute();
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	public List<Transaction> readTransactions(Config config) {
		Connection connection;
		List<Transaction> transactions = new ArrayList<>();
		try {
			connection = DriverManager.getConnection(dbUrl, username, password);
			PreparedStatement statement = connection.prepareStatement(""
					+ " SELECT `userId`, `checkIn`, `checkOut`, `from`, `to`, `amount`, `product`"
					+ " FROM Transaction"
					+ " WHERE userId = '" + config.getUsername() + "'"
					+ " ORDER BY GREATEST(checkIn, checkOut) DESC");
			
			ResultSet results = statement.executeQuery();
			while(results.next() != false) {
				DateTime checkIn = null;
				if(results.getTimestamp(2).compareTo(Database.NO_TIMESTAMP) == 0) {
					checkIn = new DateTime(Database.NO_TIMESTAMP.getTime());
				} else {
					checkIn = new DateTime(results.getTimestamp(2).getTime());
				}
				DateTime checkOut = null;
				if(results.getTimestamp(3).compareTo(Database.NO_TIMESTAMP) == 0) {
					checkOut = new DateTime(Database.NO_TIMESTAMP.getTime());
				} else {
					checkOut = new DateTime(results.getTimestamp(3).getTime());
				}
				String from = results.getString(4);
				String to = results.getString(5);
				Double amount = results.getDouble(6);
				String product = results.getString(7);
				
				Transaction transaction = new Transaction(checkIn, checkOut, from, to, amount, product);
				transactions.add(transaction);
			}
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return transactions;
	}
}
