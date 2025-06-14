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
    ('Admin', 'Admin', 'Salvadore�o', 'Masculino', 'admin@biblioteca.com', '12345', 1), --ADMIN
    ('Luc�a', 'P�rez', 'Mexicana', 'Femenino', 'lucia.perez@correo.com', 'pass1', 3), --USUARIO
    ('Carlos', 'Ram�rez', 'Guatemalteco', 'Masculino', 'carlos.ramirez@correo.com', 'pass2', 2), --SECRETARIO
    ('Elena', 'Mart�nez', 'Colombiana', 'Femenino', 'elena.martinez@correo.com', 'pass3', 3), --USUARIO
    ('Jorge', 'G�mez', 'Argentino', 'Masculino', 'jorge.gomez@correo.com', 'pass4', 4); --Bibliotecario (Posiblemente se descarte)
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
    ('Cien a�os de soledad', 'Gabriel Garc�a M�rquez', 1967, 1, 3),
    ('Don Quijote de la Mancha', 'Miguel de Cervantes', 1605, 1, 2),
    ('La sombra del viento', 'Carlos Ruiz Zaf�n', 2001, 1, 4),
    ('Rayuela', 'Julio Cort�zar', 1963, 1, 2),
    ('El amor en los tiempos del c�lera', 'Gabriel Garc�a M�rquez', 1985, 1, 3);
GO

-- Tabla Inventario
CREATE TABLE Inventario (
    Id INT IDENTITY(1,1) PRIMARY KEY,
    LibroId INT NOT NULL,
    CodigoCopia VARCHAR(50) UNIQUE NOT NULL,
    FechaAdquisicion DATE NOT NULL DEFAULT GETDATE(),
    Estado VARCHAR(50) CHECK (Estado IN ('Disponible', 'Prestado', 'Da�ado', 'En reparaci�n')) DEFAULT 'Disponible',
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
    (4, 'LIB004-A', '2021-09-20', 'En reparaci�n'),
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

CREATE TABLE Multas (
    IdMulta INT PRIMARY KEY IDENTITY(1,1),
    IdPrestamo INT NOT NULL,
    IdUsuario INT NOT NULL,
    DiasRetraso INT NOT NULL,
    Monto DECIMAL(10, 2) NOT NULL,
    FechaMulta DATE NOT NULL DEFAULT GETDATE(),
    Pagado BIT NOT NULL DEFAULT 0,

    CONSTRAINT FK_Multas_Prestamo FOREIGN KEY (IdPrestamo)
        REFERENCES Prestamos(Id),
    CONSTRAINT FK_Multas_Usuario FOREIGN KEY (IdUsuario)
        REFERENCES Usuarios(Id),
    CONSTRAINT CK_Multas_DiasRetraso CHECK (DiasRetraso >= 1),
    CONSTRAINT CK_Multas_Monto CHECK (Monto >= 0)
);
Go

-- Tabla para procesar pagos de mora
CREATE TABLE PagoMora (
    IdPago INT PRIMARY KEY IDENTITY(1,1),
    IdMulta INT NOT NULL,
    FechaPago DATE NOT NULL DEFAULT GETDATE(),
    MontoPagado DECIMAL(10, 2) NOT NULL,

    CONSTRAINT FK_PagoMora_Multa FOREIGN KEY (IdMulta)
        REFERENCES Multas(IdMulta)
);

-- Insert Prestamos
INSERT INTO Prestamos (LibroId, CodigoCopia, UsuarioId)
VALUES
    (1, 'LIB001-A', 2),
    (2, 'LIB002-A', 3),
    (3, 'LIB003-A', 4),
    (4, 'LIB004-A', 5),
    (5, 'LIB005-A', 2);
	Go

	-- Trigger calcular multas
CREATE TRIGGER TR_CrearMulta_Devolucion
ON Devoluciones
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @IdPrestamo INT, @FechaEntrega DATE, @FechaDevolucion DATE, @IdUsuario INT;
    DECLARE @DiasRetraso INT, @Monto DECIMAL(10,2);

    SELECT 
        @IdPrestamo = i.PrestamoId,
        @FechaEntrega = i.FechaRealDevolucion
    FROM INSERTED i;

    SELECT 
        @FechaDevolucion = p.FechaDevolucion,
        @IdUsuario = p.UsuarioId
    FROM Prestamos p
    WHERE p.Id = @IdPrestamo;

    IF @FechaEntrega > @FechaDevolucion
    BEGIN
        SET @DiasRetraso = DATEDIFF(DAY, @FechaDevolucion, @FechaEntrega);

        -- Escalado de multas
        IF @DiasRetraso = 1
            SET @Monto = 0.50;
        ELSE IF @DiasRetraso BETWEEN 2 AND 3
            SET @Monto = 1.50;
        ELSE IF @DiasRetraso BETWEEN 4 AND 5
            SET @Monto = 3.00;
        ELSE IF @DiasRetraso BETWEEN 6 AND 7
            SET @Monto = 5.00;
        ELSE
            SET @Monto = 5.00 + (@DiasRetraso - 7) * 1.00;

        -- Insertar multa
        INSERT INTO Multas (IdPrestamo, IdUsuario, DiasRetraso, Monto)
        VALUES (@IdPrestamo, @IdUsuario, @DiasRetraso, @Monto);
    END
END;

-- Insert Devoluciones
INSERT INTO Devoluciones (PrestamoId, FechaRealDevolucion)
VALUES 
    (1, '01-05-2025'),
    (2, '01-05-2025'),
    (3, '15-05-2025'),
    (4, '01-05-2025'),
    (5, '20-06-2025');
	Go
	
--SP Insertar Usuario
CREATE PROCEDURE sp_InsertarUsuario
    @Nombre VARCHAR(50),
    @Apellido VARCHAR(50),
    @Nacionalidad VARCHAR(50),
    @Sexo VARCHAR(10),
    @Correo VARCHAR(100),
    @Contrasenia VARCHAR(100),
    @RolId INT
AS
BEGIN
    INSERT INTO Usuarios (Nombre, Apellido, Nacionalidad, Sexo, Correo, Contrasenia, RolId)
    VALUES (@Nombre, @Apellido, @Nacionalidad, @Sexo, @Correo, @Contrasenia, @RolId);
END;
GO

--SP Actualizar Usuario
CREATE PROCEDURE sp_ActualizarUsuario
    @Id INT,
    @Nombre NVARCHAR(100),
    @Apellido NVARCHAR(100),
    @Nacionalidad NVARCHAR(100),
    @Correo NVARCHAR(100)
AS
BEGIN
    UPDATE Usuarios
    SET Nombre = @Nombre,
        Apellido = @Apellido,
        Nacionalidad = @Nacionalidad,
        Correo = @Correo
    WHERE Id = @Id;
END;

GO

--Eliminar Usuario
CREATE PROCEDURE sp_EliminarUsuario
    @Id INT
AS
BEGIN
    DELETE FROM Usuarios
    WHERE Id = @Id;
END;
GO

--Obtener Usuario por ID
CREATE PROCEDURE sp_ObtenerUsuarioPorId
    @Id INT
AS
BEGIN
    SELECT U.Id, U.Nombre, U.Apellido, U.Nacionalidad, U.Sexo, U.Correo, U.Contrasenia, R.NombreRol, U.RolId
    FROM Usuarios U
    INNER JOIN Roles R ON U.RolId = R.Id
    WHERE U.Id = @Id;
END;
GO

-- Obtener pr�stamos con filtro de usuario (nombre, apellido o correo)
Create PROCEDURE sp_ObtenerPrestamosPorUsuario
    @Filtro NVARCHAR(100)
AS
BEGIN
    SELECT 
        p.Id,
        l.Titulo,
        i.CodigoCopia AS CodigoCopia,
        u.Nombre + ' ' + u.Apellido AS Usuario,
        p.FechaHoraPrestamo,
        p.FechaDevolucion,
        d.FechaRealDevolucion
    FROM Prestamos p
    INNER JOIN Inventario i ON p.CodigoCopia = i.CodigoCopia
    INNER JOIN Libros l ON p.LibroId = l.Id
    INNER JOIN Usuarios u ON p.UsuarioId = u.Id
    LEFT JOIN Devoluciones d ON p.Id = d.PrestamoId
    WHERE u.Nombre LIKE '%' + @Filtro + '%'
       OR u.Apellido LIKE '%' + @Filtro + '%'
       OR u.Correo LIKE '%' + @Filtro + '%'
    ORDER BY p.FechaHoraPrestamo DESC
END;
GO

-- Obtener todos los prestamos
CREATE PROCEDURE sp_ObtenerTodosLosPrestamos
AS
BEGIN
    SELECT 
        p.Id,
        l.Titulo,
        i.CodigoCopia AS CodigoCopia,
        u.Nombre + ' ' + u.Apellido AS Usuario,
        p.FechaHoraPrestamo,
        p.FechaDevolucion,
        d.FechaRealDevolucion
    FROM Prestamos p
    INNER JOIN Inventario i ON p.CodigoCopia = i.CodigoCopia
    INNER JOIN Libros l ON p.LibroId = l.Id
    INNER JOIN Usuarios u ON p.UsuarioId = u.Id
    LEFT JOIN Devoluciones d ON p.Id = d.PrestamoId
    ORDER BY p.FechaHoraPrestamo DESC
END

-- Registrar un Prestamo
CREATE PROCEDURE sp_InsertarPrestamo
    @LibroId INT,
    @CodigoCopia VARCHAR(50),
    @UsuarioId INT
AS
BEGIN
    INSERT INTO Prestamos (LibroId, CodigoCopia, UsuarioId)
    VALUES (@LibroId, @CodigoCopia, @UsuarioId);

    UPDATE Inventario
    SET Estado = 'Prestado'
    WHERE CodigoCopia = @CodigoCopia;
END;
GO

--Actualizar Prestamo
CREATE PROCEDURE sp_ActualizarPrestamo
    @Id INT,
    @LibroId INT,
    @CodigoCopia VARCHAR(50),
    @UsuarioId INT
AS
BEGIN
    UPDATE Prestamos
    SET LibroId = @LibroId,
        CodigoCopia = @CodigoCopia,
        UsuarioId = @UsuarioId
    WHERE Id = @Id;
END;
GO

-- Procesar devoluci�n
Create PROCEDURE sp_ProcesarDevolucion
    @PrestamoId INT
AS
BEGIN
    IF EXISTS (SELECT 1 FROM Devoluciones WHERE PrestamoId = @PrestamoId)
    BEGIN
        RAISERROR('Este pr�stamo ya ha sido devuelto.', 16, 1);
        RETURN;
    END

    INSERT INTO Devoluciones (PrestamoId, FechaRealDevolucion)
    VALUES (@PrestamoId, GETDATE());

    DECLARE @CodigoCopia VARCHAR(50)
    SELECT @CodigoCopia = CodigoCopia FROM Prestamos WHERE Id = @PrestamoId;

    UPDATE Inventario SET Estado = 'Disponible'
    WHERE CodigoCopia = @CodigoCopia;
END

GO

--SP Historial de prestamos
CREATE PROCEDURE sp_HistorialPrestamosUsuario
    @Correo NVARCHAR(100)
AS
BEGIN
    SELECT 
        p.Id AS IdPrestamo,
        l.Titulo AS Libro,
        i.CodigoCopia,
        p.FechaHoraPrestamo,
        p.FechaDevolucion,
        d.FechaRealDevolucion
    FROM Prestamos p
    INNER JOIN Usuarios u ON p.UsuarioId = u.Id
    INNER JOIN Libros l ON p.LibroId = l.Id
    INNER JOIN Inventario i ON p.CodigoCopia = i.CodigoCopia
    LEFT JOIN Devoluciones d ON d.PrestamoId = p.Id
    WHERE u.Correo = @Correo
    ORDER BY p.FechaHoraPrestamo DESC
END;
GO

--SP Cargar Info del panel Usuarios
CREATE PROCEDURE sp_ObtenerUsuarioPorCorreo
    @Correo NVARCHAR(100)
AS
BEGIN
    SELECT Id, Nombre, Apellido, Nacionalidad, Sexo, Correo, Contrasenia, RolId
    FROM Usuarios
    WHERE Correo = @Correo;
END;
GO

--SP Verificar Devolucion Prestamo
CREATE PROCEDURE sp_VerificarDevolucion
    @PrestamoId INT
AS
BEGIN
    IF EXISTS (SELECT 1 FROM Devoluciones WHERE PrestamoId = @PrestamoId)
        SELECT 1 AS YaDevuelto;
    ELSE
        SELECT 0 AS YaDevuelto;
END;
GO

--SP Obtener Libros Disponibles (Usuarios)
CREATE PROCEDURE sp_LibrosDisponibles
AS
BEGIN
    SELECT l.id, l.titulo, l.autor, l.anio, l.disponible, l.stock
    FROM Libros l
    WHERE l.disponible = 1 AND l.stock > 0;
END;
GO

--SP Copias disponibles de libros
CREATE PROCEDURE sp_CopiasDisponiblesPorLibro
    @LibroId INT
AS
BEGIN
    SELECT CodigoCopia
    FROM Inventario
    WHERE LibroId = @LibroId AND Estado = 'Disponible';
END;
GO

-- SP ListarInventario
CREATE PROCEDURE sp_ObtenerInventario
AS
BEGIN
    SELECT 
        Id AS id,
        LibroId AS id_libro,
        CodigoCopia AS codigo_copia,
        Estado AS estado
    FROM Inventario
    ORDER BY Id;
END;
GO

-- SP Obtener Inventario mediante ID
CREATE OR ALTER PROCEDURE sp_ObtenerInventarioPorId
    @IdInventario INT
AS
BEGIN
    SELECT 
        i.Id AS LibroId,
        i.CodigoCopia,
        i.Estado,		
        l.titulo AS TituloLibro,
		i.FechaAdquisicion
    FROM Inventario i
    INNER JOIN Libros l ON i.LibroId = l.id
    WHERE i.Id = @IdInventario;
END;
GO

-- SP Insertar Copia de Libro
CREATE PROCEDURE sp_InsertarInventario
    @LibroId INT,
    @CodigoCopia VARCHAR(50),
    @Estado VARCHAR(50),
    @FechaAdquisicion DATE
AS
BEGIN
    INSERT INTO Inventario (LibroId, CodigoCopia, Estado, FechaAdquisicion)
    VALUES (@LibroId, @CodigoCopia, @Estado, @FechaAdquisicion);

    UPDATE Libros SET stock = stock + 1 WHERE id = @LibroId;
END
GO

-- SP Actualizar Copia de Libro
CREATE OR ALTER PROCEDURE sp_ActualizarInventario
    @IdInventario INT,
    @LibroId INT,
    @CodigoCopia VARCHAR(50),
    @Estado VARCHAR(50)
AS
BEGIN
    UPDATE Inventario
    SET LibroId = @LibroId,
        CodigoCopia = @CodigoCopia,
        Estado = @Estado
    WHERE Id = @IdInventario;
END
GO

-- SP Eliminar Copia de Libro
CREATE PROCEDURE sp_EliminarInventario
    @IdInventario INT
AS
BEGIN
    DECLARE @LibroId INT;

    SELECT @LibroId = LibroId
    FROM Inventario
    WHERE Id = @IdInventario;

    DELETE FROM Inventario
    WHERE Id = @IdInventario;

    IF @LibroId IS NOT NULL
    BEGIN
        -- Opcional: disminuir stock al eliminar copia
        UPDATE Libros SET stock = stock - 1 WHERE id = @LibroId;
    END
END;
GO

-- SP Buscar Multas por Correo
CREATE PROCEDURE sp_MultasPorUsuario
    @Correo NVARCHAR(100)
AS
BEGIN
    SELECT 
        l.titulo AS Titulo,
        p.CodigoCopia,
        m.DiasRetraso,
        m.Monto,
        m.FechaMulta,
        CASE WHEN m.Pagado = 1 THEN 'Pagado' ELSE 'Pendiente' END AS Estado
    FROM Multas m
    INNER JOIN Usuarios u ON m.IdUsuario = u.Id
    INNER JOIN Prestamos p ON m.IdPrestamo = p.Id
    INNER JOIN Libros l ON p.LibroId = l.id
    WHERE u.Correo = @Correo
    ORDER BY m.FechaMulta DESC;
END

-- SP Obtener Multas (Secretario)
CREATE PROCEDURE sp_ObtenerMultas
AS
BEGIN
    SELECT 
        M.IdMulta,
        U.Nombre + ' ' + U.Apellido AS NombreUsuario,
        M.DiasRetraso,
        M.Monto,
        M.FechaMulta,
        CASE WHEN M.Pagado = 1 THEN 'Pagado' ELSE 'Pendiente' END AS Estado
    FROM Multas M
    INNER JOIN Usuarios U ON M.IdUsuario = U.Id
    ORDER BY M.Pagado, M.FechaMulta;
END

-- SP Pagar Mora
CREATE PROCEDURE sp_PagarMulta
    @IdMulta INT
AS
BEGIN
    DECLARE @Monto DECIMAL(10,2);

    SELECT @Monto = Monto FROM Multas WHERE IdMulta = @IdMulta;

    INSERT INTO PagoMora (IdMulta, MontoPagado)
    VALUES (@IdMulta, @Monto);

    UPDATE Multas
    SET Pagado = 1
    WHERE IdMulta = @IdMulta;
END

-- Trigger para calcular FechaDevolucion autom�ticamente
CREATE TRIGGER TR_Prestamos_FechaDevolucion
ON Prestamos
AFTER INSERT
AS
BEGIN
    UPDATE Prestamos
    SET FechaDevolucion = DATEADD(week, 2, FechaHoraPrestamo)
    WHERE Id IN (SELECT Id FROM inserted);
END;