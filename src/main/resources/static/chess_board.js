const PIECES = new Map([
    [' ', ''],
    ['K', '♔'],
    ['Q', '♕'],
    ['R', '♖'],
    ['B', '♗'],
    ['N', '♘'],
    ['P', '♙'],
    ['k', '♚'],
    ['q', '♛'],
    ['r', '♜'],
    ['b', '♝'],
    ['n', '♞'],
    ['p', '♟']
]);

const DEFAULT_BOARD = 'rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR';

function drawInitialBoard(gameId, isBlack) {
    let request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (request.readyState === 4) {
            let chessBoardTable = document.getElementById('chess-board');
            chessBoardTable.firstChild.remove();
            if (request.status === 200) {
                chessBoardTable.appendChild(createBoard(JSON.parse(request.responseText).board, isBlack));
            } else if (request.status === 204) {
                chessBoardTable.appendChild(createBoard(DEFAULT_BOARD, isBlack));
            }
        }
    }
    request.open('get', '/api/game/' + gameId + '/lastMove', true);
    request.send();
}

function drawNewBoard(moveId, isBlack) {
    let request = new XMLHttpRequest();
    request.onreadystatechange = function () {
        if (request.readyState === 4) {
            let chessBoardTable = document.getElementById('chess-board');
            chessBoardTable.firstChild.remove();
            chessBoardTable.appendChild(createBoard(JSON.parse(request.responseText).board, isBlack));
        }
    }
    request.open('get', '/api/move/' + moveId, true);
    request.send();
}

function createBoard(board, isBlack) {
    let boardArray = board.split('');
    boardArray.forEach(function (item, i) {
        boardArray[i] = PIECES.get(item);
    });
    if (isBlack) {
        boardArray = boardArray.reverse();
    }
    let chessBoardTBody = document.createElement('tbody');
    for (let i = 0; i < 8; i++) {
        let row = document.createElement('tr');
        let rankCode = isBlack ? i + 1 : 8 - i;
        row.appendChild(createRankHeader(rankCode));
        for (let j = 0; j < 8; j++) {
            row.appendChild(createSquare(boardArray[i * 8 + j], j, i));
        }
        chessBoardTBody.appendChild(row);
    }
    let files = document.createElement('tr');
    files.appendChild(createFileHeader(''));
    if (isBlack) {
        'hgfedcba'.split('').forEach(e => files.appendChild(createFileHeader(e)));
    } else {
        'abcdefgh'.split('').forEach(e => files.appendChild(createFileHeader(e)));
    }
    chessBoardTBody.appendChild(files);
    return chessBoardTBody;
}

function createFileHeader(fileCode) {
    let file = document.createElement('td');
    file.style.height = '1em';
    file.style.lineHeight = '0';
    file.style.textAlign = 'center';
    file.textContent = fileCode;
    return file;
}

function createRankHeader(rankCode) {
    let rank = document.createElement('td');
    rank.style.lineHeight = '0';
    rank.textContent = rankCode;
    return rank;
}

function createSquare(piece, file, rank) {
    let square = document.createElement('td');
    square.style.width = '1.5em';
    square.style.height = '1.5em';
    square.style.textAlign = 'center';
    square.style.fontSize = '2em';
    square.style.lineHeight = '0';
    if (file % 2 === rank % 2) {
        square.style.background = '#eee';
    } else {
        square.style.background = '#aaa';
    }
    square.textContent = piece;
    return square;
}