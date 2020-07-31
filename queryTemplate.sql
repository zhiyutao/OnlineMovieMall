select * from stars where id="nm0089509";

select id,title, movies.year, director,rating from movies join 
	(select movieId, rating from ratings order by rating desc LIMIT 20) as r on movies.id=movieId;

select id, name from stars join 
	(select starId from stars_in_movies where movieId = "tt0355680") as a 
    on id = a.starId limit 3;

select id, name from genres join
     (select genreId from genres_in_movies where movieId = "tt0355680") as a
     on id = a.genreId limit 3;
     
select id, name, birthYear from stars join (select starId from stars_in_movies where movieId = "tt0355680") as a on id = a.starId;

select id, name from genres join (select genreId from genres_in_movies where movieId = "tt0355680") as a on id = a.genreId;

select id, title, year, director from movies join (select movieId from stars_in_movies where starId = "nm0798798") as a on id = a.movieId;

# --- get total number by the way
select * from movies join (select movieId from genres_in_movies where genreId = 2 limit 5 offset 0) as a on a.movieId = id, (select count(*) as totalNum from genres_in_movies where genreId = 1) as c;
# --- starts with *
select * from movies where REGEXP_LIKE(title, '^[^a-z0-9]');
# --- order actor ---
select id, name, a.counter from stars join
    (select c.starId, count(*) as counter from stars_in_movies as c join
        (select starId from stars_in_movies where movieId = 'tt0498362') as b on b.starId=c.starId group by c.starId) as a on id = a.starId order by a.counter desc, name asc limit 10 ;

# --- search by initial ---
select movies.* from movies join
	(select distinct movieId from stars_in_movies join
		(select id, name from stars where name like '%x%')
	as n on n.id = starId)
as s on id = s.movieId and title like '%b%' and director like '%x%' and year = 2004;

# --- search movies
select * from (select * from movies join (select distinct movieId from stars_in_movies join (select id, name from stars where name like '%x%') as n on n.id = starId) as s on id = s.movieId where title like '%b%' and director like '%x%' and year = 2004
) as m left join ratings r on m.id = r.movieId limit 10;

# --- generate price for each movie ----
# alter table movies add column price float not null default 0;
# update movies set price=(0.99 + floor(rand() * 12)); # (p.s. need to close safe mode)

# --- add count column to table sales ---
alter table sales add column count int not null default 1;