DROP FUNCTION IF EXISTS get_next_id;
DELIMITER $$
CREATE FUNCTION get_next_id (table_name VARCHAR(20))
RETURNS VARCHAR(20)
DETERMINISTIC

BEGIN
	DECLARE max_id VARCHAR(20);
	DECLARE prefix VARCHAR(2);
	DECLARE ret VARCHAR(20);
	
	IF table_name = 'movies' THEN
		SET prefix = 'tt';
		SELECT MAX(id) INTO max_id FROM movies;
	ELSEIF table_name = 'stars' THEN
		SET prefix = 'nm';
		SELECT MAX(id) INTO max_id FROM stars;
	END IF;
	SET max_id = SUBSTRING(max_id, 3);
	SET max_id = max_id+1;
	SET ret = CONCAT(prefix, LPAD(max_id, 7, 0));
	RETURN(ret);
END
$$
DELIMITER ;

DROP PROCEDURE IF EXISTS add_movie;
DELIMITER $$
CREATE PROCEDURE add_movie(
    IN t VARCHAR(100),
    IN y INTEGER,
    IN d VARCHAR(100),
    IN s VARCHAR(100),
    IN g VARCHAR(32),
    OUT ret VARCHAR(1000)
)

BEGIN
    DECLARE movieExistsCheck BOOLEAN;
    DECLARE genreExistsCheck BOOLEAN;
    DECLARE starExistsCheck BOOLEAN;
    DECLARE newStarId VARCHAR(10);
    DECLARE newMovieId VARCHAR(10);
    DECLARE tempGenreId VARCHAR(100);
    DECLARE newGenreId Integer;

    SET movieExistsCheck = EXISTS(SELECT * FROM movies WHERE title = t AND year = y AND director = d);
    SET genreExistsCheck = EXISTS(SELECT * FROM genres WHERE name = g);
    SET starExistsCheck = EXISTS(SELECT * FROM stars WHERE name = s);
    IF (NOT movieExistsCheck) THEN
        SET newMovieId = get_next_id('movies');
        INSERT INTO movies VALUES (newMovieId, t, y, d);
        
        IF (NOT genreExistsCheck) THEN
            SELECT MAX(id) + 1 INTO newGenreId FROM genres;
            INSERT INTO genres VALUES (newGenreId, g);
        ELSE
            SELECT id INTO newGenreId FROM genres WHERE name = g;
        END IF;

        IF (NOT starExistsCheck) THEN
            SET newStarId = get_next_id('stars');
            INSERT INTO stars VALUES (newStarId, s, NULL);
        ELSE
            SELECT id INTO newStarId FROM stars WHERE name = s LIMIT 1;
        END IF;

        INSERT INTO genres_in_movies VALUES (newGenreId, newMovieId);
        INSERT INTO stars_in_movies VALUES (newStarId, newMovieId);
        SET ret = CONCAT_WS("/", newMovieId, newStarId, newGenreId);
    ELSE
        SET ret = "";
    END IF;
END
$$
DELIMITER ;