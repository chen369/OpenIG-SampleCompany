# OpenIG-SampleCompany

*OpenIG configuration files for SampleCompany*


Disclaimer of Liability :
=========================
The sample code described herein is provided on an "as is" basis, without warranty of any kind, to the fullest extent permitted by law. ForgeRock does not warrant or guarantee the individual success developers may have in implementing the sample code on their development platforms or in production configurations.

ForgeRock does not warrant, guarantee or make any representations regarding the use, results of use, accuracy, timeliness or completeness of any data or information relating to the sample code. ForgeRock disclaims all warranties, expressed or implied, and in particular, disclaims all warranties of merchantability, and warranties related to the code, or any service or software related thereto.

ForgeRock shall not be liable for any direct, indirect or consequential damages or costs of any type arising out of any action taken by you or others related to the sample code.

Pre-requisites :
================
1. The server hosting OpenIG should have internet connectivity as first request tries to download required jars from maven repo. The custom groovy script uses @Grab and it downloads the required dependencies under <User-Home>/.groovy/grapes.
2. Binaries for OpenIG, OpenDJ and OpenAM are available.
3. Specify below local host enteries (both on server hosting and client accessing these applications): <br />
   [IP Address]  openig1.sc.com  # OpenIG, Port:9000 <br />
   [IP Address]  openig2.sc.com  # OpenIG, Port:9002 <br />
   [IP Address]  openam13.sc.com # OpenAM, Port:8080 <br />
   [IP Address]  employees.sc.com # Internal Employee App, Port:8002 <br />
   [IP Address]  employees-ig.sc.com  # Internal Employee App via OpenIG, Port:9000 <br />
   [IP Address]  customers.sc.com  # External Customer App, Port:8004 <br />
   [IP Address]  customers-ig.sc.com # External Customer App  via OpenIG, Port:9000 <br />
   [IP Address]  apis.sample.com # API server, Port:8010 <br />
   [IP Address]  apis-ig.sample.com  # API server via OpenIG, Port:9002 <br />
   [IP Address]  travel.sample.com  # Internal Travel App, Port:8012 <br />
   [IP Address]  travel-ig.sample.com # Internal Travel App via OpenIG, Port:9002 <br />
   [IP Address]  benefits.sample.com # Internal Benefits App, Port:8014 <br />
   [IP Address]  benefits-ig.sample.com # Internal Benefits App via OpenIG, Port:9002
4. Install and configure SampleCompany application: refer https://github.com/CharanMann/SampleCompany    
   
OpenIG Installation & Configuration:
====================================
1. Install 2 OpenIG instances on Apache Tomcat under /opt/forgerock/OpenIG1 and /opt/forgerock/OpenIG. Refer https://backstage.forgerock.com/#!/docs/openig/4/gateway-guide#install
2. Change port number for Apache Tomcat for OpenIG1 to 9000 and OpenIG2 to 9002 respectively.  
3. Specify config directories for each OpenIG instance by specifying OPENIG_BASE. e.g for OpenIG1 specify "export OPENIG_BASE=/home/forgerock/.openig1" in /opt/forgerock/OpenIG1/bin/setenv.sh
4. Create logs directory for each OpenIG instance. e.g for OpenIG1 create /home/forgerock/.openig1/logs
5. Copy OpenIG configurations. e.g for OpenIG1 copy https://github.com/CharanMann/OpenIG-SampleCompany/tree/master/openig1 to /home/forgerock/.openig1 
6. Specify CORS filter for OpenIG2 in /opt/forgerock/OpenIG2/conf/web.xml. Refer https://tomcat.apache.org/tomcat-7.0-doc/config/filter.html#CORS_Filter for sample CORS filter configuration.
7. Start both OpenIG instances. Verify there are no errors in Apache Tomcat and OpenIG logs. 

OpenAM Installation & Configuration:
====================================
1. Create 2 realms: employees and customers
2. Add users:
3. Create OpenID connect agent in employees with creds: employeeApp:password

OpenIG1:
1. Add configs and routes

OpenIG2:
1. Add configs and routes
2. Disable CORS filter, Sample below:

OpenIG Use Cases testing:
========================= 
1. OpenIG-OpenAM PEP for web applications
2. OpenIG-OpenAM PEP for REST APIs
3. OpenIG-OAuth2 RS
4. OpenIG-SAML SP
5. OpenIG-Credentials Replay-OpenAMAgent: Not yet implemented
6. OpenIG-Credentials Replay-File-DB: Not yet implemented
7. OpenIG-OIDC RP
8. OpenIG-UMA RS: Not yet implemented


SampleCompany URLs :
===========================
1. CommonServices direct: http://apis.sample.com:8010/history/emp1 <br />
   CommonServices via OpenIG: http://apis-ig.sample.com:9002/history/emp1
2. EmployeeApp direct: http://employees.sc.com:8002/employeeApp/#/ <br />
   EmployeeApp via OpenIG: http://employees-ig.sc.com:9000/employeeApp/#/
3. CustomerApp direct: http://employees.sc.com:8002/employeeApp/#/ <br />
   CustomerApp via OpenIG: http://employees-ig.sc.com:9000/employeeApp/#/
4. TravelApp direct: http://travel.sample.com:8012/travelApp/#/ <br />
   TravelApp via OpenIG: http://travel-ig.sample.com:9002/travelApp/#/
5. BenefitsApp direct: http://benefits.sample.com:8014/benefitsApp/#/ <br />
   BenefitsApp via OpenIG:: http://benefits-ig.sample.com:9002/benefitsApp/#/

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
