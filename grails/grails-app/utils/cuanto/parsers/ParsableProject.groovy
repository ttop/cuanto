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

package cuanto.parsers

/**
 * A Cuanto project - corresponds to a project which contains test results.
 * <p>A project has a <b>name</b> and a <b>projectGroup</b>. ProjectGroups are a string that represents an arbitrary group of projects,
 * so that two projects that both have the projectGroup "FictionWare" will be logically grouped together in the "FictionWare" group.</p>
 * <p>A <b>projectKey</b> is string to serve as the "key" or shorthand string for this project. It must be unique for this
 * Cuanto server.</p>
 * <p>The <b>testType</b> is the type of test that is included in this project -- typically "JUnit" or "TestNG". It
 * needs to correspond to a TestType the Cuanto server understands.</p>
 * <p>The <b>bugUrlPattern</b> is a pattern that represents the pattern in which bug numbers or bug identifiers are mapped to a
 * bug tracking system's URL, where {BUG} represents where the bug identifier, for example <code>http://jira.fictionware.org/browse/{BUG}</code>.
 * This is for automatically linking bug numbers to a bug tracking system for a project.</p>
 */
public class ParsableProject {
    String name
    String projectGroup
    String projectKey
    String bugUrlPattern
    String testType
    Long id
	Integer purgeDays
}