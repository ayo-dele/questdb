/*******************************************************************************
 *    ___                  _   ____  ____
 *   / _ \ _   _  ___  ___| |_|  _ \| __ )
 *  | | | | | | |/ _ \/ __| __| | | |  _ \
 *  | |_| | |_| |  __/\__ \ |_| |_| | |_) |
 *   \__\_\\__,_|\___||___/\__|____/|____/
 *
 * Copyright (C) 2014-2016 Appsicle
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 ******************************************************************************/

package com.questdb.net.ha;

import com.questdb.JournalWriter;
import com.questdb.factory.configuration.JournalConfiguration;
import com.questdb.factory.configuration.JournalMetadata;
import com.questdb.factory.configuration.JournalStructure;
import com.questdb.model.Quote;
import com.questdb.net.ha.comsumer.HugeBufferConsumer;
import com.questdb.net.ha.producer.HugeBufferProducer;
import com.questdb.test.tools.AbstractTest;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class MetadataReplicationTest extends AbstractTest {
    @Test
    public void testReplication() throws Exception {

        try (JournalWriter w = getWriterFactory().writer(Quote.class)) {

            MockByteChannel channel = new MockByteChannel();
            HugeBufferProducer p = new HugeBufferProducer(new File(w.getMetadata().getLocation(), JournalConfiguration.FILE_NAME));
            HugeBufferConsumer c = new HugeBufferConsumer(new File(w.getMetadata().getLocation(), "_remote"));
            p.write(channel);
            c.read(channel);

            try (JournalWriter w2 = getWriterFactory().writer(
                    new JournalStructure(
                            new JournalMetadata(c.getHb())
                    ).location("xyz")
            )) {

                Assert.assertTrue(w.getMetadata().isCompatible(w2.getMetadata(), false));
            }
            p.free();
            c.free();
        }

    }
}
