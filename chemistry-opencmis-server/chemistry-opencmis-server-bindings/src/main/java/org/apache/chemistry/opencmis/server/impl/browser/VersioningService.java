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
package org.apache.chemistry.opencmis.server.impl.browser;

import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_ALLOWABLE_ACTIONS;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_CHECKIN_COMMENT;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_FILTER;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_MAJOR;
import static org.apache.chemistry.opencmis.commons.impl.Constants.PARAM_TRANSACTION;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.RESOURCE_CONTENT;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileBaseUrl;
import static org.apache.chemistry.opencmis.server.impl.atompub.AtomPubUtils.compileUrl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.CONTEXT_OBJECT_ID;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createAddAcl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createContentStream;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createCookieValue;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createPolicies;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createProperties;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.createRemoveAcl;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.getSimpleObject;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.setCookie;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.writeEmpty;
import static org.apache.chemistry.opencmis.server.impl.browser.BrowserBindingUtils.writeJSON;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getBooleanParameter;
import static org.apache.chemistry.opencmis.server.shared.HttpUtils.getStringParameter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.impl.JSONConverter;
import org.apache.chemistry.opencmis.commons.impl.TypeCache;
import org.apache.chemistry.opencmis.commons.impl.server.TypeCacheImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Versioning Service operations.
 */
public class VersioningService {

    private VersioningService() {
    }

    /**
     * checkOut.
     */
    public static void checkOut(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String transaction = getStringParameter(request, PARAM_TRANSACTION);

        // execute
        Holder<String> checkOutId = new Holder<String>(objectId);
        service.checkOut(repositoryId, checkOutId, null, null);

        ObjectData object = getSimpleObject(service, repositoryId, checkOutId.getValue());
        if (object == null) {
            throw new CmisRuntimeException("PWC is null!");
        }

        // return object
        TypeCache typeCache = new TypeCacheImpl(repositoryId, service);
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false);

        // set headers
        String location = compileUrl(compileBaseUrl(request, repositoryId), RESOURCE_CONTENT, object.getId());

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setHeader("Location", location);

        response.setStatus(HttpServletResponse.SC_CREATED);
        setCookie(request, response, repositoryId, transaction,
                createCookieValue(HttpServletResponse.SC_CREATED, object.getId(), null, null));

        writeJSON(jsonObject, request, response);
    }

    /**
     * checkOut.
     */
    public static void cancelCheckOut(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);

        // execute
        service.cancelCheckOut(repositoryId, objectId, null);

        response.setStatus(HttpServletResponse.SC_OK);
        writeEmpty(request, response);
    }

    /**
     * checkIn.
     */
    public static void checkIn(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        Boolean major = getBooleanParameter(request, PARAM_MAJOR);
        String checkinComment = getStringParameter(request, PARAM_CHECKIN_COMMENT);
        String transaction = getStringParameter(request, PARAM_TRANSACTION);

        // execute
        ControlParser cp = new ControlParser(request);
        TypeCache typeCache = new TypeCacheImpl(repositoryId, service);
        Holder<String> objectIdHolder = new Holder<String>(objectId);

        service.checkIn(repositoryId, objectIdHolder, major, createProperties(cp, null, typeCache),
                createContentStream(request), checkinComment, createPolicies(cp), createAddAcl(cp),
                createRemoveAcl(cp), null);

        String newObjectId = (objectIdHolder.getValue() == null ? objectId : objectIdHolder.getValue());

        ObjectData object = getSimpleObject(service, repositoryId, newObjectId);
        if (object == null) {
            throw new CmisRuntimeException("New version is null!");
        }

        // return object
        JSONObject jsonObject = JSONConverter.convert(object, typeCache, false);

        String location = compileUrl(compileBaseUrl(request, repositoryId), RESOURCE_CONTENT, object.getId());

        response.setStatus(HttpServletResponse.SC_CREATED);
        response.setHeader("Location", location);

        setCookie(request, response, repositoryId, transaction,
                createCookieValue(HttpServletResponse.SC_CREATED, object.getId(), null, null));

        writeJSON(jsonObject, request, response);
    }

    @SuppressWarnings("unchecked")
    public static void getAllVersions(CallContext context, CmisService service, String repositoryId,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        // get parameters
        String objectId = (String) context.get(CONTEXT_OBJECT_ID);
        String filter = getStringParameter(request, PARAM_FILTER);
        Boolean includeAllowableActions = getBooleanParameter(request, PARAM_ALLOWABLE_ACTIONS);

        // execute
        List<ObjectData> versions = service.getAllVersions(repositoryId, objectId, null, filter,
                includeAllowableActions, null);

        if (versions == null) {
            throw new CmisRuntimeException("Versions are null!");
        }

        TypeCache typeCache = new TypeCacheImpl(repositoryId, service);
        JSONArray jsonVersions = new JSONArray();
        for (ObjectData version : versions) {
            jsonVersions.add(JSONConverter.convert(version, typeCache, false));
        }

        response.setStatus(HttpServletResponse.SC_OK);
        writeJSON(jsonVersions, request, response);
    }
}
