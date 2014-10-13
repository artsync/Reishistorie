package nl.arthurvlug.ovclaim.domain;

@SuppressWarnings("serial")
public class NoCheckOutTransaction extends Transaction {
	public NoCheckOutTransaction() {
		super(null, null, "?", "?", null, "?");
	}
}
