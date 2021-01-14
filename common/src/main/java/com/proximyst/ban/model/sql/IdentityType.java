package com.proximyst.ban.model.sql;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum IdentityType {
  UUID(0),
  IPV4(1),
  IPV6(2),
  CONSOLE(3),
  ;

  private static final Map<Byte, IdentityType> IDENTITY_TYPES = Arrays.stream(values())
      .collect(Collectors.toUnmodifiableMap(IdentityType::type, Function.identity()));

  private final byte type;

  IdentityType(final int type) {
    this.type = (byte) type;
  }

  public byte type() {
    return type;
  }

  public static @NonNull Optional<@NonNull IdentityType> fromType(final byte type) {
    return Optional.ofNullable(IDENTITY_TYPES.get(type));
  }
}
