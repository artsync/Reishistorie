package nl.arthurvlug.ovclaim.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import nl.arthurvlug.ovclaim.database.Database;
import nl.arthurvlug.ovclaim.domain.Transaction;

import org.apache.commons.lang3.SerializationUtils;

public class Resources {
	private static Database database = new Database();
	
	public static List<Transaction> readTransactions(Config config) {
		return database.readTransactions(config);
	}

	public static void writeTransactions(ArrayList<Transaction> newTransactions, Config config) throws FileNotFoundException {
		database.writeTransactions(newTransactions, config);
		
		// Update the lastUpdated file
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String lastEditDate = sdf.format(Calendar.getInstance().getTime());
		SerializationUtils.serialize(lastEditDate, new FileOutputStream(file("lastUpdated", config)));
	}

	private static File file(String filenamePrefix, Config config) {
		return new File(filenamePrefix + "-" + config.getUsername() + ".dat");
	}
}
