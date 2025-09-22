# create schema sd3;
use
sd3;

CREATE TABLE users
(
    `id`       VARCHAR(36)  NOT NULL,
    `name`     VARCHAR(200) NOT NULL,
    `login`    VARCHAR(20)  NOT NULL,
    `password` VARCHAR(100) NOT NULL,
    `role`     VARCHAR(20)  NOT NULL, -- Adicione esta linha
    PRIMARY KEY (`id`),
    UNIQUE INDEX `id_UNIQUE` (`id` ASC) VISIBLE
);