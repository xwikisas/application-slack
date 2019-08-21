/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.xwiki.slack.internal;

import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.internal.AbstractDocumentConfigurationSource;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.LocalDocumentReference;
import org.xwiki.model.reference.SpaceReference;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;

/**
 * {@link org.xwiki.configuration.ConfigurationSource} reading the values from the configuration page on the main wiki.
 *
 * @version $Id$
 */
@Component
@Named("slack")
@Singleton
public class SlackConfigurationSource extends AbstractDocumentConfigurationSource
{

    private static final List<String> SPACE_NAMES = Arrays.asList("Slack", "Code");

    private static final LocalDocumentReference CLASS_REFERENCE =
        new LocalDocumentReference(SPACE_NAMES, "SlackConfigurationClass");

    @Override
    protected DocumentReference getDocumentReference()
    {
        XWikiContext xcontext = xcontextProvider.get();
        XWikiDocument currentDoc = xcontext.getDoc();
        SpaceReference lastSpaceRef = currentDoc.getDocumentReference().getLastSpaceReference();

        while (lastSpaceRef.getType() == EntityType.SPACE) {
            DocumentReference localConfigDocRef = new DocumentReference("WebPreferences", lastSpaceRef);
            try {
                XWikiDocument localConfigDoc = xcontext.getWiki().getDocument(localConfigDocRef, xcontext);
                if (localConfigDoc != null && localConfigDoc.getXObject(CLASS_REFERENCE) != null) {
                    return localConfigDocRef;
                }
            } catch (XWikiException e) {
                logger.error("Failed to retrieve the document for the reference [{}].", localConfigDocRef, e);
                return null;
            }
            if (lastSpaceRef.getParent().getType() == EntityType.SPACE) {
                lastSpaceRef = new SpaceReference(lastSpaceRef.getParent());
            }
            break;
        }

        return null;
    }

    @Override
    protected LocalDocumentReference getClassReference()
    {
        return CLASS_REFERENCE;
    }

    @Override
    protected String getCacheId()
    {
        return "configuration.document.slack";
    }

}
