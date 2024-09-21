package com.github.nathanmbrown.uncloakinvoke

import com.intellij.codeInsight.hints.*
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.suggested.endOffset
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.idea.caches.resolve.analyze
import org.jetbrains.kotlin.idea.intentions.isInvokeOperator
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.source.KotlinSourceElement
import javax.swing.JComponent

class InvokeInlayProvider : InlayHintsProvider<NoSettings> {
    override val key: SettingsKey<NoSettings>
        get() = SettingsKey("InvokeCloak")
    override val name: String
        get() = "InvokeCloak"
    override val previewText: String?
        get() = null

    override fun createSettings(): NoSettings {
        return NoSettings()
    }

    override fun createConfigurable(settings: NoSettings): ImmediateConfigurable {
        return object : ImmediateConfigurable {
            override fun createComponent(listener: ChangeListener): JComponent = panel {}
        }
    }

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector =
        object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (element is KtCallExpression) {
                    val firstChild: PsiElement? = (element as PsiElement).firstChild
                    if (firstChild?.text != "invoke") {
                        val bindingContext: BindingContext = element.analyze()
                        val resolvedCall: ResolvedCall<out CallableDescriptor>? =
                            element.getResolvedCall(bindingContext)
                        val functionDescriptor = resolvedCall?.resultingDescriptor
                        functionDescriptor?.apply {
                            if (this.isInvokeOperator) {
                                val offset = (element as PsiElement).getFirstChild().endOffset
                                val text = factory.text(".invoke")
                                val presentation: InlayPresentation = factory.roundWithBackgroundAndSmallInset(text)
                                val reference = factory.referenceOnHover(presentation) { _, _ ->
                                    navigateToFunctionDeclaration(editor.project!!, this)
                                }
                                sink.addInlineElement(
                                    offset,
                                    true,
                                    reference,
                                    false
                                )
                            }
                        }
                    }
                }
                return true
            }
        }

    fun navigateToFunctionDeclaration(project: Project, functionDescriptor: CallableDescriptor) {
        // Resolve the PsiElement from the FunctionDescriptor
        val sourceElement = (functionDescriptor.source as KotlinSourceElement).psi
        val psiElement = sourceElement.psiOrParent as PsiElement?

        // Ensure the PsiElement is valid and not null
        if (psiElement != null && psiElement.isValid) {
            // Perform the navigation
            DumbService.getInstance(project).smartInvokeLater {
                val navigatable = PsiNavigationSupport.getInstance().createNavigatable(
                    project,
                    psiElement.containingFile.virtualFile,
                    psiElement.textOffset
                )
                navigatable.navigate(true)
            }
        } else {
            println("Error: Unable to find the declaration for the given FunctionDescriptor.")
        }
    }
}