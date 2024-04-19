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

package com.falsepattern.zigbrains.project.ide.newproject;

import com.intellij.openapi.util.Disposer;
import com.intellij.platform.GeneratorPeerImpl;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

import static com.falsepattern.zigbrains.common.util.dsl.JavaPanel.newPanel;

public class ZigProjectGeneratorPeer extends GeneratorPeerImpl<ZigProjectConfigurationData> {
    private final ZigNewProjectPanel newProjectPanel;

    public ZigProjectGeneratorPeer(boolean handleGit) {
        newProjectPanel = new ZigNewProjectPanel(handleGit);
    }

    @Override
    public @NotNull ZigProjectConfigurationData getSettings() {
        return newProjectPanel.getData();
    }

    @Override
    public @NotNull JComponent getComponent() {
        return newPanel(newProjectPanel::attachPanelTo);
    }

    public void dispose() {
        Disposer.dispose(newProjectPanel);
    }
}
