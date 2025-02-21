# Square Games API

A Spring application exposing a REST API for board games, starting with Tic-tac-toe.

## Features

- Create and manage games
- Play moves
- View game history
- Get available moves
- List all games

## API Endpoints

### Games

- `GET /api/games` - List all games
- `POST /api/games/{gameId}` - Create a new game
- `GET /api/games/{gameId}` - Get game details
- `POST /api/games/{gameId}/moves` - Make a move
- `GET /api/games/{gameId}/available-moves` - Get available moves
- `GET /api/games/{gameId}/history` - Get game history

## Setup

1. Clone the repository
```bash
git clone [your-repository-url]
```

2. Build the project
```bash
mvn clean install
```

3. Run the application
```bash
mvn spring-boot:run
```

## Usage

To create a new game:
```http
POST http://localhost:8080/api/games/tictactoe
{
    "numberOfPlayers": 2,
    "boardSize": 3
}
```

To make a move:
```http
POST http://localhost:8080/api/games/{gameId}/moves
Header: X-UserId: {playerId}
{
    "x": 0,
    "y": 0
}
```
