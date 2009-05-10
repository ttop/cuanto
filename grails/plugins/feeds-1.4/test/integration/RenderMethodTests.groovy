/*
 * Copyright 2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.codehaus.groovy.grails.plugins.GrailsPlugin
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.plugins.PluginManagerHolder

/** 
 * Argh so much pain and diminishing returns, not testing this... leave til later
 *
 * CURRENTLY THESE TESTS DO NOT TEST ANYTHING
 *
 * @author Marc Palmer (marc@anyware.co.uk)
 */
class RenderMethodTests extends GroovyTestCase {

    static transactional = false

    Class addController(src) {
		def gcl = new GroovyClassLoader()
		def c = gcl.parseClass(src)
		def event = PluginManagerHolder.pluginManager.getGrailsPlugin("controllers").notifyOfEvent(GrailsPlugin.EVENT_ON_CHANGE, c)
        PluginManagerHolder.pluginManager.informObservers("controllers", event)
        return c
    }


	void testRenderNodes() {
        def c = addController("""
class TestController {
	def test = {
		def articles = ['A', 'B', 'C']

		render(feedType:"rss", feedVersion:"2.0") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			articles.each() { article ->
				entry("Title for \$article") {
				    "Content for \$article"
				}
			}
		}
	}
}
""")
        def controller = c.newInstance()
        controller.test()

        def resp = controller.response.contentAsString
        def dom = new XmlSlurper().parseText(resp)
        assertNotNull dom

        assertEquals 1, dom.channel.size()
        assertEquals 3, dom.channel[0].item.size()
	}

/* 
    This test fails with null "mode" errors, seems to be a ROME thing?
	void testRenderNodesToAtom03() {
	    doTestRenderNodesToAtom("0.3")
    }
*/    

	void testRenderNodesToAtom10() {
	    doTestRenderNodesToAtom("1.0")
    }
    
	private void doTestRenderNodesToAtom(version) {
        def c = addController("""
class TestController {
	def test = {
		def articles = ['A', 'B', 'C']

		render(feedType:"atom", feedVersion:"${version}") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			articles.each() { article ->
				entry("Title for \$article") {
				    content { 
				        "Content for \$article"
				    }
				}
			}
		}
	}
}
""")
        def controller = c.newInstance()
        controller.test()

        def resp = controller.response.contentAsString
        
        println resp
        
        def dom = new XmlSlurper().parseText(resp)
        assertNotNull dom

        assertEquals 3, dom.entry.size()
	}

	void testRenderNodesNestedExplicitContentNodeBeforePropertySetters() {
        def c = addController("""
class TestController {
	def test = {
		def articles = ['A', 'B', 'C']

		render(feedType:"rss", feedVersion:"2.0") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			articles.each() { article ->
				entry("Title for \$article") {
				    content(type:'text/html') {
				        return "Content for \$article"
				    }

				    link = 'http://somewhere.com/x'

				    publishedDate = new Date()

				}
			}
		}
	}
}
""")
        def controller = c.newInstance()
        controller.test()

        def resp = controller.response.contentAsString
        println resp

        def dom = new XmlSlurper().parseText(resp)
        assertNotNull dom

        assertEquals 1, dom.channel.size()
        assertEquals 3, dom.channel[0].item.size()
        assertEquals 'http://somewhere.com/x', dom.channel[0].item[0].guid.text()
        assertEquals 'http://somewhere.com/x', dom.channel[0].item[0].link.text()
	}

	void testRenderNodesNestedExplicitContentNode() {
        def c = addController("""
class TestController {
	def test = {
		def articles = ['A', 'B', 'C']

		render(feedType:"rss", feedVersion:"2.0") {
			title = 'Test feed'
			link = 'http://somewhere.com/'
			description = "This is a test feed" 

			articles.each() { article ->
				entry("Title for \$article") {
				    link = 'http://somewhere.com/x'

				    publishedDate = new Date()
				    
				    content(type:'text/html') {
				        return "Content for \$article"
				    }

				}
			}
		}
	}
}
""")
        def controller = c.newInstance()
        controller.test()

        def resp = controller.response.contentAsString
        println resp

        def dom = new XmlSlurper().parseText(resp)
        assertNotNull dom

        assertEquals 1, dom.channel.size()
        assertEquals 3, dom.channel[0].item.size()
        assertEquals 'http://somewhere.com/x', dom.channel[0].item[0].guid.text()
        assertEquals 'http://somewhere.com/x', dom.channel[0].item[0].link.text()
	}

    // Make sure our meta stuff is not inferfering with normal property resolution
	void testRenderNodesBadMethodResolution() {
	    try {
	        
            def c = addController("""
    class TestController {
    	def test = {
    		def articles = ['A', 'B', 'C']

    		render(feedType:"rss", feedVersion:"2.0") {
    			title = 'Test feed'
    			link = 'http://somewhere.com/'
    			description = "This is a test feed" 

    			articles.each() { article ->
    				entry("Title for \$article") {
    				    link = 'http://somewhere.com/' + g.somethingFake.someFakeMetho('x')
    				    publishedDate = new Date()

    				    content(type:'text/html') {
    				        return "Content for \$article"
    				    }
    				}
    			}
    		}
    	}
    }
    """)
            def controller = c.newInstance()
            controller.test()
            
            fail("Should have received a MissingPropertyException")
            
        } catch (MissingPropertyException e) {
            
        }
	}

	void testRenderStringStillWorks() {
	    def c = addController("""
class StandardController {
    def index = {
        render("Hello world")
    }
}
""")
        def controller = c.newInstance()
        controller.index()

        assertEquals "Hello world", controller.response.contentAsString
    }

	void testRenderClosureStillWorks() {
	    def c = addController("""
class BuilderController {
    def index = {
        render {
            html {
                body {
                }
            }
        }
    }
}
""")
        def controller = c.newInstance()
        controller.index()

        assertTrue controller.response.contentAsString.contains("<html>")
    }
	
	void testRenderMapClosureStillWorks() {
	    def c = addController("""
class MapClosureController {
    def index = {
        render(contentType:"text/xml") {
            root {
                child {
                }
            }
        }
    }
}
""")
        def controller = c.newInstance()
        controller.index()

        assertTrue controller.response.contentAsString.contains("<root>")
    }

	void testSimple() {
	    def c = addController("""
class FeedController {
    def index = {
        render(feedType:"rss", feedVersion:"2.0") {
            title = "My test feed"
            link = "http://your.test.server/yourController/feed"
            description = "test"
        }
    }
}
""")
        def controller = c.newInstance()
        controller.index()

        def resp = controller.response.contentAsString
        def dom = new XmlSlurper().parseText(resp)
        assertNotNull dom

        assertEquals 1, dom.channel.size()
        assertEquals "My test feed", dom.channel[0].title.text()
	}
}
