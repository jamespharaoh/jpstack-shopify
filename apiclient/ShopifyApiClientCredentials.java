package wbs.integrations.shopify.apiclient;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors (fluent = true)
@Data
public
class ShopifyApiClientCredentials {

	String storeName;
	String username;
	String password;

}
