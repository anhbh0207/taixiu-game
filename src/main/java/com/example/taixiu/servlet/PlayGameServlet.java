package com.example.taixiu.servlet;

import com.example.taixiu.model.DiceGame;
import com.example.taixiu.util.Account;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class PlayGameServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession();

        // Tài khoản 2tr
        Account account = (Account) session.getAttribute("account");
        if (account == null) {
            account = new Account();
            session.setAttribute("account", account);
        }

        // Lịch sử Tài/Xỉu
        List<DiceGame.Side> history =
                (List<DiceGame.Side>) session.getAttribute("history");
        if (history == null) {
            history = new ArrayList<>();
        }

        String mode = request.getParameter("mode");
        if (mode == null) mode = "roll";     // mặc định: mở phiên

        PrintWriter out = response.getWriter();

        if ("bet".equalsIgnoreCase(mode)) {
            // --------- ĐẶT CƯỢC CHO PHIÊN SẮP TỚI ----------
            String choiceParam = request.getParameter("choice");
            String betParam = request.getParameter("betAmount");

            if (choiceParam == null) {
                out.write("{\"error\":\"Bạn phải chọn Tài hoặc Xỉu!\"}");
                return;
            }

            DiceGame.Side choiceSide =
                    "tai".equalsIgnoreCase(choiceParam) ? DiceGame.Side.TAI : DiceGame.Side.XIU;

            long bet = 0;
            try {
                if (betParam != null && !betParam.isEmpty()) {
                    bet = Long.parseLong(betParam);
                }
            } catch (NumberFormatException e) {
                out.write("{\"error\":\"Tiền cược không hợp lệ!\"}");
                return;
            }

            if (bet < 0) {
                out.write("{\"error\":\"Tiền cược không hợp lệ!\"}");
                return;
            }

            if (bet > 0 && bet > account.getBalance()) {
                out.write("{\"error\":\"Số dư không đủ để cược!\"}");
                return;
            }

            // Lưu “cược chờ” trong session
            session.setAttribute("pendingChoice", choiceSide);
            session.setAttribute("pendingBet", bet);

            String sideStr = (choiceSide == DiceGame.Side.TAI) ? "Tài" : "Xỉu";
            String msg;
            if (bet == 0) {
                msg = "Bạn chọn " + sideStr + " nhưng không đặt tiền (chỉ xem). Chờ hệ thống mở phiên.";
            } else {
                msg = "Bạn đã đặt " + bet + "₫ vào " + sideStr + " cho phiên này. Chờ hệ thống mở sau 30 giây.";
            }

            String json = "{"
                    + "\"betPlaced\":true,"
                    + "\"pendingBet\":" + bet + ","
                    + "\"pendingChoice\":\"" + sideStr + "\","
                    + "\"message\":\"" + escapeJson(msg) + "\","
                    + "\"balance\":" + account.getBalance()
                    + "}";

            out.write(json);
            return;
        }

        // --------- MỞ PHIÊN (đến lúc 30 giây) ----------
        // Sinh kết quả phiên
        DiceGame.Side winningSide = DiceGame.randomWinningSide(history);
        int[] dice = DiceGame.rollDiceForSide(winningSide);
        int sum = dice[0] + dice[1] + dice[2];

        // Lấy cược chờ từ session
        DiceGame.Side pendingChoice = (DiceGame.Side) session.getAttribute("pendingChoice");
        Long pendingBetObj = (Long) session.getAttribute("pendingBet");
        long pendingBet = (pendingBetObj != null) ? pendingBetObj : 0;

        boolean hasBet = pendingBet > 0 && pendingChoice != null;
        boolean isWin = hasBet && (pendingChoice == winningSide);
        long balanceChange = 0;

        if (hasBet) {
            if (isWin) {
                account.add(pendingBet);
                balanceChange = pendingBet;
            } else {
                account.subtract(pendingBet);
                balanceChange = -pendingBet;
            }
        }

        // Clear cược chờ cho phiên mới
        session.setAttribute("pendingChoice", null);
        session.setAttribute("pendingBet", 0L);

        // Cập nhật lịch sử
        history.add(winningSide);
        if (history.size() > 40) {
            history.remove(0);
        }
        session.setAttribute("history", history);
        session.setAttribute("account", account);

        String sideStr = (winningSide == DiceGame.Side.TAI) ? "Tài" : "Xỉu";
        String resultMsg = "Kết quả: " + dice[0] + " - " + dice[1] + " - " + dice[2] +
                " (tổng " + sum + " ⇒ " + sideStr + ")";

        String statusMsg;
        if (!hasBet) {
            statusMsg = "Phiên này bạn không đặt cược.";
        } else if (isWin) {
            String userSideStr = (pendingChoice == DiceGame.Side.TAI) ? "Tài" : "Xỉu";
            statusMsg = "Bạn đặt " + userSideStr + " và ĐÃ THẮNG! Nhận thêm " + pendingBet + "₫.";
        } else {
            String userSideStr = (pendingChoice == DiceGame.Side.TAI) ? "Tài" : "Xỉu";
            statusMsg = "Bạn đặt " + userSideStr + " và đã THUA. Mất " + pendingBet + "₫.";
        }

        String json = "{"
                + "\"result\":\"" + escapeJson(resultMsg) + "\","
                + "\"status\":\"" + escapeJson(statusMsg) + "\","
                + "\"winningSide\":\"" + sideStr + "\","
                + "\"isWin\":" + isWin + ","
                + "\"hasBet\":" + hasBet + ","
                + "\"balanceChange\":" + balanceChange + ","
                + "\"dice1\":" + dice[0] + ","
                + "\"dice2\":" + dice[1] + ","
                + "\"dice3\":" + dice[2] + ","
                + "\"sum\":" + sum + ","
                + "\"balance\":" + account.getBalance()
                + "}";

        out.write(json);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        resp.getWriter().println("POST /play với mode=bet hoặc mode=roll");
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
