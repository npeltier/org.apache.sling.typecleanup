package org.apache.sling.typecleanup.impl;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.apache.sling.typecleanup.TypeCleanupInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * unit testing TypeCleanup Service
 */
public class TypeCleanupServiceImplTest {

    TypeCleanupServiceImpl service;
    ResourceResolver resolver;

    @Rule
    public final SlingContext context = new SlingContext();

    @Before
    public void init() {
        service = new TypeCleanupServiceImpl();
        service.checkedResourceTypes = Arrays.asList(new String[] {"/apps/blah","/libs"});
        service.excludedResourceTypes = Arrays.asList(new String[] {"/apps/blah/ignored", "/libs/ignored"});
        resolver = context.resourceResolver();
        ContentLoader contentLoader = new ContentLoader(resolver);
        contentLoader.json("/contentloader/resourceTypes.json", "/apps");
        contentLoader.json("/contentloader/toClean.json", "/content");
    }

    @Test
    public void testCleanupLists() {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(TypeCleanupServiceImpl.PROP_INCLUSIONS, new String[] {"","/apps/blah","", "/libs",""});
        properties.put(TypeCleanupServiceImpl.PROP_EXCLUSIONS, new String[]{"", "/apps/blah/ignored", "", "/libs/ignored", ""});
        Assert.assertEquals(2, service.checkedResourceTypes.size());
        Assert.assertEquals(2, service.excludedResourceTypes.size());
    }

    @Test
    public void testTypeInclusion() {
        Assert.assertFalse("configured exclusion is ignored", service.isTypeIncluded("/apps/blah/ignored"));
        Assert.assertFalse("configured exclusion is ignored", service.isTypeIncluded("/libs/ignored"));
        Assert.assertFalse("descendant of configured exclusion is ignored", service.isTypeIncluded("/apps/blah/ignored/foo"));
        Assert.assertFalse("unconfigured inclusion is ignored", service.isTypeIncluded("/apps/foo"));
        Assert.assertTrue("configured inclusion is included", service.isTypeIncluded("/apps/blah"));
        Assert.assertTrue("configured inclusion is included", service.isTypeIncluded("/libs"));
        Assert.assertTrue("descendants are included", service.isTypeIncluded("/apps/blah/foo"));
        Assert.assertTrue("descendants are included", service.isTypeIncluded("/libs/foo"));
    }

    @Test
    public void testIsObsolete() {
        Assert.assertTrue("resource is obsolete", service.isObsolete(resolver.getResource("/content/toClean/notexisting")));
        Assert.assertFalse("resource is not obsolete", service.isObsolete(resolver.getResource("/content/toKeep/notexistingButNotConfigured")));
        Assert.assertFalse("resource is not obsolete", service.isObsolete(resolver.getResource("/content/toKeep/existsAndConfigured")));
        Assert.assertFalse("resource is not obsolete", service.isObsolete(resolver.getResource("/content/toKeep/existsAndNotConfigured")));
        Assert.assertFalse("resource is not obsolete", service.isObsolete(resolver.getResource("/content/toKeep/notExistingButExcluded")));
        Assert.assertFalse("resource is not obsolete", service.isObsolete(resolver.getResource("/content")));
    }

    @Test
    public void testCollectObsoletePaths() {

        TypeCleanupInfo infos = new TypeCleanupInfo();
        service.collectObsoletePaths(infos, resolver, resolver.getResource("/content"));
        Assert.assertEquals("There should be one cleaned up path from the sample content", 1, infos.getPaths().size());
        if (infos.getPaths().size() > 0){
            Assert.assertEquals("The path should be the parent, i.e. /content/toClean/notexisting", "/content/toClean/notexisting", infos.getPaths().get(0));
        }
    }
}
