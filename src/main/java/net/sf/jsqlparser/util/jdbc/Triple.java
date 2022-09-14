/*-
 * #%L
 * JSQLParser library
 * %%
 * Copyright (C) 2004 - 2022 JSQLParser
 * %%
 * Dual licensed under GNU LGPL 2.1 or Apache License 2.0
 * #L%
 */
package net.sf.jsqlparser.util.jdbc;

import java.util.Objects;

class Triple<S, T, U> extends Tuple<S, T> {
  private U third;

  public Triple(S first, T second, U third) {
    super(first, second);
    this.third = third;
  }

  public U getThird() {
    return third;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Triple)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
    return Objects.equals(third, triple.third);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), third);
  }
}
