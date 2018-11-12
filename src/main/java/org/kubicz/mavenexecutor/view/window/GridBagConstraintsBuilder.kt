package org.kubicz.mavenexecutor.view.window

import java.awt.*

class GridBagConstraintsBuilder {

    private var gridx = GridBagConstraints.RELATIVE

    private var gridy = GridBagConstraints.RELATIVE

    private var gridwidth = 1

    private var gridheight = 1

    private var weightx = 0.0

    private var weighty = 0.0

    private var anchor = GridBagConstraints.CENTER

    private var fill = GridBagConstraints.NONE

    private var insetTop = 0

    private var insetLeft = 0

    private var insetBottom = 0

    private var insetRight = 0

    private var ipadx = 0

    private var ipady = 0


    fun gridx(gridx: Int): GridBagConstraintsBuilder {
        this.gridx = gridx
        return this
    }

    fun gridy(gridy: Int): GridBagConstraintsBuilder {
        this.gridy = gridy
        return this
    }

    fun gridwidth(gridwidth: Int): GridBagConstraintsBuilder {
        this.gridwidth = gridwidth
        return this
    }

    fun gridheight(gridheight: Int): GridBagConstraintsBuilder {
        this.gridheight = gridheight
        return this
    }

    fun weightx(weightx: Double): GridBagConstraintsBuilder {
        this.weightx = weightx
        return this
    }

    fun weighty(weighty: Double): GridBagConstraintsBuilder {
        this.weighty = weighty
        return this
    }

    fun anchor(anchor: Int): GridBagConstraintsBuilder {
        this.anchor = anchor
        return this
    }

    fun anchorWest(): GridBagConstraintsBuilder {
        this.anchor = GridBagConstraints.WEST
        return this
    }

    fun anchorEast(): GridBagConstraintsBuilder {
        this.anchor = GridBagConstraints.EAST
        return this
    }

    fun fill(fill: Int): GridBagConstraintsBuilder {
        this.fill = fill
        return this
    }

    fun fillHorizontal(): GridBagConstraintsBuilder {
        this.fill = GridBagConstraints.HORIZONTAL
        return this
    }

    fun fillVertical(): GridBagConstraintsBuilder {
        this.fill = GridBagConstraints.VERTICAL
        return this
    }

    fun fillBoth(): GridBagConstraintsBuilder {
        this.fill = GridBagConstraints.BOTH
        return this
    }

    fun fillNone(): GridBagConstraintsBuilder {
        this.fill = GridBagConstraints.NONE
        return this
    }

    fun ipadx(ipadx: Int): GridBagConstraintsBuilder {
        this.ipadx = ipadx
        return this
    }

    fun ipady(ipady: Int): GridBagConstraintsBuilder {
        this.ipady = ipady
        return this
    }

    fun insetTop(insetTop: Int): GridBagConstraintsBuilder {
        this.insetTop = insetTop
        return this
    }

    fun insetLeft(insetLeft: Int): GridBagConstraintsBuilder {
        this.insetLeft = insetLeft
        return this
    }

    fun insetBottom(insetBottom: Int): GridBagConstraintsBuilder {
        this.insetBottom = insetBottom
        return this
    }

    fun insetRight(insetRight: Int): GridBagConstraintsBuilder {
        this.insetRight = insetRight
        return this
    }

    fun build(): GridBagConstraints {
        return GridBagConstraints(gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, Insets(insetTop, insetLeft, insetBottom, insetRight), ipadx, ipady)
    }

}