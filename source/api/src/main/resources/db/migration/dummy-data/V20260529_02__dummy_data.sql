/*
 * Dummy data to be able to run some API endpoints tests.
 */

INSERT INTO calculation_versions (calculation_version_id, name) VALUES (1, '2025.2');

INSERT INTO substances (substance_id, name) VALUES (1, 'NOx');
INSERT INTO substances (substance_id, name) VALUES (2, 'NH3');

INSERT INTO result_types (result_type_id, name) VALUES (1, 'Deposition');
INSERT INTO result_types (result_type_id, name) VALUES (2, 'Concentration');

INSERT INTO source_characteristics (
	source_characteristic_id,
	heat_content,
	height,
	spread,
	emission_diurnal_variation
) VALUES (1, 1050, 10, 10, 2);

INSERT INTO mesh_points (mesh_point_id, x, y) VALUES (1, 100001, 400001);
