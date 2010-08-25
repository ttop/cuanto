package cuanto

class TestOutcomeLink implements Comparable {

	static belongsTo = [testOutcome: TestOutcome]

     String description
     String url


	int compareTo(Object o) {
		TestOutcomeLink other = (TestOutcomeLink) o
		int result = this.url.compareTo(other.url)
		if (result == 0) {
			return this.description.compareTo(other.url)
		} else {
			return result
		}
	}
}
