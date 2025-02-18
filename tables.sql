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
    foreign key(aid) references Artist(aid),
    foreign key(sid) references Song(sid)
);

