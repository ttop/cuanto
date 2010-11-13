%{--

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


--}%


<%@ page contentType="text/html;charset=UTF-8" %>
<html>
  <head><title>Cuanto: Help</title>
	  <meta name="layout" content="main" />
	  <p:css name='help'/>
  </head>
  <body>

	  <div id="helpRegion">

		  <img src="${resource(dir: 'images/logos', file:'cuanto-logo-48.png')}" alt="Cuanto logo"/>
		  This is Cuanto version <g:meta name="app.version"/>.
		  
		  <h1>Help</h1>

		  <h2><a href="#setting_up_a_project">Setting up a project</a></h2>
		  <h3>Project groups</h3>
		  <h3>Project key</h3>
		  <h3>Bug URL pattern</h3>
		  <h3>Manual test cases</h3>

		  <h2><a href="#using_the_cuanto_ant_task">Using the Cuanto ant task</a></h2>
		  <h3>Downloading the Cuanto ant task</h3>
		  <h3>Defining the task in an ant file</h3>
		  <h3>Submitting test results with the ant task</h3>

		  <h2><a href="#test_run_history">Test Run History</a></h2>

		  <h2><a href="#test_run_results">Test Run Analysis</a></h2>
		  <h3>Filtering test results</h3>
		  <h3>Searching test results</h3>
		  <h3>Test name formatting</h3>
		  <h3>Analyzing test results</h3>
		  <h3>Test output</h3>
		  <h3>Batch analysis</h3>
		  <h3>Test case history</h3>

		  <h3>Test run summary</h3>

		  <h2><a href="#searching">Searching</a></h2>

		  <h2><a href="#manual_test_case_mgmt">Manual test case management</a></h2>

          <h2><a href="#using_cuanto_adapters">Using Cuanto Adapters</a></h2>
          <h3>TestNgListener</h3>

		  <h2><a href="#about">About Cuanto</a></h2>

		  <h2><a href="#license">License</a></h2>

		  <hr/>
		  <h1><a name="setting_up_a_project">Setting up a project</a></h1>


		  <h2>Project groups</h2>
		  <p>The Project Group allows you to designated multiple projects as having the same group. Each group has
		  it's own page listing the projects associated with the group.</p>

		  <h2>Project key</h2>
		  <p>The project key is used to customize URLs for the project. It's also used when submitting test results
		  to Cuanto via the Cuanto Ant Task or the CuantoConnector API.</p>

		  <h2>Bug URL pattern</h2>
		  <p>The bug URL pattern is used for referencing an external bug tracking system. Use this field to describe the
		  URL scheme for the bug tracking system by using the text {BUG} to substitute for the bug number or bug
		  identifier in the bug tracking system's bug URLs.</p>

		  <p>For example, if your Bugzilla tracking system has URLs like http://bugzilla/show_bug.cgi?id=14587, you
		  would use a bug URL pattern of http://bugzilla/show_bug.cgi?id={BUG}. A JIRA bug tracking system might have
		  URLs like http://jira/browse/CUANTO-34, then the bug tracking pattern would be http://jira/browse/{BUG}.</p>

		  <p>There are a couple of advantages to having a bug URL pattern specified. Entering a bug number into the test
		  analysis table's bug column will automatically create a link to that bug. For example, using the JIRA bug
		  pattern above, entering CUANTO-15 in the bug column would make the text CUANTO-15 link to
		  http://jira/browse/CUANTO-15.  If you make an entry that <i>matches</i> the bug URL pattern, it will extract
		  the bug number in order to create a link. For example, entering http://jira/browse/CUANTO-11 in the bug column
		  would result in a bug number of CUANTO-11 linking to that URL.</p>

		  <h2>Manual test cases</h2>
		  <p>If this project is being created to store the results of manual (non-automated) tests, check this box.
		  Read the help section about <a href="#manual_test_case_mgmt">manual test case management for more information.</a></p>


		  <h1><a name="using_the_cuanto_ant_task">Using the Cuanto ant task</a></h1>
		  <!--<h2>Downloading the Cuanto Client Jar</h2>-->
		  <!--<p>You can download the Cuanto client jar for this version of Cuanto <a href="client/cuanto-api-2.2.0.jar">here</a>.</p>-->


		  <h2>Defining the task in an ant file</h2>
		  <p>With the Cuanto client jar and it's dependencies (both found in the distribution's api directory) in your
		  classpath, define the task in your ant file like this:</p>
		  <pre class="code">&lt;taskdef name="cuanto" classname="cuanto.api.CuantoAntTask"/&gt;</pre>


		  <h2>Submitting test results with the ant task</h2>
		  <p>After tests have executed and the XML test report files have been created, submit the reports to Cuanto
		    by using the Cuanto ant task:</p>
		  <pre class="code">&lt;cuanto url="http://cuantourl" testProject="CUANTO"&gt;
	&lt;fileset dir="src/test/resources/surefire-reports" includes="**/*.xml"/&gt;
	&lt;property name="build" value="823"/&gt;
	&lt;property name="milestone" value="1.0"/&gt;
	&lt;property name="environment" value="test lab"/&gt;
	&lt;link description="Test artifacts" url="http://my/url/link"/&gt;	  
&lt;/cuanto&gt;</pre>
		  <br/>
		  <p>Use the project key as the testProject value. &lt;property&gt; nodes can be used to specify arbitrary properties
		  for a Test Run. &lt;link&gt; nodes can be used to associate arbitrary URLs with a test run.</p>
		  <p>You can also specify a nested propertyset to the Cuanto ant task to refer to existing properties instead of
		  or along with individual nested properties:</p>
	  <pre class="code">&lt;propertyset id="mypropset"&gt;
	&lt;propertyref name="milestone"/&gt;
	&lt;propertyref name="build"/&gt;
&lt;/propertyset&gt;

 &lt;cuanto url="http://cuantourl" testProject="CUANTO"&gt;
	&lt;fileset dir="src/test/resources/surefire-reports" includes="**/*.xml"/&gt;
	&lt;propertyset refid='mypropset'/&gt;
	&lt;property name="environment" value="test lab"/&gt;
	&lt;link description="Test artifacts" url="http://my/url/link"/&gt;
&lt;/cuanto&gt;</pre>

		  <h1><a name="test_run_history">Test Run History</a></h1>
		  <p>The Test Run History page shows the history of test runs for a project. The feed icon links to an RSS feed
		     of the Test Run History so that you can subscribe to new results. Clicking on a Test Run row will take you
		  to the Test Run Analysis details for that Test Run.</p>


		  <h1><a name="test_run_results">Test Run Analysis</a></h1>

		  <h2>Filtering test results</h2>
		  <p>By default, the analysis view shows only tests that failed.</p>
		  <img src="${resource(dir:'images/help', file:'filter_results.png')}" alt="analysis filters screenshot"/>
		  <br/><br/>
		  
		  <p>You can choose a different filter for the view to display all tests, all failures, unanalyzed failures or just the new failures.
		  <em>New failures</em> are defined as tests that failed for this test run but passed the previous test run. Any test that failed in
		  the previous test run is not displayed in this view, allowing you to quickly see what new bugs may have
		  been introduced since the previous test run. Note that analyzing failures while in the <em>unanalyzed failures</em> view
		  can result in hard-to-predict paging behavior (missing tests while paging), as you are changing the size of the selected result set by analyzing
		  tests.</p>

		  <h2><a name="searching">Searching test results</a></h2>
		  <img src="${resource(dir:'images/help', file:'search.png')}" alt="search screenshot"/><br/><br/>
		  <p>The Name, Notes, Owner and Test Output fields are searchable. Query terms are case-insensitive and ANDed.  The search
		  results will be persisted across column sorts and paging until a blank search term is submitted.</p>

		  <h2>Test name formatting</h2>
		  <img src="${resource(dir:'images/help', file:'name_format.png')}" alt="name formatting screenshot"/><br/><br/>
		  <p>You can choose what format to use when displaying test names in the analysis table. Tests with long package
		  names tend to make the table much larger, so you may want to display only the test method name or another
		  smaller format. You can also set a default name format on a per-project basis by editing the project settings.
		  <em>Note: When sorting on the Test Name column, the sorting is always by the fully-qualified
		  package name of the test, regardless of the current display format.</em></p>

		  <h2>Analyzing test results</h2>

		  <p>The Test Run Analysis page is where you can make notes about a test and the reasons it has failed. The
		  <em>Result</em>, <em>Reason</em>, <em>Bug</em>, <em>Owner</em> and <em>Note</em> columns are all editable.
		  If you've correctly set up your Bug URL pattern, you can create a link to a bug just by entering the bug number.</p>

		  <p>The <em>Tools</em> column contains icons that link to three dialogs: <em>Test Case History</em>,
		  <em>Test Output</em>, and <em>Batch Analysis</em>.</p>

		  <h2>Test output</h2>
		  The test output dialog shows the output for a test.

		  <h2>Test case history</h2>
		  <p>The test case history icon
		  (<img src="${resource(dir:'images/tango/16x16', file:'folder-saved-search.png')}" alt="test case history icon"/>)
		  in the Tools column allows you to see a history table for a particular test case's analyses.</p>

		  <h2>Batch analysis</h2>
		  <p>Often a test run will have a number of failures due to a single bug, so a number of tests will have identical
		  analyses. Using the batch analysis tool allows you to easily apply one or more analysis fields to multiple
		  test cases. You can invoke the batch analysis tool by clicking on the batch analysis icon
		  (<img src="${resource(dir:'images/tango/16x16', file:'accessories-text-editor.png')}" alt="batch analysis icon"/>)
		  in the Tools column for the test case that you wish to use as the source analysis.</p>

		  <p><img src="${resource(dir:'images/help', file:'bulk_analysis.png')}" alt="bulk analysis screenshot"/></p>

		  <p>Select the checkboxes of the analysis fields that you wish to bulk-apply. In the analysis table, use the
		  Select column to select the target test cases to which you wish to apply the analysis. You can navigate between
		  pages of the table and the checked selections will be retained. Once you've selected all the test cases for appying,
		  click the "Apply to selected tests" button to complete the bulk analysis.</p>


		  <p>If you wish to apply an analysis for a <em>previous</em> test run to one or more test cases, locate the source
		 analysis in the Analysis History list on the right half of the analysis dialog. So, for example, if you know this test case failed yesterday
		  due to a bug and it failed today due to the same bug, you can quickly apply yesterday's analysis to today's result.</p>


		  <h1><a name="manual_test_case_mgmt">Manual Test Case Management</a></h1>
		  <h2>Entering test cases</h2>
		  <p>If you've designated a project as having manual test cases, you can specify the test cases by going to the
		  Test Cases tab and using the Add Test Case link. This will allow you to enter a test name, package name, and
		  description for a test case.</p> 

		  <h2>Creating a manual test run</h2>
		  <p>After test cases have been specified, you can create a test run or test pass of them by going to the
		  Test Run History page for the project and selecting "Create Manual Test Run". <em>Note: this option is only
		  available if the project has manual test cases enabled.</em> Creating a manual run allows you to manually
		  enter information about the test run.  After creating the
		  manual test run, you will see it show up in the project's test run history as having 0 tests. This is because
		  no test results have actually been entered. Proceed to the analysis page for the test run, and change the
		  view filter to "All Results." Now you can see all of the tests have a result of "Unexecuted".  You can
		  record the results of your manual test cases by changing the result to Pass, Fail, etc.</p>

          <h1><a name="using_cuanto_adapters">Using Cuanto Adapters</a></h1>

          <h2>TestNgListener</h2>

          <p>The <span class="mono">TestNgListener</span> is an <span class="mono">ITestListener</span>
              implementation for TestNG that submits test results to Cuanto on the fly.
              <span class="mono">TestNgListener</span> can either submit
              <span class="mono">TestOutcomes</span> to the specified <span class="mono">TestRun</span>
              or to a <span class="mono">TestRun</span> automatically created by it.
              It is also thread-safe in that concurrent test threads may submit results to
              different <span class="mono">Projects</span> or <span class="mono">TestRuns</span>.</p>
          <p>To use <span class="mono">TestNgListener</span>, define it as a TestNG listener.
              See the <a href="http://testng.org/doc/documentation-main.html#testng-listeners">TestNG documentation</a>
              for details. </p>
          <p><span class="mono">TestNgListener</span> may be configured using environment variables or programmatically.</p>
          <p>Using environment variables:</p>
          <pre class="code">
-Dcuanto.url=http://localhost:8080/cuanto
-Dcuanto.projectKey=CNG
-Dcuanto.testrun.create=true
// -Dcuanto.testrun=123
-Dcuanto.testrun.properties=milestone:1.0.0-SNAPSHOT,changelist:12345
-Dcuanto.testrun.links=specs:http://localhost/specs,test_plan:http://localhost/plans</pre>
          <p/>
          <p>Programmatically:</p>
          <pre class="code">
TestNgListenerArguments arguments = new TestNgListenerArguments();

arguments.setCuantoUrl(new URI("http://localhost:8080/cuanto"));
arguments.setProjectKey("CNG");
arguments.setCreateTestRun(true);
// arguments.setTestRunId(123L);

Map<String, String> testProperties = new HashMap<String, String>();
testProperties.put("milestone", "1.0.0-SNAPSHOT");
testProperties.put("changelist", "12345");
arguments.setTestProperties(testProperties);

Map<String, String> links = new HashMap<String, String>();
links.put("specs", "http://localhost/specs");
links.put("test_plan", "http://localhost/plans");
arguments.setLinks(links);

TestNgListener.setTestNgListenerArguments(arguments);</pre>
          <p/>
          <p>Note that <span class="mono">TestNgListener</span> keeps track of
              <span class="mono">TestNgListenerArguments</span> per thread. If a new test thread does not
              set its own <span class="mono">TestNgListenerArguments</span>,
              <span class="mono">TestNgListener</span> will use the configuration
              defined by the environment variables as failover configuration.</p>

		  <h1><a name="about">About Cuanto</a></h1>

		  <p>Cuanto was created by <a href="mailto:ttopwells@gmail.com">Todd Wells</a>,
		  with advice, bugs and feature suggestions contributed by a number of his colleagues at thePlatform.
		  Contributors include Suk-Hyun Cho.</p>

		  <h1><a name="license">Copyright and License</a></h1>

		  <p>Cuanto is Copyright (C) 2009-2010 by thePlatform, Inc. and Todd Wells.</p>

		  <p>Cuanto is made available under the terms of the GNU Lesser General Public License, version 3, also known
		  as <a href="http://www.gnu.org/licenses/lgpl.html">LGPLv3</a>.</p>

		  <img src="images/lgplv3-147x51.png" alt="LGPLv3"/>

		  <h1>Additional licenses used</h1>
		  <p><a href="js/yui/2.8.2r1/LICENSE">YUI License</a></p>

		  <p>Additional license information can be found in the <a href="http://github.com/ttop/cuanto">source 
		  distribution.</a></p>

	  </div>
  </body>
</html>
