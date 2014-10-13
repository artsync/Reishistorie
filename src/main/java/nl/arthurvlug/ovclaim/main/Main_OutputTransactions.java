package nl.arthurvlug.ovclaim.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import nl.arthurvlug.ovclaim.domain.Transaction;
import nl.arthurvlug.ovclaim.domain.Trip;
import nl.arthurvlug.ovclaim.utils.Config;
import nl.arthurvlug.ovclaim.utils.Resources;

import org.apache.commons.io.FileUtils;

public class Main_OutputTransactions {
	private final Config config;
	private final StringBuilder sb = new StringBuilder();

	public Main_OutputTransactions(Config config) throws IOException {
		this.config = config;

		List<Trip> trips = trips();

		for(int i = 0; i < trips.size(); i++) {
			Trip trip = trips.get(i);
			if (trip.forgotCheckout() && trip.getAmount() != 0) {
				outputTrip(trips, i);
			}
		}

		sb.append("Finished\n");

		System.out.println(sb.toString());
		FileUtils.write(new File("console"), sb.toString());
	}

	/**
	 * Prints one trip, and the trip before and after
	 * 
	 * @param trips
	 * @param i
	 */
	private void outputTrip(List<Trip> trips, int i) {
		sb.append("\n");
		sb.append("---------------------------------------------\n");

		sb.append("Vergeten uit te checken!\n");
		if (i > 0) {
			sb.append("Previous trip: " + trips.get(i - 1) + "\n");
		}
		sb.append("Trip waar het om gaat: " + trips.get(i) + "\n");

		if (i < trips.size() - 1) {
			sb.append("Next trip: " + trips.get(i + 1) + "\n");
		}

		sb.append("---------------------------------------------\n");
		sb.append("\n");
	}

	private List<Trip> trips() throws FileNotFoundException {
		// Read the transactions from cache
		List<Transaction> transactions = Resources.readTransactions(config);
		//		System.out.println(StringUtils.join(transactions, "\n"));

		// Convert transactions to trips
		List<Trip> trips = new ArrayList<>();
		Transaction last = null;
		for(Transaction t : transactions) {
			if (t.isCheckIn()) {
				Trip trip = new Trip(t, last);
				trips.add(trip);
			}
			last = t;
		}
		return trips;
	}

	public static void main(String[] args) throws ParseException, IOException {
		Config config = Config.extractUsernamePassword(args);
		new Main_OutputTransactions(config);
	}
}
