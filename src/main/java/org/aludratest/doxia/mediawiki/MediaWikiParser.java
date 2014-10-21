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

/*
 * Original work from Sebastian Hoss <mail@shoss.de>
 */
package org.aludratest.doxia.mediawiki;

import java.io.StringWriter;

import org.aludratest.doxia.AbstractWikitextParser;
import org.apache.maven.doxia.parser.Parser;
import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;

/**
 * Doxia parser for MediaWiki documents.
 */
@Component(role = Parser.class, hint = "mediawiki")
public class MediaWikiParser extends AbstractWikitextParser {

    @Override
    protected MarkupLanguage createMarkupLanguage() {
        return new DoxiaMediaWikiLanguage();
    }

    @Override
    protected String parseToHtml(String markupContent) {
        MarkupParser parser = createMarkupParser();
        
        StringWriter out = new StringWriter();
        DoxiaHtmlDocumentBuilder builder = new DoxiaHtmlDocumentBuilder(out);
        builder.setPrependImagePrefix("./images/");
        parser.setBuilder(builder);
        parser.parse(markupContent);
        parser.setBuilder(null);
        
        return out.toString();
    }

}
