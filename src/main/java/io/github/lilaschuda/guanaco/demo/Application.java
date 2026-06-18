package io.github.lilaschuda.guanaco.demo;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import io.github.lilaschuda.guanaco.core.GuanacoContext;
import org.springframework.context.support.StaticApplicationContext;

/**
 *
 * @author lila
 */
public class Application {

    public static void main(String[] args) throws Exception {
        GuanacoContext ctx = new GuanacoContext("org.guanaco.demo");
        StaticApplicationContext sac = new StaticApplicationContext();
        
        ctx.setApplicationContext(sac);
        configure(ctx);
        ctx.wireRoutes();
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
