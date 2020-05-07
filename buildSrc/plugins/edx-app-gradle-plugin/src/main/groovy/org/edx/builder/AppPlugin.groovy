package org.tta.builder

import org.gradle.api.*

class AppPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create("edx", Configuration)
        loadPluginConfiguration(project)
        addTasks(project)
    }
    
    def addTasks(Project project) {
        def helper = new TaskHelper()
        project.task('printConfigPath') {
            doLast {
                println "Your current configuration path is " + project.edx.dir
            }
        }

        project.task('printConfigFiles') {
            doLast {
                helper.printConfigFiles(project)
            }
        }

        project.task('printConfig') {
            doLast {
                helper.printConfig(project)
            }
        }

        project.task('format') {
            doLast {
                def files = project.edx.activeConfig.getSrcFiles(project)
                helper.format(project, files)
            }
        }
    }
    private def loadPluginConfiguration(project) {
        // first find out where the more specific properties are
        try {
            project.apply from: project.file('edx.properties')
        }
        catch(GradleException e) {
            println "Could not load edx.properties, using default configuration"
        }

        // then actually load them
        def configPath = project.uri(project.edx.dir + '/' + 'edx.properties')
        try {
            project.apply from: configPath
        }
        catch(GradleException e) {
            println "Configuration in " + configPath + " is malformed."
            throw e
        }
    }


}
