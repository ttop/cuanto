/*
Copyright (c) 2010 Todd Wells

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package cuanto.sample;

import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class GroupedSample {

	@Test
	public void testOne() {
		failOne();
	}


	private void failOne() {
		fail("This has failed once");
	}


	@Test
	public void testTwoA() {
		failTwo();
	}


	@Test
	public void testTwoB() {
		failTwo();
	}


	private void failTwo() {
		fail("This has failed twice");
	}


	@Test
	public void testThreeA() {
		failThree();
	}


	@Test
	public void testThreeB() {
		failThree();
	}


	@Test
	public void testThreeC() {
		failThree();
	}


	private void failThree() {
		fail("This has failed three times");
	}


	@Test
	public void testFourA() {
		failFour();
	}


	@Test
	public void testFourB() {
		failFour();
	}


	@Test
	public void testFourC() {
		failFour();
	}


	@Test
	public void testFourD() {
		failFour();
	}


	private void failFour() {
		fail("This has failed four times");
	}

}
