package nl.arthurvlug.ovclaim.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Config {
//	static final String USERNAME = "DrLauraLector";
//	static final String PASSWORD = "Laura02021991";
//	 = "arthurvlug"
//			 = "egwwtc74"
	
	private final Calendar startDate;
	private final Calendar endDate;
	private final String username;
	private final String password;
	
	public static Config extractUsernamePassword(String[] args) throws ParseException {
		if(args.length != 2) {
			System.err.println(""
					+ "Arguments should have 2 elements: username, password.\n"
					+ "username and password should be your OV-chipcard.n account credentials");
			System.exit(0);
		}
		
		Config config = new Config(null, null, args[0], args[1]);
		return config;
	}
	
	public static Config extractConfig(String[] args) throws ParseException {
		if(args.length != 4) {
			System.err.println(""
					+ "Arguments should have 4 elements: startDate, endDate, username, password.\n"
					+ "startDate and endDate should both have format yyyy-MM-dd (e.g. 2010-06-20 for 20 June 2010)\n"
					+ "username and password should be your OV-chipcard.n account credentials");
			System.exit(0);
		}
		Calendar startDate = calendar(args[0]);
		Calendar endDate = calendar(args[1]);
		String username = args[2];
		String password = args[3];
		
		Config config = new Config(startDate, endDate, username, password);
		return config;
	}

	private static Calendar calendar(String sDate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = sdf.parse(sDate);
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}
}
