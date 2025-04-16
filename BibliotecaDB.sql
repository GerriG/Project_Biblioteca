CREATE DATABASE BibliotecaDB;
GO

USE BibliotecaDB;
GO

CREATE TABLE Usuarios (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL,
    Apellido VARCHAR(50) NOT NULL,
    Nacionalidad VARCHAR(50) NOT NULL,
    Sexo VARCHAR(10) NOT NULL CHECK (Sexo IN ('Masculino', 'Femenino')),
    Correo VARCHAR(100) UNIQUE NOT NULL,
    Contrasenia VARCHAR(100) NOT NULL
);
GO

INSERT INTO Usuarios (Nombre, Apellido, Nacionalidad, Sexo, Correo, Contrasenia) 
VALUES ('Admin', 'Admin', 'Salvadoreño', 'Masculino', 'admin@biblioteca.com', '12345');
GO

CREATE TABLE Libros (
    id INT IDENTITY(1,1) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    anio INT,
    disponible BIT DEFAULT 1,
    stock INT NOT NULL DEFAULT 0
);