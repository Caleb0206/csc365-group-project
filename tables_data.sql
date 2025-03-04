INSERT INTO Artist (aname) VALUES 
('Sabrina Carpenter'),
('Taylor Swift'),
('Drake'),
('BTS'),
('Ed Sheeran'),
('Metallica');

INSERT INTO Song (sname, album, genre, length) VALUES 
('Espresso', 'Short n\' Sweet', 'pop', 175),
('Shake It Off', '1989', 'pop', 219),
('Gods Plan', 'Scorpion', 'hip-hop', 198),
('Dynamite', 'BE', 'kpop', 203),
('Shape of You', 'Divide', 'pop', 234),
('Enter Sandman', 'Metallica', 'metal', 331);

INSERT INTO Performs (aid, sid) VALUES 
(1, 1), -- Sabrina Carpenter -> Espresso
(2, 2), -- Taylor Swift -> Shake It Off
(3, 3), -- Drake -> God's Plan
(4, 4), -- BTS -> Dynamite
(5, 5), -- Ed Sheeran -> Shape of You
(6, 6); -- Metallica -> Enter Sandman

select * from Song;
select * from Artist;
select * from Performs;

