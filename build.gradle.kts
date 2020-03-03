import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  base
  kotlin("jvm") version "1.3.70" apply false
  java
  `maven-publish`
  id("net.researchgate.release") version "2.6.0"
}

allprojects {
  group = "com.degrendel"

  repositories {
    mavenCentral()
    jcenter()
  }
}

release {
  tagTemplate = "v\${version}"
}

dependencies {
  // Make the root project archives configuration depend on every subproject
  subprojects.forEach {
    archives(it)
  }
}

tasks.register("printVersion") {
  doLast {
    println(project.version)
  }
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "maven-publish")

  if (project.properties.containsKey("internalNexusURL"))
  {
    val internalNexusUsername: String by project
    val internalNexusPassword: String by project
    val internalNexusURL: String by project

    publishing {
      publications {
        create<MavenPublication>("maven") {
          from(components["java"])
        }
      }
      repositories {
        maven {
          credentials {
            username = internalNexusUsername
            password = internalNexusPassword
          }
          val releasesRepoUrl = "$internalNexusURL/repository/maven-releases/"
          val snapshotsRepoUrl = "$internalNexusURL/repository/maven-snapshots/"
          url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
          name = "Internal-Nexus"
        }
      }
    }

    repositories {
      maven {
        credentials {
          username = internalNexusUsername
          password = internalNexusPassword
        }
        url = uri("$internalNexusURL/repository/maven-public")
        name = "Internal-Nexus"
      }
    }
  }

  dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api:1.7.26")
    implementation("ch.qos.logback:logback-classic:1.2.3")
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }
}
