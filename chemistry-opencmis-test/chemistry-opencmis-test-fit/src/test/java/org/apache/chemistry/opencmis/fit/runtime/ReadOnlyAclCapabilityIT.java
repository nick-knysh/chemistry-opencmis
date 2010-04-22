/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.fit.runtime;

import java.util.List;

import org.apache.chemistry.opencmis.commons.api.AclCapabilities;
import org.apache.chemistry.opencmis.commons.api.PermissionMapping;
import org.apache.chemistry.opencmis.commons.api.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.api.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class ReadOnlyAclCapabilityIT extends AbstractSessionTest {

    private AclCapabilities aclCapabilities = null;

    @Before
    public void setup() throws Exception {
        RepositoryInfo r = this.session.getRepositoryInfo();
        // capabilities
        RepositoryCapabilities repcap = r.getCapabilities();
        CapabilityAcl capacl = repcap.getAclCapability();

        if (capacl != CapabilityAcl.NONE) {
            // acl capabilities
            this.aclCapabilities = r.getAclCapabilities();
        }

        Assume.assumeNotNull(this.aclCapabilities);
    }

    @Test
    public void repositoryCapabilitiesAclPropagation() {
        AclPropagation aclprop = this.aclCapabilities.getAclPropagation();
        switch (aclprop) {
        case OBJECTONLY:
            break;
        case PROPAGATE:
            break;
        case REPOSITORYDETERMINED:
            break;
        default:
            Assert.fail("enumeration not supported");
        }
    }

    @Test
    public void repositoryCapabilitiesAclPermissionMapping() {
        PermissionMapping apm = this.aclCapabilities.getPermissionMapping()
                .get(PermissionMapping.CAN_ADD_POLICY_OBJECT);
        List<String> aclps = apm.getPermissions();
        Assert.assertNotNull(aclps);
    }
}
