# JDA-Toolkit
###### Toolkit to make working with JDA even more convenient

## Setup
Using Gradle to set up this dependency is strongly recommended.  
Example including JDA (build.gradle):
```
plugins {
    id 'java'
    id 'application'
    id 'com.github.johnrengelman.shadow' version '7.0.0'
}

mainClassName = 'your.main.clazz.Here' // insert your main class here
version '1.0'
sourceCompatibility = 16

compileJava {
    options.compilerArgs << '-parameters' // required for commands to work properly
}

repositories {
    mavenCentral()
    maven {
        name 'm2-dv8tion'
        url 'https://m2.dv8tion.net/releases'
    }
    maven {
        name 'hypercdn-maven2'
        url 'https://nexus.hypercdn.de/repository/hypercdn-maven'
    }
}

dependencies {
    implementation 'net.dv8tion:JDA:###' // insert latest version as seen https://github.com/DV8FromTheWorld/JDA
    implementation 'de.netbeacon.tools:JDA-Toolkit:###' // insert latest version as seen https://nexus.hypercdn.de/#browse/browse:hypercdn-maven:de%2Fnetbeacon%2Ftools%2FJDA-Toolkit
}

configurations.all {
    resolutionStrategy.cacheDynamicVersionsFor 10, 'minutes'
    resolutionStrategy.cacheChangingModulesFor 1, 'minutes'
}

shadowJar {
    archiveClassifier.set("")
}

compileJava.options.encoding = 'UTF-8'
```


## Usage

### Buttons / Selection Menus
### Commands
### LanguageManager
### EventManager
### EventWaiter
### ExecutionActions