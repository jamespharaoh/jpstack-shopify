CREATE INDEX shopify_event_subject_pending
ON shopify_event_subject (id)
WHERE pending;

CREATE INDEX shopify_metafield_owner
ON shopify_metafield (account_id, owner_resource, owner_id);