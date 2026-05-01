UPDATE products
SET image_url = '/images/products/iphone-15.jpg'
WHERE name = 'iPhone 15';

UPDATE products
SET image_url = '/images/products/galaxy-s24.jpg'
WHERE name = 'Samsung Galaxy S24';

UPDATE products
SET image_url = '/images/products/macbook-air-m3.jpg'
WHERE name = 'MacBook Air M3';

UPDATE products
SET image_url = '/images/products/mx-master-3s.jpg'
WHERE name = 'Logitech MX Master 3S';

UPDATE products
SET image_url = '/images/products/sony-wh1000xm5.jpg'
WHERE name = 'Sony WH-1000XM5';

UPDATE products
SET image_url = '/images/products/dell-xps13.jpg'
WHERE name = 'Dell XPS 13';

DELETE FROM products
WHERE name = 'Test Product Gateway';
