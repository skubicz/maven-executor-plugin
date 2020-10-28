package org.kubicz.mavenexecutor.view.window

import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel

class MavenExecutorToolWindowContent: SimpleToolWindowPanel(true, true), Disposable {

    @Volatile
    private var disposed = false

    override fun dispose() {
        disposed = true
    }

}