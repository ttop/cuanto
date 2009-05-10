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

<title>Cuanto: History of Test Case ${testCase?.id?.encodeAsHTML()}: ${testCase?.fullName?.encodeAsHTML()}</title>

<p:css name='analysis'/>
<p:css name='../js/yui/2.6.0/datatable/assets/skins/sam/datatable'/>
<p:css name='../js/yui/2.6.0/paginator/assets/skins/sam/paginator'/>

<yui:javascript dir="datasource" file="datasource-min.js" version="2.6.0"/>
<yui:javascript dir="datatable" file="datatable-min.js" version="2.6.0"/>
<yui:javascript dir="paginator" file="paginator-min.js" version="2.6.0"/>

<yui:javascript dir="get" file="get-min.js" version="2.6.0"/>
<g:javascript src="cuanto/events.js"/>
<g:javascript src="cuanto/formatBug.js"/>
<g:javascript src="cuanto/url.js"/>
<g:javascript src="cuanto/tableHelper.js"/>
<g:javascript src="cuanto/tcHistory.js"/>