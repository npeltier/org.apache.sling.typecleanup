/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.typecleanup;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;

@SlingServlet(
    label = "Apache Sling Type Cleanup list",
    description = "list 'obsolete' resources, being resources whose types don't exist in the repository",
    resourceTypes   = TypeCleanupServlet.RESOURCE_TYPE,
    methods = {"GET", "POST"},
    extensions = {"txt"}
)
@Service
@Properties({
    @Property(name = Constants.SERVICE_VENDOR, value = "The Apache Software Foundation"),
    @Property(name = Constants.SERVICE_DESCRIPTION, value = "list 'obsolete' resources, being resources whose types don't exist in the repository")
})
public class TypeCleanupServlet extends SlingAllMethodsServlet {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String RESOURCE_TYPE = "sling/typecleanup";

    private static final String PARAM_PATH = "path";

    @Reference
    TypeCleanupService typeCleanupService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        try {
            if (!typeCleanupService.isConfigured()) {
                response.getWriter().append("not configured.");
            } else {
                String path = (String) request.getParameter(PARAM_PATH);
                if (StringUtils.isBlank(path)){
                    response.getWriter().append("no path provided in parameter.");
                } else {
                    Resource resource = request.getResourceResolver().getResource(path);
                    TypeCleanupInfo infos  = typeCleanupService.buildCleanupInfo(resource);
                    response.getWriter().printf("%d resources traversed, %d obsolete resources\n", infos.getNbResourceParsed(), infos.getPaths().size());
                    for (String obsoletePath : infos.getPaths()){
                        response.getWriter().printf("%s\n", obsoletePath);
                    }
                    response.getWriter().printf("done.");
                }
            }
            response.flushBuffer();
        } catch(Exception e){
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }
}
