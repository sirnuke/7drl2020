import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  base
  kotlin("jvm") version "1.4.21" apply false
  java
  `maven-publish`
  id("net.researchgate.release") version "2.8.1"
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

tasks.register("printVersion") {
  doLast {
    println(project.version)
  }
}

subprojects {
  apply(plugin = "java")
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "maven-publish")

  if (project.properties.containsKey("internalMavenURL"))
  {
    val internalMavenUsername: String by project
    val internalMavenPassword: String by project
    val internalMavenURL: String by project

    publishing {
      publications {
        create<MavenPublication>("maven") {
          from(components["java"])
        }
      }
      repositories {
        maven {
          credentials {
            username = internalMavenUsername
            password = internalMavenPassword
          }
          val releasesRepoUrl = "$internalMavenURL/releases/"
          val snapshotsRepoUrl = "$internalMavenURL/snapshots/"
          url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
          name = "Internal-Maven-Publish"
        }
      }
    }

    repositories {
      maven {
        credentials {
          username = internalMavenUsername
          password = internalMavenPassword
        }
        url = uri("$internalMavenURL/releases")
        name = "Internal-Maven-Releases"
      }
    }

    repositories {
      maven {
        credentials {
          username = internalMavenUsername
          password = internalMavenPassword
        }
        url = uri("$internalMavenURL/snapshots")
        name = "Internal-Maven-Snapshots"
      }
    }
  }

  dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("ch.qos.logback:logback-classic:1.2.3")
  }

  tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }
}
