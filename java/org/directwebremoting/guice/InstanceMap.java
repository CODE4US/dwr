/*
 * Copyright 2007 Tim Peierls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.directwebremoting.guice;

import java.util.concurrent.ConcurrentMap;

import com.google.inject.Key;

/**
 * A specialization of ConcurrentMap with keys of type {@code Key<T>} and 
 * values of type {@code InstanceProvider<T>}.
 * @author Tim Peierls [tim at peierls dot net]
 */
public interface InstanceMap<T> extends ConcurrentMap<Key<T>, InstanceProvider<T>>
{
}