package wbs.integrations.shopify.apiclient.collect;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;

import wbs.integrations.shopify.apiclient.ShopifyApiResponse;
import wbs.integrations.shopify.apiclient.metafield.ShopifyMetafieldResponse;

@Accessors (fluent = true)
@Data
@DataClass
public
class ShopifyCollectListResponse
	implements ShopifyApiResponse {

	@DataChildren (
		childrenElement = "collects")
	List <ShopifyCollectResponse> collects =
		emptyList ();

	@DataChildren (
		childrenElement = "metafields")
	List <ShopifyMetafieldResponse> metafields =
		emptyList ();

}
