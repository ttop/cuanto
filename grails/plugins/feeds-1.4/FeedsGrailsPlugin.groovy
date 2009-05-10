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

import grails.util.GrailsUtil

import com.sun.syndication.io.SyndFeedOutput
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import feedsplugin.FeedBuilder

/**
 * 
 * A plugin that renders RSS/Atom feeds, or any other formats supported by the ROME API.
 * 
 * It works like this - you call render() method from your controller action as normal, but instead
 * of using a view you specify a feedType parameter and a feedVersion parameter, as well as a closure
 * that uses a custom feed builder to define the feed. Currently feed types "rss" and "atom" are supported.
 * 
 * Example:
 * 
 * class YourController {
 *     def feed = {
 *         render(feedType:"rss", feedVersion:"2.0") {
 *             title = "My test feed"
 *             link = "http://your.test.server/yourController/feed"
 *             
 *             Article.list().each() {
 *                 entry(it.title) {
 *                     link = "http://your.test.server/article/${it.id}"
 *                     it.content // return the content
 *                 }
 *             }
 *         }
 *     }
 * }
 * 
 * Some of the feed formats have different required properties that you must set on the feed (AKA Channel)
 * and the child nodes (entry and content). Exceptions will occur if you don't meet these constraints.
 * 
 * The feed builder is very forgiving. The general pattern is:
 * 
 * 1. set feed top-level properties i.e. link and title
 * 2. define entry nodes and properties of them
 * 3. define content for entry nodes, and properties of content
 * 
 * There are some smarts, namely:
 * 
 * * entry nodes can take a title parameter as a shortcut.
 * * content nodes can take a string parameter which is used as the text/plain content body
 * * entry node bodies can just return an object, the string value of which will be used as text/plain content for the entry
 * * entry and content nodes can take a map as parameter, to set any properties of the node
 * 
 * Common properties of entry nodes you may want to set:
 * 
 * publishedDate - the date the entry was published
 * categories - the list of categories
 * author - author name
 * link - link to the online entry
 * 
 * Common properties of content nodes you may want to set:
 * 
 * type - mime type
 * 
 * Enclosures and descriptions are currently not directly supported by the builder but can be constructed
 * using the ROME API directly.
 * 
 * @author Marc Palmer (marc@anyware.co.uk)
 */
class FeedsGrailsPlugin {
	def version = "1.4"
	def dependsOn = [controllers:GrailsUtil.grailsVersion]
    def loadAfter = ['controllers']
    def observe = ['controllers']
	def watchedResources = ["file:./grails-app/controllers/**/*Controller.groovy",
							"file:./plugins/*/grails-app/controllers/**/*Controller.groovy"]
							
    def author = "Marc Palmer"
    def authorEmail = "marc@anyware.co.uk"
    def title = "Render RSS/Atom feeds with a simple builder"
    def description = '''
		This plugin adds a feedType and feedVersion parameters to the render method of controllers, which if passed
		a valid feed type such as "rss" or "atom" and a version will expect a closure to be passed with render,
		which will render a feed using a custom builder. The FeedBuilder used for this accepts entry and content nodes, any properties
		of which can be set within the builder. These are beans from the ROME API so all properties there
		should work.
	'''
	def documentation = "http://grails.org/Feeds+Plugin"	
	
	static MIME_TYPES = [
		atom:'application/atom+xml',
		rss:'application/rss+xml'
	]
	
	def doWithSpring = {
	}   
	def doWithApplicationContext = { applicationContext ->
	}
	def doWithWebDescriptor = { xml ->
	}	                                      
	def doWithDynamicMethods = { ctx ->
		application.controllerClasses.each() { controllerClass ->
            replaceRenderMethod(controllerClass)
		}
	}	
	def onChange = { event ->
        if(application.isArtefactOfType(ControllerArtefactHandler.TYPE, event.source)) {
			def clz = application.getControllerClass(event.source?.name)
			replaceRenderMethod(clz)
		}
	}
	                                                                                  
	def onApplicationChange = { event ->
	}
	
	void replaceRenderMethod(controllerClass) {
		def oldRender = controllerClass.metaClass.pickMethod("render", [Map, Closure] as Class[])
		controllerClass.metaClass.render = { Map params, Closure closure ->
			if (params.feedType) {
				// Here we should assert feed type is supported
				def builder = new FeedBuilder()
				builder.feed(closure)

				def type = params.feedType
				def version = params.feedVersion
				def mimeType = params.contentType ? params.contentType : MIME_TYPES[type]
				if (!mimeType) {
					throw new IllegalArgumentException("No mime type known for feed type [${type}]")
				}
				response.contentType = mimeType
				response.characterEncoding = "UTF-8"

		        SyndFeedOutput output = new SyndFeedOutput()
		        output.output(builder.makeFeed(type, version),response.writer)
			} else {
			    // Defer to original render method
				oldRender.invoke(delegate, [params, closure])
			}
		}
	}
}
