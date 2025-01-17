/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.index.shard;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.CompositeReaderContext;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.store.BaseDirectoryWrapper;
import org.opensearch.common.lucene.index.OpenSearchDirectoryReader;
import org.opensearch.core.internal.io.IOUtils;
import org.opensearch.test.OpenSearchTestCase;

import java.io.IOException;

public class ShardUtilsTests extends OpenSearchTestCase {

    public void testExtractShardId() throws IOException {
        BaseDirectoryWrapper dir = newDirectory();
        IndexWriter writer = new IndexWriter(dir, newIndexWriterConfig());
        writer.commit();
        ShardId id = new ShardId("foo", "_na_", random().nextInt());
        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            OpenSearchDirectoryReader wrap = OpenSearchDirectoryReader.wrap(reader, id);
            assertEquals(id, ShardUtils.extractShardId(wrap));
        }
        final int numDocs = 1 + random().nextInt(5);
        for (int i = 0; i < numDocs; i++) {
            Document d = new Document();
            d.add(newField("name", "foobar", StringField.TYPE_STORED));
            writer.addDocument(d);
            if (random().nextBoolean()) {
                writer.commit();
            }
        }

        try (DirectoryReader reader = DirectoryReader.open(writer)) {
            OpenSearchDirectoryReader wrap = OpenSearchDirectoryReader.wrap(reader, id);
            assertEquals(id, ShardUtils.extractShardId(wrap));
            CompositeReaderContext context = wrap.getContext();
            for (LeafReaderContext leaf : context.leaves()) {
                assertEquals(id, ShardUtils.extractShardId(leaf.reader()));
            }
        }
        IOUtils.close(writer, dir);
    }
}
