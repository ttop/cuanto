package cuanto.user;

/**
 * Encapsulates a name and value.
 */
public class TestProperty {
	String name;
	String value;
	Long id;

	TestProperty(String name, String value) {
		this.name = name;
		this.value = value;
	}

	void setId(Long id) {
		this.id = id;
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
