DROP FUNCTION IF EXISTS fn_get_categories;

CREATE OR REPLACE FUNCTION fn_get_categories()
RETURNS TABLE ("id" INTEGER, "name" VARCHAR, "parentId" INTEGER, "langId" INTEGER, "created" TIMESTAMP, "modified" TIMESTAMP)
AS
DECLARE
    c_id integer;
$$
BEGIN
--SELECT category_id FROM categories WHERE parent_id IS NULL AND tag='category' INTO c_id;
RETURN QUERY SELECT "t"."category_id", "t"."category_name", "t"."parent_id", "t"."lang_id", "t"."created_at", COALESCE ("t"."modified_at", "t"."created_at") as modified_at
             FROM "categories" t
             WHERE "t"."parent_path" <@ 'root.1';
END
$$ LANGUAGE plpgsql;