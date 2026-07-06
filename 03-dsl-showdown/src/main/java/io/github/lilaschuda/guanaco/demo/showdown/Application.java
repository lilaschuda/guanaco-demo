package io.github.lilaschuda.guanaco.demo.showdown;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
import io.github.lilaschuda.guanaco.core.GuanacoContext;
import io.github.lilaschuda.guanaco.demo.showdown.route.LegacyFluentShowdownRoute;
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
        ctx.addRoutes(new LegacyFluentShowdownRoute());
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
