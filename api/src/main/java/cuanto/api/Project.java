package cuanto.api;

/**
 * User: Todd Wells Date: 9/19/11 Time: 1:34 PM
 */
public class Project
{
	String name;
	String projectGroup;
	String projectKey;
	String bugUrlPattern;
	String testType;


	/**
	 * Creates a new Project.
	 *
	 * @param name          The name of the Project.
	 * @param projectGroup  The group the project belongs to.
	 * @param projectKey    The unique projectKey of the project.
	 * @param bugUrlPattern <p></p>The bug URL pattern is used for referencing an external bug tracking system. Use this
	 *                      field to describe the URL scheme for the bug tracking system by using the text {BUG} to
	 *                      substitute for the bug number or bug identifier in the bug tracking system's bug URLs.</p>
	 *                      <p/>
	 *                      <p>For example, iNf your Bugzilla tracking system has URLs like http://bugzilla/show_bug.cgi?id=14587,
	 *                      you would use a bug URL pattern of http://bugzilla/show_bug.cgi?id={BUG}. A JIRA bug tracking
	 *                      system might have URLs like http://jira/browse/CUANTO-34, then the bug tracking pattern would
	 *                      be http://jira/browse/{BUG}.
	 * @param testType      At present, JUnit, TestNG, NUnit and Manual are valid values.
	 */

	Project(String name, String projectGroup, String projectKey, String bugUrlPattern, String testType)
	{
		this.name = name;
		this.projectGroup = projectGroup;
		this.projectKey = projectKey;
		this.bugUrlPattern = bugUrlPattern;
		this.testType = testType;
	}


	/**
	 * Creates a new Project.
	 *
	 * @param name         The name of the Project.
	 * @param projectGroup The group the project belongs to.
	 * @param projectKey   The unique projectKey of the project.
	 * @param testType     At present, JUnit, TestNG, NUnit and Manual are valid values.
	 */
	Project(String name, String projectGroup, String projectKey, String testType)
	{
		this(name, projectGroup, projectKey, null, testType);
	}


	public String getName()
	{
		return name;
	}


	public void setName(String name)
	{
		this.name = name;
	}


	public String getProjectGroup()
	{
		return projectGroup;
	}


	public void setProjectGroup(String projectGroup)
	{
		this.projectGroup = projectGroup;
	}


	public String getProjectKey()
	{
		return projectKey;
	}


	public void setProjectKey(String projectKey)
	{
		this.projectKey = projectKey;
	}


	public String getBugUrlPattern()
	{
		return bugUrlPattern;
	}


	public void setBugUrlPattern(String bugUrlPattern)
	{
		this.bugUrlPattern = bugUrlPattern;
	}


	public String getTestType()
	{
		return testType;
	}


	public void setTestType(String testType)
	{
		this.testType = testType;
	}

}
