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
package feedsplugin

import com.sun.syndication.feed.synd.*
import com.sun.syndication.io.SyndFeedOutput
import com.sun.syndication.feed.module.itunes.*

/**
 * This is a builder for creating ROME feed content using entry() and content() nodes.
 *
 * It can create a ROME SyndFeed object for a feed in a specific flavour, or it can render
 * such a feed to a String.
 * 
 * It supports setting any properties on the root node, and any properties on entry and content nodes.
 * There's a bit of smarts to handle different entry()/content() invocation such that it will "do the right thing"
 * for quick and dirty feeds:
 *
 *  - entry nodes can take a title parameter as a shortcut.
 *  - content nodes can take a string parameter which is used as the text/plain content body
 *  - entry node bodies can just return an object, the string value of which will be used as text/plain content for the entry
 *  - entry and content nodes can take a map as parameter, to set any properties of the node
 * 
 * Examples
 * ========
 *
 * Root node properties: 
 * 
 * def builder = new FeedBuilder()
 * builder.feed {
 *   title = "My title"
 *   link = "http://www.myblogsite.com"    
 * }
 * def feedA = builder.makeFeed('rss_2.0')
 *
 * def builder2 = new FeedBuilder()
 * builder2.feed(title: "My title", link: "http://www.myblogsite.com") {
 *   // nodes here     
 * }
 * def feedB = builder2.makeFeed('rss_2.0')
 * 
 * def builder3 = new FeedBuilder()
 * builder3.feed("My title") {
 *   link = "http://www.myblogsite.com"    
 * }
 * def feedC = builder2.makeFeed('rss_2.0')
 *
 * Entry nodes: 
 * 
 * entry {
 *     title = "Article 1"
 *     link = "http://somedomain.com/feed/1"
 *     publishDate = new Date()
 *     
 *     //content here
 * }
 * 
 * entry("Article 2") {
 *     link = "http://somedomain.com/feed/2"
 *     publishDate = new Date()
 *     //content here
 * }
 * 
 * entry(title:"Title here", link:"http://somedomain.com/feed") {
 *     publishDate = new Date()
 *     //content here
 * }
 *
 * Content nodes: 
 * 
 * content() {
 *    type = "text/html"
 *    '<p>Hello world</p>' // can use "return" also
 * }
 *
 * content('Hello world') 
 *
 * content(type:'text/html') {
 *    '<p>Hello world</p>' // can use "return" also
 * }
 * 
 * content(type:'text/html', value:'<p>Hello world</p>')
 * 
 * @author Marc Palmer (marc@anyware.co.uk)
 */
class FeedBuilder extends GroovyObjectSupport {

	static NODE_FEED = 'feed'
	static NODE_ENTRY = 'entry'
	static NODE_CONTENT = 'content'
	static NODE_ENCLOSURE = 'enclosure'
	
	static MODULE_ITUNES = 'iTunes'
	
	static NODE_MAPPINGS = [
	    (NODE_FEED):SyndFeedImpl, 
	    (NODE_ENTRY):SyndEntryImpl, 
	    (NODE_CONTENT):SyndContentImpl,
	    (NODE_ENCLOSURE):SyndEnclosureImpl
	]
	
	static TYPE_RSS = "rss"
	static TYPE_ATOM = "atom"
	
	static TYPES = [ TYPE_RSS, TYPE_ATOM ]
	static DEFAULT_VERSIONS = [ 
	    (TYPE_RSS):"2.0",
	    (TYPE_ATOM):"1.0"
	]
	
	static SUBNODES = [
		(NODE_FEED): [NODE_ENTRY] as HashSet,
		(NODE_ENTRY): [NODE_CONTENT, NODE_ENCLOSURE] as HashSet,
		(NODE_CONTENT): [],
		(NODE_ENCLOSURE): []
	]

	static MODULES = [
	    (MODULE_ITUNES): [
            	(NODE_FEED): EnhancediTunesFeedInformationImpl,
            	(NODE_ENTRY): EnhancediTunesEntryInformationImpl
	        ]
	]

	String feedType
	List entries
	def feedProperties
	def current
	def currentName
	def proxy = new FeedBuilderProxy(this)
	def typeStack = []
	def closureStack = []
	
	/**
	 * Evaluate a feed builder closure, taking a map of root node properties 
	 */
	void feed(Map attributes, Closure closure) {
		feed( closure)
		feedProperties.putAll(attributes)
	}

	/**
	 * Evaluate a feed builder closure
	 */
	void feed(Closure closure) {
		entries = []
		feedProperties = [:]
		current = feedProperties
		currentName = NODE_FEED
		typeStack.clear()
		typeStack << currentName 
		
		invokeClosure(closure)
	}

	private handleNode(name, args) {
	    def parentName = typeStack[-1]
        def moduleInfo = MODULES[name]

	    // Is this node type allowed here?
		if (!SUBNODES[parentName].contains(name) && !moduleInfo?.get(parentName) ) {
			throw new IllegalArgumentException("Cannot have [$name] here")
		}
		
		// Search for built in node types
		def type = NODE_MAPPINGS[name]
		if (!type) {
		    if (moduleInfo) {
		        type = moduleInfo[parentName] // get the class the module uses when under the current parent node
		    }
		    if (!type) {
			    throw new IllegalArgumentException("Node type [$name] is not supported by RSS Builder")
		    }
		}

		def previousCurrent = current  // save current so we can re-instate as we are re-entrant here
		current = type.newInstance()
		currentName = name
		typeStack << currentName
		def thisCurrent = current

		// Store the node in right place
		switch (thisCurrent.class) {
			case SyndEntry: 
				entries << thisCurrent
				break;
			case SyndContent:
				addContent(entries[-1], thisCurrent)
				break;
			case SyndEnclosure:
				addEnclosure(entries[-1], thisCurrent)
				break;
    		default:
    		    // If not a known node, it is a module
    		    if (moduleInfo) {
        		    // assume its a module
        		    if (!previousCurrent.modules) {
        		        previousCurrent.modules = []
        		    }
    		        previousCurrent.modules << thisCurrent
		        }
    		    break;
		}

		def closureResult
	
		if (args.size() == 0) {
			// do nothing
		}
		else if (args.size() == 1) { // Accept (Closure) or (value)
			if (args[0] instanceof Closure) {
				closureResult = invokeClosure(args[0])
			} else if (args[0] instanceof Map) {
				mapToProperties( args[0], thisCurrent)
			} else {
				setValueOnNode(thisCurrent, args[0])
			}
		}
		else if (args.size() == 2) { // Accept (Map, Closure) or (value, Closure) 
			if (args[0] instanceof Map) {
				if (args[1] instanceof Closure) {
					mapToProperties( args[0], thisCurrent)
					closureResult = invokeClosure(args[1])
				} else {
					throw new IllegalArgumentException( "[${name}] can accept (Map, Closure) or (Object, Closure) only")
				}
			} else { 
				if (args[1] instanceof Closure) {
					setValueOnNode(thisCurrent, args[0])
					closureResult = invokeClosure(args[1])
				} else {
					throw new IllegalArgumentException( "[${name}] can accept (Map, Closure) or (Object, Closure) only")
				}
			}
		}
		
		// Handle simple results from closures that create implicit nodes
		if (closureResult && (closureResult instanceof String)) {
			switch (name) {
				case 'entry': 
					addContent(thisCurrent, new SyndContentImpl(type:'text/plain', value:closureResult))
					break;
				case 'content': 
					if (thisCurrent.value == null) {
						thisCurrent.value = closureResult
					}
					break;
			}
		}

		typeStack.pop()
		current = previousCurrent // do this so that we can be re-entrant and not lose "current" parent 
		return thisCurrent
	}

	private void addContent(entry, content) {
		if (entry.contents == null) {
			entry.contents = []
		}
		entry.contents << content
	}


	private void addEnclosure(entry, enclosure) {
		if (entry.enclosures == null) {
			entry.enclosures = []
		}
		entry.enclosures << enclosure
	}
	
	private setValueOnNode(node, value) {
		// ...and set something sensible to value, depending on node / or trash this and throw?
		switch (node.class) {
			case SyndFeed: 
				node.title = value.toString()
				break
			case SyndEntry: 
				node.title = value.toString()
				break
			case SyndContent: 
				node.type = 'text/plain'
				node.value = value
				break
			default:
			    throw new IllegalArgumentException("You cannot set this node to a single value") 
			    break;
		}	
	}

	private invokeClosure(Closure closure) {
	    if (closureStack.size() == 0) {
		    closure.delegate = proxy
	    } else {
	        closure.delegate = closureStack[closureStack.size()-1]
	    }
	    // Push "this" as delegate for nested closures
	    closureStack << closure
		def result = closure.call(current)
		// Pop "this" delegate
		closureStack.pop()
		return result
	}

	/**
	 * Generate a ROME API SyndFeed object representing the feed for the specified
	 * feed type (e.g. "rss") and version eg. "2.0"
	 */
	SyndFeed makeFeed(type, version = null) {
        if (!TYPES.contains(type)) {
            throw new IllegalArgumentException("Unknown feed type [$type]")
        }
        
		def feed = new SyndFeedImpl()
		mapToProperties(feedProperties, feed)
		if (!version) {
		    version = DEFAULT_VERSIONS[type]
		}
		feed.feedType = "${type}_${version}"
		feed.entries = entries
		return feed
	}
	
	private void mapToProperties( Map src, dest) {
		src.each() { k, v ->
			dest[k] = v
		}	
	}
	
	/**
	 * Render the current state of the builder to a string feed, using the specified type
	 */
	def render(type, version = null) {
		def feed = makeFeed(type, version)

	    StringWriter writer = new StringWriter()
	    SyndFeedOutput output = new SyndFeedOutput()
	    output.output(feed,writer)
	    writer.close()
	
		return writer.toString()
	}
}

class FeedBuilderProxy extends GroovyObjectSupport {
	private builder
	
	FeedBuilderProxy( FeedBuilder builder) {
		this.@builder = builder
	}

    // The builder will handle method invocations from within the node closures
	Object invokeMethod(String name, Object args) {
		return this.@builder.handleNode(name, args)
	}
	
	// Handle property setting within closures such that it sets properties of the current builder node
	void setProperty(String property, Object newValue) {
		if (this.@builder.current != null) {
			try {
				this.@builder.current."$property" = newValue
				return
			} catch (Exception e) {
			    e.printStackTrace()
			}
		}
		super.setProperty(property, newValue)
	}	
	
	// Handle property getting withing closures such that it gets properties of the current builder node
	def getProperty(String property) {
		if (this.@builder.current != null) {
			try {
				return this.@builder.current."$property"
			} catch (Exception e) {
				// Ouch this is expensive, don't rely on it for functionality
			}
		}
		return super.getProperty(property)
	}

}