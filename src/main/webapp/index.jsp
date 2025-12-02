<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tài Xỉu</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/style.css">
</head>
<body>
<div class="page">
    <div class="game-wrapper">
        <h1>TÀI XỈU</h1>
        <p class="sub-note">Thắng làm vua thua thì thôi.</p>

        <div class="top-info">
            <div>Thời gian còn lại: <span id="countdown">30</span> giây</div>
            <div>Số dư: <span id="balance">2000000</span> ₫</div>
        </div>

        <div class="game-area">
            <button id="btn-tai" class="side-big" onclick="setChoice('tai')">TÀI</button>

            <div class="dice-area">
                <div class="dice-row">
                    <div class="dice" id="dice1">?</div>
                    <div class="dice" id="dice2">?</div>
                    <div class="dice" id="dice3">?</div>
                </div>
                <div id="diceSum" class="dice-sum">Chưa mở phiên</div>
                <div id="winLose" class="winlose"></div>
            </div>

            <button id="btn-xiu" class="side-big" onclick="setChoice('xiu')">XỈU</button>
        </div>

        <!-- KHU VỰC ĐẶT CƯỢC -->
        <div class="bet-panel">
            <span>Chọn tiền cược (cộng dồn, trừ All in & Chỉ xem):</span>
            <div class="chips-row">
                <button class="chip" data-value="10000" onclick="selectChip(this)">10K</button>
                <button class="chip" data-value="50000" onclick="selectChip(this)">50K</button>
                <button class="chip" data-value="100000" onclick="selectChip(this)">100K</button>
                <button class="chip" data-value="200000" onclick="selectChip(this)">200K</button>
                <button class="chip" data-value="500000" onclick="selectChip(this)">500K</button>
                <button class="chip" data-value="ALLIN" onclick="selectChip(this)">ALL IN</button>
                <button class="chip" data-value="0" onclick="selectChip(this)">Chỉ xem</button>
            </div>

            <div class="current-bet-row">
                Tiền cược phiên này: <span id="currentBet">0</span> ₫
            </div>

            <button class="play-btn" onclick="playNow()">ĐẶT PHIÊN NÀY</button>
        </div>

        <div id="statusResult" class="status-line">
            Chờ bạn đặt cược / chờ hệ thống mở phiên...
        </div>

        <div class="history-panel">
            <div class="history-title">Lịch sử phiên (đen = Tài, trắng = Xỉu):</div>
            <div id="historyBar" class="history-bar"></div>
        </div>
    </div>
</div>

<script>
    const contextPath = '${pageContext.request.contextPath}';
</script>
<script src="${pageContext.request.contextPath}/assets/script.js"></script>
</body>
</html>
