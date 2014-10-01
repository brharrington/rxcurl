package com.github.brharrington.rxcurl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.ssl.DefaultFactories;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientBuilder;
import io.reactivex.netty.protocol.http.client.HttpClientRequest;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/** Required javadoc for public class. */
public class Main {

  public static void main(String[] args, PrintStream out) {
    OptionParser parser = new OptionParser();
    parser.accepts("max-time").withRequiredArg().ofType(Integer.class);
    OptionSet opts = parser.parse(args);

    final int maxTime = opts.has("max-time") ? (Integer) opts.valueOf("max-time") : 60;

    URI uri = URI.create(args[0]);
    int port = (uri.getPort() < 0)
        ? ("https".equals(uri.getScheme()) ? 443 : 80)
        : uri.getPort();

    HttpClient.HttpClientConfig config = new HttpClient.HttpClientConfig.Builder()
        .readTimeout(maxTime, TimeUnit.SECONDS)
        .build();

    HttpClientBuilder<ByteBuf, ByteBuf> clientBuilder =
        RxNetty.<ByteBuf, ByteBuf>newHttpClientBuilder(uri.getHost(), port).config(config);

    clientBuilder.channelOption(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

    if ("https".equals(uri.getScheme())) {
      clientBuilder.withSslEngineFactory(DefaultFactories.TRUST_ALL);
    }

    HttpClient<ByteBuf, ByteBuf> client = clientBuilder.build();
    client.submit(HttpClientRequest.createGet(uri.getPath()))
        .flatMap(response -> {
          System.out.println(response.getStatus());
          return response.getContent();
        })
        .map(data -> data.toString(Charset.defaultCharset()))
        .toBlocking().forEach(out::println);
  }

  public static void main(String[] args) {
    main(args, System.out);
  }
}
