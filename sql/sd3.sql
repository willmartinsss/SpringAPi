create schema sd3;
use sd3;

CREATE TABLE users (
  `id` VARCHAR(36) NOT NULL,         -- UUID (não autoincremento)
  `name` VARCHAR(200) NOT NULL,      -- Nome do usuário
  `login` VARCHAR(20) NOT NULL,      -- Login
  `password` VARCHAR(100) NOT NULL,  -- Senha (texto ou hash)
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE
);