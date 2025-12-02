// assets/script.js

let currentChoice = "tai";      // mặc định chọn Tài
let countdown = 30;
let timerId = null;

// tổng tiền cược phiên hiện tại (cộng dồn chip)
let currentBetAmount = 0;

// chọn Tài / Xỉu
function setChoice(choice) {
    currentChoice = choice;
    const btnTai = document.getElementById("btn-tai");
    const btnXiu = document.getElementById("btn-xiu");

    btnTai.classList.remove("active");
    btnXiu.classList.remove("active");

    if (choice === "tai") {
        btnTai.classList.add("active");
    } else {
        btnXiu.classList.add("active");
    }
}

// cập nhật hiển thị tiền cược phiên này
function updateBetDisplay() {
    document.getElementById("currentBet").innerText = currentBetAmount;
}

// bấm vào chip
// - Nếu value = số (10k...500k) -> cộng dồn
// - Nếu value = 0 -> chỉ xem (reset cược = 0)
// - Nếu value = ALLIN -> currentBet = toàn bộ số dư hiện tại
function selectChip(btn) {
    const value = btn.dataset.value;
    const chips = document.querySelectorAll(".chip");
    chips.forEach(c => c.classList.remove("chip-active"));
    btn.classList.add("chip-active");

    if (value === "ALLIN") {
        const balanceText = document.getElementById("balance").innerText;
        const balance = parseInt(balanceText.replace(/\D/g, ""), 10) || 0;
        currentBetAmount = balance;          // không cộng dồn, cược hết
    } else {
        const amount = parseInt(value, 10) || 0;
        if (amount === 0) {
            // "Chỉ xem" -> reset cược
            currentBetAmount = 0;
        } else {
            // chip thường -> cộng dồn
            currentBetAmount += amount;
        }
    }

    updateBetDisplay();
}

// bấm ĐẶT PHIÊN NÀY: gửi cược cho phiên hiện tại (chỉ set pending, chưa mở kết quả)
function playNow() {
    if (!currentChoice) {
        currentChoice = "tai";
    }

    // cho phép cược 0 (chỉ xem)
    if (currentBetAmount < 0) {
        currentBetAmount = 0;
    }

    fetch(contextPath + "/play", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
        },
        body: "mode=bet"
            + "&choice=" + encodeURIComponent(currentChoice)
            + "&betAmount=" + encodeURIComponent(currentBetAmount)
    })
        .then(resp => resp.json())
        .then(data => {
            if (data.error) {
                alert(data.error);
                return;
            }

            const statusEl = document.getElementById("statusResult");
            statusEl.innerText = data.message;

            // Sau khi đã đặt, KHÔNG reset currentBetAmount
            // để người chơi nhìn lại số đã đặt; lần sau muốn đổi thì bấm chip lại.
        })
        .catch(err => {
            console.error("Error:", err);
        });
}

// tự động mở phiên khi hết 30s
function rollRound() {
    fetch(contextPath + "/play", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8"
        },
        body: "mode=roll"
    })
        .then(resp => resp.json())
        .then(data => {
            if (data.error) {
                console.error(data.error);
                return;
            }

            // xúc xắc + tổng
            document.getElementById("dice1").innerText = data.dice1;
            document.getElementById("dice2").innerText = data.dice2;
            document.getElementById("dice3").innerText = data.dice3;
            document.getElementById("diceSum").innerText =
                data.sum + " điểm - " + data.winningSide;

            // số dư
            document.getElementById("balance").innerText = data.balance;

            // THẮNG / THUA to
            const winLoseEl = document.getElementById("winLose");
            winLoseEl.classList.remove("win-text", "lose-text", "neutral-text");

            if (!data.hasBet) {
                winLoseEl.innerText = "CHỈ XEM";
                winLoseEl.classList.add("neutral-text");
            } else if (data.isWin) {
                winLoseEl.innerText = "THẮNG";
                winLoseEl.classList.add("win-text");
            } else {
                winLoseEl.innerText = "THUA";
                winLoseEl.classList.add("lose-text");
            }

            document.getElementById("statusResult").innerText = data.status;

            addHistoryDot(data.winningSide);

            // Sau khi MỞ phiên xong -> chuẩn bị cho phiên mới:
            // giữ currentBetAmount (người chơi có thể bấm thêm chip tiếp cho phiên sau)
        })
        .catch(err => console.error("Error:", err));
}

// thêm chấm lịch sử (đen = Tài, trắng = Xỉu)
function addHistoryDot(winningSide) {
    const bar = document.getElementById("historyBar");
    const dot = document.createElement("div");
    dot.classList.add("dot");
    if (winningSide === "Tài") {
        dot.classList.add("dot-tai");
    } else {
        dot.classList.add("dot-xiu");
    }
    bar.appendChild(dot);

    if (bar.children.length > 40) {
        bar.removeChild(bar.firstChild);
    }
}

// timer 30 giây
function startTimer() {
    updateCountdown();
    timerId = setInterval(() => {
        countdown--;
        if (countdown <= 0) {
            countdown = 30;
            rollRound(); // tới lúc mở phiên
        }
        updateCountdown();
    }, 1000);
}

function updateCountdown() {
    document.getElementById("countdown").innerText = countdown;
}

window.addEventListener("load", () => {
    setChoice("tai");

    // mặc định chưa chọn chip nào, cược = 0
    currentBetAmount = 0;
    updateBetDisplay();

    startTimer();
});
