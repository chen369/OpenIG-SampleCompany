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
 * OpenIG-OpenAM-BasicAuthn: Created by Charan Mann on 4/1/16 , 6:12 AM.
 */

/*
 * Groovy script for testing OpenAM Attributes using groovyx.net.http.RESTClient.
 * Refer https://github.com/jgritman/httpbuilder/wiki/RESTClient
 */
import groovyx.net.http.RESTClient
import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status

/**
 * Creates unauthorized error
 * @return Status.UNAUTHORIZED
 */
def getUnauthorizedError() {
    Response response = new Response()

    response.status = Status.UNAUTHORIZED
    response.headers.add("Content-Type", ["application/json; charset=utf-8"])
    response.entity = "{\"code\": 401,\"reason\":\"Unauthorized\",\"message\":\"Authentication Failed\"}"
    return response
}

def openAMREST = new RESTClient('http://openam13.sc.com:8080/openam/json/')
def response
try {
    response = openAMREST.post(path: 'authenticate', headers: ['X-OpenAM-Username': 'testUser1', 'X-OpenAM-Password': 'password1'])
}
catch (Exception e) {
    response = getUnauthorizedError()
    assert response.status == Status.UNAUTHORIZED
    assert response.getEntity().getJson().getAt("message") == "Authentication Failed"
    return
}

assert response.status == 200  // HTTP response code; 404 means not found, etc.
tokenId = response.getData().get("tokenId")
println("tokenId: " + tokenId)

response = openAMREST.post(path: 'sessions/' + tokenId, query: ['_action': 'validate'])
isTokenValid = response.getData().get("valid")
println("Token validation response: " + response.getData())
assert isTokenValid

