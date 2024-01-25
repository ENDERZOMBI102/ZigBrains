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

package com.falsepattern.zigbrains.zig.parser;

import com.falsepattern.zigbrains.zig.psi.ZigTypes;
import com.intellij.psi.tree.TokenSet;

public interface ZigTokenSets {
    TokenSet COMMENTS = TokenSet.create(ZigTypes.LINE_COMMENT, ZigTypes.DOC_COMMENT, ZigTypes.CONTAINER_DOC_COMMENT);
    TokenSet STRINGS = TokenSet.create(ZigTypes.STRING_LITERAL);
}
