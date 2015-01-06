Apache Sling Type Cleanup
=========================

Apache Sling services for cleaning up resources whose types don't exist.

1. First install the bundle
2. configure it (i.e. set the resource type prefixes you want to check your repository against, the resource type prefixes you *don't* want
the tool to collect)
3. then a servlet is ready to serve the "sling/typecleanup" resource type, you have to create a resource entry point with that type, with correct
ACLs, let's say at /var/resourceCleanup
4. Running
```GET /var/resourceCleanup.txt?path="/content"```
 against your sling instance will return you the number of resource traversed and the obsolete paths, e.g.:
```
16732 resources traversed, 2 obsolete resources
/content/obsolete/resource,
/content/another/obsolete/resource
done.
```
5. Running
```POST /var/resourceCleanup.txt?action=cleanup&paths="/content/obsolete/resource,
                                                      /content/some/user/error,
                                                      /content/another/obsolete/resource"```
against your sling instance will return you the treatement result with here ```/content/some/user/error``` ignored (and spit out in the return message)


