/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jupiter.common.util;

import org.jupiter.common.util.internal.UnsafeReferenceFieldUpdater;
import org.jupiter.common.util.internal.UnsafeUpdater;

/**
 * 基于 {@link ThreadLocal} 的 {@link StringBuilder} 重复利用
 *
 * 注意不要在相同的线程中嵌套使用
 *
 * jupiter
 * org.jupiter.common.util
 *
 * @author jiachun.fjc
 */
public class StringBuilderHelper {

    private static final UnsafeReferenceFieldUpdater<StringBuilder, char[]> bufferUpdater =
            UnsafeUpdater.newReferenceFieldUpdater(StringBuilder.class.getSuperclass(), "value");

    private static final int DISCARD_LIMIT = 1024 << 3; // 8k

    private static final ThreadLocal<StringBuilderHolder>
            threadLocalStringBuilderHolder = new ThreadLocal<StringBuilderHolder>() {

        @Override
        protected StringBuilderHolder initialValue() {
            return new StringBuilderHolder();
        }
    };

    public static StringBuilder get() {
        StringBuilderHolder holder = threadLocalStringBuilderHolder.get();
        return holder.getStringBuilder();
    }

    public static void truncate() {
        StringBuilderHolder holder = threadLocalStringBuilderHolder.get();
        holder.truncate();
    }

    private static class StringBuilderHolder {

        private final StringBuilder buf = new StringBuilder();

        private StringBuilder getStringBuilder() {
            truncate();
            return buf;
        }

        private void truncate() {
            if (buf.capacity() > DISCARD_LIMIT) {
                bufferUpdater.set(buf, new char[1024]);
            }
            buf.setLength(0);
        }
    }
}
