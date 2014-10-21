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

import javax.inject.Inject;

import org.apache.maven.doxia.module.site.AbstractSiteModule;
import org.apache.maven.doxia.module.site.SiteModule;
import org.codehaus.plexus.component.annotations.Component;

/**
 * <p>
 * Doxia site module for MediaWiki sources. All sources must be located under the <em>mediawiki</em> folder and have an
 * <code>mediawiki</code> file extension.
 * </p>
 */
@SuppressWarnings("deprecation")
@Component(role = SiteModule.class, hint = "mediawiki")
public class MediaWikiSiteModule extends AbstractSiteModule {

    /** Folder prefix for MediaWiki sources. */
    public static final String SOURCE_DIRECTORY = "mediawiki";

    /** File extension for MediaWiki sources. */
    public static final String FILE_EXTENSION   = MediaWikiSiteModule.SOURCE_DIRECTORY;

    /** ID of the MediaWiki parser. */
    public static final String PARSER_ID        = MediaWikiSiteModule.SOURCE_DIRECTORY;

    /**
     * Constructor for a new Maven site module, configured for MediaWiki sources.
     */
    @Inject
    public MediaWikiSiteModule() {
        super(MediaWikiSiteModule.SOURCE_DIRECTORY, MediaWikiSiteModule.FILE_EXTENSION,
                MediaWikiSiteModule.PARSER_ID);
    }

}
