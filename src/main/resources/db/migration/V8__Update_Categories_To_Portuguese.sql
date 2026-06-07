-- V8__Update_Categories_To_Portuguese.sql

UPDATE categories 
SET name = 'Cuidados com a pele', slug = 'cuidados-com-a-pele'
WHERE id = 2;

UPDATE categories 
SET name = 'Cuidados com o corpo', slug = 'cuidados-com-o-corpo'
WHERE id = 3;

UPDATE categories 
SET name = 'Cuidados com o cabelo', slug = 'cuidados-com-o-cabelo'
WHERE id = 4;

UPDATE categories 
SET name = 'Acessórios', slug = 'acessorios'
WHERE id = 5;

-- Add Kits e Presentes
INSERT INTO categories (id, name, slug, description) 
VALUES (6, 'Kits e Presentes', 'kits-e-presentes', 'Kits exclusivos para presentear')
ON CONFLICT (slug) DO NOTHING;
