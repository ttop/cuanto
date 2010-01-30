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


package cuanto.api

/**
 * A state of the analysis, or cause of a failure. The possible values are determined by the Cuanto database.
 * The analysisStates in a default installation are "Unanalyzed", "Bug", "Environment", "Harness", "No Repro", "Other",
 * "Test Bug", and "Investigate".
 */
public class AnalysisState {

	String name

	/**
	 * @param isAnalyzed Whether this analysis state should be considered "Analyzed" or not when determining what 
	 * TestOutcomes have been analyzed.
	 */
	Boolean isAnalyzed

	/**
	* @return Whether this AnalysisState is
	*/
	Boolean isDefault
	Boolean isBug


	String toString() {
		name
	}


}