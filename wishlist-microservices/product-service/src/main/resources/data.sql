-- Catálogo de productos simulado, incluye productos con y sin stock
-- para poder probar la notificación de "sin stock" en la lista de deseos
INSERT IGNORE INTO products (sku, name, description, price, stock) VALUES
('SKU-0001', 'Cuaderno cuadriculado 100 hojas', 'Cuaderno tipo cuadriculado de 100 hojas, tamaño carta', 8500.00, 120),
('SKU-0002', 'Resma de papel carta', 'Resma de papel bond tamaño carta, 500 hojas', 19900.00, 0),
('SKU-0003', 'Caja de lápices de colores x12', 'Caja de 12 lápices de colores para uso escolar', 15900.00, 45),
('SKU-0004', 'Agenda ejecutiva 2026', 'Agenda ejecutiva tapa dura, día por página', 42000.00, 8),
('SKU-0005', 'Marcador permanente negro', 'Marcador de tinta permanente color negro punta fina', 3200.00, 200),
('SKU-0006', 'Carpeta AZ oficio', 'Carpeta tipo AZ tamaño oficio, lomo ancho', 12500.00, 0),
('SKU-0007', 'Mouse inalámbrico', 'Mouse óptico inalámbrico 2.4GHz', 35000.00, 60),
('SKU-0008', 'Teclado mecánico compacto', 'Teclado mecánico 75%, switches rojos', 129900.00, 15);
