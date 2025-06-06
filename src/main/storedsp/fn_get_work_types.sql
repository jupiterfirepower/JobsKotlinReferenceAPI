DROP FUNCTION IF EXISTS fn_get_work_types;

CREATE OR REPLACE FUNCTION fn_get_work_types()
RETURNS TABLE ("id" INTEGER, "name" VARCHAR, "parentId" INTEGER, "langId" INTEGER, "created" TIMESTAMP, "modified" TIMESTAMP)
AS
$$
BEGIN
--RETURN QUERY SELECT "t"."WorkTypeId", "t"."WorkTypeName", "t"."Created", "t"."Modified" FROM "WorkTypes" t;
RETURN QUERY SELECT "t"."category_id", "t"."category_name", "t"."parent_id", "t"."lang_id", "t"."created_at", COALESCE ("t"."modified_at", "t"."created_at") as modified_at
             FROM "categories" t
             WHERE "t"."parent_path" <@ 'root.30';
END
$$ LANGUAGE plpgsql;