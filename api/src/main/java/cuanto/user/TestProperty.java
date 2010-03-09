package cuanto.user;

/**
 * Encapsulates a name and value.
 */
public class TestProperty {
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
}
