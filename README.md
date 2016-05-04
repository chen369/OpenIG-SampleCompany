# OpenIG-SampleCompany

*OpenIG configuration files for SampleCompany*


Disclaimer of Liability :
=========================
The code for this project is only meant for DEMO purposes only and is not PRODUCTION ready. This is a sample implementation only and is not supported by ForgeRock. 
I make no warranty, representation or undertaking whether expressed or implied, nor does it assume any legal liability, whether direct or indirect, or responsibility for the accuracy, 
completeness, or usefulness of any information. 

Further analysis on the detailed requirements, fine-tuning and validation of the proposed architecture is still required by the selected Systems Integrator and/or Architects in charge of 
architecting the IAM project. 

Pre-requisites :
================
1. OpenIG & OpenAM is deployed and configured.
2. REST API server is up and running. This sample uses OpenDJ as REST API server.
3. "OpenDJ Authorization header" filter is required for this sample as OpenDJ is used as REST API server. This filter can be customized as required by specific REST application.
4. The server hosting OpenIG should have internet connectivity as first request tries to download required jars from maven repo. The custom groovy script uses @Grab and it downloads the required dependencies under <User-Home>/.groovy/grapes.
   
OpenIG Configuration:
=====================


Testing:
======== 
Curl command(s):
Note the first request may take few minutes as it downloads the required jars from maven repo.



Disclaimer of Liability :
=========================
The code for this project is only meant for DEMO purposes only and is not PRODUCTION ready. This is a sample implementation only and is not supported by ForgeRock. 
I make no warranty, representation or undertaking whether expressed or implied, nor does it assume any legal liability, whether direct or indirect, or responsibility for the accuracy, 
completeness, or usefulness of any information. 

Further analysis on the detailed requirements, fine-tuning and validation of the proposed architecture is still required by the selected Systems Integrator and/or Architects in charge of 
architecting the IAM project.

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
