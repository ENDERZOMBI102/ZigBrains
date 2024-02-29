/*
 * Copyright 2023-2024 FalsePattern
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.falsepattern.zigbrains.lsp.actions;

import com.falsepattern.zigbrains.lsp.editor.EditorEventManager;
import com.falsepattern.zigbrains.lsp.editor.EditorEventManagerBase;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.find.FindBundle;
import com.intellij.find.findUsages.FindUsagesOptions;
import com.intellij.find.findUsages.PsiElement2UsageTargetAdapter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.intellij.ui.LightweightHint;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewUtil;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageInfo2UsageAdapter;
import com.intellij.usages.UsageTarget;
import com.intellij.usages.UsageViewManager;
import com.intellij.usages.UsageViewPresentation;
import com.intellij.util.concurrency.AppExecutorUtil;
import lombok.val;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Action for references / see usages (SHIFT+ALT+F7)
 */
public class LSPReferencesAction extends DumbAwareAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        if (editor != null) {
            EditorEventManager eventManager = EditorEventManagerBase.forEditor(editor);
            if (eventManager == null) {
                return;
            }
            List<PsiElement2UsageTargetAdapter> targets = new ArrayList<>();
            Pair<List<PsiElement>, List<VirtualFile>> references = eventManager
                    .references(editor.getCaretModel().getCurrentCaret().getOffset(), true, true);
            if (references.first != null) {
                references.first.forEach(element -> targets.add(new PsiElement2UsageTargetAdapter(element, true)));
            }
            showReferences(editor, targets, editor.getCaretModel().getCurrentCaret().getLogicalPosition());
        }
    }

    public void forManagerAndOffset(EditorEventManager manager, int offset) {
        ReadAction.nonBlocking(() -> {
            val references = manager.references(offset, true, true);
            val targets = new ArrayList<PsiElement2UsageTargetAdapter>();
            if (references.first != null) {
                references.first.forEach(element -> targets.add(new PsiElement2UsageTargetAdapter(element, true)));
            }
            return targets;
                  })
                  .expireWhen(() -> manager.editor.isDisposed())
                  .finishOnUiThread(ModalityState.nonModal(), targets -> {
                      val editor = manager.editor;
                      if (editor.isDisposed())
                          return;
                      showReferences(editor, targets, editor.offsetToLogicalPosition(offset));
                  })
                  .submit(AppExecutorUtil.getAppExecutorService());
    }

    private void showReferences(Editor editor, List<PsiElement2UsageTargetAdapter> targets, LogicalPosition position) {
        if (targets.isEmpty()) {
            short constraint = HintManager.ABOVE;
            int flags = HintManager.HIDE_BY_ANY_KEY | HintManager.HIDE_BY_TEXT_CHANGE | HintManager.HIDE_BY_SCROLLING;
            JLabel label = new JLabel("No references found");
            label.setBackground(new JBColor(new Color(150, 0, 0), new Color(150, 0, 0)));
            LightweightHint hint = new LightweightHint(label);
            Point p = HintManagerImpl.getHintPosition(hint, editor, position, constraint);
            HintManagerImpl.getInstanceImpl().showEditorHint(hint, editor, p, flags, 0, false,
                    HintManagerImpl.createHintHint(editor, p, hint, constraint).setContentActive(false));
        } else {
            List<Usage> usages = new ArrayList<>();
            targets.forEach(ut -> {
                PsiElement elem = ut.getElement();
                usages.add(new UsageInfo2UsageAdapter(new UsageInfo(elem, -1, -1, false)));
            });

            if (editor == null) {
                return;
            }
            Project project = editor.getProject();
            if (project == null) {
                return;
            }
            UsageViewPresentation presentation = createPresentation(targets.get(0).getElement(),
                    new FindUsagesOptions(editor.getProject()), false);
            UsageViewManager.getInstance(project)
                    .showUsages(new UsageTarget[0], usages.toArray(new Usage[0]),
                            presentation);
        }
    }

    private UsageViewPresentation createPresentation(PsiElement psiElement, FindUsagesOptions options,
            boolean toOpenInNewTab) {
        UsageViewPresentation presentation = new UsageViewPresentation();
        String scopeString = options.searchScope.getDisplayName();
        presentation.setScopeText(scopeString);
        String usagesString = options.generateUsagesString();
        presentation.setSearchString(usagesString);
        String title = FindBundle.message("find.usages.of.element.in.scope.panel.title", usagesString,
                UsageViewUtil.getLongName(psiElement), scopeString);
        presentation.setTabText(title);
        presentation.setTabName(FindBundle
                .message("find.usages.of.element.tab.name", usagesString, UsageViewUtil.getShortName(psiElement)));
        presentation.setTargetsNodeText(StringUtil.capitalize(UsageViewUtil.getType(psiElement)));
        presentation.setOpenInNewTab(toOpenInNewTab);
        presentation.setShowCancelButton(true);
        return presentation;
    }
}
