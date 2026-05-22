-- V3__Seed_Categories.sql
INSERT INTO categories (name, slug, description) VALUES
  ('Perfumes',     'perfumes',    'Fragrâncias exclusivas e sofisticadas'),
  ('Skincare',     'skincare',    'Cuidados premium para a pele'),
  ('Body Care',    'body-care',   'Produtos de cuidado corporal'),
  ('Hair Care',    'hair-care',   'Cuidados para cabelos'),
  ('Accessories',  'accessories', 'Acessórios de beleza')
ON CONFLICT (slug) DO NOTHING;
