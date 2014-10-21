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
import java.util.Locale;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.builder.HtmlDocumentBuilder;
import org.eclipse.mylyn.wikitext.core.util.XmlStreamWriter;

public class DoxiaHtmlDocumentBuilder extends HtmlDocumentBuilder {

    private final static String WIKI_URL_PREFIX = "/wiki/";

    private boolean sourceDiv;

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
        if (type == BlockType.PREFORMATTED && !sourceDiv) {
            Attributes srcAttr = new Attributes();
            srcAttr.setCssClass("source");
            super.beginBlock(BlockType.DIV, srcAttr);
            sourceDiv = true;
        }
        super.beginBlock(type, attributes);
    }

    @Override
    public void endBlock() {
        super.endBlock();
        // also end the DIV block, if preformatted
        if (sourceDiv) {
            super.endBlock();
            sourceDiv = false;
        }
    }

    @Override
    protected String makeUrlAbsolute(String url) {
        // if url is in /wiki/XY syntax, convert to ./XY.html
        if (url.startsWith(WIKI_URL_PREFIX)) {
            return "./" + url.substring(WIKI_URL_PREFIX.length()).toLowerCase(Locale.US) + ".html";
        }
        else {
            return super.makeUrlAbsolute(url);
        }
    }

}
