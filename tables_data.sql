-- Insert more sample data for Artist, Song, and Performs with valid genres
INSERT INTO Artist (aname) VALUES 
('Sabrina Carpenter'),
('Taylor Swift'),
('Drake'),
('BTS'),
('Ed Sheeran'),
('Metallica'),
('Billie Eilish'),
('Ariana Grande'),
('Justin Bieber'),
('The Weeknd'),
('Imagine Dragons'),
('Post Malone'),
('Katy Perry'),
('Dua Lipa'),
('Lil Nas X'),
('Lizzo'),
('Olivia Rodrigo'),
('Harry Styles'),
('Shawn Mendes'),
('Adele'),
('Coldplay'),
('Maroon 5'),
('Travis Scott'),
('Miley Cyrus'),
('The Chainsmokers'),
('Doja Cat'),
('Lil Wayne'),
('Halsey'),
('Sam Smith'),
('Bebe Rexha'),
('Blackpink'),
('Shakira'),
('Pitbull'),
('Selena Gomez'),
('Cardi B'),
('Nicki Minaj'),
('Rihanna'),
('Florence + The Machine'),
('The Killers'),
('Green Day'),
('Panic! At The Disco'),
('Twenty One Pilots'),
('My Chemical Romance'),
('Fall Out Boy'),
('Linkin Park'),
('Paramore'),
('Foo Fighters'),
('The Strokes'),
('Arctic Monkeys'),
('The Offspring'),
('Blink-182'),
('The Beatles'),
('Queen'),
('Bon Jovi'),
('(G)I-DLE');

INSERT INTO Song (sname, album, genre, length) VALUES 
('Espresso', 'Short n\' Sweet', 'pop', 175),
('Shake It Off', '1989', 'pop', 219),
('God\'s Plan', 'Scorpion', 'hip-hop', 198),
('Dynamite', 'BE', 'kpop', 203),
('Shape of You', 'Divide', 'pop', 234),
('Enter Sandman', 'Metallica', 'metal', 331),
('Bad Guy', 'When We All Fall Asleep', 'pop', 194),
('Thank U, Next', 'Thank U, Next', 'pop', 232),
('Peaches', 'Justice', 'r&b', 197),
('Blinding Lights', 'After Hours', 'pop', 201),
('Radioactive', 'Night Visions', 'rock', 186),
('Circles', 'Hollywood\'s Bleeding', 'pop', 220),
('Teenage Dream', 'Teenage Dream', 'pop', 239),
('Levitating', 'Future Nostalgia', 'pop', 203),
('Montero', 'Montero', 'hip-hop', 222),
('Truth Hurts', 'Cuz I Love You', 'pop', 234),
('Good 4 U', 'SOUR', 'punk', 188),
('As It Was', 'Harry’s House', 'pop', 174),
('Stitches', 'Handwritten', 'pop', 203),
('Someone Like You', '21', 'pop', 285),
('Viva La Vida', 'Viva La Vida', 'rock', 242),
('Sugar', 'V', 'pop', 235),
('SICKO MODE', 'Astroworld', 'hip-hop', 318),
('Wrecking Ball', 'Bangerz', 'pop', 259),
('Closer', 'Collage', 'edm', 244),
('Say So', 'Hot Pink', 'pop', 239),
('Lollipop', 'Tha Carter III', 'rap', 198),
('Without Me', 'Hollywood\'s Bleeding', 'pop', 225),
('Happier Than Ever', 'Happier Than Ever', 'pop', 237),
('Roses', 'After Hours', 'r&b', 193),
('Hot Girl Bummer', 'Hot Girl Bummer', 'pop', 174),
('I Like It', 'Invasion of Privacy', 'latin', 261),
('Taki Taki', 'Taki Taki', 'reggae', 203),
('Hips Don\'t Lie', 'Oral Fixation, Vol. 2', 'latin', 238),
('Vicious', 'emails i can\'t send', 'pop', 150),
('Smells Like Teen Spirit', 'Nevermind', 'punk', 301),
('American Idiot', 'American Idiot', 'punk', 258),
('Bohemian Rhapsody', 'A Night at the Opera', 'rock', 354),
('We Will Rock You', 'News of the World', 'rock', 155),
('Under Pressure', 'Hot Space', 'rock', 242),
('Livin\' On A Prayer', 'Slippery When Wet', 'rock', 250),
('You Give Love A Bad Name', 'Slippery When Wet', 'rock', 222),
('Oh My God', 'I Trust', 'kpop', 196);

-- Insert data for Performs (Artist-Song relationships)

INSERT INTO Performs (aid, sid) VALUES 
(1, 1), -- Sabrina Carpenter -> Espresso
(2, 2), -- Taylor Swift -> Shake It Off
(3, 3), -- Drake -> God's Plan
(4, 4), -- BTS -> Dynamite
(5, 5), -- Ed Sheeran -> Shape of You
(6, 6), -- Metallica -> Enter Sandman
(7, 7), -- Billie Eilish -> Bad Guy
(8, 8), -- Ariana Grande -> Thank U, Next
(9, 9), -- Justin Bieber -> Peaches
(10, 10), -- The Weeknd -> Blinding Lights
(11, 11), -- Imagine Dragons -> Radioactive
(12, 12), -- Post Malone -> Circles
(13, 13), -- Katy Perry -> Teenage Dream
(14, 14), -- Dua Lipa -> Levitating
(15, 15), -- Lil Nas X -> Montero
(16, 16), -- Lizzo -> Truth Hurts
(17, 17), -- Olivia Rodrigo -> Good 4 U
(18, 18), -- Harry Styles -> As It Was
(19, 19), -- Shawn Mendes -> Stitches
(20, 20), -- Adele -> Someone Like You
(21, 21), -- Coldplay -> Viva La Vida
(22, 22), -- Maroon 5 -> Sugar
(23, 23), -- Travis Scott -> SICKO MODE
(24, 24), -- Miley Cyrus -> Wrecking Ball
(25, 25), -- The Chainsmokers -> Closer
(26, 26), -- Doja Cat -> Say So
(27, 27), -- Lil Wayne -> Lollipop
(28, 28), -- Halsey -> Without Me
(7, 29),  -- Billie Eilish -> Happier Than Ever
(30, 30), -- Bebe Rexha -> Roses
(42, 31), -- Twenty One Pilots -> Hot Girl Bummer
(35, 32), -- Cardi B -> I Like It
(34, 33), -- Selena Gomez -> Taki Taki
(32, 34), -- Shakira -> Hips Don’t Lie
(1, 35),  -- Sabrina Carpenter -> Vicious
(36, 36), -- Florence + The Machine -> Smells Like Teen Spirit
(40, 37), -- Green Day -> American Idiot
(53, 38), -- Queen -> Bohemian Rhapsody
(53, 39), -- Queen-> We Will Rock You
(54, 41), -- Bon Jovi -> Livin' On A Prayer
(54, 42), -- Bon Jovi -> You Give Love A Bad Name
(55, 43); -- (G)I-DLE -> Oh My God
