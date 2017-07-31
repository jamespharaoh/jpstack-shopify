CREATE INDEX shopify_event_subject_pending
ON shopify_event_subject (id)
WHERE pending;