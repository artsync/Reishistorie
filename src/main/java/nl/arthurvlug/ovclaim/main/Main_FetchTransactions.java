package nl.arthurvlug.ovclaim.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.arthurvlug.ovclaim.domain.Transaction;
import nl.arthurvlug.ovclaim.utils.Config;
import nl.arthurvlug.ovclaim.utils.Resources;
import nl.arthurvlug.ovclaim.utils.exceptions.IncorrectCredentialsException;
import nl.arthurvlug.ovclaim.utils.exceptions.NoHistoryException;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVReader;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class Main_FetchTransactions {
	private final WebClient webClient = init();
	private final Config config;

	public Main_FetchTransactions(Config config) throws FailingHttpStatusCodeException, MalformedURLException, IOException, ParseException {
		this.config = config;
		
		try {
			// Fetch and parse the transaction data
			HtmlPage transactionPage = login();
			
			transactionPage = selectDate(transactionPage);
			TextPage csv = fetchCSV(transactionPage);
			ArrayList<Transaction> transactions = findTransactions(csv);
			
			// Write serialized transactions to file
			Resources.writeTransactions(transactions, config);
			
			System.out.println("Finished writing " + transactions.size() + " transactions");
			
		} catch(NoHistoryException e) {
			System.out.println("There is no history for this user");
		} catch (IncorrectCredentialsException e) {
			System.out.println("User/password combination incorrect");
		}
	}

	private HtmlPage login() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		HtmlPage page = fetchPage("https://www.ov-chipkaart.nl/mijnovchipkaart/reizenentransacties/mijnreizenentransacties/toonreizenentransacties/");
		((HtmlTextInput) page.getElementByName("gebruikersnaam:input")).setValueAttribute(config.getUsername());
		((HtmlPasswordInput) page.getElementByName("wachtwoord:input")).setValueAttribute(config.getPassword());
		HtmlPage newPage = page.getElementByName("inloggen").click();
		return newPage;
	}
	
	private HtmlPage selectDate(HtmlPage transactionPage) throws IOException, IncorrectCredentialsException {
//		printPage(transactionPage);
		
		// Select "Typ periode in" & submit
		HtmlSelect select;
		try {
			select = transactionPage.getElementByName("periodes");
		} catch(ElementNotFoundException e) {
			throw new IncorrectCredentialsException();
		}
		transactionPage = select.setSelectedAttribute("1", true);
		transactionPage = transactionPage.getElementByName("submitzoekopdracht").click();
		
		// Set only trips, the date & submit again
		setDate(transactionPage);
		HtmlSelect transactieTypeSelect = transactionPage.getElementByName("transactieTypes");
		transactieTypeSelect.setSelectedAttribute("1", true);
		transactionPage = transactionPage.getElementByName("submitzoekopdracht").click();
		
		// Check if we have errors
		Iterable<HtmlElement> elements = transactionPage.getElementById("id1b").getChildElements();
		for(HtmlElement htmlElement : elements) {
			if("feedbackPanel".equals(htmlElement.getAttribute("class"))) {
				throw new IllegalArgumentException("Date is not correct: " + htmlElement.asXml());
			}
		}
		return transactionPage;
	}

	private void printPage(HtmlPage transactionPage) {
		System.out.println(transactionPage.asXml());
		System.out.println("");
	}

	private TextPage fetchCSV(HtmlPage transactionPage) throws IOException, MalformedURLException, NoHistoryException {
		// Fetch CSV
		HtmlElement checkAllCheckbox = transactionPage.getElementById("idc");
		if(checkAllCheckbox == null) {
			throw new NoHistoryException();
		}
		checkAllCheckbox.click();
		return transactionPage.getElementById("save").getChildElements().iterator().next().click();
	}
	
	private ArrayList<Transaction> findTransactions(TextPage csv) throws IOException, ParseException {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm");
		CSVReader csvReader = new CSVReader(new StringReader(csv.getContent()), ';');
		
		// Skip header
		csvReader.readNext();
		
		// Read all rows
		String[] row = null;
		ArrayList<Transaction> list = new ArrayList<>();
		while((row = csvReader.readNext()) != null) {
			DateTime checkIn = null;
			if(!StringUtils.isEmpty(row[1])) {
				checkIn = fmt.parseDateTime(row[0] + " " + row[1]);
			}
			
			DateTime checkOut = null;
			if(!StringUtils.isEmpty(row[3])) {
				checkOut = fmt.parseDateTime(row[0] + " " + row[3]);
			}
			
			Transaction transaction = new Transaction(checkIn, checkOut, row[2], row[4], Double.parseDouble(row[5].replace(',', '.')), row[8]);
			list.add(transaction);
		}
		
		// Close reader
		csvReader.close();
		
		return list;
	}

	@SuppressWarnings("serial")
	private WebClient init() throws FileNotFoundException {
		Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
		
		WebClient webClient = new WebClient(BrowserVersion.CHROME_16);
		webClient.setThrowExceptionOnScriptError(false);
		webClient.setJavaScriptEnabled(true);
		webClient.setCssEnabled(false);
		webClient.getCookieManager().clearCookies();
		webClient.getCookieManager().setCookiesEnabled(true);
		webClient.setAjaxController(new AjaxController(){
		    public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
		        return true;
		    }
		});
		
		return webClient;
	}

	private void setDate(HtmlPage transactiePage) {
		setField(transactiePage, "startDatum-year", config.getStartDate().get(Calendar.YEAR));
		setField(transactiePage, "startDatum-month", config.getStartDate().get(Calendar.MONTH)+1);
		setField(transactiePage, "startDatum-day", config.getStartDate().get(Calendar.DAY_OF_MONTH));

		setField(transactiePage, "eindDatum-year", config.getEndDate().get(Calendar.YEAR));
		setField(transactiePage, "eindDatum-month",config.getEndDate().get(Calendar.MONTH)+1);
		setField(transactiePage, "eindDatum-day", config.getEndDate().get(Calendar.DAY_OF_MONTH));
	}
	
	private void setField(HtmlPage page, String fieldId, int value) {
		HtmlTextInput field = (HtmlTextInput) page.getElementById(fieldId);
		field.setValueAttribute(value + "");
	}
	
	private HtmlPage fetchPage(String transactieUrl) throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		return webClient.getPage(transactieUrl);
	}

	public static void main(String[] args) throws FailingHttpStatusCodeException, MalformedURLException, IOException, ParseException {
		Config config = Config.extractConfig(args);
		new Main_FetchTransactions(config);
	}
}
