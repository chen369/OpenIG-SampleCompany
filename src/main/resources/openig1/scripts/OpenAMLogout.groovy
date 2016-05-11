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
 * OpenIG-SampleCompany: Created by Charan Mann on 5/11/16 , 10:12 AM.
 */

/*
 * Groovy script for OpenAM logout
 *
 * This script requires these arguments: openamUrl
 */
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1')

import groovyx.net.http.RESTClient
import org.forgerock.http.protocol.Cookie

def openAMRESTClient = new RESTClient(openamUrl)

// Check if OpenAM session cookie is present
if (null != request.cookies['iPlanetDirectoryPro']) {
    Cookie openAMCookie = request.cookies['iPlanetDirectoryPro'][0]
    String openAMCookieValue = request.cookies['iPlanetDirectoryPro'][0].value

    // Perform logout
    println("iPlanetDirectoryPro cookie found, performing logout")
    response = openAMRESTClient.post(path: 'sessions/' , query: ['_action': 'logout'], headers: ['iplanetDirectoryPro': openAMCookieValue])
    result = response.getData().get("result")
    println("OpenAM logout request response: " + result)

    //iplanetDirectoryPro cookie invalidation
    request.cookies['iPlanetDirectoryPro'][0].maxAge = -1
    //request.cookies['iPlanetDirectoryPro'][0] = null
    request.cookies['iPlanetDirectoryPro'][0].name = "iPlanetDirectoryPro_expired"
    request.cookies['iPlanetDirectoryPro'][0].value = "1111"
    request.cookies.remove("iPlanetDirectoryPro")
    attributes.validToken = "invalid"
    println("iPlanetDirectoryPro cookie: " + request.cookies['iPlanetDirectoryPro'][0].name)
    println("attributes.validToken: " + attributes.validToken)
}

return next.handle(context, request)




