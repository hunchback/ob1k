package com.outbrain.gruffalo.netty;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.outbrain.metrics.MetricFactory;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

class MetricBatcher extends SimpleChannelInboundHandler<String> {

  private static final Logger log = LoggerFactory.getLogger(MetricBatcher.class);
  private static final AtomicInteger lastBatchSize = new AtomicInteger(0);
  private final int batchBufferCapacity;
  private final Counter connectionCounter;
  private final Counter metricsCounter;
  private final ChannelGroup activeChannels;
  private StringBuilder batch;
  private int currBatchSize;

  public MetricBatcher(final MetricFactory metricFactory, final int batchBufferCapacity, ChannelGroup activeChannels) {
    Preconditions.checkNotNull(metricFactory, "metricFactory may not be null");
    this.batchBufferCapacity = batchBufferCapacity;
    this.activeChannels = Preconditions.checkNotNull(activeChannels, "activeChannels must not be null");
    prepareNewBatch();

    String component = getClass().getSimpleName();
    connectionCounter = metricFactory.createCounter(component, "connections");
    metricsCounter = metricFactory.createCounter(component, "metricsReceived");
    metricFactory.createGauge(component, "batchSize", new Gauge<Integer>() {
      @Override
      public Integer value() {
        return lastBatchSize.get();
      }
    });
  }

  @Override
  public void channelRead0(final ChannelHandlerContext ctx, final String msg) throws Exception {
    currBatchSize++;
    if (batch.capacity() < batch.length() + msg.length()) {
      sendBatch(ctx);
    }

    batch.append(msg);
    metricsCounter.inc();
  }

  private void sendBatch(final ChannelHandlerContext ctx) {
    ctx.fireChannelRead(new Batch(batch, currBatchSize));
    prepareNewBatch();
  }

  private void prepareNewBatch() {
    batch = new StringBuilder(batchBufferCapacity);
    lastBatchSize.set(currBatchSize);
    currBatchSize = 0;
  }

  @Override
  public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      final IdleStateEvent e = (IdleStateEvent) evt;
      if (e.state() == IdleState.READER_IDLE && 0 < batch.length()) {
        sendBatch(ctx);
      }
    }
  }

  @Override
  public void channelRegistered(final ChannelHandlerContext ctx) throws Exception {
    if (ctx.channel().remoteAddress() != null) {
      connectionCounter.inc();
      activeChannels.add(ctx.channel());
    }
  }

  @Override
  public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
    connectionCounter.dec();
    try {
      sendBatch(ctx);
    } catch (final RuntimeException e) {
      log.warn("failed to send last batch when closing channel " + ctx.channel().remoteAddress());
    }
  }
}