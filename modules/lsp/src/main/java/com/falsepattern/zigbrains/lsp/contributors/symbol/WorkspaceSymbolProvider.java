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
package com.falsepattern.zigbrains.lsp.contributors.symbol;

import com.falsepattern.zigbrains.common.util.FileUtil;
import com.falsepattern.zigbrains.lsp.IntellijLanguageClient;
import com.falsepattern.zigbrains.lsp.client.languageserver.ServerStatus;
import com.falsepattern.zigbrains.lsp.client.languageserver.requestmanager.RequestManager;
import com.falsepattern.zigbrains.lsp.client.languageserver.serverdefinition.LanguageServerDefinition;
import com.falsepattern.zigbrains.lsp.client.languageserver.wrapper.LanguageServerWrapper;
import com.falsepattern.zigbrains.lsp.contributors.icon.LSPIconProvider;
import com.falsepattern.zigbrains.lsp.contributors.label.LSPLabelProvider;
import com.falsepattern.zigbrains.lsp.requests.Timeouts;
import com.falsepattern.zigbrains.lsp.utils.FileUtils;
import com.falsepattern.zigbrains.lsp.utils.GUIUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolTag;
import org.eclipse.lsp4j.WorkspaceSymbol;
import org.eclipse.lsp4j.WorkspaceSymbolParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The workspace symbole provider implementation based on LSP
 *
 * @author gayanper
 */
public class WorkspaceSymbolProvider {

  private static final Logger LOG = Logger.getInstance(WorkspaceSymbolProvider.class);

  public List<LSPNavigationItem> workspaceSymbols(String name, Project project) {
    final Set<LanguageServerWrapper> serverWrappers = IntellijLanguageClient
        .getProjectToLanguageWrappers()
        .getOrDefault(FileUtils.projectToUri(project), Collections.emptySet());

    final WorkspaceSymbolParams symbolParams = new WorkspaceSymbolParams(name);
    return serverWrappers.stream().filter(s -> s.getStatus() == ServerStatus.INITIALIZED)
        .flatMap(server -> collectSymbol(server, server.getRequestManager(), symbolParams))
        .map(s -> createNavigationItem(s, project)).filter(Objects::nonNull).collect(Collectors.toList());
  }

  private LSPNavigationItem createNavigationItem(LSPSymbolResult result, Project project) {
    final SymbolInformation information = (result.getSymbolInformation() != null) ?
            result.getSymbolInformation() : from(result.getWorkspaceSymbol());
    final Location location = information.getLocation();
    final VirtualFile file = FileUtil.virtualFileFromURI(location.getUri());

    if (file != null) {
      final LSPIconProvider iconProviderFor = GUIUtils.getIconProviderFor(result.getDefinition());
      final LSPLabelProvider labelProvider = GUIUtils.getLabelProviderFor(result.getDefinition());
      return new LSPNavigationItem(labelProvider.symbolNameFor(information, project),
              labelProvider.symbolLocationFor(information, project), iconProviderFor.getSymbolIcon(information),
              project, file,
              location.getRange().getStart().getLine(),
              location.getRange().getStart().getCharacter());
    } else {
      return null;
    }
  }

  private SymbolInformation from(WorkspaceSymbol symbol) {
    SymbolInformation information = new SymbolInformation();
    information.setContainerName(symbol.getContainerName());
    information.setKind(symbol.getKind());
    information.setName(symbol.getName());
    if(symbol.getLocation().isLeft()) {
      information.setLocation(symbol.getLocation().getLeft());
    } else {
      information.setLocation(new Location());
      information.getLocation().setUri(symbol.getLocation().getRight().getUri());
    }
    information.setTags(symbol.getTags());
    if(symbol.getTags() != null) {
      information.setDeprecated(symbol.getTags().contains(SymbolTag.Deprecated));
    }
    return information;
  }

  @SuppressWarnings("squid:S2142")
  private Stream<LSPSymbolResult> collectSymbol(LanguageServerWrapper wrapper,
      RequestManager requestManager,
      WorkspaceSymbolParams symbolParams) {
    final CompletableFuture<Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>>> request = requestManager
        .symbol(symbolParams);

    if (request == null) {
      return Stream.empty();
    }

    try {
      Either<List<? extends SymbolInformation>, List<? extends WorkspaceSymbol>> symbolInformations = request
          .get(20000, TimeUnit.MILLISECONDS);
      wrapper.notifySuccess(Timeouts.SYMBOLS);
      if(symbolInformations.isLeft()) {
        return symbolInformations.getLeft().stream().map(si -> new LSPSymbolResult(si, wrapper.getServerDefinition()));
      } else if (symbolInformations.isRight()) {
        return symbolInformations.getRight().stream().map(si -> new LSPSymbolResult(si, wrapper.getServerDefinition()));
      }
    } catch (TimeoutException e) {
      LOG.warn(e);
      wrapper.notifyFailure(Timeouts.SYMBOLS);
    } catch (ExecutionException | InterruptedException e) {
      LOG.warn(e);
      wrapper.crashed(e);
    }
    return Stream.empty();
  }

  private static class LSPSymbolResult {

    private SymbolInformation symbolInformation;
    private WorkspaceSymbol workspaceSymbol;
    private LanguageServerDefinition definition;

    public LSPSymbolResult(SymbolInformation symbolInformation,
        LanguageServerDefinition definition) {
      this.symbolInformation = symbolInformation;
      this.definition = definition;
    }

    public LSPSymbolResult(WorkspaceSymbol workspaceSymbol,
                           LanguageServerDefinition definition) {
      this.workspaceSymbol = workspaceSymbol;
      this.definition = definition;
    }

    public SymbolInformation getSymbolInformation() {
      return symbolInformation;
    }

    public LanguageServerDefinition getDefinition() {
      return definition;
    }

    public WorkspaceSymbol getWorkspaceSymbol() {
      return workspaceSymbol;
    }
  }
}
