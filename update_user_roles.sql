-- Update user roles for testing advanced features
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@library.com';
UPDATE users SET role = 'LIBRARIAN' WHERE email = 'librarian@library.com';
UPDATE users SET role = 'MEMBER' WHERE email = 'member@library.com';
