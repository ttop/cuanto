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
 
import feedsplugin.FeedBuilder
import com.sun.syndication.feed.module.itunes.types.*

/**
 * @author Marc Palmer (marc@anyware.co.uk)
 */
class FeedBuilderTests extends GroovyTestCase {

	void testBuilderRootNodeWithAttribs() {
		def builder = new FeedBuilder()
		
		builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
			entry {
				"Hello world"
			}
		}
		
		def feed = builder.makeFeed("rss","2.0")
		assertEquals "Test feed", feed.title
		assertEquals "http://somewhere.com/", feed.link
	}
	
	void testBuilderRootNodeWithAttribsAndPropertySetting() {
		def builder = new FeedBuilder()
		builder.feed(title:'Test feed') {
			link = 'http://somewhere.com/'
			entry {
				"Hello world"
			}
		}
		
		def feed = builder.makeFeed("rss","2.0")
		assertEquals "Test feed", feed.title
		assertEquals "http://somewhere.com/", feed.link
	}

	void testBuilderEntries() {
		def builder = new FeedBuilder()
	
		def pubDate = new Date()
		
		builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
			entry('entry one') {
				"Hello"
			}
			entry(title:'entry two',link:'http://somewhere.com/entry2') {
				"Hello"
			}
			entry([title:'entry three',link:'http://somewhere.com/entry3']) {
				"Hello"
			} 
			entry {
				"Hello"
			}
			entry(title:'entry five', publishedDate: pubDate) {
				link = 'http://somewhere.com/entry5'
				"Hello"
			}
		}

		def feed = builder.makeFeed("rss","2.0")

		assertEquals "entry one", feed.entries[0].title
		assertEquals "Hello", feed.entries[0].contents[0].value
		
		assertEquals "entry two", feed.entries[1].title
		assertEquals 'http://somewhere.com/entry2', feed.entries[1].link
		assertEquals "Hello", feed.entries[1].contents[0].value

		assertEquals "entry three", feed.entries[2].title
		assertEquals 'http://somewhere.com/entry3', feed.entries[2].link
		assertEquals "Hello", feed.entries[2].contents[0].value

		assertEquals "Hello", feed.entries[3].contents[0].value

		assertEquals "Hello", feed.entries[4].contents[0].value
		assertEquals pubDate, feed.entries[4].publishedDate
		assertEquals 'http://somewhere.com/entry5', feed.entries[4].link
	}

	void testBuilderEnclosures() {
		def builder = new FeedBuilder()
	
		builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
			entry {
				title = "entry one"
				link = "http://somewhere.com/podcast/"
				content {
				    "Hello"
				}
				enclosure(type:'audio/mp3', url:'http://somewhere.com/podcast/episode1.mp3', length:99999)
				enclosure(type:'audio/m4a', url:'http://somewhere.com/podcast/episode1b.mp3', length:1234567)
			}
		}

		def feed = builder.makeFeed("rss","2.0")

		assertEquals "entry one", feed.entries[0].title
		assertEquals "Hello", feed.entries[0].contents[0].value
		assertEquals "http://somewhere.com/podcast/", feed.entries[0].link
		
		assertEquals "audio/mp3", feed.entries[0].enclosures[0].type
		assertEquals "http://somewhere.com/podcast/episode1.mp3", feed.entries[0].enclosures[0].url
		assertEquals 99999, feed.entries[0].enclosures[0].length
		
		assertEquals "audio/m4a", feed.entries[0].enclosures[1].type
		assertEquals "http://somewhere.com/podcast/episode1b.mp3", feed.entries[0].enclosures[1].url
		assertEquals 1234567, feed.entries[0].enclosures[1].length
		
	}

	void testBuilderiTunesTags() {
		def builder = new FeedBuilder()
	
		builder.feed( title: 'Test podcast', link:'http://somewhere.com/podcast') {
			iTunes {
			    author = "Marc Palmer"
			    keywords = ["music", "rock"]
			    categories = [new Category(name:'cat', subcategory: new Subcategory('subcat'))]
			}
			
			entry {
				title = "episode 1"
				link = "http://somewhere.com/podcast/1"
				content {
				    "Hello"
				}
				enclosure(type:'audio/mp3', url:'http://somewhere.com/podcast/episode1.mp3', length:99999)
				iTunes {
				    summary = "Angel of Death"
				    author = "Slayer"
				    duration = new Duration("0:59")
				    keywords = ["rock", "death", "metal"]
				}
			}
		}

		def feed = builder.makeFeed("rss","2.0")

		assertEquals "episode 1", feed.entries[0].title
		assertEquals "Hello", feed.entries[0].contents[0].value
		assertEquals "http://somewhere.com/podcast/1", feed.entries[0].link
		
		assertEquals "audio/mp3", feed.entries[0].enclosures[0].type
		assertEquals "http://somewhere.com/podcast/episode1.mp3", feed.entries[0].enclosures[0].url
		assertEquals 99999, feed.entries[0].enclosures[0].length
		
		// itunes channel bits
		def module = feed.getModule("http://www.itunes.com/dtds/podcast-1.0.dtd")
		assertEquals "Marc Palmer", module.author
		assert ["music", "rock"] == module.keywords
		assertEquals "cat", module.categories[0].name
		assertEquals "subcat", module.categories[0].subcategory.name
	
		// itunes item bits
		module = feed.entries[0].getModule("http://www.itunes.com/dtds/podcast-1.0.dtd")
		assertEquals "Angel of Death", module.summary
		assertEquals "Slayer", module.author
		assertEquals 59000, module.duration.milliseconds
		assert ["rock", "death", "metal"] == module.keywords
		
	}
	
	void testBuilderiTunesTagsEnhancedUsage() {
		def builder = new FeedBuilder()
	
		builder.feed( title: 'Test podcast', link:'http://somewhere.com/podcast') {
			iTunes {
			    author = "Marc Palmer"
			    keywords = ["music", "rock"]
			    categories = [ ['cat', 'subcat'], 'test' ] 
			}
			
			entry {
				title = "episode 1"
				link = "http://somewhere.com/podcast/1"
				content {
				    "Hello"
				}
				iTunes {
				    durationText = "0:59"
				}
			}
		}

		def feed = builder.makeFeed("rss","2.0")

		// itunes channel bits
		def module = feed.getModule("http://www.itunes.com/dtds/podcast-1.0.dtd")
		assertEquals "Marc Palmer", module.author
		assert ["music", "rock"] == module.keywords
		assertEquals "cat", module.categories[0].name
		assertEquals "subcat", module.categories[0].subcategory.name
		assertEquals "test", module.categories[1].name
	
		// itunes item bits
		module = feed.entries[0].getModule("http://www.itunes.com/dtds/podcast-1.0.dtd")
		assertEquals 59000, module.duration.milliseconds
		
	}
	
	void testBuilderContent() {
		def builder = new FeedBuilder()
	
		builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
			description = "This is a test feed" 
			
			entry {
				"One"
			}
			entry {
				content("Two")
			}
			entry {
				content([type:'text/html']) {
					"<p>Three</p>"
				}
			}
			entry {
				content(type:'text/html') {
					"<p>Four</p>"
				}
			}
			entry {
				content {
					type = 'text/html'
					return "<p>Five</p>"
				}
			}
			entry {
				content {
					type = 'text/html'
					return "<p>Six A</p>"
				}
				content {
					type = 'text/html'
					return "<p>Six B</p>"
				}
			}
		}

		def feed = builder.makeFeed("rss","2.0")

		assertEquals "One", feed.entries[0].contents[0].value
		assertEquals "text/plain", feed.entries[0].contents[0].type

		assertEquals "Two", feed.entries[1].contents[0].value
		assertEquals "text/plain", feed.entries[1].contents[0].type

		assertEquals "<p>Three</p>", feed.entries[2].contents[0].value
		assertEquals "text/html", feed.entries[2].contents[0].type

		assertEquals "<p>Four</p>", feed.entries[3].contents[0].value
		assertEquals "text/html", feed.entries[3].contents[0].type
		
		assertEquals "<p>Five</p>", feed.entries[4].contents[0].value
		assertEquals "text/html", feed.entries[4].contents[0].type

		assertEquals "<p>Six A</p>", feed.entries[5].contents[0].value
		assertEquals "text/html", feed.entries[5].contents[0].type
		assertEquals "<p>Six B</p>", feed.entries[5].contents[1].value
		assertEquals "text/html", feed.entries[5].contents[1].type
	}

	void testBuilderValidation() {
		def builder = new FeedBuilder()

		shouldFail {
			builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
				entry {
					entry {
						"Invalid"
					}
				}
			}
		}

		// We can't assert that this is an error, as the last expression in a closure would 
		// always break it
/*
		builder = new FeedBuilder()
		shouldFail {
			builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
				"Invalid"
			}
		}
*/
		builder = new FeedBuilder()
		shouldFail {
			builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
				content("Invalid")
			}
		}

		builder = new FeedBuilder()
		shouldFail {
			builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
				entry {
					content {
						content()
					}
				}
			}
		}

		builder = new FeedBuilder()
		shouldFail {
			builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
				entry {
					content {
						entry()
					}
				}
			}
		}

		builder = new FeedBuilder()
		shouldFail {
			builder.feed( title: 'Test feed', link:'http://somewhere.com/') {
				content {
					entry {
						"Invalid"
					}
				}
			}
		}
	}
	

}

