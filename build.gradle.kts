import net.minecraftforge.gradle.user.UserBaseExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
  repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven")
    maven("https://files.minecraftforge.net/maven")
    maven("https://dl.bintray.com/kotlin/kotlinx")
  }

  dependencies {
    classpath("net.minecraftforge.gradle:ForgeGradle:2.3-SNAPSHOT")
    classpath("org.spongepowered:mixingradle:0.4-SNAPSHOT")
  }
}

plugins {
  kotlin("jvm") version "1.3.61"
}

apply(plugin = "net.minecraftforge.gradle.liteloader")
apply(plugin = "org.spongepowered.mixin")

group = "com.twoandahalfdevs"
version = "1.0-SNAPSHOT"
val mcVersion = "1.12.2"

repositories {
  mavenCentral()
}

val embedded = configurations.create("embedded")

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  embedded(kotlin("stdlib-jdk8"))
  embedded(kotlin("reflect"))
}

configure<UserBaseExtension> {
  version = mcVersion
  mappings = "stable_39"
  runDir = "run"
}

configure<org.spongepowered.asm.gradle.plugins.MixinExtension> {
  defaultObfuscationEnv = "notch"
}

java.sourceSets["main"].ext {
  set("refMap", "mixin.drimprovement.refmap.json")
}

tasks.withType<ProcessResources> {
  val props =
    mapOf(
      "version" to version,
      "mcversion" to mcVersion
    )

  from(java.sourceSets["main"].resources.srcDirs) {
    include("litemod.json")
    expand(props)
  }
  from(java.sourceSets["main"].resources.srcDirs) {
    exclude("litemod.json")
  }
}

tasks.withType<Jar> {
  dependsOn(configurations.runtimeClasspath)
  from(embedded.asFileTree.fold(files().asFileTree) { collection, file ->
    if (file.isDirectory) {
      collection
    } else {
      collection.plus(zipTree(file))
    }
  })

  from(java.sourceSets["main"].java)
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}
