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

package com.falsepattern.zigbrains.project.execution;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.CapturingAnsiEscapesAwareProcessHandler;
import com.intellij.util.io.BaseOutputReader;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ZigCapturingProcessHandler extends CapturingAnsiEscapesAwareProcessHandler {
    public static Optional<ZigCapturingProcessHandler> startProcess(GeneralCommandLine commandLine) {
        try {
            return Optional.of(new ZigCapturingProcessHandler(commandLine));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public ZigCapturingProcessHandler(@NotNull GeneralCommandLine commandLine) throws ExecutionException {
        super(commandLine);
    }

    @Override
    protected BaseOutputReader.@NotNull Options readerOptions() {
        return BaseOutputReader.Options.BLOCKING;
    }
}
