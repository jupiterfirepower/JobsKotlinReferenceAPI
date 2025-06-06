DROP FUNCTION IF EXISTS fn_get_emp_types;

CREATE OR REPLACE FUNCTION fn_get_emp_types()
RETURNS TABLE ("id" INTEGER, "name" VARCHAR, "parentId" INTEGER, "langId" INTEGER, "created" TIMESTAMP, "modified" TIMESTAMP)
AS
$$
BEGIN
RETURN QUERY SELECT "t"."category_id", "t"."category_name", "t"."parent_id", "t"."lang_id", "t"."created_at", COALESCE ("t"."modified_at", "t"."created_at") as modified_at
             FROM "categories" t
             WHERE "t"."parent_path" <@ 'root.35';
END
$$ LANGUAGE plpgsql;