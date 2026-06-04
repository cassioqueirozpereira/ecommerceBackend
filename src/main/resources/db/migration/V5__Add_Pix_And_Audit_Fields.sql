-- V5__Add_Pix_And_Audit_Fields.sql

-- Pix Manual: comprovante e dados do pagador
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS pix_receipt_url TEXT,
    ADD COLUMN IF NOT EXISTS payer_name      VARCHAR(150),
    ADD COLUMN IF NOT EXISTS payer_cpf       VARCHAR(14),
    ADD COLUMN IF NOT EXISTS payer_phone     VARCHAR(20);

-- Audit trail: aprovação/rejeição manual
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS approved_by  UUID,
    ADD COLUMN IF NOT EXISTS approved_at  TIMESTAMP,
    ADD COLUMN IF NOT EXISTS rejected_by  UUID,
    ADD COLUMN IF NOT EXISTS rejected_at  TIMESTAMP;

-- Converter payment_method para enum-compatível (string salva como texto)
-- O tipo do enum é gerenciado pelo JPA (@Enumerated(EnumType.STRING))
-- Garante que valores legados "MERCADOPAGO" sejam atualizados
UPDATE payments SET payment_method = 'MERCADO_PAGO_CARD'
    WHERE payment_method NOT IN ('PIX_MANUAL', 'MERCADO_PAGO_CARD');
