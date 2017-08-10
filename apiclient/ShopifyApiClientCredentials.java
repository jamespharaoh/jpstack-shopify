package wbs.integrations.shopify.apiclient;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ShopifyApiClientCredentials {

	Long accountId;

	String storeName;
	String username;
	String password;

}
