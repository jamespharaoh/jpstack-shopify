CREATE INDEX shopify_event_pending
ON shopify_event (id)
WHERE pending;

CREATE UNIQUE INDEX shopify_metafield_owner
ON shopify_metafield (account_id, owner_resource, owner_id);