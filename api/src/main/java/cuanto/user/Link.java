package cuanto.user;

/**
 * A hyperlink.
 */
public class Link {
	String url;
	String description;
	Long id;

	Link (String url, String description, Long id) {
		this.url = url;
		this.description = description;
		this.id = id;
	}

	Link (String url, String description) {
		this(url, description, null);
	}

	Link (String url) {
		this(url, null, null);
	}


	public String getUrl() {
		return url;
	}


	public String getDescription() {
		return description;
	}


	Long getId() {
		return id;
	}
}
