package net.nonylene.gradle.external

import com.intellij.ide.DataManager
import com.intellij.ide.macro.MacroManager
import com.intellij.ide.macro.MacrosDialog
import com.intellij.openapi.actionSystem.DataKeys
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.FixedSizeButton
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.tools.ToolsBundle
import com.intellij.tools.ToolEditorDialog
import com.intellij.ui.IdeBorderFactory
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.io.File
import javax.swing.*
import javax.swing.text.BadLocationException


/**
 * @see ToolEditorDialog
 */
class ExternalPreferencePanel {

    private val provider = ServiceManager.getService(ExternalPreferenceProvider::class.java)

    // command fields
    private val myTfCommandWorkingDirectory = JTextField()
    private val myTfCommand = JTextField()
    private val myParametersField = JTextField()
    private var myInsertWorkingDirectoryMacroButton: JButton? = null
    private var myInsertCommandMacroButton: JButton? = null
    private var myInsertParametersMacroButton: JButton? = null

    // panels
    private val mySimpleProgramPanel = createCommandPane()

    private var project: Project? = null

    fun createPanel(): JPanel {
        val panel = JPanel(GridBagLayout())
        var constr: GridBagConstraints

        // custom panels (put into same place)
        constr = GridBagConstraints()
        constr.gridx = 0
        constr.gridy = 0
        constr.gridwidth = 4
        constr.fill = GridBagConstraints.BOTH
        constr.weightx = 1.0
        constr.weighty = 1.0
        constr.anchor = GridBagConstraints.NORTH
        panel.add(mySimpleProgramPanel, constr)


        return panel
    }

    init {
        val dataContext = DataManager.getInstance().dataContextFromFocus.result;
        MacroManager.getInstance().cacheMacrosPreview(dataContext)
        project = DataKeys.PROJECT.getData(dataContext)
        addListeners()
    }

    private fun createCommandPane(): JPanel {
        val pane = JPanel(GridBagLayout())
        pane.border = IdeBorderFactory.createTitledBorder(ToolsBundle.message("tools.tool.group"), true)
        var constr: GridBagConstraints

        // program

        constr = GridBagConstraints()
        constr.gridx = 0
        constr.gridy = 0
        constr.insets = Insets(0, 0, 0, 10)
        constr.anchor = GridBagConstraints.BASELINE_LEADING
        pane.add(JLabel(ToolsBundle.message("tools.program.label")), constr)

        val browseCommandButton = FixedSizeButton(myTfCommand)

        addCommandBrowseAction(pane, browseCommandButton, myTfCommand)

        val _pane0 = JPanel(BorderLayout())
        _pane0.add(myTfCommand, BorderLayout.CENTER)
        _pane0.add(browseCommandButton, BorderLayout.EAST)

        constr = GridBagConstraints()
        constr.gridx = 1
        constr.gridy = 0
        constr.insets = Insets(0, 0, 0, 10)
        constr.fill = GridBagConstraints.HORIZONTAL
        constr.anchor = GridBagConstraints.BASELINE_LEADING
        constr.weightx = 1.0
        pane.add(_pane0, constr)

        constr = GridBagConstraints()
        constr.gridx = 2
        constr.gridy = 0
        constr.insets = Insets(0, 0, 0, 0)
        constr.fill = GridBagConstraints.HORIZONTAL
        constr.anchor = GridBagConstraints.BASELINE_LEADING
        myInsertCommandMacroButton = JButton(ToolsBundle.message("tools.insert.macro.button"))
        pane.add(myInsertCommandMacroButton, constr)

        // parameters

        constr = GridBagConstraints()
        constr.gridx = 0
        constr.gridy = 1
        constr.insets = Insets(5, 0, 0, 10)
        constr.anchor = GridBagConstraints.BASELINE_LEADING
        pane.add(JLabel(ToolsBundle.message("tools.parameters.label")), constr)

        constr = GridBagConstraints()
        constr.gridx = 1
        constr.gridy = 1
        constr.insets = Insets(5, 0, 0, 10)
        constr.fill = GridBagConstraints.HORIZONTAL
        constr.anchor = GridBagConstraints.BASELINE_LEADING
        constr.weightx = 1.0
        pane.add(myParametersField, constr)

        constr = GridBagConstraints()
        constr.gridx = 2
        constr.gridy = 1
        constr.insets = Insets(5, 0, 0, 0)
        constr.fill = GridBagConstraints.HORIZONTAL
        constr.anchor = GridBagConstraints.BASELINE_LEADING
        myInsertParametersMacroButton = JButton(ToolsBundle.message("tools.insert.macro.button.a"))
        pane.add(myInsertParametersMacroButton, constr)

        // working directory

        constr = GridBagConstraints()
        constr.gridx = 0
        constr.gridy = 2
        constr.insets = Insets(5, 0, 0, 10)
        constr.anchor = GridBagConstraints.BASELINE_LEADING
        pane.add(JLabel(ToolsBundle.message("tools.working.directory.label")), constr)

        val browseDirectoryButton = FixedSizeButton(myTfCommandWorkingDirectory)
        addWorkingDirectoryBrowseAction(pane, browseDirectoryButton, myTfCommandWorkingDirectory)

        val _pane1 = JPanel(BorderLayout())
        _pane1.add(myTfCommandWorkingDirectory, BorderLayout.CENTER)
        _pane1.add(browseDirectoryButton, BorderLayout.EAST)

        constr = GridBagConstraints()
        constr.gridx = 1
        constr.gridy = 2
        constr.gridwidth = 1
        constr.insets = Insets(5, 0, 0, 10)
        constr.fill = GridBagConstraints.HORIZONTAL
        constr.anchor = GridBagConstraints.WEST
        constr.weightx = 1.0
        pane.add(_pane1, constr)

        constr = GridBagConstraints()
        constr.gridx = 2
        constr.gridy = 2
        constr.insets = Insets(5, 0, 0, 0)
        constr.fill = GridBagConstraints.HORIZONTAL
        constr.anchor = GridBagConstraints.BASELINE_LEADING
        myInsertWorkingDirectoryMacroButton = JButton(ToolsBundle.message("tools.insert.macro.button.c"))
        pane.add(myInsertWorkingDirectoryMacroButton, constr)

        // for normal resizing
        constr = GridBagConstraints()
        constr.gridy = 3
        constr.fill = GridBagConstraints.BASELINE_LEADING
        constr.weighty = 1.0
        pane.add(JLabel(), constr)

        reset()

        return pane
    }

    protected fun addWorkingDirectoryBrowseAction(pane: JPanel,
                                                  browseDirectoryButton: FixedSizeButton,
                                                  tfCommandWorkingDirectory: JTextField) {
        browseDirectoryButton.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            val chooser = FileChooserFactory.getInstance().createPathChooser(descriptor, project, pane)

            chooser.choose(null, { files ->
                val file = if (!files.isEmpty()) files[0] else null
                if (file != null) {
                    myTfCommandWorkingDirectory.text = file.presentableUrl
                }
            })
        }
    }

    protected fun addCommandBrowseAction(pane: JPanel, browseCommandButton: FixedSizeButton, tfCommand: JTextField) {
        browseCommandButton.addActionListener {
            val descriptor = FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor()
            val chooser = FileChooserFactory.getInstance().createPathChooser(descriptor, project, pane)
            chooser.choose(null) { files ->
                val file = if (!files.isEmpty()) files.get(0) else null
                if (file != null) {
                    myTfCommand.text = file.presentableUrl
                    val workingDirectory = myTfCommandWorkingDirectory.text
                    if (workingDirectory == null || workingDirectory.isEmpty()) {
                        val parent = file.parent
                        if (parent != null && parent.isDirectory) {
                            myTfCommandWorkingDirectory.text = parent.presentableUrl
                        }
                    }
                }
            }
        }
    }

    private inner class InsertMacroActionListener(private val myTextField: JTextField) : ActionListener {

        override fun actionPerformed(e: ActionEvent) {
            val dialog = MacrosDialog(project)
            if (dialog.showAndGet() && dialog.selectedMacro != null) {
                val macro = dialog.selectedMacro.name
                val position = myTextField.caretPosition
                try {
                    myTextField.document.insertString(position, "$" + macro + "$", null)
                    myTextField.caretPosition = position + macro.length + 2
                } catch (ignored: BadLocationException) {
                }

            }
            IdeFocusManager.findInstance().requestFocus(myTextField, true)
        }
    }

    private fun addListeners() {
        myInsertCommandMacroButton!!.addActionListener(InsertMacroActionListener(myTfCommand))
        myInsertParametersMacroButton!!.addActionListener(InsertMacroActionListener(myParametersField))
        myInsertWorkingDirectoryMacroButton!!.addActionListener(InsertMacroActionListener(myTfCommandWorkingDirectory))
    }

    fun isModified(): Boolean {
        return provider.state?.let {
            it.workingDirectory != toSystemIndependentFormat(myTfCommandWorkingDirectory.text) ||
                    it.program != convertString(myTfCommand.text) ||
                    it.parameters != convertString(myParametersField.text)
        } ?: false
    }

    /**
     * Initialize controls
     */
    fun reset() {
        myTfCommandWorkingDirectory.text = toCurrentSystemFormat(provider.state?.workingDirectory)
        myTfCommand.text = provider.state?.program
        myParametersField.text = provider.state?.parameters
    }

    fun apply() {
        provider.state?.let {
            it.workingDirectory = toSystemIndependentFormat(myTfCommandWorkingDirectory.text)
            it.program = convertString(myTfCommand.text)
            it.parameters = convertString(myParametersField.text)
        }
    }

    private fun convertString(s: String?): String? {
        if (s != null && s.trim({ it <= ' ' }).isEmpty()) return null
        return s
    }

    private fun toSystemIndependentFormat(s: String?): String? {
        var s = s
        if (s == null) return null
        s = s.trim{ it <= ' ' }
        if (s.isEmpty()) return null
        return s.replace(File.separatorChar, '/')
    }

    private fun toCurrentSystemFormat(s: String?): String? {
        var s = s
        if (s == null) return null
        s = s.trim{ it <= ' ' }
        if (s.isEmpty()) return null
        return s.replace('/', File.separatorChar)
    }
}