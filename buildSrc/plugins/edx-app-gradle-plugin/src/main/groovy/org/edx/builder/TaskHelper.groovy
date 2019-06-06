package org.tta.builder

import org.yaml.snakeyaml.*
import org.gradle.api.*

class TaskHelper {
    // List of files to load config keys from
    private def getConfigPaths(project) {
        def result = []
        for(configName in project.edx.activeConfig.configFiles) {
            result.add(project.file(project.edx.dir + '/' + configName))
        }
        return result
    }

    def loadConfig(project) {
        def yaml = new Yaml()
        def properties = [:]
        for(path in getConfigPaths(project)) {
            try {
                def dict = yaml.load(new FileInputStream(path))
                if(dict != null) {
                    properties = properties + dict
                }
            }
            catch(FileNotFoundException e) {
                println path + " not found. Skipping."
            }
        }
        return properties
    }

    def printConfigFiles(project) {
        println "You are loading configuration data from the following:"
        for(path in getConfigPaths(project)) {
            println "\t" + path
        }
    }

    def printConfig(project) {
        def config = loadConfig(project)

        def options = new DumperOptions()
        options.prettyFlow = true

        def yaml = new Yaml(options)

        println "Your current configuration is:"
        println yaml.dump(config)
    }

    def format(project, files) {
        println ""
        println "This command requires uncrustify (http://uncrustify.sourceforge.net)"
        println ""
        
        files.visit { file ->
            if(!file.isDirectory()) {
                project.exec {
                    executable "uncrustify"
                    args '-c', '.uncrustify', '--replace', '--no-backup', '-l', 'OC', file.file.path
                }
            }
        }
    }
}
