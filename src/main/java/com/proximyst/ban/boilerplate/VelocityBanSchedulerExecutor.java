package com.proximyst.ban.boilerplate;

import com.proximyst.ban.BanPlugin;
import java.util.concurrent.Executor;
import org.checkerframework.checker.nullness.qual.NonNull;

public final class VelocityBanSchedulerExecutor implements Executor {
  @NonNull
  private final BanPlugin main;

  public VelocityBanSchedulerExecutor(@NonNull BanPlugin main) {
    this.main = main;
  }

  @Override
  public void execute(@NonNull Runnable command) {
    main.getProxyServer().getScheduler().buildTask(main, command).schedule();
  }
}