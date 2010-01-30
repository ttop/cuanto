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
 * The interface for the CuantoClient.
 */
public interface ICuantoClient {


	/**
	 * Creates a project. The project's id value will be set once created.
	 *
	 * @param project The project to create.
	 * @return The ID of the project
	 */
	public Long createProject(Project project)


	/**
	 * Deletes a project and all of it's associated data.
	 *
	 * @param id The ID of the project to delete.
	 */
	public void deleteProject(Long id)


	/**
	 * Fetch a project's details.
	 *
	 * @param projectId The ID of the project
	 * @return The Project
	 */
	public Project getProject(Long projectId)


	/**
	 * Fetch a project's details.
	 *
	 * @param projectKey The projectKey of the project
	 * @return The Project
	 */
	public Project getProject(String projectKey)


	/**
	* Fetch a test run's details.
	* @param testRunId The ID of the test run
	* @return The TestRun
	*/
	public TestRun getTestRun(Long testRunId)


	/**
	* Create a new test run. The Test Run's id value will be set once created.
	 *
	* @param testRun The TestRun to create.
	* @return The ID of the created Test Run
	*/
	public Long createTestRun(TestRun testRun)


	/**
	* Update a test run with the provided values. Uses the TestRun.id field to determine which TestRun to update.
	* @param testRun The new TestRun details
	*/
	public void updateTestRun(TestRun testRun)


	/**
	* Delete a test run.
	* @param testRunId The ID of the test run to delete
	*/
	public void deleteTestRun(Long testRunId)


	/**
	 * Fetch an existing TestOutcome's details.
	 * @param testOutcomeId The ID of the TestOutcome to fetch
	 * @return The TestOutcome
	 */
	public TestOutcome getTestOutcome(Long testOutcomeId)


	/**
	* Submit a file to the Cuanto server for parsing. The file needs to be of the appropriate type (JUnit, TestNG) for the project.
    * @param file The file to submit.
	* @param testRunId The ID of the TestRun for this file.
	*/
	public void submitFile(File file, Long testRunId)

	/**
	* Submit a group of files to the Cuanto server for parsing. The files need to be of the appropriate type (JUnit, TestNG) for the project.
    * @param files A list of files to submit.
	* @param testRunId The ID of the TestRun for this file.
	*/
	public void submitFiles(List<File> files, Long testRunId)


	/**
	 * Add a new TestOutcome to the specified TestRun. The ID value of the TestOutcome will be assigned.
	 * @param testOutcome The TestOutcome to add
	 * @param testRunId The ID of the TestRun to add the TestOutcome to
	 * @return The ID assigned to the created TestOutcome
	 */
	public Long createTestOutcomeForTestRun(TestOutcome testOutcome, Long testRunId)


	/**
	 * Add a new TestOutcome to the specified project. This is for adding TestOutcomes that are not affliated with
	 * a specific TestRun. The ID value of the TestOutcome will be assigned.
	 * @param testOutcome The TestOutcome to add
	 * @param projectId The ID of the Project to add the TestOutcome to.
	 * @return The ID assigned to the created TestOutcome
	 */
	public Long createTestOutcomeForProject(TestOutcome testOutcome, Long projectId)



	/**
	 * Update the fields of specified TestOutcome (based on it's ID).
	 * @param testOutcome The updated TestOutcome
	 */
	public void updateTestOutcome(TestOutcome testOutcome)


	/**
	 * Delete the specified TestOutcome.
	 * @param testOutcomeId The ID of the TestOutcome to delete.
	 */
	public void deleteTestOutcome(Long testOutcomeId)


	/**
	 * Get all TestRuns for the specified Project that include the specified TestProperties. The properties can be a
	 * subset of a TestRun's properties, but all of the specified properties must match for a TestRun to be returned.
	 * @param projectId The ID of the project to search.
	 * @param testProperties A List of TestProperties for which to search.
	 * @return A List of TestRuns with match properties, in descending order by dateExecuted.
	 */
	public List<TestRun> getTestRunsWithProperties(Long projectId, List<TestProperty> testProperties)


	/**
	 * For the a project, locate the TestCase the matches the specified values. All of the specified values must match for
	 * the TestCase to be returned.
	 * @param projectId The ID of the Project to in which to search
	 * @param testPackage The testPackage of the TestCase
	 * @param testName The testName of the TestCase
	 * @param parameters The parameters of the TestCase
	 * @return The matching TestCase or null if none is found.
	 */
	public TestCase getTestCase(Long projectId, String testPackage, String testName, String parameters)


	/**
	 * For the a project, locate the TestCase the matches the specified values. All of the specified values must match for
	 * the TestCase to be returned.
	 * @param projectKey The projectKey of the Project to in which to search
	 * @param testPackage The testPackage of the TestCase
	 * @param testName The testName of the TestCase
	 * @param parameters The parameters of the TestCase
	 * @return The matching TestCase or null if none is found.
	 */
	public TestCase getTestCase(String projectKey, String testPackage, String testName, String parameters)


	/**
	 * Get all of the TestOutcomes for the specified TestCase and TestRun. If multiple TestOutcomes exist for this TestCase,
	 * they will be returned in descending order by the "finishedAt" value of the TestOutcomes (if any is available). In
	 * most circumstances there is only one execution (TestOutcome) of a specific TestCase in a single TestRun.
	 * @param testRunId The ID of the TestRun to search
	 * @param testCaseId The TestCase ID for which you wish to fetch the TestOutcomes
	 * @return A List of TestOutcomes in descending order by the "finishedAt" value.
	 *
	 */
	public List<TestOutcome> getTestOutcomes(Long testRunId, Long testCaseId)


	/**
	 * Get all of the TestOutcomes for the specified TestRun.
	 * @param testRunId The ID of the TestRun
	 * @return All of the TestOutcomes for the specified TestRun. 
	 */
	public List<TestOutcome> getAllTestOutcomes(Long testRunId)

}