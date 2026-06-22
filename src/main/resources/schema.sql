PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS engenheiro (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome VARCHAR(150) NOT NULL,
    area VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS cat (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    engenheiro_id INTEGER NOT NULL,
    nome VARCHAR(150) NOT NULL,
    numero_cat VARCHAR(60) NOT NULL,
    municipio VARCHAR(120) NOT NULL,
    observacoes VARCHAR(1000),
    CONSTRAINT fk_cat_engenheiro
        FOREIGN KEY (engenheiro_id) REFERENCES engenheiro (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS cat_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    cat_id INTEGER NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    quantidade DECIMAL(15,2) NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    CONSTRAINT fk_cat_item_cat
        FOREIGN KEY (cat_id) REFERENCES cat (id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS analise (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    area VARCHAR(30) NOT NULL,
    data_criacao TEXT NOT NULL,
    resultado VARCHAR(20),
    cobertura DECIMAL(5,2)
);

CREATE TABLE IF NOT EXISTS analise_item (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    analise_id INTEGER NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    quantidade DECIMAL(15,2) NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    CONSTRAINT fk_analise_item_analise
        FOREIGN KEY (analise_id) REFERENCES analise (id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_engenheiro_area ON engenheiro (area);
CREATE INDEX IF NOT EXISTS idx_cat_engenheiro ON cat (engenheiro_id);
CREATE INDEX IF NOT EXISTS idx_cat_item_cat ON cat_item (cat_id);
CREATE INDEX IF NOT EXISTS idx_cat_item_descricao ON cat_item (descricao);
CREATE INDEX IF NOT EXISTS idx_analise_area ON analise (area);
CREATE INDEX IF NOT EXISTS idx_analise_item_analise ON analise_item (analise_id);
