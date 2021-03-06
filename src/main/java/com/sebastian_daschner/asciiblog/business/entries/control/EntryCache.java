/*
 * Copyright (C) 2015 Sebastian Daschner, sebastian-daschner.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebastian_daschner.asciiblog.business.entries.control;

import com.sebastian_daschner.asciiblog.business.entries.entity.Entry;
import com.sebastian_daschner.asciiblog.business.environment.control.FileConfig;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class EntryCache {

    /**
     * The blog entries identified by the entry name.
     */
    private Map<String, Entry> entries;
    private DB mapDB;

    @Inject
    @FileConfig(FileConfig.Location.MAP_DB)
    File mapDBFile;

    @PostConstruct
    public void initCache() {
        mapDB = DBMaker.newFileDB(mapDBFile)
                // TODO perf test
//                .mmapFileEnable()
                .make();
        entries = mapDB.getHashMap("asciiblog-entries");
    }

    @PreDestroy
    public void closeCache() {
        mapDB.close();
    }

    public List<Entry> getLastEntries(final int number) {
        return entries.values().stream()
                .sorted(Comparator.comparing(Entry::getDate).reversed())
                .limit(number)
                .collect(Collectors.toList());
    }

    public List<Entry> getAllEntries() {
        return entries.values().stream()
                .sorted(Comparator.comparing(Entry::getDate).reversed())
                .collect(Collectors.toList());
    }

    public Entry get(final String name) {
        return entries.get(name);
    }

    public void store(final Entry entry) {
        entries.put(entry.getLink(), entry);
        mapDB.commit();
    }

    public void remove(final String name) {
        entries.remove(name);
        mapDB.commit();
    }

}
