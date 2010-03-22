package cuanto.user;

/**
 * Encapsulates a name and value.
 */
public class TestProperty implements Comparable{
	String name;
	String value;
	Long id;

	TestProperty(String name, String value, Long id) {
		this.name = name;
		this.value = value;
		this.id = id;
	}

	TestProperty(String name, String value) {
		this(name, value, null);
	}

	public String getName() {
		return name;
	}


	public String getValue() {
		return value;
	}


	public Long getId() {
		return id;
	}


	public int compareTo(Object o) {
		TestProperty otherProperty = (TestProperty) o;
		if (this.getName() == null && otherProperty.getName() != null) {
			return -1;
		}

		if (this.getName() != null) {
			return this.getName().compareTo(otherProperty.getName());
		}

		if (this.getValue() == null && otherProperty.getValue() != null) {
			return -1;
		}

		return this.getValue().compareTo(otherProperty.getValue());
	}
}
