package com.github.brharrington.rxcurl;

import io.reactivex.netty.RxNetty;
import joptsimple.OptionParser;

import java.io.PrintStream;
import java.nio.charset.Charset;

/** Required javadoc for public class. */
public class Main {

  public static void main(String[] args, PrintStream out) {
    OptionParser parser = new OptionParser();
    parser.accepts("max-time").withRequiredArg().ofType(Integer.class);

    RxNetty.createHttpGet(args[0])
        .flatMap(response -> response.getContent())
        .map(data -> "Client => " + data.toString(Charset.defaultCharset()))
        .toBlocking().forEach(out::println);
  }

  public static void main(String[] args) {
    main(args, System.out);
  }
}
