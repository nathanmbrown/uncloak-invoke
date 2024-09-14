package com.github.nathanmbrown.uncloakinvoke

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.kotlin.analyzer.AnalysisResult
import javax.swing.JComponent
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.resolve.calls.callUtil.getResolvedCall
import org.jetbrains.kotlin.idea.caches.resolve

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
    ): InlayHintsCollector? =
        object : FactoryInlayHintsCollector(editor) {
            override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
                if (element is KtCallExpression) {
//                    val calleeExpression = element.calleeExpression
//                    if (calleeExpression is KtNameReferenceExpression) {
                    element.safeAnalyzeNonSourceRootCode(BodyResolveMode.PARTIAL_WITH_CFA)
//                        val resolved = calleeExpression.reference?.resolve()
                        // Check if the resolved function is an `invoke` operator
//                        if (resolved is KtFunction) {
//                            println("$resolved")
//                            val offset = element.textOffset + element.textLength
//                            sink.addInlineElement(
//                                offset,
//                                true,
//                                factory.text(".invoke"),
//                                false
//                            )
//                        }
//                    }
                }
                return true
            }
        }
}