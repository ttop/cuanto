package cuanto

class Link implements Comparable {

    static constraints = {
	    description(nullable:true)
    }

	String description
	String url


	public int compareTo(Object t) {
		Link other = (Link) t
		int descriptionCompare = this.description.compareTo(other.description)
		if (descriptionCompare == 0) {
			return this.url.compareTo(other.url)
		} else {
			return descriptionCompare
		}
	}

	public boolean equals(Object t) {
		Link other = (Link) t
		return this.description == other.description && this.url == other.url
	}
}
