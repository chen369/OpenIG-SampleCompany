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
 * OpenIG-SampleCompany: Created by Charan Mann on 5/3/16 , 6:12 AM.
 */

/*
 * Groovy script for retrieving user profile attributes and setting as HTTP headers
 *
 * This script requires these arguments: profileAttributes, openamUrl
 */
import org.forgerock.http.protocol.Request
import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status
import org.forgerock.util.AsyncFunction

import static org.forgerock.http.protocol.Response.newResponsePromise

/**
 * Creates unauthorized error
 */
def getUnauthorizedError() {
    logger.info("Returning UNAUTHORIZED error")
    Response errResponse = new Response(Status.UNAUTHORIZED)
    errResponse.entity.json = [code: 401, reason: "Unauthorized", message: "Authentication Failed"];

    // Need to wrap the response object in a promise
    return newResponsePromise(errResponse)
}

// Check if OpenAM session cookie is present
if (null != request.cookies['iPlanetDirectoryPro']) {
    String openAMCookie = request.cookies['iPlanetDirectoryPro'][0].value

    // Perform cookie validation and get uid
    logger.info("iPlanetDirectoryPro cookie found, performing validation")

    Request validation = new Request()
    validation.uri = "${openamUrl}/sessions/${openAMCookie}?_action=validate"
    validation.method = "POST"
    return http.send(context, validation)
    // when there will be a response available ...
    // Need to use 'thenAsync' instead of 'then' because we'll return another promise, not directly a response
            .thenAsync({ validationResponse ->
        logger.info("Token Validation Response : ${validationResponse.entity.json}")
        def data = validationResponse.entity.json
        def isTokenValid = data['valid']
        def uid = data['uid']

        // If cookie validation succeeds and has valid uid
        if (isTokenValid && null != uid) {

            if (!(profileAttributes.empty)) {
                // Retrieving user profile attributes
                logger.info("Retrieving user profile attributes: ${profileAttributes} for user: ${uid}")
                Request attributes = new Request()
                attributes.uri = "${openamUrl}/users/${uid}"
                attributes.headers.put('iPlanetDirectoryPro', openAMCookie)
                attributes.method = "GET"

                return http.send(context, attributes)
                        .thenAsync({ attributesResponse ->
                    profileAttributes.each { name ->
                        def attrs = attributesResponse.entity.json
                        def values = attrs[name]
                        logger.info("Retrieved user profile attribute values: ${values} for attribute name: ${name}")

                        // Check if some attribute is present for specified name
                        if (null != values) {
                            def attrValue = values[0]

                            // Set the attributes in headers of the original request
                            // Security tip: These header values can be encrypted by a symmetric key shared between OpenIG and protected application
                            logger.info("Setting HTTP header: ${name}, value: ${attrValue}")
                            request.headers.add(name, attrValue)
                        }
                    }

                    // Call the next handler with the modified request
                    // That returns a new promise without blocking the current flow of execution
                    return next.handle(context, request)
                } as AsyncFunction)
            } else {
                logger.info("No attributes retrieval required, invoking next handler")
                // Call the next handler
                return next.handle(context, request)
            }
        }
        logger.info("Token validation failed")
        return getUnauthorizedError()
    } as AsyncFunction)
} else {
    logger.info("No iPlanetDirectoryPro cookie present")
    return getUnauthorizedError()
}




