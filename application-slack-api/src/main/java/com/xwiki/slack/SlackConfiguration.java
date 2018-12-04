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
package com.xwiki.slack;

import java.util.Arrays;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.SpaceReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;

/**
 * The component used to access the Slack configuration.
 * 
 * @version $Id$
 */
@Component(roles = SlackConfiguration.class)
@Singleton
public class SlackConfiguration
{
    @Inject
    private Provider<XWikiContext> xcontextProvider;

    private Object getPropertyValue(String propertyName) throws XWikiException
    {
        XWikiContext context = xcontextProvider.get();
        SpaceReference spaceReference = new SpaceReference("xwiki", Arrays.asList("Slack", "Code"));

        DocumentReference configReference = new DocumentReference("SlackConfiguration", spaceReference);
        XWikiDocument configDoc = context.getWiki().getDocument(configReference, context);
        DocumentReference configClassReference = new DocumentReference("SlackConfigurationClass", spaceReference);
        BaseObject slackObj = configDoc.getXObject(configClassReference);
        BaseProperty<?> slackProperty = (BaseProperty<?>) slackObj.get(propertyName);
        return slackProperty.getValue();
    }

    /**
     * Check if Slack is enabled in XWiki.
     * 
     * @return true if Slack is enabled, false otherwise
     * @throws XWikiException whenever an exception occurs
     */
    public boolean isEnabled() throws XWikiException
    {
        return getPropertyValue("enable").equals(1);
    }

    /**
     * Retrieve the Webhook URL of the Slack channel.
     * 
     * @return the Webhook URL of the Slack channel
     * @throws XWikiException whenever an exception occurs
     */
    public String getWebhookUrl() throws XWikiException
    {
        return getPropertyValue("webhookUrl").toString();
    }
}
