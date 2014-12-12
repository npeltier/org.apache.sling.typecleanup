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

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.RepositoryException;
import java.util.List;

/**
 * Service offering to cleanup obsolete resources.
 * A resource is declared obsolete if:
 * - the current
 */
public interface TypeCleanupService {
    /**
     * Check wether the service has configuration for cleanup, if not configured, no cleanup should
     * happen.
     * @return
     */
    public boolean isConfigured();

    /**
     * Get obsolete resources (meaning resource with types not defined)
     * @param root
     * @return
     */
    public List<String> getObsoletePaths(Resource root) throws Exception;
    /**
     * Remove obsolete resources  (meaning resource with types not defined)
     */
    public void removeObsoleteResources(Resource root) throws RepositoryException;

    /**
     * Remove resources passed in parameter
     * @param resolver
     * @param paths
     */
    public void removeResources(ResourceResolver resolver, List<String> paths) throws RepositoryException;

}
