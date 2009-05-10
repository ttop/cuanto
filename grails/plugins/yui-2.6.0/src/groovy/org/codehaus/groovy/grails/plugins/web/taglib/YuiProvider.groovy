package org.codehaus.groovy.grails.plugins.web.taglib;

/**
 * An implementation for the Yahoo! UI Library.
 */
class YuiProvider implements JavascriptProvider {

    def doRemoteFunction(taglib, attrs, out) {
        
        if (attrs.onLoading) {
            out << "${attrs.onLoading};"
        }

        def method = (attrs.method ? attrs.remove('method') : 'POST')
        out << "YAHOO.util.Connect.asyncRequest('${method}'"

        def jsParams = attrs.params?.findAll { it.value instanceof JavascriptValue }
        jsParams?.each { attrs.params?.remove(it.key) }
                
        // build url
        def url
        if (attrs.url) {
            url = taglib.createLink(attrs.url)
        }
        else {
            url = taglib.createLink(attrs)
        }
        attrs.remove('url')

        if (!attrs.params) attrs.params = [:]
        jsParams?.each { attrs.params[it.key] = it.value }
        
        // process params
        def postData = 'null'
        def i = url?.indexOf('?')
        if (i > -1) {
            if (attrs.params instanceof String) {
                attrs.params += "+'&${url[i+1..-1].encodeAsJavaScript()}'"
            }
            else if (attrs.params instanceof Map) {
                def params = createQueryString(attrs.params)
                attrs.params = "'${params}${params ? '&' : ''}${url[i+1..-1].encodeAsJavaScript()}'"
            }
            else {
                attrs.params = "'${url[i+1..-1].encodeAsJavaScript()}'"
            }
            url = url[0..i-1]
        }
        if (attrs.params) {
            def params = attrs.remove('params')
            if (params instanceof Map) {
                params = createQueryString(params)
                params = "'${params}'"
            }
            if (method == 'POST') {
                postData = params
                url = "'${url}'"
            }
            else {
                url = "'${url}?'+${params}"
            }
        }
        else {
            url = "'${url}'"
        }

        out << ", ${url}, "
        buildCallback(attrs, out)
        out << ", ${postData});"

        // remove all onX callback events
        def callbacks = attrs.findAll { k, v ->
            k ==~ /on(\p{Upper}|\d){1}\w+/
        }
        callbacks.each { k, v ->
            attrs.remove(k)
        }
        
        attrs.remove('params')
    }

    // helper method to create callback object
    def buildCallback(attrs, out) {     
        
        out << '{'
        
        // success
        out << 'success: function(o){'
        if (attrs.onLoaded) {
            out << "${attrs.onLoaded};"
        }
        if (attrs.update instanceof Map) {
            if (attrs.update?.success) {
                out << "YAHOO.util.Dom.get('${attrs.update.success}').innerHTML = o.responseText;"
            }
        }
        else if (attrs.update) {
            out <<  "YAHOO.util.Dom.get('${attrs.update}').innerHTML = o.responseText;"
        }
        if(attrs.onSuccess) {
            out << "${attrs.onSuccess};"
        }	
        if(attrs.onComplete) {
            out << "${attrs.onComplete};"
        }		  
        out << '}, '
        
        // failure
        out << 'failure: function(o){'									
        if (attrs.update instanceof Map) {
            if (attrs.update?.failure) {
                out << "YAHOO.util.Dom.get('${attrs.update.failure}').innerHTML = o.statusText;"
            }
        }
        if (attrs.onFailure) {
            out << "${attrs.onFailure};"
        }	
        if (attrs.onComplete) {
            out << "${attrs.onComplete};"
        }													
        out << '}'
        
        out << '}'
    }

    private String createQueryString(params) {
        def allParams = []
        for (entry in params) {
            def value = entry.value
            def key = entry.key
            if (value instanceof JavascriptValue) {
                allParams << "${key.encodeAsURL()}='+${value.value}+'"
            }
            else {
                allParams << "${key.encodeAsURL()}=${value.encodeAsURL()}".encodeAsJavaScript()
            }
        }
        if (allParams.size() == 1) {
            return allParams[0]
        }
        else {
            return allParams.join('&')
        }
    }
    
    def prepareAjaxForm(attrs) {
        if (attrs.before) {
            attrs.before += ";YAHOO.util.Connect.setForm('${attrs.name}')"
        }
        else {
            attrs.before = "YAHOO.util.Connect.setForm('${attrs.name}')"
        }
	}
}
