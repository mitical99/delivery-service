CREATE DATABASE [KurirskaSluzba]
go
use [KurirskaSluzba]
go



CREATE TABLE [Administrator]
( 
	[IdKor]              integer  NOT NULL 
)
go

CREATE TABLE [Adresa]
( 
	[IdAdr]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Ulica]              varchar(100)  NOT NULL ,
	[Broj]               integer  NOT NULL ,
	[xKord]              integer  NOT NULL ,
	[yKord]              integer  NOT NULL ,
	[IdGrad]             integer  NOT NULL 
)
go

CREATE TABLE [Etapa]
( 
	[IdE]                integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdVoz]              integer  NOT NULL ,
	[IdKor]              integer  NOT NULL ,
	[RedosledIsporuke]   integer  NOT NULL 
	CONSTRAINT [VeceOdNula_936769466]
		CHECK  ( RedosledIsporuke >= 0 ),
	[IdPak]              integer  NOT NULL ,
	[Za_magacin]         integer  NOT NULL 
	CONSTRAINT [IzmedjuNulaIJedan_776075021]
		CHECK  ( Za_magacin BETWEEN 0 AND 1 ),
	[Za_pokupiti]        integer  NOT NULL 
	CONSTRAINT [IzmedjuNulaIJedan_1581976931]
		CHECK  ( Za_pokupiti BETWEEN 0 AND 1 )
)
go

CREATE TABLE [Grad]
( 
	[IdGrad]             integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Naziv]              varchar(100)  NULL ,
	[PostanskiBr]        integer  NULL 
)
go

CREATE TABLE [Korisnik]
( 
	[IdKor]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[Ime]                varchar(100)  NOT NULL ,
	[Prezime]            varchar(100)  NOT NULL ,
	[KorisnickoIme]      varchar(100)  NOT NULL ,
	[Sifra]              varchar(100)  NOT NULL ,
	[IdAdr]              integer  NOT NULL 
)
go

CREATE TABLE [Kupac]
( 
	[IdKor]              integer  NOT NULL 
)
go

CREATE TABLE [Kurir]
( 
	[BrojIsporuka]       integer  NULL 
	CONSTRAINT [PocetnaVrednostNula_1905258675]
		 DEFAULT  0,
	[Profit]             decimal(10,3)  NULL 
	CONSTRAINT [PocetnaVrednostNula_772454611]
		 DEFAULT  0,
	[Status]             integer  NULL 
	CONSTRAINT [PocetnaVrednostNula_756591300]
		 DEFAULT  0
	CONSTRAINT [IzmedjuNulaIJedan_387532634]
		CHECK  ( Status BETWEEN 0 AND 1 ),
	[IdKor]              integer  NOT NULL ,
	[BrVozackaDozvola]   varchar(100)  NOT NULL 
)
go

CREATE TABLE [Lokacija_magacin]
( 
	[IdLok]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdAdr]              integer  NOT NULL 
)
go

CREATE TABLE [Paket]
( 
	[IdPak]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[StatusIsporuke]     integer  NOT NULL 
	CONSTRAINT [PocetnaVrednostNula_1600174844]
		 DEFAULT  0
	CONSTRAINT [IzmedjuNulaICetiri_1097966527]
		CHECK  ( StatusIsporuke BETWEEN 0 AND 4 ),
	[Cena]               decimal(10,3)  NULL ,
	[VremePrihvatanjaPonude] datetime  NULL ,
	[VremeKreiranjaZahtev] datetime  NOT NULL ,
	[TipPaket]           integer  NOT NULL 
	CONSTRAINT [IzmedjuNulaITri_2037447704]
		CHECK  ( TipPaket BETWEEN 0 AND 3 ),
	[Tezina]             decimal(10,3)  NOT NULL ,
	[IdAdrDo]            integer  NOT NULL ,
	[IdAdrOd]            integer  NOT NULL ,
	[IdKor]              integer  NOT NULL ,
	[IdMag]              integer  NULL 
)
go

CREATE TABLE [Parkirano]
( 
	[IdVoz]              integer  NOT NULL ,
	[IdLok]              integer  NOT NULL 
)
go

CREATE TABLE [Ponuda]
( 
	[Cena]               decimal(10,3)  NULL ,
	[IdPak]              integer  NOT NULL 
)
go

CREATE TABLE [Prevozi_se]
( 
	[IdPak]              integer  NOT NULL ,
	[IdVoz]              integer  NOT NULL ,
	[IdKor]              integer  NOT NULL ,
	[Za_magacin]         integer  NOT NULL 
	CONSTRAINT [IzmedjuNulaIJedan_644060209]
		CHECK  ( Za_magacin BETWEEN 0 AND 1 )
)
go

CREATE TABLE [Vozi]
( 
	[IdVoz]              integer  NOT NULL ,
	[IdKor]              integer  NOT NULL ,
	[DuzinaPuta]         decimal(10,3)  NOT NULL 
	CONSTRAINT [PocetnaVrednostNula_543255420]
		 DEFAULT  0,
	[IdAdr]              integer  NOT NULL ,
	[SlobodanProstor]    decimal(10,3)  NOT NULL 
	CONSTRAINT [VeceOdNula_737689592]
		CHECK  ( SlobodanProstor >= 0 ),
	[Zarada]             decimal(10,3)  NOT NULL ,
	[BrojIsporuka]       integer  NOT NULL ,
	[IdLok]              integer  NOT NULL 
)
go

CREATE TABLE [Vozilo]
( 
	[IdVoz]              integer  IDENTITY ( 1,1 )  NOT NULL ,
	[RegBr]              varchar(100)  NOT NULL ,
	[TipGoriva]          integer  NOT NULL 
	CONSTRAINT [IzmedjuNulaIDva_1666358207]
		CHECK  ( TipGoriva BETWEEN 0 AND 2 ),
	[Potrosnja]          decimal(10,3)  NOT NULL 
	CONSTRAINT [VeceOdNula_1836474064]
		CHECK  ( Potrosnja >= 0 ),
	[Nosivost]           decimal(10,3)  NOT NULL 
	CONSTRAINT [VeceOdNula_1536643443]
		CHECK  ( Nosivost >= 0 )
)
go

CREATE TABLE [Vozio]
( 
	[IdVozio]            integer  IDENTITY ( 1,1 )  NOT NULL ,
	[IdKor]              integer  NOT NULL ,
	[IdVoz]              integer  NOT NULL 
)
go

CREATE TABLE [ZahtevKurir]
( 
	[BrVozackaDozvola]   varchar(100)  NOT NULL ,
	[IdKor]              integer  NOT NULL 
)
go

ALTER TABLE [Administrator]
	ADD CONSTRAINT [XPKAdministrator] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [Adresa]
	ADD CONSTRAINT [XPKAdresa] PRIMARY KEY  CLUSTERED ([IdAdr] ASC)
go

ALTER TABLE [Etapa]
	ADD CONSTRAINT [XPKEtapa] PRIMARY KEY  CLUSTERED ([IdE] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XPKGrad] PRIMARY KEY  CLUSTERED ([IdGrad] ASC)
go

ALTER TABLE [Grad]
	ADD CONSTRAINT [XAK1Grad] UNIQUE ([PostanskiBr]  ASC)
go

ALTER TABLE [Korisnik]
	ADD CONSTRAINT [XPKKorisnik] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [Korisnik]
	ADD CONSTRAINT [XAK1Korisnik] UNIQUE ([KorisnickoIme]  ASC)
go

ALTER TABLE [Kupac]
	ADD CONSTRAINT [XPKKupac] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [XPKKurir] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [Kurir]
	ADD CONSTRAINT [XAK1Kurir] UNIQUE ([BrVozackaDozvola]  ASC)
go

ALTER TABLE [Lokacija_magacin]
	ADD CONSTRAINT [XPKLokacija_magacin] PRIMARY KEY  CLUSTERED ([IdLok] ASC)
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [XPKPaket] PRIMARY KEY  CLUSTERED ([IdPak] ASC)
go

ALTER TABLE [Parkirano]
	ADD CONSTRAINT [XPKParkirano] PRIMARY KEY  CLUSTERED ([IdVoz] ASC)
go

ALTER TABLE [Ponuda]
	ADD CONSTRAINT [XPKPonuda] PRIMARY KEY  CLUSTERED ([IdPak] ASC)
go

ALTER TABLE [Prevozi_se]
	ADD CONSTRAINT [XPKPrevozi_se] PRIMARY KEY  CLUSTERED ([IdPak] ASC)
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [XPKVozi] PRIMARY KEY  CLUSTERED ([IdVoz] ASC,[IdKor] ASC)
go

ALTER TABLE [Vozilo]
	ADD CONSTRAINT [XPKVozilo] PRIMARY KEY  CLUSTERED ([IdVoz] ASC)
go

ALTER TABLE [Vozilo]
	ADD CONSTRAINT [XAK1Vozilo] UNIQUE ([RegBr]  ASC)
go

ALTER TABLE [Vozio]
	ADD CONSTRAINT [XPKVozio] PRIMARY KEY  CLUSTERED ([IdVozio] ASC)
go

ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [XPKZahtevKurir] PRIMARY KEY  CLUSTERED ([IdKor] ASC)
go

ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [XAK1ZahtevKurir] UNIQUE ([BrVozackaDozvola]  ASC)
go


ALTER TABLE [Administrator]
	ADD CONSTRAINT [R_6] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Adresa]
	ADD CONSTRAINT [R_1] FOREIGN KEY ([IdGrad]) REFERENCES [Grad]([IdGrad])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Etapa]
	ADD CONSTRAINT [R_31] FOREIGN KEY ([IdVoz],[IdKor]) REFERENCES [Vozi]([IdVoz],[IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Etapa]
	ADD CONSTRAINT [R_32] FOREIGN KEY ([IdPak]) REFERENCES [Paket]([IdPak])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Korisnik]
	ADD CONSTRAINT [R_3] FOREIGN KEY ([IdAdr]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Kupac]
	ADD CONSTRAINT [R_4] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Kurir]
	ADD CONSTRAINT [R_5] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE CASCADE
		ON UPDATE CASCADE
go


ALTER TABLE [Lokacija_magacin]
	ADD CONSTRAINT [R_2] FOREIGN KEY ([IdAdr]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Paket]
	ADD CONSTRAINT [R_19] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [R_25] FOREIGN KEY ([IdAdrOd]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [R_26] FOREIGN KEY ([IdAdrDo]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Paket]
	ADD CONSTRAINT [R_27] FOREIGN KEY ([IdMag]) REFERENCES [Lokacija_magacin]([IdLok])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Parkirano]
	ADD CONSTRAINT [R_22] FOREIGN KEY ([IdVoz]) REFERENCES [Vozilo]([IdVoz])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Parkirano]
	ADD CONSTRAINT [R_23] FOREIGN KEY ([IdLok]) REFERENCES [Lokacija_magacin]([IdLok])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Ponuda]
	ADD CONSTRAINT [R_24] FOREIGN KEY ([IdPak]) REFERENCES [Paket]([IdPak])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Prevozi_se]
	ADD CONSTRAINT [R_29] FOREIGN KEY ([IdPak]) REFERENCES [Paket]([IdPak])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Prevozi_se]
	ADD CONSTRAINT [R_30] FOREIGN KEY ([IdVoz],[IdKor]) REFERENCES [Vozi]([IdVoz],[IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_8] FOREIGN KEY ([IdVoz]) REFERENCES [Vozilo]([IdVoz])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_7] FOREIGN KEY ([IdKor]) REFERENCES [Kurir]([IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_28] FOREIGN KEY ([IdAdr]) REFERENCES [Adresa]([IdAdr])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Vozi]
	ADD CONSTRAINT [R_33] FOREIGN KEY ([IdLok]) REFERENCES [Lokacija_magacin]([IdLok])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [Vozio]
	ADD CONSTRAINT [R_34] FOREIGN KEY ([IdKor]) REFERENCES [Kurir]([IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go

ALTER TABLE [Vozio]
	ADD CONSTRAINT [R_35] FOREIGN KEY ([IdVoz]) REFERENCES [Vozilo]([IdVoz])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


ALTER TABLE [ZahtevKurir]
	ADD CONSTRAINT [R_18] FOREIGN KEY ([IdKor]) REFERENCES [Korisnik]([IdKor])
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
go


CREATE FUNCTION getNumberOfSentPackagesForUser
(
	@KorIme varchar(100)
)
RETURNS int
AS
BEGIN
	
	return (
	select count(*)
	from Paket
	where StatusIsporuke in(2, 3) and IdKor=(
		select IdKor
		from Korisnik
		where KorisnickoIme=@KorIme
		)
	)
END
GO

CREATE PROCEDURE eraseAll
AS
BEGIN
	delete from Ponuda
	delete from Etapa
	delete from Prevozi_se
	delete from Vozi
	delete from Vozio
	delete from Parkirano
	delete from ZahtevKurir
	delete from Paket
	delete from Lokacija_magacin
	delete from Korisnik
	delete from Vozilo
	delete from Adresa
	delete from Grad
END
GO

CREATE TRIGGER checkForStockroomsInCity
   ON  Lokacija_magacin
   AFTER INSERT
AS 
BEGIN
	declare @IdAdr int
	declare @myCursor cursor
	set @myCursor = cursor for
	select IdAdr
	from inserted

	open @myCursor
	fetch from @myCursor
	into @IdAdr

	while @@FETCH_STATUS=0 begin
		
		declare @IdG int

		set @IdG = (
		select IdGrad
		from Adresa
		where IdAdr=@IdAdr
		)

		if(exists(
		select IdLok 
		from Lokacija_magacin L join Adresa A on L.IdAdr=A.IdAdr
		where A.IdGrad=@IdG and L.IdAdr!=@IdAdr
		)) begin
			rollback transaction
			break
		end
		
		fetch from @myCursor
		into @IdAdr
	end
	close @myCursor
	deallocate @myCursor

END
GO

CREATE TRIGGER checkIfUserIsAlreadyCourier
   ON  ZahtevKurir 
   AFTER INSERT
AS 
BEGIN
	declare @myCursor cursor
	declare @idKor int

	set @myCursor = cursor for
	select IdKor
	from inserted

	open @myCursor
	fetch from @myCursor
	into @idKor

	while @@FETCH_STATUS=0 begin
		
		if(exists(
			select *
			from Kurir
			where IdKor=@idKor
		)) begin
			rollback transaction
			break
		end
		
		fetch from @myCursor
		into @idKor
	end
	close @myCursor
	deallocate @myCursor

END
GO

CREATE TRIGGER deleteRequestForCourier
   ON  Kurir
   AFTER INSERT
AS 
BEGIN
	
	declare @myCursor cursor
	declare @IdKor int

	set @myCursor = cursor for
	select IdKor
	from inserted

	open @myCursor
	fetch from @myCursor
	into @IdKor

	while @@FETCH_STATUS=0 begin
		
		delete from ZahtevKurir
		where @IdKor=IdKor

		fetch from @myCursor
		into @IdKor
	end
	close @myCursor
	deallocate @myCursor

END
GO

CREATE FUNCTION getAverageCourierProfit
(
	@numberOfDeliveries int
)
RETURNS decimal(10,3)
AS
BEGIN
	
	return (case 
		when @numberOfDeliveries=-1 then (
		select cast(avg(Profit) as decimal(10,3))
		from Kurir
		)
		else (
		select cast(avg(Profit) as decimal(10,3))
		from Kurir
		where BrojIsporuka=@numberOfDeliveries
		)
	end)

END
GO

CREATE FUNCTION calculateDistance
(
	@IdAdrFrom int,
	@IdAdrTo int
)
RETURNS decimal(10, 3)
AS
BEGIN
	
	return (
	select cast(SQRT(POWER(A1.xKord-A2.xKord, 2) + POWER(A1.yKord-A2.yKord, 2)) as decimal(10, 3))
	from Adresa A1, Adresa A2
	where A1.IdAdr=@IdAdrFrom and A2.IdAdr=@IdAdrTo
	)

END
GO


CREATE FUNCTION calculateOfferPrice
(
	@Type int,
	@IdAdrFrom int,
	@IdAdrTo int,
	@Weight decimal(10, 3)
)
RETURNS decimal(10, 3)
AS
BEGIN
	
	declare @PricePerKG int, @StartPrice int
	declare @Distance decimal(10, 3)

	set @PricePerKG = (
	case 
	when @Type=0 then (select 0)
	when @Type=1 then (select 100)
	when @Type=2 then (select 100)
	when @Type=3 then (select 500)
	end
	)

	set @StartPrice = (
	case 
	when @Type=0 then (select 115)
	when @Type=1 then (select 175)
	when @Type=2 then (select 250)
	when @Type=3 then (select 350)
	end
	)

	set @Distance = dbo.calculateDistance(@IdAdrFrom, @IdAdrTo)

	return (@StartPrice + @Weight * @PricePerKG) * @Distance

END
GO

CREATE TRIGGER createOffer
   ON  Paket 
   AFTER INSERT, UPDATE
AS 
BEGIN
	
	declare @myCursor cursor
	declare @IdPak int, @IdAdrFrom int, @IdAdrTo int, @Type int
	declare @Price decimal(10,3), @Weight decimal(10,3)

	set @myCursor = cursor for
	select IdPak, Tezina, TipPaket, IdAdrOd, IdAdrDo
	from inserted
	where StatusIsporuke<=1

	open @myCursor
	fetch from @myCursor
	into @IdPak, @Weight, @Type, @IdAdrFrom, @IdAdrTo

	while @@FETCH_STATUS=0 begin

		set @Price = dbo.calculateOfferPrice(@Type, @IdAdrFrom, @IdAdrTo, @Weight)

		if(exists(select * from deleted where IdPak=@IdPak)) begin
			update Ponuda
			set Cena=@Price
			where IdPak=@IdPak
		end else begin
			insert into Ponuda(IdPak, Cena)
			values (@IdPak, @Price)
		end

		fetch from @myCursor
		into @IdPak, @Weight, @Type, @IdAdrFrom, @IdAdrTo
	end
	close @myCursor
	deallocate @myCursor

END
GO

CREATE TRIGGER startDelivery
   ON  Vozi
   AFTER INSERT
AS 
BEGIN
	declare @myCursor cursor
	declare @IdKor int
	declare @IdVoz int

	set @myCursor = cursor for
	select IdVoz, IdKor
	from inserted

	open @myCursor
	fetch from @myCursor
	into @IdVoz, @IdKor

	while @@FETCH_STATUS=0 begin
		
		if(exists(select * from Vozi where ((@IdVoz=IdVoz or @IdKor=IdKor) and (@IdVoz!=IdVoz and @IdKor!=IdKor))) 
			or @IdKor not in(select IdKor from Kurir where Status=0)) begin
			rollback transaction
			break
		end
		else begin
			update Kurir
			set Status=1
			where IdKor=@IdKor

			delete from Parkirano
			where IdVoz=@IdVoz
		end
		fetch from @myCursor
		into @IdVoz, @IdKor
	end
	close @myCursor
	deallocate @myCursor

END
GO

CREATE TRIGGER updatePackageStatus
   ON  Prevozi_se
   AFTER INSERT
AS 
BEGIN
	
	declare @packageCursor cursor

	declare @idPak int

	set @packageCursor = cursor for
	select IdPak
	from inserted

	open @packageCursor
	fetch from @packageCursor
	into @idPak

	while @@FETCH_STATUS=0 begin

	update Paket
	set StatusIsporuke=2, IdMag=null
	where IdPak=@idPak

	fetch from @packageCursor
	into @idPak
	end
	close @packageCursor
	deallocate @packageCursor

END
GO


CREATE TRIGGER updateDeliveryStatusOnDelete
   ON  Prevozi_se
   AFTER DELETE
AS 
BEGIN
	
	declare @packageCursor cursor

	declare @idPak int, @IdKor int

	set @packageCursor = cursor for
	select IdPak, IdKor
	from deleted
	where Za_magacin=0

	open @packageCursor
	fetch from @packageCursor
	into @idPak, @IdKor

	while @@FETCH_STATUS=0 begin

	update Paket
	set StatusIsporuke=3
	where IdPak=@idPak

	update Vozi
	set BrojIsporuka=BrojIsporuka+1, Zarada=Zarada+(select Cena from Paket where IdPak=@idPak)
	where IdKor=@IdKor

	fetch from @packageCursor
	into @idPak, @IdKor
	end
	close @packageCursor
	deallocate @packageCursor
END
GO

CREATE TRIGGER endDelivery
   ON Vozi
   AFTER DELETE
AS 
BEGIN
	
	declare @drivenCursor cursor
	declare @IdKor int, @IdVoz int, @IdLok int

	set @drivenCursor = cursor for
	select IdKor, IdVoz, IdLok
	from deleted

	open @drivenCursor
	fetch from @drivenCursor
	into @IdKor, @IdVoz, @IdLok 

	while @@FETCH_STATUS=0 begin
		
		insert into Vozio(IdKor, IdVoz)
		values (@IdKor, @IdVoz)

		insert into Parkirano(IdLok, IdVoz)
		values(@IdLok, @IdVoz)

		fetch from @drivenCursor
		into @IdKor, @IdVoz, @IdLok
	end
	close @drivenCursor
	deallocate @drivenCursor

END
GO
