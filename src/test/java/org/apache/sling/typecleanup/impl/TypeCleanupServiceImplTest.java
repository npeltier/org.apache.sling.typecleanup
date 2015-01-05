package org.apache.sling.typecleanup.impl;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.apache.sling.testing.mock.sling.loader.ContentLoader;
import org.apache.sling.typecleanup.TypeCleanupInfo;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * unit testing TypeCleanup Service
 */
public class TypeCleanupServiceImplTest {

    TypeCleanupServiceImpl service;

    @Rule
    public final SlingContext context = new SlingContext();

    @Before
    public void init() {
        service = new TypeCleanupServiceImpl();
        service.checkedResourceTypes = Arrays.asList(new String[] {"/apps/blah","/libs"});
        service.excludedResourceTypes = Arrays.asList(new String[] {"/apps/blah/ignored", "/libs/ignored"});
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
    public void testCollectObsoletePaths() {
        ResourceResolver resolver = context.resourceResolver();
        ContentLoader contentLoader = new ContentLoader(resolver);
        contentLoader.json("/contentloader/resourceTypes.json", "/apps");
        contentLoader.json("/contentloader/toClean.json", "/content");

        List<String> paths = new ArrayList<String>();
        service.collectObsoletePaths(new TypeCleanupInfo(), resolver, resolver.getResource("/content"));
        Assert.assertEquals("There should be one cleaned up path from the sample content", 1, paths.size());
        if (paths.size() > 0){
            Assert.assertEquals("The path should be the parent, i.e. /content/toClean/notexisting", "/content/toClean/notexisting", paths.get(0));
        }
    }
}
