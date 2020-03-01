dependencies {
  api("com.badlogicgames.ashley:ashley:1.7.3")
  api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.2")
}

plugins {
  id("com.peterabeles.gversion") version "1.7.0"
}

gversion {
  srcDir = "src/main/kotlin/"
  className = "GitVersion"
  classPackage = "com.degrendel.outrogue.common"
  language = "kotlin"
}

tasks.compileKotlin {
  dependsOn.add(tasks.createVersionFile)
}

