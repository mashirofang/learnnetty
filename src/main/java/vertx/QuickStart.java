package vertx;

import io.vertx.core.Vertx;

public class QuickStart
{

    public static void main(String[] args)
        throws Exception
    {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Server());
    }

}
