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
 * OpenIG-SampleCompany: Created by Charan Mann on 5/11/16 , 10:12 AM.
 */

/*
 * Groovy script for OpenAM logout
 *
 * This script requires these arguments: openamUrl
 */
@Grab(group = 'org.codehaus.groovy.modules.http-builder', module = 'http-builder', version = '0.7.1')

import groovyx.net.http.RESTClient

def openAMRESTClient = new RESTClient(openamUrl)

// Check if OpenAM session cookie is present
if (null != request.cookies['iPlanetDirectoryPro']) {
    String openAMCookieValue = request.cookies['iPlanetDirectoryPro'][0].value

    // Perform logout
    logger.info("iPlanetDirectoryPro cookie found, performing logout")
    response = openAMRESTClient.post(path: 'sessions/', query: ['_action': 'logout'], headers: ['iplanetDirectoryPro': openAMCookieValue])
    result = response.getData().get("result")
    logger.info("OpenAM logout request response: " + result)
}

return next.handle(context, request)




