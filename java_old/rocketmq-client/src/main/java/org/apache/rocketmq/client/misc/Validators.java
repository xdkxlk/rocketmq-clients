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

package org.apache.rocketmq.client.misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.ClientException;
import org.apache.rocketmq.client.exception.ErrorCode;
import org.apache.rocketmq.client.message.Message;

public class Validators {

    public static final String TOPIC_REGEX = "^[%|a-zA-Z0-9._-]+$";
    public static final Pattern TOPIC_PATTERN = Pattern.compile(TOPIC_REGEX);
    public static final int TOPIC_MAX_LENGTH = 255;

    public static final String GROUP_REGEX = TOPIC_REGEX;
    public static final Pattern GROUP_PATTERN = Pattern.compile(GROUP_REGEX);
    public static final int CONSUMER_GROUP_MAX_LENGTH = TOPIC_MAX_LENGTH;

    public static final int MESSAGE_BODY_MAX_SIZE = 1024 * 1024 * 4;

    public static final String INSTANCE_REGEX = "MQ_INST_\\w+_\\w+";

    public static final Pattern NAME_SERVER_ENDPOINT_PATTERN = Pattern.compile("^(\\w+://|).*");
    public static final Pattern NAME_SERVER_ENDPOINT_WITH_NAMESPACE_PATTERN =
            Pattern.compile("^(\\w+://|)" + INSTANCE_REGEX + "\\..*");

    private Validators() {
    }

    private static boolean regexNotMatched(String origin, Pattern pattern) {
        Matcher matcher = pattern.matcher(origin);
        return !matcher.matches();
    }

    public static void checkTopic(String topic) throws ClientException {
        if (StringUtils.isAnyBlank(topic)) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Topic is blank.");
        }
        if (regexNotMatched(topic, TOPIC_PATTERN)) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, String.format("Topic[%s] is illegal.", topic));
        }
        if (topic.length() > TOPIC_MAX_LENGTH) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT,
                                      "Topic's length exceeds the threshold, masSize=" + TOPIC_MAX_LENGTH + " bytes");
        }
    }

    public static void checkGroup(String group) throws ClientException {
        if (!StringUtils.isNoneBlank(group)) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Group is blank.");
        }
        if (regexNotMatched(group, GROUP_PATTERN)) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, String.format("Group[%s] is illegal.", group));
        }
        if (group.length() > CONSUMER_GROUP_MAX_LENGTH) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Group length exceeds the threshold, maxSize"
                                                                + CONSUMER_GROUP_MAX_LENGTH + " bytes");
        }
    }

    public static void checkMessage(Message message) throws ClientException {
        if (null == message) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Message is null.");
        }

        checkTopic(message.getTopic());
        final byte[] body = message.getBody();
        if (null == body) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Message body is null.");
        }
        if (0 >= body.length) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Message body's length is zero.");
        }
        if (body.length > MESSAGE_BODY_MAX_SIZE) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Message body's length exceeds the threshold, "
                                                                + "maxSize=" + MESSAGE_BODY_MAX_SIZE + " bytes.");
        }
    }

    public static void checkNamesrvAddr(String namesrvAddr) throws ClientException {
        if (!StringUtils.isNoneBlank(namesrvAddr)) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Name server address is blank.");
        }
        if (!NAME_SERVER_ENDPOINT_PATTERN.matcher(namesrvAddr).matches()
            && !NAME_SERVER_ENDPOINT_WITH_NAMESPACE_PATTERN.matcher(namesrvAddr).matches()) {
            throw new ClientException(ErrorCode.ILLEGAL_FORMAT, "Name server address is illegal.");
        }
    }
}