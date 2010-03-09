package cuanto.user;

/**
 * A hyperlink.
 */
public class Link {
	String url;
	String description;
	Long id;

	Link (String url, String description) {
		this.url = url;
		this.description = description;
	}

	Link (String url) {
		this.url = url;
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


	void setId(Long id) {
		this.id = id;
	}
}
