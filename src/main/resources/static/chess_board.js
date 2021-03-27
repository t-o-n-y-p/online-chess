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
            let container = document.getElementById('container');
            if (request.status === 200) {
                let response = JSON.parse(request.responseText);
                container.prepend(createChessBoardTableWithContainer(response.board, isBlack));
                container.prepend(createNavigation(response.notation, response.previousMove, response.nextMove, isBlack));
            } else if (request.status === 204) {
                container.prepend(createChessBoardTableWithContainer(DEFAULT_BOARD, isBlack));
            } else {
                pushErrorAlert();
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
            if (request.status === 200) {
                let response = JSON.parse(request.responseText);
                let chessBoardTable = document.getElementById('chess-board');
                chessBoardTable.firstChild.remove();
                chessBoardTable.appendChild(createBoard(response.board, isBlack));
                document.getElementById('move-notation').textContent = response.notation;
                updatePreviousMove(document.getElementById('previous-move'), response.previousMove);
                updateNextMove(document.getElementById('next-move'), response.nextMove);
            } else {
                updatePreviousMove(document.getElementById('previous-move'), null);
                updateNextMove(document.getElementById('next-move'), null);
                pushErrorAlert();
            }
        }
    }
    request.open('get', '/api/move/' + moveId, true);
    request.send();
}

function createBoard(board, isBlack) {
    let boardArray = board.split('');
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

function createSquare(pieceCode, file, rank) {
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
    square.textContent = PIECES.get(pieceCode);
    return square;
}

function createNavigation(notation, previousMove, nextMove, isBlack) {
    let panel = document.createElement('div');
    panel.className = 'row';
    panel.style.margin = '8px auto auto';
    let buttons = document.createElement('div');
    buttons.className = 'btn-group';
    buttons.role = 'group';
    buttons.style.width = '100%';
    let previousMoveButton = document.createElement('button');
    previousMoveButton.id = 'previous-move'
    previousMoveButton.type = 'button';
    previousMoveButton.className = 'btn btn-dark';
    previousMoveButton.textContent = '<<';
    previousMoveButton.isBlack = isBlack;
    updatePreviousMove(previousMoveButton, previousMove);
    previousMoveButton.addEventListener('click', function (e) {
        drawNewBoard(e.currentTarget.previousMoveId, e.currentTarget.isBlack);
    });
    let middleFakeButton = document.createElement('button');
    middleFakeButton.id = 'move-notation';
    middleFakeButton.type = 'button';
    middleFakeButton.className = 'btn btn-outline-dark';
    middleFakeButton.style.width = '70%';
    middleFakeButton.disabled = true;
    middleFakeButton.textContent = notation;

    let nextMoveButton = document.createElement('button');
    nextMoveButton.id = 'next-move'
    nextMoveButton.type = 'button';
    nextMoveButton.className = 'btn btn-dark';
    nextMoveButton.textContent = '>>';
    nextMoveButton.isBlack = isBlack;
    updateNextMove(nextMoveButton, nextMove);
    nextMoveButton.addEventListener('click', function (e) {
        drawNewBoard(e.currentTarget.nextMoveId, e.currentTarget.isBlack);
    });

    buttons.appendChild(previousMoveButton);
    buttons.appendChild(middleFakeButton);
    buttons.appendChild(nextMoveButton);
    panel.appendChild(buttons);
    return panel;
}

function updatePreviousMove(button, move) {
    if (move) {
        button.disabled = false;
        button.previousMoveId = move.id;
    } else {
        button.disabled = true;
    }
}

function updateNextMove(button, move) {
    if (move) {
        button.disabled = false;
        button.nextMoveId = move.id;
    } else {
        button.disabled = true;
    }
}

function pushErrorAlert() {
    let alertGroup = document.getElementById('alerts');
    let boardErrorAlertContainer = document.createElement('div');
    boardErrorAlertContainer.style.width = '100%';
    boardErrorAlertContainer.style.margin = 'auto';
    let boardErrorAlert = document.createElement('div');
    boardErrorAlert.className = 'alert alert-danger';
    boardErrorAlert.role = 'alert';
    boardErrorAlert.style.width = '100%';
    boardErrorAlert.style.marginBottom = '0';
    boardErrorAlert.style.textAlign = 'center';
    boardErrorAlert.textContent = 'An unexpected error with the board occurred. Please reload the page.'
    boardErrorAlertContainer.appendChild(boardErrorAlert);
    alertGroup.appendChild(boardErrorAlertContainer);
}

function createChessBoardTableWithContainer(board, isBlack) {
    let chessBoardTableContainer = document.createElement('div');
    chessBoardTableContainer.className = 'row';
    let chessBoardTable = document.createElement('table');
    chessBoardTable.id = 'chess-board';
    chessBoardTable.style.borderSpacing = '0';
    chessBoardTable.style.borderCollapse = 'collapse';
    chessBoardTable.style.margin = '8px auto auto';
    chessBoardTable.style.fontFamily = 'sans-serif';
    chessBoardTable.appendChild(createBoard(board, isBlack));
    chessBoardTableContainer.appendChild(chessBoardTable);
    return chessBoardTableContainer;
}
