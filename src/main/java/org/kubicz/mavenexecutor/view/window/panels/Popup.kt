package org.kubicz.mavenexecutor.view.window.panels

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.ActiveIcon
import com.intellij.openapi.util.Disposer
import com.intellij.ui.popup.AbstractPopup
import com.intellij.util.ui.EmptyIcon
import java.awt.Component
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.InputEvent
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.SwingUtilities

class Popup(project: Project, component: JComponent, anchor: JComponent) : AbstractPopup() {

    private val anchor = anchor
    var closeByAnchor = false

    init {
        init(
                project, component, null, false, true, false, null,
                false, "", null, true, emptySet(), false, null,
                null, null, true, ActiveIcon(EmptyIcon.ICON_0), true, false,
                true, null, 0.0f, null, true, true, arrayOfNulls<Component>(0), null, 2,
                false, emptyList(), null, null, false,
                true, true, null, true, null
        )

        Disposer.register(ApplicationManager.getApplication(), this)
    }

    override fun cancel(e: InputEvent?) {
        if (e is MouseEvent) {
            val point = e.point.clone() as Point
            SwingUtilities.convertPointToScreen(point, e.getComponent())
            val bounds = Rectangle(anchor.locationOnScreen, anchor.size)
            if(bounds.contains(point)) {
                closeByAnchor = true
            }

        }
        super.cancel(e)
    }

}