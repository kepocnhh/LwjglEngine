buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(kotlin("gradle-plugin", version = Version.kotlin))
    }
}

allprojects {
    repositories {
        jcenter()
    }
}

task<Delete>("clean") {
    delete = setOf(rootProject.buildDir, File(rootDir, "buildSrc/build"))
}
