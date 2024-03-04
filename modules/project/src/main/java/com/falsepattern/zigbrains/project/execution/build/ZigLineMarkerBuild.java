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

package com.falsepattern.zigbrains.project.execution.build;

import com.falsepattern.zigbrains.project.execution.base.ZigTopLevelLineMarkerBase;
import com.falsepattern.zigbrains.zig.psi.ZigTypes;
import com.falsepattern.zigbrains.zig.util.PsiUtil;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class ZigLineMarkerBuild extends ZigTopLevelLineMarkerBase {
    public static final ZigLineMarkerBuild UTILITY_INSTANCE = new ZigLineMarkerBuild();
    @Override
    protected @Nullable PsiElement getDeclaration(@NotNull PsiElement element) {
        if (PsiUtil.getElementType(element) != ZigTypes.IDENTIFIER) {
            return null;
        }
        if (!element.textMatches("build")) {
            return null;
        }
        var parent = element.getParent();
        if (PsiUtil.getElementType(parent) != ZigTypes.FN_PROTO) {
            return null;
        }

        var file = element.getContainingFile();
        if (file == null) {
            return null;
        }
        var fileName = file.getVirtualFile().getName();
        if (!"build.zig".equals(fileName)) {
            return null;
        }
        return parent.getParent();
    }

    @Override
    protected @NotNull Icon getIcon(@NotNull PsiElement element) {
        return AllIcons.RunConfigurations.TestState.Run;
    }
}
