/*
 * #%L
 * Text: A text model with range-based markup via standoff annotations.
 * %%
 * Copyright (C) 2010 - 2011 The Interedition Development Group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package eu.interedition.text;

import com.google.common.base.Function;
import eu.interedition.text.query.Criterion;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public interface AnnotationRepository {

  SortedSet<Name> names(Text text);

  Iterable<Annotation> create(Annotation... annotations);

  Iterable<Annotation> create(Iterable<Annotation> annotations);

  Iterable<Annotation> create(Map<Annotation, Map<Name, String>> annotations);

  void scroll(Criterion criterion, AnnotationCallback callback);

  Iterable<Annotation> find(Criterion criterion);

  void scroll(Criterion criterion, Set<Name> names, AnnotationCallback callback);

  Map<Annotation, Map<Name, String>> find(Criterion criterion, Set<Name> names);

  void delete(Iterable<Annotation> annotations);

  void delete(Annotation... annotations);

  void delete(Criterion criterion);

  void set(Map<Annotation, Map<Name, String>> data);

  void unset(Map<Annotation, Iterable<Name>> data);

  void transform(Criterion criterion, Text to, Function<Annotation, Annotation> transform);

  void transform(Map<Annotation, Map<Name, String>> annotations, Text to, Function<Annotation, Annotation> transform);

  interface AnnotationCallback {

    void annotation(Annotation annotation, Map<Name, String> data);

  }
}
