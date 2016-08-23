# OpenIG-SampleCompany

*OpenIG configuration files for SampleCompany*


Disclaimer of Liability :
=========================
The sample code described herein is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law. ForgeRock does not warrant or guarantee the individual success developers may have in implementing the sample code on their development platforms or in production configurations.

ForgeRock does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness or completeness of any data or information relating to the sample code. ForgeRock disclaims all warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related to the code, or any service or software related thereto.

ForgeRock shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any action taken by you or others related to the sample code.

Pre-requisites :
================
1. Create Linux Server/VM with 4 CPU, 8 GB RAM and 50 GB hard drive. Create user 'forgerock', this user shall be used for all operations in this guide. 
2. The server hosting OpenIG should have internet connectivity as first request tries to download required jars from maven repo. The custom groovy script uses @Grab and it downloads the required dependencies under <User-Home>/.groovy/grapes.
3. Copy binaries for OpenIG, OpenDJ and OpenAM to ~/softwares directory.
4. Copy Install scripts from https://github.com/CharanMann/OpenIG-SampleCompany/tree/master/installScripts to ~/softwares directory. 
5. Specify below local host enteries (both on server hosting and client accessing these applications): <br />
   [IP Address]  openig1.example.com  # OpenIG1, Port:9000 <br />
   [IP Address]  openig2.example.com  # OpenIG2, Port:9002 <br />
   [IP Address]  opendj.example.com   # OpenDJ, Port 1389 <br />
   [IP Address]  openam.example.com # OpenAM, Port:18080 <br />
   [IP Address]  employees.example.com # Internal Employee App, Port:8002 <br />
   [IP Address]  employees-ig.example.com  # Internal Employee App via OpenIG, Port:9000 <br />
   [IP Address]  customers.example.com  # External Customer App, Port:8004 <br />
   [IP Address]  customers-ig.example.com # External Customer App  via OpenIG, Port:9000 <br />
   [IP Address]  apis.example.net # API server, Port:8010 <br />
   [IP Address]  apis-ig.example.net  # API server via OpenIG, Port:9002 <br />
   [IP Address]  travel.example.net  # Internal Travel App, Port:8012 <br />
   [IP Address]  travel-ig.example.net # Internal Travel App via OpenIG, Port:9002 <br />
   [IP Address]  benefits.example.com # Internal Benefits App, Port:8014 <br />
   [IP Address]  benefits-ig.example.com # Internal Benefits App via OpenIG, Port:9002
6. Install and configure SampleCompany application on same or different server: refer https://github.com/CharanMann/SampleCompany  
  
OpenDJ Identity Store Installation & Configuration:
===================================================
1. Install OpenDJ 3 under /opt/opendjis. Refer https://backstage.forgerock.com/#!/docs/opendj/3/install-guide#command-line-install <br />
   Setup params: <br />
   ============= <br />
   * Hostname:                      opendj.example.com
   * LDAP Listener Port:            1389
   * Administration Connector Port: 4444
   * JMX Listener Port:
   * LDAP Secure Access:            disabled
   * Root User DN:                  cn=Directory Manager
   * Password                       cangetindj
   * Directory Data:                Backend Type: JE Backend
                                    Create New Base DN dc=example,dc=com
   * Base DN Data: Only Create Base Entry (dc=example,dc=com)

OpenAM Installation & Configuration:
====================================
1. Install OpenAM 13.0 under /opt/tomcats/am1. Refer https://backstage.forgerock.com/#!/docs/openam/13/install-guide#configure-openam-custom <br />
2. Navigate to http://openam.example.com:18080/openam for OpenAM configuration. <br />
   Setup params: <br />
   ============= <br />
   * amAdmin password: cangetinam
   * Server Setting:
     * Server URL: http://openam.example.com:18080/openam
     * Cookie Domain: .example.com
     * Configuration Directory: /home/forgerock/am1   
   * Configuration Store Details 
     * SSL/TLS Enabled: No
     * Host Name: localhost
     * Listening Port: 50389
     * Root Suffix: dc=openam,dc=forgerock,dc=org
     * User Name: cn=Directory Manager
   * User Store Details
     * User Data Store Type: OpenDJ
     * SSL/TLS Enabled: No 
     * Host Name: opendj.example.com
     * Listening Port: 1389 
     * Root Suffix: dc=example,dc=com
     * User Name: cn=Directory Manager
     * Password: cangetindj
   * Site Configuration Details: This instance is not setup behind a load balancer
   * Default Policy Agent password: cangetinwa 
3. Stop OpenDJ Identity store and import identity data using: ./import-ldif --includeBranch dc=example,dc=com --backendID userRoot --ldifFile ~/softwares/installScripts/sample.ldif
   * Passwords for all users in this ldif is: Passw0rd 
4. Install SSO Admin Tools under /opt/forgerock/OpenAM-Tools. 
5. Install patch: 12321-1-tpatch for SSO Admin Tools. Copy openam-auth-fr-oath-13.0.0.jar file from the deployed OpenAM Server war file into the lib directory in the OpenAM Tools home:
   cp /opt/forgerock/OpenAM-Server/webapps/openam/WEB-INF/lib/openam-auth-fr-oath-13.0.0.jar /opt/forgerock/OpenAM-Tools/lib
6. Import OpenAM service configs : 
   * Execute command: ./ssoadm import-svc-cfg -u amadmin -f /tmp/pwd.txt -e password -X ~/softwares/installScripts/openam-13.xml
   * Directory Service contains existing data. Do you want to delete it? [y|N] y
   * Check for any errors. Check if all service configurations have been imported successfully. 
   * Note that no OpenAM policies shall appear in any realm, this is due to bug: https://bugster.forgerock.org/jira/browse/OPENAM-8169 
7. Enable CORS filter for OpenAM. Refer https://backstage.forgerock.com/#!/docs/openam/13/install-guide/chap-prepare-install#enable-cors-support <br />
   CORS filter params: <br />
   ============= <br />
   * url-pattern: /json/*
   * methods: POST,GET,PUT,DELETE,PATCH,OPTIONS
   * origins: *
   * allowCredentials: false
   * headers: Accept,Accept-Encoding,Accept-Language,Authorization,Cookie,Connection,Content-Length,Content-Type,iPlanetDirectoryPro,Host,Origin,User-Agent,X-OpenAM-Username,X-OpenAM-Password,X-Requested-With <br />
   * expectedHostname: openam.example.com:18080
   * Leave default value for rest of parameters
8. Note that only FireFox can be used for OpenAM admin console due to this bug: https://bugster.forgerock.org/jira/browse/OPENAM-5984  

OpenIG Installation & Configuration:
====================================
1. Install two OpenIG 4.0 instances on Apache Tomcat under /opt/tomcats/ig1 and /opt/tomcats/ig2 respectively. Refer https://backstage.forgerock.com/#!/docs/openig/4/gateway-guide#install
2. Change port number for Apache Tomcat for OpenIG1 to 9000 and OpenIG2 to 9002 respectively.  
3. Specify config directories for each OpenIG instance by specifying OPENIG_BASE. e.g for OpenIG1 specify "export OPENIG_BASE=/home/forgerock/.openig1" in /opt/tomcats/ig1/bin/setenv.sh
4. Create logs directory for each OpenIG instance. e.g for OpenIG1 create /home/forgerock/.openig1/logs
5. Copy OpenIG configurations for each OpenIG instance. e.g for OpenIG1 copy https://github.com/CharanMann/OpenIG-SampleCompany/tree/master/openig1 to /home/forgerock/.openig1  <br />
   Note that certain OpenIG routes needs to be disabled for specific use case testing. Refer to section 'OpenIG Use Cases testing' for more information.  
6. Specify CORS filter for OpenIG2 in /opt/tomcats/ig2/conf/web.xml. Refer https://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#CORS_Filter for sample CORS filter template. <br />
   CORS filter params: <br />
   ============= <br />
   * url-pattern: /history/*
   * cors.allowed.origins: http://employees-ig.example.com:9000
   * cors.allowed.methods: GET,POST,HEAD,OPTIONS,PUT
   * cors.allowed.headers: Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization
   * cors.exposed.headers: Access-Control-Allow-Origin,Access-Control-Allow-Credentials
   * cors.support.credentials: false
   * Leave default value for rest of parameters
7. Start both OpenIG instances. Verify there are no errors in Apache Tomcat and OpenIG logs. Ensure that OpenAM is started before starting OpenIG servers.

SampleCompany URLs :
===========================
1. CommonServices direct: http://apis.example.net:8010/history/emp1 <br />
   CommonServices via OpenIG: http://apis-ig.example.net:9002/history/emp1
2. EmployeeApp direct: http://employees.example.com:8002/employeeApp/#/ <br />
   EmployeeApp via OpenIG: http://employees-ig.example.com:9000/employeeApp/#/
3. CustomerApp direct: http://employees.example.com:8002/employeeApp/#/ <br />
   CustomerApp via OpenIG: http://employees-ig.example.com:9000/employeeApp/#/
4. TravelApp direct: http://travel.example.net:8012/travelApp/#/ <br />
   TravelApp via OpenIG: http://travel-ig.example.net:9002/travelApp/#/
5. BenefitsApp direct: http://benefits.example.com:8014/benefitsApp/ <br />
   BenefitsApp via OpenIG:: http://benefits-ig.example.com:9002/benefitsApp/   

OpenIG Use Cases testing:
========================= 
1. OpenIG-OpenAM PEP for web applications - Minimal:
   * Enabled Route(s): 02-pep-employees-minimal.json
   * Disabled Route(s): 05-pep-employees-extended.json, 03-pep-employees-exclusions.json, 04-pep-employees-logout.json. This can be done by suffixing these files by '.disabled.' Such as 04-pep-employees-logout.json.disabled.
   * Test1: Login to EmployeeApp using emp1 account. Result: User successfully logged in. 
2. OpenIG-OpenAM PEP for web applications - URL exclusions:
   * Enabled Route(s): 03-pep-employees-exclusions.json
   * Disabled Route(s): 05-pep-employees-extended.json, 04-pep-employees-logout.json.
   * Test1: Login to EmployeeApp using emp1 account. Result: This will remove specified URL from policy evaluations.   
3. OpenIG-OpenAM PEP for web applications - Extended:
   * Enabled Route(s): 05-pep-employees-extended.json, 03-pep-employees-exclusions.json, 04-pep-employees-logout.json
   * Disabled Route(s): 02-pep-employees-minimal.json
   * Test1: Login to EmployeeApp using emp1 account. Result: User successfully logged in.
   * Test2: Logout emp1. Result: User successfully logged out. 
   * Test3: Login to EmployeeApp using cont1 account. Result: User redirected to access denied URL.        
4. OpenIG-OpenAM PEP for REST APIs
   * Enabled Route(s): 06-pep-apis.json
   * Disabled Route(s): None
   * Test1: Get TxHistory for all users: curl -X GET -H "X-OpenAM-Username: empAdmin" -H "X-OpenAM-Password: Passw0rd" "http://apis-ig.example.net:9002/txHistory/all". Result: Should return transaction history for all users
   * Test2: Get TxHistory for all users using unauthorized account: curl -X GET -H "X-OpenAM-Username: emp1" -H "X-OpenAM-Password: Passw0rd" "http://apis-ig.example.net:9002/txHistory/all". Result: Should return authorization failed.          
5. OpenIG-OAuth2 RS:
   * Enabled Route(s): 10-oauth2rs-apis.json
   * Disabled Route(s): None
   * Test1: Acquire OAuth Access token by using OAuth Resource Owner Password Credentials flow : curl -X POST -H "Authorization: BASIC ZW1wbG95ZWVBcHA6cGFzc3dvcmQ=" -H "Content-Type: application/x-www-form-urlencoded" -d 'grant_type=password&username=emp1&password=Passw0rd&scope=uid mail' "http://openam.example.com:18080/openam/oauth2/employees/access_token" <br />
     Get TxHistory for specified user: curl -X GET -H "Authorization: Bearer a04b0596-9ed7-4e7e-bd36-4008d901bcd2" "http://apis-ig.example.net:9002/history/emp1". Result: Should return transaction history for specified user.
   * Test2: Get TxHistory for specified user using invalid OAuth Access token: curl -X GET -H "Authorization: Bearer a04b0596-9ed7-4e7e-bd36-qqqqqqqq" "http://apis-ig.example.net:9002/history/emp1" -v. Result: Should return error: "The access token provided is expired, revoked, malformed, or invalid for other reasons.".   
6. OpenIG-Credentials Replay-File-DB:
   * Enabled Route(s): 20-replay-file-benefits.json
   * Disabled Route(s): 21-replay-openam-benefits.json
   * Test1: Navigate to BenefitsApp. Result: User should be automatically logged in.     
7. OpenIG-Credentials Replay-OpenAM:
   * Enabled Route(s): 21-replay-openam-benefits.json
   * Disabled Route(s): 20-replay-file-benefits.json
   * Test1: Login to BenefitsApp using emp1 account. Result: User successfully logged in.   
8. OpenIG-SAML SP - Minimal
   * Enabled Route(s): 30-saml-travel-minimal.json
   * Disabled Route(s): 31-saml-travel-extended.json
   * Test1: Login to TravelApp using emp1 account. Result: User successfully logged via SAML 2.0 SP init webSSO flow.   
9. OpenIG-SAML SP - Extended
   * Enabled Route(s): 31-saml-travel-extended.json
   * Disabled Route(s): 30-saml-travel-minimal.json
   * Test1: Login to TravelApp using emp1 account. Result: User successfully logged via SAML 2.0 SP init webSSO flow. 
   * Test2: Test SAML SLO. Result: User successfully logged out.
10. OpenIG-OIDC RP - Minimal:
   * Enabled Route(s): 40-oidc-customers-minimal.json
   * Disabled Route(s): 41-oidc-customers-exclusions.json, 42-oidc-customers-logout.json, 43-oidc-customers-extended.json. 
   * Test1: Login to CustomerApp using cus1 account. Result: User successfully logged in.   
11. OpenIG-OIDC RP - Extended:
   * Enabled Route(s): 41-oidc-customers-exclusions.json, 42-oidc-customers-logout.json, 43-oidc-customers-extended.json
   * Disabled Route(s): 40-oidc-customers-minimal.json
   * Test1: Login to CustomerApp using cus1 account. Result: User successfully logged in.
   * Test2: Logout cus1. Result: User successfully logged out.
12. OpenIG-UMA RS: Not yet implemented



* * *

The contents of this file are subject to the terms of the Common Development and
Distribution License (the License). You may not use this file except in compliance with the
License.

You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
specific language governing permission and limitations under the License.

When distributing Covered Software, include this CDDL Header Notice in each file and include
the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
Header, with the fields enclosed by brackets [] replaced by your own identifying
information: "Portions copyright [year] [name of copyright owner]".

Copyright 2016 Charan Mann
