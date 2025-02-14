INSERT INTO Artist (aname) VALUES 
('Taylor Swift'),
('Drake'),
('BTS'),
('Ed Sheeran'),
('Metallica');

INSERT INTO Song (sname, album, genre, length) VALUES 
('Shake It Off', '1989', 'pop', 219),
('Gods Plan', 'Scorpion', 'hip-hop', 198),
('Dynamite', 'BE', 'kpop', 203),
('Shape of You', 'Divide', 'pop', 234),
('Enter Sandman', 'Metallica', 'metal', 331);

INSERT INTO Performs (aid, sid) VALUES 
(1, 1), -- Taylor Swift -> Shake It Off
(2, 2), -- Drake -> God's Plan
(3, 3), -- BTS -> Dynamite
(4, 4), -- Ed Sheeran -> Shape of You
(5, 5); -- Metallica -> Enter Sandman

