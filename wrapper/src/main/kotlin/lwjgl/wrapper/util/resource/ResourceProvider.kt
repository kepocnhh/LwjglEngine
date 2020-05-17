package lwjgl.wrapper.util.resource

import java.io.File
import java.io.InputStream
import java.net.URL

class ResourceProvider {
    companion object {
        private fun getClassLoader(): ClassLoader {
            return ResourceProvider::class.java.classLoader
        }

        fun getResourceAsStreamOrNull(filePath: String): InputStream? {
            return getClassLoader().getResourceAsStream(filePath)
        }
        fun requireResourceAsStream(filePath: String): InputStream {
            return getResourceAsStreamOrNull(filePath) ?: error("Resource by path $filePath does not exists!")
        }

        fun getResourceAsUrlOrNull(filePath: String): URL? {
            return getClassLoader().getResource(filePath)
        }
        fun requireResourceAsURL(filePath: String): URL {
            return getResourceAsUrlOrNull(filePath) ?: error("Resource by path $filePath does not exists!")
        }

        fun requireResourceAsFile(filePath: String): File {
            val url = requireResourceAsURL(filePath)
            val file = File(url.toURI())
            if(!file.exists()) error("File by path $filePath does not exists!")
            return file
        }
    }
}
