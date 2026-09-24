package io.github.lilaschuda.guanaco.demo.unoq;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import io.github.lilaschuda.guanaco.context.GuanacoContext;
import io.github.lilaschuda.guanaco.demo.unoq.route.LegacyJavaRouteBuilder;
import org.springframework.context.support.StaticApplicationContext;

/**
 *
 * @author lila
 */
public class Application {

    public static void main(String[] args) throws Exception {
        GuanacoContext ctx = new GuanacoContext(Application.class.getPackageName());
        StaticApplicationContext sac = new StaticApplicationContext();
        
        ctx.setApplicationContext(sac);
        configure(ctx);
        ctx.wireRoutes();
        GuanacoUartBridge.start(ctx);
        //ctx.addRoutes(new LegacyJavaRouteBuilder());
        ctx.start();
        
        Runtime.getRuntime().addShutdownHook(new Thread(ctx::stop));
        Thread.currentThread().join();
    }
    
    private static void configure(GuanacoContext ctx){
        ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");
        ctx.getRegistry().bind("jmsConnectionFactory", cf);
        JmsComponent jms = JmsComponent.jmsComponentAutoAcknowledge(cf);
        ctx.addComponent("jms", jms);
    }
    
}
