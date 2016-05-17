var app = require('express')();
var server = require('http').Server(app);
var io = require('socket.io')(server);
var players = [];

var enemies = [];

function enemy(id, x, y){
	this.id = id;
	this.x = x;
	this.y = y;
}

function initEnemies(){
    enemyNum1 = new enemy(1111, 10, 500);
    enemyNum2 = new enemy(2222, 160, 500);
    enemyNum3 = new enemy(3333, 310, 500);
    enemyNum4 = new enemy(4444, 460, 500);
    enemyNum5 = new enemy(5555, 610, 500);

    enemies.push(enemyNum1);
    enemies.push(enemyNum2);
    enemies.push(enemyNum3);
    enemies.push(enemyNum4);
    enemies.push(enemyNum5);
}
initEnemies();

console.log("enemies[0]: "+ enemies[0].x);

// process.env.OPENSHIFT_NODEJS_PORT, process.env.OPENSHIFT_NODEJS_IP
server.listen(8080, function(){
    console.log("Server is now running...");
})

io.on('connection', function(socket){
    console.log("Player Connected!");

    setInterval(function (){
        socket.emit("enemiesMoved", enemies);
    }, 500);

    socket.emit("socketID", {id: socket.id});
    socket.emit("getPlayers", players);
    socket.broadcast.emit("newPlayer", {id : socket.id});
    socket.on("playerMoved", function(data){
        data.id= socket.id;
        socket.broadcast.emit("playerMoved", data);

        console.log("playerMoved: " +
                   "ID: " + data.id +
                   "X:" + data.x +
                   "Y:" + data.y);

        for(var i=0;i<players.length;i++){
            if(players[i].id == data.id){
                players[i].x = data.x;
                players[i].y = data.y;
            }
        }
    });
	socket.on("shotFired", function(data){
	    console.log("shotFired "+ socket.id)
		socket.broadcast.emit("shotFired", data);
	});
	socket.on("enemyDestroyed", function(data){
        //console.log(data.id)
        console.log("enemyDestroyed " +socket.id)
        for(i in enemies){
            if(enemies[i].id == data.id){
                console.log("Player "+ data.id + " popped")
                enemies.splice(i,1);
				for(var i=0;i<players.length;i++){
					if(socket.id == players[i].id){
						players[i].highscore++;
						console.log(socket.id + " "+ players[i].highscore)
					}
				}
				//io.to(socket.id).emit("enemyDead", { enemyDead : true, socketId : socket.id })
				socket.emit("enemyDead", { enemyDead : true, socketId : socket.id })
            }
        }
	});

    socket.on('disconnect', function(){
        console.log("Player Disconnected!");
        socket.broadcast.emit("playerDisconnected", {id: socket.id});
        for(var i=0; i < players.length; i++){
            if(players[i].id = socket.id){
                players.splice(i,1);
            }
        }
    })
    players.push(new player(socket.id, 0,0,0));
})

function player(id, x ,y, highscore){
    this.id= id;
    this.x = x;
    this.y = y;
	this.highscore = highscore;
}


var movingLeft = true;
enemiesWalk = function (){
for (var i = 0; i < enemies.length; i++) {
		if(enemies[enemies.length-1].x < 1000 && movingLeft){
			enemies[i].x += 5;
		} else {
            movingLeft = false;
		}
		if(enemies[0].x > 10 && !movingLeft){
		    enemies[i].x -= 5;
		} else {
		    movingLeft = true;
		}
	}
	if(enemies.length == 0){
	    initEnemies();
	}

}
	//console.log(io.engine.clientsCount);
	setInterval(enemiesWalk, 500);

