package cuanto.base

import cuanto.api.WordGenerator
import cuanto.api.CuantoConnector

/**
 * User: Todd Wells
 * Date: 9/26/11
 * Time: 8:41 PM
 */
class TestBase extends GroovyTestCase {

	static WordGenerator wordGen = new WordGenerator()
	public static CUANTO_URL = "http://localhost:8080/cuanto"

	@Override
	void setUp() {
		super.setUp()
	}


	@Override
	void tearDown() {
		super.tearDown()
	}

}
