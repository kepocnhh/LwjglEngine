package lwjgl.wrapper.util.io

fun <T: AutoCloseable?, R> T.use(block: (T) -> R): R {
    var error: Throwable? = null
    try {
        return block(this)
    } catch(throwable: Throwable) {
        error = throwable
        throw throwable
    } finally {
        when {
            KotlinVersion.CURRENT.isAtLeast(1, 1, 0) -> closeFinally(error)
            this == null -> Unit
            error == null -> close()
            else -> try {
                close()
            } catch(ignored: Throwable) {}
        }
    }
}

private fun AutoCloseable?.closeFinally(cause: Throwable?) = when {
    this == null -> Unit
    cause == null -> close()
    else -> try {
        close()
    } catch(closeException: Throwable) {
        cause.addSuppressed(closeException)
    }
}
