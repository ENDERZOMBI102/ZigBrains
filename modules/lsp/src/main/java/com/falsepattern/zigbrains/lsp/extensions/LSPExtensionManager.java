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
package com.falsepattern.zigbrains.lsp.extensions;

import com.falsepattern.zigbrains.lsp.client.ClientContext;
import com.falsepattern.zigbrains.lsp.client.DefaultLanguageClient;
import com.falsepattern.zigbrains.lsp.client.languageserver.ServerOptions;
import com.falsepattern.zigbrains.lsp.client.languageserver.requestmanager.DefaultRequestManager;
import com.falsepattern.zigbrains.lsp.client.languageserver.requestmanager.RequestManager;
import com.falsepattern.zigbrains.lsp.client.languageserver.wrapper.LanguageServerWrapper;
import com.falsepattern.zigbrains.lsp.contributors.icon.LSPDefaultIconProvider;
import com.falsepattern.zigbrains.lsp.contributors.icon.LSPIconProvider;
import com.falsepattern.zigbrains.lsp.contributors.label.LSPDefaultLabelProvider;
import com.falsepattern.zigbrains.lsp.contributors.label.LSPLabelProvider;
import com.falsepattern.zigbrains.lsp.editor.EditorEventManager;
import com.falsepattern.zigbrains.lsp.listeners.LSPCaretListenerImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.psi.PsiFile;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.jetbrains.annotations.NotNull;

public interface LSPExtensionManager {

    /**
     * LSP allows you to provide custom(language-specific) {@link RequestManager} implementations.
     * Request manager implementation is required to be modified in such situations where,
     *
     * <ul>
     * <li> Adding support for custom LSP requests/notifications which are not part of the standard LS protocol.</li>
     * <li> Default handling process of LSP requests/notifications is required to be customized.
     * </ul>
     * <p>
     * As a starting point you can extend
     * {@link DefaultRequestManager}.
     */
   RequestManager getExtendedRequestManagerFor(LanguageServerWrapper wrapper,
                                               LanguageServer server,
                                               LanguageClient client,
                                               ServerCapabilities serverCapabilities);

    /**
     * LSP allows you to provide custom {@link EditorEventManager} implementations.
     * Editor event manager implementation is required to be modified in such situations where,
     *
     * <ul>
     * <li> Modifying/optimizing lsp features for custom requirements.
     * </ul>
     * <p>
     * As a starting point you can extend
     * {@link EditorEventManager}.
     */
    EditorEventManager getExtendedEditorEventManagerFor(Editor editor,
                                                        DocumentListener documentListener,
                                                        LSPCaretListenerImpl caretListener,
                                                        RequestManager requestManager,
                                                        ServerOptions serverOptions,
                                                        LanguageServerWrapper wrapper);

    /**
     * LSP allows you to provide extended/custom {@link LanguageServer} interfaces, if required.
     */
    Class<? extends LanguageServer> getExtendedServerInterface();

    /**
     * LSP allows you to provide custom(language-specific) {@link LanguageClient} implementations.
     * Language client is required to be modified in such situations where,
     * <ul>
     * <li> Adding support for custom client notifications which are not part of the standard LS protocol.</li>
     * <li> Default handling process of LSP client requests/notifications is required to be customized.
     * </ul>
     * <p>
     * As a starting point you can extend
     * {@link DefaultLanguageClient}.
     */
    LanguageClient getExtendedClientFor(ClientContext context);

    /**
     * The icon provider for the Language Server. Override and implement your own or extend the
     * {@link LSPDefaultIconProvider} to customize the default icons.
     */
    @NotNull
    default LSPIconProvider getIconProvider() {
        return new LSPDefaultIconProvider();
    }

    /**
     * Some language servers might only need to start for files which has a specific content. This method can be used
     * in such situation to control whether the file must be connected to a language server which is registered for the
     * extension of this file.
     *
     * <b>Note:</b> By default this method returns <code>true</code>
     *
     * @param file PsiFile which is about to connect to a language server.
     * @return <code>true</code> if the file is supported.
     */
    default boolean isFileContentSupported(@NotNull PsiFile file) {
        return true;
    }

    /**
     * The label provider for the Language Server. Implement and override default behavior
     * if it needs to be customize.
     */
    @NotNull
    default LSPLabelProvider getLabelProvider() {
        return new LSPDefaultLabelProvider();
    }

}
