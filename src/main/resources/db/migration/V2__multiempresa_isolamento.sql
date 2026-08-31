-- Migração Multiempresa - Isolamento por Empresa
-- Versão: V2
-- Descrição: Adiciona colunas empresa_id e usuario_criador_id para isolamento multiempresa
-- ATENÇÃO: Este arquivo deve ser executado manualmente em produção após testes completos
-- O desenvolvimento local usará ddl-auto=update do Hibernate

-- 1. Adicionar coluna empresa_id em engenheiro (nullable inicialmente)
ALTER TABLE engenheiro ADD COLUMN IF NOT EXISTS empresa_id BIGINT;

-- 2. Adicionar coluna empresa_id em analise (nullable inicialmente)
ALTER TABLE analise ADD COLUMN IF NOT EXISTS empresa_id BIGINT;

-- 3. Adicionar coluna usuario_criador_id em analise (nullable inicialmente)
ALTER TABLE analise ADD COLUMN IF NOT EXISTS usuario_criador_id BIGINT;

-- 4. Criar empresa padrão para migrar dados existentes
INSERT INTO empresa (nome, cnpj, ativo, data_criacao)
VALUES ('Empresa Padrão', NULL, true, NOW())
ON CONFLICT DO NOTHING
RETURNING id;

-- 5. Migrar engenheiros existentes para a empresa padrão
-- Substitua 1 pelo ID da empresa criada no passo anterior
UPDATE engenheiro
SET empresa_id = (SELECT id FROM empresa WHERE nome = 'Empresa Padrão' LIMIT 1)
WHERE empresa_id IS NULL;

-- 6. Migrar análises existentes para a empresa padrão
-- Substitua 1 pelo ID da empresa criada no passo anterior
UPDATE analise
SET empresa_id = (SELECT id FROM empresa WHERE nome = 'Empresa Padrão' LIMIT 1)
WHERE empresa_id IS NULL;

-- 7. Opcional: Migrar usuário criador das análises
-- Isso requer identificar qual usuário criou cada análise
-- Se não tiver essa informação, pode deixar NULL por enquanto
-- UPDATE analise SET usuario_criador_id = 1 WHERE usuario_criador_id IS NULL;

-- 8. Adicionar FKs após migrar os dados
ALTER TABLE engenheiro
ADD CONSTRAINT fk_engenheiro_empresa
FOREIGN KEY (empresa_id) REFERENCES empresa(id);

ALTER TABLE analise
ADD CONSTRAINT fk_analise_empresa
FOREIGN KEY (empresa_id) REFERENCES empresa(id);

ALTER TABLE analise
ADD CONSTRAINT fk_analise_usuario_criador
FOREIGN KEY (usuario_criador_id) REFERENCES usuario(id);

-- 9. Tornar empresa_id NOT NULL após migração completa
-- Só execute isso após garantir que todos os dados foram migrados
-- ALTER TABLE engenheiro ALTER COLUMN empresa_id SET NOT NULL;
-- ALTER TABLE analise ALTER COLUMN empresa_id SET NOT NULL;

-- 10. Criar índices para performance
CREATE INDEX IF NOT EXISTS idx_engenheiro_empresa ON engenheiro(empresa_id);
CREATE INDEX IF NOT EXISTS idx_analise_empresa ON analise(empresa_id);
CREATE INDEX IF NOT EXISTS idx_analise_usuario_criador ON analise(usuario_criador_id);
