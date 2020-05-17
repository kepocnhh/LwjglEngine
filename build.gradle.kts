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
