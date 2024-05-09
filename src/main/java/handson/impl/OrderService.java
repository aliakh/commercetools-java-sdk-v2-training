package handson.impl;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderState;
import com.commercetools.api.models.order.StagedOrderUpdateAction;
import com.commercetools.api.models.order_edit.OrderEdit;
import io.vrap.rmf.base.client.ApiHttpResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * This class provides operations to work with {@link Order}s.
 */
public class OrderService {

    final ProjectApiRoot apiRoot;

    public OrderService(final ProjectApiRoot client) {
        this.apiRoot = client;
    }

    public CompletableFuture<ApiHttpResponse<Order>> getOrderById(final String orderId) {
        return apiRoot
                .orders()
                .withId(orderId)
                .get()
                .execute();
    }

        public CompletableFuture<ApiHttpResponse<Order>> createOrder(final ApiHttpResponse<Cart> cartApiHttpResponse) {

        final Cart cart = cartApiHttpResponse.getBody();

        return
            apiRoot
                .orders()
                .post(
                    orderFromCartDraftBuilder -> orderFromCartDraftBuilder
                        .cart(cartResourceIdentifierBuilder -> cartResourceIdentifierBuilder.id(cart.getId()))
                        .version(cart.getVersion())
                )
                .execute();
    }


    public CompletableFuture<ApiHttpResponse<Order>> changeState(
            final ApiHttpResponse<Order> orderApiHttpResponse,
            final String storeKey,
            final OrderState state) {

        Order order = orderApiHttpResponse.getBody();

        return
            apiRoot
                .inStore(storeKey)
                .orders()
                .withId(order.getId())
                .post(
                    orderUpdateBuilder -> orderUpdateBuilder
                        .version(order.getVersion())
                        .plusActions(
                            orderUpdateActionBuilder -> orderUpdateActionBuilder.changeOrderStateBuilder()
                                .orderState(state)
                        )
                )
                .execute();
    }


    public CompletableFuture<ApiHttpResponse<Order>> changeWorkflowState(
            final ApiHttpResponse<Order> orderApiHttpResponse,
            final String storeKey,
            final String workflowStateKey) {

        Order order = orderApiHttpResponse.getBody();

        return
            apiRoot
                .inStore(storeKey)
                .orders()
                .withId(order.getId())
                .post(
                    orderUpdateBuilder -> orderUpdateBuilder
                        .version(order.getVersion())
                        .plusActions(
                            orderUpdateActionBuilder -> orderUpdateActionBuilder.transitionStateBuilder()
                                .state(stateResourceIdentifierBuilder -> stateResourceIdentifierBuilder.key(workflowStateKey))
                        )
                )
                .execute();
    }

    public CompletableFuture<ApiHttpResponse<OrderEdit>> getOrderEditById(
            final String orderEditId
            ) {

        return
                apiRoot
                        .orders()
                        .edits()
                        .withId(orderEditId)
                        .get()
                        .withExpand("order")
                        .execute();
    }

    public CompletableFuture<ApiHttpResponse<OrderEdit>> createOrderEdit(
            final ApiHttpResponse<Order> orderApiHttpResponse,
            final StagedOrderUpdateAction stagedOrderUpdateAction) {

        Order order = orderApiHttpResponse.getBody();

        return
                apiRoot
                        .orders()
                        .edits()
                        .post(
                                orderEditDraftBuilder -> orderEditDraftBuilder
                                        .stagedActions(stagedOrderUpdateAction)
                                        .resource(orderReferenceBuilder -> orderReferenceBuilder.id(order.getId()))
                        )
                        .execute();
    }

    public CompletableFuture<ApiHttpResponse<OrderEdit>> applyOrderEdit(
            final ApiHttpResponse<OrderEdit> orderEditApiHttpResponse) {

        OrderEdit orderEdit = orderEditApiHttpResponse.getBody();

        return
                apiRoot
                        .orders()
                        .edits()
                        .withId(orderEdit.getId())
                        .apply()
                        .post(
                                orderEditApplyBuilder -> orderEditApplyBuilder
                                        .editVersion(orderEdit.getVersion())
                                        .resourceVersion(orderEdit.getResource().getObj().getVersion())
                        )
                        .execute();
    }

}
