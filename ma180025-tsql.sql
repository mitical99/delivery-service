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
