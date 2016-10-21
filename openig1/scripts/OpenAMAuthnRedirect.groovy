/*
 * Copyright © 2016 ForgeRock, AS.
 *
 * This is unsupported code made available by ForgeRock for community development subject to the license detailed below.
 * The code is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law.
 *
 * ForgeRock does not warrant or guarantee the individual success developers may have in implementing the code on their
 * development platforms or in production configurations.
 *
 * ForgeRock does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness
 * or completeness of any data or information relating to the alpha release of unsupported code. ForgeRock disclaims all
 * warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related
 * to the code, or any service or software related thereto.
 *
 * ForgeRock shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any
 * action taken by you or others related to the code.
 *
 * The contents of this file are subject to the terms of the Common Development and Distribution License (the License).
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License at https://forgerock.org/cddlv1-0/. See the License for the specific language governing
 * permission and limitations under the License.
 *
 * Portions Copyrighted 2016 Charan Mann
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
