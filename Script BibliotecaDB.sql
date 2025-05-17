CREATE DATABASE BibliotecaDB;
GO

USE BibliotecaDB;
GO

-- Tabla Roles
CREATE TABLE Roles (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    NombreRol VARCHAR(50) UNIQUE NOT NULL
);
GO

-- Tabla Usuarios
CREATE TABLE Usuarios (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    Nombre VARCHAR(50) NOT NULL,
    Apellido VARCHAR(50) NOT NULL,
    Nacionalidad VARCHAR(50) NOT NULL,
    Sexo VARCHAR(10) NOT NULL CHECK (Sexo IN ('Masculino', 'Femenino')),
    Correo VARCHAR(100) UNIQUE NOT NULL,
    Contrasenia VARCHAR(100) NOT NULL,
    RolId INT,
    FOREIGN KEY (RolId) REFERENCES Roles(Id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
GO

-- Insert Roles
INSERT INTO Roles (NombreRol)
VALUES
    ('Administrador'),
    ('Secretario'),
    ('Usuario'),
    ('Bibliotecario');
GO

-- Insert Usuarios
INSERT INTO Usuarios (Nombre, Apellido, Nacionalidad, Sexo, Correo, Contrasenia, RolId)
VALUES
    ('Admin', 'Admin', 'Salvadoreño', 'Masculino', 'admin@biblioteca.com', '12345', 1),
    ('Lucía', 'Pérez', 'Mexicana', 'Femenino', 'lucia.perez@correo.com', 'pass1', 3),
    ('Carlos', 'Ramírez', 'Guatemalteco', 'Masculino', 'carlos.ramirez@correo.com', 'pass2', 2),
    ('Elena', 'Martínez', 'Colombiana', 'Femenino', 'elena.martinez@correo.com', 'pass3', 3),
    ('Jorge', 'Gómez', 'Argentino', 'Masculino', 'jorge.gomez@correo.com', 'pass4', 4);
GO

-- Tabla Libros
CREATE TABLE Libros (
    id INT IDENTITY(1,1) PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    autor VARCHAR(255) NOT NULL,
    anio INT,
    disponible BIT DEFAULT 1,
    stock INT NOT NULL DEFAULT 0
);
GO

-- Insert Libros
INSERT INTO Libros (titulo, autor, anio, disponible, stock)
VALUES
    ('Cien años de soledad', 'Gabriel García Márquez', 1967, 1, 3),
    ('Don Quijote de la Mancha', 'Miguel de Cervantes', 1605, 1, 2),
    ('La sombra del viento', 'Carlos Ruiz Zafón', 2001, 1, 4),
    ('Rayuela', 'Julio Cortázar', 1963, 1, 2),
    ('El amor en los tiempos del cólera', 'Gabriel García Márquez', 1985, 1, 3);
GO

-- Tabla Inventario
CREATE TABLE Inventario (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    LibroId INT NOT NULL,
    CodigoCopia VARCHAR(50) UNIQUE NOT NULL,
    FechaAdquisicion DATE NOT NULL,
    Estado VARCHAR(50) DEFAULT 'Disponible',
    FOREIGN KEY (LibroId) REFERENCES Libros(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
GO

-- Insert Inventario
INSERT INTO Inventario (LibroId, CodigoCopia, FechaAdquisicion, Estado)
VALUES
    (1, 'LIB001-A', '2024-01-15', 'Disponible'),
    (2, 'LIB002-A', '2023-12-10', 'Prestado'),
    (3, 'LIB003-A', '2022-06-05', 'Disponible'),
    (4, 'LIB004-A', '2021-09-20', 'En reparación'),
    (5, 'LIB005-A', '2020-04-30', 'Disponible');
GO

-- Tabla Prestamos
CREATE TABLE Prestamos (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    LibroId INT NOT NULL,
    CodigoCopia VARCHAR(50) NOT NULL,
    FechaHoraPrestamo DATETIME DEFAULT GETDATE(),
    FechaDevolucion DATETIME,
    UsuarioId INT NOT NULL,
    FOREIGN KEY (LibroId) REFERENCES Libros(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE    
);
GO

--Tabla Devoluciones
CREATE TABLE Devoluciones (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    PrestamoId INT NOT NULL,
    FechaRealDevolucion DATETIME NOT NULL,
    FOREIGN KEY (PrestamoId) REFERENCES Prestamos(Id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
GO

-- Insert Prestamos
INSERT INTO Prestamos (LibroId, CodigoCopia, UsuarioId)
VALUES
    (1, 'LIB001-A', 2),
    (2, 'LIB002-A', 3),
    (3, 'LIB003-A', 4),
    (4, 'LIB004-A', 5),
    (5, 'LIB005-A', 2);
	Go

-- Insert Devoluciones
INSERT INTO Devoluciones (PrestamoId, FechaRealDevolucion)
VALUES 
    (1, '2025-05-01'),
    (2, '2025-05-01'),
    (3, '2025-05-15'),
    (4, '2025-05-01'),
    (5, '2025-05-01');
	Go

-- Trigger para calcular FechaDevolucion automáticamente
CREATE TRIGGER TR_Prestamos_FechaDevolucion
ON Prestamos
AFTER INSERT
AS
BEGIN
    UPDATE Prestamos
    SET FechaDevolucion = DATEADD(week, 2, FechaHoraPrestamo)
    WHERE Id IN (SELECT Id FROM inserted);
END;