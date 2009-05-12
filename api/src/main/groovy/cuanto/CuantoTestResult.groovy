/*
 Copyright (c) 2008 thePlatform, Inc.

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


package cuanto
/**
 * User: Todd Wells
 * Date: Mar 3, 2009
 * Time: 5:58:46 PM
 * 
 */
class CuantoTestResult {

	CuantoTestResult(Long testRunId) {
		
	}


	String packageName
	String testName
	String description
	String testResult
	String testOutput
	String owner
	String bug
	String note
	String analysisState
}