delimiter $$
create function nextid(id varchar(10)) returns varchar(10) deterministic
begin
    return concat(substr(id, 1, 2) , cast(substr(id, 3) as unsigned) + 1);
end $$

delimiter $$
create procedure insert_star (
    in insertname varchar(100),
    in insertyear int,
    out insertid varchar(10))
begin
    select max(id) into insertid from stars;
    select nextid(insertid) into insertid;
    insert into stars (id, name, birthyear) values (insertid, insertname, insertyear);
end $$

delimiter $$
create procedure add_movie(
    in movietitle varchar(100), in movieyear integer, in moviedirector varchar(100),
    in starname varchar(100), in genrename varchar(32),
    out errcode integer, out movieid varchar(10), out genreid integer, out starid varchar(10))
begin
    # declare genreid integer;
    # declare starid, movieid varchar(10);
    declare mycount integer;
    declare tmpid varchar(10);
    set movieid = 'zm0';
    select count(*) into errcode from movies where title=movietitle and year=movieyear and moviedirector=director;
    if errcode != 0 then
        set errcode = -1;
    else
        set errcode = 0;
        # insert or select the star
        select count(*) into mycount from stars where name=starname;
        if mycount = 0 then
            call insert_star(starname, null, starid);
        else
            select id into starid from stars where name=starname;
        end if;
        # insert or select the genre
        select count(*) into mycount from genres where name like genrename;
        if mycount = 0 then
            insert into genres set name=genrename;
            select max(id) into genreid from genres;
        else
            select id into genreid from genres where name like genrename;
        end if;
        # insert into movie
        select max(id) into tmpid from movies where id regexp 'zm[0-9]+';
        if tmpid is null then
            select nextid(movieid) into movieid;
        else
            select nextid(tmpid) into movieid;
        end if;

        insert into movies (id, title, year, director) value (movieid, movietitle, movieyear, moviedirector);
        # insert into stars_in_movies
        insert into stars_in_movies (starid, movieid) value (starid, movieid);
        # insert into genres_in_movies
        insert into genres_in_movies (genreid, movieid) value (genreid, movieid);
    end if;
end $$

delimiter ;