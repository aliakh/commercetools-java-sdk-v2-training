package handson.impl;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.common.LocalizedStringBuilder;
import com.commercetools.api.models.product.ProductResourceIdentifierBuilder;
import com.commercetools.api.models.product_selection.*;
import com.commercetools.api.models.store.*;
import com.commercetools.importapi.models.common.ProductKeyReferenceBuilder;
import io.vrap.rmf.base.client.ApiHttpResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**

 */
public class ProductSelectionService {

    final ProjectApiRoot apiRoot;

    public ProductSelectionService(final ProjectApiRoot client) {
        this.apiRoot = client;
    }


    /**
     * Gets a product selection by key.
     *
     * @return the product selection completion stage
     */
    public CompletableFuture<ApiHttpResponse<ProductSelection>> getProductSelectionByKey(final String productSelectionKey) {
        return
                apiRoot
                        .productSelections()
                        .withKey(productSelectionKey)
                        .get()
                        .execute();
    }

    /**
     * Gets a store by key.
     *
     * @return the store completion stage
     */
    public CompletableFuture<ApiHttpResponse<Store>> getStoreByKey(final String storeKey) {
        return
                apiRoot
                        .stores()
                        .withKey(storeKey)
                        .get()
                        .execute();
    }

    /**
     * Creates a new product selection.
     *
     * @return the product selection creation completion stage
     */
    public CompletableFuture<ApiHttpResponse<ProductSelection>> createProductSelection(final String productSelectionKey, final String name) throws ExecutionException, InterruptedException {
        Map<String, String> productSelectionNames = new HashMap<String, String>() {
            {
                put("DE", name);
                put("EN", name);
            }
        };

        return
            apiRoot
                .productSelections()
                .post(
                    ProductSelectionDraftBuilder.of()
                        .key(productSelectionKey)
                        .name(
                            LocalizedStringBuilder.of()
                                .values(productSelectionNames)
                                .build()
                        )
                        .build()
                )
                .execute();
    }


    public CompletableFuture<ApiHttpResponse<ProductSelection>> addProductToProductSelection(
            final ApiHttpResponse<ProductSelection> productSelectionApiHttpResponse,
            final String productKey) {

        final ProductSelection productSelection = productSelectionApiHttpResponse.getBody();

        return
            apiRoot
                .productSelections()
                .withId(productSelection.getId())
                .post(
                    ProductSelectionUpdateBuilder.of()
                        .version(productSelection.getVersion())
                        .actions(ProductSelectionAddProductActionBuilder.of()
                            .product(
                                ProductResourceIdentifierBuilder.of()
                                    .key(productKey)
                                    .build())
                            .build())
                        .build()
                )
                .execute();
    }

    public CompletableFuture<ApiHttpResponse<Store>> addProductSelectionToStore(
            final ApiHttpResponse<Store> storeApiHttpResponse,
            final ApiHttpResponse<ProductSelection> productSelectionApiHttpResponse) {

        final ProductSelection productSelection = productSelectionApiHttpResponse.getBody();
        final Store store = storeApiHttpResponse.getBody();

        return
            apiRoot
                .stores()
                .withId(store.getId())
                .post(
                    StoreUpdateBuilder.of()
                        .version(store.getVersion())
                        .actions(
                            StoreSetProductSelectionsActionBuilder.of()
                                .productSelections(
                                    ProductSelectionSettingDraftBuilder.of()
                                        .productSelection(
                                            ProductSelectionResourceIdentifierBuilder.of()
                                                .id(productSelection.getId())
                                                .build())
                                        .active(true)
                                        .build())
                                .build()
                        )
                        .build()
                )
                .execute();
    }

    public CompletableFuture<ApiHttpResponse<ProductSelectionProductPagedQueryResponse>> getProductsInProductSelection(
            final String productSelectionKey) {

        return
            apiRoot
                .productSelections()
                .withKey(productSelectionKey)
                .products()
                .get()
                .addExpand("product")
                .execute();;
    }

    public CompletableFuture<ApiHttpResponse<ProductsInStorePagedQueryResponse>> getProductsInStore(
            final String storeKey) {

        return
            apiRoot
                .inStore(storeKey)
                .productSelectionAssignments()
                .get()
                .addExpand("product")
                .addExpand("product-selection")
                .execute();
    }
}
