import org.codehaus.groovy.grails.plugins.yui.Yui

class YuiTagLib {

    static namespace = "yui"
    
    /**
     * Includes a YUI javascript file from the YUI distribution folder
     *  
     * <yui:javascript dir="calendar" file="calendar-min.js" /> 
     * 
     * Actually imports '/app/js/{version}/yui/calendar/calendar-min.js'
     * 
     * @param dir The name of the directory within the yui folder to link to 
     * @param file The name of the file to link to
     **/
    def javascript = { attrs ->
        if (!attrs.dir)
            throwTagError("Tag [javascript] is missing required attribute [dir]")
        if (!attrs.file)
            throwTagError("Tag [javascript] is missing required attribute [file]")

        def version = attrs.version ? attrs.remove('version') : Yui.version
            
        def src = createLinkTo(dir: "js/yui/${version}/${attrs.dir}", file: attrs.file)
        out << "<script type=\"text/javascript\" src=\"${src}\"></script>"
    }
    
    /**
     * Includes a YUI stylesheet file from the YUI distribution folder
     *
     * <yui:stylesheet dir="calendar/assets" file="calendar.css" />
     * 
     * Actually imports '/app/js/yui/{version}/calendar/assets/calendar.css'
     * 
     * @param dir The name of the directory within the yui folder to link to 
     * @param file The name of the file to link to 
     **/
    def stylesheet = { attrs ->
        if (!attrs.dir)
            throwTagError("Tag [stylesheet] is missing required attribute [dir]")
        if (!attrs.file)
            throwTagError("Tag [stylesheet] is missing required attribute [file]")

        def version = attrs.version ? attrs.remove('version') : Yui.version
            
        def href = createLinkTo(dir: "js/yui/${version}/${attrs.dir}", file: attrs.file)
        out << "<link rel=\"stylesheet\" type=\"text/css\" href=\"${href}\" />"
    }
}
