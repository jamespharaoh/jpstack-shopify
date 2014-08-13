---------------------------------------- TABLE message

ALTER TABLE MESSAGE
ADD CHECK (
	(direction = 0 AND status IN (0, 1, 7, 8, 9))
OR	(direction = 1 AND status IN (0, 2, 3, 4, 5, 6, 10, 11, 12)));

CREATE UNIQUE INDEX message_other_id
ON message (route_id, direction, other_id);

---------------------------------------- TABLE message_tag

CREATE TABLE message_tag (
	message_id int REFERENCES message,
	tag text NOT NULL,
	PRIMARY KEY (message_id, tag)
);

---------------------------------------- INSERT object_type

SELECT object_type_insert (
	'message',
	'message',
	'root',
	3);

---------------------------------------- TRIGGER message

CREATE OR REPLACE FUNCTION message_after_insert ()
RETURNS trigger AS $$

BEGIN

	INSERT INTO message_stats_queue (
		service_id,
		route_id,
		affiliate_id,
		batch_id,
		network_id,
		date,
		direction,
		status,
		diff)
	VALUES (
		NEW.service_id,
		NEW.route_id,
		NEW.affiliate_id,
		NEW.batch_id,
		NEW.network_id,
		NEW.date,
		NEW.direction,
		NEW.status,
		1);

	RETURN NULL;

END;

$$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION message_after_update ()
RETURNS trigger AS $$

BEGIN

	IF OLD.service_id != NEW.service_id
		OR OLD.route_id != NEW.route_id
		OR OLD.affiliate_id != NEW.affiliate_id
		OR OLD.batch_id != NEW.batch_id
		OR OLD.network_id != NEW.network_id
		OR OLD.date != NEW.date
		OR OLD.direction != NEW.direction
		OR OLD.status != NEW.status
	THEN

		INSERT INTO message_stats_queue (
			service_id,
			route_id,
			affiliate_id,
			batch_id,
			network_id,
			date,
			direction,
			status,
			diff)
		VALUES (
			OLD.service_id,
			OLD.route_id,
			OLD.affiliate_id,
			OLD.batch_id,
			OLD.network_id,
			OLD.date,
			OLD.direction,
			OLD.status,
			-1);

		INSERT INTO message_stats_queue (
			service_id,
			route_id,
			affiliate_id,
			batch_id,
			network_id,
			date,
			direction,
			status,
			diff)
		VALUES (
			NEW.service_id,
			NEW.route_id,
			NEW.affiliate_id,
			NEW.batch_id,
			NEW.network_id,
			NEW.date,
			NEW.direction,
			NEW.status,
			1);

	END IF;

	IF OLD.status != NEW.status
		AND NEW.delivery_type_id IS NOT NULL
	THEN

		INSERT INTO delivery (
			id,
			message_id,
			old_message_status,
			new_message_status)
		VALUES (
			nextval ('delivery_id_seq'),
			NEW.id,
			OLD.status,
			NEW.status);

	END IF;

	RETURN NULL;

END;

$$ LANGUAGE 'plpgsql';

CREATE TRIGGER message_after_insert
AFTER INSERT ON message
FOR EACH ROW EXECUTE PROCEDURE message_after_insert ();

CREATE TRIGGER message_after_update
AFTER UPDATE ON message
FOR EACH ROW EXECUTE PROCEDURE message_after_update ();