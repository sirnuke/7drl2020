import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
  application
  id("com.github.johnrengelman.shadow") version "5.2.0"
}

application {
  mainClassName = "com.degrendel.outrogue.frontend.MainKt"
}

repositories {
  maven {
    url = uri("https://jitpack.io")
  }
}

buildscript {
  extra.set("zirconVersion", "2020.1.0-RELEASE")
}

dependencies {
  val zirconVersion = project.extra.get("zirconVersion")!!
  implementation("org.hexworks.zircon:zircon.core-jvm:$zirconVersion")
  implementation("org.hexworks.zircon:zircon.jvm.swing:$zirconVersion")
  implementation("info.picocli:picocli:3.9.6")
  implementation(project(":outrogue-common"))
  implementation(project(":outrogue-engine"))
  implementation(project(":outrogue-agent"))
}

tasks.withType<ShadowJar> {
  baseName = "outrogue"
  classifier = ""
}
