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

package com.falsepattern.zigbrains.zig.settings;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
@AllArgsConstructor
public final class ZLSSettings {
    public @Nullable String zlsPath;
    public @NotNull String zlsConfigPath;
    public boolean increaseTimeouts;
    public boolean asyncFolding;
    public boolean debug;
    public boolean messageTrace;
    public boolean buildOnSave;
    public @NotNull String buildOnSaveStep;
    public boolean highlightGlobalVarDeclarations;
    public boolean dangerousComptimeExperimentsDoNotEnable;

    public ZLSSettings() {
        this(null, "", false, true, false, false, false, "install", false, false);
    }
}
