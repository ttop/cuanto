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
<p:css name='../js/yui/2.8.2r1/datatable/assets/skins/sam/datatable'/>
<p:css name='../js/yui/2.8.2r1/paginator/assets/skins/sam/paginator'/>

<yui:javascript dir="datasource" file="datasource-min.js" version="2.8.2r1"/>
<yui:javascript dir="datatable" file="datatable-min.js" version="2.8.2r1"/>
<yui:javascript dir="paginator" file="paginator-min.js" version="2.8.2r1"/>

<yui:javascript dir="get" file="get-min.js" version="2.8.2r1"/>
<p:javascript src="cuanto/events"/>
<p:javascript src="cuanto/formatBug"/>
<p:javascript src="cuanto/url"/>
<p:javascript src="cuanto/tableHelper"/>
<p:javascript src="cuanto/timeParser"/>
<p:javascript src="cuanto/tcHistory"/>