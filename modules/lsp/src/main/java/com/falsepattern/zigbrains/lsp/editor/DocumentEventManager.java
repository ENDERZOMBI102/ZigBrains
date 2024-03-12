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
package com.falsepattern.zigbrains.lsp.editor;

import com.falsepattern.zigbrains.common.util.ApplicationUtil;
import com.falsepattern.zigbrains.lsp.client.languageserver.wrapper.LanguageServerWrapper;
import com.falsepattern.zigbrains.lsp.utils.FileUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.io.FileUtilRt;
import lombok.val;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DocumentEventManager {
    private final Document document;
    private final DocumentListener documentListener;
    private final TextDocumentSyncKind syncKind;
    private final LanguageServerWrapper wrapper;
    private final TextDocumentIdentifier identifier;
    private int version = -1;
    protected Logger LOG = Logger.getInstance(EditorEventManager.class);

    private final Set<Document> openDocuments = new HashSet<>();

    DocumentEventManager(Document document, DocumentListener documentListener, TextDocumentSyncKind syncKind, LanguageServerWrapper wrapper) {
        this.document = document;
        this.documentListener = documentListener;
        this.syncKind = syncKind;
        this.wrapper = wrapper;
        this.identifier = new TextDocumentIdentifier(FileUtils.documentToUri(document));
    }

    public void removeListeners() {
        document.removeDocumentListener(documentListener);
    }

    public void registerListeners() {
        document.addDocumentListener(documentListener);
    }

    public int getDocumentVersion() {
        return this.version;
    }

    public void documentChanged(DocumentEvent event) {

        DidChangeTextDocumentParams changesParams = new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(),
                Collections.singletonList(new TextDocumentContentChangeEvent()));
        changesParams.getTextDocument().setUri(identifier.getUri());
        changesParams.getTextDocument().setVersion(++version);

        // TODO this incremental update logic is kinda broken, investigate later...
//        if (syncKind == TextDocumentSyncKind.Incremental) {
//            TextDocumentContentChangeEvent changeEvent = changesParams.getContentChanges().get(0);
//            CharSequence newText = event.getNewFragment();
//            int offset = event.getOffset();
//            int newTextLength = event.getNewLength();
//
//            EditorEventManager editorEventManager = EditorEventManagerBase.forUri(FileUtils.documentToUri(document));
//            if (editorEventManager == null) {
//                LOG.warn("no editor associated with document");
//                return;
//            }
//            Editor editor = editorEventManager.editor;
//            Position lspPosition = DocumentUtils.offsetToLSPPos(editor, offset);
//            if (lspPosition == null) {
//                return;
//            }
//            int startLine = lspPosition.getLine();
//            int startColumn = lspPosition.getCharacter();
//            CharSequence oldText = event.getOldFragment();
//
//            //if text was deleted/replaced, calculate the end position of inserted/deleted text
//            int endLine, endColumn;
//            if (oldText.length() > 0) {
//                endLine = startLine + StringUtil.countNewLines(oldText);
//                String content = oldText.toString();
//                String[] oldLines = content.split("\n");
//                int oldTextLength = oldLines.length == 0 ? 0 : oldLines[oldLines.length - 1].length();
//                endColumn = content.endsWith("\n") ? 0 : oldLines.length == 1 ? startColumn + oldTextLength : oldTextLength;
//            } else { //if insert or no text change, the end position is the same
//                endLine = startLine;
//                endColumn = startColumn;
//            }
//            Range range = new Range(new Position(startLine, startColumn), new Position(endLine, endColumn));
//            changeEvent.setRange(range);
//            changeEvent.setText(newText.toString());
//        } else if (syncKind == TextDocumentSyncKind.Full) {
        if (syncKind != TextDocumentSyncKind.None) {
            changesParams.getContentChanges().get(0).setText(document.getText());
        }
//        }
        ApplicationUtil.pool(() -> wrapper.getRequestManager().didChange(changesParams));
    }

    public void documentOpened() {
        if (openDocuments.contains(document)) {
            LOG.warn("trying to send open notification for document which was already opened!");
        } else {
            openDocuments.add(document);
            val theFile = FileDocumentManager.getInstance().getFile(document);
            if (theFile == null)
                return;
            final String extension = FileUtilRt.getExtension(theFile.getName());
            wrapper.getRequestManager().didOpen(new DidOpenTextDocumentParams(new TextDocumentItem(identifier.getUri(),
                    wrapper.serverDefinition.languageIdFor(extension),
                    ++version,
                    document.getText())));
        }
    }

    public void documentClosed() {
        if (!openDocuments.contains(document)) {
            LOG.warn("trying to close document which is not open");
        } else if (EditorEventManagerBase.managersForUri(FileUtils.documentToUri(document)).size() > 1) {
            LOG.warn("trying to close document which is still open in another editor!");
        } else {
            openDocuments.remove(document);
            wrapper.getRequestManager().didClose(new DidCloseTextDocumentParams(identifier));
        }
    }
}
