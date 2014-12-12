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
package org.apache.sling.typecleanup.impl;

import aQute.bnd.maven.support.Repo;
import org.apache.commons.lang3.StringUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.typecleanup.TypeCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

@Component
@Service
@Properties({
        @Property(name=TypeCleanupServiceImpl.PROP_INCLUSIONS),
        @Property(name=TypeCleanupServiceImpl.PROP_EXCLUSIONS)
})
public class TypeCleanupServiceImpl implements TypeCleanupService{
    Logger logger = LoggerFactory.getLogger(this.getClass());

    public static final String PROP_INCLUSIONS = "org.apache.sling.typecleanup.inclusions";

    public static final String PROP_EXCLUSIONS = "org.apache.sling.typecleanup.exclusions";

    /**
     * set of resource types patterns resource will be checked against
     */
    List<String> checkedResourceTypes;

    /**
     * possible exclusions
     */
    List<String> excludedResourceTypes;

    @Reference
    ResourceResolverFactory factory;

    @Activate
    private void activate(Dictionary properties) {
        logger.debug("activate");
        configure(properties);
    }

    @Modified
    private void modified(final Dictionary properties) {
        logger.debug("modified");
        configure(properties);
    }

    @Deactivate
    private void deactivate() {
        logger.debug("deactivate");
    }

    private synchronized void configure(final Dictionary properties) {
        logger.info("configuring Type cleanup");
        String[] inclusions = PropertiesUtil.toStringArray(properties.get(PROP_INCLUSIONS));
        String[] exclusions = PropertiesUtil.toStringArray(properties.get(PROP_EXCLUSIONS));
        checkedResourceTypes = inclusions != null ? Arrays.asList(inclusions) : null;
        excludedResourceTypes = exclusions != null ? Arrays.asList(exclusions) : null;
    }

    @Override
    public boolean isConfigured() {
        return checkedResourceTypes != null && !checkedResourceTypes.isEmpty();
    }

    /**
     * Check if a resource type exists with a checkResolver's session (resource's session
     * might not have read access to the resource type)
     * @param checkResolver
     * @param resource
     * @return
     */
    private boolean resourceTypeExists(ResourceResolver checkResolver, Resource resource){
        return (checkResolver.resolve(resource.getResourceType()) != null);
    }

    /**
     * Is this resource type included in the check?
     * @param resourceType
     * @return
     */
    private boolean isTypeIncluded(String resourceType){
        boolean included = false;
        if (StringUtils.isNotBlank(resourceType)) {
            for (String prefix : checkedResourceTypes) {
                if (resourceType.startsWith(prefix)){
                    included = true;
                    break;
                }
            }
            if (included){
                for (String prefix : excludedResourceTypes) {
                    if (resourceType.startsWith(prefix)){
                        return false;
                    }
                }
            }
        }
        return included;
    }

    /**
     * traverse the resource tree recursively and list obsolete paths as follow:
     * - if resource's type belongs to the patterns to check and don't exist, add it to the list.
     * - other wise look at children and re-iterate.
     * @param paths
     * @param checker
     * @param resource
     * @return
     */
    private void fillObsoletePaths(final List<String> paths, ResourceResolver checker, Resource resource){
        if (isTypeIncluded(resource.getResourceType()) && !resourceTypeExists(checker, resource)){
            paths.add(resource.getPath());
        } else {
            for (Iterator<Resource> childIterator = resource.listChildren(); childIterator.hasNext(); ){
                fillObsoletePaths(paths, checker, childIterator.next());
            }
        }
    }

    @Override
    public List<String> getObsoletePaths(Resource root) {
        List<String> paths = new ArrayList<String>();
        ResourceResolver checker = null;
        try {
            if (isConfigured()) {
                checker = factory.getAdministrativeResourceResolver(null);
                fillObsoletePaths(paths, checker, root);
            }
        } catch (Exception e){
            logger.error("Unable to properly retrieve the paths", e);
        } finally {
            if (checker != null){
                checker.close();
            }
        }
        return paths;
    }

    @Override
    public void removeObsoleteResources(Resource root) throws RepositoryException {
        List<String> paths = getObsoletePaths(root);
        removeResources(root.getResourceResolver(), paths);
    }

    @Override
    public void removeResources(ResourceResolver resolver, List<String> paths) throws RepositoryException {
        logger.info("Starting to remove {} obsolete resources", paths.size());
        Session session = resolver.adaptTo(Session.class);
        for (int pathIndex = 0; pathIndex < paths.size(); pathIndex ++){
            String path = paths.get(pathIndex);
            Resource resource = resolver.getResource(path);
            Node node = resource != null ? resource.adaptTo(Node.class) : null;
            if (node != null){
                node.remove();
                if (pathIndex % 1000 == 0){
                    logger.info("persisting changes...");
                    session.save();
                }
            }
        }
        if (session.hasPendingChanges()){
            logger.info("persisting changes...");
            session.save();
        }
        logger.info("done");
    }

}
