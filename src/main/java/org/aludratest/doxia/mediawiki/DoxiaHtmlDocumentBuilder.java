/*
 * Copyright (C) 2010-2014 Hamburg Sud and the contributors.
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
package org.aludratest.doxia.mediawiki;

import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.util.XmlStreamWriter;

public class DoxiaHtmlDocumentBuilder extends HtmlDocumentBuilder {

    private final static String WIKI_URL_PREFIX = "/wiki/";

    private boolean sourceDiv;

    /** An internal structure to be able to append arbitrary attributes to a block being processed, or its parent blocks. */
    private Stack<BlockProcessAttributes> blockAttributes = new Stack<BlockProcessAttributes>();

    public DoxiaHtmlDocumentBuilder(Writer out) {
        super(out);
    }

    public DoxiaHtmlDocumentBuilder(XmlStreamWriter writer) {
        super(writer);
    }

    public DoxiaHtmlDocumentBuilder(Writer out, boolean formatting) {
        super(out, formatting);
    }

    @Override
    public void beginBlock(BlockType type, Attributes attributes) {
        blockAttributes.push(new BlockProcessAttributes(type));
        if (type == BlockType.PREFORMATTED && !sourceDiv) {
            Attributes srcAttr = new Attributes();
            srcAttr.setCssClass("source");
            super.beginBlock(BlockType.DIV, srcAttr);
            sourceDiv = true;
        }
        if (type == BlockType.TABLE) {
            attributes.appendCssClass("bodyTable");
            blockAttributes.peek().setAttribute("tableRowIndex", Integer.valueOf(0));
        }
        if (type == BlockType.TABLE_ROW) {
            BlockProcessAttributes attrs = getFirstAncestorAttributes(BlockType.TABLE);
            if (attrs != null) {
                Integer rowIndex = (Integer) attrs.getAttribute("tableRowIndex");
                if (rowIndex != null) {
                    attributes.appendCssClass(rowIndex.intValue() % 2 == 0 ? "a" : "b");
                    attrs.setAttribute("tableRowIndex", Integer.valueOf(rowIndex.intValue() + 1));
                }
            }
        }

        super.beginBlock(type, attributes);
    }

    @Override
    public void endBlock() {
        super.endBlock();
        blockAttributes.pop();

        // also end the DIV block, if preformatted
        if (sourceDiv) {
            super.endBlock();
            sourceDiv = false;
        }
    }

    @Override
    protected String makeUrlAbsolute(String url) {
        if (url.startsWith(WIKI_URL_PREFIX)) {
            String pageTitle = url.substring(WIKI_URL_PREFIX.length()).toLowerCase(Locale.US);
            // replace underscores (spaces in page title) with dashes
            pageTitle = pageTitle.replace('_', '-');
            return "./" + pageTitle + ".html";
        }
        else {
            return super.makeUrlAbsolute(url);
        }
    }

    private BlockProcessAttributes getFirstAncestorAttributes(BlockType blockType) {
        for (int i = blockAttributes.size() - 1; i >= 0; i--) {
            if (blockAttributes.get(i).blockType == blockType) {
                return blockAttributes.get(i);
            }
        }
        return null;
    }

    private static class BlockProcessAttributes {

        private BlockType blockType;

        private Map<String, Object> attributes;

        public BlockProcessAttributes(BlockType blockType) {
            this.blockType = blockType;
        }

        public void setAttribute(String key, Object value) {
            if (attributes == null) {
                attributes = new HashMap<String, Object>();
            }
            attributes.put(key, value);
        }

        public Object getAttribute(String key) {
            if (attributes == null) {
                return null;
            }
            return attributes.get(key);
        }

    }

}
