--psql -h localhost -U admin -d jobs_db3 -a -f jobs_db_script.sql

DROP TABLE IF EXISTS vacancy_work_types;
DROP TABLE IF EXISTS vacancy_employment_types;
DROP TABLE IF EXISTS vacancies;
DROP TABLE IF EXISTS companies;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS languages CASCADE;
DROP TABLE IF EXISTS companies;

CREATE TABLE languages (
  lang_id SERIAL PRIMARY KEY,
  lang_name VARCHAR (20) NOT NULL,
  lang_note VARCHAR (50),
  country_code VARCHAR (10) UNIQUE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP
);

INSERT INTO Languages VALUES(DEFAULT, 'English', 'English', 'en');
INSERT INTO Languages VALUES(DEFAULT, 'Ukrainian', 'Ukraine', 'ua');

CREATE EXTENSION ltree;

CREATE TABLE categories (
  category_id SERIAL PRIMARY KEY,
  category_name VARCHAR (150) UNIQUE NOT NULL,
  parent_id INT REFERENCES categories,
  parent_path LTREE,
  lang_id INT NOT NULL,
  tag VARCHAR (20) NOT NULL,
  is_visible BOOLEAN NOT NULL,
  is_active BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP
);

ALTER TABLE categories
  ADD CONSTRAINT LANG_CATEGORIES_FK FOREIGN KEY (lang_id) REFERENCES languages (lang_id);

CREATE INDEX categories_parent_path_idx ON categories USING GIST (parent_path);
CREATE INDEX categories_parent_id_idx ON categories (parent_id);

CREATE OR REPLACE FUNCTION update_categories_parent_path() RETURNS TRIGGER AS $$
    DECLARE
        path ltree;
    BEGIN
        IF NEW.parent_id IS NULL THEN
            NEW.parent_path = 'root'::ltree;
        ELSEIF TG_OP = 'INSERT' OR OLD.parent_id IS NULL OR OLD.parent_id != NEW.parent_id THEN
            SELECT parent_path || category_id::text FROM categories WHERE category_id = NEW.parent_id INTO path;
            IF path IS NULL THEN
                RAISE EXCEPTION 'Invalid parent_id %', NEW.parent_id;
            END IF;
            NEW.parent_path = path;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

DROP TRIGGER parent_path_tgr ON categories;

CREATE TRIGGER parent_path_tgr
    BEFORE INSERT OR UPDATE ON categories
    FOR EACH ROW EXECUTE PROCEDURE update_categories_parent_path();

INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Категорії', null, 'root', 2, 'category', true, true);
INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'IT, комп''ютери, інтернет', 1, 'root', 2, 'root', true, true);
DO $$
DECLARE
    c_id integer;
BEGIN
    SELECT max(category_id) FROM categories WHERE parent_id IS NULL INTO c_id;
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Адмiнiстрацiя, керівництво середньої ланки', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Будівництво, архітектура', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Бухгалтерія, аудит', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Готельно-ресторанний бізнес, туризм', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Дизайн, творчість', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'ЗМІ, видавництво, поліграфія', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Краса, фітнес, спорт', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Культура, музика, шоу-бізнес', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Логістика, склад, ЗЕД', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Маркетинг, реклама, PR', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Медицина, фармацевтика', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Нерухомість', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Освіта, наука', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Охорона, безпека', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Продаж, закупівля', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Робочі спеціальності, виробництво', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Роздрібна торгівля', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Секретаріат, діловодство, АГВ', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Сільське господарство, агробізнес', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Страхування', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Сфера обслуговування', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Телекомунікації та зв''язок', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Топменеджмент, керівництво вищої ланки', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Транспорт, автобізнес', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Управління персоналом', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Фінанси, банк', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Юриспруденція', c_id, 'root', 2, 'root', true, true);
END$$;


INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Тип роботи', null, 'root', 2, 'work-types', true, true);

DO $$
DECLARE
    c_id integer;
BEGIN
    SELECT max(category_id) FROM categories INTO c_id;
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Офіс', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Віддалено', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Офіс/Віддалено', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Гібридний', c_id, 'root', 2, 'root', true, true);
END$$;


INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Тип найму', null, 'root', 2, 'emp-types', true, true);

DO $$
DECLARE
    c_id integer;
BEGIN
    SELECT max(category_id) FROM categories WHERE parent_id IS NULL INTO c_id;
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Повна зайнятість', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Часткова зайнятість', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Тимчасова', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Договір', c_id, 'root', 2, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Фрілансер', c_id, 'root', 2, 'root', true, true);
END$$;



INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Work Types', null, 'root', 1, 'root', true, true);

DO $$
DECLARE
    c_id integer;
BEGIN
    SELECT max(category_id) FROM categories WHERE parent_id IS NULL INTO c_id;
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Office', c_id, 'root', 1, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Remote', c_id, 'root', 1, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Office/Remote', c_id, 'root', 1, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Hybrid', c_id, 'root', 1, 'root', true, true);
END$$;


INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'Employment Types', null, 'root', 1, 'root', true, true);

DO $$
DECLARE
    c_id integer;
BEGIN
    SELECT max(category_id) FROM categories WHERE parent_id IS NULL INTO c_id;
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'full-time', c_id, 'root', 1, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'part-time', c_id, 'root', 1, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'temporary', c_id, 'root', 1, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'contract', c_id, 'root', 1, 'root', true, true);
    INSERT INTO categories (category_id, category_name, parent_id, parent_path, lang_id, tag, is_visible, is_active) VALUES (DEFAULT, 'freelance', c_id, 'root', 1, 'root', true, true);
END$$;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS btree_gin;
CREATE EXTENSION IF NOT EXISTS unaccent;

CREATE TABLE companies (
  company_id SERIAL PRIMARY KEY,
  company_name VARCHAR(150) NOT NULL,
  company_description TEXT NOT NULL,
  company_logo_path VARCHAR(150) NOT NULL,
  company_link VARCHAR(250) NOT NULL,
  is_visible BOOLEAN NOT NULL,
  is_active BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP,
  search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(company_name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(company_description, '')), 'B')
    ) STORED
);

--CREATE UNIQUE INDEX companies_id_index ON companies(company_id);

--ALTER TABLE companies
--    ADD CONSTRAINT PK_companies_table PRIMARY KEY USING INDEX companies_id_index;

CREATE INDEX companies_gin_index ON companies USING GIN (search_vector);

--CREATE INDEX companies_description_index ON companies USING GIST (to_tsvector('english', company_description));

CREATE TABLE vacancies (
  vacancy_id SERIAL PRIMARY KEY,
  company_id INT NOT NULL references companies(company_id),
  category_id INT NOT NULL references categories(category_id) CHECK (category_id >= 2 and category_id <= 23),
  vacancy_title VARCHAR(250) NOT NULL,
  vacancy_description TEXT NOT NULL,
  is_visible BOOLEAN NOT NULL,
  is_active BOOLEAN NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  modified_at TIMESTAMP,
  search_vector tsvector GENERATED ALWAYS AS (
        setweight(to_tsvector('english', coalesce(vacancy_title, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(vacancy_description, '')), 'B')
    ) STORED
);

CREATE INDEX vacancies_gin_index ON vacancies USING GIN (search_vector);

DO $$
DECLARE
    c_id integer;
    work_type_id_min integer;
    work_type_id_max integer;
BEGIN
    SELECT t.category_id FROM categories t WHERE t.parent_id IS NULL and t.tag='work-types' AND t.lang_id=2 INTO c_id;
    SELECT min(t.category_id) FROM categories t WHERE t.parent_id=c_id AND t.lang_id=2 INTO work_type_id_min;
    SELECT max(t.category_id) FROM categories t WHERE t.parent_id=c_id AND t.lang_id=2 INTO work_type_id_max;

    RAISE NOTICE 'work_type_id_min: %', work_type_id_min;
    RAISE NOTICE 'work_type_id_min: %', work_type_id_max;
    CREATE TABLE vacancy_work_types (
       vacancy_id INT NOT NULL references vacancies(vacancy_id),
       work_type_id INT NOT NULL CHECK (work_type_id >= 31 and work_type_id <= 34),
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       CONSTRAINT fk_vacancy FOREIGN KEY(vacancy_id) REFERENCES vacancies(vacancy_id)
    );
END$$;

DO $$
DECLARE
    c_id integer;
    emp_type_id_min integer;
    emp_type_id_max integer;
BEGIN
    SELECT t.category_id FROM categories t WHERE t.parent_id IS NULL and t.tag='emp-types' AND t.lang_id=2 INTO c_id;
    SELECT min(t.category_id) FROM categories t WHERE t.parent_id=c_id AND t.lang_id=2 INTO emp_type_id_min;
    SELECT max(t.category_id) FROM categories t WHERE t.parent_id=c_id AND t.lang_id=2 INTO emp_type_id_max;

    RAISE NOTICE 'emp_type_id_min: %', emp_type_id_min;
    RAISE NOTICE 'emp_type_id_max: %', emp_type_id_max;
    CREATE TABLE vacancy_employment_types (
       vacancy_id INT NOT NULL references vacancies(vacancy_id),
       employment_type_id INT NOT NULL CHECK (employment_type_id >= 36 and employment_type_id <= 40),
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       CONSTRAINT fk_vacancy FOREIGN KEY(vacancy_id) REFERENCES vacancies(vacancy_id)
    );
END$$;