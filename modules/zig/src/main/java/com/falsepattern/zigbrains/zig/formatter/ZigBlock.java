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

package com.falsepattern.zigbrains.zig.formatter;

import com.intellij.formatting.Alignment;
import com.intellij.formatting.Block;
import com.intellij.formatting.Indent;
import com.intellij.formatting.Spacing;
import com.intellij.formatting.SpacingBuilder;
import com.intellij.formatting.Wrap;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.formatter.common.AbstractBlock;
import com.intellij.psi.tree.IElementType;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.falsepattern.zigbrains.zig.psi.ZigTypes.BLOCK;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.CONTAINER_DECL_AUTO;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.CONTAINER_DECL_TYPE;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.CONTAINER_DOC_COMMENT;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.EXPR_LIST;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.FN_CALL_ARGUMENTS;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.INIT_LIST;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.LBRACE;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.LPAREN;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.RBRACE;
import static com.falsepattern.zigbrains.zig.psi.ZigTypes.RPAREN;

public class ZigBlock extends AbstractBlock {

    private final SpacingBuilder spacingBuilder;

    protected ZigBlock(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, SpacingBuilder spacingBuilder) {
        super(node, wrap, alignment);
        this.spacingBuilder = spacingBuilder;
    }


    @Override
    protected List<Block> buildChildren() {
        var blocks = new ArrayList<Block>();
        var child = myNode.getFirstChildNode();
        while (child != null) {
            if (child.getElementType() != TokenType.WHITE_SPACE) {
                var block = new ZigBlock(child, null, null, spacingBuilder);
                blocks.add(block);
            }
            child = child.getTreeNext();
        }

        return blocks;
    }

    @Override
    public @Nullable Spacing getSpacing(@Nullable Block block, @NotNull Block block1) {
        return null;
    }

    @Override
    public boolean isLeaf() {
        return myNode.getFirstChildNode() == null;
    }

    @Override
    protected @Nullable Indent getChildIndent() {
        return getIndentBasedOnParentType(getNode().getElementType(), null);
    }

    @Override
    public Indent getIndent() {
        val parent = getNode().getTreeParent();
        if (parent != null) {
            return getIndentBasedOnParentType(parent.getElementType(), getNode().getElementType());
        }
        return Indent.getNoneIndent();
    }

    private static Indent getIndentBasedOnParentType(IElementType parentElementType, IElementType childElementType) {
        if ((parentElementType == BLOCK && childElementType != LBRACE && childElementType != RBRACE) ||
            (parentElementType == INIT_LIST && childElementType != LBRACE && childElementType != RBRACE) ||
            parentElementType == EXPR_LIST ||
            (parentElementType == FN_CALL_ARGUMENTS && childElementType != LPAREN && childElementType != RPAREN) ||
            (parentElementType == CONTAINER_DECL_AUTO && childElementType != CONTAINER_DECL_TYPE && childElementType != LBRACE && childElementType != CONTAINER_DOC_COMMENT && childElementType != RBRACE)) {
            return Indent.getNormalIndent();
        }
        return Indent.getNoneIndent();
    }
}
