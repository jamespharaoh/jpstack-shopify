package wbs.integrations.shopify.model;

import java.io.Serializable;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.utils.time.interval.TextualInterval;

import wbs.web.misc.HttpMethod;

@Accessors (fluent = true)
@Data
public
class ShopifyOutboundLogSearch
	implements Serializable {

	Long accountId;

	TextualInterval timestamp;

	HttpMethod method;
	String path;

	Boolean success;

}
