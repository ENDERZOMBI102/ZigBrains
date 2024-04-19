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

package com.falsepattern.zigbrains.project.openapi.components;

import com.falsepattern.zigbrains.project.ide.project.ZigProjectSettingsPanel;
import com.falsepattern.zigbrains.project.toolchain.AbstractZigToolchain;
import com.falsepattern.zigbrains.zig.lsp.ZLSStartupActivity;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

@Service(Service.Level.PROJECT)
@State(
        name = "ZigProjectSettings",
        storages = @Storage("zigbrains.xml")
)
public final class ZigProjectSettingsService extends AbstractZigProjectSettingsService<ZigProjectSettings> {
    public ZigProjectSettingsService(Project project) {
        super(project, new ZigProjectSettings());
    }

    public static ZigProjectSettingsService getInstance(Project project) {
        return project.getService(ZigProjectSettingsService.class);
    }

    public boolean isModified(ZigProjectSettings otherData) {
        val myData = getState();
        return !Objects.equals(myData.toolchainHomeDirectory, otherData.toolchainHomeDirectory) ||
               !Objects.equals(myData.explicitPathToStd, otherData.explicitPathToStd);
    }
}
