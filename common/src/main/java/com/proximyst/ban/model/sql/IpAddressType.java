package com.proximyst.ban.model.sql;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum IpAddressType {
  IPV4(0),
  IPV6(1),
  ;

  private static final Map<Byte, IpAddressType> IP_ADDRESS_TYPES = Arrays.stream(values())
      .collect(Collectors.toUnmodifiableMap(IpAddressType::type, Function.identity()));

  private final byte type;

  IpAddressType(final int type) {
    this.type = (byte) type;
  }

  public byte type() {
    return type;
  }

  public static @NonNull Optional<@NonNull IpAddressType> fromType(final byte type) {
    return Optional.ofNullable(IP_ADDRESS_TYPES.get(type));
  }
}
