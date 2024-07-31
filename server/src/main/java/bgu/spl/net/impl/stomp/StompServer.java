package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.impl.echo.LineMessageEncoderDecoder;
import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        int port = 7777;
        // port = Integer.parseInt(args[0]);
        // if( args[1] == "tpc"){
        //         Server.threadPerClient(
        //             port, //port
        //             () -> new StompMessagingProtocolImpl(), //protocol factory
        //         LineMessageEncoderDecoder::new //message encoder decoder factory
        //     ).serve();
        // } 
        // if(args[1] == "reactor") {
            Server.reactor(
                    2,
                    port, //port
                    () ->  new StompMessagingProtocolImpl(), //protocol factory
                    LineMessageEncoderDecoder::new //message encoder decoder factory
            ).serve();
        // }
    }
}
