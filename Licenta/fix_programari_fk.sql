-- Script pentru a corecta foreign key-ul din tabela programari
-- Foreign key-ul user_id trebuie să arate spre tabela 'imagini' (pacienți) nu 'utilizatori' (doctori)

USE photosolve;

-- 1. Șterge constraint-ul greșit
ALTER TABLE programari
DROP FOREIGN KEY FKsjgcqu0qbnnn58fwsbjj6e7ba;

-- 2. Creează constraint-ul corect care arată spre tabela imagini
ALTER TABLE programari
ADD CONSTRAINT FK_programari_imagini
FOREIGN KEY (user_id) REFERENCES imagini(id)
ON DELETE CASCADE
ON UPDATE CASCADE;

-- 3. Verifică constraint-ul nou creat
SELECT
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'photosolve'
    AND TABLE_NAME = 'programari'
    AND CONSTRAINT_NAME = 'FK_programari_imagini';

-- 4. Afișează toate constraint-urile din tabela programari pentru verificare
SELECT
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE
    TABLE_SCHEMA = 'photosolve'
    AND TABLE_NAME = 'programari'
    AND REFERENCED_TABLE_NAME IS NOT NULL;

