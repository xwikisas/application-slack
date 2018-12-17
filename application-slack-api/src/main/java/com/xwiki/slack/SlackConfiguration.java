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

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import com.xpn.xwiki.XWikiException;

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
     * @throws XWikiException whenever an exception occurs
     */
    public boolean isEnabled()
    {
        return configuration.getProperty("enabled", 1) == 1;
    }

    /**
     * Retrieve the Webhook URL of the Slack channel.
     * 
     * @return the Webhook URL of the Slack channel
     * @throws XWikiException whenever an exception occurs
     */
    public String getWebhookUrl()
    {
        return configuration.getProperty("webhookUrl");
    }
}
