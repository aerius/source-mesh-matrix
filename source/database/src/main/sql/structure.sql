/*
 * Switch to the default smm database.
 * Make sure this is equal to the database name in the Dockerfile.
 */
USE smm;

/*
 * calculation_versions
 * --------------------
 * Dimension table for calculation versions.
 */
CREATE TABLE IF NOT EXISTS calculation_versions (
	calculation_version_id Int16,
	name String
) ENGINE = MergeTree()
ORDER BY calculation_version_id;

/*
 * substances
 * ----------
 * Dimension table for substances.
 */
CREATE TABLE IF NOT EXISTS substances (
	substance_id Int16,
	name String
) ENGINE = MergeTree()
ORDER BY substance_id;

/*
 * source_characteristics
 * ----------------------
 * Dimension table for source characteristics.
 */
CREATE TABLE IF NOT EXISTS source_characteristics (
	source_characteristic_id Int16,
	heat_content Float32,
	height Float32,
	spread Float32,
	emission_diurnal_variation Int16
) ENGINE = MergeTree()
ORDER BY source_characteristic_id;

/*
 * result_types
 * ------------
 * Dimension table for result types.
 */
CREATE TABLE IF NOT EXISTS result_types (
	result_type_id Int16,
	name String
) ENGINE = MergeTree()
ORDER BY result_type_id;

/*
 * mesh_points
 * -----------
 * Dimension table for mesh points.
 */
CREATE TABLE IF NOT EXISTS mesh_points (
	mesh_point_id Int32,
	x Int32,
	y Int32
) ENGINE = MergeTree()
ORDER BY mesh_point_id;

/*
 * matrix
 * ------
 * Fact table for matrix values.
 *
 * ORDER BY / sort key: most selective / frequently filtered dimensions first (e.g. calculation_version_id, substance_id, mesh_point_id) so the primary index prunes well.
 */
CREATE TABLE IF NOT EXISTS matrix (
	calculation_version_id Int16,
	substance_id Int16,
	result_type_id Int16,
	source_characteristic_id Int16,
	source_x Int32,
	source_y Int32,
	mesh_point_id Int32,
	value Float32
) ENGINE = MergeTree()
-- Order by most selective / frequently filtered dimensions first (e.g. calculation_version_id, substance_id, mesh_point_id) so the primary index prunes well.
ORDER BY (
	calculation_version_id,
	substance_id,
	result_type_id,
	mesh_point_id,
	source_characteristic_id,
	source_x,
	source_y
);
