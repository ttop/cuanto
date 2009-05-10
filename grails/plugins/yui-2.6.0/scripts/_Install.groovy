Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )  
checkVersion()
configureProxy()

yuiVersion = "2.6.0"

Ant.sequential {
    mkdir(dir:"${grailsHome}/downloads")

    event("StatusUpdate", ["Downloading YUI ${yuiVersion}"])

    get(dest:"${grailsHome}/downloads/yui_${yuiVersion}.zip",
        src:"http://downloads.sourceforge.net/yui/yui_${yuiVersion}.zip",
        verbose:true,
        usetimestamp:true)
    unzip(dest:"${grailsHome}/downloads/yui_${yuiVersion}",
        src:"${grailsHome}/downloads/yui_${yuiVersion}.zip")	
	
    mkdir(dir:"${basedir}/web-app/js/yui/${yuiVersion}")
    copy(todir:"${basedir}/web-app/js/yui/${yuiVersion}") {
        fileset(dir:"${grailsHome}/downloads/yui_${yuiVersion}/yui/build", includes:"**/**")
    }		 
}            
event("StatusFinal", ["YUI ${yuiVersion} installed successfully"])
