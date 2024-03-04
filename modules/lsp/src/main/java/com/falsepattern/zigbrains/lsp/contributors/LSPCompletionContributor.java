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
package com.falsepattern.zigbrains.lsp.contributors;

import com.falsepattern.zigbrains.lsp.editor.EditorEventManager;
import com.falsepattern.zigbrains.lsp.editor.EditorEventManagerBase;
import com.falsepattern.zigbrains.lsp.utils.DocumentUtils;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.PlainPrefixMatcher;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.util.ProcessingContext;
import org.eclipse.lsp4j.Position;
import org.jetbrains.annotations.NotNull;

/**
 * The completion contributor for the LSP
 */
class LSPCompletionContributor extends CompletionContributor {
    private static final Logger LOG = Logger.getInstance(LSPCompletionContributor.class);

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        CompletionProvider<CompletionParameters> provider = new CompletionProvider<>() {
            @Override
            protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context, @NotNull CompletionResultSet result) {
                try {
                    Editor editor = parameters.getEditor();
                    int offset = parameters.getOffset();
                    Position serverPos = DocumentUtils.offsetToLSPPos(editor, offset);

                    EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
                    if (manager != null) {
                        result.addAllElements(manager.completion(serverPos));
                    }
                } catch (Exception e) {
                    LOG.warn("LSP Completions ended with an error", e);
                }
            }
        };

        Editor editor = parameters.getEditor();
        int offset = parameters.getOffset();

        EditorEventManager manager = EditorEventManagerBase.forEditor(editor);
        if (manager != null) {
            String prefix = manager.getCompletionPrefix(editor, offset);

            provider.addCompletionVariants(parameters, new ProcessingContext(), result.withPrefixMatcher(new PlainPrefixMatcher(prefix)));
            if (result.isStopped()) {
                return;
            }

            super.fillCompletionVariants(parameters, result);
        }
    }
}
