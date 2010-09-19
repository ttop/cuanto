%{--

 Copyright (c) 2010 thePlatform, Inc.

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
<g:if test="${testRun.tags}">
  <div id="tagDiv">
    <div id="tagLabel" class="inlineb">Tags:</div>

    <div id="tagButtons" class="inlineb">

      <span id="tagspan-0" class="tagspan yui-button yui-checkbox-button">
        <span class="first-child">
          <button type="button" name="tagbtn-0">Tagged</button>
        </span>
      </span>

      <span id="tagspan-1" class="tagspan yui-button yui-checkbox-button">
        <span class="first-child">
          <button type="button" name="tagbtn-1">Untagged</button>
        </span>
      </span>

      <g:each var="tag" in="${testRun.tags?.collect{it.name}.sort()}" status="tIdx">
        <span id="tagspan-${tIdx + 2}" class="tagspan yui-button yui-checkbox-button">
          <span class="first-child">
            <button type="button" name="tagbtn-${tIdx + 2}">${tag}</button>
          </span>
        </span>
        <g:if test="${(tIdx + 2) % 9 == 0}">
          <br/>
        </g:if>
      </g:each>
    </div>
  </div>
  <br/>
</g:if>
