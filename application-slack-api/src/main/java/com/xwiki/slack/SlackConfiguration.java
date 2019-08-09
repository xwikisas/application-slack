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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.xwiki.bridge.event.DocumentCreatedEvent;
import org.xwiki.bridge.event.DocumentDeletedEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.observation.event.Event;

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
    @Named("slack")
    private ConfigurationSource configuration;

    /**
     * Check if Slack is enabled in XWiki.
     * 
     * @return true if Slack is enabled, false otherwise
     */
    public boolean isEnabled()
    {
        return configuration.getProperty("enabled", 1) == 1;
    }

    /**
     * Retrieve the Webhook URL of the Slack channel.
     * 
     * @return the Webhook URL of the Slack channel
     */
    public String getWebhookUrl()
    {
        return configuration.getProperty("channelUrl");
    }

    /**
     * Check if the current event type is enabled in XWiki.
     * 
     * @param event the listened event
     * @return true if the current event type is enabled, false otherwise
     */
    public boolean isEventEnabled(Event event)
    {
        boolean isEventEnabled;
        if (event instanceof DocumentCreatedEvent) {
            isEventEnabled = configuration.getProperty("documentCreated", 1) == 1;
        } else if (event instanceof DocumentDeletedEvent) {
            isEventEnabled = configuration.getProperty("documentDeleted", 1) == 1;
        } else {
            isEventEnabled = configuration.getProperty("documentUpdated", 1) == 1;
        }
        return isEventEnabled;
    }

    /**
     * Check if the configuration source is not empty.
     * If a key is detected, a page with an object of configuration type exists.
     *
     * @return true if there if a key is identified, false otherwise
     */
    public boolean hasConfigurationSource() {
        return configuration.isEmpty() ? false : true;
    }
}
