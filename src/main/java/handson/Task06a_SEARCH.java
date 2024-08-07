package handson;

import com.commercetools.api.client.ProjectApiRoot;
import com.commercetools.api.models.category.Category;
import com.commercetools.api.models.category.CategoryReference;
import com.commercetools.api.models.category.CategoryReferenceBuilder;
import com.commercetools.api.models.product.*;
import handson.impl.ApiPrefixHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static handson.impl.ClientService.createApiClient;

public class Task06a_SEARCH {

    public static void main(String[] args) throws Exception {

        final String apiClientPrefix = ApiPrefixHelper.API_DEV_CLIENT_PREFIX.getPrefix();

        final ProjectApiRoot client = createApiClient(apiClientPrefix);
        Logger logger = LoggerFactory.getLogger(Task06a_SEARCH.class.getName());

        Category seedCategory = client
                .categories()
                .withKey("plant-seeds")
                .get()
                .execute()
                .toCompletableFuture().get()
                .getBody();

        // to get categoryReference
        CategoryReference seedCategoryReference =
                CategoryReferenceBuilder.of()
                        .id(seedCategory.getId())
                        .build();

        // filter from product projection query response

        // the effective filter from the search response
        // params found in the product projection search https://docs.commercetools.com/api/projects/products-search#search-productprojections
        ProductProjectionPagedSearchResponse productProjectionPagedSearchResponse =
            client
                // TODO Get all products
                .productProjections()
                .search()
                .get()
                .withStaged(false)
                // TODO Restrict on category plant-seeds
                .withMarkMatchingVariants(true)
                .withFilterQuery("categories.id:\"" + seedCategoryReference.getId() + "\"")
                // TODO Get all Facets for Enum size and Number weight_in_kg
                //.withFacet("variants.attributes.size")
                //.addFacet("variants.attributes.weight_in_kg:range (0 to 1), (1 to 5), (5 to 20)")
                // TODO Give price range on products with no effect on facets
                //.withFilter("variants.price.centAmount:range (100 to 100000)")
                // TODO: with effect on facets
                //.addFilterQuery("variants.price.centAmount:range (100 to 100000)")
                // TODO: Simulate click on facet box from attribute size
                //.withFilterFacets("variants.attributes.size:\"box\"")
                .executeBlocking()
                .getBody();


        int size = productProjectionPagedSearchResponse.getResults().size();
        logger.info("No. of products: " + size);
        List<ProductProjection> result =  productProjectionPagedSearchResponse.getResults().subList(0, size);
        System.out.println("products searched: ");
        result.forEach((r) -> System.out.println(r.getKey()));

        logger.info("Facet count: " + productProjectionPagedSearchResponse.getFacets().values().size());
        logger.info("Facets: " + productProjectionPagedSearchResponse.getFacets().values().keySet());
        for (String facet: productProjectionPagedSearchResponse.getFacets().values().keySet()){
            FacetResult facetResult = productProjectionPagedSearchResponse.getFacets().withFacetResults(FacetResultsAccessor::asFacetResultMap).get(facet);
            if (facetResult instanceof RangeFacetResult) {
                logger.info("No. of Ranges: {}", ((RangeFacetResult)facetResult).getRanges().size());
                logger.info("Facet Result: {}", ((RangeFacetResult)facetResult).getRanges().stream().map(facetResultRange -> facetResultRange.getFromStr() + " to " + facetResultRange.getToStr() + ": " + facetResultRange.getCount()).collect(Collectors.toList()));
            }
            else if (facetResult instanceof TermFacetResult) {
                logger.info("No. of Terms: {}", ((TermFacetResult)facetResult).getTerms().size());
                logger.info("Facet Result: {}", ((TermFacetResult)facetResult).getTerms().stream().map(facetResultTerm -> facetResultTerm.getTerm() + ": " + facetResultTerm.getCount()).collect(Collectors.joining(", ")));
            }
        }

        client.close();
    }
}
