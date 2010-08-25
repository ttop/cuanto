package cuanto

class TestOutcomeProperty implements Comparable {

	static belongsTo = [testOutcome: TestOutcome]

	String name
	String value

	int compareTo(Object o) {
		TestOutcomeProperty other = (TestOutcomeProperty) o
		int result = this.name.compareTo(other.name)
		if (result == 0) {
			return this.value.compareTo(other.value)
		} else {
			return result
		}
	}
}
