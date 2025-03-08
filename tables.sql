create table Artist (
	aid INT auto_increment primary key,
    aname VARCHAR(30) NOT NULL
);
create table Song (
	sid INT auto_increment primary key,
    sname VARCHAR(30) NOT NULL,
    album VARCHAR(30) NOT NULL,
    genre ENUM(
		'pop','hip-hop','rap','country','reggae',
        'r&b','folk','blues','edm','classical',
        'rock','jazz','metal','punk','latin',
        'kpop','other'),
	length INT NOT NULL
);
create table Performs (
	aid INT,
	sid INT,
    primary key(aid, sid),
    foreign key(aid) references Artist(aid) on delete cascade,
    foreign key(sid) references Song(sid) on delete cascade
);
create table Playlist (
	plid INT auto_increment primary key,
    plname VARCHAR(30)    
);
create table Playlist_Song (
	plid INT,
    sid INT,
    primary key (plid, sid),
    song_order int not null,
    foreign key (plid) references Playlist(plid) on delete cascade,
    foreign key (sid) references Song(sid) on delete cascade
);
