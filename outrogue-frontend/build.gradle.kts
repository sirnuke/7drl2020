import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  application
  id("com.github.johnrengelman.shadow") version "6.1.0"
}

application {
  // TODO: This is how this is supposed to be set in modern Gradle.  However, as of this writing the ShadowJar plugin
  // expects mainClassName, so leave it commented out until plugin is updated.
  //mainClass.set("com.degrendel.outrogue.frontend.MainKt")
  mainClassName = "com.degrendel.outrogue.frontend.MainKt"
}

repositories {
  maven {
    url = uri("https://jitpack.io")
  }
}

buildscript {
  extra.set("zirconVersion", "2020.2.0-RELEASE")
}

dependencies {
  val zirconVersion = project.extra.get("zirconVersion")!!
  implementation("org.hexworks.zircon:zircon.core-jvm:$zirconVersion")
  implementation("org.hexworks.zircon:zircon.jvm.swing:$zirconVersion")
  implementation("info.picocli:picocli:4.5.2")
  implementation(project(":outrogue-common"))
  implementation(project(":outrogue-engine"))
  implementation(project(":outrogue-agent"))
}

tasks.withType<ShadowJar> {
  archiveBaseName.set("outrogue")
  classifier = ""
  mergeServiceFiles {
    include("META-INF/kie.conf")
  }
}
