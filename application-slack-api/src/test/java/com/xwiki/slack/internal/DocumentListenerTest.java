
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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
//import org.junit.Test;
import org.slf4j.Logger;
import org.xwiki.bridge.event.DocumentUpdatingEvent;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xwiki.licensing.Licensor;
import com.xwiki.slack.SlackConfiguration;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

/**
 * Tests for {@link DocumentListener}.
 *
 * @version $Id$
 */
public class DocumentListenerTest
{
    @Rule
    public final MockitoComponentMockingRule<DocumentListener> mocker =
        new MockitoComponentMockingRule<>(DocumentListener.class);

    private SlackConfiguration configuration;

    private DocumentListener listener;

    private DocumentUpdatingEvent event;

    private XWikiDocument doc;

    private Licensor licensor;

    private Logger logger;

    private DocumentReference docReference;

    private XWikiContext context;

    private DocumentReference userReference;

    @Before
    public void setUp() throws ComponentLookupException
    {
        configuration = mocker.getInstance(SlackConfiguration.class);
        context = mock(XWikiContext.class);

        doc = mock(XWikiDocument.class);
        docReference = new DocumentReference("wiki", "Space", "Page");
        when(doc.getDocumentReference()).thenReturn(docReference);

        event = spy(new DocumentUpdatingEvent());

        listener = mocker.getComponentUnderTest();
        logger = mocker.getMockedLogger();
        when(logger.isDebugEnabled()).thenReturn(true);

        userReference = new DocumentReference("wiki", "XWiki", "user");
        when(context.getUserReference()).thenReturn(userReference);
        when(context.getMainXWiki()).thenReturn("xwiki");

        licensor = mocker.getInstance(Licensor.class);
        when(licensor.hasLicensure(
            new DocumentReference(context.getMainXWiki(), Arrays.asList("Slack", "Code"), "SlackConfigurationClass")))
                .thenReturn(true);

    }

    @Test
    public void slackDisabled()
    {
        when(configuration.isEnabled()).thenReturn(false);

        listener.onEvent(event, doc, context);

        verify(logger, times(1)).debug(
            "Skipping notification sending for event [{}] by user [{}] on document [{}]. Slack is disabled.",
            event.getClass().getName(), userReference, docReference);

        verify(doc, times(1)).getDocumentReference();
    }
}
