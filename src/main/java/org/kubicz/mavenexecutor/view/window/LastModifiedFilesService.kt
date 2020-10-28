package org.kubicz.mavenexecutor.view.window

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import java.util.concurrent.ConcurrentHashMap


@State(name = "lastModifiedFilesService")
class LastModifiedFilesService {

    private val modifiedFiles = ConcurrentHashMap<String, VirtualFile>();

    fun addFile(file: VirtualFile) {
        modifiedFiles.putIfAbsent(file.path, file)
    }

    fun clearFiles() {
        modifiedFiles.clear()
    }

    fun getFiles() : Collection<VirtualFile> {
        return modifiedFiles.values
    }

    companion object {
        fun getInstance(project: Project): LastModifiedFilesService {
            return ServiceManager.getService(project, LastModifiedFilesService::class.java)
        }
    }
}
