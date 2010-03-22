package cuanto.user;

/**
 * A hyperlink.
 */
public class Link implements Comparable {
	String url;
	String description;
	Long id;


	Link(String url, String description, Long id) {
		this.url = url;
		this.description = description;
		this.id = id;
	}


	Link(String url, String description) {
		this(url, description, null);
	}


	Link(String url) {
		this(url, null, null);
	}


	public String getUrl() {
		return url;
	}


	public String getDescription() {
		return description;
	}


	public Long getId() {
		return id;
	}


	public int compareTo(Object o) {
		Link otherLink = (Link) o;
		if (this.getDescription() == null && otherLink.getDescription() != null) {
			return -1;
		}

		if (this.getDescription() != null) {
			return this.getDescription().compareTo(otherLink.getDescription());
		}

		if (this.getUrl() == null && otherLink.getUrl() != null) {
			return -1;
		}

		return this.getUrl().compareTo(otherLink.getUrl());
	}

	public String toString() {
		if (this.description != null) {
			return this.description + ": " + this.url;
		}
		else {
			return this.url;
		}
	}
}
