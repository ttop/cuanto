import org.codehaus.groovy.grails.plugins.web.taglib.*
import org.codehaus.groovy.grails.plugins.yui.Yui

class YuiGrailsPlugin {
    def version = "2.6.0"
    def dependsOn = [:]
    def author = "Marcel Overdijk"
    def authorEmail = "marceloverdijk@gmail.com"
    def title = "Yahoo! User Interface Library (YUI)"
    def description = "Provides integration with the Yahoo! UI Library"
    def documentation = "http://grails.org/YUI+Plugin"
    
    def doWithApplicationContext = { applicationContext ->
        JavascriptTagLib.LIBRARY_MAPPINGS.yui = ["yui/${Yui.version}/yahoo-dom-event/yahoo-dom-event", "yui/${Yui.version}/connection/connection-min"]    
        JavascriptTagLib.PROVIDER_MAPPINGS.yui = YuiProvider.class
    }
}
