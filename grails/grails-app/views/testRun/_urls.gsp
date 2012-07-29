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

%{-- cuanto/url.js must be included for this file to work--}%

YAHOO.cuanto.urls.set('home', "${createLink(controller: 'project', action:'list')}");
YAHOO.cuanto.urls.set('testRunOutcomes', "${createLink(controller: 'testRun', action: 'outcomes')}/${testRun?.id}?");
YAHOO.cuanto.urls.set('projectHistory', "${createLink(controller: 'project', action:'history')}/" + ${project?.id} + "?");
YAHOO.cuanto.urls.set('groupHistory', "${createLink(controller: 'project', action:'groupHistory')}/" + "${group}");
YAHOO.cuanto.urls.set('groupNames', "${createLink(controller: 'project', action:'groupNames')}/");
YAHOO.cuanto.urls.set('groupTable', "${createLink(controller: 'project', action:'listGroupTable')}/");
YAHOO.cuanto.urls.set('historyImg', "${resource(dir: 'images/tango/32x32', file: 'system-search.png')}");
YAHOO.cuanto.urls.set('outputImg', "${resource(dir: 'images/tango/16x16', file: 'format-justify-left.png')}");
YAHOO.cuanto.urls.set('analysisImg', "${resource(dir: 'images/tango/16x16', file: 'accessories-text-editor.png')}");
YAHOO.cuanto.urls.set('progressImg', "${resource(dir: 'images/progress', file: 'mozilla_blu.gif')}");
YAHOO.cuanto.urls.set('shortcutImg', "${resource(dir: 'images/tango/16x16', file: 'shortcut.png')}");
YAHOO.cuanto.urls.set('alertImg', "${resource(dir: 'images/splashyicons/16x16', file: 'alert.png')}");
YAHOO.cuanto.urls.set('bugSummary', "${createLink(controller:'testRun', action: 'bugSummary')}/${testRun?.id}");
YAHOO.cuanto.urls.set('failureChart', "${createLink(controller:'testRun', action: 'failureChart')}/${testRun?.id}");
YAHOO.cuanto.urls.set('summaryTable', "${createLink(controller:'testRun', action: 'summaryTable')}/${testRun?.id}");
YAHOO.cuanto.urls.set('testRunResults', "${createLink(controller: 'testRun', action:'results')}/");
YAHOO.cuanto.urls.set('testRunLatest', "${createLink(controller: 'testRun', action:'latest')}/");
YAHOO.cuanto.urls.set('testRunUpdate', "${createLink(controller: 'testRun', action:'update')}/");
YAHOO.cuanto.urls.set('testRunUpdateNote', "${createLink(controller: 'testRun', action:'updateNote')}/");
YAHOO.cuanto.urls.set('testRunDelete', "${createLink(controller: 'testRun', action:'delete')}/");
YAHOO.cuanto.urls.set('testRunBulkDelete', "${createLink(controller: 'testRun', action:'bulkDelete')}/");
YAHOO.cuanto.urls.set('testRunRecalcStats', "${createLink(controller: 'testRun', action:'recalc')}/");
YAHOO.cuanto.urls.set('singleOutcome', "${createLink(controller: 'testOutcome', action: 'get')}/");
YAHOO.cuanto.urls.set('saveOutcome',"${createLink(controller: 'testOutcome', action: 'saveDetails')}/");
YAHOO.cuanto.urls.set('testCaseHistory', "${createLink(controller: 'testCase', action: 'history')}/");
YAHOO.cuanto.urls.set('testCaseList', "${createLink(controller: 'testCase', action:'list')}/");
YAHOO.cuanto.urls.set('testCaseEdit', "${createLink(controller: 'testCase', action:'edit')}/");
YAHOO.cuanto.urls.set('testCaseDelete', "${createLink(controller: 'testCase', action:'confirmDelete')}/");
YAHOO.cuanto.urls.set('testCaseUpdate', "${createLink(controller: 'testCase', action:'update')}/");
YAHOO.cuanto.urls.set('renamePreview', "${createLink(controller: 'testCase', action:'renamePreview')}?");
YAHOO.cuanto.urls.set('rename', "${createLink(controller: 'testCase', action:'doRename')}?");
YAHOO.cuanto.urls.set('testOutputDataSource', "${createLink(controller: 'testOutcome', action:'outputData')}/");
YAHOO.cuanto.urls.set('analysis', "${createLink(controller: 'testCase', action:'analysis')}/");
YAHOO.cuanto.urls.set('applyAnalysis', "${createLink(controller: 'testOutcome', action:'applyAnalysis')}/");
YAHOO.cuanto.urls.set('masonProjList', "${createLink(controller: 'project', action:'masonProj')}/");
YAHOO.cuanto.urls.set('mason', "${createLink(controller: 'project', action:'mason')}/");
YAHOO.cuanto.urls.set('projectInfo', "${createLink(controller: 'project', action:'get')}/");
YAHOO.cuanto.urls.set('projectHeader', "${createLink(controller: 'project', action:'projectHeader')}/");
YAHOO.cuanto.urls.set('propertyDelete', "${createLink(controller: 'testRun', action:'deleteProperty')}/");
YAHOO.cuanto.urls.set('linkDelete', "${createLink(controller: 'testRun', action:'deleteLink')}/");
YAHOO.cuanto.urls.set('groupedOutput', "${createLink(controller: 'testRun', action:'groupedOutput')}/${testRun?.id}?");


