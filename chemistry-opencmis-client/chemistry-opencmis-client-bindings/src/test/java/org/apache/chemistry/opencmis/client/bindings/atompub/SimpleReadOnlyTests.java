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
package org.apache.chemistry.opencmis.client.bindings.atompub;

import java.util.HashSet;
import java.util.Set;

import org.apache.chemistry.opencmis.client.bindings.framework.AbstractSimpleReadOnlyTests;
import org.apache.chemistry.opencmis.commons.api.CmisBinding;

/**
 * @author <a href="mailto:fmueller@opentext.com">Florian M&uuml;ller</a>
 * 
 */
public class SimpleReadOnlyTests extends AbstractSimpleReadOnlyTests {

    private Set<String> fTests;

    public SimpleReadOnlyTests() {
        fTests = new HashSet<String>();
        fTests.add(TEST_REPOSITORY_INFO);
        fTests.add(TEST_TYPES);
        fTests.add(TEST_NAVIGATION);
        fTests.add(TEST_CONTENT_STREAM);
        fTests.add(TEST_QUERY);
        fTests.add(TEST_CHECKEDOUT);
        fTests.add(TEST_CONTENT_CHANGES);
    }

    @Override
    protected CmisBinding createBinding() {
        // Add the default Java cookie manager from Java 1.6 to optimize
        // authentication
        // by reusing the common case where a token is stored in a cookie.
        // Note: Enable cookie management requires Java 1.6, uncomment the
        // following two lines to
        // enable cookie management for the tests.
        // java.net.CookieManager cm = new java.net.CookieManager(null,
        // CookiePolicy.ACCEPT_ALL);
        // java.net.CookieHandler.setDefault(cm);

        return AtomPubTestBindingFactory.createBinding(getAtomPubURL(), getUsername(), getPassword());
    }

    @Override
    protected Set<String> getEnabledTests() {
        return fTests;
    }
}
