includeTargets << new File("${basedir}/scripts/_BuildCuanto.groovy")

target(main: "Build the Cuanto API") {
	depends(cuantoapi)
}

setDefaultTarget(main)
