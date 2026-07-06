package io.github.lilaschuda.guanaco.demo.coexist.route;

import org.apache.camel.Exchange;
import io.github.lilaschuda.guanaco.annotation.GuanacoRoute;
import io.github.lilaschuda.guanaco.dsl.Processor;
import java.util.logging.Logger;

@GuanacoRoute
public class InventoryProcessor implements Processor<InventoryRoute<?>> {

    private static final Logger LOG = Logger.getLogger(InventoryProcessor.class.getName());

    @Override
    public InventoryRoute<?> process(Exchange exchange) {
        String body = exchange.getIn().getBody(String.class);
        
        boolean isItemInStock = body == null || !body.contains("BACKORDER_ITEM");
        boolean isAuditDiscrepancyFound = body != null && body.contains("STOCK_MISMATCH");

        LOG.info("Executing stock level validation against the Master Warehouse Inventory system...");

        if (isAuditDiscrepancyFound) {
            LOG.severe("CRITICAL ALERT: Physical vs. Digital counts mismatch! Routing to Inventory Audit Control.");
            return new ToInventoryFraudCheck(body);
        }

        if (!isItemInStock) {
            LOG.warning("Item backordered. Suspending shipment and routing to Bookkeeping for deferred revenue tracking.");
            return new ToBookkeeping(body);
        }

        LOG.info("Stock confirmed available. Sending pick-pack-and-ship instruction to local warehouse automation lines.");
        return new ToWarehouse(body);
    }
}