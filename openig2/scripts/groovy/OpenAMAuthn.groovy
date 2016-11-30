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
 * OpenIG-SampleCompany: Created by Charan Mann on 11/22/16 , 10:12 AM.
 */
import org.forgerock.http.protocol.Request
import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status
import org.forgerock.util.AsyncFunction

import static org.forgerock.http.protocol.Response.newResponsePromise

/*
 * This Groovy script performs following functions:
 * 1. Redirects user to OpenAM login URL in case invalid or no OpenAM cookie is present in the request
 * 2. (Optional) Retrieves user profile attributes and sets as HTTP headers
 *
 * This script requires these arguments: openamUrl(String), openamAuthUrl(String), profileAttributes([String]), sessionAttributes([String]).
 * Note: profileAttributes and sessionAttributes are optional arguments.
 *
 * Sample:
 *         {
          "comment": "Scripted filter for OpenAM Authentication redirect",
          "name": "OpenAM Authentication redirect (and Attributes retrieval)",
          "type": "ScriptableFilter",
          "config": {
            "type": "application/x-groovy",
            "file": "OpenAMAuthnS.groovy",
            "args": {
              "openamUrl": "http://openam.example.com:18080/openam/json/employees",
              "openamAuthUrl": "http://openam.example.com:18080/openam/XUI/#login/employees&goto=${urlEncodeQueryParameterNameOrValue(contexts.router.originalUri)}",
              "profileAttributes": [
                "uid"
              ],
              "sessionAttributes": [
                "sunIdentityUserPassword"
              ]
            }
          },
          "capture": "all"
        }
 */

/**
 * Creates Redirect response
 */
def getRedirectResponse() {
    logger.info("Returning redirect to OpenAM Login URL")
    Response redirectResponse = new Response(Status.FOUND)
    redirectResponse.entity.json = [code: 302, message: "Redirect to OpenAM Login URL"];
    redirectResponse.headers["Location"] = openamAuthUrl

    // Need to wrap the response object in a promise
    return newResponsePromise(redirectResponse)
}

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

            if (binding.hasVariable("profileAttributes") && !(profileAttributes.empty)) {
                // Retrieving user profile attributes
                logger.info("Retrieving user profile attributes: ${profileAttributes} for user: ${uid}")
                Request pAttributes = new Request()
                pAttributes.uri = "${openamUrl}/users/${uid}"
                pAttributes.headers.put('iPlanetDirectoryPro', openAMCookie)
                pAttributes.method = "GET"

                return http.send(context, pAttributes)
                        .thenAsync({ pAttributesResponse ->
                    profileAttributes.each { pName ->
                        def pAttrs = pAttributesResponse.entity.json
                        def pValues = pAttrs[pName]
                        logger.info("Retrieved user profile attribute values: ${pValues} for attribute name: ${pName}")

                        // Check if some attribute is present for specified name
                        if (null != pValues) {
                            def pAttrValue = pValues[0]

                            // Set the attributes in headers of the original request
                            // Security tip: These header values can be encrypted by a symmetric key shared between OpenIG and protected application
                            logger.info("Setting HTTP header: ${pName}, value: ${pAttrValue}")
                            request.headers.add(pName, pAttrValue)
                        }

                        // Now retrieve session attributes
                        retrieveSessionAttributes(uid, openAMCookie)
                    }

                    // Call the next handler with the modified request
                    // That returns a new promise without blocking the current flow of execution
                    return next.handle(context, request)
                } as AsyncFunction)
            } else {
                // In case of no profile attributes, just retrieve session attributes
                retrieveSessionAttributes(uid, openAMCookie)

                // Call the next handler with the modified request
                // That returns a new promise without blocking the current flow of execution
                return next.handle(context, request)
            }
        }
        logger.info("Token validation failed")
        return getRedirectResponse()
    } as AsyncFunction)
} else {
    logger.info("No iPlanetDirectoryPro cookie present")
    return getRedirectResponse()
}


def retrieveSessionAttributes(uid, openAMCookie) {
    if (binding.hasVariable("sessionAttributes") && !(sessionAttributes.empty)) {
        // Retrieving user profile attributes
        logger.info("Retrieving session attributes: ${sessionAttributes} for user: ${uid}")
        Request sAttributes = new Request()
        sAttributes.uri = "${openamUrl}/sessions/?_action=getProperty"
        sAttributes.headers.put('iPlanetDirectoryPro', openAMCookie)
        sAttributes.headers.put('Content-Type', 'application/json')
        sAttributes.method = "POST"

        // Create session attribute string
        def sAttrString
        sessionAttributes.each { sName ->
            sAttrString = sName + ','
        }

        if (null != sAttrString) {
            sAttrString = sAttrString.reverse().drop(1).reverse()
            sAttributes.entity.json = [properties: [sAttrString]]
        }

        return http.send(context, sAttributes)
                .thenAsync({ sAttributesResponse ->
            sessionAttributes.each { name ->
                def sAttrs = sAttributesResponse.entity.json
                def sValues = sAttrs[name]
                logger.info("Retrieved session attribute values: ${sValues} for attribute name: ${name}")

                // Check if some attribute is present for specified name
                if (null != sValues) {
                    def sAttrValue = sValues

                    // Set the attributes in headers of the original request
                    // Security tip: These header values can be encrypted by a symmetric key shared between OpenIG and protected application
                    logger.info("Setting HTTP header: ${name}, value: ${sAttrValue}")
                    request.headers.add(name, sAttrValue)
                }
            }

        } as AsyncFunction)
    } else if (!binding.hasVariable("profileAttributes") || (profileAttributes.empty)) {
        logger.info("No profile or session attributes retrieval required, invoking next handler")
    }
}


