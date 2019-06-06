package org.tta.builder

class Configuration {
    def dir = 'default_config'

    static final def IOS = 'IOS'
    static final def ANDROID = 'ANDROID'
    def platform

    def androidConfig
    def iosConfig

    def ios(action) {
        if(iosConfig == null) {
            iosConfig = new IOSConfiguration();
        }
        action.delegate = iosConfig;
        action()
    }

    def android(action) {
        if(androidConfig == null) {
            androidConfig = new AndroidConfiguration();
        }
        action.delegate = androidConfig 
        action()
    }

    def getActiveConfig() {
        if(platform.equals(IOS)) {
            return iosConfig
        }
        else {
            return androidConfig
        }
    }

}

class PlatformConfiguration {
    def configFiles = []
}

class IOSConfiguration extends PlatformConfiguration {

    def getSrcFiles(project) {
        project.fileTree(".").matching {
            include "**/*.h"
            include "**/*.m"
            exclude "Libraries"
            exclude "Pods"
        }
    }

}

class AndroidConfiguration extends PlatformConfiguration {

    def getSrcFiles(project) {
        project.fileTree(".").matching {
            include "src/**/*.java"
        }
    }

}
