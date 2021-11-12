package com.jetbrains.edu.learning.taskDescription.ui.check

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.actionSystem.impl.PresentationFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.ui.GotItTooltip
import com.intellij.ui.components.JBOptionButton
import com.intellij.util.containers.headTail
import com.jetbrains.edu.learning.actions.ActionWithProgressIcon
import com.jetbrains.edu.learning.messages.EduCoreBundle
import java.awt.*
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JPanel

/**
 * Panel with button inside and progress icon if action is appropriate
 */
class CheckPanelButtonComponent private constructor() : JPanel(BorderLayout()) {
  /**
   * @param[action] action to execute when button is clicked. Panel will also have process icon when action is being executed.
   * @param[isDefault] parameter specifies whether button is painted as default or not. `false` by default.
   * @param[isEnabled] parameter for enabling/disabling button. `true` by default.
   *
   * @see com.jetbrains.edu.learning.actions.ActionWithProgressIcon
   */
  constructor(action: ActionWithProgressIcon, isDefault: Boolean = false, isEnabled: Boolean = true) : this() {
    val buttonPanel = createButtonPanel(action, isDefault, isEnabled, null, null)
    add(buttonPanel, BorderLayout.WEST)

    val processPanel = action.processPanel
    if (processPanel != null) {
      add(processPanel, BorderLayout.CENTER)
    }
  }

  /**
   * @param[action] action to execute when button is clicked.
   * @param[isDefault] parameter specifies whether button is painted as default or not. `false` by default.
   * @param[isEnabled] parameter for enabling/disabling button. `true` by default.
   */
  constructor(action: AnAction,
              isDefault: Boolean = false,
              optionalActions: List<AnAction>? = null,
              project: Project? = null,
              isEnabled: Boolean = true) : this() {
    val buttonPanel = createButtonPanel(action, isDefault, isEnabled, optionalActions, project)
    add(buttonPanel)
  }

  private fun createButtonPanel(action: AnAction,
                                isDefault: Boolean = false,
                                isEnabled: Boolean = true,
                                optionalActions: List<AnAction>?,
                                project: Project?): JPanel {
    val cols = if (optionalActions == null) 1 else 2
    val buttonPanel = JPanel(GridLayout(1, cols, 5, 0))
    val button = createButton(action, isDefault = isDefault, isEnabled = isEnabled)
    buttonPanel.add(button)

    if (!optionalActions.isNullOrEmpty() && project != null) {
      val optionButton = createOptionalButtons(project, optionalActions)
      buttonPanel.add(optionButton)
    }

    val gridBagPanel = JPanel(GridBagLayout())
    val buttonPanelGridBagConstraints = GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                                                           GridBagConstraints.CENTER, GridBagConstraints.NONE,
                                                           Insets(8, 0, 0, 0), 0, 0)
    gridBagPanel.add(buttonPanel, buttonPanelGridBagConstraints)
    return gridBagPanel
  }

  private fun createButton(action: AnAction, isDefault: Boolean = false, isEnabled: Boolean = true): JButton {
    val button = object : JButton(action.templatePresentation.text) {
      override fun isDefaultButton(): Boolean = isDefault
      override fun isEnabled(): Boolean = isEnabled
      override fun isFocusable(): Boolean = isEnabled
    }
    if (isEnabled) {
      button.addActionListener { e ->
        performAnAction(e, this, action)
      }
    }
    return button
  }

  private fun createOptionalButtons(project: Project, optionalActions: List<AnAction>): JBOptionButton {
    val (mainAction, otherActions) = optionalActions.headTail()

    val optionButton = DefaultOptionalButton(mainAction, otherActions)
    addGotItTooltip(optionButton, project)
    return optionButton
  }

  private fun addGotItTooltip(option: JBOptionButton, project: Project) {
    val gotItTooltip = GotItTooltip("codeforces.submit.solution.button",
                                    EduCoreBundle.message("codeforces.you.can.submit.solution.from.ide"), project).withPosition(
      Balloon.Position.above)
    if (gotItTooltip.canShow()) {
      gotItTooltip.show(option, GotItTooltip.TOP_MIDDLE)
    }
  }

  private inner class DefaultOptionalButton(
    mainAction: AnAction,
    otherActions: List<AnAction>)
    : JBOptionButton(AnActionWrapper(mainAction, this@CheckPanelButtonComponent), null) {

    init {
      setOptions(otherActions)
    }

    override fun isDefaultButton() = true
  }

}

private class AnActionWrapper(
  private val action: AnAction,
  private val component: JComponent
) : AbstractAction(action.templatePresentation.text) {
  override fun actionPerformed(e: ActionEvent) {
    performAnAction(e, component, action)
  }
}

private fun performAnAction(actionEvent: ActionEvent, component: JComponent, action: AnAction) {
  val dataContext = DataManager.getInstance().getDataContext(component)
  val event = AnActionEvent(
    null,
    dataContext,
    CheckPanel.ACTION_PLACE,
    PresentationFactory().getPresentation(action),
    ActionManager.getInstance(),
    actionEvent.modifiers
  )

  // BACKCOMPAT: 2021.1
  @Suppress("DEPRECATION")
  ActionUtil.performActionDumbAwareWithCallbacks(action, event, dataContext)

}