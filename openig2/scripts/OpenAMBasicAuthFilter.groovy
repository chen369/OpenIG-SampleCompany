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
 * OpenIG-SampleCompany: Created by Charan Mann on 4/1/16 , 6:12 AM.
 */

/*
 * Groovy script for OpenAM basic authentication
 *
 * This script requires these arguments: userId, password, openamUrl
 */
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1')

import groovyx.net.http.RESTClient
import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status

/**
 * Creates unauthorized error
 * @return Status.UNAUTHORIZED
 */
def getUnauthorizedError() {
    logger.info("Returning UNAUTHORIZED error")
    Response errResponse = new Response()

    errResponse.status = Status.UNAUTHORIZED
    errResponse.headers.add("Content-Type", ["application/json; charset=utf-8"])
    errResponse.entity = "{\"code\": 401,\"reason\":\"Unauthorized\",\"message\":\"Authentication Failed\"}"
    return errResponse
}

/**
 * Set tokenId in request header and call next handler
 * @param tokenId
 */
def callNextHandler(tokenId) {
    // Set the tokenId in attributes
    attributes.tokenId = tokenId

    // Call the next handler. This returns when the request has been handled.
    return next.handle(context, request)
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

        return callNextHandler(openAMCookie)
    } else {
        logger.info("Invalid OpenAM session")
    }
}

// Invoke OpenAM authentication
logger.info("Authenticating user: " + userId)

try {
    response = openAMRESTClient.post(path: 'authenticate', headers: ['X-OpenAM-Username': userId, 'X-OpenAM-Password': password])
}
catch (Exception e) {
    logger.info("Exception in authenticating user: " + e.getMessage())
    // In case of any failure like authentication failure, server exception etc, return UNAUTHORIZED status. This can be modified to return specific response status for different failures.
    // No need to call next.handle() as we want to terminate handing here
    return getUnauthorizedError()
}

callNextHandler(response.getData().get("tokenId"))
