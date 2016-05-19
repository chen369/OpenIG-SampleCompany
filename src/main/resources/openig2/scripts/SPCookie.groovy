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
 * OpenIG-SampleCompany: Created by Charan Mann on 5/17/16 , 6:12 AM.
 */

/*
 * Groovy script for setting SP cookie
 *
 * This script requires these arguments: profileAttributes, openamUrl
 */

import org.forgerock.http.header.SetCookieHeader
import org.forgerock.http.protocol.Cookie
import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status
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



