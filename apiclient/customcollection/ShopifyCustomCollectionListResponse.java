package wbs.integrations.shopify.apiclient.customcollection;

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
class ShopifyCustomCollectionListResponse
	implements ShopifyApiResponse {

	@DataChildren (
		childrenElement = "custom_collections")
	List <ShopifyCustomCollectionResponse> collections =
		emptyList ();

}
