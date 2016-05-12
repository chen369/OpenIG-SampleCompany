/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2016 Charan Mann
 * Portions Copyrighted 2016 ForgeRock AS
 *
 * OpenIG-SampleCompany: Created by Charan Mann on 5/12/16 , 8:51 AM.
 */

/*
 * Groovy script for redirect to OpenAM for authentication
 *
 * This script requires these arguments:  openamUrl, openamAuthUrl
 */
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1')

import groovyx.net.http.RESTClient
import org.forgerock.http.protocol.Status
import org.forgerock.openig.el.Expression
import org.forgerock.openig.handler.StaticResponseHandler

/**
 * Invokes StaticResponseHandler
 */
def invokeStaticResponseHandler() {
    logger.info("Invalid OpenAM session, redirect to OpenAM for authentication")

    final StaticResponseHandler handler = new StaticResponseHandler(Status.FOUND);
    handler.addHeader("Location", Expression.valueOf(openamAuthUrl, String.class));
    return handler.handle(context, request)
}

def openAMRESTClient = new RESTClient(openamUrl)

// Check if valid session is present
if (null != request.cookies['iPlanetDirectoryPro']) {
    String openAMCookie = request.cookies['iPlanetDirectoryPro'][0].value

    // Perform cookie validation
    logger.info("iPlanetDirectoryPro cookie found, performing validation")
    response = openAMRESTClient.post(path: 'sessions/' + openAMCookie, query: ['_action': 'validate'])
    isTokenValid = response.getData().get("valid")

    if (isTokenValid) {
        logger.info("Valid OpenAM session, skipping authentication")

        // Call the next handler. This returns when the request has been handled.
        return next.handle(context, request)
    } else {
        invokeStaticResponseHandler()
    }
} else {
    invokeStaticResponseHandler()
}
