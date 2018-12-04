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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.http.Consts;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.JSONObject;
import org.xwiki.component.annotation.Component;

/**
 * Component used to push notifications to Slack.
 * 
 * @version $Id$
 */
@Component(roles = SlackClient.class)
@Singleton
public class SlackClient
{
    /**
     * Post a message to Slack.
     * 
     * @param message the message to post
     * @param url where to post the message
     * @throws IOException if posting the message fails
     */
    public void postMessage(String message, String url) throws IOException
    {
        HttpPost httpPost = new HttpPost(url);

        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
        Map<String, Object> data = new HashMap<>();
        data.put("text", message);
        data.put("mrkdwn", false);
        JSONObject json = new JSONObject(data);
        httpPost.setEntity(new StringEntity(json.toJSONString(), Consts.UTF_8));

        try (CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = httpclient.execute(httpPost)) {
            // Do nothing.
        }
    }

    /**
     * Encodes a message to be send to Slack.
     * 
     * @param message the message to be encoded
     * @return the encoded message
     */
    public String encode(String message)
    {
        return message.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
