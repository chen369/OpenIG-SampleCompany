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
import org.forgerock.http.protocol.Request
import org.forgerock.util.AsyncFunction

logger.info("Performing OpenAM logout")
// Check if OpenAM session cookie is present
if (null != request.cookies['iPlanetDirectoryPro']) {
    String openAMCookie = request.cookies['iPlanetDirectoryPro'][0].value

    Request logout = new Request()
    logout.uri = "${openamUrl}/sessions/${openAMCookie}?_action=logout"
    logout.method = "POST"
    logout.headers.put('iPlanetDirectoryPro', openAMCookie)

    return http.send(context, logout)
    // when there will be a response available ...
    // Need to use 'thenAsync' instead of 'then' because we'll return another promise, not directly a response
            .thenAsync({ logoutResponse ->
        logger.info("OpenAM logout Response : ${logoutResponse.entity.json}")

        return next.handle(context, request)
    } as AsyncFunction)

} else {
    logger.info("No iPlanetDirectoryPro cookie present for performing OpenAM logout")
}

return next.handle(context, request)





