package wbs.integrations.shopify.metamodel;

import static wbs.utils.collection.CollectionUtils.emptyList;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

import wbs.framework.component.annotations.PrototypeComponent;
import wbs.framework.data.annotations.DataChildren;
import wbs.framework.data.annotations.DataClass;
import wbs.framework.entity.meta.model.ModelMetaData;

@Accessors (fluent = true)
@Data
@DataClass ("shopify-synchronisation")
@PrototypeComponent ("shopifySynchronisationSpec")
@ModelMetaData
public
class ShopifySynchronisationSpec {

	@DataChildren (
		childrenElement = "scalar-fields",
		childElement = "field",
		valueAttribute = "name")
	List <String> scalarFieldNames =
		emptyList ();

	@DataChildren (
		childrenElement = "collection-fields",
		childElement = "field",
		valueAttribute = "name")
	List <String> collectionFieldNames =
		emptyList ();

}
