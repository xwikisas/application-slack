
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

import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.bridge.event.DocumentUpdatedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.rendering.syntax.Syntax;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.licensing.Licensor;
import com.xwiki.slack.SlackClient;
import com.xwiki.slack.SlackConfiguration;

/**
 * Listens to document events and pushes notifications to Slack.
 * 
 * @version $Id$
 */
@Component
@Named(DocumentListener.NAME)
@Singleton
public class DocumentListener extends AbstractEventListener
{
    /**
     * The event listener name.
     */
    public static final String NAME = "slackDocumentListener";

    private static final String ACTION_VIEW = "view";

    private static final String FORMAT_LINK = "<%s|%s>";

    @Inject
    private Licensor licensor;

    @Inject
    private Logger logger;

    @Inject
    private SlackClient slack;

    @Inject
    private SlackConfiguration configuration;

    /**
     * Default constructor.
     */
    public DocumentListener()
    {
        super(NAME, Arrays.asList(new DocumentCreatedEvent(), new DocumentUpdatedEvent(), new DocumentDeletedEvent()));
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        XWikiDocument doc = (XWikiDocument) source;
        XWikiContext context = (XWikiContext) data;

        // Skip if there is no valid license has expired.
        DocumentReference mainPageReference =
            new DocumentReference(context.getMainXWiki(), Arrays.asList("Slack", "Code"), "SlackConfigurationClass");
        if (!licensor.hasLicensure(mainPageReference)) {
            logger.warn("Skipping notification sending for event [{}] by user [{}] on document [{}]. "
                + "No valid Slack license has been found. Please visit the 'Licenses' section in Administration.",
                event.getClass().getName(), context.getUserReference(), doc.getDocumentReference());
            return;
        }

        if (source instanceof XWikiDocument && data instanceof XWikiContext) {
            try {
                if (this.configuration.isEnabled()) {
                    XWikiDocument document = (XWikiDocument) source;
                    XWikiContext xcontext = (XWikiContext) data;
                    String message =
                        String.format("%s was %s by %s%s", getNotificationDocument(event, document, xcontext),
                            getNotificationAction(event), getNotificationAuthor(event, document, xcontext),
                            this.slack.encode(getNotificationComment(document)));
                    String webhookURL = this.configuration.getWebhookUrl();
                    try {
                        this.slack.postMessage(message, webhookURL);
                    } catch (IOException e) {
                        this.logger.warn("Faild to post message to Slack.", e);
                    }
                }
            } catch (XWikiException e) {
                this.logger.warn("Faild to read Slack configuration", e);
            }
        }
    }

    private String getNotificationDocument(Event event, XWikiDocument document, XWikiContext xcontext)
    {
        String url = getNotificationURL(event, document, xcontext);
        if (url != null) {
            String title = document.getRenderedTitle(Syntax.PLAIN_1_0, xcontext);
            return String.format(FORMAT_LINK, url, this.slack.encode(title));
        } else {
            return document.getDocumentReference().toString();
        }
    }

    /**
     * Get the author name that we want to print in the notification message we send.
     *
     * @param event the event that happened. We need it to handle creation & modification differently than deletion
     * @param document the document that has been modified, created or deleted. We need to extract the author for
     *            creations and modifications
     * @param xcontext the XWiki Context from which we extract the current user for deletions
     * @return the author name
     */
    private String getNotificationAuthor(Event event, XWikiDocument document, XWikiContext xcontext)
    {
        String user;

        DocumentReference userReference;
        if (event instanceof DocumentDeletedEvent) {
            userReference = xcontext.getUserReference();
        } else {
            userReference = document.getAuthorReference();
        }

        if (userReference != null) {
            if (xcontext.getURLFactory() != null) {
                try {
                    user = String.format(FORMAT_LINK,
                        xcontext.getWiki().getExternalURL(userReference.toString(), ACTION_VIEW, xcontext),
                        this.slack.encode(xcontext.getWiki().getPlainUserName(userReference, xcontext)));
                } catch (XWikiException e) {
                    user = this.slack.encode(userReference.toString());
                }
            } else {
                user = this.slack.encode(userReference.toString());
            }
        } else {
            user = "Guest";
        }

        return user;
    }

    /**
     * Get the action on the page (created, deleted, modified).
     *
     * @param event the XWiki Document event
     * @return the action (e.g. "created")
     */
    private String getNotificationAction(Event event)
    {
        String action;
        if (event instanceof DocumentDeletedEvent) {
            action = "deleted";
        } else if (event instanceof DocumentCreatedEvent) {
            action = "created";
        } else {
            action = "modified";
        }
        return action;
    }

    /**
     * Get a comment part that we want to print in the notification message we send.
     *
     * @param source the source document from the event
     * @return the comment part
     */
    private String getNotificationComment(XWikiDocument source)
    {
        String comment;
        if (!StringUtils.isEmpty(source.getComment())) {
            comment = String.format(" (%s)", source.getComment());
        } else {
            comment = "";
        }
        return comment;
    }

    /**
     * Get the URL that we want to print in the notification message we send.
     *
     * @param event the XWiki Document event
     * @param source the source document from the Document event
     * @param xcontext the XWiki Context that we use to compute the external URL
     * @return the notification URL
     * @throws IRCBotException if we cannot access the XWikiContext
     */
    private String getNotificationURL(Event event, XWikiDocument source, XWikiContext xcontext)
    {
        String url = null;
        String queryString = null;

        try {
            if (!(event instanceof DocumentCreatedEvent || event instanceof DocumentDeletedEvent)) {
                // Return a diff URL since the action done was a modification
                queryString = String.format("viewer=changes&amp;rev2=%s", source.getVersion());
            }
            // Handle the case when no URL Factory is set up yet. This could happen for example when a mandatory class
            // is created at startup since that's done before the URL Factory is defined.
            if (xcontext.getURLFactory() != null) {
                url = source.getExternalURL(ACTION_VIEW, queryString, xcontext);
            }
        } catch (Exception e) {
            // Ensures that an error in computing the URL won't prevent sending a message on the IRC channel
            this.logger.warn("Failed to compute URL for document [{}] and query string [{}]",
                source.getDocumentReference(), queryString, e);
        }

        return url;
    }
}
