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

import java.util.ArrayList;
import java.util.List;

/**
 * wrapper that gathers information around the cleanup
 */
public class TypeCleanupInfo {
    List<String> paths = new ArrayList<String>();
    /**
     * this is meant to store parameterized path by users (for removal) that have been
     * ignored either because they are in conflict with configuration, or because they
     * don't exist
     */
    List<String> ignoredPaths = new ArrayList<String>();

    long nbResourceParsed = 0;

    public void add(String path){
        paths.add(path);
    }

    public void addIgnoredPath(String path) {
        ignoredPaths.add(path);
    }

    public void traverse() {
        nbResourceParsed ++;
    }

    public List<String> getPaths() {
        return paths;
    }

    public long getNbResourceParsed() {
        return nbResourceParsed;
    }

    public List<String> getIgnoredPaths() {
        return ignoredPaths;
    }
}
