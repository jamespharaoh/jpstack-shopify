package wbs.integrations.shopify.apiclient.product;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

import wbs.integrations.shopify.apiclient.ShopifyApiResponse;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyProductListResponse
	implements ShopifyApiResponse {

	@DataChildren (
		childrenElement = "products")
	List <ShopifyProductResponse> products =
		emptyList ();

}
