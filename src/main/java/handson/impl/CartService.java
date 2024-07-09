package handson.impl;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.cart.*;
import com.commercetools.api.models.channel.Channel;
import com.commercetools.api.models.channel.ChannelResourceIdentifier;
import com.commercetools.api.models.channel.ChannelResourceIdentifierBuilder;
import com.commercetools.api.models.customer.Customer;
import com.commercetools.api.models.shipping_method.ShippingMethod;
import com.commercetools.api.models.shipping_method.ShippingMethodResourceIdentifierBuilder;
import io.vrap.rmf.base.client.ApiHttpResponse;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**

 */
public class CartService {

    final ProjectApiRoot apiRoot;

    public CartService(final ProjectApiRoot client) {
        this.apiRoot = client;
    }


    /**
     * Creates a cart for the given customer.
     *
     * @return the customer creation completion stage
     */

    public CompletableFuture<ApiHttpResponse<Cart>> getCartById(final String cartId) {

        return
                apiRoot
                        .carts()
                        .withId(cartId)
                        .get()
                        .execute();
    }

    public CompletableFuture<ApiHttpResponse<Cart>> createCart(final ApiHttpResponse<Customer> customerApiHttpResponse) {

        final Customer customer = customerApiHttpResponse.getBody();

        return
            apiRoot
                .carts()
                .post(
                    CartDraftBuilder.of()
                        .currency("EUR")
                        .deleteDaysAfterLastModification(90L)
                        .customerEmail(customer.getEmail())
                        .customerId(customer.getId())
                        .country(
                           customer.getAddresses()
                               .stream()
                               .filter(address -> address.getId().equals(customer.getDefaultShippingAddressId()))
                               .findFirst()
                               .orElse(null)
                               .getCountry()
                        )
                        .shippingAddress(
                            customer.getAddresses()
                                .stream()
                                .filter(address -> address.getId().equals(customer.getDefaultShippingAddressId()))
                                .findFirst()
                                .orElse(null)
                        )
                        .inventoryMode(InventoryMode.NONE)
                        .build()
                )
                .execute();
    }


    public CompletableFuture<ApiHttpResponse<Cart>> createAnonymousCart() {

        return
                apiRoot
                        .carts()
                        .post(
                                CartDraftBuilder.of()
                                        .currency("EUR")
                                        .deleteDaysAfterLastModification(90L)
                                        .anonymousId("an" + System.nanoTime())
                                        .country("DE")
                                        .build()
                        )
                        .execute();
    }


    public CompletableFuture<ApiHttpResponse<Cart>> addProductToCartBySkusAndChannel(
            final ApiHttpResponse<Cart> cartApiHttpResponse,
            final Channel channel,
            final String ... skus) {

        final Cart cart = cartApiHttpResponse.getBody();

        List<CartUpdateAction> addLineItemActions = Stream.of(skus)
            .map(sku ->
                CartAddLineItemActionBuilder.of()
                    .sku(sku)
                    .quantity(1L)
                    .supplyChannel(
                        ChannelResourceIdentifierBuilder.of()
                            .id(channel.getId())
                            .build()
                    )
                    .build()
            )
            .collect(Collectors.toList());

        return apiRoot
            .carts()
            .withId(cart.getId())
            .post(
                CartUpdateBuilder.of()
                    .version(cart.getVersion())
                    .actions(
                        addLineItemActions
                    )
                    .build()
            )
            .execute();
    }

    public CompletableFuture<ApiHttpResponse<Cart>> addDiscountToCart(
            final ApiHttpResponse<Cart> cartApiHttpResponse, final String code) {

        final Cart cart = cartApiHttpResponse.getBody();
        return apiRoot
            .carts()
            .withId(cart.getId())
            .post(
                CartUpdateBuilder.of()
                    .version(cart.getVersion())
                    .actions(
                        CartAddDiscountCodeActionBuilder.of()
                            .code(code)
                            .build()
                    )
                    .build()
            )
            .execute();
    }

    public CompletableFuture<ApiHttpResponse<Cart>> recalculate(final ApiHttpResponse<Cart> cartApiHttpResponse) {
        final Cart cart = cartApiHttpResponse.getBody();
        return apiRoot
            .carts()
            .withId(cart.getId())
            .post(
                CartUpdateBuilder.of()
                    .version(cart.getVersion())
                    .actions(
                        CartRecalculateActionBuilder.of()
                            .build()
                    )
                    .build()
            ).execute();
    }

    public CompletableFuture<ApiHttpResponse<Cart>> setShipping(final ApiHttpResponse<Cart> cartApiHttpResponse) {
        final Cart cart = cartApiHttpResponse.getBody();
        return apiRoot
            .carts()
            .withId(cart.getId())
            .post(
                ShippingBuilder.of()
                    .version(cart.getVersion())
                    .actions(
                        CartRecalculateActionBuilder.of()
                            .build()
                    )
                    .build()
            ).execute();
    }



}
