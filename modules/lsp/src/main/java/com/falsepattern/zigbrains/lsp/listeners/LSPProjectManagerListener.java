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
package com.falsepattern.zigbrains.lsp.listeners;

import com.falsepattern.zigbrains.lsp.IntellijLanguageClient;
import com.falsepattern.zigbrains.lsp.client.languageserver.wrapper.LanguageServerWrapper;
import com.falsepattern.zigbrains.lsp.utils.FileUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class LSPProjectManagerListener implements ProjectManagerListener {

    private static final Logger LOG = Logger.getInstance(LSPProjectManagerListener.class);

    @Override
    public void projectClosing(@NotNull Project project) {
            Set<LanguageServerWrapper> languageServerWrappers = IntellijLanguageClient.getAllServerWrappersFor(FileUtils.projectToUri(project));
            languageServerWrappers.forEach(wrapper -> {
                wrapper.stop(false);
                IntellijLanguageClient.removeWrapper(wrapper);
            });
    }
}
