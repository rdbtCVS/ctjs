package com.chattriggers.ctjs.internal.engine.module

import com.fasterxml.jackson.core.Version
import java.io.File

class Module(val name: String, var metadata: ModuleMetadata, val folder: File) {
    var targetModVersion: Version? = null
    var requiredBy = mutableSetOf<String>()

    override fun toString() = "Module{name=$name,version=${metadata.version}}"
}
