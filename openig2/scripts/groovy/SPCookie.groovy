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
 * OpenIG-SampleCompany: Created by Charan Mann on 5/17/16 , 6:12 AM.
 */

/*
 * Groovy script for setting SP cookie
 *
 * This script requires these arguments: cookieName, cookieDomain, cookiePath, cookieValue(name:value pairs)
 */

import org.forgerock.http.header.SetCookieHeader
import org.forgerock.http.protocol.Cookie
import org.forgerock.http.protocol.Response
import org.forgerock.util.AsyncFunction


Cookie spCookie = new Cookie();
spCookie.setName(cookieName)
spCookie.setDomain(cookieDomain)
spCookie.setPath(cookiePath)

// This cookieValue can be encrypted by a symmetric key shared between OpenIG and SP
spCookie.setValue(cookieValue)
List cookies = new ArrayList();
cookies.add(spCookie)
SetCookieHeader cookieHeader = new SetCookieHeader(cookies)

return next.handle(context, request)
        .thenAsync({ Response.newResponsePromise(it) } as AsyncFunction)
        .thenOnResult { it.headers['Set-Cookie'] = cookieHeader.values }



