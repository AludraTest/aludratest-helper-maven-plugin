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
package org.aludratest.doxia;

import java.io.IOException;
import java.io.Reader;

import org.apache.maven.doxia.parser.AbstractTextParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.sink.Sink;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;

import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;

/**
 * Doxia parser based on Wikitext.
 */
public abstract class AbstractWikitextParser extends AbstractTextParser {

    @Override
    public void parse(final Reader reader, final Sink sink) throws ParseException {
        final String markupContent = readMarkupContent(reader);
        final String htmlContent = parseToHtml(markupContent);

        sink.rawText(htmlContent);
        sink.flush();
        sink.close();
    }

    private static String readMarkupContent(final Reader reader) throws ParseException {
        try {
            return CharStreams.toString(reader);
        } catch (final IOException exception) {
            throw new ParseException("Cannot read input", exception);
        }
        finally {
            try {
                reader.close();
            }
            catch (IOException e) {
            }
        }
    }

    protected String parseToHtml(final String markupContent) {
        Preconditions.checkNotNull(markupContent, "Cannot parse NULL Textile content to HTML!");
        Preconditions.checkArgument(!markupContent.isEmpty(), "Cannot parse empty Textile content to HTML!");

        return createMarkupParser().parseToHtml(markupContent);
    }

    protected final MarkupParser createMarkupParser() {
        final MarkupParser markupParser = new MarkupParser();
        markupParser.setMarkupLanguage(createMarkupLanguage());

        return markupParser;
    }

    protected abstract MarkupLanguage createMarkupLanguage();

}
