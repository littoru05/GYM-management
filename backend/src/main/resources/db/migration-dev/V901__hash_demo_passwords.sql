-- Local/dev demo data only.
-- Replaces plaintext demo passwords with BCrypt hashes of '123456'.

UPDATE users SET password = '$2a$10$3S2/QRwsJcJaAen4BTJFSOs8PEy2P1t3Ppe25sCv0lm6DwivEQXcC' WHERE email = 'admin@gym.local';
UPDATE users SET password = '$2a$10$.4pmyVxqQj35/9f2eKk8E.wizUtnbkKbkK4pW.WQCyoWVcqMFRHZW' WHERE email = 'staff@gym.local';
