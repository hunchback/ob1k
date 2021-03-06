package com.outbrain.ob1k.server.build;


import com.outbrain.ob1k.server.Server;
import com.outbrain.swinfra.metrics.api.MetricFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by aronen on 7/20/14.
 */
@Deprecated // use new extendable fluent builder in 'builder' package
public interface ExtraParamsPhase {
  ExtraParamsPhase acceptKeepAlive(final boolean keepAlive);
  ExtraParamsPhase supportZip(final boolean useZip);
  ExtraParamsPhase setMaxContentLength(final int maxContentLength);
  ExtraParamsPhase setRequestTimeout(final long timeout, TimeUnit unit);
  ExtraParamsPhase configureExecutorService(final int minSize, final int maxSize);
  ExtraParamsPhase setMetricFactory(final MetricFactory metricFactory);
  ExtraParamsPhase addListener(final Server.Listener listener);
}
