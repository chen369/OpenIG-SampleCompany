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
 * OpenIG-SampleCompany: Created by Charan Mann on 5/3/16 , 6:12 AM.
 */

/*
 * Groovy script for testing OpenAM replay headers using groovyx.net.http.RESTClient.
 * Refer https://github.com/jgritman/httpbuilder/wiki/RESTClient
 */
import groovyx.net.http.RESTClient

import static groovyx.net.http.ContentType.JSON

openAMRESTClient = new RESTClient('http://openam13.sc.com:8080/openam/json/')
response = openAMRESTClient.post(path: 'authenticate', headers: ['X-OpenAM-Username': 'george', 'X-OpenAM-Password': 'costanza'])

assert response.status == 200  // HTTP response code; 404 means not found, etc.
tokenId = response.getData().get("tokenId")
println("tokenId: " + tokenId)

response = openAMRESTClient.post(path: 'sessions/' + tokenId, query: ['_action': 'validate'])
isTokenValid = response.getData().get("valid")
uid = response.getData().get("uid")
println("Token validation response: " + response.getData())
assert isTokenValid
assert 'george' == uid

response = openAMRESTClient.post(path: 'sessions/', query: ['_action': 'getProperty'], headers: ['Content-Type': 'application/json', 'iPlanetDirectoryPro': tokenId], body: ["properties": ["sunIdentityUserPassword"]], requestContentType: JSON)
println("User encrypted password from sessiona: " + response.getData().get("sunIdentityUserPassword"))
