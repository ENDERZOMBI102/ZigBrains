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

package com.falsepattern.zigbrains.zig.lsp;

import com.falsepattern.zigbrains.lsp.client.languageserver.serverdefinition.RawCommandServerDefinition;
import lombok.val;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.PublishDiagnosticsCapabilities;

import java.util.List;

public class ZLSServerDefinition extends RawCommandServerDefinition {
    public ZLSServerDefinition(String[] command) {
        super("zig", command);
    }

    @Override
    public void customizeInitializeParams(InitializeParams params) {
        var textCaps = params.getCapabilities().getTextDocument();
        if (textCaps.getPublishDiagnostics() == null) {
            textCaps.setPublishDiagnostics(new PublishDiagnosticsCapabilities());
        }
        val completion = textCaps.getCompletion();
        if (completion != null) {
            val completionItem = completion.getCompletionItem();
            if (completionItem != null) {
                completionItem.setDocumentationFormat(List.of("markdown"));
            }
        }
        val hover = textCaps.getHover();
        if (hover != null) {
            hover.setContentFormat(List.of("markdown"));

        }
    }
}
