package org.kubicz.mavenexecutor.view.components

import com.intellij.vcs.log.ui.frame.WrappedFlowLayout
import java.awt.Container
import java.awt.Dimension
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingUtilities

class ProfilesWrappedFlowLayout(hGap: Int, vGap: Int): WrappedFlowLayout(hGap, vGap) {

    private var directParent = true


    override fun getWrappedSize(target: Container?): Dimension? {
        directParent = true

        val parent = findWidthParent(target!!)
        val offset = if (directParent) 0 else getOffset(target)
        val maxWidth = parent.width - (parent.insets.left + parent.insets.right) - offset
        return getDimension(target, maxWidth)
    }

    private fun getOffset(target: Container?): Int {
        return 84 // megic number
    }


    private fun findWidthParent(target: Container): Container {
        val parent = SwingUtilities.getUnwrappedParent(target)
        if (parent == null) {
            return target
        }

        if (parent.width > 0) {
            return parent
        }

        directParent = false
        return findWidthParent(parent)
    }
}