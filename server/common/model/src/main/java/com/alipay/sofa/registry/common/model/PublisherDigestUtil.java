/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.common.model;

import com.alipay.sofa.registry.common.model.dataserver.DatumSummary;
import com.alipay.sofa.registry.common.model.dataserver.DatumDigest;
import com.google.common.collect.Maps;

import java.util.*;

/**
 * @author kezhu.wukz
 * @author shangyu.wh
 * @version $Id: PublisherDigestUtil.java, v 0.1 2019-05-30 20:58 shangyu.wh Exp $
 */
public final class PublisherDigestUtil {
    private PublisherDigestUtil() {
    }

    public static Map<String, DatumDigest> digest(Map<String, DatumSummary> summaryMap) {
        if (summaryMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, RegisterVersion>> sorted = Maps.newTreeMap();
        for (Map.Entry<String, DatumSummary> summaryEntry : summaryMap.entrySet()) {
            sorted.put(summaryEntry.getKey(), new TreeMap<>(summaryEntry.getValue()
                .getPublisherVersions()));
        }

        final Map<String, DatumDigest> ret = Maps.newHashMapWithExpectedSize(summaryMap.size());
        for (Map.Entry<String, Map<String, RegisterVersion>> e : sorted.entrySet()) {
            int publisherNum = 0;
            long publisherIdSign = 0;
            long publisherVerSign = 0;
            long publisherTimestampSign = 0;

            final String dataInfoId = e.getKey();
            publisherNum += e.getValue().size();

            for (Map.Entry<String, RegisterVersion> pub : e.getValue().entrySet()) {
                final String registerId = pub.getKey();
                final long digestRegisterId = digest(registerId);
                publisherIdSign = publisherIdSign * 31 + digestRegisterId;
                final RegisterVersion ver = pub.getValue();
                publisherVerSign = 31 * publisherVerSign + ver.getVersion();
                publisherTimestampSign = 31 * publisherTimestampSign + ver.getRegisterTimestamp();
            }
            ret.put(dataInfoId, new DatumDigest(publisherNum, publisherIdSign, publisherVerSign,
                publisherTimestampSign));
        }
        return ret;
    }

    private static int digest(String str) {
        // use string.hashCode, it's the fastest. the calc result has cached.
        // but must pay attention to the compatibility of different jdk versions
        // after jdk1.2, the java doc promise:
        // The hash code for a String object is computed as
        // s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
        return str.hashCode();
    }
}