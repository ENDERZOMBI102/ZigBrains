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

package com.falsepattern.zigbrains.zig.debugger;

import com.falsepattern.zigbrains.zig.debugbridge.DebuggerDriverProvider;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.execution.debugger.backend.DebuggerDriverConfiguration;
import com.jetbrains.cidr.execution.debugger.backend.lldb.LLDBDriverConfiguration;
import lombok.val;
import org.jetbrains.annotations.Nullable;

public class Utils {
    public static @Nullable DebuggerDriverConfiguration getDebuggerConfiguration(Project project) {
        val providedDebugger = DebuggerDriverProvider.findDebuggerConfigurations(project)
                                                     .filter(x -> x instanceof DebuggerDriverConfiguration)
                                                     .map(x -> (DebuggerDriverConfiguration)x)
                                                     .findFirst()
                                                     .orElse(null);
        if (providedDebugger != null)
            return providedDebugger;


        if (LLDBDriverConfiguration.hasBundledLLDB()) {
            Notifications.Bus.notify(new Notification("ZigBrains.Debugger.Warn",
                                                      "Couldn't find a working debug toolchain, using bundled LLDB debugger!",
                                                      NotificationType.WARNING));
            return new LLDBDriverConfiguration();
        } else {
            return null;
        }
    }
}
